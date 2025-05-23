package com.example.campus.ai;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.campus.databinding.ActivityAiChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiChatActivity extends AppCompatActivity {

    private ActivityAiChatBinding binding;
    private ChatMessageAdapter messageAdapter;
    private List<ChatMessage> messageList;
    private AiService aiService;
    private String chatMode; // "study" 또는 "mental"
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Volley RequestQueue 초기화
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // AI 서비스 초기화
        aiService = AiService.getInstance();

        // Gemini API 키 설정 (실제 키로 교체 필요)
        aiService.setGeminiApiKey("AIzaSyATFfB28_oKZlP5cA_kRrcAQuvyV6KX4fw");

        // 채팅 모드 가져오기
        chatMode = getIntent().getStringExtra("chat_mode");
        if (chatMode == null) {
            chatMode = "study"; // 기본값
        }

        setupToolbar();
        setupRecyclerView();
        setupSendButton();

        // 모드에 따른 환영 메시지 표시
        if ("study".equals(chatMode)) {
            addMessage(new ChatMessage("안녕하세요! 학습을 도와드릴 AI 도우미입니다. 어떤 과목을 공부하고 계신가요?", false));
        } else if ("mental".equals(chatMode)) {
            addMessage(new ChatMessage("안녕하세요! 오늘 기분이 어떠신가요? 언제든 편하게 이야기해주세요.", false));
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // 채팅 모드에 따라 타이틀 설정
            if ("study".equals(chatMode)) {
                getSupportActionBar().setTitle("학습 도우미");
            } else if ("mental".equals(chatMode)) {
                getSupportActionBar().setTitle("멘탈 케어");
            } else {
                getSupportActionBar().setTitle("AI 챗봇");
            }
        }
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(this, messageList);
        binding.recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void setupSendButton() {
        binding.btnSend.setOnClickListener(v -> {
            String message = binding.etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                binding.etMessage.setText("");
            }
        });
    }

    private void sendMessage(String message) {
        // 사용자 메시지 추가
        ChatMessage userMessage = new ChatMessage(message, true);
        addMessage(userMessage);

        // 채팅 기록 저장
        saveMessageToFirestore(userMessage);

        // 로딩 표시
        binding.progressBar.setVisibility(View.VISIBLE);

        // 모드에 따라 프롬프트 생성
        String prompt;
        if ("study".equals(chatMode)) {
            prompt = aiService.createStudyHelperPrompt(message);
        } else if ("mental".equals(chatMode)) {
            prompt = aiService.createMentalCarePrompt(message);
        } else {
            prompt = message;
        }

        // Gemini API 직접 호출
        aiService.generateAiResponseWithFunctions(prompt, new AiService.AiCallback() {
            @Override
            public void onSuccess(String response) {
                // 로딩 숨김
                binding.progressBar.setVisibility(View.GONE);

                // AI 응답 메시지 추가
                ChatMessage aiMessage = new ChatMessage(response, false);
                addMessage(aiMessage);

                // 채팅 기록 저장
                saveMessageToFirestore(aiMessage);
            }

            @Override
            public void onFailure(Exception e) {
                // 로딩 숨김
                binding.progressBar.setVisibility(View.GONE);

                Toast.makeText(AiChatActivity.this, "오류: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();

                // 에러 메시지 추가
                ChatMessage errorMessage = new ChatMessage("응답을 생성하는 중 오류가 발생했습니다.", false);
                addMessage(errorMessage);
            }
        });
    }

    private void addMessage(ChatMessage message) {
        messageList.add(message);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        binding.recyclerViewMessages.scrollToPosition(messageList.size() - 1);
    }

    private void saveMessageToFirestore(ChatMessage message) {
        // 현재 로그인된 사용자만 저장
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        String chatId = userId + "_" + chatMode; // 각 채팅 모드별로 구분

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("text", message.getText());
        messageData.put("isUser", message.isUser());
        messageData.put("timestamp", message.getTimestamp());

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(messageData)
                .addOnFailureListener(e -> Log.e("AiChatActivity", "메시지 저장 실패", e));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}