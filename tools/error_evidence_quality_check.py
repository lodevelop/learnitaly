#!/usr/bin/env python3
"""v5.4.0 personal wrong-sentence queue isolation and repair-loop quality gate."""
from pathlib import Path
import sys
ROOT=Path(__file__).resolve().parents[1]
APP=ROOT/'app'
errors=[]
def read(path):
    p=ROOT/path
    if not p.exists():
        errors.append('missing '+path);return ''
    return p.read_text(encoding='utf-8')
def need(text,marker,label):
    if marker not in text: errors.append(label+' missing '+marker)

build=read('app/build.gradle');need(build,"versionName '5.4.0-native'",'version');need(build,'def defaultVersionCode = 88','version')
main=read('app/src/main/java/com/italiano2774/nativeapp/MainActivity.java');need(main,'5.4.0-preupgrade','upgrade backup');need(main,'openErrorEvidenceRepair','navigation');need(main,'unresolvedPracticeErrorCount','pending-error reconciliation')
policy=read('app/src/main/java/com/italiano2774/nativeapp/ErrorEvidencePolicy.java')
for m in ['isRepairEligible','listen_speak_output','weekly_exam_output','modeLabel','attemptText','cleanDetail'] : need(policy,m,'error evidence policy')
need(main,'ReminderScheduler.createChannel(this);reconcilePendingErrorRepairs();','startup queue reconciliation')
entity=read('app/src/main/java/com/italiano2774/nativeapp/ErrorRecordEntity.java');need(entity,'repairedAt','error entity');need(entity,'repairAttempts','error entity')
db=read('app/src/main/java/com/italiano2774/nativeapp/LearningDatabase.java');need(db,'version = 6','Room schema');need(db,'MIGRATION_5_6','Room migration');need(db,'ADD COLUMN `repairedAt`','Room migration');need(db,'ADD COLUMN `repairAttempts`','Room migration')
dao=read('app/src/main/java/com/italiano2774/nativeapp/LearningStateDao.java')
for m in ['unresolvedPracticeErrors','unresolvedPracticeErrorCount','incrementMatchingErrorRepairAttempt','markMatchingErrorRepaired'] : need(dao,m,'error DAO')
for m in ["mode IN ('writing','freechat','daily_speaking'","listen_speak_output","weekly_exam_output","mode='listen_speak' AND wordId=0"] : need(dao,m,'sentence-only DAO filter')
if 'mode!=\'grammar\'' in dao: errors.append('error DAO still accepts every non-grammar record into wrong-sentence queue')
progress=read('app/src/main/java/com/italiano2774/nativeapp/ProgressStore.java');need(progress,'"version",32','backup format');need(progress,'pendingErrorRepairs()','pending state');need(progress,'markEvidenceRepairComplete','repair completion');need(progress,'error_evidence_repair','repair statistics');need(progress,'ErrorEvidencePolicy.isRepairEligible','pending counter isolation')
frag=read('app/src/main/java/com/italiano2774/nativeapp/ErrorEvidenceRepairFragment.java')
for m in ['unresolvedPracticeErrors(40)','ErrorCauseAnalyzer.analyzeSentence','markMatchingErrorRepaired','SentenceFsrsRepository.recordDimension','不算修复','markEvidenceRepairComplete','ErrorEvidencePolicy.modeLabel','ErrorEvidencePolicy.attemptText','ErrorEvidencePolicy.cleanDetail'] : need(frag,m,'repair fragment')
layout=read('app/src/main/res/layout/fragment_error_evidence_repair.xml')
for m in ['edit_error_evidence_answer','button_error_evidence_check','button_error_evidence_reveal','text_error_evidence_expected'] : need(layout,m,'repair layout')
writing=read('app/src/main/java/com/italiano2774/nativeapp/WritingFragment.java');need(writing,'"writing",p.reference,text','writing evidence');need(writing,'SentenceFsrsRepository.recordDimension','writing FSRS')
free=read('app/src/main/java/com/italiano2774/nativeapp/FreeConversationFragment.java');need(free,'outputNeedsRepair','free conversation evidence');need(free,'"freechat",expected,user','free conversation evidence')
plan=read('app/src/main/java/com/italiano2774/nativeapp/DailySmartPlanEngine.java');need(plan,'pendingEvidence=progress.pendingErrorRepairs()','daily planner');need(plan,'"error_evidence_repair"','daily planner');need(plan,'recoveryPrimary','daily planner budget guard')
home=read('app/src/main/java/com/italiano2774/nativeapp/CourseHomeFragment.java');need(home,'case "error_evidence_repair"','daily task router')
practice=read('app/src/main/java/com/italiano2774/nativeapp/PracticeHubFragment.java');need(practice,'button_simple_error_evidence','practice hub')
weak=read('app/src/main/java/com/italiano2774/nativeapp/WeaknessCenterFragment.java');need(weak,'button_diag_error_evidence','weakness center')
summary=read('app/src/main/java/com/italiano2774/nativeapp/DailySummaryFragment.java');need(summary,'button_summary_error_evidence','daily summary')
backup=read('app/src/main/java/com/italiano2774/nativeapp/LocalBackupManager.java');need(backup,'"repairedAt"','local backup');need(backup,'"repairAttempts"','local backup')

course=read('app/src/main/java/com/italiano2774/nativeapp/CourseLessonFragment.java')
need(course,'actual.trim().isEmpty()','blank course answer guard')
need(course,'"course",q.answer,actual','course analytics mode')
if 'course_v334' in course: errors.append('raw legacy course_v334 mode still emitted')
listen=read('app/src/main/java/com/italiano2774/nativeapp/ListeningSpeakingFragment.java');need(listen,'"listen_speak_output"','sentence-output mode')
weekly=read('app/src/main/java/com/italiano2774/nativeapp/WeeklyExamFragment.java');need(weekly,'"weekly_exam_output"','weekly sentence-output mode')
cm=read('codemagic.yaml');need(cm,'v5.4.0','Codemagic identity');need(cm,'python3 tools/error_evidence_quality_check.py','Codemagic gate')
if errors:
    print('ERROR EVIDENCE QUALITY CHECK FAILED')
    for e in errors: print(' -',e)
    sys.exit(1)
print('Error evidence quality check OK: sentence-only queue + human labels + startup reconciliation + persisted repair loop + backup chain')
