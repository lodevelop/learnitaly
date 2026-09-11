#!/usr/bin/env python3
"""v5.4.0 gate: audit fixed-course ownership, one-pass exposure balance and answer ambiguity."""
from pathlib import Path
import argparse, collections, json, re, sys

ROOT=Path(__file__).resolve().parents[1]
ASSETS=ROOT/'app/src/main/assets'
JAVA=ROOT/'app/src/main/java/com/italiano2774/nativeapp'
errors=[]
def err(x): errors.append(x)

def load_words():
    raw=json.load(open(ASSETS/'words.json',encoding='utf-8'))
    return raw.get('words',[]) if isinstance(raw,dict) else raw

def pre_lessons(u): return max(1,int(u.get('lessonCount',1))-1)
def focus_ids(u,lesson):
    ids=u.get('wordIds',[]); chunks=pre_lessons(u)
    lesson=max(0,min(chunks-1,lesson)); n=len(ids)
    a=int((lesson*n)//chunks); b=int(((lesson+1)*n)//chunks)
    if b<=a: b=min(n,a+1)
    return ids[a:b]
def focus_lesson(u,wid):
    for lesson in range(pre_lessons(u)):
        if wid in focus_ids(u,lesson): return lesson
    return -1
def review_budget(u,lesson):
    n=len(focus_ids(u,lesson))
    return 0 if n<3 else min(2,max(1,n//3))
def stable_score(u,lesson,wid):
    x=wid*1103515245+(lesson+1)*12345+(int(u.get('index',0))+1)*2654435761
    return x%2147483647
def reinforcement_ids(u,target_lesson):
    chunks=pre_lessons(u); target=max(0,min(chunks-1,target_lesson)); reviewed=set(); result=[]
    for lesson in range(target+1):
        budget=review_budget(u,lesson)
        eligible=[]
        for wid in u.get('wordIds',[]):
            if wid in reviewed: continue
            first=focus_lesson(u,wid)
            if first>=0 and first<=lesson-2: eligible.append(wid)
        eligible.sort(key=lambda wid: stable_score(u,lesson,wid))
        chosen=eligible[:budget]
        reviewed.update(chosen)
        if lesson==target: result=chosen
    return result
def challenge_ids(u,limit=10):
    reviewed=set()
    chunks=pre_lessons(u)
    for lesson in range(chunks): reviewed.update(reinforcement_ids(u,lesson))
    pool=[wid for wid in u.get('wordIds',[]) if wid not in reviewed]
    if len(pool)<min(6,limit): pool.extend(wid for wid in u.get('wordIds',[]) if wid in reviewed)
    pool.sort(key=lambda wid:(1+(wid in reviewed),1 if focus_lesson(u,wid)>=chunks-1 else 0,stable_score(u,chunks,wid)))
    return pool[:min(limit,len(pool))]

def norm_parts(s):
    out=set()
    for p in re.split(r'[|;/；，、,]+',s or ''):
        q=re.sub(r'\s+',' ',re.sub(r'[()（）\[\]{}]',' ',p.lower())).strip()
        if len(q)>=2: out.add(q)
    return out
def meaning_conflict(a,b):
    return bool(norm_parts(a.get('chinese'))&norm_parts(b.get('chinese')) or norm_parts(a.get('english'))&norm_parts(b.get('english')))

words=load_words(); by_id={int(w['id']):w for w in words}
units=json.load(open(ASSETS/'course_curriculum.json',encoding='utf-8')).get('units',[])
engine=(JAVA/'CourseLessonEngine.java').read_text(encoding='utf-8')
planner=(JAVA/'CourseDistributionPlanner.java').read_text(encoding='utf-8') if (JAVA/'CourseDistributionPlanner.java').exists() else ''
repo=(JAVA/'CourseCurriculumRepository.java').read_text(encoding='utf-8')

if len(words)!=2774: err(f'expected 2774 words, got {len(words)}')
if len(units)!=98: err(f'expected 98 units, got {len(units)}')

owners=collections.defaultdict(list); assignments=0
for u in units:
    ids=[int(x) for x in u.get('wordIds',[])]
    assignments+=len(ids)
    if len(ids)!=len(set(ids)): err(f"{u.get('id')}: duplicate word ids")
    for wid in ids: owners[wid].append(u.get('id'))
if assignments!=2774: err(f'expected 2774 assignments, got {assignments}')
if set(owners)!=set(by_id): err(f'course ownership mismatch: owners={len(owners)} lexicon={len(by_id)}')
for wid,us in owners.items():
    if len(us)!=1: err(f'word {wid} has {len(us)} owners: {us}')

# Static course paths should remain the original contiguous source-skill ranges.
for u in units:
    ids=[int(x) for x in u.get('wordIds',[])]
    if ids and ids!=list(range(ids[0],ids[-1]+1)):
        err(f"{u.get('id')}: source-course word range is no longer contiguous")

exposure=collections.Counter(); lesson_stats=[]; challenge_stats=[]
for u in units:
    ids=set(int(x) for x in u.get('wordIds',[])); chunks=pre_lessons(u)
    focused=[]; reviewed=set(); previous_targets=set()
    for lesson in range(chunks):
        f=focus_ids(u,lesson); r=reinforcement_ids(u,lesson)
        if not set(f+r)<=ids: err(f"{u.get('id')} L{lesson+1}: cross-unit target detected")
        focused.extend(f)
        for wid in r:
            if wid in reviewed: err(f"{u.get('id')} word {wid}: reinforced more than once before challenge")
            reviewed.add(wid)
            if focus_lesson(u,wid)>lesson-2: err(f"{u.get('id')} word {wid}: reinforced without one full lesson of spacing")
        if set(f)&set(r): err(f"{u.get('id')} L{lesson+1}: focus/review overlap")
        if r:
            focus_share=len(f)/(len(f)+len(r))
            if focus_share<0.75-1e-9: err(f"{u.get('id')} L{lesson+1}: focus share {focus_share:.2%} below 75%")
        if previous_targets and set(r)&previous_targets:
            err(f"{u.get('id')} L{lesson+1}: same reinforcement repeated in consecutive lesson")
        for wid in f+r: exposure[wid]+=1
        previous_targets=set(f+r)
        lesson_stats.append((u.get('id'),lesson+1,len(f),len(r)))
    if len(focused)!=len(u.get('wordIds',[])) or set(focused)!=ids or len(focused)!=len(set(focused)):
        err(f"{u.get('id')}: every unit word must be focused exactly once before challenge")
    c=challenge_ids(u,min(10,len(ids)))
    if len(c)!=len(set(c)) or not set(c)<=ids: err(f"{u.get('id')}: invalid challenge target set")
    before={wid:1+(wid in reviewed) for wid in ids}
    candidate=[wid for wid in ids if wid not in reviewed]
    if len(candidate)<min(6,len(c) or 10): candidate.extend(wid for wid in ids if wid in reviewed)
    ordered=sorted(candidate,key=lambda wid:(before[wid],1 if focus_lesson(u,wid)>=chunks-1 else 0,stable_score(u,chunks,wid)))[:len(c)]
    if c!=ordered: err(f"{u.get('id')}: challenge no longer prioritizes under-exposed words")
    for wid in c: exposure[wid]+=1
    challenge_stats.append((u.get('id'),len(c),sum(before[w] for w in c)/max(1,len(c))))

# One fixed-course pass should be broad, not hammer a small subset.
if exposure:
    mn,mx=min(exposure.values()),max(exposure.values())
    if mn<1: err(f'minimum one-pass exposure {mn} < 1')
    if mx>2: err(f'maximum one-pass exposure {mx} > 2')
    over2=[wid for wid,n in exposure.items() if n>2]
    if over2: err(f'{len(over2)} words exceed 2 scheduled target appearances')

# Every meaning question must have at least three clean same-unit distractors in both ZH/EN support modes.
ambiguous=[]
for u in units:
    pool=[by_id[int(wid)] for wid in u.get('wordIds',[])]
    for w in pool:
        clean=[d for d in pool if d['id']!=w['id'] and not meaning_conflict(w,d)]
        if len(clean)<3: ambiguous.append((u.get('id'),w.get('word'),len(clean)))
if ambiguous: err('not enough non-overlapping same-unit meaning distractors: '+str(ambiguous[:10]))

# Reported historical regression word remains isolated.
pre=next((w for w in words if w.get('word')=='prenotazione'),None)
if not pre or owners.get(int(pre['id']))!=['A1-U19']:
    err('prenotazione must be owned only by A1-U19')

for token in ['CourseDistributionPlanner.focusIds','CourseDistributionPlanner.reinforcementIds','CourseDistributionPlanner.challengeIds','meaningConflict(target,d)','Distractors also remain inside the unit']:
    if token not in engine: err('CourseLessonEngine missing v5.2 token: '+token)
for token in ['reinforcementBudget','first<=lesson-2','scheduledExposureCount','under-exposed words']:
    if token not in planner: err('CourseDistributionPlanner missing v5.2 token: '+token)
for token in ['byWordId','ownerOfWord','Duplicate fixed-course word owner']:
    if token not in repo: err('CourseCurriculumRepository missing runtime ownership guard: '+token)
if 'repo.all()' in '\n'.join(line for line in engine.splitlines() if 'Options' in line or 'distractor' in line.lower()):
    err('course option generation appears to use global repo.all() fallback')

counts=collections.Counter(exposure.values())
report_lines=[
    '终学意语 v5.4.0 全课程词汇分布审计报告',
    '========================================',
    f'固定单元：{len(units)}',
    f'课程词：{len(words)}（静态归属 {assignments}，唯一归属 {len(owners)}）',
    f'单次完整课程目标出现次数：最少 {min(exposure.values()) if exposure else 0}，最多 {max(exposure.values()) if exposure else 0}',
    '出现次数分布：'+ ' · '.join(f'{k}次={counts[k]}词' for k in sorted(counts)),
    f'正常课当前焦点占比下限：{min((f/(f+r) for _,_,f,r in lesson_stats if f+r),default=1):.1%}',
    f'同单元强化上限：每词 1 次（挑战前）',
    '跨单元课程目标：0',
    f'中文/英文释义歧义导致不足3个干扰项：{len(ambiguous)}',
    'prenotazione 固定归属：A1-U19 披萨店',
    '',
    '规则：',
    '1. 每个词在挑战前恰好作为当前焦点出现 1 次。',
    '2. 同单元强化至少间隔 1 个完整小课，且挑战前最多强化 1 次。',
    '3. 普通小课有强化时，当前焦点内容占比不得低于 75%。',
    '4. 单元挑战优先只抽取此前仅出现 1 次的低暴露词，当前课程一遍最多让任何词作为目标出现 2 次。',
    '5. 固定课程的题目目标与选择项都只来自当前单元；全局 FSRS 复习留在智能复习入口。',
    '6. 中英两种辅助语言下，释义选择题均要求至少 3 个不与正确答案共享核心释义的同单元干扰项。',
]

parser=argparse.ArgumentParser(add_help=False);parser.add_argument('--write-report',action='store_true');args,_=parser.parse_known_args()
if args.write_report:
    (ROOT/'全课程词汇分布审计报告_v5.4.0.txt').write_text('\n'.join(report_lines)+'\n',encoding='utf-8')

if errors:
    for x in errors: print('ERROR:',x)
    print('COURSE DISTRIBUTION CHECK FAILED:',len(errors),'error(s)')
    sys.exit(1)
print('\n'.join(report_lines[:9]))
print('Course distribution OK: 98 units / 2774 unique owners / balanced spaced reinforcement / under-exposed challenge / same-unit unambiguous choices.')
