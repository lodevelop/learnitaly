#!/usr/bin/env python3
"""Run the shipped Java filter and verify bilingual search/unlock boundaries."""
from pathlib import Path
import subprocess,tempfile,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/italiano2774/nativeapp'
HARNESS=r'''
import com.italiano2774.nativeapp.PracticeFilter;
import com.italiano2774.nativeapp.UiEnglishTranslator;
public class PracticeFilterCheck {
    static int checks=0;
    static void require(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    static boolean match(String query,String category,boolean only,boolean unlocked){
        return PracticeFilter.matches(query,category,"listen",only,unlocked,"听力与开口","listening speaking 听力 开口");
    }
    static void eq(String a,String b){require(a.equals(b),a+" != "+b);}
    public static void main(String[] args){
        require(match("","all",false,true),"Empty search lists practices");
        require(match("听力","all",false,true),"Chinese search");
        require(match("LISTENING","all",false,true),"Case-insensitive English");
        require(match(" listening  speaking ","listen",false,true),"Multiple search tokens");
        require(!match("listening grammar","all",false,true),"All tokens must match");
        require(!match("听力","words",false,true),"Category narrows search");
        require(!match("listening","listen",true,false),"Unlocked-only cannot show locked rows");
        require(match("listening","listen",false,false),"Locked rows discoverable without changing access");
        require(match("   ","listen",true,true),"Whitespace query");
        require(!match("unrelated","all",false,true),"No results");
        require(PracticeFilter.matches("caffe","words","words",false,true,"Caffè",""),"Italian accents");
        require(PracticeFilter.matches("拼写","words","words",false,true,"Spelling practice","拼写 words"),"Chinese query in English UI");
        require(PracticeFilter.matches("spelling","words","words",false,true,"拼写练习","spelling words"),"English query in Chinese UI");
        require(!match("[.*","all",false,true),"Search text is literal, not regex");
        require(match(null,"all",false,true),"Null query means all");
        eq(UiEnglishTranslator.translate("今天的意大利语"),"Today's Italian");
        eq(UiEnglishTranslator.translate("背单词"),"Practice vocabulary");
        eq(UiEnglishTranslator.translate("查看复习日历"),"Review calendar");
        eq(UiEnglishTranslator.translate("只看已解锁"),"Show unlocked only");
        eq(UiEnglishTranslator.translate("没有符合条件的练习，试试其他关键词或分类。"),"No matching practice. Try another keyword or category.");
        eq(UiEnglishTranslator.translate("今天准备学习多久（分钟）"),"Daily target in minutes");
        eq(UiEnglishTranslator.translate("词汇与复习"),"Vocabulary & review");
        eq(UiEnglishTranslator.translate("生活实战"),"Real-life practice");
        System.out.println("Practice filter and new UI translations: "+checks+" behavior checks passed");
    }
}
'''
with tempfile.TemporaryDirectory() as tmp:
    test=Path(tmp)/'PracticeFilterCheck.java';test.write_text(HARNESS,encoding='utf-8')
    subprocess.run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-encoding','UTF-8','-d',tmp,str(JAVA/'PracticeFilter.java'),str(JAVA/'UiEnglishTranslator.java'),str(test)],check=True)
    subprocess.run(['java','-cp',tmp,'PracticeFilterCheck'],check=True)
# Verify important Android layout contracts that cannot be covered by the JVM harness.
A='{http://schemas.android.com/apk/res/android}'
for file in ['fragment_course_home.xml','fragment_practice_hub.xml']:
    tree=ET.parse(ROOT/'app/src/main/res/layout'/file)
    ids=[e.get(A+'id') for e in tree.iter() if e.get(A+'id')]
    assert len(ids)==len(set(ids)),file+': duplicate view ID'
    for e in tree.iter():
        if e.tag.endswith('MaterialButton'):
            assert e.get(A+'layout_height')=='wrap_content',file+': fixed-height button can clip large text'
print('Home and practice layout IDs and adaptive button heights: passed')
