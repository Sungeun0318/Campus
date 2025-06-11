package com.example.campus;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

        // 툴바 설정 (안전하게)
        setupToolbar();

        // 기본 프래그먼트 설정 (홈)
        if (savedInstanceState == null) {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 하단 네비게이션 이벤트 설정
        setupBottomNavigation();
    }

    private void setupToolbar() {
        try {
            if (binding != null && binding.toolbar != null) {
                setSupportActionBar(binding.toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
                    getSupportActionBar().setTitle("AI & 힐링 투어");
                }
            }
        } catch (Exception e) {
            // 툴바 설정 실패 시 로그 출력하고 계속 진행
            e.printStackTrace();
            // 기본 타이틀 설정
            setTitle("AI & 힐링 투어");
        }
    }

    private void setupBottomNavigation() {
        if (binding == null || binding.bottomNav == null) {
            return;
        }

        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            try {
                if (itemId == R.id.navigation_timer) {
                    selectedFragment = new TimerFragment();
                } else if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.navigation_more) {
                    selectedFragment = new MoreFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 뒤로가기 버튼 클릭 시 IntroActivity로 이동
            try {
                Intent intent = new Intent(this, IntroActivity.class);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                finish(); // 오류 발생 시 최소한 액티비티는 종료
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // 시스템 뒤로가기 버튼도 IntroActivity로 이동
        try {
            Intent intent = new Intent(this, IntroActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            super.onBackPressed(); // 기본 뒤로가기 동작
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binding != null) {
            binding = null;
        }
    }
}