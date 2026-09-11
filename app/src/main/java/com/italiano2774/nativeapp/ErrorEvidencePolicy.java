package com.italiano2774.nativeapp;

/**
 * Keeps the personal wrong-sentence notebook limited to real sentence output.
 * Word-choice/course/grammar analytics still stay in error history, but never become
 * "rewrite this sentence" tasks.
 */
public final class ErrorEvidencePolicy {
    private static final String PROMPT_MARKER="\n<<<ERROR_PROMPT>>>\n";
    private ErrorEvidencePolicy(){}

    /** Store the original learner-facing prompt inside the existing detail field.
     *  This keeps Room schema compatibility while making future repair cards self-contained. */
    public static String detailWithPrompt(String detail,String prompt){
        String d=detail==null?"":detail.trim();String p=prompt==null?"":prompt.trim();
        if(p.isEmpty())return d;
        return d+PROMPT_MARKER+p.replace(PROMPT_MARKER," ");
    }

    public static String extractPrompt(String detail){
        if(detail==null)return "";int i=detail.indexOf(PROMPT_MARKER);
        return i<0?"":detail.substring(i+PROMPT_MARKER.length()).trim();
    }

    public static boolean isRepairEligible(String mode,int wordId,String expected,String actual,String detail){
        if(expected==null||expected.trim().isEmpty())return false;
        String m=mode==null?"":mode.trim();
        if("writing".equals(m)||"freechat".equals(m)||"daily_speaking".equals(m)||
                "intensive_listening".equals(m)||"listening_course".equals(m)||
                "sentence_dictation".equals(m)||"shadowing".equals(m)||
                "dialogue_beginner".equals(m)||"dialogue_intermediate".equals(m)||
                "dialogue_advanced".equals(m)||"listen_speak_output".equals(m)||
                "weekly_exam_output".equals(m))return true;
        // Historical rows before v5.0.2 used shared mode names. Only keep the rows
        // that can be proven to come from productive Italian output.
        String d=detail==null?"":detail;
        if("listen_speak".equals(m))return wordId==0&&d.contains("语音识别内容匹配");
        if("weekly_exam".equals(m))return wordId==0&&(d.startsWith("周测口语")||d.startsWith("周测真实使用"));
        return false;
    }

    public static String modeLabel(String mode){
        String m=mode==null?"":mode;
        if("writing".equals(m))return "真实写作";
        if("freechat".equals(m))return "自由会话";
        if("daily_speaking".equals(m))return "每日5句";
        if("intensive_listening".equals(m))return "精听听写";
        if("listening_course".equals(m))return "听力课程";
        if("sentence_dictation".equals(m))return "句子听写";
        if("shadowing".equals(m))return "Shadowing裸说";
        if("dialogue_beginner".equals(m))return "生活场景 · 基础应对";
        if("dialogue_intermediate".equals(m))return "生活场景 · 独立表达";
        if("dialogue_advanced".equals(m))return "生活场景 · 真实实战";
        if("listen_speak_output".equals(m)||"listen_speak".equals(m))return "听说训练 · 开口";
        if("weekly_exam_output".equals(m)||"weekly_exam".equals(m))return "每周实战 · 主动表达";
        return "真实表达练习";
    }

    public static String attemptText(ErrorRecordEntity e){
        if(e==null)return "";
        String a=e.actual==null?"":e.actual.trim();
        if(!a.isEmpty())return a;
        String m=e.mode==null?"":e.mode;
        if("daily_speaking".equals(m))return "（当时选择了不会，未说出完整句子）";
        if("shadowing".equals(m)||m.startsWith("dialogue_")||m.startsWith("listen_speak")||m.startsWith("weekly_exam"))return "（当时没有识别到完整的意大利语表达）";
        return "（当时没有完成这句意大利语）";
    }

    public static String cleanDetail(String detail){
        if(detail==null)return "";
        String d=detail;int marker=d.indexOf(PROMPT_MARKER);if(marker>=0)d=d.substring(0,marker);d=d.trim();
        if(d.startsWith("unit=")||d.contains("course_v"))return "";
        return d;
    }
}
