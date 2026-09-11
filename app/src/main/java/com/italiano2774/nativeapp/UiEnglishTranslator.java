package com.italiano2774.nativeapp;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Lightweight offline UI translation for legacy hard-coded Chinese labels. */
public final class UiEnglishTranslator {
    private static final Map<String,String> EXACT=new LinkedHashMap<>();
    private static final Map<String,String> PART=new LinkedHashMap<>();
    static{
        put("不会，查看答案","Don't know \u00b7 Show answer");
        put("清除搜索和筛选","Clear search and filters");
        put("最近使用","Recently used");
        put("返回学习路径","Return to learning path");
        put("播放意大利语发音","Play Italian pronunciation");

        put("今天准备学习多久（分钟）","Daily target in minutes");
        put("今天的意大利语","Today's Italian");
        put("背单词","Practice vocabulary");
        put("查看复习日历","Review calendar");
        put("搜索练习，例如 听力 / 拼写","Search practice, e.g. listening / spelling");
        put("全部","All");
        put("听说","Listen & speak");
        put("词句","Words & sentences");
        put("语法","Grammar");
        put("实战","Real life");
        put("只看已解锁","Show unlocked only");
        put("没有符合条件的练习，试试其他关键词或分类。","No matching practice. Try another keyword or category.");
        put("词汇与复习","Vocabulary & review");
        put("语法练习","Grammar practice");
        put("生活实战","Real-life practice");
        put("今天准备学习多久","Daily target in minutes");

        put("再看一次正确表达","Show the correct expression again");
        put("不会 · 显示正确表达","Don't know \u00b7 Show the correct expression");
        put("记住了 · 遮住答案重写","Ready \u00b7 Hide the answer and rewrite");
        put("答案已遮住。请凭记忆重新写一遍，再检查。","The answer is hidden. Rewrite it from memory, then check.");
        put("这次只算查看答案，不算修复。看完后点击“记住了 · 遮住答案重写”，再凭记忆输入。","Viewing the answer does not count as a repair. When ready, hide the answer and rewrite it from memory.");

        put("学习设置","Learning settings");put("日常设置","Daily settings");put("每天学习时长","Daily study time");put("中文近似读音","Chinese pronunciation hint");put("发音速度","Audio speed");put("界面字号","Text size");put("保存设置","Save settings");put("高级设置 ▾","Advanced settings ▾");put("收起高级设置 ▴","Hide advanced settings ▴");put("高级课程参数","Advanced course settings");put("学习目标","Learning goal");put("入门水平测试","Placement test");put("进行 / 重做水平测试","Take / retake placement test");put("旧版词汇路线","Vocabulary route");put("智能背词每日新词数","Daily new words");put("旧版课程开始日期","Legacy course start date");put("进度与数据","Progress & data");put("导出进度 JSON","Export progress JSON");put("导入进度 JSON","Import progress JSON");put("立即创建本地备份","Create local backup now");put("恢复最近本地备份","Restore latest local backup");put("数据库健康与错误日志","Database health & error log");put("待检查内容","Items to review");put("清空学习进度","Reset learning progress");
        put("首页使用简洁模式（推荐）","Use simple home screen (recommended)");put("每天提醒我学习","Remind me to study every day");put("联网时优先播放原始在线音频","Prefer original online audio when connected");
        put("从第一课开始","Start from lesson 1");put("你主要为什么学意大利语？","Why are you learning Italian?");put("每天准备学习多久？","How long will you study each day?");put("从 A0 第一课开始","Start from A0 lesson 1");put("我学过一些，先做水平测试","I know some Italian — take a placement test first");put("使用推荐设置：每天20分钟","Use recommended setting: 20 minutes/day");
        put("今天","Today");put("进度","Progress");put("练习","Practice");put("词库","Vocabulary");put("设置","Settings");put("学习","Learn");put("课程","Course");put("我的","Me");put("继续学习","Continue learning");put("查看今日详情","View today's details");put("今日学习总结","Today's summary");
        put("选择正确意思","Choose the correct meaning");put("显示答案","Show answer");put("显示意大利语答案","Show Italian answer");put("下一题 →","Next →");put("下一题","Next");put("下一句","Next sentence");put("下一条","Next item");put("继续","Continue");put("开始","Start");put("完成","Complete");put("取消","Cancel");put("关闭","Close");put("恢复","Restore");put("清空","Clear");put("检查","Check");put("正确","Correct");put("不会","Don't know");put("先跳过","Skip for now");
        put("词汇起步","Vocabulary start");put("听力训练","Listening practice");put("句子理解","Sentence comprehension");put("语法微练","Grammar mini-practice");put("主动表达","Active production");put("听力巩固","Listening reinforcement");put("综合运用","Integrated practice");put("单元挑战","Unit challenge");
        put("选择正确冠词","Choose the correct article");put("选择正确的现在时变位","Choose the correct present-tense form");put("个人错句本","Personal sentence repair");put("个人错题诊断中心","Personal error diagnosis");put("学习能力护照","Skill passport");put("个人遗忘画像","Personal forgetting profile");put("真实情景会话","Real-life conversation");put("真实生活任务地图","Real-life task map");put("Shadowing 三遍训练室","3-pass Shadowing room");put("意大利语发音专项","Italian pronunciation");put("句型训练","Sentence patterns");put("语法弱点诊断","Grammar diagnosis");put("每周实战考试","Weekly practical exam");put("今日弱词微短文","Today's weak-word mini story");
        put("基础应对","Basic response");put("独立表达","Independent expression");put("真实实战","Real-life challenge");put("初级","Beginner");put("中级","Intermediate");put("高级","Advanced");put("全部","All");put("全部场景","All scenarios");put("全部主题","All topics");
        put("识义","Meaning");put("听力","Listening");put("拼写","Spelling");put("口语","Speaking");put("语法","Grammar");put("真实使用","Real-life use");put("主动回忆","Active recall");put("到期复习","Due review");put("错词本","Wrong words");put("收藏词","Favorites");put("顽固词","Stubborn words");put("句子听写","Sentence dictation");put("听音认词","Listen → recognize");put("听句选义","Listen → choose meaning");put("听音选词","Listen → choose word");
        put("目前没有顽固词 🎉","No stubborn words right now 🎉");put("今天没有到期句子","No sentences due today");put("尚未开始","Not started");put("已完成","Completed");put("已收藏","Favorited");put("未完成","Not completed");put("未测试","Not tested");put("待安排","Not scheduled");put("未建立","Not established");put("正确表达","Correct expression");put("错误来源","Error source");
        put("小","Small");put("标准（推荐）","Standard (recommended)");put("大","Large");put("超大","Extra large");put("总是显示","Always show");put("自动隐藏（推荐）","Auto-hide (recommended)");put("点击/显示答案后显示","Show after tap / reveal");put("永不显示","Never show");
        put("学习辅助语言 / Support language","Support language");put("中英对照（推荐）","Chinese + English (recommended)");
        put("只看中文 · 自己回答 →","Use the English prompt · Answer yourself →");put("只看中文主动说","Speak from the English prompt");put("显示中文","Show meaning");put("隐藏中文","Hide meaning");
        put("练习中心","Practice");put("今日复习","Today\'s review");put("中文→意大利语","English → Italian");put("拼写练习","Spelling practice");put("听写","Dictation");put("🎙 跟读发音","🎙 Pronunciation practice");put("易混词专项","Confusable words");put("🇮🇹 生活场景","🇮🇹 Life scenarios");put("🧩 句型训练","🧩 Sentence patterns");put("💬 情景对话","💬 Scenario dialogue");put("🎧 通勤听力","🎧 Commute listening");put("🗣 本地表达","🗣 Speaking practice");put("🧠 语法诊断","🧠 Grammar diagnosis");put("📚 个性课程","📚 Personal course");put("🔤 发音专项","🔤 Pronunciation");put("🎓 阶段自测","🎓 Level check");put("📖 分级阅读","📖 Graded reading");put("🩺 错题诊断","🩺 Error diagnosis");put("🆘 救急意语","🆘 Survival Italian");put("⭐ 我的词句","⭐ My words & sentences");
        put("课程","Course");put("从 A0 一路学到 B1。当前阶段会自动展开。","Study from A0 to B1. Your current stage expands automatically.");put("📅 查看未来7天巩固日历","📅 View 7-day review calendar");
        put("A0 入门","A0 Beginner");put("A1 基础","A1 Foundation");put("A2 日常交流","A2 Everyday communication");put("B1 独立交流","B1 Independent communication");
        put("今天只做这些","Today\'s plan");put("正在整理今天的学习…","Preparing today\'s plan…");put("下一步","Next");put("你只需要完成下一步。","Just complete the next step.");put("今日详细计划","Today\'s detailed plan");put("系统会根据复习压力和薄弱项自动调整。","The plan adapts automatically to review load and weak areas.");put("今天准备学习多久","Daily target in minutes");
        put("🧠 智能背词","🧠 Smart vocabulary review");put("正在整理今天的复习与新词…","Preparing today\'s reviews and new words…");put("开始智能背词","Start smart vocabulary");put("📖 2000词 · 十篇通关","📖 2,000 words · 10 stories");put("10篇主题故事 · 正在读取进度…","10 themed stories · Loading progress…");put("进入十篇通关","Open 10-story course");put("当前单元学习路径","Current unit path");put("继续当前单元","Continue current unit");put("下一单元：基础交流","Next unit: Basic communication");
        put("主课程已经自动安排复习。这里只在你想额外练某一项时使用。","Your main course already schedules reviews automatically. Use this page only when you want extra practice in a specific skill.");
        put("🧪 每周实战考试","🧪 Weekly practical exam");put("🗺 真实生活任务地图","🗺 Real-life task map");put("📚 今日弱词微短文","📚 Today\'s weak-word mini story");put("🗣 每日5句开口","🗣 Daily 5-sentence speaking");put("🎧🗣 听力与开口","🎧🗣 Listening & speaking");put("🎭 真实情景会话","🎭 Real-life conversation");put("🧩 微语法实战","🧩 Micro-grammar practice");put("🗣 发音练习","🗣 Pronunciation practice");put("❤️ 错题复习","❤️ Error review");put("🧾 个人错句本","🧾 Personal sentence repair");put("✍️ 拼写练习","✍️ Spelling practice");
        put("更多练习工具 ▾","More practice tools ▾");put("收起练习工具 ▴","Hide practice tools ▴");put("进阶工具","Advanced tools");put("💬 核心句子","💬 Core sentences");put("🗺 语法知识地图","🗺 Grammar map");put("🔤 动词变位","🔤 Verb conjugation");put("💬 高频短语","💬 High-frequency phrases");put("📊 弱项分析","📊 Weak-area analysis");
        put("🎙 开始跟读","🎙 Start speaking");put("🔊 再听一次","🔊 Play again");put("输入意大利语","Enter Italian");put("提交答案","Submit answer");
        // common dynamic fragments, longest/specific first
        part("只看中文","Use the English prompt");part("中级只给中文","Intermediate: prompt only");part("中文提示","meaning hint");part("中文意思","meaning");part("显示中文","Show meaning");part("隐藏中文","Hide meaning");
        part("今天完成","Completed today");part("今日完成","Completed today");part("下一步：","Next: ");part("当前最弱：","Current weakest: ");part("当前弱项：","Current weak area: ");part("当前","Current");part("历史正确率","Historical accuracy");part("本轮正确","Correct this round");part("本轮完成","Round complete");part("已接触","Seen");part("已掌握","Mastered");part("已学习","Learned");part("已完成","Completed");part("待修复","To repair");part("到期","Due");part("复习","Review");part("新词","new words");part("错词","wrong words");part("顽固词","stubborn words");part("正确率","accuracy");part("匹配度","match");part("得分","score");part("剩余","remaining");part("分钟","min");part("小时","hours");part("天","days");part("条","items");part("句","sentences");part("词","words");part("题","questions");part("次","times");part("轮","rounds");part("关","stages");part("单元","unit");part("小节","section");part("篇","article");part("第","No. ");part("共","total ");part("约","about ");part("开始","Start");part("继续","Continue");part("返回","Back");part("显示","Show");part("隐藏","Hide");part("答案","answer");part("提示","Hint");part("正确","Correct");part("错误","Error");part("完成","Complete");part("语法","Grammar");part("口语","Speaking");part("听力","Listening");part("拼写","Spelling");part("词义","meaning");part("课程","course");part("学习","study");part("练习","practice");part("今天","today");part("明天","tomorrow");
    }
    private UiEnglishTranslator(){}
    private static void put(String zh,String en){EXACT.put(zh,en);}private static void part(String zh,String en){PART.put(zh,en);}
    public static String translate(String input){
        if(input==null||input.isEmpty())return input;
        String exact=EXACT.get(input);if(exact!=null)return exact;
        if(input.contains("\n")){
            String[] lines=input.split("\n",-1);StringBuilder out=new StringBuilder();
            for(int i=0;i<lines.length;i++){if(i>0)out.append('\n');out.append(translateLine(lines[i]));}
            return out.toString();
        }
        return translateLine(input);
    }

