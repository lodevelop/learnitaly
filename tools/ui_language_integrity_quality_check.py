#!/usr/bin/env python3
"""v5.4.0 gate: English UI must render whole sentences, never Chinese/English fragment soup."""
from pathlib import Path
import subprocess, tempfile, shutil, sys, textwrap
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/italiano2774/nativeapp'
errors=[]

def need(text, token, label):
    if token not in text: errors.append(f'{label} missing: {token}')

def read(name):
    p=JAVA/name
    if not p.exists(): errors.append('missing '+str(p.relative_to(ROOT))); return ''
    return p.read_text(encoding='utf-8')

ui=read('UiEnglishTranslator.java')
lang=read('LearningLanguage.java')
home=read('CourseHomeFragment.java')
course_map=read('CourseMapFragment.java')
practice=read('PracticeHubFragment.java')
lesson=read('CourseLessonFragment.java')

need(ui,'return containsChinese(out)?input:out;','no-partial-translation safety rule')
need(ui,'private static String dynamic(String s)','whole-sentence dynamic translator')
need(lang,'uiText(ProgressStore p,String source)','central dynamic UI helper')
need(lang,'stageName(String stage,ProgressStore p)','localized CEFR stage helper')
for token in ['tasks complete · about ','Today: ','LearningLanguage.uiText(progress,next.title)','Listening, speaking, reading and writing are mixed in automatically']:
    need(home,token,'course home whole-sentence templates')
for token in ['units completed','core words','LearningLanguage.stageName(s,progress)']:
    need(course_map,token,'course map whole-sentence templates')
for token in ['Weekly practical exam','Real-life task map','Personal sentence repair','Listening & speaking']:
    need(practice,token,'practice hub whole-sentence templates')
need(lesson,'LearningLanguage.stageName(unit.stage,progress)','course lesson localized header')

# Compile the pure-Java translator and verify the exact strings reported in real-device screenshots.
javac=shutil.which('javac'); java=shutil.which('java')
if not javac or not java:
    errors.append('javac/java missing: cannot execute UI translation self-test')
else:
    with tempfile.TemporaryDirectory() as td:
        td=Path(td); pkg=td/'com/italiano2774/nativeapp'; pkg.mkdir(parents=True)
        shutil.copy2(JAVA/'UiEnglishTranslator.java',pkg/'UiEnglishTranslator.java')
        test=td/'UiTranslationSelfTest.java'
        test.write_text(textwrap.dedent(r'''
            import com.italiano2774.nativeapp.UiEnglishTranslator;
            public class UiTranslationSelfTest {
              static void eq(String input,String expected){
                String actual=UiEnglishTranslator.translate(input);
                if(!expected.equals(actual))throw new RuntimeException("Mismatch for input: "+input+"\nexpected="+expected+"\nactual="+actual);
              }
              public static void main(String[] args){
                eq("B1 \u72ec\u7acb\u4ea4\u6d41 \u00b7 \u7b2c36\u5355\u5143","B1 Independent communication \u00b7 Unit 36");
                eq("\u4eca\u5929\u76ee\u6807 60 \u5206\u949f \u00b7 \u5df2\u5b66\u4e60 22 \u5206\u949f \u00b7 \u8ba1\u5212\u8fd8\u5269\u7ea6 46 \u5206\u949f","Today: 60 min \u00b7 Studied 22 min \u00b7 About 46 min left");
                eq("2 / 10\u9879\u5b8c\u6210 \u00b7 \u8fd8\u5269\u7ea646\u5206\u949f \u00b7 \u4eca\u65e5\u76ee\u680760\u5206\u949f","2 / 10 tasks complete \u00b7 about 46 min left \u00b7 60 min goal");
                eq("36 / 36 \u5355\u5143\u5b8c\u6210","36 / 36 units completed");
                eq("5 / 5 \u5173 \u00b7 18\u4e2a\u6838\u5fc3\u8bcd","5 / 5 stages \u00b7 18 core words");
                eq("\ud83e\uddfe \u4e2a\u4eba\u9519\u53e5\u672c","\ud83e\uddfe Personal sentence repair");
                eq("\u4e3b\u8bfe\u7a0b\u5df2\u7ecf\u81ea\u52a8\u5b89\u6392\u590d\u4e60\u3002\u8fd9\u91cc\u53ea\u5728\u4f60\u60f3\u989d\u5916\u7ec3\u67d0\u4e00\u9879\u65f6\u4f7f\u7528\u3002","Your main course already schedules reviews automatically. Use this page only when you want extra practice in a specific skill.");
                String unknown="\u8fd9\u662f\u4e00\u4e2a\u672a\u8986\u76d6\u7684\u4e2d\u6587\u52a8\u6001\u53e5\u5b50 5\u6761";
                eq(unknown,unknown); // never return a half-translated hybrid
              }
            }
        '''),encoding='utf-8')
        out=td/'out'; out.mkdir()
        c=subprocess.run([javac,'-encoding','UTF-8','-d',str(out),str(pkg/'UiEnglishTranslator.java'),str(test)],capture_output=True,text=True)
        if c.returncode: errors.append('UiEnglishTranslator javac failed: '+c.stderr.strip())
        else:
            r=subprocess.run([java,'-cp',str(out),'UiTranslationSelfTest'],capture_output=True,text=True)
            if r.returncode: errors.append('UI translation self-test failed: '+(r.stderr.strip() or r.stdout.strip()))

if errors:
    for e in errors: print('ERROR:',e)
    print('UI LANGUAGE INTEGRITY CHECK FAILED:',len(errors),'error(s)')
    sys.exit(1)
print('UI language integrity OK: screenshot regressions covered; English mode uses whole-sentence templates and never emits partial Chinese/English fragment translations.')
