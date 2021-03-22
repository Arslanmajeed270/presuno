package com.sn.androiddualcameracapture;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.nobrain.android.permissions.AndroidPermissions;
import com.nobrain.android.permissions.Checker;
import com.nobrain.android.permissions.Result;

public class MainActivity extends Activity {

    Button button;
    private static final int REQUEST_CODE = 102;
    public static String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,DualCamActivity.class);
                startActivity(intent);
            }
        });



        setUpActionbar();
        AndroidPermissions.check(this)
                .permissions(Manifest.permission.CAMERA)
                .hasPermissions(new Checker.Action0() {
                    @Override
                    public void call(String[] permissions) {
                        String msg = "Permission has " + permissions[0];
                        Log.d(TAG, msg);
                        Intent intent = new Intent(MainActivity.this,DualCamActivity.class);
                        startActivity(intent);
                        /*Toast.makeText(MainActivity.this,
                                "Yo",
                                Toast.LENGTH_SHORT).show();*/
                    }
                })
                .noPermissions(new Checker.Action1() {
                    @Override
                    public void call(String[] permissions) {
                        String msg = "Permission has no " + permissions[0];
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this,
                                msg,
                                Toast.LENGTH_SHORT).show();

                        ActivityCompat.requestPermissions(MainActivity.this
                                , new String[]{Manifest.permission.CAMERA}
                                , REQUEST_CODE);
                    }
                })
                .check();
    }
    public void onClickFullScreen(View view){
        startActivity(new Intent(this,DualCamActivity.class));
    }

    private void setUpActionbar() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, final String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AndroidPermissions.result(MainActivity.this)
                .addPermissions(REQUEST_CODE, Manifest.permission.CAMERA)
                .putActions(REQUEST_CODE, () -> {

                    Intent intent = new Intent(MainActivity.this,DualCamActivity.class);
                    startActivity(intent);

                    String msg = "Request Success : " + permissions[0];

                   /* Toast.makeText(MainActivity.this,
                            msg,
                            Toast.LENGTH_SHORT).show();*/


                }, new Result.Action1() {
                    @Override
                    public void call(String[] hasPermissions, String[] noPermissions) {
                        String msg = "Request Fail : " + noPermissions[0];
                        /*Toast.makeText(MainActivity.this,
                                msg,
                                Toast.LENGTH_SHORT).show();*/
                        Toast.makeText(MainActivity.this, "Please Allow Camera Permission to Continue", Toast.LENGTH_SHORT).show();

                    }
                })
                .result(requestCode, permissions, grantResults);
    }


}