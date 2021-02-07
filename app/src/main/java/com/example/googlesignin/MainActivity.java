package com.example.googlesignin;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    Button signOutBtn;
    TextView nameTV,mailTV,idTV;
    ImageView imageIV;
    private int RC_SIGN_IN = 0;
    String TAG = "GoogleSignIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nameTV = findViewById(R.id.nameTV);
        idTV = findViewById(R.id.idTV);
        mailTV = findViewById(R.id.mailTV);
        imageIV = findViewById(R.id.imageIV);
        signOutBtn = findViewById(R.id.signOutBtn);
        signInButton = findViewById(R.id.sign_in_button);

        creatRequest(); //配置Google登入
        userSignStatus();//檢查使用者是否已經有登入

        signInButton.setOnClickListener(new View.OnClickListener()//不用onClick的方式，因為需要在初始配置中先獲得mGoogleSignInClient
        {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }
    private void creatRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) //R.string.default_web_client_id需要將系統執行一次，讓他產生
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
    }
    private void userSignStatus()//檢查使用者是否已經有登入，有的話直接跳到ProfileActivity
    {
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount != null)
        {
            idTV.setText(signInAccount.getId());
            nameTV.setText(signInAccount.getDisplayName());
            mailTV.setText(signInAccount.getEmail());
            Uri AccountUri = signInAccount.getPhotoUrl();
            Glide.with(this).load(String.valueOf(AccountUri)).into(imageIV);

            signOutBtn.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.INVISIBLE);
        }
    }
    private void signIn() {
        Intent signIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signIntent,RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask)
    {
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount signInAccount = completedTask.getResult(ApiException.class);
            idTV.setText(signInAccount.getId());
            nameTV.setText(signInAccount.getDisplayName());
            mailTV.setText(signInAccount.getEmail());
            Uri AccountUri = signInAccount.getPhotoUrl();
            Glide.with(this).load(String.valueOf(AccountUri)).into(imageIV);

            signOutBtn.setVisibility(View.VISIBLE);
            signInButton.setVisibility(View.INVISIBLE);

        } catch (ApiException e) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e);
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    public void signOut(View view) {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this,"Signed Out",Toast.LENGTH_SHORT).show();

                    }
                });
        idTV.setText("");
        nameTV.setText("");
        mailTV.setText("");
        imageIV.setImageResource(R.drawable.ic_baseline_account_box_24);

        signOutBtn.setVisibility(View.INVISIBLE);
        signInButton.setVisibility(View.VISIBLE);
    }

}