package com.boardsaver;

import static android.widget.Toast.LENGTH_SHORT;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.opencv.android.OpenCVLoader;


//Mobile2App Application Entry Point (starting activity)
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Log.d("BoardSavr","Running App...");
        Context mainContext = this;

        //check dependencies
        if( OpenCVLoader.initLocal()) {
            Log.d("OpenCV", "OpenCV initialized");
        } else {
            Log.d("OpenCV", "OpenCV failed to initialize");
        }


        //check for permissions (only checks 1 time)
        if (!allPermissionsGranted()) {
            Log.d("BoardSavr","Permissions not granted");
            requestPermissionsFromUser();
        }

        //get login views
        EditText usernameEditText = findViewById(R.id.edit_text_username);
        EditText passwordEditText = findViewById(R.id.edit_text_password);


        //ON 'SIGN IN' CLICK
        Button buttonSignIn = findViewById(R.id.button_sign_in);
        buttonSignIn.setOnClickListener(view -> {
            UserRepository userRepo = new UserRepository(this);

            //get info from views
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            boolean signInSuccess = userRepo.authenticate(username, password);

            Intent intent = new Intent(this, MainActivity.class);
            if(signInSuccess) {
                intent.putExtra("isLoggedIn", true);
                startActivity(intent);
            } else {
                //login failed
                //start AlertDialog pop-up and prompt user to create account or continue without
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Yikes.. No account found");

                //Create Account option
                alertDialog.setPositiveButton(R.string.alert_dialog_positive_text, (dialog, which) -> {
                    try {
                        long userRow = userRepo.registerUser(username, password);
                        if(userRow > 0) {
                            intent.putExtra("isLoggedIn", true);
                            startActivity(intent);
                        } else {
                            Toast newToast = new Toast(mainContext);
                            newToast.setText("Failed to create account");
                            newToast.setDuration(LENGTH_SHORT);
                            newToast.show();
                        }

                    } catch (Exception e) {
                        Log.d("LoginActivity","Error registering user");
                    }
                });

                //Continue without account option
                alertDialog.setNeutralButton(R.string.alert_dialog_neutral_text, (dialog, which) -> {
                    //continue with less privilege
                    //FIXME:: add privilege tracking
                    intent.putExtra("isLoggedIn", false);
                    startActivity(intent);
                });

                //publish dialog to screen
                alertDialog.show();
            }
        });


        //ON 'CREATE ACCOUNT' CLICK
        Button buttonCreateAccount = findViewById(R.id.button_create_account);
        buttonCreateAccount.setOnClickListener(view -> {
            UserRepository userRepo = new UserRepository(this);

            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            long userRow = userRepo.registerUser(username, PasswordHasher.hashPassword(password));

            //if register is successful
            Toast newToast = new Toast(this);
            if (userRow > 0) {
                newToast.setText("Account created successfully");
            } else {
                newToast.setText("Failed to create account");
            }
            newToast.setDuration(LENGTH_SHORT);
            newToast.show();
        });

    }




    // Application specific request code to match with a result reported to
    private static final int REQUEST_CODE_PERMISSIONS = 10;

    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Requests permissions to be granted to this application.
     *
     */
    private void requestPermissionsFromUser() {
        requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    /**
     * Determine whether all the required permission have been granted.
     *
     * @return true if the permissions have been granted, false otherwise.
     */
    private boolean allPermissionsGranted() {
        boolean cameraGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean storageGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return cameraGranted && storageGranted;
    }

}