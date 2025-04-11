package com.example.campus.study;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.campus.R;

import java.util.concurrent.TimeUnit;

/**
 * 학습 계획 알림 관리 클래스
 */
public class StudyReminderManager {

    private static final String CHANNEL_ID = "study_reminder_channel";
    private static final String CHANNEL_NAME = "학습 계획 알림";
    private static final String CHANNEL_DESCRIPTION = "학습 계획 시작 시간에 알림을 보냅니다.";
    private static final String TAG = "StudyReminderManager";

    /**
     * 알림 채널 생성 (Android 8.0 이상)
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * 학습 계획 알림 예약
     * @param context 컨텍스트
     * @param notificationId 알림 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param triggerTimeMillis 알림 발생 시간 (밀리초)
     */
    public static void scheduleReminder(Context context, int notificationId, String title, String message, long triggerTimeMillis) {
        // 알림 채널 생성
        createNotificationChannel(context);

        long currentTimeMillis = System.currentTimeMillis();
        long delayMillis = triggerTimeMillis - currentTimeMillis;

        // 과거 시간이면 알림 예약하지 않음
        if (delayMillis <= 0) {
            Log.w(TAG, "알림 시간이 현재 시간보다 이전입니다. 알림이 예약되지 않습니다.");
            return;
        }

        // 작업 데이터 설정
        Data inputData = new Data.Builder()
                .putInt("notificationId", notificationId)
                .putString("title", title)
                .putString("message", message)
                .putString("channelId", CHANNEL_ID)
                .build();

        // 한 번만 실행되는 작업 생성
        OneTimeWorkRequest reminderWorkRequest = new OneTimeWorkRequest.Builder(StudyReminderWorker.class)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("reminder_" + notificationId) // 태그 추가
                .build();

        // 작업 예약
        WorkManager.getInstance(context).enqueue(reminderWorkRequest);

        Log.d(TAG, "알림 예약됨: ID=" + notificationId + ", 제목=" + title + ", " +
                (delayMillis / (60 * 1000)) + "분 후 발생");
    }

    /**
     * 학습 계획 알림 취소
     * @param context 컨텍스트
     * @param notificationId 알림 ID
     */
    public static void cancelReminder(Context context, int notificationId) {
        // 작업 취소
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_" + notificationId);

        // 이미 표시된 알림도 취소
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(notificationId);
        }

        Log.d(TAG, "알림 취소됨: ID=" + notificationId);
    }

    /**
     * 알림 표시
     * @param context 컨텍스트
     * @param notificationId 알림 ID
     * @param title 알림 제목
     * @param message 알림 메시지
     * @param channelId 채널 ID
     */
    public static void showNotification(Context context, int notificationId, String title, String message, String channelId) {
        // 알림 클릭 시 학습 계획 화면으로 이동
        Intent intent = new Intent(context, StudyPlannerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // PendingIntent 생성
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 알림 생성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_dashboard_black_24dp) // 앱 아이콘으로 변경 필요
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // 알림 표시
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }
}