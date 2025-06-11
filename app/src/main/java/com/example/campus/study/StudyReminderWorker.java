package com.example.campus.study;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * 학습 계획 알림을 표시하는 작업자 클래스
 */
public class StudyReminderWorker extends Worker {

    private static final String TAG = "StudyReminderWorker";

    public StudyReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        try {
            // 알림 데이터 가져오기
            int notificationId = getInputData().getInt("notificationId", 0);
            String title = getInputData().getString("title");
            String message = getInputData().getString("message");
            String channelId = getInputData().getString("channelId");

            if (title == null || message == null || channelId == null) {
                Log.e(TAG, "알림 데이터가 올바르지 않습니다.");
                return Result.failure();
            }

            // 알림 표시
            StudyReminderManager.showNotification(context, notificationId, title, message, channelId);

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "알림 표시 중 오류 발생: " + e.getMessage());
            return Result.failure();
        }
    }
}