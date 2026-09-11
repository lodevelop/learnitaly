package com.italiano2774.nativeapp;
import java.util.ArrayList;
import java.util.List;
/** Device-local navigation history, separate from learning scores. */
public final class RecentPracticeHistory {
    private RecentPracticeHistory(){}
    public static List<String> entries(String saved){
        List<String> result=new ArrayList<>();
        if(saved!=null)for(String key:saved.split(",")){
            if(key.matches("button_(simple|adv)_[a-z_]+")&&!result.contains(key))result.add(key);
            if(result.size()==3)break;
        }
        return result;
    }
    public static String record(String saved,String latest){
        if(latest==null||!latest.matches("button_(simple|adv)_[a-z_]+"))return String.join(",",entries(saved));
        List<String> keys=entries(saved);keys.remove(latest);keys.add(0,latest);
        return String.join(",",keys.subList(0,Math.min(3,keys.size())));
    }
}
