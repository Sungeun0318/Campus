package com.example.campus.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.campus.R;
import com.example.campus.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MoreFragment extends Fragment {

    private FirebaseAuth auth;

    public MoreFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        // Firebase Auth 초기화
        auth = FirebaseAuth.getInstance();

        // 로그아웃 버튼 설정
        setupLogoutButton(view);

        return view;
    }

    private void setupLogoutButton(View view) {
        Button btnLogout = view.findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                auth.signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }
    }
}