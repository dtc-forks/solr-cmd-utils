package de.tblsoft.solr.pipeline.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import de.tblsoft.solr.crawl.JSoupAnalyzer;
import de.tblsoft.solr.crawl.Webpage;
import de.tblsoft.solr.crawl.attr.Attribute;
import de.tblsoft.solr.crawl.attr.Attributes;
import de.tblsoft.solr.http.ElasticHelper;
import de.tblsoft.solr.pipeline.AbstractFilter;
import de.tblsoft.solr.pipeline.bean.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public class HtmlFilter extends AbstractFilter {

    private static Logger LOG = LoggerFactory.getLogger(HttpWorker.class);

    protected String html;

    protected String htmlField;
    protected String urlField;

    private Map<String, String> webPageMapping;


	@Override
	public void init() {

        htmlField = getProperty("htmlField", "html");
        urlField = getProperty("urlField", "url");
        List<String> mappingConfiguration = getPropertyAsList("mapping", new ArrayList<String>());
        webPageMapping = readConfig(mappingConfiguration, "webpage");
		super.init();
	}


    private Map<String, String> readConfig(List<String> mappingConfiguration, String prefix) {
        Map<String, String> mapping = new HashMap<>();
        for (String v : mappingConfiguration) {
            if(!v.startsWith(prefix)) {
                continue;
            }
            v = v.replaceFirst(prefix + ":", "");
            String[] s = v.split("->");
            mapping.put(s[0], s[1]);
        }

        return mapping;
    }



	@Override
	public void document(Document document) {
	    String url = document.getFieldValue(urlField);
	    String html = document.getFieldValue(htmlField);

        JSoupAnalyzer jSoupAnalyzer = new JSoupAnalyzer(url, html);

        Attributes attributes = new Attributes();
        attributes.setStrategy("tableThTd");
        attributes.setSelector(Arrays.asList("table.techdata"));
        jSoupAnalyzer.analyze();
        jSoupAnalyzer.extractAttributes(attributes);

        Webpage webpage = jSoupAnalyzer.getWebpage();
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        try {
            objectMapper.writeValue(writer, webpage);
            DocumentContext context = JsonPath.
                    using(
                            Configuration.
                                    defaultConfiguration().
                    addOptions(Option.SUPPRESS_EXCEPTIONS)).
                            parse(writer.toString());

            for (Map.Entry<String, String> mapping : webPageMapping.entrySet()) {
                String jsonPath = mapping.getKey();
                String fieldName = mapping.getValue();
                Object value = context.read(jsonPath);
                document.setField(fieldName, value);
            }

            if (webpage.getAttributes() != null) {
                for (Attribute attribute : webpage.getAttributes().getAttributes()) {
                    String key = ElasticHelper.normalizeKey(attribute.getName());
                    document.setField("attr_" + key, attribute.getValue());
                    document.addField("datatypes", key + "=" + ElasticHelper.guessDatatype(attribute.getValue()));
                    document.addField("attributeKeys", key);
                    document.addField("attributeKeysOriginal", attribute.getName());
                }
            }

            super.document(document);
        } catch (IOException e) {
            e.printStackTrace();
        }



	}






}