package de.tblsoft.solr.pipeline.filter;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import de.tblsoft.solr.http.UrlUtil;
import de.tblsoft.solr.pipeline.bean.Field;
import de.tblsoft.solr.util.DateUtils;
import de.tblsoft.solr.util.MapUtils;
import de.tblsoft.solr.util.PairBuilder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by tblsoft on 23.09.17.
 */
public class SimpleMapping {


    private Map<String, List<String>> mapping = new HashMap<String, List<String>>();
    private Map<String, List<String>> mappingFunctions = new HashMap<String, List<String>>();
    private Map<String, String> joins = new HashMap<String, String>();
    private Map<String, String> config = new HashMap<>();


    private List<String> mappingDefinition;

    private List<String> mappingConfiguration;

    public SimpleMapping() {

    }

    public SimpleMapping(List<String> mappingDefinition, List<String> mappingConfiguration) {
        this.mappingConfiguration = mappingConfiguration;
        this.mappingDefinition = mappingDefinition;
        readConfig();
    }

    private void readConfig() {
        for(String configDefinition : mappingConfiguration) {
            Pair<String, String> p = PairBuilder.createPair(configDefinition, "=");
            config.put(p.getLeft(), p.getValue());
        }


        for (String v : mappingDefinition) {
            if (v.startsWith("join:")) {
                v = v.replace("join:", "");
                String[] s = v.split("=", 2);
                joins.put(s[0], s[1]);
            } else {
                String[] s = v.split("->");

                String[] f = s[1].split(Pattern.quote("|"));

                List<String> mappingList = mapping.get(s[0]);
                if (mappingList == null) {
                    mappingList = new ArrayList<String>();
                }
                mappingList.add(f[0]);
                mapping.put(s[0], mappingList);
                List<String> functions = new ArrayList<String>();
                for (int i = 1; i < f.length; i++) {
                    functions.add(f[i]);
                }
                mappingFunctions.put(f[0], functions);
            }
        }
    }


    public String executeFunction(String function, String value) {
        if(Strings.isNullOrEmpty(function)) {
            return value;
        }

        if("md5".equals(function)) {
            return DigestUtils.md5Hex(value);
        } else if ("mapGermanChars".equals(function)) {
            return mapGermanChars(value);
        } else if ("mapFranceChars".equals(function)) {
            return mapFranceChars(value);
        } else if ("lowercase".equals(function)) {
            return StringUtils.lowerCase(value);
        } else if ("urlencode".equals(function)) {
            return UrlUtil.encode(value);
        } else if ("urldecode".equals(function)) {
            return UrlUtil.decode(value);
        } else if ("trim".equals(function)) {
            return StringUtils.trim(value);
        }else if ("removeWhitespace".equals(function)) {
            return value.replaceAll(" ", "");
        } else if ("toSolrDate".equals(function)) {
            return DateUtils.toSolrDate(value);
        } else if ("uniq".equals(function)) {
            return value;
        } else if ("removeSpecialChars".equals(function)) {
            value = value.replaceAll("[^a-zA-Z0-9']+", " ");
            return value;
        } else if ("leftPad".equals(function)) {
            int size = Integer.valueOf(MapUtils.getOrDefault(config,"leftPad.size", "10" ));
            String padChar = MapUtils.getOrDefault(config,"leftPad.padChar", "0");
            return StringUtils.leftPad(value, size, padChar );
    }

        throw new IllegalArgumentException("The function: " + function
                + " is not implemented.");
    }


    public static void executeFieldFunction(String function, Field field) {

        if("md5".equals(function)) {
            return;
        } else if ("mapGermanChars".equals(function)) {
            return;
        } else if ("mapFranceChars".equals(function)) {
            return;
        }else if ("lowercase".equals(function)) {
            return;
        } else if ("urlencode".equals(function)) {
            return;
        } else if ("urldecode".equals(function)) {
            return;
        } else if ("trim".equals(function)) {
            return;
        } else if ("removeWhitespace".equals(function)) {
            return;
        } else if ("toSolrDate".equals(function)) {
            return;
        } else if ("uniq".equals(function)) {
            Set<String> uniqValues= new HashSet<String>();
            uniqValues.addAll(field.getValues());
            field.setValues(new ArrayList(uniqValues));
            return;
        } else if ("removeSpecialChars".equals(function)) {
            return;
        } else if ("leftPad".equals(function)) {
            return;
        }

        throw new IllegalArgumentException("The function: " + function
                + " is not implemented.");
    }


    public Map<String, List<String>> getMapping() {
        return mapping;
    }

    public Map<String, List<String>> getMappingFunctions() {
        return mappingFunctions;
    }

    public Map<String, String> getJoins() {
        return joins;
    }


    static String mapGermanChars(String value) {
        value = value.replaceAll("\u00c4", "Ae");
        value = value.replaceAll("\u00d6", "Oe");
        value = value.replaceAll("\u00dc", "Ue");
        value = value.replaceAll("\u00e4", "ae");
        value = value.replaceAll("\u00f6", "oe");
        value = value.replaceAll("\u00fc", "ue");
        value = value.replaceAll("\u00df", "ss");
        return value;
    }

    static String mapFranceChars(String value) {
        value = value.replaceAll("\u00c8", "E"); // È
        value = value.replaceAll("\u00e8", "e"); // è
        value = value.replaceAll("\u00c9", "E"); // É
        value = value.replaceAll("\u00e9", "e"); // é
        value = value.replaceAll("é", "e"); // é
        value = value.replaceAll("\u00c0", "A"); // À
        value = value.replaceAll("\u00e0", "a"); // à
        value = value.replaceAll("\u00d9", "U"); // Ù
        value = value.replaceAll("\u00f9", "u"); // ù
        value = value.replaceAll("\u00c2", "A"); // Â
        value = value.replaceAll("\u00e2", "a"); // â
        value = value.replaceAll("\u00ca", "E"); // Ê
        value = value.replaceAll("\u00ea", "e"); // ê
        value = value.replaceAll("\u00ce", "I"); // Î
        value = value.replaceAll("\u00ee", "i"); // î
        value = value.replaceAll("\u00d4", "O"); // Ô
        value = value.replaceAll("\u00f4", "o"); // ô
        value = value.replaceAll("\u00db", "U"); // Û
        value = value.replaceAll("\u00fb", "u"); // û
        value = value.replaceAll("\u00c7", "C"); // Ç
        value = value.replaceAll("\u00e7", "c"); // ç
        value = value.replaceAll("\u00cb", "E"); // Ë
        value = value.replaceAll("\u00eb", "e"); // ë
        value = value.replaceAll("\u00cf", "I"); // Ï
        value = value.replaceAll("\u00ee", "i"); // î
        value = value.replaceAll("\u00dc", "U"); // Ü
        value = value.replaceAll("\u00fb", "u"); // û
        return value;
    }
}
