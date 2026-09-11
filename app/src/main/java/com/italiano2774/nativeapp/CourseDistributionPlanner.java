package com.italiano2774.nativeapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * v5.2.0 fixed-course exposure planner.
 *
 * Goals for one normal pass through a fixed unit:
 * 1) every unit word is introduced exactly once before the challenge;
 * 2) same-unit reinforcement is spaced and capped at once per word;
 * 3) reinforcement never uses words from another unit;
 * 4) the challenge favors under-exposed words instead of repeatedly sampling lucky words;
 * 5) normal lessons stay >=75% current-focus material when reinforcement is present.
 *
 * Global FSRS/due review deliberately lives outside the fixed course path.
 */
public final class CourseDistributionPlanner {
    private CourseDistributionPlanner(){}

    public static int preChallengeLessons(CourseUnit unit){
        return unit==null?1:Math.max(1,unit.lessonCount-1);
    }

    public static List<Integer> focusIds(CourseUnit unit,int lessonIndex){
        List<Integer> out=new ArrayList<>();
        if(unit==null||unit.wordIds.isEmpty())return out;
        int chunks=preChallengeLessons(unit);
        int lesson=Math.max(0,Math.min(chunks-1,lessonIndex));
        int n=unit.wordIds.size();
        int a=(int)Math.floor(lesson*n/(double)chunks);
        int b=(int)Math.floor((lesson+1)*n/(double)chunks);
        if(b<=a)b=Math.min(n,a+1);
        for(int i=Math.min(a,n);i<Math.min(b,n);i++)out.add(unit.wordIds.get(i));
        return out;
    }

    /** Same-unit review budget keeps >=75% of a normal lesson on its current focus. */
    public static int reinforcementBudget(CourseUnit unit,int lessonIndex){
        int focus=focusIds(unit,lessonIndex).size();
        if(focus<3)return 0;
        return Math.min(2,Math.max(1,focus/3));
    }

    /**
     * Returns spaced same-unit reinforcement for this lesson. A word is reviewed at most once
     * before the challenge and normally gets at least one full lesson of spacing after first use.
     */
    public static List<Integer> reinforcementIds(CourseUnit unit,int lessonIndex){
        List<Integer> out=new ArrayList<>();
        if(unit==null||unit.wordIds.isEmpty())return out;
        int chunks=preChallengeLessons(unit);
        int targetLesson=Math.max(0,Math.min(chunks-1,lessonIndex));
        Set<Integer> alreadyReviewed=new HashSet<>();
        for(int lesson=0;lesson<=targetLesson;lesson++){
            int budget=reinforcementBudget(unit,lesson);
            if(budget<=0)continue;
            List<Integer> eligible=new ArrayList<>();
            for(Integer wid:unit.wordIds){
                if(wid==null||alreadyReviewed.contains(wid))continue;
                int first=focusLesson(unit,wid);
                // Never reinforce before introduction and normally keep a one-lesson gap.
                if(first>=0&&first<=lesson-2)eligible.add(wid);
            }
            final int currentLesson=lesson;
            Collections.sort(eligible,Comparator.comparingLong(wid->stableScore(unit,currentLesson,wid)));
            List<Integer> chosen=new ArrayList<>();
            for(Integer wid:eligible){
                chosen.add(wid);alreadyReviewed.add(wid);
                if(chosen.size()>=budget)break;
            }
            if(lesson==targetLesson)out.addAll(chosen);
        }
        return out;
    }

    /** Challenge favors words that only had their one required focus exposure. */
    public static List<Integer> challengeIds(CourseUnit unit,int limit){
        List<Integer> out=new ArrayList<>();
        if(unit==null||unit.wordIds.isEmpty()||limit<=0)return out;
        Set<Integer> reviewed=new HashSet<>();
        int chunks=preChallengeLessons(unit);
        for(int lesson=0;lesson<chunks;lesson++)reviewed.addAll(reinforcementIds(unit,lesson));
        List<Integer> pool=new ArrayList<>();
        for(Integer wid:unit.wordIds)if(!reviewed.contains(wid))pool.add(wid);
        // Current curriculum always leaves at least 9 under-exposed words. If a future very tiny
        // unit does not, keep a minimum viable challenge by adding the least-repeated remainder.
        if(pool.size()<Math.min(6,limit))for(Integer wid:unit.wordIds)if(reviewed.contains(wid))pool.add(wid);
        Collections.sort(pool,(a,b)->{
            int ea=1+(reviewed.contains(a)?1:0),eb=1+(reviewed.contains(b)?1:0);
            if(ea!=eb)return Integer.compare(ea,eb);
            int la=focusLesson(unit,a),lb=focusLesson(unit,b);
            int recentA=la>=chunks-1?1:0,recentB=lb>=chunks-1?1:0;
            if(recentA!=recentB)return Integer.compare(recentA,recentB);
            return Long.compare(stableScore(unit,chunks,a),stableScore(unit,chunks,b));
        });
        for(Integer wid:pool){out.add(wid);if(out.size()>=Math.min(limit,pool.size()))break;}
        return out;
    }

    public static int focusLesson(CourseUnit unit,int wordId){
        if(unit==null)return -1;
        int chunks=preChallengeLessons(unit);
        for(int lesson=0;lesson<chunks;lesson++)if(focusIds(unit,lesson).contains(wordId))return lesson;
        return -1;
    }

    public static int scheduledExposureCount(CourseUnit unit,int wordId){
        int count=focusLesson(unit,wordId)>=0?1:0;
        int chunks=preChallengeLessons(unit);
        for(int lesson=0;lesson<chunks;lesson++)if(reinforcementIds(unit,lesson).contains(wordId))count++;
        if(challengeIds(unit,Math.min(10,unit==null?0:unit.wordIds.size())).contains(wordId))count++;
        return count;
    }

    private static long stableScore(CourseUnit unit,int lesson,int wordId){
        long x=(long)wordId*1103515245L+(long)(lesson+1)*12345L+(long)(unit.index+1)*2654435761L;
        long mod=2147483647L;long r=x%mod;return r<0?r+mod:r;
    }
}
