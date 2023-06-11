package com.humanebicycle.spirometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.humanebicycle.spirometer.model.User;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private Button googleBtn;

    private GoogleSignInClient mGoogleSignInClient;

    private int RC_SIGN_IN = 101;

    private EditText emailEt;
    private EditText passEt;
    private TextInputLayout emailLayout;
    private TextInputLayout passLayout;
    private Button loginBtn;

    //for referral

    private ConstraintLayout referralLayout;
    private ConstraintLayout mainLayout;

    private Boolean isNewUserRegistered = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressBar = findViewById(R.id.progressBar);

        TextView registerTxt = findViewById(R.id.registerTxt);
        registerTxt.setText(Html.fromHtml("Don't have a account?<b> <font color=#2948FF>Register here</font> </b> "));

        registerTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(LoginActivity.this, RegisterActivity.class), 901);
            }
        });

        setGoogleLoginBtn();
        setGoogleLogin();
        setEmailLogin();

        checkExtras();
    }

    private void checkExtras() {
        if (getIntent().getBooleanExtra("isMsgIncluded", false)) {
            new AlertDialog.Builder(this)
                    .setTitle(getIntent().getStringExtra("title"))
                    .setMessage(getIntent().getStringExtra("message"))
                    .setCancelable(true)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        }
    }


    private void setEmailLogin() {
        emailEt = findViewById(R.id.emailET);
        passEt = findViewById(R.id.passwordET);
        emailLayout = findViewById(R.id.emailLayout);
        passLayout = findViewById(R.id.passwordLayout);
        loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNullValues();
            }
        });
    }

    private void checkNullValues() {
        Boolean isEmpty = false;
        if (passEt.getText() == null || passEt.getText().toString().isEmpty()) {
            isEmpty = true;
            passLayout.setError("Password is required");
        }
        if (emailEt.getText() == null || emailEt.getText().toString().isEmpty()) {
            isEmpty = true;
            emailLayout.setError("Email is required");
        }
        if (!isEmpty) {
            disableEverything();
            startLoginFlow();
        }
    }

    private void disableEverything() {
        loginBtn.setEnabled(false);
        passLayout.setEnabled(false);
        emailLayout.setEnabled(false);
        googleBtn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

    }

    private void enableEverything() {
        loginBtn.setEnabled(true);
        passLayout.setEnabled(true);
        emailLayout.setEnabled(true);
        googleBtn.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }


    private void startLoginFlow() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }

        mAuth.signInWithEmailAndPassword(emailEt.getText().toString(), passEt.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            newUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Auth", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            enableEverything();
                            Log.w("Auth", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed. " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            updateUI(null);
                        }
                    }
                });
    }

    boolean newUser = false;


    private void setGoogleLogin() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void setGoogleLoginBtn() {
        googleBtn = findViewById(R.id.googleBtn);
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                disableEverything();
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        Boolean isGoogleVerified = false;
        Boolean isEmailVerified = false;
        if (user != null) {
            for (UserInfo x : user.getProviderData()) {
                if (x.getProviderId().equals(GoogleAuthProvider.PROVIDER_ID)) {
                    isGoogleVerified = true;
                }
                if (x.getProviderId().equals(EmailAuthProvider.PROVIDER_ID)) {
                    if (user.isEmailVerified()) {
                        isEmailVerified = true;
                    }
                }
            }
            if (isEmailVerified || isGoogleVerified) {
                if ((isNewUserRegistered) || isEmailVerified) {
                    if (newUser) {
//                        launchReferralLayout();
                    } else {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("isNewLogin", true);
                        startActivity(intent);
                        finish();
                    }
                }
            } else {
                mAuth.signOut();
                enableEverything();
                new AlertDialog.Builder(this)
                        .setCancelable(true)
                        .setTitle("Verify your email")
                        .setMessage("In order to prevent spamming bots we have send a link to your email address. You have to click that link in" +
                                " order to verify your account. After verification you will be able to login")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();
            }
        }
    }

    Button continueBtn;
    Button skipBtn;
    EditText phoneEditTxt;


    boolean referralDone = false;

