package com.italiano2774.nativeapp;
/** Rules shared by grading and the next resumable question. */
public final class CourseAttemptPolicy {
    private CourseAttemptPolicy(){}
    public static boolean isCorrect(boolean gaveUp,boolean answerMatches){return !gaveUp&&answerMatches;}
    public static int resumeIndex(int index,int total,boolean graded){
        int size=Math.max(0,total),current=Math.max(0,Math.min(index,size));
        return graded&&current<size?current+1:current;
    }
}
