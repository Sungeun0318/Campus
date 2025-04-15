package com.example.campus.study;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campus.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudyPlanAdapter extends RecyclerView.Adapter<StudyPlanAdapter.StudyPlanViewHolder> {

    private final Context context;
    private List<StudyPlan> studyPlans;
    private final StudyPlanClickListener listener;

    public interface StudyPlanClickListener {
        void onPlanCheckedChanged(StudyPlan plan, boolean isChecked);
        void onPlanClicked(StudyPlan plan);
        void onPlanLongClicked(StudyPlan plan);
    }

    public StudyPlanAdapter(Context context, List<StudyPlan> studyPlans, StudyPlanClickListener listener) {
        this.context = context;
        this.studyPlans = studyPlans;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudyPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_study_plan, parent, false);
        return new StudyPlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudyPlanViewHolder holder, int position) {
        StudyPlan plan = studyPlans.get(position);
        holder.bind(plan);
    }

    @Override
    public int getItemCount() {
        return studyPlans.size();
    }

    public void updateStudyPlans(List<StudyPlan> newStudyPlans) {
        this.studyPlans = newStudyPlans;
        notifyDataSetChanged();
    }

    class StudyPlanViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkCompleted;
        private TextView tvSubject;
        private TextView tvDescription;
        private TextView tvTime;
        private TextView tvDuration;

        StudyPlanViewHolder(@NonNull View itemView) {
            super(itemView);
            checkCompleted = itemView.findViewById(R.id.checkCompleted);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDuration = itemView.findViewById(R.id.tvDuration);

            // 아이템 클릭 리스너 설정
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onPlanClicked(studyPlans.get(position));
                }
            });

            // 롱 클릭 리스너 설정
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onPlanLongClicked(studyPlans.get(position));
                    return true;
                }
                return false;
            });

            // 체크박스 변경 리스너 설정
            checkCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && buttonView.isPressed()) {
                    listener.onPlanCheckedChanged(studyPlans.get(position), isChecked);
                    updateTextStyle(isChecked);
                }
            });
        }

        void bind(StudyPlan plan) {
            checkCompleted.setChecked(plan.isCompleted());
            tvSubject.setText(plan.getSubject());
            tvDescription.setText(plan.getDescription());

            // 시간 포맷
            SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.getDefault());
            tvTime.setText(sdf.format(new Date(plan.getTimestamp())));

            // 학습 시간
            tvDuration.setText(plan.getDurationString());

            // 완료 상태에 따른 텍스트 스타일 업데이트
            updateTextStyle(plan.isCompleted());
        }

        private void updateTextStyle(boolean completed) {
            // 완료 상태이면 취소선 추가
            int flags = completed ? Paint.STRIKE_THRU_TEXT_FLAG : 0;
            tvSubject.setPaintFlags(tvSubject.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG | flags);
            tvDescription.setPaintFlags(tvDescription.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG | flags);
            tvTime.setPaintFlags(tvTime.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG | flags);
            tvDuration.setPaintFlags(tvDuration.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG | flags);
        }
    }
}