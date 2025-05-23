package com.example.campus.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campus.MainActivity;
import com.example.campus.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar 설정
        if (binding.toolbar != null) {
           // setSupportActionBar(binding.toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
            }
        }
        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 회원가입 버튼 클릭 리스너
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = binding.etName.getText().toString().trim();
                String email = binding.etEmail.getText().toString().trim();
                String password = binding.etPassword.getText().toString().trim();
                String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

                if (validateInputs(name, email, password, confirmPassword)) {
                    registerUser(name, email, password);
                }
            }
        });

        // 로그인 텍스트 클릭 리스너
        binding.tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 화면 종료 (로그인 화면으로 돌아감)
            }
        });
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            binding.etName.setError("이름을 입력해주세요");
            return false;
        }

        if (email.isEmpty()) {
            binding.etEmail.setError("이메일을 입력해주세요");
            return false;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("비밀번호를 입력해주세요");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.setError("비밀번호 확인을 입력해주세요");
            return false;
        }

        if (password.length() < 6) {
            binding.etPassword.setError("비밀번호는 최소 6자 이상이어야 합니다");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("비밀번호가 일치하지 않습니다");
            return false;
        }

        return true;
    }

    private void registerUser(final String name, final String email, String password) {
        // 로딩 표시
        binding.progressBar.setVisibility(View.VISIBLE);

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
                            binding.progressBar.setVisibility(View.GONE);
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
                        binding.progressBar.setVisibility(View.GONE);

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