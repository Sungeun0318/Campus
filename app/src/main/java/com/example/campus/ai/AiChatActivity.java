package com.example.campus.ai;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.campus.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiChatActivity extends AppCompatActivity {

    // UI 요소들 직접 선언 (ViewBinding 대신)
    private Toolbar toolbar;
    private RecyclerView recyclerViewMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar progressBar;

    private ChatMessageAdapter messageAdapter;
    private List<ChatMessage> messageList;
    private AiService aiService;
    private String chatMode; // "study" 또는 "mental"
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Volley RequestQueue 초기화
        requestQueue = Volley.newRequestQueue(this);

        // AI 서비스 초기화
        aiService = AiService.getInstance();

        // API 키 설정 (환경변수나 BuildConfig 사용 권장)
        String apiKey = "AIzaSyAvvEg02npprhuQ_S4Ln8lfbmxY4zAm2BU"; // 실제 환경에서는 BuildConfig 사용
        aiService.setGeminiApiKey(apiKey);

        // 채팅 모드 가져오기
        chatMode = getIntent().getStringExtra("chat_mode");
        if (chatMode == null) {
            chatMode = "study"; // 기본값
        }

        // UI 초기화
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSendButton();

        // 모드에 따른 환영 메시지 표시
        showWelcomeMessage();
    }

    private void initializeViews() {
        // findViewById로 UI 요소들 초기화
        toolbar = findViewById(R.id.toolbar);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);

        // Null 체크
        if (toolbar == null || recyclerViewMessages == null ||
                etMessage == null || btnSend == null || progressBar == null) {
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
        } catch (Exception e) {
            e.printStackTrace();
            setTitle("AI 챗봇");
        }
    }

    private void setupRecyclerView() {
        if (recyclerViewMessages == null) return;

        messageList = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(this, messageList);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void setupSendButton() {
        if (btnSend == null || etMessage == null) return;

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(message);
                etMessage.setText("");
            }
        });
    }

    private void showWelcomeMessage() {
        if ("study".equals(chatMode)) {
            addMessage(new ChatMessage("안녕하세요! 학습을 도와드릴 AI 도우미입니다. 어떤 과목을 공부하고 계신가요?", false));
        } else if ("mental".equals(chatMode)) {
            addMessage(new ChatMessage("안녕하세요! 오늘 기분이 어떠신가요? 언제든 편하게 이야기해주세요.", false));
        }
    }

    private void sendMessage(String message) {
        // 사용자 메시지 추가
        ChatMessage userMessage = new ChatMessage(message, true);
        addMessage(userMessage);

        // 채팅 기록 저장
        saveMessageToFirestore(userMessage);

        // 로딩 표시
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // 모드에 따라 프롬프트 생성
        String prompt;
        if ("study".equals(chatMode)) {
            prompt = aiService.createStudyHelperPrompt(message);
        } else if ("mental".equals(chatMode)) {
            prompt = aiService.createMentalCarePrompt(message);
        } else {
            prompt = message;
        }

        // Firebase Functions 사용 (더 안전함)
        aiService.generateAiResponseWithFunctions(prompt, new AiService.AiCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    // 로딩 숨김
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    // AI 응답 메시지 추가
                    ChatMessage aiMessage = new ChatMessage(response, false);
                    addMessage(aiMessage);

                    // 채팅 기록 저장
                    saveMessageToFirestore(aiMessage);
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    // 로딩 숨김
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    Log.e("AiChatActivity", "AI 응답 오류", e);

                    // Firebase Functions가 실패한 경우 직접 API 호출 시도
                    tryDirectApiCall(prompt);
                });
            }
        });
    }

    private void tryDirectApiCall(String prompt) {
        // 직접 Gemini API 호출
        aiService.generateAiResponseWithGemini(prompt, requestQueue, new AiService.AiCallback() {
            @Override
            public void onSuccess(String response) {
                // AI 응답 메시지 추가
                ChatMessage aiMessage = new ChatMessage(response, false);
                addMessage(aiMessage);

                // 채팅 기록 저장
                saveMessageToFirestore(aiMessage);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AiChatActivity.this, "죄송합니다. 현재 AI 서비스에 일시적인 문제가 있습니다.",
                        Toast.LENGTH_SHORT).show();

                // 기본 응답 메시지 추가
                ChatMessage errorMessage = new ChatMessage(
                        "죄송합니다. 현재 서비스에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.", false);
                addMessage(errorMessage);
            }
        });
    }

    private void addMessage(ChatMessage message) {
        if (messageList != null && messageAdapter != null) {
            messageList.add(message);
            messageAdapter.notifyItemInserted(messageList.size() - 1);
            if (recyclerViewMessages != null) {
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            }
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}