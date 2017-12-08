package com.jason.supervise;

import android.app.Application;
import android.content.Context;
import android.support.annotation.RawRes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by Jason on 2017/12/8.
 */

public class TApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initSuperVise();
    }

    private void initSuperVise() {
        final String renderConfig = readRawAsString(this, R.raw.render_config);
        SuperviseConfiguration configuration =
                new SuperviseConfiguration.Builder(this).
                        samplingRate(5000).
                        renderConfig(renderConfig).build();
        SuperVise.getInstance().init(configuration);
        SuperVise.getInstance().start();
    }


    public static String readRawAsString(Context context, @RawRes int id) {
        if (context == null) return null;
        try {
            InputStream stream = context.getResources().openRawResource(id);
            return readAsString(stream, "utf-8");
        } catch (Exception e) {
            return null;
        }
    }

    public static String readAsString(InputStream is, String encode) {
        if (is != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, encode));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                is.close();
                return sb.toString();
            } catch (UnsupportedEncodingException e) {
            } catch (IOException e) {
            }
        }
        return "";
    }
}
