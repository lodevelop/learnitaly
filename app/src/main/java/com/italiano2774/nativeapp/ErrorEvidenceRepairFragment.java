package com.italiano2774.nativeapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

/** v4.5.0 retained error-evidence repair queue used by daily practice and weekly diagnosis. */
public class ErrorEvidenceRepairFragment extends Fragment {
    private final List<ErrorRecordEntity> items=new ArrayList<>();
    private final ErrorRepairSession session=new ErrorRepairSession();
    private ProgressStore progress;private WordRepository words;private int index=0,totalPending=0;private long startedAt=0L;
    private TextView stats,source,promptLabel,promptText,actual,detail,feedback,expected;private EditText answer;private MaterialButton check,reveal,skip,next;

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle state){
        View v=inflater.inflate(R.layout.fragment_error_evidence_repair,container,false);progress=new ProgressStore(requireContext());words=WordRepository.get(requireContext());
        stats=v.findViewById(R.id.text_error_evidence_stats);source=v.findViewById(R.id.text_error_evidence_source);promptLabel=v.findViewById(R.id.text_error_evidence_prompt_label);promptText=v.findViewById(R.id.text_error_evidence_prompt);actual=v.findViewById(R.id.text_error_evidence_actual);detail=v.findViewById(R.id.text_error_evidence_detail);feedback=v.findViewById(R.id.text_error_evidence_feedback);expected=v.findViewById(R.id.text_error_evidence_expected);answer=v.findViewById(R.id.edit_error_evidence_answer);check=v.findViewById(R.id.button_error_evidence_check);reveal=v.findViewById(R.id.button_error_evidence_reveal);skip=v.findViewById(R.id.button_error_evidence_skip);next=v.findViewById(R.id.button_error_evidence_next);
        v.findViewById(R.id.button_error_evidence_back).setOnClickListener(x->((MainActivity)requireActivity()).openPractice());check.setOnClickListener(x->checkRepair());reveal.setOnClickListener(x->revealAnswer());skip.setOnClickListener(x->advance(false));next.setOnClickListener(x->advance(false));load();return v;
    }

    private ErrorRecordEntity current(){return items.isEmpty()||index<0||index>=items.size()?null:items.get(index);}
    private void load(){
        stats.setText("正在整理最近的真实错误…");final android.content.Context app=requireContext().getApplicationContext();
        new Thread(()->{LearningStateDao dao=LearningDatabase.get(app).learningStateDao();List<ErrorRecordEntity> scan=dao.unresolvedPracticeErrors(120);long now=System.currentTimeMillis();for(ErrorRecordEntity e:scan)if(!ErrorEvidencePromptResolver.hasUsablePrompt(app,progress,e))dao.archiveUnrepairableError(e.id,now);List<ErrorRecordEntity> x=dao.unresolvedPracticeErrors(40);int count=dao.unresolvedPracticeErrorCount();if(!isAdded())return;requireActivity().runOnUiThread(()->{items.clear();items.addAll(x);totalPending=count;progress.setPendingErrorRepairs(count);index=0;render();});},"error-evidence-load").start();
    }

    private void render(){
        ErrorRecordEntity e=current();boolean empty=e==null;session.reset();answer.setError(null);skip.setVisibility(View.VISIBLE);reveal.setText("不会 · 显示正确表达");answer.setText("");answer.setEnabled(!empty);check.setEnabled(!empty);reveal.setEnabled(!empty);skip.setEnabled(!empty);next.setVisibility(View.GONE);expected.setVisibility(View.GONE);feedback.setText("");
        if(empty){stats.setText("待修复 0 条 · 最近记录都已经重新做对");source.setText("🎉 个人错句本已清空");promptLabel.setText("");promptText.setText("");actual.setText("继续做听力、写作、每日5句或自由会话；新的真实错误会自动进入这里。");detail.setText("显示答案不会被算作修复。以后出现错误时，重新写对一次才会从队列消失，并进入句子间隔复习。");startedAt=System.currentTimeMillis();return;}
        stats.setText("待修复 "+totalPending+" 条 · 当前 "+(index+1)+" / "+items.size()+" · 正确重写后才移出队列");source.setText(ErrorCause.label(e.cause)+" · "+ErrorEvidencePolicy.modeLabel(e.mode)+" · 已回炉 "+e.repairAttempts+" 次");
        String originalPrompt=ErrorEvidencePromptResolver.resolve(requireContext(),progress,e);boolean hasPrompt=!originalPrompt.trim().isEmpty();promptLabel.setText(hasPrompt?"原题 / 提示":"旧记录缺少原题");promptText.setText(hasPrompt?originalPrompt:"这是一条升级前保存的旧记录，原题无法可靠恢复。系统不会要求你盲猜；可以先跳过，之后仍会通过句子间隔复习重新出现。");promptText.setTextColor(ContextCompat.getColor(requireContext(),hasPrompt?R.color.text_primary:R.color.text_secondary));
        actual.setText(ErrorEvidencePolicy.attemptText(e));String cleanDetail=ErrorEvidencePolicy.cleanDetail(e.detail);detail.setText((cleanDetail.isEmpty()?(hasPrompt?"根据上面的原题提示，把这句话重新写正确。":"旧记录信息不完整，不建议盲写。"):cleanDetail)+"\n"+(hasPrompt?"提示：先自己重新组织意大利语，不要直接照抄答案。":"提示：可直接先跳过这条旧记录。"));answer.setEnabled(hasPrompt);check.setEnabled(hasPrompt);startedAt=System.currentTimeMillis();
    }

    private void checkRepair(){
        ErrorRecordEntity e=current();if(e==null||!session.canCheck())return;String typed=answer.getText()==null?"":answer.getText().toString().trim();if(typed.isEmpty()){answer.setError("先自己重写一次");return;}
        ErrorCauseAnalyzer.SentenceAnalysis a=ErrorCauseAnalyzer.analyzeSentence(e.expected,typed,words);int threshold="writing".equals(e.mode)?70:(wordCount(e.expected)<=2?96:86);boolean ok=a.score>=threshold;long ms=Math.max(1,System.currentTimeMillis()-startedAt);progress.recordAuxiliaryResult("error_evidence_repair",ok,ms);
        SentenceFsrsRepository.recordDimension(requireContext(),"个人错句本 · "+ErrorEvidencePolicy.modeLabel(e.mode),e.expected,repairHint(e),SentenceFsrsRepository.DIM_RECALL,ok,a.score,null);
        final android.content.Context app=requireContext().getApplicationContext();new Thread(()->{LearningStateDao dao=LearningDatabase.get(app).learningStateDao();if(ok)dao.markMatchingErrorRepaired(e.mode,e.expected,e.actual,System.currentTimeMillis());else dao.incrementMatchingErrorRepairAttempt(e.mode,e.expected,e.actual);},"error-evidence-grade").start();
        if(ok){session.complete();answer.setEnabled(false);progress.markEvidenceRepairComplete();totalPending=Math.max(0,totalPending-1);feedback.setText("✓ 修复成功 · 匹配度 "+a.score+"%\n这条正确表达已经进入句子间隔复习，之后还会再次出现。");feedback.setTextColor(ContextCompat.getColor(requireContext(),R.color.success));expected.setText("正确表达\n"+e.expected);expected.setVisibility(View.VISIBLE);check.setEnabled(false);reveal.setEnabled(false);skip.setVisibility(View.GONE);next.setVisibility(View.VISIBLE);}else{e.repairAttempts++;feedback.setText("△ 还没有修复成功 · 匹配度 "+a.score+"%\n"+a.summary()+"\n"+a.diff+"\n先根据提示再改一次；这条仍会留在待修复队列里。");feedback.setTextColor(ContextCompat.getColor(requireContext(),R.color.error));startedAt=System.currentTimeMillis();}
    }

    private void revealAnswer(){
        ErrorRecordEntity e=current();if(e==null||session.isComplete())return;
        if(session.isAnswerVisible()){
            session.hideAnswer();expected.setText("");expected.setVisibility(View.GONE);
            answer.setText("");answer.setError(null);answer.setEnabled(true);check.setEnabled(true);
            reveal.setText("再看一次正确表达");feedback.setText("答案已遮住。请凭记忆重新写一遍，再检查。");
            answer.requestFocus();startedAt=System.currentTimeMillis();return;
        }
        boolean firstReveal=session.showAnswer();answer.setText("");answer.setError(null);
        answer.setEnabled(false);check.setEnabled(false);reveal.setText("记住了 · 遮住答案重写");
        expected.setText("正确表达\n"+e.expected);expected.setVisibility(View.VISIBLE);feedback.setText("这次只算查看答案，不算修复。看完后点击“记住了 · 遮住答案重写”，再凭记忆输入。");feedback.setTextColor(ContextCompat.getColor(requireContext(),R.color.text_secondary));if(!firstReveal)return;progress.recordAuxiliaryResult("error_evidence_repair",false,Math.max(1,System.currentTimeMillis()-startedAt));SentenceFsrsRepository.recordDimension(requireContext(),"个人错句本 · "+ErrorEvidencePolicy.modeLabel(e.mode),e.expected,repairHint(e),SentenceFsrsRepository.DIM_RECALL,false,0,null);e.repairAttempts++;final String mode=e.mode,target=e.expected,old=e.actual;final android.content.Context app=requireContext().getApplicationContext();new Thread(()->LearningDatabase.get(app).learningStateDao().incrementMatchingErrorRepairAttempt(mode,target,old),"error-evidence-reveal").start();startedAt=System.currentTimeMillis();
    }

    private void advance(boolean reload){
        if(reload){load();return;}ErrorRecordEntity e=current();if(e!=null&&e.repairedAt==0&&next.getVisibility()==View.VISIBLE){e.repairedAt=System.currentTimeMillis();items.remove(index);if(index>=items.size())index=0;}else if(!items.isEmpty())index=(index+1)%items.size();render();
    }

    private int wordCount(String s){String b=ErrorCauseAnalyzer.basic(s);return b.isEmpty()?0:b.split("\\s+").length;}
    private String repairHint(ErrorRecordEntity e){String d=ErrorEvidencePolicy.cleanDetail(e.detail);String p=ErrorEvidencePromptResolver.resolve(requireContext(),progress,e);String base=d.isEmpty()?ErrorCause.label(e.cause):ErrorCause.label(e.cause)+" · "+d;return p.isEmpty()?base:base+" · 原题："+p;}
    }
