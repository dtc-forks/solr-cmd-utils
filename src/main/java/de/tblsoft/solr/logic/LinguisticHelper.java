package de.tblsoft.solr.logic;

public class LinguisticHelper {
	
	
	
    public static boolean containsOnlyGermanCharacters(String value) {
        return value.matches("[A-Za-zÖÄÜöäüß]*");
    }


}
