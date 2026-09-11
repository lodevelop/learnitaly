package com.italiano2774.nativeapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * v3.3.4 guided mixed-skill course scheduler.
 *
 * A unit is no longer a row of vocabulary-only lessons.  New words are distributed across
 * vocabulary, listening, sentence, grammar and active-output nodes, then brought together in
 * the unit challenge.  Each node still stays short (<=12 scored/teaching cards) for beginners.
 *
 * v5.0.1 rule: fixed course lessons are topic-pure. Global due reviews must stay in the
 * daily smart/review routes and must never be injected into unrelated course units.
 *
 * v5.2.0 rule: one-pass exposure is balanced by CourseDistributionPlanner. Every word is
 * introduced once, same-unit reinforcement is spaced/capped, and the challenge prioritizes
 * under-exposed words instead of random repeats. Distractors also remain inside the unit.
 */
public class CourseLessonEngine {
    private static final int ROLE_VOCAB=0,ROLE_LISTEN=1,ROLE_SENTENCE=2,ROLE_GRAMMAR=3,
            ROLE_ACTIVE=4,ROLE_LISTEN_REINFORCE=5,ROLE_INTEGRATE=6,ROLE_CHALLENGE=7;
    private static final String[] PERSONS={"io","tu","lui/lei","noi","voi","loro"};

    private final WordRepository repo;private final ProgressStore progress;
    public CourseLessonEngine(WordRepository r,ProgressStore p){repo=r;progress=p;}

    public List<CourseQuestion> build(CourseUnit unit,int lessonIndex){
        List<Word> words=unitWords(unit);if(words.isEmpty())return new ArrayList<>();
        int safeLesson=Math.max(0,Math.min(unit.lessonCount-1,lessonIndex));
        Random rnd=new Random(31L*unit.id.hashCode()+safeLesson*997L);
        int role=role(unit,safeLesson);
        if(role==ROLE_CHALLENGE)return challenge(words,unit,rnd);

        int preChallenge=CourseDistributionPlanner.preChallengeLessons(unit);
        List<Word> focus=wordsByIds(CourseDistributionPlanner.focusIds(unit,safeLesson));
        if(focus.isEmpty())focus.add(words.get(Math.min(safeLesson,words.size()-1)));
        List<CourseQuestion> out=new ArrayList<>();

        switch(role){
            case ROLE_VOCAB:
                for(Word w:focus)out.add(meaning(w,unit,rnd,true));
                addSeenReinforcement(out,unit,safeLesson,rnd,ROLE_LISTEN);
                break;
            case ROLE_LISTEN:
                for(Word w:focus)out.add(listen(w,unit,rnd));
                addSeenReinforcement(out,unit,safeLesson,rnd,ROLE_SENTENCE);
                break;
            case ROLE_SENTENCE:
                for(Word w:focus)out.add(sentenceLearning(w,unit,rnd));
                addSeenReinforcement(out,unit,safeLesson,rnd,ROLE_LISTEN);
                break;
            case ROLE_GRAMMAR:
                for(Word w:focus)out.add(grammarLearning(w,unit,rnd));
                addSeenReinforcement(out,unit,safeLesson,rnd,ROLE_SENTENCE);
                break;
            case ROLE_ACTIVE:
                for(Word w:focus)out.add(spellHint(w));
                addSeenReinforcement(out,unit,safeLesson,rnd,ROLE_ACTIVE);
                break;
            case ROLE_LISTEN_REINFORCE:
                for(Word w:focus)out.add(listen(w,unit,rnd));
                addSeenReinforcement(out,unit,safeLesson,rnd,ROLE_GRAMMAR);
                break;
            default:
                int turn=0;
                for(Word w:focus){
                    int m=turn++%4;
                    if(m==0)out.add(listen(w,unit,rnd));
                    else if(m==1)out.add(sentenceLearning(w,unit,rnd));
                    else if(m==2)out.add(grammarLearning(w,unit,rnd));
                    else out.add(spellHint(w));
                }
                addSeenReinforcement(out,unit,safeLesson,rnd,ROLE_SENTENCE);
                break;
        }
        if(out.size()>12)out=new ArrayList<>(out.subList(0,12));
        return out;
    }

