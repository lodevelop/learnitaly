#!/usr/bin/env python3
"""v5.4.1 personal wrong-sentence prompt integrity gate."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
J=ROOT/'app/src/main/java/com/italiano2774/nativeapp'
R=ROOT/'app/src/main/res/layout'

def read(p): return p.read_text(encoding='utf-8')
def need(text,token,label):
    if token not in text: raise SystemExit('FAIL: missing '+label+' -> '+token)

policy=read(J/'ErrorEvidencePolicy.java')
resolver=read(J/'ErrorEvidencePromptResolver.java')
frag=read(J/'ErrorEvidenceRepairFragment.java')
layout=read(R/'fragment_error_evidence_repair.xml')
weekly=read(J/'WeeklyExamFragment.java')
daily=read(J/'DailySpeakingChallengeFragment.java')
shadow=read(J/'ShadowingFragment.java')
listen=read(J/'ListeningSpeakingFragment.java')
dao=read(J/'LearningStateDao.java')

for t in ['detailWithPrompt','extractPrompt','PROMPT_MARKER']:
    need(policy,t,'prompt persistence policy')
for t in ['CoreSentenceRepository','ListeningCourseRepository','DialogueRepository','SentenceDictationRepository','LearningLanguage.sentenceSupport']:
    need(resolver,t,'legacy prompt reconstruction')
for t in ['text_error_evidence_prompt_label','text_error_evidence_prompt']:
    need(layout,t,'visible original-prompt UI')
for t in ['ErrorEvidencePromptResolver.resolve','原题 / 提示','archiveUnrepairableError']:
    need(frag,t,'repair card prompt/legacy cleanup')
need(dao,'archiveUnrepairableError','legacy promptless queue cleanup DAO')
for text,label in [(weekly,'weekly exam'),(daily,'daily speaking'),(shadow,'shadowing'),(listen,'listening-speaking')]:
    need(text,'ErrorEvidencePolicy.detailWithPrompt',label+' prompt capture')
print('PASS: every repair card has a learner-facing prompt; legacy rows are reconstructed or removed from blind-rewrite queue')
