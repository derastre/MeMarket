package com.me_market.android.memarket;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.me_market.android.memarket.components.BaseActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;

import static com.me_market.android.memarket.SplashActivity.USER_ID;

public class BarcodeReader extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "BarcodeReader";

    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String PRODUCT_ID = "ProductId";
    public static final String PRODUCT_BARCODE = "ProductBarcode";

    private String mUserId;
    private SurfaceView cameraView;
    private TextView barcodeInfo;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private FloatingActionButton enter_code_fab;
    private boolean autoFocus;
    private boolean useFlash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_reader);

        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        barcodeInfo = (TextView) findViewById(R.id.code_info);
        enter_code_fab = (FloatingActionButton) findViewById(R.id.enter_code_button);
        enter_code_fab.setOnClickListener(this);

        // read parameters from the intent used to launch the activity.
        autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
        useFlash = getIntent().getBooleanExtra(UseFlash, false);
        mUserId = getIntent().getStringExtra(USER_ID);


        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            accessCamera();
        } else {
            requestCameraPermission();
        }
    }

    private void accessCamera() {
        createCameraSource(autoFocus, useFlash);
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                startCameraSource();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {

                    barcodeInfo.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {
                            barcodeInfo.setText(    // Update the TextView
                                    barcodes.valueAt(0).displayValue
                            );
                        }
                    });

                    readProductFromFirebase(barcodes.valueAt(0).rawValue);

                }
            }
        });
    }

    private void startProductActivity(String id, String code) {
        startActivity(new Intent(this, ProductActivity.class)
                .putExtra(PRODUCT_ID, id)
                .putExtra(PRODUCT_BARCODE, code)
                .putExtra(USER_ID, mUserId));
    }

    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();
        barcodeDetector = new BarcodeDetector.Builder(context).setBarcodeFormats(Barcode.ALL_FORMATS).build();
        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }


        // Creates and starts the camera.
        cameraSource = new CameraSource
                .Builder(context, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(640, 480)
                .setRequestedFps(15.0f)
                .build();
    }

    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (cameraSource != null) {
            try {
                cameraSource.start(cameraView.getHolder());
            } catch (IOException ie) {
                Log.e("CAMERA SOURCE", ie.getMessage());
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{android.Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            this.recreate();
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions, RC_HANDLE_CAMERA_PERM);
            }
        };

        findViewById(R.id.camera_view).setOnClickListener(listener);
        Snackbar.make(cameraView, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    private void enterCodeManually() {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.barcode_manual_entry_button);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String code = input.getText().toString();
                readProductFromFirebase(code);
            }
        });
        builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog = builder.create();
        dialog.show();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public void readProductFromFirebase(final String code) {

        FirebaseDatabase mDatabase;
        DatabaseReference myRef;
        ValueEventListener myProductListener;

        showProgressDialog(getString(R.string.loading));

        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference().child("products_keys").child(code);

        myProductListener = new ValueEventListener() {
            @Override

            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<String> productNameList = new ArrayList<>();
                ArrayList<String> productKeyArrayList = new ArrayList<>();
                String id, name, key;
                if (dataSnapshot.getChildrenCount() == 1) {
                    for (DataSnapshot productSnapshop : dataSnapshot.getChildren()) {
                        id = productSnapshop.getKey();
                        startProductActivity(id, code);
                    }
                } else if (dataSnapshot.getChildrenCount() == 0) {
                    //if mProduct code doesn't exist in database go to activity add new mProduct
                    startActivity(new Intent(BarcodeReader.this, NewProduct.class).putExtra(PRODUCT_BARCODE, code).putExtra(USER_ID, mUserId));
                } else if (dataSnapshot.getChildrenCount() > 1) {
                    for (DataSnapshot productSnapshop : dataSnapshot.getChildren()) {
                        if (productSnapshop.getValue()!=null) {
                            name = productSnapshop.getValue().toString();
                            key = productSnapshop.getKey();
                            productNameList.add(name);
                            productKeyArrayList.add(key);
                        }
                    }
                    selectProductDialog(productNameList, productKeyArrayList, code);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
                Snackbar.make(findViewById(R.id.camera_view), databaseError.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        };

        myRef.addListenerForSingleValueEvent(myProductListener);

    }

    private void selectProductDialog(final ArrayList<String> productNameList, final ArrayList<String> productKeyArrayList, final String code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_product);
        builder.setMessage(R.string.select_product_instructions);


        CharSequence[] cs = productNameList.toArray(new CharSequence[productNameList.size()]);
        builder.setItems(cs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String id = productKeyArrayList.get(i);
                startProductActivity(id, code);
            }
        });
        builder.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        enter_code_fab.hide();
    }

    @Override
    public void onResume() {
        super.onResume();
        enter_code_fab.show();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.enter_code_button) {
            enterCodeManually();
        }
    }
}

