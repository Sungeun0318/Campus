package com.example.campus.study;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.campus.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StudyPlanAddActivity extends AppCompatActivity {

    // UI 요소들 직접 선언
    private Toolbar toolbar;
    private TextView tvDate;
    private TextView tvTime;
    private Button btnSelectTime;
    private TextInputEditText etSubject;
    private TextInputEditText etDescription;
    private TextView tvDuration;
    private SeekBar seekBarDuration;
    private Button btnSave;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Calendar selectedDateTime;
    private int durationMinutes = 60; // 기본값 1시간

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_plan_add);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // UI 요소 초기화
        initializeViews();

        // 툴바 설정
        setupToolbar();

        // 선택된 날짜 가져오기
        long selectedDateMillis = getIntent().getLongExtra("selectedDate", System.currentTimeMillis());
        selectedDateTime = Calendar.getInstance();
        selectedDateTime.setTimeInMillis(selectedDateMillis);

        // 현재 시간으로 설정
        Calendar now = Calendar.getInstance();
        selectedDateTime.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
        selectedDateTime.set(Calendar.MINUTE, now.get(Calendar.MINUTE));

        // 날짜 표시
        updateDateTimeText();

        // 시간 선택 버튼 리스너
        btnSelectTime.setOnClickListener(v -> showTimePicker());

        // 시간 슬라이더 설정
        setupDurationSeekBar();

        // 저장 버튼 리스너
        btnSave.setOnClickListener(v -> savePlan());
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        etSubject = findViewById(R.id.etSubject);
        etDescription = findViewById(R.id.etDescription);
        tvDuration = findViewById(R.id.tvDuration);
        seekBarDuration = findViewById(R.id.seekBarDuration);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        // Null 체크
        if (toolbar == null || tvDate == null || tvTime == null || btnSelectTime == null ||
                etSubject == null || etDescription == null || tvDuration == null ||
                seekBarDuration == null || btnSave == null || progressBar == null) {
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
                    getSupportActionBar().setTitle("학습 계획 추가");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            setTitle("학습 계획 추가");
        }
    }

    private void updateDateTimeText() {
        if (tvDate == null || tvTime == null || selectedDateTime == null) return;

        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.getDefault());
        SimpleDateFormat timeSdf = new SimpleDateFormat("a h:mm", Locale.getDefault());

        tvDate.setText(dateSdf.format(selectedDateTime.getTime()));
        tvTime.setText(timeSdf.format(selectedDateTime.getTime()));
    }

    private void showTimePicker() {
        if (selectedDateTime == null) return;

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeText();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void setupDurationSeekBar() {
        if (seekBarDuration == null) return;

        // 슬라이더 초기값 설정 (1시간)
        seekBarDuration.setProgress(60);
        updateDurationText(60);

        seekBarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 최소값 15분으로 설정
                int duration = Math.max(15, progress);
                durationMinutes = duration;
                updateDurationText(duration);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 필요 없음
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 필요 없음
            }
        });
    }

    private void updateDurationText(int minutes) {
        if (tvDuration == null) return;

        int hours = minutes / 60;
        int mins = minutes % 60;

        String durationText;
        if (hours > 0) {
            durationText = hours + "시간 " + (mins > 0 ? mins + "분" : "");
        } else {
            durationText = mins + "분";
        }

        tvDuration.setText(durationText);
    }

    private void savePlan() {
        // 현재 로그인된 사용자가 없으면 중단
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String subject = etSubject.getText() != null ? etSubject.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        if (subject.isEmpty()) {
            etSubject.setError("과목명을 입력해주세요");
            return;
        }

        // 로딩 표시
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        String userId = auth.getCurrentUser().getUid();

        // 학습 계획 객체 생성
        StudyPlan plan = new StudyPlan(
                selectedDateTime.getTimeInMillis(),
                subject,
                description,
                durationMinutes,
                userId
        );

        // Firestore에 저장
        db.collection("studyPlans")
                .add(plan)
                .addOnSuccessListener(documentReference -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    // 알림 설정
                    plan.setId(documentReference.getId());
                    scheduleReminder(plan);

                    Toast.makeText(StudyPlanAddActivity.this, "학습 계획이 저장되었습니다", Toast.LENGTH_SHORT).show();
                    finish(); // 액티비티 종료
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Toast.makeText(StudyPlanAddActivity.this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void scheduleReminder(StudyPlan plan) {
        // 현재 시간과 계획 시간 비교
        long now = System.currentTimeMillis();
        long planTime = plan.getTimestamp();

        // 미래의 일정인 경우에만 알림 설정
        if (planTime > now) {
            String title = plan.getSubject();
            String message = "학습 계획: " + plan.getDescription();
            int notificationId = plan.getId().hashCode(); // 문서 ID로 고유한 알림 ID 생성

            // 알림 예약
            StudyReminderManager.scheduleReminder(
                    this,
                    notificationId,
                    title,
                    message,
                    planTime
            );
        }
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