package com.italiano2774.nativeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.time.LocalDate;

/** v3.0 first run: only ask questions a beginner can answer confidently. */
public class OnboardingFragment extends Fragment {
    private ProgressStore progress;private Spinner goal,minutes,supportLanguage;private TextView plan;
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle state){
        View v=inflater.inflate(R.layout.fragment_onboarding,container,false);progress=new ProgressStore(requireContext());((MainActivity)requireActivity()).setFocusUi(true);goal=v.findViewById(R.id.spinner_onboarding_goal);minutes=v.findViewById(R.id.spinner_onboarding_minutes);supportLanguage=v.findViewById(R.id.spinner_onboarding_support_language);plan=v.findViewById(R.id.text_onboarding_auto_plan);
        String[] supportModes={"中文","English","中英对照（推荐）"};ArrayAdapter<String> la=new ArrayAdapter<>(requireContext(),android.R.layout.simple_spinner_item,supportModes);la.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);supportLanguage.setAdapter(la);supportLanguage.setSelection(progress.supportLanguageMode());supportLanguage.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View x,int pos,long id){if(progress.supportLanguageMode()!=pos){progress.setSupportLanguageMode(pos);requireActivity().recreate();}}});
        String[] goals=LearningLanguage.englishUi(progress)?new String[]{"🇮🇹 Life in Italy","💼 Work communication","📝 Reach A2","🎓 Reach B1","🌍 Balanced learning"}:new String[]{"🇮🇹 在意大利生活","💼 工作交流","📝 通过A2","🎓 学到B1","🌍 综合学习"};ArrayAdapter<String> ga=new ArrayAdapter<>(requireContext(),android.R.layout.simple_spinner_item,goals);ga.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);goal.setAdapter(ga);goal.setSelection(0);
        Integer[] mins={10,20,30,45,60};ArrayAdapter<Integer> ma=new ArrayAdapter<>(requireContext(),android.R.layout.simple_spinner_item,mins);ma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);minutes.setAdapter(ma);minutes.setSelection(1);minutes.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onItemSelected(android.widget.AdapterView<?> p,View x,int pos,long id){int m=(Integer)minutes.getSelectedItem();plan.setText(LearningLanguage.ui(progress,"每天约 "+recommendedWords(m)+" 个新词，会自动混入旧词复习、听力和句子。前7个学习日功能会逐步开放，你只需要点“继续学习”。","About "+recommendedWords(m)+" new words per day, mixed automatically with review, listening and sentences. Features unlock gradually during your first 7 active study days; just tap Continue learning."));}public void onNothingSelected(android.widget.AdapterView<?> p){}});
        v.findViewById(R.id.button_onboarding_start).setOnClickListener(x->{save();((MainActivity)requireActivity()).finishOnboarding(false);});v.findViewById(R.id.button_onboarding_test).setOnClickListener(x->{save();((MainActivity)requireActivity()).finishOnboarding(true);});v.findViewById(R.id.button_onboarding_skip).setOnClickListener(x->{progress.setLearningGoal(ProgressStore.GOAL_LIFE);progress.setSessionMinutes(20);progress.setPerDay(20);progress.setPronunciationMode(ProgressStore.PRON_AUTO);progress.setHomeSimpleMode(true);progress.setStartDate(LocalDate.now());progress.markOnboardingCompleted();((MainActivity)requireActivity()).finishOnboarding(false);});return v;
    }
    private void save(){int m=(Integer)minutes.getSelectedItem();progress.setLearningGoal(goal.getSelectedItemPosition());progress.setSessionMinutes(m);progress.setPerDay(recommendedWords(m));progress.setPronunciationMode(ProgressStore.PRON_AUTO);progress.setHomeSimpleMode(true);progress.setStartDate(LocalDate.now());progress.markOnboardingCompleted();}
    private int recommendedWords(int m){if(m<=10)return 10;if(m<=20)return 20;if(m<=30)return 20;if(m<=45)return 30;return 50;}
}
