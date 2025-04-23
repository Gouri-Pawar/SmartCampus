package com.example.smartcampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {


    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private RadioButton facultyRadioButton, studentRadioButton;
    private Button signUpButton;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        facultyRadioButton = findViewById(R.id.facultyRadioButton);
        studentRadioButton = findViewById(R.id.studentRadioButton);
        signUpButton = findViewById(R.id.signUpButton);
        progressBar = findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();

        signUpButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            registerUser();
        });
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String role = facultyRadioButton.isChecked() ? "faculty" : studentRadioButton.isChecked() ? "student" : "";

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role.isEmpty()) {
            showToast("Please fill all fields and select a role");
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (!password.equals(confirmPassword))
        {
            showToast("Passwords do not match");
            progressBar.setVisibility(View.GONE);
            return;
        }

        saveUserToFirestore(email, password, role);
    }

    private void saveUserToFirestore(String email, String password, String role) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("email", email);
                            userMap.put("role", role);

                            db.collection("users").document(uid).set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        showToast("Sign up successful!");
                                        progressBar.setVisibility(View.GONE);
                                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        showToast("Failed to save user: " + e.getMessage());
                                        progressBar.setVisibility(View.GONE);
                                    });
                        }
                    } else {
                        showToast("Sign up failed: " + task.getException().getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }


    private void showToast(String message) {
        Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}