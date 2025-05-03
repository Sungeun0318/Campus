package com.example.campus.study;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campus.R;
import com.example.campus.databinding.ActivityStudyPlanAddBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StudyPlanEditActivity extends AppCompatActivity {

    private ActivityStudyPlanAddBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Calendar selectedDateTime;
    private int durationMinutes = 60;
    private String planId;
    private StudyPlan currentPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudyPlanAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 툴바 설정
        // setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("학습 계획 수정");
        }

        // 계획 ID 가져오기
        planId = getIntent().getStringExtra("planId");
        if (planId == null) {
            Toast.makeText(this, "학습 계획 정보를 가져올 수 없습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 시간 선택 버튼 리스너
        binding.btnSelectTime.setOnClickListener(v -> showTimePicker());

        // 시간 슬라이더 설정
        setupDurationSeekBar();

        // 저장 버튼 리스너 - 텍스트 변경
        if (binding.btnSave != null) {
            binding.btnSave.setText("수정하기");
            binding.btnSave.setOnClickListener(v -> updatePlan());
        }

        // 계획 정보 로드
        loadPlanData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_plan, menu);
        return true;
    }

    private void loadPlanData() {
        // 로딩 표시
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        // Firestore에서 계획 정보 가져오기
        if (planId != null) {
            db.collection("studyPlans")
                    .document(planId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        // 로딩 숨김
                        if (binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }

                        if (documentSnapshot.exists()) {
                            currentPlan = documentSnapshot.toObject(StudyPlan.class);
                            if (currentPlan != null) {
                                currentPlan.setId(planId);
                                // UI 업데이트
                                updateUI(currentPlan);
                            }
                        } else {
                            Toast.makeText(this, "계획 정보를 가져올 수 없습니다", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        // 로딩 숨김
                        if (binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                        Toast.makeText(this, "데이터 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

    private void updateUI(StudyPlan plan) {
        // 날짜 및 시간 설정
        selectedDateTime = Calendar.getInstance();
        selectedDateTime.setTimeInMillis(plan.getTimestamp());
        updateDateTimeText();

        // 과목 및 설명 설정
        if (binding.etSubject != null && binding.etDescription != null) {
            binding.etSubject.setText(plan.getSubject());
            binding.etDescription.setText(plan.getDescription());
        }

        // 학습 시간 설정
        durationMinutes = plan.getDurationMinutes();
        if (binding.seekBarDuration != null) {
            binding.seekBarDuration.setProgress(durationMinutes);
        }
        updateDurationText(durationMinutes);
    }

    private void showTimePicker() {
        if (selectedDateTime != null) {
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
    }

    private void updateDateTimeText() {
        if (binding.tvDate != null && binding.tvTime != null && selectedDateTime != null) {
            SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.getDefault());
            SimpleDateFormat timeSdf = new SimpleDateFormat("a h:mm", Locale.getDefault());

            binding.tvDate.setText(dateSdf.format(selectedDateTime.getTime()));
            binding.tvTime.setText(timeSdf.format(selectedDateTime.getTime()));
        }
    }

    private void setupDurationSeekBar() {
        if (binding.seekBarDuration != null) {
            binding.seekBarDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
    }

    private void updateDurationText(int minutes) {
        if (binding.tvDuration != null) {
            int hours = minutes / 60;
            int mins = minutes % 60;

            String durationText;
            if (hours > 0) {
                durationText = hours + "시간 " + (mins > 0 ? mins + "분" : "");
            } else {
                durationText = mins + "분";
            }

            binding.tvDuration.setText(durationText);
        }
    }

    private void updatePlan() {
        // 현재 로그인된 사용자가 없으면 중단
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String subject = "";
        String description = "";

        if (binding.etSubject != null && binding.etDescription != null) {
            subject = binding.etSubject.getText().toString().trim();
            description = binding.etDescription.getText().toString().trim();

            if (subject.isEmpty()) {
                binding.etSubject.setError("과목명을 입력해주세요");
                return;
            }
        } else {
            Toast.makeText(this, "UI 요소를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // 로딩 표시
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        // 업데이트할 데이터
        DocumentReference docRef = db.collection("studyPlans").document(planId);

        // 업데이트 데이터 맵
        String userId = auth.getCurrentUser().getUid();

        // 학습 계획 객체 업데이트
        StudyPlan updatedPlan = new StudyPlan(
                selectedDateTime.getTimeInMillis(),
                subject,
                description,
                durationMinutes,
                currentPlan != null && currentPlan.isCompleted(),
                userId,
                planId
        );

        // Firestore 업데이트
        docRef.set(updatedPlan)
                .addOnSuccessListener(aVoid -> {
                    if (binding.progressBar != null) {
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    // 알림 업데이트
                    updateReminder(updatedPlan);

                    Toast.makeText(StudyPlanEditActivity.this, "학습 계획이 수정되었습니다", Toast.LENGTH_SHORT).show();
                    finish(); // 액티비티 종료
                })
                .addOnFailureListener(e -> {
                    if (binding.progressBar != null) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                    Toast.makeText(StudyPlanEditActivity.this, "수정 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateReminder(StudyPlan plan) {
        // 기존 알림 취소
        if (plan != null && plan.getId() != null) {
            StudyReminderManager.cancelReminder(this, plan.getId().hashCode());

            // 계획이 완료되지 않은 경우, 미래 시간이면 알림 예약
            if (!plan.isCompleted()) {
                long now = System.currentTimeMillis();
                long planTime = plan.getTimestamp();

                if (planTime > now) {
                    String title = plan.getSubject();
                    String message = "학습 계획: " + plan.getDescription();
                    int notificationId = plan.getId().hashCode();

                    StudyReminderManager.scheduleReminder(
                            this,
                            notificationId,
                            title,
                            message,
                            planTime
                    );
                }
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("학습 계획 삭제")
                .setMessage("이 학습 계획을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deletePlan())
                .setNegativeButton("취소", null)
                .show();
    }

    private void deletePlan() {
        // 로딩 표시
        if (binding.progressBar != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }

        if (planId != null) {
            db.collection("studyPlans")
                    .document(planId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        if (binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }

                        // 알림 취소
                        StudyReminderManager.cancelReminder(this, planId.hashCode());

                        Toast.makeText(this, "학습 계획이 삭제되었습니다", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        if (binding.progressBar != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                        Toast.makeText(this, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish(); // 뒤로가기
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}