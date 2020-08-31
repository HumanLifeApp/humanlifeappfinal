package com.humanlife.humanlifeapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private CoordinatorLayout coordinatorLayout;
    boolean doubleBackToExitPressedOnce = false;
    boolean flagOnBack = true;
    private static final String TAG = "tag" ;
    String uid;
    private DatabaseReference mDatabaseRef;

   BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    GoogleSignInClient mGoogleSignInClient;

    //Drawer Menu
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    //ImageView menuIcon,home_btn,profile_btn,help_btn;

    ImageView profile;
    ImageView userPic;
    TextView userName;
    TextView userEmail;
    public int count = 0;

    private final static int FCR = 1;
    private WebView webView;
    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private ValueCallback<Uri> mUploadMessage;
    private Uri mCapturedImageURI = null;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private static final int FILECHOOSER_RESULTCODE = 1;
    public static final int REQUEST_SELECT_FILE = 100;
    public ValueCallback<Uri[]> uploadMessage;

    String historyUrl="";
    String idToken;


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = null;
            // Check that the response is a good one 
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo 
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                }

                else {
                    String dataString = data.getDataString();

                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        }
        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == this.mUploadMessage) {
                    return;
                }
                Uri result = null;
                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    }
                    else {
                        // retrieve from the private variable if the intent is null 
                        result = data == null ? mCapturedImageURI : data.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "activity :" + e,
                            Toast.LENGTH_LONG).show();
                }
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
        return;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_home);
        mAuth = FirebaseAuth.getInstance();
        coordinatorLayout = findViewById(R.id.coordinator);

        webView = findViewById(R.id.home_web);

        bottomNavigationView = findViewById(R.id.bottom_nav);
       bottomNavigationView.setOnNavigationItemSelectedListener(bottomnavmethod);
       bottomNavigationView.setSelectedItemId(R.id.home_btn);
//menu = findViewById(R.id.main_menu)
//       MenuItem notification = menu.findViewById(R.id.nav_notification);

        mAuth.getCurrentUser().getIdToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                             idToken = task.getResult().getToken();
                            Log.d("TOKEN123",idToken);
                        } else {
                            Toast.makeText(Home.this, (CharSequence) task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        //Menu Hooks
       drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);



        final View headerView = navigationView.getHeaderView(0);
        userName = headerView.findViewById(R.id.user_name);
        userEmail = headerView.findViewById(R.id.user_email);
        userPic = headerView.findViewById(R.id.user_photo);

        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

       // menuHeader();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);




        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        mDatabaseRef.child(user.getUid()).child("notificationTokens").child(token).setValue(true);
                        Log.d(TAG, msg);
                       // Toast.makeText(Home.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        DatabaseReference login = mDatabaseRef.child(user.getUid()); //here user will sign in using his name so it is based on user, directory will change dynamically
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Reg_info_upload users = dataSnapshot.getValue(Reg_info_upload.class);

                Uri photoURL= Uri.parse(String.valueOf(users.getPhotoURL()));

                userName.setText(users.getName());
                userEmail.setText(users.getEmail());
                Glide.with(headerView).load(photoURL).placeholder(R.drawable.ic_baseline_account_circle_24).into(userPic);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        login.addValueEventListener(listener);



        uid = user.getUid();
        Log.d("ASD1",uid);

        //   }
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
        }
        assert webView != null;

        WebSettings webSettings = webView.getSettings();
        webView.setWebViewClient(new WebViewClient());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings ().setUseWideViewPort (true);
        webView.getSettings ().setLoadWithOverviewMode (true);
        webView.getSettings().setBuiltInZoomControls(false);

