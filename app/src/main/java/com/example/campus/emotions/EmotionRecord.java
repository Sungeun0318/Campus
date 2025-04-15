package com.example.campus.emotions;

/**
 * 감정 기록 데이터 클래스
 */
@SuppressWarnings("unused") // Firestore에서 리플렉션으로 사용되므로 경고 억제
public class EmotionRecord {
    private long timestamp;
    private int emotionLevel; // 1-5 등급 (1: 매우 나쁨, 5: 매우 좋음)
    private String emotionType; // "행복", "슬픔", "분노", "불안", "중립" 등
    private String note; // 감정에 대한 간단한 메모
    private String userId; // Firebase 사용자 ID

    /**
     * Firebase용 기본 생성자
     */
    public EmotionRecord() {
        // Empty constructor needed for Firestore
    }

    /**
     * 감정 기록 생성자
     * @param emotionLevel 감정 수준 (1-5)
     * @param emotionType 감정 유형
     * @param note 메모
     * @param userId 사용자 ID
     */
    public EmotionRecord(int emotionLevel, String emotionType, String note, String userId) {
        this.timestamp = System.currentTimeMillis();
        this.emotionLevel = emotionLevel;
        this.emotionType = emotionType;
        this.note = note;
        this.userId = userId;
    }

    /**
     * 모든 필드를 포함한 생성자
     */
    public EmotionRecord(long timestamp, int emotionLevel, String emotionType, String note, String userId) {
        this.timestamp = timestamp;
        this.emotionLevel = emotionLevel;
        this.emotionType = emotionType;
        this.note = note;
        this.userId = userId;
    }

    // Getter와 Setter 메서드
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getEmotionLevel() {
        return emotionLevel;
    }

    public void setEmotionLevel(int emotionLevel) {
        this.emotionLevel = emotionLevel;
    }

    public String getEmotionType() {
        return emotionType;
    }

    public void setEmotionType(String emotionType) {
        this.emotionType = emotionType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}