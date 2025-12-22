package com.bypriyan.togocart.register;

import static android.app.ProgressDialog.show;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bypriyan.togocart.BuildConfig;
import com.bypriyan.togocart.R;
import com.bypriyan.togocart.activity.MainActivity;
import com.bypriyan.togocart.databinding.ActivityLoginBinding;
import com.bypriyan.togocart.utilities.Constant;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance(); // ✅ IMPORTANT

        binding.otpBtn.setOnClickListener(v -> {
            String number = binding.mobile.getText().toString().trim();

            if (number.length() != 10) {
                binding.mobile.setError("Enter valid number");
                return;
            }

            // ✅ TEST LOGIN (DEBUG ONLY)
            if (BuildConfig.DEBUG && number.equals("9179593730")) {
                testLogin();
            } else {
                otpSend(number);
            }
        });
    }

    // ✅ Anonymous Firebase Login (creates UID)
    private void testLogin() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            goToMain();
            return;
        }

        mAuth.signInAnonymously()
                .addOnSuccessListener(authResult -> goToMain())
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void otpSend(String number) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91" + number)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {}

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
                                intent.putExtra("verificationId", verificationId);
                                intent.putExtra("phoneNumber", number);
                                startActivity(intent);
                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}