//        WebBackForwardList mWebBackForwardList = webView.copyBackForwardList();
//        historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex()).getUrl();

        Log.d("geturl",historyUrl);

        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.setMixedContentMode(0);
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        } else if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else if (Build.VERSION.SDK_INT < 19) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        }
//        webView.setWebViewClient(new WebViewClient());
        webView.setWebViewClient(new Callback());

     //   For url checking
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //   super.shouldOverrideUrlLoading(view, url);
             //   Log.d("profile","current 1 URL = " + url);
                switch (url) {
                    case "https://maininfo-a3b3f.web.app/viewprofile.html":
                        bottomNavigationView.getMenu().getItem(1).setChecked(true);

                        break;
                    case "https://maininfo-a3b3f.web.app/help2.html":
                        bottomNavigationView.getMenu().getItem(2).setChecked(true);
                        break;

                    case "https://maininfo-a3b3f.web.app/mentalhelp.html?All":
                        bottomNavigationView.getMenu().getItem(2).setChecked(true);
                        break;

                    case "https://maininfo-a3b3f.web.app/financialhelp.html?All":
                        bottomNavigationView.getMenu().getItem(2).setChecked(true);
                        break;

                    case "https://maininfo-a3b3f.web.app/homepagenew.html":
                        bottomNavigationView.getMenu().getItem(0).setChecked(true);
                        break;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d("profile","current 2 URL = " + url);



            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

//                if(url.equals("https://humanlife-8b7e1.web.app/iframe.html"))
//                {
//                    Log.d("url",url);
//                    // Log.d(LOGTAG, "GO TO OPENTOK!!!");
//                    startActivity(new Intent(ProfileQues.this,Home.class));
//                }


            }



        });

        String check=getIntent().getStringExtra("check");
        if(check!=null && check.equals("co2pageopen")){
            webView.loadUrl("https://maininfo-a3b3f.web.app/co2landpage.html?"+uid);
        }else {
            webView.loadUrl("https://maininfo-a3b3f.web.app/homepagenew.html?" + uid);
        }


        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);

        webView.setWebChromeClient(new WebChromeClient() {




            private File createImageFile() throws IOException {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                File imageFile = File.createTempFile(
                        imageFileName,
                        /* prefix */
                        ".jpg",
                        /* suffix */
                        storageDir     /* directory */
                );
                return imageFile;
            }
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePath, WebChromeClient.FileChooserParams fileChooserParams) {
                // Double check that we don't have any existing callbacks 
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue (null);
                }

                mFilePathCallback = filePath;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go 
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();

                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        // Error occurred while creating the File 
                        Log.e(TAG, "Unable to create Image File", ex);
                    }
                    // Continue only if the File was successfully created 
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("image/*");
                Intent[] intentArray;
                if (takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }
                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
                return true;
            }
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                // Create AndroidExampleFolder at sdcard 
                // Create AndroidExampleFolder at sdcard 
                File imageStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES)
                        , "AndroidExampleFolder");
                if (!imageStorageDir.exists()) {
                    // Create AndroidExampleFolder at sdcard 
                    imageStorageDir.mkdirs();
                }
                // Create camera captured image file path and name 
                File file = new File(
                        imageStorageDir + File.separator + "IMG_"
                                + String.valueOf(System.currentTimeMillis())
                                + ".jpg");
                mCapturedImageURI = Uri.fromFile(file);
                // Camera capture image intent 
                final Intent captureIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                // Create file chooser intent 
                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                // Set camera intent to file chooser 
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                        , new Parcelable[] { captureIntent });
                // On select image call onActivityResult method of activity 
                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);        }
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType,
                                        String capture) {
                openFileChooser(uploadMsg, acceptType);
            }
        });


      //  menuHeader();

userNotification();

    }


    public void userNotification(){
        // Request Notification
        DatabaseReference noti = FirebaseDatabase.getInstance().getReference("notifications");
        DatabaseReference noti2 = FirebaseDatabase.getInstance().getReference("wallethistory");
        DatabaseReference noti3 = FirebaseDatabase.getInstance().getReference("SupportPointHistory");
        DatabaseReference noti4 = FirebaseDatabase.getInstance().getReference("support/" + uid + "/volunteer/rating");
        DatabaseReference noti5 = FirebaseDatabase.getInstance().getReference("support/" + uid + "/mental/rating");
        DatabaseReference noti6 = FirebaseDatabase.getInstance().getReference("users/" + uid + "/rating");
        DatabaseReference noti7 = FirebaseDatabase.getInstance().getReference("friend_list");
        DatabaseReference noti8 = FirebaseDatabase.getInstance().getReference("mental_friend_list");
        DatabaseReference noti9 = FirebaseDatabase.getInstance().getReference("financial_friend_list");

        noti.orderByChild("SendTo").equalTo(user.getUid()).addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    Notification_status users = dataSnapshot.getValue(Notification_status.class);
//                    assert users != null;
                    String data = dataSnapshot.child("status").getValue().toString();
                  //  Log.d("TAG!", data);
                    if (data.equals("Pending")) {
                        Log.d("RES1", data+"FR");
                        count++;
                    }
                    if(count>0){
                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.ic_baseline_notifications_active_24);
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("New Notifications");
                        Log.d("RES1",count+"count");
                        count=0;
                    }
                    else{
                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.bell_icon);
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("Notification");
                        Log.d("RES1","something2");
                        count=0;

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        noti2.orderByChild("SendTo").equalTo(user.getUid()).addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
//                    Notification_status users = dataSnapshot.getValue(Notification_status.class);
//                    assert users != null;
                    String data = dataSnapshot.child("status").getValue().toString();
                    if (data.equals("Unread")) {
                        Log.d("RES1", data+"wallet");
                        count++;
                    }
                    if(count>0){
                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.ic_baseline_notifications_active_24);
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("New Notifications");
                        Log.d("RES1",count+"count");
                        count=0;
                    }
                    else{
                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.bell_icon);
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("Notification");
                        Log.d("RES1","something2");
                        count=0;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Home.this, "Error", Toast.LENGTH_SHORT).show();

            }
        });

        noti3.orderByChild("SendTo").equalTo(user.getUid()).addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