    public String lessonTitle(CourseUnit unit,int lessonIndex){
        switch(role(unit,lessonIndex)){
            case ROLE_VOCAB:return LearningLanguage.ui(progress,"词汇起步","Vocabulary start");
            case ROLE_LISTEN:return LearningLanguage.ui(progress,"听力训练","Listening practice");
            case ROLE_SENTENCE:return LearningLanguage.ui(progress,"句子理解","Sentence comprehension");
            case ROLE_GRAMMAR:return LearningLanguage.ui(progress,"语法微练","Grammar mini-practice");
            case ROLE_ACTIVE:return LearningLanguage.ui(progress,"主动表达","Active production");
            case ROLE_LISTEN_REINFORCE:return LearningLanguage.ui(progress,"听力巩固","Listening reinforcement");
            case ROLE_INTEGRATE:return LearningLanguage.ui(progress,"综合运用","Integrated practice");
            default:return LearningLanguage.ui(progress,"单元挑战","Unit challenge");
        }
    }
    public String lessonEmoji(CourseUnit unit,int lessonIndex){
        switch(role(unit,lessonIndex)){
            case ROLE_VOCAB:return "🌱";
            case ROLE_LISTEN:return "🎧";
            case ROLE_SENTENCE:return "💬";
            case ROLE_GRAMMAR:return "🧩";
            case ROLE_ACTIVE:return "✍️";
            case ROLE_LISTEN_REINFORCE:return "🎧";
            case ROLE_INTEGRATE:return "🔄";
            default:return "🏆";
        }
    }
    public String pathSummary(CourseUnit unit){
        List<String> labels=new ArrayList<>();
        for(int i=0;i<unit.lessonCount;i++){String x=lessonTitle(unit,i);if(!labels.contains(x))labels.add(x);}
        return join(labels," → ");
    }

    private int role(CourseUnit unit,int lessonIndex){
        int n=Math.max(5,unit.lessonCount),i=Math.max(0,Math.min(n-1,lessonIndex));
        if(i==n-1)return ROLE_CHALLENGE;
        if(n==5){int[] r={ROLE_VOCAB,ROLE_LISTEN,ROLE_SENTENCE,ROLE_ACTIVE};return r[i];}
        if(n==6){int[] r={ROLE_VOCAB,ROLE_LISTEN,ROLE_SENTENCE,ROLE_GRAMMAR,ROLE_ACTIVE};return r[i];}
        if(n==7){int[] r={ROLE_VOCAB,ROLE_LISTEN,ROLE_SENTENCE,ROLE_GRAMMAR,ROLE_ACTIVE,ROLE_INTEGRATE};return r[i];}
        int[] r={ROLE_VOCAB,ROLE_LISTEN,ROLE_SENTENCE,ROLE_GRAMMAR,ROLE_ACTIVE,ROLE_LISTEN_REINFORCE,ROLE_INTEGRATE};
        return r[Math.min(i,r.length-1)];
    }

    private List<CourseQuestion> challenge(List<Word> words,CourseUnit unit,Random rnd){
        List<CourseQuestion> out=new ArrayList<>();List<Word> pool=wordsByIds(CourseDistributionPlanner.challengeIds(unit,Math.min(10,words.size())));int turn=0;
        for(Word w:pool){
            int m=turn++%6;
            if(m==0)out.add(listen(w,unit,rnd));
            else if(m==1)out.add(active(w));
            else if(m==2)out.add(hasUsableExample(w)?exampleMeaning(w,unit,rnd):meaning(w,unit,rnd));
            else if(m==3)out.add(grammarLearning(w,unit,rnd));
            else if(m==4)out.add(hasUsableExample(w)?cloze(w,unit,rnd):spellHint(w));
            else out.add(meaning(w,unit,rnd));
        }
        if(out.size()>12)out=new ArrayList<>(out.subList(0,12));return out;
    }

    private void addSeenReinforcement(List<CourseQuestion> out,CourseUnit unit,int lessonIndex,Random rnd,int mode){
        if(out.size()>=12)return;
        List<Word> seen=wordsByIds(CourseDistributionPlanner.reinforcementIds(unit,lessonIndex));
        for(Word w:seen){
            int reviewMode=mode;
            int first=CourseDistributionPlanner.focusLesson(unit,w.id);
            int original=first<0?-1:role(unit,first);
            if(original==reviewMode)reviewMode=(reviewMode==ROLE_LISTEN?ROLE_SENTENCE:ROLE_LISTEN);
            CourseQuestion q;
            if(reviewMode==ROLE_LISTEN)q=listen(w,unit,rnd);
            else if(reviewMode==ROLE_SENTENCE)q=hasUsableExample(w)?cloze(w,unit,rnd):meaning(w,unit,rnd);
            else if(reviewMode==ROLE_GRAMMAR)q=grammarLearning(w,unit,rnd);
            else q=active(w);
            out.add(q);if(out.size()>=12)break;
        }
    }

