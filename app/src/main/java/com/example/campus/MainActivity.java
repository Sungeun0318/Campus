package com.example.campus;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.campus.ai.AiChatActivity;
import com.example.campus.auth.LoginActivity;
import com.example.campus.databinding.ActivityMainBinding;
import com.example.campus.emotions.EmotionRecordActivity;
import com.example.campus.emotions.EmotionStatsActivity;
import com.example.campus.study.StudyPlannerActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

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

        // 네비게이션 설정
        setupNavigation();

        // 버튼 리스너 설정
        setupButtonListeners();

        // 사용자 정보 표시
        updateUserInfo(currentUser);
    }

    private void setupNavigation() {
        // 바텀 네비게이션 설정
        BottomNavigationView navView = binding.navView;
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    // 홈 화면 (현재 화면)
                    return true;
                } else if (itemId == R.id.navigation_dashboard) {
                    // 감정 통계 화면으로 이동
                    startActivity(new Intent(MainActivity.this, EmotionStatsActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_notifications) {
                    // 학습 계획 화면으로 이동
                    startActivity(new Intent(MainActivity.this, StudyPlannerActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void setupButtonListeners() {
        // AI 학습 도우미 버튼
        binding.cardStudyAi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AiChatActivity.class);
                intent.putExtra("chat_mode", "study");
                startActivity(intent);
            }
        });

        // AI 멘탈 케어 버튼
        binding.cardMentalAi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AiChatActivity.class);
                intent.putExtra("chat_mode", "mental");
                startActivity(intent);
            }
        });

        // 감정 기록 버튼
        binding.cardEmotionRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, EmotionRecordActivity.class));
            }
        });

        // 학습 계획 버튼
        binding.cardStudyPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, StudyPlannerActivity.class));
            }
        });

        // 로그아웃 버튼
        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
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