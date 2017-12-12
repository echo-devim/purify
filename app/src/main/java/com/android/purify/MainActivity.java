package com.android.purify;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.purify.util.FileUtil;
import com.android.purify.util.ReplacingInputStream;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;
    private String apkPath;
    private TextView apkName;
    private TextView txtLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apkName = (TextView)findViewById(R.id.txtApkPath);
        txtLog = (TextView)findViewById(R.id.txtLog);
    }

    public void onBtnSelectClick(View v) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        showFileChooser();
    }

    public void onBtnPurifyClick(View v) {
        if ((apkPath == null) || (apkPath.equals(""))) {
            Toast.makeText(this, "Please select an apk first", Toast.LENGTH_SHORT).show();
            return;
        }
        final String outDir = this.getExternalCacheDir().getPath() + "/purifytmp/";
        txtLog.setText("");
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                txtLog.append((String) msg.obj);
                super.handleMessage(msg);
            }
        };
        Purify p = new Purify(apkPath, outDir, handler);
        p.purifyApk();
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select an APK to purify"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d("purify", uri.toString());
                    // Get the path
                    try {
                        apkPath = FileUtil.getPath(this, uri);
                        apkName.setText(apkPath.substring(apkPath.lastIndexOf('/')+1));
                    } catch (URISyntaxException e) {
                        Toast.makeText(this, "Error: invalid path",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}
