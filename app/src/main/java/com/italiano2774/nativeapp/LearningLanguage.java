package com.italiano2774.nativeapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * v5.1 learner support-language layer.
 *
 * MODE_ZH   : Chinese UI + Chinese learning support.
 * MODE_EN   : English UI + English learning support.
 * MODE_BOTH : Chinese UI + Chinese/English learning support side by side.
 *
 * Italian is always the target language and is never translated away.
 */
public final class LearningLanguage {
    public static final int MODE_ZH=0, MODE_EN=1, MODE_BOTH=2;
    private static final WeakHashMap<TextView,TextState> STATES=new WeakHashMap<>();
    private static final WeakHashMap<View,Boolean> WATCHED=new WeakHashMap<>();
    private static final Pattern TOKEN=Pattern.compile("[\\p{L}’']+|[^\\p{L}’']+");

    private LearningLanguage(){}

    public static int mode(ProgressStore p){return p==null?MODE_BOTH:p.supportLanguageMode();}
    public static boolean englishUi(ProgressStore p){return mode(p)==MODE_EN;}
    public static boolean bilingualSupport(ProgressStore p){return mode(p)==MODE_BOTH;}

    public static String modeLabel(int mode){
        switch(mode){
            case MODE_ZH:return "中文";
            case MODE_EN:return "English";
            default:return "中英对照";
        }
    }

    /** Exact bilingual word meaning: all 2774 shipped words already have English glosses. */
    public static String wordMeaning(Word w,ProgressStore p){
        if(w==null)return "";
        String zh=safe(w.chinese),en=cleanEnglish(w.english);
        if(mode(p)==MODE_EN)return !en.isEmpty()?en:zh;
        if(mode(p)==MODE_BOTH){
            if(zh.isEmpty())return en;
            if(en.isEmpty()||zh.equalsIgnoreCase(en))return zh;
            return zh+" · "+en;
        }
        return !zh.isEmpty()?zh:en;
    }

    /** Source-language prompt for reverse recall. */
    public static String recallPrompt(Word w,ProgressStore p){return wordMeaning(w,p);}

    /** Chinese sentence support plus an English literal gloss generated from the audited lexicon. */
    public static String sentenceSupport(Context c,String italian,String chinese,ProgressStore p){return sentenceSupport(WordRepository.get(c),italian,chinese,p);}

    public static String sentenceSupport(WordRepository repo,String italian,String chinese,ProgressStore p){
        String zh=safe(chinese);String en=englishGloss(repo,italian);
        if(mode(p)==MODE_EN)return !en.isEmpty()?en:zh;
        if(mode(p)==MODE_BOTH){
            if(zh.isEmpty())return en;
            if(en.isEmpty())return zh;
            return zh+"\nEN: "+en;
        }
        return zh;
    }

    /** UI labels are English only in MODE_EN; bilingual mode keeps the uncluttered Chinese UI. */
    public static String ui(ProgressStore p,String zh,String en){return mode(p)==MODE_EN?en:zh;}
    public static String uiText(ProgressStore p,String source){return mode(p)==MODE_EN?UiEnglishTranslator.translate(source):source;}

    public static String stageName(String stage,ProgressStore p){
        String zh=CourseCurriculumRepository.stageName(stage);String en;
        if("A0".equals(stage))en="A0 Beginner";else if("A1".equals(stage))en="A1 Foundation";else if("A2".equals(stage))en="A2 Everyday communication";else en="B1 Independent communication";
        return ui(p,zh,en);
    }

    public static String promptLanguageName(ProgressStore p){
        if(mode(p)==MODE_EN)return "English";
        if(mode(p)==MODE_BOTH)return "中文 / English";
        return "中文";
    }

    /** Course data already ships a compact English title in titleIt. */
    public static String courseTitle(CourseUnit u,ProgressStore p){
        if(u==null)return "";String zh=safe(u.titleZh),en=safe(u.titleIt);
        if(mode(p)==MODE_EN)return !en.isEmpty()?en:zh;
        if(mode(p)==MODE_BOTH&& !en.isEmpty()&&!zh.equalsIgnoreCase(en))return zh+" · "+en;
        return !zh.isEmpty()?zh:en;
    }

    /** Title support for assets that ship Italian + Chinese titles but no edited English title. */
    public static String titleSupport(WordRepository repo,String italianTitle,String chineseTitle,ProgressStore p){
        String zh=safe(chineseTitle),en=englishGloss(repo,italianTitle);
        if(mode(p)==MODE_EN)return !en.isEmpty()?en:(!safe(italianTitle).isEmpty()?italianTitle:zh);
        if(mode(p)==MODE_BOTH){if(zh.isEmpty())return en;if(en.isEmpty())return zh;return zh+" · EN: "+en;}
        return zh;
    }

    public static String englishGloss(Context c,String italian){return englishGloss(WordRepository.get(c),italian);}

