package com.example.campus.emotions;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campus.R;
import com.example.campus.databinding.ActivityEmotionStatsBinding;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmotionStatsActivity extends AppCompatActivity {

    private ActivityEmotionStatsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String timeRange = "주간"; // 기본값: 주간
    private static final String TAG = "EmotionStatsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmotionStatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 툴바 설정
        //  setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("감정 통계");
        }

        // 스피너 설정
        setupTimeRangeSpinner();

        // 현재 날짜 표시
        updateDateRangeText();

        // 데이터 로드
        loadEmotionData();
    }

    private void setupTimeRangeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.time_range_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTimeRange.setAdapter(adapter);

        binding.spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] options = getResources().getStringArray(R.array.time_range_options);
                timeRange = options[position];

                // 날짜 범위 업데이트
                updateDateRangeText();

                // 데이터 다시 로드
                loadEmotionData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무 것도 하지 않음
            }
        });
    }

    private void updateDateRangeText() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault());
        String endDateStr = sdf.format(calendar.getTime());

        // 시작 날짜 계산
        String startDateStr;
        switch (timeRange) {
            case "주간":
                calendar.add(Calendar.DATE, -7); // 7일 전
                startDateStr = sdf.format(calendar.getTime());
                break;
            case "월간":
                calendar.add(Calendar.MONTH, -1); // 1달 전
                startDateStr = sdf.format(calendar.getTime());
                break;
            case "연간":
                calendar.add(Calendar.YEAR, -1); // 1년 전
                startDateStr = sdf.format(calendar.getTime());
                break;
            default:
                startDateStr = endDateStr;
                break;
        }

        binding.tvDateRange.setText(startDateStr + " ~ " + endDateStr);
    }

    private void loadEmotionData() {
        // 현재 로그인된 사용자가 없으면 중단
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // 로딩 표시
        binding.progressBar.setVisibility(View.VISIBLE);

        // 시작 날짜 계산
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // 시작 날짜 계산
        switch (timeRange) {
            case "주간":
                calendar.add(Calendar.DATE, -7); // 7일 전
                break;
            case "월간":
                calendar.add(Calendar.MONTH, -1); // 1달 전
                break;
            case "연간":
                calendar.add(Calendar.YEAR, -1); // 1년 전
                break;
        }

        Date startDate = calendar.getTime();

        db.collection("emotions")
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("timestamp", startDate.getTime())
                .whereLessThanOrEqualTo("timestamp", endDate.getTime())
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // 데이터가 없는 경우
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "해당 기간에 기록된 감정이 없습니다", Toast.LENGTH_SHORT).show();
                        setupEmptyCharts();
                        return;
                    }

                    List<EmotionRecord> records = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        EmotionRecord record = document.toObject(EmotionRecord.class);
                        records.add(record);
                    }

                    // 차트 업데이트
                    updateCharts(records);

                    binding.progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "데이터 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "데이터 로드 실패", e);
                });
    }

    private void setupEmptyCharts() {
        // 라인 차트 초기화
        binding.lineChart.setNoDataText("데이터가 없습니다");
        binding.lineChart.invalidate();

        // 파이 차트 초기화
        binding.pieChart.setNoDataText("데이터가 없습니다");
        binding.pieChart.invalidate();
    }

    private void updateCharts(List<EmotionRecord> records) {
        updateLineChart(records);
        updatePieChart(records);
        updateStats(records);
    }

    private void updateLineChart(List<EmotionRecord> records) {
        List<Entry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        SimpleDateFormat labelFormat;
        switch (timeRange) {
            case "주간":
                labelFormat = new SimpleDateFormat("E", Locale.getDefault()); // 요일
                break;
            case "월간":
                labelFormat = new SimpleDateFormat("dd", Locale.getDefault()); // 일
                break;
            case "연간":
                labelFormat = new SimpleDateFormat("MM월", Locale.getDefault()); // 월
                break;
            default:
                labelFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());
                break;
        }

        for (int i = 0; i < records.size(); i++) {
            EmotionRecord record = records.get(i);
            entries.add(new Entry(i, record.getEmotionLevel()));

            Date date = new Date(record.getTimestamp());
            xLabels.add(labelFormat.format(date));
        }

        LineDataSet dataSet = new LineDataSet(entries, "감정 수준");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);

        binding.lineChart.setData(lineData);

        // X축 설정
        XAxis xAxis = binding.lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

        // Y축 설정
        YAxis leftAxis = binding.lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(6f);
        leftAxis.setGranularity(1f);

        YAxis rightAxis = binding.lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        binding.lineChart.getDescription().setEnabled(false);
        binding.lineChart.getLegend().setEnabled(true);
        binding.lineChart.setTouchEnabled(true);
        binding.lineChart.setDragEnabled(true);
        binding.lineChart.setScaleEnabled(true);
        binding.lineChart.setPinchZoom(true);

        binding.lineChart.invalidate(); // 차트 갱신
    }

    private void updatePieChart(List<EmotionRecord> records) {
        // 감정 유형별 카운트
        Map<String, Integer> emotionCounts = new HashMap<>();

        for (EmotionRecord record : records) {
            String type = record.getEmotionType();
            emotionCounts.put(type, emotionCounts.getOrDefault(type, 0) + 1);
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : emotionCounts.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "감정 유형");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);

        binding.pieChart.setData(pieData);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.getLegend().setEnabled(true);
        binding.pieChart.setCenterText("감정 분포");
        binding.pieChart.setCenterTextSize(16f);
        binding.pieChart.setEntryLabelTextSize(14f);
        binding.pieChart.setHoleRadius(40f);
        binding.pieChart.setTransparentCircleRadius(45f);

        binding.pieChart.invalidate(); // 차트 갱신
    }

    private void updateStats(List<EmotionRecord> records) {
        // 기본 통계 계산
        int totalRecords = records.size();
        int sumEmotionLevel = 0;

        for (EmotionRecord record : records) {
            sumEmotionLevel += record.getEmotionLevel();
        }

        float avgEmotionLevel = totalRecords > 0 ? (float) sumEmotionLevel / totalRecords : 0;

        // 텍스트 업데이트
        binding.tvTotalRecords.setText(String.valueOf(totalRecords));
        binding.tvAvgMood.setText(String.format(Locale.getDefault(), "%.1f", avgEmotionLevel));

        // 감정 분석 텍스트 업데이트
        String analysisText;
        if (avgEmotionLevel >= 4) {
            analysisText = "전반적으로 긍정적인 감정 상태입니다. 좋은 상태를 유지하세요!";
        } else if (avgEmotionLevel >= 3) {
            analysisText = "대체로 평온한 감정 상태입니다. 지금 상태가 편안하신가요?";
        } else if (avgEmotionLevel >= 2) {
            analysisText = "다소 부정적인 감정이 있습니다. 기분 전환을 위한 활동을 해보세요.";
        } else {
            analysisText = "감정 상태가 좋지 않습니다. 전문가와 상담하거나 AI 멘탈 케어를 이용해보세요.";
        }

        binding.tvAnalysis.setText(analysisText);
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