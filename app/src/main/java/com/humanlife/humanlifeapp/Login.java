package com.humanlife.humanlifeapp;


import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 123;
    ProgressBar progressBar;
    EditText et_email, et_password;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference databaseReference;
    FirebaseUser user1;

    @Override
    protected void onStart() {
        super.onStart();
        user1 = FirebaseAuth.getInstance().getCurrentUser();

        if (user1 != null) {
            progressBar.setVisibility(View.VISIBLE);

          //  if (user1.isEmailVerified()) {
                databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
                databaseReference.child(user1.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Log.d("ASD1", user1.getEmail());
                            Intent intent = new Intent(Login.this, Home.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            finish();
                        }
                        else {
//                        Log.d("ASD1",user1.getEmail());

                            if (user1.getEmail() != null) {
                                if (user1.getDisplayName() != null) {
                                    Toast.makeText(Login.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Login.this, Registration.class);
                                    intent.putExtra("method", "Google");

                                    progressBar.setVisibility(View.GONE);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(Login.this, Registration.class);
                                    intent.putExtra("method", "Login");
                                    Toast.makeText(Login.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                    intent.putExtra("email", user1.getEmail());
                                    progressBar.setVisibility(View.GONE);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                            else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(Login.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Login.this, Registration.class);
                                intent.putExtra("method", "Mobile");


                                startActivity(intent);
                                finish();
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            }
//        else {
//            progressBar.setVisibility(View.GONE);
//            AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
//            builder.setTitle("Failed!")
//                    .setMessage(R.string.loginAlert)
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                        }
//                    }).create().show();
//        }
      //  }
        else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progress_login);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        findViewById(R.id.tv_register).setOnClickListener(this);
        findViewById(R.id.login).setOnClickListener(this);
        findViewById(R.id.google_login).setOnClickListener(this);
        findViewById(R.id.phone_login).setOnClickListener(this);
        findViewById(R.id.forgotPassword).setOnClickListener(this);
        createGoogleRequest();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_register:
                startActivity(new Intent(Login.this, SignUp.class));
                break;
            case R.id.login:

                userLogin();
                break;
            case R.id.google_login:
                googleLogin();
                break;
            case R.id.phone_login:
                startActivity(new Intent(Login.this, PhoneAuth.class));
                break;
            case R.id.forgotPassword:
                forgotPassword(v);
                break;
        }

    }

    private void forgotPassword(View v) {
        final EditText resetMail = new EditText(v.getContext());
        final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
        passwordResetDialog.setTitle("Reset Password ?");
        passwordResetDialog.setMessage("Enter Your Email To Received Reset Link.");
        passwordResetDialog.setView(resetMail);

        passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // extract the email and send reset link
                String mail = resetMail.getText().toString();
                mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Login.this, "Reset Link Sent To Your Email.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this, "Error ! Reset Link is Not Sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // close the dialog
            }
        });

        passwordResetDialog.create().show();
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
                         checkRegDBGoogle();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login.this, "Authentication failed", Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }


    private void userLogin() {


        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            et_email.setError("Email is Required.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            et_password.setError("Password is Required.");
            return;
        }
        if (password.length() < 6) {
            et_password.setError("Password Must be >= 6 Characters");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(mAuth.getCurrentUser().isEmailVerified()) {
                                // Sign in success, update UI with the signed-in user's information
                                checkRegDBLogin();
                            }else{
                                progressBar.setVisibility(View.GONE);
                                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                                builder.setTitle("Failed!")
                                        .setMessage(R.string.loginAlert)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        }).create().show();
                            }




                        } else {
                            // If sign in fails, display a message to the user.
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }

    private void checkRegDBLogin() {
        final FirebaseUser user = mAuth.getCurrentUser();
        Log.d("RESO",user.getEmail());
        Log.d("RESO",user.getUid());
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    Intent intent = new Intent(Login.this, Home.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Toast.makeText(Login.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    startActivity(intent);
                    finish();
                }
                else{
                    Log.d("ASD1","ZXC");


                    Intent intent = new Intent(Login.this, Registration.class);
                    intent.putExtra("method", "Login");
                    intent.putExtra("email", user.getEmail());
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Login.this,databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void checkRegDBGoogle() {
       FirebaseUser user = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Intent intent = new Intent(Login.this, Home.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Toast.makeText(Login.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                }
                else{
                    Log.d("ASD1","ZXC");
                    Intent intent = new Intent(Login.this, Registration.class);
                    intent.putExtra("method", "Google");
                    Toast.makeText(Login.this, "Logged in successfully", Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Login.this,databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}