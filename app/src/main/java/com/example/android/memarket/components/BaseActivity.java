package com.example.android.memarket.components;

import android.app.ProgressDialog;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;

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
}
