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


        //check for permissions
        if (!allPermissionsGranted()) {
            Log.d("BoardSavr","Permissions not granted");
            requestPermissionsFromUser();
        }



        //get views
        EditText usernameEditText = findViewById(R.id.edit_text_username);
        EditText passwordEditText = findViewById(R.id.edit_text_password);



        //FIXME:: CLEAN UP ONCLICK LISTENER BY MOVING TO SEPARATE FUNCTION
        //ON 'SIGN IN' CLICK
        Button buttonSignIn = findViewById(R.id.button_sign_in);
        buttonSignIn.setOnClickListener(view -> {
            UserRepository userRepo = new UserRepository(this);

            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            String passwordHash = PasswordHasher.hashPassword(password);

            boolean success = userRepo.authenticate(username, passwordHash);

            Intent intent = new Intent(this, MainActivity.class);
            if(success) {
                intent.putExtra("isLoggedIn", true);
                startActivity(intent);
            } else {
                //login failed
                //start AlertDialog pop-up and prompt user to create account or continue without
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Yikes.. No account found");

                alertDialog.setPositiveButton(R.string.alert_dialog_positive_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            long userRow = userRepo.registerUser(username, passwordHash);
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
                    }
                });
                alertDialog.setNeutralButton(R.string.alert_dialog_neutral_text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //continue with less privilege
                        //FIXME:: add privilege tracking
                        intent.putExtra("isLoggedIn", false);
                        startActivity(intent);
                    }
                });

                //publish dialog to screen
                alertDialog.show();

            }
        });



        //FIXME:: CLEAN UP ONCLICK LISTENER BY MOVING TO SEPARATE FUNCTION
        //ON 'CREATE ACCOUNT' CLICK
        Button buttonCreateAccount = findViewById(R.id.button_create_account);
        buttonCreateAccount.setOnClickListener(view -> {
            UserRepository userRepo = new UserRepository(this);

            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            long userRow = userRepo.registerUser(username, PasswordHasher.hashPassword(password));
            if(userRow > 0) {
                Toast newToast = new Toast(this);
                newToast.setText("Account created successfully");
                newToast.setDuration(LENGTH_SHORT);
                newToast.show();
            } else {
                Toast newToast = new Toast(this);
                newToast.setText("Failed to create account");
                newToast.setDuration(LENGTH_SHORT);
                newToast.show();
            }
        });

    }


    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private void requestPermissionsFromUser() {
        requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }


    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

    }

}