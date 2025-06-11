package com.example.campus.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.campus.MainActivity;
import com.example.campus.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    // UI 요소들 직접 선언
    private Toolbar toolbar;
    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnSignUp;
    private TextView tvLogin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI 요소 초기화
        initializeViews();

        // 툴바 설정
        setupToolbar();

        // 클릭 리스너 설정
        setupClickListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);

        // Null 체크
        if (etName == null || etEmail == null || etPassword == null ||
                etConfirmPassword == null || btnSignUp == null || tvLogin == null || progressBar == null) {
            Toast.makeText(this, "UI 초기화 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        try {
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
                    getSupportActionBar().setTitle("회원가입");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            setTitle("회원가입");
        }
    }

    private void setupClickListeners() {
        // 회원가입 버튼 클릭 리스너
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText() != null ? etName.getText().toString().trim() : "";
                String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
                String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
                String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

                if (validateInputs(name, email, password, confirmPassword)) {
                    registerUser(name, email, password);
                }
            }
        });

        // 로그인 텍스트 클릭 리스너
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 화면 종료 (로그인 화면으로 돌아감)
            }
        });
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            etName.setError("이름을 입력해주세요");
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("이메일을 입력해주세요");
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("비밀번호를 입력해주세요");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("비밀번호 확인을 입력해주세요");
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("비밀번호는 최소 6자 이상이어야 합니다");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("비밀번호가 일치하지 않습니다");
            return false;
        }

        return true;
    }

    private void registerUser(final String name, final String email, String password) {
        // 로딩 표시
        progressBar.setVisibility(View.VISIBLE);

        // reCAPTCHA 관련 코드 주석 처리하고 바로 회원가입 진행
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // 회원가입 성공
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Firestore에 사용자 정보 저장
                            saveUserToFirestore(user.getUid(), name, email);
                        } else {
                            // 로딩 숨김
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SignUpActivity.this, "회원가입 실패: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String userId, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("created_at", System.currentTimeMillis());

        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // 로딩 숨김
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();

                            // 메인 화면으로 이동
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "사용자 정보 저장 실패: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}