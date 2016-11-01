package de.tblsoft.solr.pipeline.filter;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import de.tblsoft.solr.logic.LinguisticHelper;
import de.tblsoft.solr.pipeline.AbstractFilter;
import de.tblsoft.solr.pipeline.bean.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tblsoft 17.05.16.
 */
public class CompoundWordFilter extends AbstractFilter {

    private String nounnFieldName;

    private Set<String> nounList = new HashSet<String>();
    
    private static Set<String> blacklist = new HashSet<String>();
    static {
    	blacklist.add("euch");
    	blacklist.add("herb");
    }
    
    private static Set<String> whitelist = new HashSet<String>();
    static {
    	whitelist.add("leucht");
    	whitelist.add("bohrung");
    	whitelist.add("alu");
    	whitelist.add("dampf");
    	whitelist.add("kasten");
    	whitelist.add("natrium");
    	whitelist.add("bau");
    	whitelist.add("leiste");
    	whitelist.add("schutz");
    	whitelist.add("tor");
    }

    @Override
    public void init() {
        nounnFieldName = getProperty("nounnFieldName", "noun");
        super.init();
    }

    @Override
    public void document(Document document) {
        String noun = document.getFieldValue(nounnFieldName);
        if(Strings.isNullOrEmpty(noun)) {
        	return;
        }
        if(blacklist.contains(noun)) {
        	return;
        }
        if(LinguisticHelper.containsOnlyGermanCharacters(noun) && noun.length() > 3) {
        		nounList.add(noun.toLowerCase());
        }
    }

    @Override
    public void end() {
    	nounList.addAll(whitelist);
        for(String noun: nounList) {
            List<String> compoundList = new ArrayList<String>();
            for(String compound: nounList) {
            	if(noun.contains(compound) && !noun.equals(compound)) {
            		int diff = Math.abs(compound.length() - noun.length());
            		if(diff > 3) {
            			compoundList.add(compound);
            		}
                }
            }
            if(!compoundList.isEmpty()) {
                Document document = new Document();
                document.addField("noun",noun);
                for(String compound: compoundList) {
                	document.addField("compound", compound);
//                	if(!isOverlap(compound, compoundList)) {
//                		document.addField("compound", compound);
//                	}
                	
                }
                List<String> tokens = tokenize(noun, compoundList);
                List<String> additionalTokens = new ArrayList<String>();
//                for(String token:tokens) {
//                	additionalTokens.addAll(tokenize(token, compoundList));
//                }
                tokens.addAll(additionalTokens);
                
                String joinedTokens = Joiner.on(" ").join(tokens);
                document.addField("tokenized", joinedTokens);
                super.document(document);
            }
        }
        super.end();
    }
    
    List<String> tokenize(String noun, List<String> compoundList) {
    	List<String> ret = new ArrayList<String>();
    	StringBuilder b = new StringBuilder();

    	for (int i = 0; i < noun.length(); i++) {
			b.append(noun.charAt(i));
			String part = b.toString();
			String candidate = null;
			boolean startsWith = false;
			for(String compound : compoundList) {
				if(compound.equals(part)) {
					candidate = compound;
					continue;
				}
				if(compound.startsWith(part)) {
					startsWith = true;
				}
			}
			if(candidate != null && startsWith == false) {
				ret.add(part);
				candidate = null;
				b = new StringBuilder();
				continue;
				
			}
			if(startsWith == false) {
				if(part.length() > 1) {
					i = i - part.length() +1;
				}
				b = new StringBuilder();
			}
			
		}
    	return ret;
    }
    
    boolean isOverlap(String compoundToCompare, List<String> compoundList) {
    	for(String compound: compoundList) {
    		if(compoundToCompare.equals(compound)) {
    			continue;
    		}
    		if(compound.contains(compoundToCompare)) {
    			return true;
    		}
    		
    	}
    	return false;
    	
    }
}
