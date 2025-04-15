package com.example.campus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.campus.ai.AiChatActivity;
import com.example.campus.auth.LoginActivity;
import com.example.campus.databinding.ActivityMainBinding;
import com.example.campus.emotions.EmotionRecordActivity;
import com.example.campus.emotions.EmotionStatsActivity;
import com.example.campus.study.StudyPlannerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firebase Auth 초기화
        auth = FirebaseAuth.getInstance();

        // 로그인 상태 확인
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // 로그인되지 않은 경우, 로그인 화면으로 이동
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 버튼 리스너 설정
        setupButtonListeners();

        // 사용자 정보 표시
        updateUserInfo(currentUser);
    }

    private void setupButtonListeners() {
        // AI 학습 도우미 버튼
        binding.cardStudyAi.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AiChatActivity.class);
            intent.putExtra("chat_mode", "study");
            startActivity(intent);
        });

        // AI 멘탈 케어 버튼
        binding.cardMentalAi.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AiChatActivity.class);
            intent.putExtra("chat_mode", "mental");
            startActivity(intent);
        });

        // 감정 기록 버튼
        binding.cardEmotionRecord.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EmotionRecordActivity.class));
        });

        // 감정 통계 버튼 (추가)
        binding.cardEmotionStats.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EmotionStatsActivity.class));
        });

        // 학습 계획 버튼
        binding.cardStudyPlan.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, StudyPlannerActivity.class));
        });

        // 로그아웃 버튼
        binding.btnLogout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void updateUserInfo(FirebaseUser user) {
        if (user != null) {
            // 사용자 이메일 표시
            binding.tvUserEmail.setText(user.getEmail());

            // 사용자 이름 표시 (있는 경우)
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                binding.tvUserName.setText(displayName);
            } else {
                binding.tvUserName.setText("캠퍼스 사용자");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 로그인 상태 다시 확인
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // 로그인되지 않은 경우, 로그인 화면으로 이동
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}