package com.example.campus.emotions;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.campus.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
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

    // UI 요소들 직접 선언
    private Toolbar toolbar;
    private Spinner spinnerTimeRange;
    private TextView tvDateRange;
    private LineChart lineChart;
    private PieChart pieChart;
    private TextView tvTotalRecords;
    private TextView tvAvgMood;
    private TextView tvAnalysis;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String timeRange = "주간"; // 기본값: 주간
    private static final String TAG = "EmotionStatsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion_stats);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // UI 요소 초기화
        initializeViews();

        // 툴바 설정
        setupToolbar();

        // 스피너 설정
        setupTimeRangeSpinner();

        // 현재 날짜 표시
        updateDateRangeText();

        // 데이터 로드
        loadEmotionData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerTimeRange = findViewById(R.id.spinnerTimeRange);
        tvDateRange = findViewById(R.id.tvDateRange);
        lineChart = findViewById(R.id.lineChart);
        pieChart = findViewById(R.id.pieChart);
        tvTotalRecords = findViewById(R.id.tvTotalRecords);
        tvAvgMood = findViewById(R.id.tvAvgMood);
        tvAnalysis = findViewById(R.id.tvAnalysis);
        progressBar = findViewById(R.id.progressBar);

        // Null 체크
        if (toolbar == null || spinnerTimeRange == null || tvDateRange == null ||
                lineChart == null || pieChart == null || tvTotalRecords == null ||
                tvAvgMood == null || tvAnalysis == null || progressBar == null) {
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
                    getSupportActionBar().setTitle("감정 통계");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            setTitle("감정 통계");
        }
    }

    private void setupTimeRangeSpinner() {
        if (spinnerTimeRange == null) return;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.time_range_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeRange.setAdapter(adapter);

        spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        if (tvDateRange == null) return;

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

        tvDateRange.setText(startDateStr + " ~ " + endDateStr);
    }

    private void loadEmotionData() {
        // 현재 로그인된 사용자가 없으면 중단
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // 로딩 표시
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

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
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
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

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Toast.makeText(this, "데이터 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "데이터 로드 실패", e);
                });
    }

    private void setupEmptyCharts() {
        // 라인 차트 초기화
        if (lineChart != null) {
            lineChart.setNoDataText("데이터가 없습니다");
            lineChart.invalidate();
        }

        // 파이 차트 초기화
        if (pieChart != null) {
            pieChart.setNoDataText("데이터가 없습니다");
            pieChart.invalidate();
        }

        // 통계 초기화
        if (tvTotalRecords != null) tvTotalRecords.setText("0");
        if (tvAvgMood != null) tvAvgMood.setText("0.0");
        if (tvAnalysis != null) tvAnalysis.setText("감정 데이터가 충분하지 않습니다. 더 많은 감정을 기록해주세요.");
    }

    private void updateCharts(List<EmotionRecord> records) {
        updateLineChart(records);
        updatePieChart(records);
        updateStats(records);
    }

    private void updateLineChart(List<EmotionRecord> records) {
        if (lineChart == null) return;

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

        lineChart.setData(lineData);

        // X축 설정
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

        // Y축 설정
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(6f);
        leftAxis.setGranularity(1f);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);

        lineChart.invalidate(); // 차트 갱신
    }

    private void updatePieChart(List<EmotionRecord> records) {
        if (pieChart == null) return;

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

        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(true);
        pieChart.setCenterText("감정 분포");
        pieChart.setCenterTextSize(16f);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);

        pieChart.invalidate(); // 차트 갱신
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
        if (tvTotalRecords != null) {
            tvTotalRecords.setText(String.valueOf(totalRecords));
        }
        if (tvAvgMood != null) {
            tvAvgMood.setText(String.format(Locale.getDefault(), "%.1f", avgEmotionLevel));
        }

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

        if (tvAnalysis != null) {
            tvAnalysis.setText(analysisText);
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