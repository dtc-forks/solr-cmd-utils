package de.tblsoft.solr.pipeline.filter;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tblsoft.solr.http.ElasticHelper;
import de.tblsoft.solr.http.HTTPHelper;
import de.tblsoft.solr.pipeline.AbstractFilter;
import de.tblsoft.solr.pipeline.bean.Document;
import de.tblsoft.solr.pipeline.bean.Field;
import de.tblsoft.solr.util.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JsonWriter extends AbstractFilter {


    private Gson gson;

    private String type;

    private String location;
    
    private String elasticMappingLocation;
    
    private boolean delete;




    @Override
    public void init() {
        type = getProperty("type", "file");
        Boolean pretty = getPropertyAsBoolean("pretty", false);
        location = getProperty("location", null);
        verify(location, "For the JsonWriter a location must be defined.");

        delete = getPropertyAsBoolean("delete", Boolean.TRUE);
        elasticMappingLocation = getProperty("elasticMappingLocation", null);


        GsonBuilder builder = new GsonBuilder();
        if(pretty) {
            builder = builder.setPrettyPrinting();
        }
        gson = builder.create();
        
        
        if("elastic".equals(type)) {
            if(delete) {
                try {
                    String indexUrl = ElasticHelper.getIndexUrl(location);
                    HTTPHelper.delete(indexUrl);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        	if(elasticMappingLocation != null) {
        		String mappingJson;
				try {
					String indexUrl = ElasticHelper.getIndexUrl(location);
                    File elasticMappingFile = new File(IOUtils.getAbsoluteFile(getBaseDir(),elasticMappingLocation));

					mappingJson = FileUtils.readFileToString(elasticMappingFile);
            		HTTPHelper.put(indexUrl, mappingJson);
				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }

        	}
        } 


        super.init();
    }


    Object transformDatatype(List<String> values) {
        List<Long> longList = new ArrayList<Long>();
        for(String value: values) {
            Object transformedValue = transformDatatype(value);
            if(transformedValue instanceof Long) {
                longList.add((Long) transformedValue);
            } else {
                return values;
            }
        }
        return longList;
    }

    Object transformDatatype(String value) {
        if(NumberUtils.isNumber(value)) {
            try {
                Long intValue = Long.valueOf(value);
                return intValue;
            } catch (NumberFormatException e) {
                return value;
            }
        }
        return value;
    }



    @Override
    public void document(Document document) {

        Map<String, Object> jsonDocument = mapToJson(document);

        if(!jsonDocument.isEmpty()) {

            String json = gson.toJson(document);



            if("elastic".equals(type)) {
                HTTPHelper.post(location, json);
            } else if ("file".equals(type)) {
                try {
                    FileUtils.writeStringToFile(new File("foo.txt"), json, true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if ("stdout".equals(type)) {
                System.out.println(json);
            }



        }

        super.document(document);
    }


    Map<String, Object> mapToJson(Document document) {
        Map<String, Object> jsonDocument = new HashMap<String, Object>();
        for(Field field: document.getFields()) {
            List<String> values = field.getValues();
            if(values == null || values.isEmpty()) {
                continue;
            }
            if(values.size() == 1){
                jsonDocument.put(field.getName(), transformDatatype(field.getValue()));
            } else {
                jsonDocument.put(field.getName(), field.getValues());
            }

        }



        return jsonDocument;

    }



    @Override
    public void end() {
        super.end();
    }



}