    private CourseQuestion sentenceLearning(Word w,CourseUnit u,Random rnd){
        if(!hasUsableExample(w))return meaning(w,u,rnd,true);
        return (w.id%2==0)?cloze(w,u,rnd):exampleMeaning(w,u,rnd);
    }
    private CourseQuestion grammarLearning(Word w,CourseUnit u,Random rnd){
        CourseQuestion q=articleQuestion(w,rnd);if(q!=null)return q;
        q=verbQuestion(w,u,rnd);if(q!=null)return q;
        if(hasUsableExample(w))return cloze(w,u,rnd);
        return meaning(w,u,rnd,true);
    }

    private CourseQuestion articleQuestion(Word w,Random rnd){
        if(!"noun".equalsIgnoreCase(safe(w.partOfSpeech)))return null;
        String article=safe(w.article);if(article.isEmpty())return null;
        CourseQuestion q=new CourseQuestion();q.type=CourseQuestion.GRAMMAR_ARTICLE;q.word=w;q.prompt=LearningLanguage.ui(progress,"选择正确冠词","Choose the correct article");q.display="___ "+w.word;q.answer=article;q.dimension=ProgressStore.DIM_MEANING;
        String gender="f".equalsIgnoreCase(safe(w.gender))?LearningLanguage.ui(progress,"阴性","feminine"):LearningLanguage.ui(progress,"阳性","masculine");q.support=zh(w)+" · "+gender+(safe(w.number).isEmpty()?"":" · "+("plural".equalsIgnoreCase(w.number)?LearningLanguage.ui(progress,"复数","plural"):LearningLanguage.ui(progress,"单数","singular")));
        String[] base=("plural".equalsIgnoreCase(safe(w.number))||"i".equals(article)||"gli".equals(article)||"le".equals(article))?new String[]{"i","gli","le","dei"}:new String[]{"il","lo","la","l'"};
        q.options.add(article);for(String x:base)if(!q.options.contains(x))q.options.add(x);while(q.options.size()>4)q.options.remove(q.options.size()-1);Collections.shuffle(q.options,rnd);return q;
    }
    private CourseQuestion verbQuestion(Word w,CourseUnit u,Random rnd){
        if(!ItalianGrammar.isVerb(w))return null;String[] forms=ItalianGrammar.presentIndicative(w);if(forms==null||forms.length<6)return null;
        int person=Math.floorMod(w.id,6);String lemma=safe(w.lemma);if(lemma.isEmpty())lemma=w.word;
        CourseQuestion q=new CourseQuestion();q.type=CourseQuestion.GRAMMAR_VERB;q.word=w;q.prompt=LearningLanguage.ui(progress,"选择正确的现在时变位","Choose the correct present-tense form");q.display=PERSONS[person]+" ___  ("+lemma+")";q.answer=forms[person];q.dimension=ProgressStore.DIM_SPELLING;q.support=zh(w)+" · "+LearningLanguage.ui(progress,"原形 ","infinitive ")+lemma;
        LinkedHashSet<String> opts=new LinkedHashSet<>();opts.add(q.answer);for(String f:forms)if(f!=null&&!f.trim().isEmpty())opts.add(f);
        if(opts.size()<4){List<Word> unitWords=unitWords(u);Collections.shuffle(unitWords,rnd);for(Word other:unitWords){if(other.id==w.id||!ItalianGrammar.isVerb(other))continue;String[] of=ItalianGrammar.presentIndicative(other);if(of!=null&&person<of.length)opts.add(of[person]);if(opts.size()>=4)break;}}
        q.options.addAll(opts);while(q.options.size()>4)q.options.remove(q.options.size()-1);if(q.options.size()<2)return null;Collections.shuffle(q.options,rnd);return q;
    }

