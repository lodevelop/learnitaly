#!/usr/bin/env python3
"""v5.4.0 Chinese / English / bilingual learner-support integration gate."""
from pathlib import Path
import json, re, sys
ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'app/src/main/java/com/italiano2774/nativeapp'
errors=[]
def err(x): errors.append(x)
def read(rel):
    p=ROOT/rel
    if not p.exists(): err('missing '+rel); return ''
    return p.read_text(encoding='utf-8')
def need(text,token,label):
    if token not in text: err(label+' missing: '+token)

build=read('app/build.gradle')
need(build,'def defaultVersionCode = 88','versionCode')
need(build,"versionName '5.4.0-native'",'versionName')
need(build,"applicationId 'com.italiano2774.nativeapp'",'fixed applicationId')

lang=read('app/src/main/java/com/italiano2774/nativeapp/LearningLanguage.java')
ui=read('app/src/main/java/com/italiano2774/nativeapp/UiEnglishTranslator.java')
for token in ['MODE_ZH=0','MODE_EN=1','MODE_BOTH=2','wordMeaning(','sentenceSupport(','englishGloss(','courseTitle(','watchUi(','applyBottomNav(','uiText(','stageName(']:
    need(lang,token,'LearningLanguage')
need(ui,'translate(String input)','offline UI translator')
need(ui,'return containsChinese(out)?input:out;','whole-sentence fallback safety')

store=read('app/src/main/java/com/italiano2774/nativeapp/ProgressStore.java')
for token in ['supportLanguageMode()','setSupportLanguageMode','"supportLanguageMode"','"support_language_mode"','o.put("version",32)']:
    need(store,token,'persisted support language / backup v32')
settings=read('app/src/main/java/com/italiano2774/nativeapp/SettingsFragment.java')
layout=read('app/src/main/res/layout/fragment_settings.xml')
for token in ['spinner_support_language','中文','English','中英对照']:
    need(settings+layout,token,'settings language selector')
onboarding=read('app/src/main/java/com/italiano2774/nativeapp/OnboardingFragment.java')
onboarding_layout=read('app/src/main/res/layout/fragment_onboarding.xml')
for token in ['spinner_onboarding_support_language','setSupportLanguageMode','English','中英对照']:
    need(onboarding+onboarding_layout,token,'first-run language selector')
main=read('app/src/main/java/com/italiano2774/nativeapp/MainActivity.java')
for token in ['5.4.0-preupgrade','LearningLanguage.applyBottomNav','LearningLanguage.watchUi','FragmentLifecycleCallbacks']:
    need(main,token,'app-wide language wiring')

# All shipped words must have both Chinese and English support glosses.
raw=json.load(open(ROOT/'app/src/main/assets/words.json',encoding='utf-8'))
words=raw.get('words',[]) if isinstance(raw,dict) else raw
if len(words)!=2774: err(f'expected 2774 words, got {len(words)}')
missing_zh=[w.get('id') for w in words if not str(w.get('chinese','')).strip()]
missing_en=[w.get('id') for w in words if not str(w.get('english','')).strip()]
if missing_zh: err(f'{len(missing_zh)} words missing Chinese meaning; first={missing_zh[:8]}')
if missing_en: err(f'{len(missing_en)} words missing English meaning; first={missing_en[:8]}')

repo=read('app/src/main/java/com/italiano2774/nativeapp/WordRepository.java')
for token in ['englishPromptCount','isEnglishPromptUnique','isSupportPromptUnique']:
    need(repo,token,'English reverse-recall uniqueness')
review=read('app/src/main/java/com/italiano2774/nativeapp/SmartReviewModeEngine.java')
need(review,'isSupportPromptUnique','smart-review support-language uniqueness')

# Major learner-facing routes must use the same support-language layer rather than bypassing it.
checks={
 'CourseLessonEngine.java':['LearningLanguage.wordMeaning','LearningLanguage.sentenceSupport'],
 'StudySessionFragment.java':['LearningLanguage.wordMeaning','LearningLanguage.sentenceSupport'],
 'WordAdapter.java':['LearningLanguage.wordMeaning','LearningLanguage.sentenceSupport'],
 'PracticeFragment.java':['LearningLanguage.wordMeaning'],
 'LevelExamFragment.java':['LearningLanguage.wordMeaning'],
 'WeeklyExamEngine.java':['LearningLanguage.wordMeaning','LearningLanguage.sentenceSupport'],
 'PlacementTestFragment.java':['LearningLanguage.wordMeaning'],
 'DailySpeakingChallengeFragment.java':['LearningLanguage.sentenceSupport'],
 'ActiveRecallFragment.java':['LearningLanguage.sentenceSupport'],
 'SentenceReviewFragment.java':['LearningLanguage.sentenceSupport'],
 'ShadowingFragment.java':['LearningLanguage.sentenceSupport'],
 'IntensiveListeningFragment.java':['LearningLanguage.sentenceSupport'],
 'ListeningCourseFragment.java':['LearningLanguage.sentenceSupport'],
 'ListeningSpeakingFragment.java':['LearningLanguage.sentenceSupport'],
 'WeakWordStoryFragment.java':['LearningLanguage.sentenceSupport'],
 'ReadingDetailFragment.java':['LearningLanguage.sentenceSupport','LearningLanguage.wordMeaning'],
 'PhraseAdapter.java':['LearningLanguage.sentenceSupport'],
 'CourseHomeFragment.java':['LearningLanguage.courseTitle'],
 'CourseMapFragment.java':['LearningLanguage.courseTitle'],
}
for fn,tokens in checks.items():
    text=read('app/src/main/java/com/italiano2774/nativeapp/'+fn)
    for token in tokens: need(text,token,fn)

cm=read('codemagic.yaml')
need(cm,'v5.4.0','Codemagic release identity')
need(cm,'python3 tools/bilingual_support_quality_check.py','Codemagic bilingual gate')
need(cm,'python3 tools/ui_language_integrity_quality_check.py','Codemagic whole-sentence UI integrity gate')
need(cm,'zhongxue_release','fixed signing identity')

# The English sentence layer must remain explicitly local/lexicon based: no network translation API.
if re.search(r'googletrans|translate\.google|deepl|microsofttranslator|openai',lang,re.I):
    err('LearningLanguage must remain offline; network translation dependency found')

if errors:
    for e in errors: print('ERROR:',e)
    print('BILINGUAL SUPPORT CHECK FAILED:',len(errors),'error(s)')
    sys.exit(1)
print('Bilingual support OK: v5.4.0 whole-sentence English UI, 2774/2774 Chinese+English word meanings, 3 support modes, v32 persistence, major learning routes connected, fixed signing preserved.')
