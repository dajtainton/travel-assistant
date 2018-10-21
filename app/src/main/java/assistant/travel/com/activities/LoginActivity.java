/*
 * Copyright 2018 David Tainton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package assistant.travel.com.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import assistant.travel.com.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Handles user login
 */

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_SIGNUP = 0;
    private ProgressDialog mProgressDialog;

    @BindView(R.id.input_username)
    EditText mUsername;
    @BindView(R.id.input_password)
    EditText mPassword;
    @BindView(R.id.btn_login)
    Button mLoginButton;
    @BindView(R.id.link_forgot_password)
    TextView mForgotPasswordLink;
    @BindView(R.id.link_signup)
    TextView mSignupLink;
    @BindView(R.id.text_error)
    TextView mErrorText;

    private FirebaseAuth mAuth;


    // region AndroidLifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ui components
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        mProgressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
        mProgressDialog.setIndeterminate(true);

        mSignupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        mForgotPasswordLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    // endregion

    @SuppressLint("StaticFieldLeak")
    public void handleLogin() {
        if (validate()) {

            mAuth.signInWithEmailAndPassword(mUsername.getText().toString(), mPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }

                        }
                    });

        }
    }

    public void updateUI(FirebaseUser user) {

        if (user != null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            mErrorText.setText("Login Error!");
            mErrorText.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }


    public boolean validate() {
        boolean valid = true;

        String email = mUsername.getText().toString();
        String password = mPassword.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mUsername.setError("enter a valid email address");
            valid = false;
        } else {
            mUsername.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }

    private void resetPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("To reset your password, please enter your email address to be sent a link where you can reset it.");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password, (ViewGroup) findViewById(android.R.id.content), false);
        // Set up the input
        final EditText inputEmail = viewInflated.findViewById(R.id.input_email_address);

        // Specify the type of input expected
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String emailAddress = inputEmail.getText().toString();
                if (emailAddress.contains("@")) {

                    dialog.dismiss();

                    mProgressDialog.setMessage("Sending email...");
                    mProgressDialog.show();

                    FirebaseAuth.getInstance().sendPasswordResetEmail(inputEmail.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mProgressDialog.dismiss();

                                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                        builder.setTitle("Reset Password");
                                        builder.setMessage("Email sent please go your inbox to reset password.");
                                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                // Dismiss dialog
                                            }
                                        });
                                    }
                                }
                            });

                } else {
                    inputEmail.setError("Please enter a valid email address");
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}