    public static String englishGloss(WordRepository repo,String italian){
        if(italian==null||italian.trim().isEmpty())return "";
        StringBuilder out=new StringBuilder();Matcher m=TOKEN.matcher(italian);
        while(m.find()){
            String t=m.group();
            if(!containsLetter(t)){out.append(t);continue;}
            String key=t.toLowerCase(Locale.ROOT).replace('’','\'');
            String phrase=basicEnglish(key);
            if(!phrase.isEmpty()){out.append(matchCase(t,phrase));continue;}
            Word w=repo.lookupSurface(t);String en=w==null?"":firstEnglish(w.english);
            out.append(en.isEmpty()?t:matchCase(t,en));
        }
        String s=out.toString().replaceAll("\\s+([,.!?;:])","$1").replaceAll("\\s+"," ").trim();
        return s;
    }

    private static boolean containsLetter(String s){for(int i=0;i<s.length();i++)if(Character.isLetter(s.charAt(i)))return true;return false;}
    private static String firstEnglish(String s){
        s=cleanEnglish(s);if(s.isEmpty())return "";int bar=s.indexOf('|');if(bar>=0)s=s.substring(0,bar).trim();return s.replaceAll("\\s*\\([^)]*\\)\\s*"," ").trim();
    }
    private static String cleanEnglish(String s){return safe(s).replaceAll("\\s+"," ").trim();}
    private static String matchCase(String src,String en){if(src.length()>0&&Character.isUpperCase(src.charAt(0))&&en.length()>0)return Character.toUpperCase(en.charAt(0))+en.substring(1);return en;}

    private static String basicEnglish(String s){
        switch(s){
            case "il":case "lo":case "la":case "i":case "gli":case "le":return "the";
            case "un":case "uno":case "una":case "un'":return "a";
            case "e":return "and"; case "o":return "or"; case "ma":return "but";
            case "di":return "of"; case "del":case "della":case "dei":case "delle":return "of the";
            case "a":return "to"; case "al":case "alla":case "ai":case "alle":return "to the";
            case "da":return "from"; case "dal":case "dalla":return "from the";
            case "in":return "in"; case "nel":case "nella":case "nei":case "nelle":return "in the";
            case "con":return "with"; case "per":return "for"; case "su":return "on";
            case "che":return "that"; case "non":return "not"; case "sì":return "yes"; case "no":return "no";
            case "io":return "I";case "tu":return "you";case "lui":return "he";case "lei":return "she";case "noi":return "we";case "voi":return "you";case "loro":return "they";
            case "mi":return "me";case "ti":return "you";case "ci":return "us";case "vi":return "you";
            default:return "";
        }
    }

    /** English-only UI retrofit for the legacy screens that still contain Chinese literals. */
    public static void watchUi(View root,ProgressStore p){
        if(root==null||p==null)return;applyUi(root,p);
        synchronized(WATCHED){if(WATCHED.containsKey(root))return;WATCHED.put(root,Boolean.TRUE);}
        ViewTreeObserver vto=root.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(()->{if(root.isAttachedToWindow())applyUi(root,p);});
    }

    public static void applyUi(View root,ProgressStore p){
        if(root==null||p==null)return;
        if(root instanceof TextView)translateTextView((TextView)root,p);
        if(root instanceof ViewGroup){ViewGroup g=(ViewGroup)root;for(int i=0;i<g.getChildCount();i++)applyUi(g.getChildAt(i),p);}
    }

    private static void translateTextView(TextView v,ProgressStore p){
        // Editable contents belong to the learner, including search terms and answers.
        // Translate hints only; never replace typed text during a layout pass.
        if(v instanceof android.widget.EditText){
            CharSequence hint=v.getHint();
            if(hint!=null&&mode(p)==MODE_EN&&containsChinese(hint.toString()))v.setHint(UiEnglishTranslator.translate(hint.toString()));
            return;
        }
        CharSequence cs=v.getText();if(cs==null)return;String current=cs.toString();
        TextState st=STATES.get(v);String base;
        if(st!=null&&current.equals(st.rendered))base=st.base;else base=current;
        String rendered=base;
        if(mode(p)==MODE_EN&&containsChinese(base))rendered=UiEnglishTranslator.translate(base);
        if(!rendered.equals(current))v.setText(rendered);
        STATES.put(v,new TextState(base,rendered));
        CharSequence hint=v.getHint();if(hint!=null&&mode(p)==MODE_EN&&containsChinese(hint.toString()))v.setHint(UiEnglishTranslator.translate(hint.toString()));
    }

    public static void applyBottomNav(BottomNavigationView nav,ProgressStore p){
        if(nav==null)return;
        nav.getMenu().findItem(R.id.nav_today).setTitle(ui(p,"学习","Learn"));
        nav.getMenu().findItem(R.id.nav_calendar).setTitle(ui(p,"课程","Course"));
        nav.getMenu().findItem(R.id.nav_practice).setTitle(ui(p,"练习","Practice"));
        nav.getMenu().findItem(R.id.nav_settings).setTitle(ui(p,"我的","Me"));
    }

    private static boolean containsChinese(String s){return s!=null&&s.matches(".*[\\u4e00-\\u9fff].*");}
    private static String safe(String s){return s==null?"":s.trim();}
    private static final class TextState{final String base,rendered;TextState(String b,String r){base=b;rendered=r;}}
}
