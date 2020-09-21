package com.humanlife.humanlifeapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

public class Registration extends AppCompatActivity {
    private EditText et_name, et_mobileno, et_email, et_city;
    Toolbar toolbar;
    private TextView tv_name, tv_email, tv_mobile;
    ImageView img;
    DatePicker datePicker;
    private Button submit;
    private int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri,personPhoto;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private String dob;
    private StorageTask mUploadTask;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        toolbar = findViewById(R.id.toolbar_registration);
        img = findViewById(R.id.prof_iv);
        et_name = findViewById(R.id.name);
        tv_name = findViewById(R.id.tv_name);
        et_mobileno = findViewById(R.id.mobile_no);
        tv_mobile = findViewById(R.id.tv_mobile_no);
        et_email = findViewById(R.id.email);
        tv_email = findViewById(R.id.tv_email);
        datePicker = findViewById(R.id.dob);
        et_city = findViewById(R.id.et_city);
        submit = findViewById(R.id.submit);
        Resources resources = getResources();
        mAuth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference("images");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        user = FirebaseAuth.getInstance().getCurrentUser();
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back_icon);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        intentValidation();
        findViewById(R.id.choose_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDob();
                submission();
            }
        });

    }

    private void intentValidation() {
        //If login with email and pass
        String method = getIntent().getStringExtra("method");
        if(method != null) {
            if (method.equalsIgnoreCase("Login")) {
                String email = getIntent().getStringExtra("email");
                et_email.setText(email);
                et_email.setVisibility(View.GONE);
                tv_email.setVisibility(View.VISIBLE);
                tv_email.setText(email);

            }
            else if (method.equalsIgnoreCase("Google")) {
                GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
                if (acct != null) {
                    String personName = acct.getDisplayName();
                    String personGivenName = acct.getGivenName();
                    String personEmail = acct.getEmail().trim();
//                Uri personPhoto = acct.getPhotoUrl();
                    personPhoto = Uri.parse(String.valueOf(acct.getPhotoUrl()));
                    if (personName != null) {
                        et_name.setText(personName);
                    } else if (personGivenName != null) {
                        et_name.setText(personGivenName);
                    }
                    if (personEmail != null) {
                        et_email.setText(personEmail);
                        et_email.setVisibility(View.GONE);
                        tv_email.setVisibility(View.VISIBLE);
                        tv_email.setText(personEmail);
                    }
                    if (personPhoto != null) {
                        Log.d("image",personPhoto.toString());
                        Glide.with(this).load(personPhoto).into(img);
                    }


                }

            }
            else if (method.equalsIgnoreCase("Mobile")) {


                String mobile = getIntent().getStringExtra("mobile_no");
                if(mobile == ""){
                    mobile =  user.getPhoneNumber();
                }
                et_mobileno.setText(mobile);
                et_mobileno.setVisibility(View.GONE);
                tv_mobile.setVisibility(View.VISIBLE);
                tv_mobile.setText(mobile);

            }
        }

    }

    private void setDob() {
        final int year = datePicker.getYear();
        final int month = datePicker.getMonth() + 1;
        final int day = datePicker.getDayOfMonth();
        dob = year + "/" + month + "/" + day;
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();

            Glide.with(this).load(mImageUri).into(img);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    //calls on submit button
    private void submission() {
        final String name = et_name.getText().toString().trim();
        final String email = et_email.getText().toString().trim();
        final String mobile = et_mobileno.getText().toString().trim();
        final String city = et_city.getText().toString().trim();
//        if (mImageUri == null&&personPhoto==null) {
//            Toast.makeText(this, "Please choose an image for profile", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if (TextUtils.isEmpty(name)) {
            et_name.setError("Field can not be empty");
            et_name.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            et_email.setError("Field can not be empty");
            et_email.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(mobile)) {
            et_mobileno.setError("Field can not be empty");
            et_mobileno.requestFocus();
            return;
        }
        else if (mobile.length() != 10) {
            et_mobileno.setError("Incorrect mobile no");
            et_mobileno.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(city)) {
            et_city.setError("Field can not be empty");
            et_city.requestFocus();
            return;
        }

        //after validation uploading will start


//        Log.d("image",mImageUri.toString());
//        mUploadTask = fileReference.putFile(mImageUri)                      //Image upload on firebase storage
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                        Log.d("image1",taskSnapshot.toString());
//                        Reg_info_upload info_upload = new Reg_info_upload(city, dob, email, mobile, name, fileReference.getDownloadUrl().toString());
//                        Log.d("image", fileReference.getDownloadUrl().toString());
//                        mDatabaseRef.child(user.getUid()).setValue(info_upload);                    //Data upload on firebase database
//                        Toast.makeText(Registration.this, "Registration Successful", Toast.LENGTH_LONG).show();
//                        Intent intent = new Intent(Registration.this, ProfileQues.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        intent.putExtra("uid",user.getUid());
//                        startActivity(intent);
//                        finish();
//                    }
//                })


        if((personPhoto != null) || (mImageUri == null)){
       //     Log.d("TAG123",personPhoto.toString());
            Reg_info_upload info_upload;
            if(personPhoto==null)
          info_upload = new Reg_info_upload(city, dob, email, mobile, name,null);
            else{
             info_upload = new Reg_info_upload(city, dob, email, mobile, name, personPhoto.toString());

            }
            mDatabaseRef.child(user.getUid()).setValue(info_upload);                    //Data upload on firebase database

            Toast.makeText(Registration.this, "Registration Successful", Toast.LENGTH_LONG).show();
            Log.d("Reg1","from gmail");
            Intent intent = new Intent(Registration.this, ProfileQues.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("uid",user.getUid());
            startActivity(intent);
            finish();
        }
        else {
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));
            fileReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Uri downloadUrl = uri;
                            Log.d("image2", downloadUrl.toString());
                            Toast.makeText(Registration.this,city+dob+email+mobile+name+downloadUrl.toString(), Toast.LENGTH_SHORT).show();
                            Reg_info_upload info_upload = new Reg_info_upload(city, dob, email, mobile, name, downloadUrl.toString());

                            mDatabaseRef.child(user.getUid()).setValue(info_upload);                    //Data upload on firebase database
                            FirebaseDatabase.getInstance().getReference("dob").child(user.getUid()).child("date").setValue(dob);
                            Toast.makeText(Registration.this, "Registration Successful", Toast.LENGTH_LONG).show();
                            Log.d("Reg1"," not gmail ");
                            Intent intent = new Intent(Registration.this, ProfileQues.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("uid", user.getUid());
                            startActivity(intent);
                            finish();


                            //Do what you want with the url
                        }
//             Toast.makeText(Registration.this, e.getMessage(), Toast.LENGTH_SHORT).show();


                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Registration.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    public void logout() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Registration.this,Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
