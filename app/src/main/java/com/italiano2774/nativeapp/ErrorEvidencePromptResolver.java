package com.italiano2774.nativeapp;

import android.content.Context;
import java.util.Locale;

/**
 * v5.2.1 resolves the learner-facing prompt for a sentence repair card.
 * New errors persist the original prompt in ErrorRecordEntity.detail. Legacy rows are
 * reconstructed from audited local sentence assets whenever possible, so the learner is
 * never asked to rewrite an unknown sentence blindly.
 */
public final class ErrorEvidencePromptResolver {
    private ErrorEvidencePromptResolver(){}

    public static String resolve(Context context,ProgressStore progress,ErrorRecordEntity e){
        if(context==null||e==null)return "";
        String embedded=ErrorEvidencePolicy.extractPrompt(e.detail);
        if(!embedded.isEmpty())return embedded;

        String expected=norm(e.expected);
        if(expected.isEmpty())return "";
        WordRepository words=WordRepository.get(context);

        // Weekly exam / daily speaking / many shadowing rows use the audited 430 core sentences.
        for(CoreSentence s:CoreSentenceRepository.get(context).all()){
            if(norm(s.italian).equals(expected))return LearningLanguage.sentenceSupport(words,s.italian,s.chinese,progress);
        }

        // Listening-course rows have their own audited Chinese support.
        for(ListeningCourse c:ListeningCourseRepository.get(context).all()){
            for(ListeningSentence s:c.sentences)if(norm(s.italian).equals(expected))
                return LearningLanguage.sentenceSupport(words,s.italian,s.chinese,progress);
        }

        // Real-life dialogue rows can be recovered from the shipped scenario phrases.
        for(Scenario s:ScenarioRepository.get(context).all()){
            for(ScenarioPhrase q:s.phrases)if(norm(q.it).equals(expected))
                return LearningLanguage.sentenceSupport(words,q.it,q.zh,progress);
        }

        // Three-level dialogue content is stored separately from the task-map phrases.
        for(DialogueScenario d:DialogueRepository.get(context).all()){
            for(DialogueTurn t:d.turns){
                for(String choice:t.choices)if(norm(choice).equals(expected))
                    return LearningLanguage.sentenceSupport(words,choice,t.answerZh,progress);
            }
        }

        // Dictation also includes the shipped emergency phrase set.
        for(SentenceDictationItem x:new SentenceDictationRepository(context).all())if(norm(x.italian).equals(expected))
            return LearningLanguage.sentenceSupport(words,x.italian,x.chinese,progress);

        // Legacy writing rows stored "title · prompt · score=..." in detail.
        if("writing".equals(e.mode)&&e.detail!=null){
            String d=ErrorEvidencePolicy.cleanDetail(e.detail);int score=d.indexOf(" · score=");
            if(score>=0)d=d.substring(0,score).trim();
            int sep=d.indexOf(" · ");if(sep>=0&&sep+3<d.length())d=d.substring(sep+3).trim();
            if(!d.isEmpty())return d;
        }
        return "";
    }

    public static boolean hasUsablePrompt(Context context,ProgressStore progress,ErrorRecordEntity e){return !resolve(context,progress,e).trim().isEmpty();}

    private static String norm(String s){
        if(s==null)return "";
        return s.toLowerCase(Locale.ITALIAN).replace('’','\'').replaceAll("[^\\p{L}' ]"," ").replaceAll("\\s+"," ").trim();
    }
}
