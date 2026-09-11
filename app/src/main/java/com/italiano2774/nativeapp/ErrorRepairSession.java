package com.italiano2774.nativeapp;

/** Per-card recall state. Looking at a reference is never a successful repair. */
public final class ErrorRepairSession {
    private boolean answerVisible, revealed, complete;
    public void reset() { answerVisible=false; revealed=false; complete=false; }
    public boolean showAnswer() {
        if(complete) return false;
        answerVisible=true;
        boolean first=!revealed;
        revealed=true;
        return first;
    }
    public void hideAnswer() { answerVisible=false; }
    public boolean isAnswerVisible() { return answerVisible; }
    public boolean canCheck() { return !answerVisible && !complete; }
    public void complete() { if(canCheck()) complete=true; }
    public boolean isComplete() { return complete; }
}