    /** Translate an entire dynamic UI sentence. Never return the old half-Chinese/half-English word salad. */
    private static String translateLine(String input){
        if(input==null||input.isEmpty())return input;
        String exact=EXACT.get(input);if(exact!=null)return exact;
        String dynamic=dynamic(input);if(dynamic!=null)return dynamic;
        String out=input;for(Map.Entry<String,String> e:PART.entrySet())out=out.replace(e.getKey(),e.getValue());
        // v5.2 safety rule: a partial translation is worse than a complete source-language sentence.
        // If fragments remain Chinese, keep the whole original line until a full template exists.
        return containsChinese(out)?input:out;
    }

    private static String dynamic(String s){
        Matcher m;
        m=Pattern.compile("^(A0|A1|A2|B1) (入门|基础|日常交流|独立交流) · 第(\\d+)单元$").matcher(s);
        if(m.matches())return stage(m.group(1))+" · Unit "+m.group(3);
        m=Pattern.compile("^(A0|A1|A2|B1) (入门|基础|日常交流|独立交流)课程$").matcher(s);
        if(m.matches())return stage(m.group(1))+" course";
        m=Pattern.compile("^(\\d+) / (\\d+) 单元完成$").matcher(s);
        if(m.matches())return m.group(1)+" / "+m.group(2)+" units completed";
        m=Pattern.compile("^(\\d+) / (\\d+) 关 · (\\d+)个核心词$").matcher(s);
        if(m.matches())return m.group(1)+" / "+m.group(2)+" stages · "+m.group(3)+" core words";
        m=Pattern.compile("^第(\\d+)个学习日 · 今天不用研究全部功能，只跟着“继续学习”往下走。$").matcher(s);
        if(m.matches())return "Study day "+m.group(1)+" · Just follow Continue learning; you do not need to explore every feature today.";
        m=Pattern.compile("^当前单元 · (.+) · 听、说、读、写会由系统自动穿插。$").matcher(s);
        if(m.matches())return "Current unit · "+translatePath(m.group(1))+" · Listening, speaking, reading and writing are mixed in automatically.";
        m=Pattern.compile("^🔥 (\\d+)天$").matcher(s);if(m.matches())return "🔥 "+m.group(1)+" days";
        m=Pattern.compile("^今天目标 (\\d+) 分钟 · 已学习 (\\d+) 分钟 · 计划还剩约 (\\d+) 分钟$").matcher(s);
        if(m.matches())return "Today: "+m.group(1)+" min · Studied "+m.group(2)+" min · About "+m.group(3)+" min left";
        m=Pattern.compile("^(\\d+) / (\\d+)项完成 · 还剩约(\\d+)分钟 · 今日目标(\\d+)分钟$").matcher(s);
        if(m.matches())return m.group(1)+" / "+m.group(2)+" tasks complete · about "+m.group(3)+" min left · "+m.group(4)+" min goal";
        m=Pattern.compile("^下一步：(.+) · 约(\\d+)分钟$").matcher(s);
        if(m.matches())return "Next: "+translateEmbedded(m.group(1))+" · about "+m.group(2)+" min";
        m=Pattern.compile("^(\\d+)\\. (.+) · 下一项$").matcher(s);
        if(m.matches())return m.group(1)+". "+translateEmbedded(m.group(2))+" · Next";
        m=Pattern.compile("^约(\\d+)分$").matcher(s);if(m.matches())return "about "+m.group(1)+" min";
        m=Pattern.compile("^开始下一项 · (.+)$").matcher(s);if(m.matches())return "Start next · "+translateEmbedded(m.group(1));
        m=Pattern.compile("^今日复习 (\\d+) · 新词 (\\d+) · 四维掌握 (\\d+)$").matcher(s);
        if(m.matches())return "Due reviews "+m.group(1)+" · New words "+m.group(2)+" · 4-skill mastered "+m.group(3);
        m=Pattern.compile("^已完成 (\\d+) / (\\d+) 小节 · 已遇到 (\\d+) · 已认识 (\\d+) · 真正掌握 (\\d+)$").matcher(s);
        if(m.matches())return "Completed "+m.group(1)+" / "+m.group(2)+" sections · Encountered "+m.group(3)+" · Recognized "+m.group(4)+" · Mastered "+m.group(5);
        m=Pattern.compile("^待复习 (\\d+) · 错词 (\\d+) · 今日正确率 (\\d+)% · 本地表达 (\\d+)轮$").matcher(s);
        if(m.matches())return "Due "+m.group(1)+" · Wrong words "+m.group(2)+" · Today accuracy "+m.group(3)+"% · Speaking rounds "+m.group(4);
        m=Pattern.compile("^四维：识义 (\\d+)% · 听力 (\\d+)% · 拼写 (\\d+)% · 口语 (\\d+)%$").matcher(s);
        if(m.matches())return "4 skills: Meaning "+m.group(1)+"% · Listening "+m.group(2)+"% · Spelling "+m.group(3)+"% · Speaking "+m.group(4)+"%";
        m=Pattern.compile("^当前单元 · (.+)$").matcher(s);if(m.matches())return "Current unit · "+translateEmbedded(m.group(1));
        return null;
    }

    private static String translatePath(String x){
        String[] parts=x.split(" → ");StringBuilder out=new StringBuilder();
        for(int i=0;i<parts.length;i++){if(i>0)out.append(" → ");out.append(translateEmbedded(parts[i]));}
        return out.toString();
    }
    private static String translateEmbedded(String x){String e=EXACT.get(x);if(e!=null)return e;String d=dynamic(x);return d==null?x:d;}
    private static String stage(String s){if("A0".equals(s))return "A0 Beginner";if("A1".equals(s))return "A1 Foundation";if("A2".equals(s))return "A2 Everyday communication";return "B1 Independent communication";}
    private static boolean containsChinese(String s){return s!=null&&s.matches(".*[\u4e00-\u9fff].*");}
}