    private List<Word> unitWords(CourseUnit u){List<Word> out=new ArrayList<>();for(Integer id:u.wordIds){Word w=repo.byId(id==null?0:id);if(w!=null)out.add(w);}return out;}
    private List<Word> wordsByIds(List<Integer> ids){List<Word> out=new ArrayList<>();if(ids==null)return out;for(Integer id:ids){Word w=repo.byId(id==null?0:id);if(w!=null)out.add(w);}return out;}
    private boolean hasUsableExample(Word w){return ExampleQuality.isUsable(w);}
    private String zh(Word w){return LearningLanguage.wordMeaning(w,progress);}

    private CourseQuestion intro(Word w){CourseQuestion q=new CourseQuestion();q.type=CourseQuestion.INTRO;q.word=w;q.prompt=LearningLanguage.ui(progress,"先认识这个表达","Learn this expression first");q.display=w.word;boolean safe=hasUsableExample(w);q.support=zh(w)+(safe?"\n\n"+w.example+"\n"+LearningLanguage.sentenceSupport(repo,w.example,w.exampleZh,progress):"");q.answer=w.word;q.autoPlayAudio=true;return q;}
    private CourseQuestion meaning(Word w,CourseUnit u,Random rnd){return meaning(w,u,rnd,false);}
    private CourseQuestion meaning(Word w,CourseUnit u,Random rnd,boolean teachBeforeTest){CourseQuestion q=new CourseQuestion();q.type=CourseQuestion.MEANING;q.word=w;q.prompt=LearningLanguage.ui(progress,"选择正确意思","Choose the correct meaning");q.display=w.word;q.answer=zh(w);q.dimension=ProgressStore.DIM_MEANING;q.teachBeforeTest=teachBeforeTest;q.options.addAll(chineseOptions(w,u,rnd));return q;}
    private CourseQuestion listen(Word w,CourseUnit u,Random rnd){CourseQuestion q=new CourseQuestion();q.type=CourseQuestion.LISTEN;q.word=w;q.prompt=LearningLanguage.ui(progress,"听音频，选择你听到的意大利语","Listen and choose the Italian you hear");q.display="🔊";q.support=zh(w);q.answer=w.word;q.dimension=ProgressStore.DIM_LISTENING;q.autoPlayAudio=true;q.options.addAll(italianOptions(w,u,rnd));return q;}
    private CourseQuestion spellHint(Word w){CourseQuestion q=new CourseQuestion();q.type=CourseQuestion.SPELL_HINT;q.word=w;q.prompt=LearningLanguage.ui(progress,"根据辅助语言和提示补出意大利语","Use the support-language hint to complete the Italian");q.display=zh(w);q.answer=w.word;q.dimension=ProgressStore.DIM_SPELLING;q.hint=mask(w.word);return q;}
    private CourseQuestion active(Word w){CourseQuestion q=new CourseQuestion();q.type=CourseQuestion.ACTIVE;q.word=w;q.prompt=LearningLanguage.ui(progress,"不要看选项，写出意大利语","Write the Italian without choices");q.display=zh(w);q.answer=w.word;q.dimension=ProgressStore.DIM_SPELLING;q.hint=LearningLanguage.ui(progress,"想不起来也没关系，提交后会看到答案","If you cannot recall it, submit to reveal the answer");return q;}
    private CourseQuestion cloze(Word w,CourseUnit u,Random rnd){CourseQuestion q=new CourseQuestion();q.type=CourseQuestion.CLOZE;q.word=w;q.prompt=LearningLanguage.ui(progress,"把句子补完整","Complete the sentence");q.display=replaceIgnoreCase(w.example,w.word,"_____");q.support=LearningLanguage.sentenceSupport(repo,w.example,w.exampleZh,progress);q.answer=w.word;q.dimension=ProgressStore.DIM_MEANING;q.options.addAll(clozeOptions(w,u,rnd));return q;}
    private CourseQuestion exampleMeaning(Word w,CourseUnit u,Random rnd){CourseQuestion q=new CourseQuestion();q.type=CourseQuestion.EXAMPLE_MEANING;q.word=w;q.prompt=LearningLanguage.ui(progress,"这句话是什么意思？","What does this sentence mean?");q.display=w.example;q.answer=LearningLanguage.sentenceSupport(repo,w.example,w.exampleZh,progress);q.dimension=ProgressStore.DIM_MEANING;q.options.add(q.answer);for(Word d:safeExampleDistractors(w,u,rnd,Math.max(8,u.wordIds.size()))){String z=LearningLanguage.sentenceSupport(repo,d.example,d.exampleZh,progress);if(!q.options.contains(z))q.options.add(z);if(q.options.size()>=4)break;}Collections.shuffle(q.options,rnd);return q;}

