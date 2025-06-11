package com.example.campus.Fragment;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.campus.R;

public class TimerFragment extends Fragment {

    private TextView timerText;
    private Button startButton;
    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private long timeLeftInMillis = 25 * 60 * 1000; // 25분

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        // UI 요소 초기화
        timerText = view.findViewById(R.id.timer_text);
        startButton = view.findViewById(R.id.start_button);

        // Null 체크 추가
        if (timerText == null || startButton == null) {
            return view;
        }

        updateTimerText();

        startButton.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });

        return view;
    }

    private void startTimer() {
        if (timeLeftInMillis <= 0) {
            timeLeftInMillis = 25 * 60 * 1000; // 리셋
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            public void onFinish() {
                isRunning = false;
                timeLeftInMillis = 25 * 60 * 1000; // 다시 25분으로 리셋
                updateTimerText();
                if (startButton != null) {
                    startButton.setText("시작");
                }
            }
        }.start();

        isRunning = true;
        if (startButton != null) {
            startButton.setText("일시정지");
        }
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        if (startButton != null) {
            startButton.setText("시작");
        }
    }

    private void updateTimerText() {
        if (timerText == null) return;

        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timerText.setText(timeFormatted);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 메모리 누수 방지
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}