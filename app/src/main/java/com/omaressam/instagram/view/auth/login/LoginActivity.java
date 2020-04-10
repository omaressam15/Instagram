package com.omaressam.instagram.view.auth.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.omaressam.instagram.R;
import com.omaressam.instagram.view.auth.register.RegisterActivity;
import com.omaressam.instagram.view.main.MainActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail;
    private EditText passwords;
    private Button SinIn;
    private TextView SinUp;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupView();
    }

    private void setupView () {
        editTextEmail = findViewById(R.id.editText_Email);
        passwords  = findViewById(R.id.editText_password);
        SinIn = findViewById(R.id.button_login);
        SinUp = findViewById(R.id.textView_singUp);

        SinIn.setOnClickListener(this);
        SinUp.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login:
              registerUsers();

            break;
            case R.id.textView_singUp:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
        }
    }

    private void registerUsers () {

        String email = editTextEmail.getText().toString();
        String Password = passwords.getText().toString();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Enter your email");
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Password)) {
            passwords.setError("Enter your password");
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            //stopping execution further
            return;
        }
        signIn(email, Password);

    }

    private void signIn (final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();

                        } else {

                            Toast.makeText(LoginActivity.this, "Wrong email and password!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
