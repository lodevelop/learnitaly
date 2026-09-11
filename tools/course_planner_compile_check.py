#!/usr/bin/env python3
"""Compile and execute the shipped planner, not a Python reimplementation."""
from pathlib import Path
import subprocess,tempfile
root=Path(__file__).resolve().parents[1]
j=root/'app/src/main/java/com/italiano2774/nativeapp'
harness='''
import com.italiano2774.nativeapp.CourseUnit;
import com.italiano2774.nativeapp.CourseDistributionPlanner;
import java.util.*;
public class PlannerCheck {
 public static void main(String[] args){
  CourseUnit u=new CourseUnit();u.lessonCount=6;u.index=7;
  for(int i=1;i<=30;i++)u.wordIds.add(i);
  Set<Integer> reviewed=new HashSet<>();
  for(int lesson=0;lesson<5;lesson++){
   List<Integer> ids=CourseDistributionPlanner.reinforcementIds(u,lesson);
   if(!ids.equals(CourseDistributionPlanner.reinforcementIds(u,lesson)))throw new AssertionError("Unstable ordering");
   if(ids.size()>CourseDistributionPlanner.reinforcementBudget(u,lesson))throw new AssertionError("Budget exceeded");
   for(int id:ids){
    if(!u.wordIds.contains(id)||!reviewed.add(id))throw new AssertionError("Wrong unit or repeated reinforcement");
    if(CourseDistributionPlanner.focusLesson(u,id)>lesson-2)throw new AssertionError("Spacing lost");
   }
  }
  if(reviewed.isEmpty())throw new AssertionError("No reinforcement exercised");
  List<Integer> challenge=CourseDistributionPlanner.challengeIds(u,10);
  if(challenge.size()!=10||new HashSet<>(challenge).size()!=10)throw new AssertionError("Challenge size/deduplication");
  System.out.println("Real Java planner compiled; deterministic ordering, spacing, budget and challenge checks passed");
 }
}
'''
with tempfile.TemporaryDirectory() as td:
 p=Path(td)/'PlannerCheck.java';p.write_text(harness)
 subprocess.run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-encoding','UTF-8','-d',td,str(j/'CourseUnit.java'),str(j/'CourseDistributionPlanner.java'),str(p)],check=True)
 subprocess.run(['java','-cp',td,'PlannerCheck'],check=True)
