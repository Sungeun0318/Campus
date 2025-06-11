package com.example.campus.emotions;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campus.databinding.ActivityEmotionRecordBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class EmotionRecordActivity extends AppCompatActivity {

    private ActivityEmotionRecordBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private int selectedEmotionLevel = 3; // 기본값: 중간
    private String selectedEmotionType = "중립"; // 기본값: 중립

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmotionRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 툴바 설정
        // setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("감정 기록하기");
        }

        // 감정 수준 선택 리스너 설정
        setupEmotionLevelListeners();

        // 감정 유형 선택 리스너 설정
        setupEmotionTypeListeners();

        // 저장 버튼 클릭 리스너
        binding.btnSave.setOnClickListener(v -> saveEmotionRecord());
    }

    private void setupEmotionLevelListeners() {
        // 기본 선택 설정
        updateEmotionLevelUI(3);

        binding.level1.setOnClickListener(v -> updateEmotionLevelUI(1));
        binding.level2.setOnClickListener(v -> updateEmotionLevelUI(2));
        binding.level3.setOnClickListener(v -> updateEmotionLevelUI(3));
        binding.level4.setOnClickListener(v -> updateEmotionLevelUI(4));
        binding.level5.setOnClickListener(v -> updateEmotionLevelUI(5));
    }

    private void setupEmotionTypeListeners() {
        // 기본 선택 설정
        updateEmotionTypeUI("중립");

        binding.chipHappy.setOnClickListener(v -> updateEmotionTypeUI("행복"));
        binding.chipSad.setOnClickListener(v -> updateEmotionTypeUI("슬픔"));
        binding.chipAngry.setOnClickListener(v -> updateEmotionTypeUI("분노"));
        binding.chipAnxious.setOnClickListener(v -> updateEmotionTypeUI("불안"));
        binding.chipNeutral.setOnClickListener(v -> updateEmotionTypeUI("중립"));
    }

    private void updateEmotionLevelUI(int level) {
        // 모든 레벨 버튼 초기화
        binding.level1.setAlpha(0.5f);
        binding.level2.setAlpha(0.5f);
        binding.level3.setAlpha(0.5f);
        binding.level4.setAlpha(0.5f);
        binding.level5.setAlpha(0.5f);

        // 선택된 레벨 강조
        switch (level) {
            case 1:
                binding.level1.setAlpha(1.0f);
                binding.tvEmotionDescription.setText("매우 나쁨");
                break;
            case 2:
                binding.level2.setAlpha(1.0f);
                binding.tvEmotionDescription.setText("나쁨");
                break;
            case 3:
                binding.level3.setAlpha(1.0f);
                binding.tvEmotionDescription.setText("보통");
                break;
            case 4:
                binding.level4.setAlpha(1.0f);
                binding.tvEmotionDescription.setText("좋음");
                break;
            case 5:
                binding.level5.setAlpha(1.0f);
                binding.tvEmotionDescription.setText("매우 좋음");
                break;
        }

        selectedEmotionLevel = level;
    }

    private void updateEmotionTypeUI(String type) {
        // 모든 칩 선택 해제
        binding.chipHappy.setChecked(false);
        binding.chipSad.setChecked(false);
        binding.chipAngry.setChecked(false);
        binding.chipAnxious.setChecked(false);
        binding.chipNeutral.setChecked(false);

        // 선택된 감정 유형 체크
        switch (type) {
            case "행복":
                binding.chipHappy.setChecked(true);
                break;
            case "슬픔":
                binding.chipSad.setChecked(true);
                break;
            case "분노":
                binding.chipAngry.setChecked(true);
                break;
            case "불안":
                binding.chipAnxious.setChecked(true);
                break;
            case "중립":
                binding.chipNeutral.setChecked(true);
                break;
        }

        selectedEmotionType = type;
    }

    private void saveEmotionRecord() {
        // 현재 로그인된 사용자가 없으면 중단
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // 로딩 표시
        binding.progressBar.setVisibility(View.VISIBLE);

        String userId = auth.getCurrentUser().getUid();
        String note = Objects.requireNonNull(binding.etNote.getText()).toString().trim();

        // 감정 기록 객체 생성
        EmotionRecord record = new EmotionRecord(selectedEmotionLevel, selectedEmotionType, note, userId);

        // Firestore에 저장
        db.collection("emotions")
                .add(record)
                .addOnSuccessListener(documentReference -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(EmotionRecordActivity.this, "감정이 기록되었습니다", Toast.LENGTH_SHORT).show();
                    finish(); // 액티비티 종료
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(EmotionRecordActivity.this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 뒤로가기
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}