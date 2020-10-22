package com.omaressam.instagram.view.auth.register;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.omaressam.instagram.R;
import com.omaressam.instagram.models.User;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText username;
    private EditText email;
    private EditText password;
    private Button sinUp;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupView();
    }

    void setupView() {
        username = findViewById(R.id.username_editText);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        sinUp = findViewById(R.id.sign_up_button);
        sinUp.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View v) {
      registerUsers();
    }

    public void registerUsers () {

        String Username = username.getText().toString();
        String Email = email.getText().toString();
        String Password = password.getText().toString();

        if (TextUtils.isEmpty(Username)) {
            username.setError("Enter your email");
            Toast.makeText(this, "Please enter yor name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Email)) {
            email.setError("Enter your email");
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
            //stopping execution further
            return;
        }

        if (TextUtils.isEmpty(Password)) {
            password.setError("Enter your password");
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            //stopping execution further
            return;
        }


        User user = new User();
        user.setName(Username);
        user.setEmail(Email);
        user.setPassword(Password);
        createUser(user);
    }

    private void createUser(final User user) {

        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            assert firebaseUser != null;
                            saveUsers(firebaseUser.getUid(),user,password.getText().toString());
                        } else {

                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUsers (String id,User user ,String password) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users").child(id);
        user.setPassword(password);
        myRef.setValue(user);
        Toast.makeText(RegisterActivity.this, "Success",
                Toast.LENGTH_SHORT).show();
        finish();
    }
}
