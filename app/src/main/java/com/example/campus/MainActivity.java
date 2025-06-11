package com.example.campus;

import android.content.Intent;
import com.example.campus.R;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.campus.auth.LoginActivity;
import com.example.campus.databinding.ActivityMainBinding;
import com.example.campus.Fragment.HomeFragment;
import com.example.campus.Fragment.TimerFragment;
import com.example.campus.Fragment.MoreFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 사용자 정보 표시
        updateUserInfo(currentUser);

        // 기본 프래그먼트 설정 (홈)
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        // 하단 네비게이션 이벤트 설정
        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId(); // 이 줄 추가!

            if (itemId == R.id.navigation_timer) {
                // selectedFragment = new TimerFragment(); // 실제 Fragment로 교체
                return true;
            } else if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
                return true;
            } else if (itemId == R.id.navigation_more) {
                selectedFragment = new MoreFragment();
                return true;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

    }

    private void updateUserInfo(FirebaseUser user) {
        if (user != null) {
            binding.tvUserEmail.setText(user.getEmail());
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
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}
