package com.example.campus.study;

/**
 * 학습 계획 데이터 클래스
 */
public class StudyPlan {
    private long timestamp; // 계획 시간
    private String subject; // 과목
    private String description; // 설명
    private int durationMinutes; // 학습 시간 (분)
    private boolean completed; // 완료 여부
    private String userId; // Firebase 사용자 ID
    private String id; // Firestore 문서 ID

    /**
     * Firebase용 기본 생성자
     */
    public StudyPlan() {
        // Empty constructor needed for Firestore
    }

    /**
     * 학습 계획 생성자
     * @param timestamp 계획 시간
     * @param subject 과목
     * @param description 설명
     * @param durationMinutes 학습 시간 (분)
     * @param userId 사용자 ID
     */
    public StudyPlan(long timestamp, String subject, String description, int durationMinutes, String userId) {
        this.timestamp = timestamp;
        this.subject = subject;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.completed = false; // 기본값은 미완료
        this.userId = userId;
    }

    /**
     * 모든 필드를 포함한 생성자
     */
    public StudyPlan(long timestamp, String subject, String description, int durationMinutes, boolean completed, String userId, String id) {
        this.timestamp = timestamp;
        this.subject = subject;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.completed = completed;
        this.userId = userId;
        this.id = id;
    }

    // Getter와 Setter 메서드
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * 학습 시간을 문자열로 변환 (1시간 30분 형식)
     */
    public String getDurationString() {
        int hours = durationMinutes / 60;
        int mins = durationMinutes % 60;

        if (hours > 0) {
            return hours + "시간 " + (mins > 0 ? mins + "분" : "");
        } else {
            return mins + "분";
        }
    }
}