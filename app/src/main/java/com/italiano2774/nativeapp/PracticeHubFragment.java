package com.italiano2774.nativeapp;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

/** v5.0 practice hub: extra tools appear gradually during the first seven active study days. */
public class PracticeHubFragment extends Fragment {
    private ProgressStore progress;private MainActivity activity;
    private final java.util.Map<Integer,Runnable> practiceActions=new java.util.HashMap<>();
    private View hubRoot;private android.widget.EditText search;
    private String selectedCategory="all";private boolean unlockedOnly,advancedExpanded;
    private final int[] practiceIds={R.id.button_simple_daily_speaking,R.id.button_simple_listen,R.id.button_simple_pronunciation,
        R.id.button_simple_weak_story,R.id.button_simple_wrong,R.id.button_simple_error_evidence,R.id.button_simple_spelling,
        R.id.button_simple_micro_grammar,R.id.button_simple_life_tasks,R.id.button_simple_scenarios,R.id.button_simple_weekly_exam,
        R.id.button_adv_reading,R.id.button_adv_sentences,R.id.button_adv_grammar,R.id.button_adv_verbs,R.id.button_adv_phrases,R.id.button_adv_all};
    private final String[] categories={"listen","listen","listen","words","words","words","words","grammar","life","life","life","words","words","grammar","grammar","words","life"};
    private final String[] keywords={"口语 开口 每日5句 speaking daily sentences", "听说 听力 开口 listening speaking audio", "发音 跟读 pronunciation",
        "阅读 短文 弱词 story reading weak words", "错题 复习 error review words", "错句 复习 重写 sentence repair mistakes review", "拼写 单词 spelling words",
        "语法 grammar", "生活 任务 实战 real life tasks", "场景 情景 对话 会话 conversation dialogue scenarios", "考试 测试 周测 weekly exam test",
        "阅读 文章 graded reading articles", "核心 句子 core sentences", "语法 地图 grammar map", "动词 变位 verbs conjugation", "高频 短语 phrases", "弱项 分析 weakness analysis"};
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle state){
        View v=inflater.inflate(R.layout.fragment_practice_hub,container,false);hubRoot=v;practiceActions.clear();activity=(MainActivity)requireActivity();progress=new ProgressStore(requireContext());
        TextView hint=v.findViewById(R.id.text_practice_hint);hint.setText(BeginnerGuideEngine.practiceHint(progress));

        MaterialButton weekly=v.findViewById(R.id.button_simple_weekly_exam);String weeklyLabel=progress.weeklyExamDue()?LearningLanguage.ui(progress,"🧪 每周实战考试 · 已到期","🧪 Weekly practical exam · Due now"):LearningLanguage.ui(progress,"🧪 每周实战考试 · "+Math.min(7,progress.weeklyExamActiveDaysSinceLast())+"/7活跃日","🧪 Weekly practical exam · "+Math.min(7,progress.weeklyExamActiveDaysSinceLast())+"/7 active days");bindGuided(weekly,7,weeklyLabel,LearningLanguage.ui(progress,"每周实战考试","Weekly practical exam"),()->activity.openWeeklyExam());
        MaterialButton life=v.findViewById(R.id.button_simple_life_tasks);bindGuided(life,5,LearningLanguage.ui(progress,"🗺 真实生活任务地图 · "+LifeTaskEngine.completedStages(progress)+"/36关","🗺 Real-life task map · "+LifeTaskEngine.completedStages(progress)+"/36 stages"),LearningLanguage.ui(progress,"真实生活任务地图","Real-life task map"),()->activity.openLifeTaskMap());
        bindGuided(v.findViewById(R.id.button_simple_weak_story),4,LearningLanguage.ui(progress,"📚 今日弱词微短文","📚 Today\'s weak-word mini story"),LearningLanguage.ui(progress,"今日弱词微短文","Today\'s weak-word mini story"),()->activity.openWeakWordStory());
        bindGuided(v.findViewById(R.id.button_simple_daily_speaking),3,LearningLanguage.ui(progress,"🗣 每日5句开口","🗣 Daily 5-sentence speaking"),LearningLanguage.ui(progress,"每日5句开口","Daily 5-sentence speaking"),()->activity.openDailySpeakingChallenge());
        bindGuided(v.findViewById(R.id.button_simple_listen),2,LearningLanguage.ui(progress,"🎧🗣 听力与开口","🎧🗣 Listening & speaking"),LearningLanguage.ui(progress,"听力与开口","Listening & speaking"),()->activity.openListeningSpeaking());
        bindGuided(v.findViewById(R.id.button_simple_scenarios),5,LearningLanguage.ui(progress,"🎭 真实情景会话","🎭 Real-life conversation"),LearningLanguage.ui(progress,"真实情景会话","Real-life conversation"),()->activity.openScenarios());
        bindGuided(v.findViewById(R.id.button_simple_micro_grammar),4,LearningLanguage.ui(progress,"🧩 微语法实战","🧩 Micro-grammar practice"),LearningLanguage.ui(progress,"微语法实战","Micro-grammar practice"),()->activity.openMicroGrammar());
        bindGuided(v.findViewById(R.id.button_simple_pronunciation),3,LearningLanguage.ui(progress,"🗣 发音练习","🗣 Pronunciation practice"),LearningLanguage.ui(progress,"发音练习","Pronunciation practice"),()->activity.openPronunciation());
        bindGuided(v.findViewById(R.id.button_simple_wrong),4,LearningLanguage.ui(progress,"❤️ 错题复习","❤️ Error review"),LearningLanguage.ui(progress,"错题复习","Error review"),()->activity.openWrongWordRepair());
        MaterialButton evidence=v.findViewById(R.id.button_simple_error_evidence);bindGuided(evidence,4,LearningLanguage.ui(progress,"🧾 个人错句本 · 待修复 "+progress.pendingErrorRepairs()+" 条","🧾 Personal sentence repair · "+progress.pendingErrorRepairs()+" to repair"),LearningLanguage.ui(progress,"个人错句本","Personal sentence repair"),()->activity.openErrorEvidenceRepair());
        bindGuided(v.findViewById(R.id.button_simple_spelling),3,LearningLanguage.ui(progress,"✍️ 拼写练习","✍️ Spelling practice"),LearningLanguage.ui(progress,"拼写练习","Spelling practice"),()->activity.openPracticeMode("spell"));

        LinearLayout advanced=v.findViewById(R.id.container_advanced_practice);MaterialButton more=v.findViewById(R.id.button_more_practice);if(BeginnerGuideEngine.active(progress)){more.setVisibility(View.GONE);advanced.setVisibility(View.GONE);}else more.setOnClickListener(x->{advancedExpanded=!advancedExpanded;applyFilters();});
        bindAdvanced(v,R.id.button_adv_sentences,3,LearningLanguage.ui(progress,"💬 核心句子","💬 Core sentences"),LearningLanguage.ui(progress,"核心句子","Core sentences"),()->activity.openCoreSentences());
        bindAdvanced(v,R.id.button_adv_phrases,8,LearningLanguage.ui(progress,"💬 高频短语","💬 High-frequency phrases"),LearningLanguage.ui(progress,"高频短语","High-frequency phrases"),()->activity.openPhrases());
        bindAdvanced(v,R.id.button_adv_grammar,10,LearningLanguage.ui(progress,"🗺 语法知识地图","🗺 Grammar map"),LearningLanguage.ui(progress,"语法知识地图","Grammar map"),()->activity.openGrammarMap());
        bindAdvanced(v,R.id.button_adv_verbs,16,LearningLanguage.ui(progress,"🔤 动词变位","🔤 Verb conjugation"),LearningLanguage.ui(progress,"动词变位","Verb conjugation"),()->activity.openVerbCenter());
        bindAdvanced(v,R.id.button_adv_reading,24,LearningLanguage.ui(progress,"📖 分级阅读","📖 Graded reading"),LearningLanguage.ui(progress,"分级阅读","Graded reading"),()->activity.openReadingList());
        bindAdvanced(v,R.id.button_adv_all,32,LearningLanguage.ui(progress,"📊 弱项分析","📊 Weak-area analysis"),LearningLanguage.ui(progress,"弱项分析","Weak-area analysis"),()->activity.openWeaknessCenter());
        setupFilters(v,state);return v;
    }
    private android.content.SharedPreferences recentPreferences(){return requireContext().getSharedPreferences("italiano_practice_recent",android.content.Context.MODE_PRIVATE);}
    private void rememberPractice(MaterialButton button){String key=getResources().getResourceEntryName(button.getId());String saved=recentPreferences().getString("entries","");recentPreferences().edit().putString("entries",RecentPracticeHistory.record(saved,key)).apply();}
    private void renderRecent(){
        LinearLayout box=hubRoot.findViewById(R.id.container_practice_recent);box.removeAllViews();
        TextView heading=new TextView(requireContext());heading.setText(LearningLanguage.ui(progress,"最近使用","Recently used"));heading.setTextSize(16);heading.setTypeface(null,android.graphics.Typeface.BOLD);box.addView(heading);
        for(String key:RecentPracticeHistory.entries(recentPreferences().getString("entries",""))){
            for(int id:practiceIds){if(key.startsWith("button_adv_")&&BeginnerGuideEngine.active(progress))continue;if(!getResources().getResourceEntryName(id).equals(key)||!practiceActions.containsKey(id))continue;
                MaterialButton original=hubRoot.findViewById(id),shortcut=new MaterialButton(requireContext());shortcut.setText(original.getText());shortcut.setAllCaps(false);shortcut.setMinHeight((int)(56*getResources().getDisplayMetrics().density));
                shortcut.setOnClickListener(x->original.performClick());box.addView(shortcut,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));break;
            }
        }
    }

    private void setupFilters(View v,Bundle state){
        if(state!=null){selectedCategory=state.getString("practice_category","all");unlockedOnly=state.getBoolean("practice_unlocked",false);advancedExpanded=state.getBoolean("practice_advanced",false);}
        search=v.findViewById(R.id.edit_practice_search);
        ((com.google.android.material.textfield.TextInputLayout)v.findViewById(R.id.layout_practice_search)).setHint(LearningLanguage.ui(progress,"搜索练习，例如 听力 / 拼写","Search practice, e.g. listening / spelling"));
        if(state!=null)search.setText(state.getString("practice_query",""));
        com.google.android.material.chip.ChipGroup group=v.findViewById(R.id.group_practice_category);
        int selected="listen".equals(selectedCategory)?R.id.chip_practice_listen:"words".equals(selectedCategory)?R.id.chip_practice_words:"grammar".equals(selectedCategory)?R.id.chip_practice_grammar:"life".equals(selectedCategory)?R.id.chip_practice_life:R.id.chip_practice_all;
        group.check(selected);
        group.setOnCheckedStateChangeListener((g,ids)->{if(ids.isEmpty())return;int id=ids.get(0);selectedCategory=id==R.id.chip_practice_listen?"listen":id==R.id.chip_practice_words?"words":id==R.id.chip_practice_grammar?"grammar":id==R.id.chip_practice_life?"life":"all";applyFilters();});
        android.widget.CompoundButton available=v.findViewById(R.id.check_practice_unlocked);available.setChecked(unlockedOnly);
        available.setOnCheckedChangeListener((b,checked)->{unlockedOnly=checked;applyFilters();});
        search.addTextChangedListener(new android.text.TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){applyFilters();}
            public void afterTextChanged(android.text.Editable value){}
        });
        v.findViewById(R.id.button_practice_clear).setOnClickListener(x->{search.setText("");available.setChecked(false);group.check(R.id.chip_practice_all);});
        renderRecent();applyFilters();
    }

    private void applyFilters(){
        if(hubRoot==null||search==null)return;
        String query=search.getText()==null?"":search.getText().toString();
        boolean filtering=!query.trim().isEmpty()||!"all".equals(selectedCategory)||unlockedOnly;
        hubRoot.findViewById(R.id.button_practice_clear).setVisibility(filtering?View.VISIBLE:View.GONE);
        View recent=hubRoot.findViewById(R.id.container_practice_recent);recent.setVisibility(!filtering&&((android.widget.LinearLayout)recent).getChildCount()>1?View.VISIBLE:View.GONE);
        boolean advancedAllowed=!BeginnerGuideEngine.active(progress),showAdvanced=advancedAllowed&&(advancedExpanded||filtering);
        int visible=0,advancedMatches=0;int[] groupCounts=new int[4];
        for(int i=0;i<practiceIds.length;i++){
            MaterialButton b=hubRoot.findViewById(practiceIds[i]);
            boolean match=PracticeFilter.matches(query,selectedCategory,categories[i],unlockedOnly,b.isEnabled(),b.getText().toString(),keywords[i]);
            if(i>=11){if(match)advancedMatches++;match=match&&showAdvanced;}
            b.setVisibility(match?View.VISIBLE:View.GONE);
            if(match){visible++;if(i<11){int section="listen".equals(categories[i])?0:"words".equals(categories[i])?1:"grammar".equals(categories[i])?2:3;groupCounts[section]++;}}
        }
        int[] sections={R.id.section_practice_listen,R.id.section_practice_words,R.id.section_practice_grammar,R.id.section_practice_life};
        for(int i=0;i<sections.length;i++)hubRoot.findViewById(sections[i]).setVisibility(groupCounts[i]>0?View.VISIBLE:View.GONE);
        hubRoot.findViewById(R.id.container_advanced_practice).setVisibility(showAdvanced&&advancedMatches>0?View.VISIBLE:View.GONE);
        MaterialButton more=hubRoot.findViewById(R.id.button_more_practice);more.setVisibility(advancedAllowed&&!filtering?View.VISIBLE:View.GONE);
        more.setText(advancedExpanded?LearningLanguage.ui(progress,"收起进阶工具 ▴","Hide advanced tools ▴"):LearningLanguage.ui(progress,"更多练习工具 ▾","More practice tools ▾"));
        ((TextView)hubRoot.findViewById(R.id.text_practice_result_count)).setText(LearningLanguage.ui(progress,"显示 "+visible+" 项练习","Showing "+visible+" practices"));
        hubRoot.findViewById(R.id.text_practice_empty).setVisibility(visible==0?View.VISIBLE:View.GONE);
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state){
        super.onSaveInstanceState(state);state.putString("practice_category",selectedCategory);state.putBoolean("practice_unlocked",unlockedOnly);state.putBoolean("practice_advanced",advancedExpanded);
        if(search!=null)state.putString("practice_query",search.getText().toString());
    }

    private void bindGuided(MaterialButton b,int unlockDay,String label,String plainLabel,Runnable action){boolean unlocked=BeginnerGuideEngine.practiceUnlocked(progress,unlockDay);b.setEnabled(unlocked);b.setAlpha(unlocked?1f:.58f);b.setText(unlocked?label:BeginnerGuideEngine.lockedLabel(progress,plainLabel,plainLabel,unlockDay));if(unlocked){practiceActions.put(b.getId(),action);b.setOnClickListener(x->{rememberPractice(b);action.run();});}else{b.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.line)));b.setTextColor(ContextCompat.getColor(requireContext(),R.color.text_secondary));}}
    private void bindAdvanced(View root,int id,int minUnit,String label,String plainLabel,Runnable action){MaterialButton b=root.findViewById(id);boolean unlocked=progress.courseUnlockedUnitIndex()>=minUnit;b.setEnabled(unlocked);b.setAlpha(unlocked?1f:.58f);b.setText(unlocked?label:LearningLanguage.ui(progress,"🔒 "+plainLabel+" · "+unlockLabel(minUnit)+"解锁","🔒 "+plainLabel+" · Unlocks "+unlockLabelEn(minUnit)));if(unlocked){practiceActions.put(b.getId(),action);b.setOnClickListener(x->{rememberPractice(b);action.run();});}else{b.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.line)));b.setTextColor(ContextCompat.getColor(requireContext(),R.color.text_secondary));}}
    private String unlockLabel(int index){if(index<8)return "A0第"+(index+1)+"单元后";if(index<32)return "A1第"+(index-7)+"单元后";if(index<62)return "A2第"+(index-31)+"单元后";return "B1阶段";}
    private String unlockLabelEn(int index){if(index<8)return "after A0 Unit "+(index+1);if(index<32)return "after A1 Unit "+(index-7);if(index<62)return "after A2 Unit "+(index-31);return "in B1";}
}