//    private void launchReferralLayout() {
//        mainLayout = findViewById(R.id.mainLayout);
//        referralLayout = findViewById(R.id.referralLayout);
//        mainLayout.setVisibility(View.GONE);
//        progressBar.setVisibility(View.GONE);
//        referralLayout.setVisibility(View.VISIBLE);
//
//        continueBtn = findViewById(R.id.continueBtn);
//        skipBtn = findViewById(R.id.skipBtn);
//        continueBtn.setEnabled(false);
//        referralLayout.setVisibility(View.VISIBLE);
//        skipBtn.setVisibility(View.VISIBLE);
//
//        skipBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                referralLayout.setVisibility(View.GONE);
//                referralDone = true;
//                startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                LoginActivity.this.finish();
//            }
//        });
//
//        phoneEditTxt = findViewById(R.id.editTextPhone);
//
//        phoneEditTxt.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                int charLimit = 6;
//                if (phoneEditTxt.getText().toString().length() != charLimit) {
//                    continueBtn.setEnabled(false);
//                    continueBtn.setBackground(LoginActivity.this.getResources().getDrawable(R.drawable.btn_primary_dis_bg));
//                } else {
//                    continueBtn.setBackground(LoginActivity.this.getResources().getDrawable(R.drawable.btn_primary_bg));
//                    continueBtn.setEnabled(true);
//                }
//            }
//        });
//
//        continueBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (phoneEditTxt.getText() != null && !phoneEditTxt.getText().toString().isEmpty()) {
//                    disableRef();
//                }
//            }
//        });
//    }



    private void enableRef() {
        continueBtn.setEnabled(true);
        skipBtn.setEnabled(true);
        phoneEditTxt.setEnabled(true);
    }


    private void disableRef() {
        progressBar.setVisibility(View.VISIBLE);
        continueBtn.setEnabled(false);
        skipBtn.setEnabled(false);
        phoneEditTxt.setEnabled(false);
    }


    @Override
    protected void onStart() {
        super.onStart();
        updateUI(FirebaseAuth.getInstance().getCurrentUser());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                progressBar.setVisibility(View.GONE);
                enableEverything();
                Toast.makeText(this, "Login with Google failed. " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.w("Google Sign In Result", "Google sign in failed", e);
            }

        }

        if (requestCode == 902 && resultCode == RESULT_OK) {
            isNewUserRegistered = true;
            updateUI(FirebaseAuth.getInstance().getCurrentUser());
        }

        if (requestCode == 901 && resultCode == RESULT_OK && data != null) {
            emailEt.setText(data.getStringExtra("Email"));
            passEt.setText(data.getStringExtra("Pass"));
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkNewUser(task);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Firebase Sign In", "signInWithCredential:failure", task.getException());
                            progressBar.setVisibility(View.GONE);
                            enableEverything();
                            Snackbar.make((ConstraintLayout) findViewById(R.id.mainLayout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void checkNewUser(Task<AuthResult> task) {
        FirebaseUser fuser = task.getResult().getUser();

        if (task.getResult().getAdditionalUserInfo().isNewUser()) {
            newUser = true;
            User user = new User(fuser.getUid(), System.currentTimeMillis());
            user.setUid(task.getResult().getUser().getUid());
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            if (u.getEmail() != null)
                user.setEmail(u.getEmail());
            if (u.getPhotoUrl() != null)
                user.setName(u.getDisplayName());
            if (u.getDisplayName() != null)
                user.setPhotoUrl(u.getPhotoUrl().toString());

            String androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            user.setDeviceId(androidId);

            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(fuser.getUid())
                    .set(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                isNewUserRegistered = true;
                                updateUI(fuser);
                            } else {
                                Toast.makeText(LoginActivity.this, "Some error occurred! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                enableEverything();
                            }
                        }
                    });
        } else {
            isNewUserRegistered = true;
            updateUI(fuser);
        }
    }


}