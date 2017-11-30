package com.me_market.android.memarket.components;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Arturo Deras on 23/9/2017.
 * BaseActivity
 * Includes progress dialog
 */

public class BaseActivity extends AppCompatActivity {

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    public static class CircleTransform extends BitmapTransformation {
        public CircleTransform(Context context) {
            super(context);
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return circleCrop(pool, toTransform);
        }

        private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
            if (source == null) return null;

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override
        public String getId() {
            return getClass().getName();
        }
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static ArrayList readObjectsFromFile(String filename, Context context) throws IOException, ClassNotFoundException {
        ArrayList objects = new ArrayList();
        FileInputStream is = null;
        try {
            is = context.openFileInput(filename);
            ObjectInputStream ois = new ObjectInputStream(is);
            while (true) {
                try {
                    Object object = ois.readObject();
                    objects.add(object);
                } catch (EOFException ex) {
                    break;
                }
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return objects;
    }

    public static void writeObjectsToFile(String filename, ArrayList objects, Context context) throws IOException {
        FileOutputStream os = null;

        try {
            os = context.openFileOutput(filename, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(os);
            for (Object object : objects) {
                oos.writeObject(object);
            }
            oos.flush();
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    public static void emptyObjectFile(String filename, Context context) throws IOException {
        FileOutputStream os = null;
        try {
            os = context.openFileOutput(filename, Context.MODE_PRIVATE);
        } finally {
            if (os != null) {
                os.close();
            }
        }
    }

    public static void deleteObjectFile(String filename) {
        File f = new File(filename);
        if (f.delete()) {
            System.out.println(filename + " deleted sucessfully...");
        } else {
            System.out.println(filename + " deletion failed!");
        }
    }
}