//                    Notification_status users = dataSnapshot.getValue(Notification_status.class);
//                    assert users != null;
                    String data = dataSnapshot.child("status").getValue().toString();
                    if (data.equals("Unread")) {
                        Log.d("RES1", data+"Swallet");
                        count++;
                    }
                    if(count>0){
                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.ic_baseline_notifications_active_24);
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("New Notifications");
                        Log.d("RES1",count+"count");
                        count=0;
                    }
                    else{
                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.bell_icon);
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("Notification");
                        Log.d("RES1","something2");
                        count=0;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        noti4.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
//                    Notification_status users = dataSnapshot.getValue(Notification_status.class);
//                    assert users != null;
                    String data = dataSnapshot.child("status").getValue().toString();
                    if (data.equals("Unread")) {
                        Log.d("RES1", data+"SVR");
                        count++;
                    }
                    if(count>0){
                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.ic_baseline_notifications_active_24);
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("New Notifications");
                        Log.d("RES1",count+"count");
                        count=0;
                    }
                    else{
                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.bell_icon);
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("Notification");
                        Log.d("RES1","something2");
                        count=0;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        noti5.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
//                    Notification_status users = dataSnapshot.getValue(Notification_status.class);
//                    assert users != null;
                    String data = dataSnapshot.child("status").getValue().toString();
                    if (data.equals("Unread")) {
                        Log.d("RES1", data+"SMR");
                        count++;
                    }
                    if(count>0){
                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.ic_baseline_notifications_active_24);
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("New Notifications");
                        Log.d("RES1",count+"count");
                        count=0;
                    }
                    else{
                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.bell_icon);
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("Notification");
                        Log.d("RES1","something2");
                        count=0;
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        noti6.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
//                    Notification_status users = dataSnapshot.getValue(Notification_status.class);
//                    assert users != null;
                    String data = dataSnapshot.child("status").getValue().toString();
                    if(data.equals("Unread")) {
                        Log.d("RES1", data+"R");
                        count++;
                    }
                    if(count>0){
                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.ic_baseline_notifications_active_24);
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("New Notifications");
                        Log.d("RES1",count+"count");
                        count=0;
                    }
                    else{
                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.bell_icon);
                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("Notification");
                        Log.d("RES1","something2");
                        count=0;
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        noti7.addValueEventListener(new ValueEventListener(){
            String chatKey;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
//                    Notification_status users = dataSnapshot.getValue(Notification_status.class);
//                    assert users != null;
                    // String data = dataSnapshot.getValue().toString();

                    String friendid= dataSnapshot.child("friendId").getValue().toString();
                    String userid= dataSnapshot.child("userId").getValue().toString();

                    if(friendid.equals(uid) | userid.equals(uid)){
                         chatKey=dataSnapshot.getKey();
                    }

                    FirebaseDatabase.getInstance().getReference("chatMessages/"+chatKey).
                            addValueEventListener(new ValueEventListener(){


                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
                            for(DataSnapshot dataSnapshot1: snapshot1.getChildren()){
                                String status= dataSnapshot1.child("status").getValue().toString();
                                String userID= dataSnapshot1.child("userId").getValue().toString();


                                if(status.equals("Unread")){
                                    Log.d("RES1", status+"Msg");
                                    Log.d("RES1", userID+" Msg");
                                    Log.d("RES1", chatKey+" Msgkey");
                                    count++;
                                }
                                if(count>0){
                                    navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.ic_baseline_notifications_active_24);
                                    navigationView.getMenu().findItem(R.id.nav_notification).setTitle("New Notifications");
                                    Log.d("RES1",count+"count");
                                    count=0;
                                }
                                else{
                                    navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.bell_icon);
                                    navigationView.getMenu().findItem(R.id.nav_notification).setTitle("Notification");
                                    Log.d("RES1","something2");
                                    count=0;
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        noti8.addValueEventListener(new ValueEventListener(){
String chatKey;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
//                    Notification_status users = dataSnapshot.getValue(Notification_status.class);
//                    assert users != null;
                    // String data = dataSnapshot.getValue().toString();
                    String friendid= dataSnapshot.child("friendId").getValue().toString();
                    String userid= dataSnapshot.child("userId").getValue().toString();

                    if(friendid.equals(uid) | userid.equals(uid)){
                        chatKey=dataSnapshot.getKey();
                    }

                    FirebaseDatabase.getInstance().getReference("mental_chatMessages/"+chatKey).addValueEventListener(new ValueEventListener(){

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                                String status= dataSnapshot.child("status").getValue().toString();
                                String userID= dataSnapshot.child("userId").getValue().toString();
                                String origin= dataSnapshot.child("origin").getValue().toString();

                                if(status.equals("Unread") && !userID.equals(uid)){

                                    if(origin.equals("Help")||origin.equals("Support")){
                                        Log.d("RES1", status+"MMsg");
                                        count++;
                                    }
                                    if(count>0){
                                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.ic_baseline_notifications_active_24);
                                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("New Notifications");
                                        Log.d("RES1",count+"count");
                                        count=0;
                                    }
                                    else{
                                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.bell_icon);
                                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("Notification");
                                        Log.d("RES1","something2");
                                        count=0;
                                    }
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        noti9.addValueEventListener(new ValueEventListener(){
            String chatKey;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
//                    Notification_status users = dataSnapshot.getValue(Notification_status.class);
//                    assert users != null;
                    // String data = dataSnapshot.getValue().toString();

                    String friendid= dataSnapshot.child("friendId").getValue().toString();
                    String userid= dataSnapshot.child("userId").getValue().toString();

                    if(friendid.equals(uid) | userid.equals(uid)){
                        chatKey=dataSnapshot.getKey();
                    }

                    FirebaseDatabase.getInstance().getReference("financial_chatMessages/"+chatKey).addValueEventListener(new ValueEventListener(){

                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String status = dataSnapshot.child("status").getValue().toString();
                                String userID = dataSnapshot.child("userId").getValue().toString();
                                String origin = dataSnapshot.child("origin").getValue().toString();

                                if (status.equals("Unread") && !userID.equals(uid)) {

                                    if (origin.equals("Help") || origin.equals("Support")) {
                                        Log.d("RES1", status+"VMsg");
                                        count++;
                                    }

                                    if(count>0){
                                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.ic_baseline_notifications_active_24);
                                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("New Notifications");
                                        Log.d("RES1",count+"count");
                                        count=0;
                                    }
                                    else{
                                        navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.bell_icon);
                                        navigationView.getMenu().findItem(R.id.nav_notification).setTitle("Notification");
                                        Log.d("RES1","something2");
                                        count=0;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        if (count == 0){
            navigationView.getMenu().findItem(R.id.nav_notification).setIcon(R.drawable.bell_icon);
            navigationView.getMenu().findItem(R.id.nav_notification).setTitle("Notification");
            Log.d("RES1","something2");

        }


    }

    public void profile(View view) {

        webView.loadUrl("https://maininfo-a3b3f.web.app/viewprofile.html?"+uid);
        bottomNavigationView.getMenu().getItem(1).setChecked(true);
        drawerLayout.closeDrawer(GravityCompat.START);
    }

    public class Callback extends WebViewClient {
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getApplicationContext(), "Failed loading app!", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);

        }
        else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            if(webView.canGoBack()){

                //webView.goBack();
                Log.d("geturl", webView.getUrl());
               // String url = webView.getUrl();

               webView.goBack();
               tabChange();
            }
            else{
                Snackbar.make(coordinatorLayout,"Double back to exit",Snackbar.LENGTH_SHORT).show();
            }

            this.doubleBackToExitPressedOnce = true;
            if(flagOnBack) {
                flagOnBack = false;
                Toast.makeText(this, "Double click back to exit", Toast.LENGTH_SHORT).show();
            }
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 300);
        }


    }


    public void tabChange(){
        Log.d("geturl", webView.getOriginalUrl());
        if (webView.getOriginalUrl().equals("https://maininfo-a3b3f.web.app/homepagenew.html?"+uid)){
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
            //  webView.goBack();
        }
        else if (webView.getOriginalUrl().equals("https://maininfo-a3b3f.web.app/help2.html?"+uid)){
            bottomNavigationView.getMenu().getItem(2).setChecked(true);
            //  webView.goBack();
        }
        else if (webView.getOriginalUrl().equals("https://maininfo-a3b3f.web.app/viewprofile.html?"+uid)){
            bottomNavigationView.getMenu().getItem(1).setChecked(true);
            //  webView.goBack();
        }
        else if (webView.getOriginalUrl().equals("https://maininfo-a3b3f.web.app/mentalhelp.html?All")){
            bottomNavigationView.getMenu().getItem(2).setChecked(true);
            //  webView.goBack();
        }
        else if (webView.getOriginalUrl().equals("https://maininfo-a3b3f.web.app/financialhelp.html?All")){
            bottomNavigationView.getMenu().getItem(2).setChecked(true);
            //   webView.goBack();
        }
        else if (webView.getOriginalUrl().equals("https://maininfo-a3b3f.web.app/homepagenew.html")){
            bottomNavigationView.getMenu().getItem(0).setChecked(true);
            //   webView.goBack();
        }
        else if (webView.getOriginalUrl().equals("https://maininfo-a3b3f.web.app/viewprofile.html")){
            bottomNavigationView.getMenu().getItem(1).setChecked(true);
            //   webView.goBack();
        }
    }


    //BOTTOM NAVIGATION
    private BottomNavigationView.OnNavigationItemSelectedListener bottomnavmethod =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.home_btn:
                            webView.loadUrl("https://maininfo-a3b3f.web.app/homepagenew.html?"+uid);
                           // userNotification();
                            break;
                        case R.id.profile_btn:
                            webView.loadUrl("https://maininfo-a3b3f.web.app/viewprofile.html?"+uid);
                            break;
                        case R.id.help_btn:
                            webView.loadUrl("https://maininfo-a3b3f.web.app/help2.html?"+uid);
                            break;
                        case R.id.menu_icon:
                            if (drawerLayout.isDrawerVisible(GravityCompat.START)){
                                drawerLayout.closeDrawer(GravityCompat.START);
                               // userNotification();

                            }
                            else {

                                drawerLayout.openDrawer(GravityCompat.START);
                            }
                            break;
                    }
                    return true;
                }
            };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.nav_logout:
                drawerLayout.closeDrawer(GravityCompat.START);
                logout();
                break;

            case R.id.nav_connect:
                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                webView.loadUrl("https://maininfo-a3b3f.web.app/chat.html?"+uid);
                drawerLayout.closeDrawer(GravityCompat.START);
            //    startActivity(new Intent(Home.this,Connect.class));
                break;

//            case R.id.nav_chatBot:
//                bottomNavigationView.getMenu().getItem(3).setChecked(true);
//                webView.loadUrl("https://maininfo-a3b3f.web.app/chat-bubble-master/chatbot/newchatasst.html");
//                drawerLayout.closeDrawer(GravityCompat.START);
//            //    startActivity(new Intent(Home.this,Connect.class));
//
//                break;

            case R.id.nav_support:
                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                webView.loadUrl("https://maininfo-a3b3f.web.app/support.html?"+uid);
                drawerLayout.closeDrawer(GravityCompat.START);
                // startActivity(new Intent(Home.this,Login.class));
                break;

            case R.id.nav_the_earth:
                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                webView.loadUrl("https://maininfo-a3b3f.web.app/theearth.html?"+uid);
                drawerLayout.closeDrawer(GravityCompat.START);
                // startActivity(new Intent(Home.this,Login.class));
                break;

            case R.id.nav_report:
                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                webView.loadUrl("https://maininfo-a3b3f.web.app/contact.html?"+uid);
                drawerLayout.closeDrawer(GravityCompat.START);
                //  startActivity(new Intent(Home.this,Login.class));
                break;

            case R.id.nav_wallet:
                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                webView.loadUrl("https://maininfo-a3b3f.web.app/wallet.html?"+uid);
                drawerLayout.closeDrawer(GravityCompat.START);
                // startActivity(new Intent(Home.this,Login.class));
                break;



            case R.id.nav_notification:
                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                webView.loadUrl("https://maininfo-a3b3f.web.app/notification.html?"+uid);
                drawerLayout.closeDrawer(GravityCompat.START);
                // startActivity(new Intent(Home.this,Login.class));
                break;

            case R.id.nav_share:
                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                drawerLayout.closeDrawer(GravityCompat.START);
                shareApp("http://play.google.com/store/apps/details?id=" + this.getPackageName(), this);
                // startActivity(new Intent(Home.this,Login.class));
                break;

            case R.id.nav_rate_us:
                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + this.getPackageName())));
                //  startActivity(new Intent(Home.this,Login.class));
                break;

            default:
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
        }


        return true;
    }


    public static void shareApp(String content, Context context) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Download the HumanLife App ");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
        context.startActivity(Intent.createChooser(sharingIntent, ""));
    }

    public void logout() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();

        Intent intent = new Intent(Home.this,Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Toast.makeText(Home.this, "Logged out", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }




}
