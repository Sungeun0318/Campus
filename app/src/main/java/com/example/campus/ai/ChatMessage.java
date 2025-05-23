package com.example.campus.ai;

/**
 * 채팅 메시지 데이터 클래스
 */
public class ChatMessage {
    private String text;
    private boolean isUser;
    private long timestamp;

    /**
     * 채팅 메시지 생성자
     * @param text 메시지 내용
     * @param isUser 사용자가 보낸 메시지인지 여부 (true: 사용자, false: AI)
     */
    public ChatMessage(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 전체 생성자
     * @param text 메시지 내용
     * @param isUser 사용자가 보낸 메시지인지 여부
     * @param timestamp 타임스탬프
     */
    public ChatMessage(String text, boolean isUser, long timestamp) {
        this.text = text;
        this.isUser = isUser;
        this.timestamp = timestamp;
    }

    // Getter와 Setter
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Firebase를 위한 기본 생성자
    public ChatMessage() {
        // Empty constructor needed for Firestore
    }
}