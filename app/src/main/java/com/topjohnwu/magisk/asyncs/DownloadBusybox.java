package com.topjohnwu.magisk.asyncs;

import android.content.Context;
import android.os.Build;

import com.topjohnwu.magisk.MagiskManager;
import com.topjohnwu.magisk.utils.Shell;
import com.topjohnwu.magisk.utils.Utils;
import com.topjohnwu.magisk.utils.WebService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

public class DownloadBusybox extends ParallelTask<Void, Void, Void> {

    private static final String BUSYBOX_ARM = "https://github.com/topjohnwu/ndk-busybox/releases/download/1.27.2/busybox-arm";
    private static final String BUSYBOX_X86 = "https://github.com/topjohnwu/ndk-busybox/releases/download/1.27.2/busybox-x86";

    private File busybox;

    @Override
    protected Void doInBackground(Void... voids) {
        Context context = MagiskManager.get();
        busybox = new File(context.getCacheDir(), "busybox");
        Utils.removeItem(context.getApplicationInfo().dataDir + "/busybox");
        try {
            FileOutputStream out  = new FileOutputStream(busybox);
            HttpURLConnection conn = WebService.request(
                    Build.SUPPORTED_32_BIT_ABIS[0].contains("x86") ?
                            BUSYBOX_X86 :
                            BUSYBOX_ARM,
                    null
            );
            if (conn == null) throw new IOException();
            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            byte[] buffer = new byte[4096];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.close();
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (busybox.exists()) {
            Shell.su(
                    "rm -rf " + MagiskManager.BUSYBOXPATH,
                    "mkdir -p " + MagiskManager.BUSYBOXPATH,
                    "cp " + busybox + " " + MagiskManager.BUSYBOXPATH,
                    "chmod -R 755 " + MagiskManager.BUSYBOXPATH,
                    MagiskManager.BUSYBOXPATH + "/busybox --install -s " + MagiskManager.BUSYBOXPATH
            );
            busybox.delete();
        }
        return null;
    }
}
