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
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import assistant.travel.com.R;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Handles user signup
 */

public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.input_firstname)
    EditText mFirstname;
    @BindView(R.id.input_lastname)
    EditText mLastname;
    @BindView(R.id.input_email)
    EditText mEmail;
    @BindView(R.id.input_password)
    EditText mPassword;
    @BindView(R.id.input_re_enter_password)
    EditText mReEnterPassword;
    @BindView(R.id.btn_signup)
    Button mSignupButton;
    @BindView(R.id.link_login)
    TextView mLoginLink;

    private ProgressDialog mProgressDialog;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

        mSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup(v);
            }
        });

        mLoginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Finish the registration screen and return to the login activity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

        mProgressDialog = new ProgressDialog(SignupActivity.this,
                R.style.AppTheme_Dark_Dialog);
        mProgressDialog.setIndeterminate(true);
    }

    @SuppressLint("StaticFieldLeak")
    public void signup(final View view) {

        if (validate()) {

            mProgressDialog.setMessage("Setting up account...");
            mProgressDialog.show();

            mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            mProgressDialog.dismiss();

                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(mFirstname.getText().toString() + " " + mLastname.getText().toString()).build();

                                user.updateProfile(profileUpdates);

                                updateUI(user, view);
                            } else {
                                // If sign in fails, display a message to the user
                                updateUI(null, view);
                            }

                        }
                    });

        }
    }

    public void updateUI(FirebaseUser user, View view) {

        if(user != null) {
            mSignupButton.setEnabled(true);
            setResult(RESULT_OK, null);
            Snackbar.make(view, "Registration successful", Snackbar.LENGTH_LONG).show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        } else {
            Snackbar.make(view, "Registration failed please try again", Snackbar.LENGTH_LONG).show();
            mSignupButton.setEnabled(true);
        }

    }

    public boolean validate() {
        boolean valid = true;

        String firstname = mFirstname.getText().toString();
        String lastname = mLastname.getText().toString();
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        String reEnterPassword = mReEnterPassword.getText().toString();

        if (firstname.isEmpty() || firstname.length() < 3) {
            mFirstname.setError("at least 3 characters");
            valid = false;
        } else {
            mFirstname.setError(null);
        }

        if (lastname.isEmpty() || lastname.length() < 3) {
            mLastname.setError("at least 3 characters");
            valid = false;
        } else {
            mLastname.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("enter a valid email address");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            mPassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        if (!(reEnterPassword.equals(password))) {
            mReEnterPassword.setError("passwords do not match");
            valid = false;
        } else {
            mReEnterPassword.setError(null);
        }

        return valid;
    }

}
