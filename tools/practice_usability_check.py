#!/usr/bin/env python3
from pathlib import Path
import subprocess,tempfile
root=Path(__file__).resolve().parents[1]
j=root/'app/src/main/java/com/italiano2774/nativeapp'
harness='''
import com.italiano2774.nativeapp.RecentPracticeHistory;
import com.italiano2774.nativeapp.CourseAttemptPolicy;
public class UsabilityCheck {
 static int count=0;
 static void yes(boolean v){count++;if(!v)throw new AssertionError("Check "+count);}
 public static void main(String[] args){
  String a="button_simple_listen",b="button_simple_spelling",c="button_adv_reading",d="button_simple_wrong";
  yes(RecentPracticeHistory.entries(null).isEmpty());
  String saved=RecentPracticeHistory.record("",a);yes(saved.equals(a));
  saved=RecentPracticeHistory.record(saved,b);saved=RecentPracticeHistory.record(saved,c);
  yes(saved.equals(c+","+b+","+a));
  saved=RecentPracticeHistory.record(saved,b);yes(saved.equals(b+","+c+","+a));
  saved=RecentPracticeHistory.record(saved,d);yes(saved.equals(d+","+b+","+c));
  yes(RecentPracticeHistory.record(saved,"bad/key").equals(saved));
  yes(RecentPracticeHistory.entries("junk,"+a+","+a+","+b).size()==2);
  yes(!CourseAttemptPolicy.isCorrect(true,true));
  yes(!CourseAttemptPolicy.isCorrect(true,false));
  yes(CourseAttemptPolicy.isCorrect(false,true));
  yes(!CourseAttemptPolicy.isCorrect(false,false));
  yes(CourseAttemptPolicy.resumeIndex(2,8,false)==2);
  yes(CourseAttemptPolicy.resumeIndex(2,8,true)==3);
  yes(CourseAttemptPolicy.resumeIndex(3,8,false)==3);
  yes(CourseAttemptPolicy.resumeIndex(7,8,true)==8);
  yes(CourseAttemptPolicy.resumeIndex(8,8,true)==8);
  yes(CourseAttemptPolicy.resumeIndex(0,0,true)==0);
  yes(CourseAttemptPolicy.resumeIndex(20,8,false)==8);
  System.out.println(count+" recent-practice and grading/resume checks passed");
 }
}
'''
with tempfile.TemporaryDirectory() as td:
 p=Path(td)/'UsabilityCheck.java';p.write_text(harness)
 subprocess.run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-d',td,str(j/'RecentPracticeHistory.java'),str(j/'CourseAttemptPolicy.java'),str(p)],check=True)
 subprocess.run(['java','-cp',td,'UsabilityCheck'],check=True)
