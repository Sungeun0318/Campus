package com.example.campus.emotions;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.campus.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EmotionRecordActivity extends AppCompatActivity {

    // UI 요소들 직접 선언
    private Toolbar toolbar;
    private TextView tvCurrentDate;
    private TextView tvEmotionDescription;
    private ImageView level1, level2, level3, level4, level5;
    private ChipGroup emotionTypeChipGroup;
    private Chip chipHappy, chipSad, chipAngry, chipAnxious, chipNeutral;
    private TextInputEditText etNote;
    private Button btnSave;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private int selectedEmotionLevel = 3; // 기본값: 중간
    private String selectedEmotionType = "중립"; // 기본값: 중립

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion_record);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // UI 요소 초기화
        initializeViews();

        // 툴바 설정
        setupToolbar();

        // 현재 날짜 표시
        updateCurrentDate();

        // 감정 수준 선택 리스너 설정
        setupEmotionLevelListeners();

        // 감정 유형 선택 리스너 설정
        setupEmotionTypeListeners();

        // 저장 버튼 클릭 리스너
        btnSave.setOnClickListener(v -> saveEmotionRecord());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvEmotionDescription = findViewById(R.id.tvEmotionDescription);
        level1 = findViewById(R.id.level1);
        level2 = findViewById(R.id.level2);
        level3 = findViewById(R.id.level3);
        level4 = findViewById(R.id.level4);
        level5 = findViewById(R.id.level5);
        emotionTypeChipGroup = findViewById(R.id.emotionTypeChipGroup);
        chipHappy = findViewById(R.id.chipHappy);
        chipSad = findViewById(R.id.chipSad);
        chipAngry = findViewById(R.id.chipAngry);
        chipAnxious = findViewById(R.id.chipAnxious);
        chipNeutral = findViewById(R.id.chipNeutral);
        etNote = findViewById(R.id.etNote);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        // Null 체크
        if (toolbar == null || tvCurrentDate == null || tvEmotionDescription == null ||
                level1 == null || level2 == null || level3 == null || level4 == null || level5 == null ||
                emotionTypeChipGroup == null || chipHappy == null || chipSad == null ||
                chipAngry == null || chipAnxious == null || chipNeutral == null ||
                etNote == null || btnSave == null || progressBar == null) {
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
                    getSupportActionBar().setTitle("감정 기록하기");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            setTitle("감정 기록하기");
        }
    }

    private void updateCurrentDate() {
        if (tvCurrentDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());
            tvCurrentDate.setText(sdf.format(new Date()));
        }
    }

    private void setupEmotionLevelListeners() {
        // 기본 선택 설정
        updateEmotionLevelUI(3);

        if (level1 != null) level1.setOnClickListener(v -> updateEmotionLevelUI(1));
        if (level2 != null) level2.setOnClickListener(v -> updateEmotionLevelUI(2));
        if (level3 != null) level3.setOnClickListener(v -> updateEmotionLevelUI(3));
        if (level4 != null) level4.setOnClickListener(v -> updateEmotionLevelUI(4));
        if (level5 != null) level5.setOnClickListener(v -> updateEmotionLevelUI(5));
    }

    private void setupEmotionTypeListeners() {
        // 기본 선택 설정
        updateEmotionTypeUI("중립");

        if (chipHappy != null) chipHappy.setOnClickListener(v -> updateEmotionTypeUI("행복"));
        if (chipSad != null) chipSad.setOnClickListener(v -> updateEmotionTypeUI("슬픔"));
        if (chipAngry != null) chipAngry.setOnClickListener(v -> updateEmotionTypeUI("분노"));
        if (chipAnxious != null) chipAnxious.setOnClickListener(v -> updateEmotionTypeUI("불안"));
        if (chipNeutral != null) chipNeutral.setOnClickListener(v -> updateEmotionTypeUI("중립"));
    }

    private void updateEmotionLevelUI(int level) {
        // 모든 레벨 버튼 초기화
        if (level1 != null) level1.setAlpha(0.5f);
        if (level2 != null) level2.setAlpha(0.5f);
        if (level3 != null) level3.setAlpha(0.5f);
        if (level4 != null) level4.setAlpha(0.5f);
        if (level5 != null) level5.setAlpha(0.5f);

        // 선택된 레벨 강조
        switch (level) {
            case 1:
                if (level1 != null) level1.setAlpha(1.0f);
                if (tvEmotionDescription != null) tvEmotionDescription.setText("매우 나쁨");
                break;
            case 2:
                if (level2 != null) level2.setAlpha(1.0f);
                if (tvEmotionDescription != null) tvEmotionDescription.setText("나쁨");
                break;
            case 3:
                if (level3 != null) level3.setAlpha(1.0f);
                if (tvEmotionDescription != null) tvEmotionDescription.setText("보통");
                break;
            case 4:
                if (level4 != null) level4.setAlpha(1.0f);
                if (tvEmotionDescription != null) tvEmotionDescription.setText("좋음");
                break;
            case 5:
                if (level5 != null) level5.setAlpha(1.0f);
                if (tvEmotionDescription != null) tvEmotionDescription.setText("매우 좋음");
                break;
        }

        selectedEmotionLevel = level;
    }

    private void updateEmotionTypeUI(String type) {
        // 모든 칩 선택 해제
        if (chipHappy != null) chipHappy.setChecked(false);
        if (chipSad != null) chipSad.setChecked(false);
        if (chipAngry != null) chipAngry.setChecked(false);
        if (chipAnxious != null) chipAnxious.setChecked(false);
        if (chipNeutral != null) chipNeutral.setChecked(false);

        // 선택된 감정 유형 체크
        switch (type) {
            case "행복":
                if (chipHappy != null) chipHappy.setChecked(true);
                break;
            case "슬픔":
                if (chipSad != null) chipSad.setChecked(true);
                break;
            case "분노":
                if (chipAngry != null) chipAngry.setChecked(true);
                break;
            case "불안":
                if (chipAnxious != null) chipAnxious.setChecked(true);
                break;
            case "중립":
                if (chipNeutral != null) chipNeutral.setChecked(true);
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
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        String userId = auth.getCurrentUser().getUid();
        String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";

        // 감정 기록 객체 생성
        EmotionRecord record = new EmotionRecord(selectedEmotionLevel, selectedEmotionType, note, userId);

        // Firestore에 저장
        db.collection("emotions")
                .add(record)
                .addOnSuccessListener(documentReference -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Toast.makeText(EmotionRecordActivity.this, "감정이 기록되었습니다", Toast.LENGTH_SHORT).show();
                    finish(); // 액티비티 종료
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
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