    private List<String> chineseOptions(Word target,CourseUnit u,Random rnd){
        List<String> x=new ArrayList<>();x.add(zh(target));
        for(Word d:distractors(target,u,rnd,Math.max(8,u.wordIds.size()))){
            String z=zh(d);if(meaningConflict(target,d)||x.contains(z))continue;x.add(z);if(x.size()>=4)break;
        }
        // Course choices stay inside the current unit; 2-4 clean choices are better than a
        // fourth unrelated or semantically ambiguous distractor.
        Collections.shuffle(x,rnd);return x;
    }
    private List<String> italianOptions(Word target,CourseUnit u,Random rnd){List<String> x=new ArrayList<>();x.add(target.word);for(Word d:distractors(target,u,rnd,Math.max(8,u.wordIds.size()))){if(!x.contains(d.word))x.add(d.word);if(x.size()>=4)break;}Collections.shuffle(x,rnd);return x;}
    private List<String> clozeOptions(Word target,CourseUnit u,Random rnd){
        List<String> x=new ArrayList<>();x.add(target.word);
        List<Word> pool=unitWords(u);Collections.shuffle(pool,rnd);
        for(Word d:pool){if(d.id==target.id||!sameGrammarClass(target,d)||x.contains(d.word))continue;x.add(d.word);if(x.size()>=4)break;}
        if(x.size()<4)for(Word d:pool){if(d.id==target.id||x.contains(d.word))continue;x.add(d.word);if(x.size()>=4)break;}
        Collections.shuffle(x,rnd);return x;
    }
    private boolean sameGrammarClass(Word a,Word b){
        String ap=safe(a.partOfSpeech),bp=safe(b.partOfSpeech);if(!ap.equals(bp))return false;
        if("noun".equals(ap)){String ag=safe(a.gender),bg=safe(b.gender);if(!ag.isEmpty()&&!bg.isEmpty()&&!ag.equals(bg))return false;}
        return true;
    }
    private List<Word> safeExampleDistractors(Word target,CourseUnit u,Random rnd,int max){List<Word> pool=unitWords(u);Collections.shuffle(pool,rnd);List<Word> out=new ArrayList<>();for(Word w:pool){if(w.id==target.id||!ExampleQuality.isUsable(w))continue;out.add(w);if(out.size()>=max)break;}return out;}
    private List<Word> distractors(Word target,CourseUnit u,Random rnd,int max){List<Word> pool=unitWords(u);Collections.shuffle(pool,rnd);List<Word> out=new ArrayList<>();for(Word w:pool){if(w.id==target.id)continue;out.add(w);if(out.size()>=max)break;}return out;}

    private boolean meaningConflict(Word a,Word b){
        if(a==null||b==null)return false;
        return glossOverlap(a.chinese,b.chinese)||glossOverlap(a.english,b.english);
    }
    private boolean glossOverlap(String a,String b){
        java.util.Set<String> x=glossParts(a),y=glossParts(b);
        for(String s:x)if(y.contains(s))return true;
        return false;
    }
    private java.util.Set<String> glossParts(String s){
        java.util.Set<String> out=new java.util.HashSet<>();
        if(s==null)return out;
        for(String p:s.toLowerCase(Locale.ROOT).split("[|;/；，、,]+")){String q=p.replaceAll("[()（）\\[\\]{}]"," ").replaceAll("\\s+"," ").trim();if(q.length()>=2)out.add(q);}
        return out;
    }

    private String mask(String s){if(s==null||s.isEmpty())return "";StringBuilder b=new StringBuilder();int letters=0;for(int i=0;i<s.length();i++){char c=s.charAt(i);if(Character.isLetter(c)){b.append(letters++%2==0?c:'_');}else b.append(c);}return b.toString();}
    private String replaceIgnoreCase(String src,String needle,String repl){int i=src.toLowerCase(Locale.ROOT).indexOf(needle.toLowerCase(Locale.ROOT));if(i<0)return src;return src.substring(0,i)+repl+src.substring(i+needle.length());}
    private String safe(String s){return s==null?"":s.trim();}
    private String join(List<String> xs,String sep){StringBuilder b=new StringBuilder();for(String x:xs){if(b.length()>0)b.append(sep);b.append(x);}return b.toString();}
}
