package com.italiano2774.nativeapp;

/**
 * v5.0 beginner shell. It does not change the real learning algorithms; it only controls
 * how much complexity is exposed to the learner during the first seven active study days.
 */
public final class BeginnerGuideEngine {
    private BeginnerGuideEngine(){}

    public static boolean active(ProgressStore p){return p!=null&&p.firstWeekGuidanceActive();}
    public static int day(ProgressStore p){return p==null?8:p.firstWeekLearningDay();}

    public static String coachLine(ProgressStore p,DailySmartPlan plan,DailySmartTask next){
        if(p==null)return "只需要完成系统安排的下一步。";
        if(active(p)){
            switch(day(p)){
                case 1:return LearningLanguage.ui(p,"第1天 · 先完成今天安排，不需要研究所有功能。先建立每天学习的习惯。","Day 1 · Just complete today's plan. You do not need to explore every feature yet; build the daily habit first.");
                case 2:return LearningLanguage.ui(p,"第2天 · 今天开始多听意大利语。先听懂，再追求说得快。","Day 2 · Listen to more Italian today. Understand first; speed can come later.");
                case 3:return LearningLanguage.ui(p,"第3天 · 今天开始主动开口。说不完整也没关系，系统会保留真正的薄弱点。","Day 3 · Start speaking actively today. Incomplete answers are fine; the system will keep track of real weak points.");
                case 4:return LearningLanguage.ui(p,"第4天 · 做错的词和句子会自动回来。重新做对，比反复看答案更重要。","Day 4 · Wrong words and sentences will come back automatically. Correcting them from memory matters more than rereading answers.");
                case 5:return LearningLanguage.ui(p,"第5天 · 开始把学过的表达放进真实生活场景，目标是能把事情办成。","Day 5 · Start using what you learned in real-life scenarios. The goal is to get things done in Italian.");
                case 6:return LearningLanguage.ui(p,"第6天 · 系统已经开始根据你的真实表现调整听力、拼写、口语和复习量。","Day 6 · The system now adjusts listening, spelling, speaking and review load from your real performance.");
                default:return LearningLanguage.ui(p,"第7天 · 完成今天后，完整周测和学习分析会接管后续节奏。你仍然只需要点“继续学习”。","Day 7 · After today's plan, the full weekly check and learning analysis will guide the next cycle. You still only need to tap Continue learning.");
            }
        }
        if(plan!=null&&plan.recoveryMode)return LearningLanguage.ui(p,"今天先把容易忘和反复错的内容处理掉，系统已经自动减少新内容。","Today starts with easily forgotten and repeatedly missed material; new content has been reduced automatically.");
        if(next!=null)return LearningLanguage.ui(p,"系统已经按今天的复习压力和薄弱项排好顺序。完成这一项，回来继续下一项即可。","Today's tasks are already ordered by review load and weak areas. Finish this one, then return for the next.");
        return LearningLanguage.ui(p,"今天的主任务已经完成。可以查看总结，也可以停止学习，让记忆休息和巩固。","Today's main tasks are complete. You can view the summary or stop and let the memory consolidate.");
    }

    public static String practiceHint(ProgressStore p){
        if(!active(p))return LearningLanguage.ui(p,"主课程已经自动安排复习。这里只在你想额外练某一项时使用。","Your main course already schedules reviews automatically. Use this page only when you want extra practice in a specific skill.");
        return LearningLanguage.ui(p,"第"+day(p)+"个学习日 · 系统会在每日计划里自动安排需要的训练；额外练习会逐日开放，不需要一次学会所有功能。","Study day "+day(p)+" · Required practice is scheduled automatically in the daily plan. Extra tools unlock gradually; you do not need to learn every feature at once.");
    }

    public static boolean practiceUnlocked(ProgressStore p,int unlockDay){
        return !active(p)||day(p)>=Math.max(1,unlockDay);
    }

    public static String lockedLabel(ProgressStore p,String labelZh,String labelEn,int unlockDay){
        return LearningLanguage.ui(p,"🔒 "+labelZh+" · 第"+unlockDay+"天开放","🔒 "+labelEn+" · Unlocks on day "+unlockDay);
    }
}
