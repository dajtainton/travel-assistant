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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.IOException;
import java.util.Locale;

import assistant.travel.com.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.internal.Utils;

public class ProfileActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.img_profile)
    ImageView mProfileImage;
    @BindView(R.id.fab)
    FloatingActionButton mProfileImageFab;

    @BindView(R.id.layout_firstname)
    LinearLayout mLayoutFName;
    @BindView(R.id.layout_lastname)
    LinearLayout mLayoutLName;

    @BindView(R.id.text_firstname)
    TextView mFirstname;
    @BindView(R.id.text_lastname)
    TextView mLastname;

    private FirebaseAuth mAuth;
    private Uri mPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        loadUser();
        setOnclickListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                    startActivityForResult(intent, 0);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);


        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {

                    try {

                        String[] projection = {
                                MediaStore.MediaColumns._ID,
                                MediaStore.Images.ImageColumns.ORIENTATION,
                                MediaStore.Images.Media.DATA
                        };
                        Cursor c = getContentResolver().query(mPhotoUri, projection, null, null, null);
                        c.moveToFirst();
                        String photoFileName = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                        Bitmap photo = BitmapFactory.decodeFile(photoFileName);
                        ///Utils.saveToInternalStorage(getApplicationContext(), photo, "profile");
                        mProfileImage.setImageBitmap(photo);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    /*Bitmap photo = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    Utils.saveToInternalStorage(getApplicationContext(), photo, "profile");
                    mProfileImage.setImageBitmap(photo);*/
                }

                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    try {
                        Uri selectedImage = imageReturnedIntent.getData();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        //Utils.saveToInternalStorage(getApplicationContext(), bitmap, "profile");
                        mProfileImage.setImageURI(selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void setOnclickListeners() {

        mProfileImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Profile Image");

                builder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        // The request code used in ActivityCompat.requestPermissions()
                        // and returned in the Activity's onRequestPermissionsResult()
                        int PERMISSION_ALL = 3;
                        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

                        if (!hasPermissions(ProfileActivity.this, PERMISSIONS)) {
                            ActivityCompat.requestPermissions(ProfileActivity.this, PERMISSIONS, PERMISSION_ALL);
                        } else {

                            mPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);

                            startActivityForResult(intent, 0);
                        }

                    }
                });

                builder.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, 1);//one can be replaced with any action code
                    }
                });

                builder.show();
            }
        });

        mLayoutFName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Firstname");

                View viewInflated = LayoutInflater.from(ProfileActivity.this)
                        .inflate(R.layout.dialog_edit_text,
                                (ViewGroup) findViewById(android.R.id.content), false);

                // Set up the input
                final EditText inputText = viewInflated.findViewById(R.id.input_text);
                inputText.setHint("Enter Firstname");

                // Specify the type of input expected
                builder.setView(viewInflated);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @SuppressLint("StaticFieldLeak")
                    public void onClick(DialogInterface dialog, int whichButton) {

                        FirebaseUser user = mAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(inputText.getText().toString() + " " + mLastname.getText().toString()).build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    loadUser();
                                }
                            }
                        });


                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Dismiss dialog
                    }
                });

                builder.show();

            }
        });

        mLayoutLName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Lastname");

                View viewInflated = LayoutInflater.from(ProfileActivity.this)
                        .inflate(R.layout.dialog_edit_text,
                                (ViewGroup) findViewById(android.R.id.content), false);

                // Set up the input
                final EditText inputText = viewInflated.findViewById(R.id.input_text);
                inputText.setHint("Enter Lastname");

                // Specify the type of input expected
                builder.setView(viewInflated);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @SuppressLint("StaticFieldLeak")
                    public void onClick(DialogInterface dialog, int whichButton) {


                        FirebaseUser user = mAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(mFirstname.getText().toString() + " " + inputText.getText().toString()).build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    loadUser();
                                }
                            }
                        });

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Dismiss dialog
                    }
                });

                builder.show();
            }
        });


    }

    private String[] splitDisplayName(String displayName) {

        return displayName.split("\\s+");
    }

    private void loadUser() {
        String[] nameArray = splitDisplayName(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        mFirstname.setText(nameArray[0]);
        mLastname.setText(nameArray[nameArray.length - 1]);

    }

}
