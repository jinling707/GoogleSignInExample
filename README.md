### 簡介
Google API 實現登入功能(單頁Activity)

將 Google 登錄機制加入的應用中，讓用戶可使用自己的 Google 帳號進行 Firebase 身份驗證


<img src="/README_IMG/Android Google 登入9.png" width="720px" />

### 說明
需先申辦Firebase的帳號，並將專案與Firebase的連接。
以下將演示登入專案的實現方。

### 專案與Firebase的連接
開啟新專案，點選Tool->Firebase,待跳出右邊視窗後，選Authentication->Email and password authentication

<img src="/README_IMG/Android Google 登入0.png" width="1057px" />

點開後將專案將專案與Firebase進行連結
不需要事先到Firebase中開啟新專案，直接在以下步驟中創建(Connect your app to Firebase)並連結(Add Firebase Authentication to your app)即可

<img src="/README_IMG/Android Google 登入1.png" width="420px" />

接著到自己的Firebase中的控制台 https://console.firebase.google.com/u/3/ 點選剛剛創建的專案

<img src="/README_IMG/Android Google 登入2.png" width="1057px" />

接著從旁邊的authentication中進入開啟Google登入

<img src="/README_IMG/Android Google 登入3.png" width="1057px" />

再回到Android Studio 點選右邊的 gradle->點開專案->Task->android->signingReport便可以看到SHA1和SHA-256，等等需要將其加入Firebase的專案中
(執行APP時，記得將上面的模式調回app才可以執行)

<img src="/README_IMG/Android Google 登入4.png" width="1057px" />

到Firebase的專案總攬->專案設定

<img src="/README_IMG/Android Google 登入5.png" width="1057px" />

在專案設定中的一般設定裡，將剛剛的SHA1和SHA-256新增上去，設定好後，下載google-services.json檔

<img src="/README_IMG/Android Google 登入6.png" width="1057px" />

將google-services.json檔放入android專案中的app

<img src="/README_IMG/Android Google 登入7.png" width="391px" />

#### 補充說明

官方教學文件可以點選android studio的連結進去 https://firebase.google.com/docs/auth/android/google-signin?utm_source=studio

<img src="/README_IMG/Android Google 登入8.png" width="720" />

如果要將APP上架到Play Store，需要到Firebase中設定專案與Play Store的連結

<img src="/README_IMG/Android Google 登入10.png" width="1057px" />

### android studio gradle設定
build.gradle(Project)  加入   classpath 'com.google.gms:google-services:4.2.0' 
``` java
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath "com.android.tools.build:gradle:4.0.1"
        classpath 'com.google.gms:google-services:4.2.0'  //GoogleSign
    }
}

```

build.gradle(app))  

``` java
    // Import the BoM for the Firebase platform
    implementation platform('com.google.firebase:firebase-bom:26.2.0')
    // Declare the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation 'com.google.firebase:firebase-auth'
    // Also declare the dependency for the Google Play services library and specify its version
    implementation 'com.google.android.gms:play-services-auth:19.0.0'

    implementation 'com.google.firebase:firebase-database:17.0.0'
    implementation 'com.google.firebase:firebase-core:17.0.0'
    implementation 'com.google.firebase:firebase-firestore:19.0.0'

    //google用戶圖片
    implementation 'com.github.bumptech.glide:glide:4.12.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'
```

### 程式碼
MainActivity 
``` java
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
```

activity_main 
``` xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:orientation="vertical"
    android:layout_height="match_parent"
    tools:context=".MainActivity">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="50dp"
        android:orientation="vertical">

        <ImageView
            android:id="@+id/imageIV"
            android:layout_gravity="center"
            android:layout_width="200dp"
            android:layout_height="200dp"
            android:src="@drawable/ic_baseline_account_box_24"
            />
        <TextView
            android:id="@+id/idTV"
            android:layout_gravity="center"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"/>
        <TextView
            android:id="@+id/nameTV"
            android:layout_gravity="center"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"/>
        <TextView
            android:id="@+id/mailTV"
            android:layout_gravity="center"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"/>

    </LinearLayout>

    <com.google.android.gms.common.SignInButton
        android:id="@+id/sign_in_button"
        android:layout_width="match_parent"
        android:layout_alignParentBottom="true"
        android:layout_height="wrap_content"
        />

    <Button
        android:layout_alignParentBottom="true"
        android:id="@+id/signOutBtn"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:onClick="signOut"
        android:text="Sign Out"
        android:background="#abcabc"
        android:visibility="invisible"

        />


</RelativeLayout>
```


### 參考資料
https://www.youtube.com/watch?v=t-yZUqthDMM
https://www.youtube.com/watch?v=E1eqRNTZqDM
官方教學: https://firebase.google.com/docs/auth/android/google-signin?utm_source=studio


