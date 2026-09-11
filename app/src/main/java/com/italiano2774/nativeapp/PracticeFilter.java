package com.italiano2774.nativeapp;

import java.text.Normalizer;
import java.util.Locale;

/** Offline search; filtering never changes a practice's unlock status. */
public final class PracticeFilter {
    private PracticeFilter() {}
    public static String normalize(String value) {
        if(value==null)return "";
        return Normalizer.normalize(value,Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
    }
    public static boolean matches(String query,String category,String itemCategory,
                                  boolean unlockedOnly,boolean unlocked,String label,String keywords) {
        if(unlockedOnly&&!unlocked)return false;
        if(!"all".equals(category)&&!category.equals(itemCategory))return false;
        String haystack=normalize(label+" "+keywords);
        String search=normalize(query);
        if(search.isEmpty())return true;
        for(String token:search.split("\\s+"))if(!haystack.contains(token))return false;
        return true;
    }
}
