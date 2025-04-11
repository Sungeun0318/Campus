package com.example.campus.ai;

import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AiService {
    private static final String TAG = "AiService";
    private static volatile AiService INSTANCE;
    private FirebaseFunctions functions;
    private String geminiApiKey;

    private AiService() {
        functions = FirebaseFunctions.getInstance();
    }

    public static AiService getInstance() {
        if (INSTANCE == null) {
            synchronized (AiService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AiService();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Gemini API 키 설정
     * @param apiKey Gemini API 키
     */
    public void setGeminiApiKey(String apiKey) {
        this.geminiApiKey = apiKey;
    }

    /**
     * Firebase Functions를 통해 AI 응답 생성
     * @param prompt 사용자 메시지
     * @param callback 결과 콜백
     */
    public void generateAiResponseWithFunctions(String prompt, final AiCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("prompt", prompt);

        functions.getHttpsCallable("generateAIResponse")
                .call(data)
                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                    @Override
                    public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> result = (Map<String, Object>) task.getResult().getData();
                            String responseText = (String) result.get("text");
                            callback.onSuccess(responseText);
                        } else {
                            Log.e(TAG, "Error generating AI response", task.getException());
                            callback.onFailure(task.getException());
                        }
                    }
                });
    }

    /**
     * Gemini API를 직접 호출하여 AI 응답 생성
     * @param prompt 사용자 메시지
     * @param requestQueue Volley RequestQueue
     * @param callback 결과 콜백
     */
    public void generateAiResponseWithGemini(String prompt, RequestQueue requestQueue, final AiCallback callback) {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            callback.onFailure(new IllegalStateException("Gemini API 키가 설정되지 않았습니다."));
            return;
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + geminiApiKey;

            JSONObject jsonBody = new JSONObject();
            JSONArray contents = new JSONArray();

            JSONObject contentObject = new JSONObject();
            contentObject.put("role", "user");

            JSONArray parts = new JSONArray();
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt);
            parts.put(textPart);

            contentObject.put("parts", parts);
            contents.put(contentObject);

            jsonBody.put("contents", contents);
            jsonBody.put("generationConfig", new JSONObject()
                    .put("temperature", 0.7)
                    .put("maxOutputTokens", 1000));

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                Log.d(TAG, "Gemini API response: " + response.toString());
                                String aiResponse = extractTextFromGeminiResponse(response);
                                callback.onSuccess(aiResponse);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error parsing Gemini response", e);
                                callback.onFailure(e);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Error calling Gemini API", error);
                            callback.onFailure(error);
                        }
                    });

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating Gemini API request", e);
            callback.onFailure(e);
        }
    }

    private String extractTextFromGeminiResponse(JSONObject response) throws JSONException {
        StringBuilder result = new StringBuilder();

        JSONArray candidates = response.getJSONArray("candidates");
        if (candidates.length() > 0) {
            JSONObject candidate = candidates.getJSONObject(0);
            JSONObject content = candidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");

            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.getJSONObject(i);
                if (part.has("text")) {
                    result.append(part.getString("text"));
                }
            }
        }

        return result.toString();
    }

    /**
     * 특화된 학습 도우미 프롬프트 생성
     * @param query 사용자 질문/요청
     * @return 학습 도우미 맥락이 추가된 프롬프트
     */
    public String createStudyHelperPrompt(String query) {
        return "당신은 친절하고 효과적인 학습 도우미입니다. 학생들이 더 효율적으로 공부하고, 개념을 이해하고, " +
                "학습 계획을 세우는 데 도움을 주는 것이 당신의 역할입니다. 다음 질문이나 요청에 대해 도움을 주세요: " +
                query;
    }

    /**
     * 특화된 멘탈 케어 프롬프트 생성
     * @param query 사용자 감정/상황
     * @return 멘탈 케어 맥락이 추가된 프롬프트
     */
    public String createMentalCarePrompt(String query) {
        return "당신은 공감적이고 지지적인 심리 상담사입니다. 스트레스, 불안, 슬픔을 겪는 사람들에게 " +
                "위로와 실용적인 조언을 제공하는 것이 당신의 역할입니다. 다음 감정이나 상황에 대해 도움을 주세요: " +
                query;
    }

    /**
     * AI 응답 결과 콜백 인터페이스
     */
    public interface AiCallback {
        void onSuccess(String response);
        void onFailure(Exception e);
    }
}