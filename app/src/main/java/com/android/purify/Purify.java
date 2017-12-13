package com.android.purify;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.purify.util.FileUtil;
import com.android.purify.util.ReplacingInputStream;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Purify {

    private String outDir;
    private String apkPath;
    private Handler output;

    public Purify(String apkPath, String outDir, Handler output) {
        this.apkPath = apkPath;
        this.outDir = outDir;
        this.output = output;
    }

    private void print(String text) {
        Message msg = this.output.obtainMessage();
        msg.obj = text;
        this.output.sendMessage(msg);
    }

    public void purifyApk() {
        final File out = new File(outDir);
        out.mkdirs();
        File apk = new File(apkPath);
        if (apk.exists()) {
            print("Please wait...\n");
            print("Extracting APK...");
            Thread worker = new Thread(){
                @Override
                public void run() {
                    //Step 1 - Unzip the apk
                    if (FileUtil.unpackZip(outDir, apkPath)) {
                        print("OK\n");
                    } else {
                        print("FAILED\n");
                        return;
                    }
                    //Step 2 - Edit classes.dex to remove ads
                    print("Removing ads urls...");
                    try {
                        removeAds(outDir + "classes.dex");
                        print("OK\n");
                    } catch (Exception e) {
                        print("FAILED\n");
                    }
                    //Step 3 - Create the apk (i.e. a jar archive)
                    //Remove previous signatures [disabled]
                    //FileUtil.deleteRecursive(new File(outDir + "/META-INF"));
                    print("Creating new apk...");
                    String newApkPath = apkPath+"-purified.apk";
                    if (createNewApk(outDir, newApkPath)) {
                        print("OK\n");
                    } else {
                        print("FAILED\n");
                        return;
                    }
                    //TODO: Step 4 - Sign the app [external, not implemented here]
                    //Clear temporary files
                    FileUtil.deleteRecursive(out);
                    print("Finish! The APK is purified!\nSign and reinstall it.\n");
                    print("New APK exported into:\n" + newApkPath);
                }
            };
            worker.start();
        } else {
            print("Invalid apk:\n" + apkPath);
        }
    }

    private void removeAds(String path) throws IOException {
        String[] domains = {"googleads.g.doubleclick.net", "mobileads.google.com"};
        Log.d("purify","Removing ads from "+path);
        for (String domain : domains) {
            File file = new File(path);
            byte[] bytearr = FileUtil.file2bytearray(file);
            ByteArrayInputStream bis = new ByteArrayInputStream(bytearr);
            String replacementString = "";
            for (int i = 0; i < domain.length(); i++) replacementString += "a";
            byte[] search = domain.getBytes();
            byte[] replacement = replacementString.getBytes();
            InputStream ris = new ReplacingInputStream(bis, search, replacement);
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));

            int b;
            while (-1 != (b = ris.read()))
                bos.write(b);

            bos.close();

        }
    }

    /* An apk is just a jar file */
    private boolean createNewApk(String source, String destination) {
        return FileUtil.createJarFile(source, destination);
    }
}
