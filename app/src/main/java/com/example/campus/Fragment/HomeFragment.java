// ===== HomeFragment.java 전체 수정 =====
package com.example.campus.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.campus.R;
import com.example.campus.ai.AiChatActivity;
import com.example.campus.emotions.EmotionRecordActivity;
import com.example.campus.emotions.EmotionStatsActivity;
import com.example.campus.study.StudyPlannerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private FirebaseAuth auth;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Firebase Auth 초기화
        auth = FirebaseAuth.getInstance();

        // UI 요소들 초기화
        initializeViews(view);

        return view;
    }

    private void initializeViews(View view) {
        // 사용자 정보 업데이트
        updateUserInfo(view);

        // 카드 클릭 이벤트 설정
        setupCardClickListeners(view);
    }

    private void updateUserInfo(View view) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && getContext() != null) {
            // ViewBinding 대신 findViewById 사용
            TextView tvUserEmail = view.findViewById(R.id.tvUserEmail);
            TextView tvUserName = view.findViewById(R.id.tvUserName);

            if (tvUserEmail != null) {
                tvUserEmail.setText(currentUser.getEmail());
            }

            if (tvUserName != null) {
                String displayName = currentUser.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    tvUserName.setText(displayName);
                } else {
                    tvUserName.setText("캠퍼스 사용자");
                }
            }
        }
    }

    private void setupCardClickListeners(View view) {
        // 학습 AI 카드 클릭
        View cardStudyAi = view.findViewById(R.id.cardStudyAi);
        if (cardStudyAi != null) {
            cardStudyAi.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AiChatActivity.class);
                intent.putExtra("chat_mode", "study");
                startActivity(intent);
            });
        }

        // 멘탈 케어 AI 카드 클릭
        View cardMentalAi = view.findViewById(R.id.cardMentalAi);
        if (cardMentalAi != null) {
            cardMentalAi.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AiChatActivity.class);
                intent.putExtra("chat_mode", "mental");
                startActivity(intent);
            });
        }

        // 감정 기록 카드 클릭
        View cardEmotionRecord = view.findViewById(R.id.cardEmotionRecord);
        if (cardEmotionRecord != null) {
            cardEmotionRecord.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), EmotionRecordActivity.class));
            });
        }

        // 감정 통계 카드 클릭
        View cardEmotionStats = view.findViewById(R.id.cardEmotionStats);
        if (cardEmotionStats != null) {
            cardEmotionStats.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), EmotionStatsActivity.class));
            });
        }

        // 학습 계획 카드 클릭
        View cardStudyPlan = view.findViewById(R.id.cardStudyPlan);
        if (cardStudyPlan != null) {
            cardStudyPlan.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), StudyPlannerActivity.class));
            });
        }

        // 로그아웃 버튼 클릭
        View btnLogout = view.findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                auth.signOut();
                Intent intent = new Intent(getContext(), com.example.campus.auth.LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }
    }
}