package com.omaressam.instagram.view.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.omaressam.instagram.R;
import com.omaressam.instagram.view.auth.login.LoginActivity;
import com.omaressam.instagram.view.main.MainActivity;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth auth ;

    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        imageView = findViewById(R.id.imageView2);
        auth = FirebaseAuth.getInstance();

        final FirebaseUser user = auth.getCurrentUser();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (user==null){

                    startActivity(new Intent(SplashActivity.this,LoginActivity.class));
                    finish();
                }else {

                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }

            }
        }, 2000);

    }


}
