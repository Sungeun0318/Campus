package com.example.campus.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campus.R;
import com.example.campus.IntroActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    // UI 요소들 직접 선언
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvSignUp;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase Auth 초기화
        mAuth = FirebaseAuth.getInstance();

        // UI 요소 초기화
        initializeViews();

        // 클릭 리스너 설정
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        progressBar = findViewById(R.id.progressBar);

        // Null 체크
        if (etEmail == null || etPassword == null || btnLogin == null ||
                tvSignUp == null || progressBar == null) {
            Toast.makeText(this, "UI 초기화 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupClickListeners() {
        // 로그인 버튼 클릭 리스너
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

                if (!email.isEmpty() && !password.isEmpty()) {
                    signIn(email, password);
                } else {
                    Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 회원가입 텍스트 클릭 리스너
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });
    }

    private void signIn(String email, String password) {
        // 로딩 표시
        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // 로딩 숨김
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // 로그인 성공
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "로그인 성공: " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();

                            // ✅ IntroActivity로 이동
                            startActivity(new Intent(LoginActivity.this, IntroActivity.class));
                            finish();
                        } else {
                            // 로그인 실패
                            Toast.makeText(LoginActivity.this, "로그인 실패: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 🔽 자동 로그인 방지를 위해 주석 처리
        /*
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, IntroActivity.class));
            finish();
        }
        */
    }
}