package de.tblsoft.solr.logic;


import com.google.common.base.Strings;
import de.tblsoft.solr.parser.SolrXmlParser;
import de.tblsoft.solr.util.IOUtils;
import de.tblsoft.solr.util.OutputStreamStringBuilder;

import java.io.OutputStream;
import java.util.*;

/**
 * @deprecated use the NounExtractorFilter instead.
 */
@Deprecated
public class NounExtractor extends SolrXmlParser {

    private Set<String> dictionary = new HashSet<String>();

    private String outputFileName;

    private List<String> fieldWhiteList;

    private List<String> fieldBlackList;


    public void extractNouns() throws Exception {
        parse();
        List<String> dictionaryList = new ArrayList<String>(dictionary);
        Collections.sort(dictionaryList);
        OutputStream out = IOUtils.getOutputStream(outputFileName);
        OutputStreamStringBuilder dict = new OutputStreamStringBuilder(out);
        for (String value : dictionaryList) {
            dict.append(value);
            dict.append("\n");
        }
        out.close();
    }

    @Override
    public void field(String name, String value) {
        if(isFieldIncluded(name)) {
            dictionary.addAll(tokenize(value));
        }
    }

    boolean isFieldIncluded(String name) {
        if(fieldBlackList != null){
            for(String item :fieldBlackList){
                if(name.matches(item)) {
                    return false;
                }
            }
        }

        if(fieldWhiteList != null) {
            for(String item :fieldWhiteList){
                if(name.matches(item)) {
                    return true;
                }
            }
            return false;
        }

        return true;

    }

    boolean isFirstCharUpperCase(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return false;
        }

        if (value.length() < 3) {
            return false;
        }
        char v = value.charAt(0);
        return Character.isUpperCase(v);
    }

    boolean containsOnlyGermanCharacters(String value) {
        return value.matches("[A-Za-zÖÄÜöäüß]*");
    }


    Set<String> tokenize(String value) {
        StringTokenizer tokenizer = new StringTokenizer(value);
        Set<String> values = new HashSet<String>();
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();

            if (isFirstCharUpperCase(token) && containsOnlyGermanCharacters(token)) {
                values.add(token.toLowerCase());
            }
        }

        return values;

    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }
}

