package com.humanlife.humanlifeapp;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignUp extends AppCompatActivity implements View.OnClickListener {
    EditText et_email,et_password;
    ProgressBar progressB;
    private FirebaseAuth mAuth;
    public static final String TAG = "TAG";
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mAuth = FirebaseAuth.getInstance();
        progressB = findViewById(R.id.progress_signup);
        et_email = findViewById(R.id.et_signup_email);
        et_password = findViewById(R.id.et_signup_password);
        findViewById(R.id.signup).setOnClickListener(this);
        findViewById(R.id.tv_login).setOnClickListener(this);
        findViewById(R.id.google_signup).setOnClickListener(this);
        findViewById(R.id.phone_signup).setOnClickListener(this);

        createGoogleRequest();

    }


    private void createGoogleRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }
    // to pop up email option for user to select email after clicking google signin
    private void googleLogin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAGRES", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, e.getMessage()+"ERROR", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignUp.this, "Signed in Successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUp.this, Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignUp.this, "Authentication failed", Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.tv_login:
                startActivity(new Intent(SignUp.this, Login.class));
                finish();
                break;
            case R.id.signup:
                userSignup();
                break;

            case R.id.google_signup:
                googleLogin();
                break;

            case R.id.phone_signup:
                startActivity(new Intent(SignUp.this, PhoneAuth.class));
                break;
        }

    }

    private void userSignup() {
        final String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        if(TextUtils.isEmpty(email)) {
            et_email.setError("Email is Required.");
            et_email.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(password)){
            et_password.setError("Password is Required.");
            et_password.requestFocus();
            return;
        }

        if(password.length() < 6){
            et_password.setError("Password Must be >= 6 Characters");
            et_password.requestFocus();
            return;
        }
        progressB.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    // email sent
                                                    Toast.makeText(SignUp.this, "Verification Link has been sent.", Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Log.d(TAG,"onFailure: Email not sent "+e.getMessage());
                                }
                            });

//                            Log.d("RESO",user.getEmail());
                            progressB.setVisibility(View.GONE);
                            Toast.makeText(SignUp.this, "User Created.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUp.this,Login.class));
                            finish();
                        } else {
                            progressB.setVisibility(View.GONE);
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignUp.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }
}