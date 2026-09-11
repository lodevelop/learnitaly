#!/usr/bin/env python3
"""Execute the real per-card Java state machine without Android or network access."""
from pathlib import Path
import subprocess, tempfile
ROOT=Path(__file__).resolve().parents[1]
HARNESS='''
import com.italiano2774.nativeapp.ErrorRepairSession;
public class RepairSessionCheck {
    static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        ErrorRepairSession s = new ErrorRepairSession();
        require(s.canCheck(), "Fresh card must allow independent recall");
        require(s.showAnswer(), "First reveal must record one lapse");
        require(!s.canCheck(), "Visible answer must block grading");
        s.complete();
        require(!s.isComplete(), "Reading a reference cannot repair the error");
        s.hideAnswer();
        require(s.canCheck(), "Hidden answer must allow recall");
        require(!s.showAnswer(), "Repeated reveals must not inflate lapses");
        require(!s.canCheck(), "Repeated reveal still blocks grading");
        s.hideAnswer(); s.complete();
        require(s.isComplete() && !s.canCheck(), "Successful recall cannot be counted twice");
        require(!s.showAnswer(), "Completed cards cannot record another lapse");
        s.reset();
        require(s.canCheck() && !s.isComplete() && !s.isAnswerVisible(), "Next card must start fresh");
        require(s.showAnswer(), "Next card has its own reveal count");
        s.reset();
        require(s.canCheck(), "Skipping a revealed card must reset state");
        s.complete();
        require(s.isComplete(), "Independent recall without reveal can complete");
        System.out.println("Error repair session: 12 behavior checks passed");
    }
}
'''
with tempfile.TemporaryDirectory() as tmp:
    harness=Path(tmp)/'RepairSessionCheck.java';harness.write_text(HARNESS)
    source=ROOT/'app/src/main/java/com/italiano2774/nativeapp/ErrorRepairSession.java'
    subprocess.run(['java','-m','jdk.compiler/com.sun.tools.javac.Main','-d',tmp,str(source),str(harness)],check=True)
    subprocess.run(['java','-cp',tmp,'RepairSessionCheck'],check=True)
