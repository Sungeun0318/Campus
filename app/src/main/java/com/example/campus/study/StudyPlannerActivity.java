package com.example.campus.study;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.campus.R;
import com.example.campus.databinding.ActivityStudyPlannerBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudyPlannerActivity extends AppCompatActivity implements StudyPlanAdapter.StudyPlanClickListener {

    private ActivityStudyPlannerBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private StudyPlanAdapter adapter;
    private List<StudyPlan> studyPlans;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudyPlannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 툴바 설정
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("학습 계획");
        }

        // 현재 날짜로 초기화
        selectedDate = Calendar.getInstance();
        updateDateText();

        // 리사이클러뷰 설정
        setupRecyclerView();

        // 날짜 선택 버튼 클릭 리스너
        binding.btnSelectDate.setOnClickListener(v -> showDatePicker());

        // 계획 추가 버튼 클릭 리스너
        binding.fabAdd.setOnClickListener(v -> showAddPlanDialog());

        // 데이터 로드
        loadStudyPlans();
    }

    private void setupRecyclerView() {
        studyPlans = new ArrayList<>();
        adapter = new StudyPlanAdapter(this, studyPlans, this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void updateDateText() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.getDefault());
        binding.tvSelectedDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateText();
                        loadStudyPlans();
                    }
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadStudyPlans() {
        // 현재 로그인된 사용자가 없으면 중단
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // 선택된 날짜의 시작과 끝 시간 계산
        Calendar startCal = (Calendar) selectedDate.clone();
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = (Calendar) selectedDate.clone();
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);

        long startTime = startCal.getTimeInMillis();
        long endTime = endCal.getTimeInMillis();

        // 로딩 표시
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("studyPlans")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp", startTime)
                .whereLessThanOrEqualTo("timestamp", endTime)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    studyPlans.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        StudyPlan plan = document.toObject(StudyPlan.class);
                        plan.setId(document.getId());
                        studyPlans.add(plan);
                    }

                    // 어댑터 업데이트
                    adapter.updateStudyPlans(studyPlans);

                    // 데이터 유무에 따른 안내 메시지 표시
                    if (studyPlans.isEmpty()) {
                        binding.tvNoPlans.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvNoPlans.setVisibility(View.GONE);
                    }

                    binding.progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "데이터 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddPlanDialog() {
        Intent intent = new Intent(this, StudyPlanAddActivity.class);
        intent.putExtra("selectedDate", selectedDate.getTimeInMillis());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 활동 재개시 데이터 다시 로드
        loadStudyPlans();
    }

    @Override
    public void onPlanCheckedChanged(StudyPlan plan, boolean isChecked) {
        // 완료 상태 업데이트
        plan.setCompleted(isChecked);

        // Firestore 업데이트
        db.collection("studyPlans")
                .document(plan.getId())
                .update("completed", isChecked)
                .addOnSuccessListener(aVoid -> {
                    // 성공적으로 업데이트 됨
                    // 알림 생성 또는 취소
                    if (isChecked) {
                        // 완료된 계획의 알림 취소
                        StudyReminderManager.cancelReminder(this, plan.getId().hashCode());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "상태 업데이트 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onPlanClicked(StudyPlan plan) {
        // 계획 상세 보기 또는 편집
        Intent intent = new Intent(this, StudyPlanEditActivity.class);
        intent.putExtra("planId", plan.getId());
        startActivity(intent);
    }

    @Override
    public void onPlanLongClicked(StudyPlan plan) {
        // 계획 삭제 다이얼로그 표시
        new AlertDialog.Builder(this)
                .setTitle("학습 계획 삭제")
                .setMessage("이 학습 계획을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deletePlan(plan))
                .setNegativeButton("취소", null)
                .show();
    }

    private void deletePlan(StudyPlan plan) {
        db.collection("studyPlans")
                .document(plan.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "학습 계획이 삭제되었습니다", Toast.LENGTH_SHORT).show();

                    // 알림 취소
                    StudyReminderManager.cancelReminder(this, plan.getId().hashCode());

                    // 데이터 다시 로드
                    loadStudyPlans();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 뒤로가기
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}