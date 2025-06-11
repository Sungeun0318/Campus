package com.example.campus;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.campus.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class IntroActivity extends AppCompatActivity {

    private Button btnTour, btnAiCare, btnMarket;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

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

        // 버튼 초기화
        initializeButtons();

        // 버튼 클릭 이벤트 설정
        setupButtonClickListeners();
    }

    private void setupToolbar() {
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_logout);
                    getSupportActionBar().setTitle("캠퍼스 서비스 선택");
                }
            }
        } catch (Exception e) {
            // 툴바 설정 실패 시 로그 출력하고 계속 진행
            e.printStackTrace();
            // 기본 타이틀 설정
            setTitle("캠퍼스 서비스 선택");
        }
    }

    private void initializeButtons() {
        btnTour = findViewById(R.id.btnTour);
        btnAiCare = findViewById(R.id.btnAiCare);
        btnMarket = findViewById(R.id.btnMarket);

        // Null 체크
        if (btnTour == null || btnAiCare == null || btnMarket == null) {
            Toast.makeText(this, "UI 초기화 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void setupButtonClickListeners() {
        // 캠퍼스 투어 버튼
        btnTour.setOnClickListener(v -> {
            Toast.makeText(this, "캠퍼스 투어 기능은 준비 중입니다", Toast.LENGTH_SHORT).show();
            // TODO: 캠퍼스 투어 액티비티로 이동
        });

        // AI & 힐링 투어 버튼 (메인 기능)
        btnAiCare.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(intent);
                // finish()를 호출하지 않아서 뒤로가기로 돌아올 수 있음
            } catch (Exception e) {
                Toast.makeText(this, "화면 전환 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        // 교재 장터 버튼
        btnMarket.setOnClickListener(v -> {
            Toast.makeText(this, "교재 장터 기능은 준비 중입니다", Toast.LENGTH_SHORT).show();
            // TODO: 교재 장터 액티비티로 이동
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 로그아웃 후 로그인 화면으로 이동
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // 뒤로가기 버튼으로 로그아웃 확인 다이얼로그 표시
        showLogoutConfirmDialog();
    }

    private void showLogoutConfirmDialog() {
        try {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("로그아웃")
                    .setMessage("정말 로그아웃 하시겠습니까?")
                    .setPositiveButton("로그아웃", (dialog, which) -> performLogout())
                    .setNegativeButton("취소", null)
                    .show();
        } catch (Exception e) {
            // 다이얼로그 생성 실패 시 바로 로그아웃
            performLogout();
        }
    }

    private void performLogout() {
        try {
            if (auth != null) {
                auth.signOut();
            }
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            finish(); // 오류 발생 시 최소한 액티비티는 종료
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 로그인 상태 재확인
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
}