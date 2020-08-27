package com.myfav.rider.ui.home.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.myfav.rider.R;
import com.myfav.rider.interfaces.ScannerResultHandler;
import com.myfav.rider.interfaces.ScannerStatusListener;
import com.myfav.rider.interfaces.UserActionHandler;
import com.myfav.rider.ui.home.data.PacketInfo;
import com.myfav.rider.utils.UserAlertClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.myfav.rider.ui.home.HomeViewModel.ACTION_RESUME_TRIP;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Scanner extends Fragment implements View.OnClickListener {

    //UI
    TextureView textureView;
    FloatingActionButton captureBt;
    TextView statusTv;

    //Data
    private UserAlertClient userAlertClient;
    private ArrayList<String> barcodeList;
    int cameraFacing;
    final int CAMERA_REQUEST_CODE = 10;
    String cameraId;
    Size previewSize;

    //Scanner and Camera
    TextureView.SurfaceTextureListener surfaceTextureListener;
    CameraManager cameraManager;
    HandlerThread backgroundThread;
    Handler backgroundHandler;
    CameraDevice cameraDevice;
    CameraCaptureSession cameraCaptureSession;
    CaptureRequest.Builder captureRequestBuilder;
    CaptureRequest captureRequest;


    private final int camera_open = 1;
    private final int camera_closed = 2;
    private final int camera_intermediate = 0;
    private final int capture_mode_one_shot = 10;
    private final int capture_mode_continuous = 20;
    private int captureMode = -1;
    private int cameraStatus = camera_closed;
    private boolean isProcessingFrame = false;

    //DATA
    private static Scanner mThis;
    private static boolean newInstance = false;
    private Timer timer;
    private boolean isTimerActive = false;
    private ScannerResultHandler mScannerResultHandler;
    private ScannerStatusListener listener;
    private final String tag = "_scanner";
    public static Scanner getInstance() {
        if (mThis == null) {
            mThis = new Scanner();
            newInstance = true;
        } else {
            newInstance = false;
        }
        return mThis;
    }

    public Scanner() {

    }

    public void setScannerResultHandler(ScannerResultHandler handler) {
        mScannerResultHandler = handler;
    }

    public void setScannerStatusListener(ScannerStatusListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //UI
        View view = inflater.inflate(R.layout.camera_preview, container, false);
        textureView = view.findViewById(R.id.texture_view);
        statusTv = view.findViewById(R.id.status);
        captureBt = view.findViewById(R.id.scan);
        barcodeList = new ArrayList<>();
        mScannerResultHandler.doReset();

        //Initialize
        userAlertClient = new UserAlertClient(getActivity());
        cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        cameraFacing = CameraCharacteristics.LENS_FACING_BACK;

        //Listeners
        captureBt.setOnClickListener(this);
        surfaceTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        };

        //Flow
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        openCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        closeCamera();
        stopBarcodeScan();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan:
                if(isTimerActive)
                    stopBarcodeScan();
                else
                    startBarcodeScan();
        }
    }

    private void openCamera() {
        cameraStatus = camera_intermediate;

        backgroundThread = new HandlerThread("camera_background_thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        if (textureView.isAvailable()) {
            try {
                for (String cameraId : cameraManager.getCameraIdList()) {
                    CameraCharacteristics cameraCharacteristics =
                            cameraManager.getCameraCharacteristics(cameraId);
                    if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                            cameraFacing) {
                        StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(
                                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                        previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                        this.cameraId = cameraId;
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            try {
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    private void closeCamera() {
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }

        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }

        cameraStatus = camera_closed;
    }

    private void setCaptureMode(int mode) {
        if (mode == capture_mode_one_shot) {
            try {
                cameraCaptureSession.capture(captureRequestBuilder.build(),
                        null, backgroundHandler);
                captureMode = mode;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else if (mode == capture_mode_continuous) {
            try {
                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(),
                        null, backgroundHandler);
                captureMode = mode;
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }


    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Scanner.this.cameraDevice = cameraDevice;
            createPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            Scanner.this.cameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            Scanner.this.cameraDevice = null;
        }
    };

    private void createPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (cameraDevice == null) {
                                return;
                            }

                            captureRequest = captureRequestBuilder.build();
                            Scanner.this.cameraCaptureSession = cameraCaptureSession;
                            setCaptureMode(capture_mode_continuous);
                            cameraStatus = camera_open;
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }


    private void startBarcodeScan() {
        isTimerActive = true;
        listener.onStatusChanged(true);
        statusTv.setText("Status: Scanning...");
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
                                      public void run() {
                                          if(!isProcessingFrame)
                                              processFrame();
                                          else
                                              Log.i(tag,"Waiting...");
                                              //statusTv.setText("Waiting...");
                                      }
                                  }, 10,
                500);

    }

    private void stopBarcodeScan() {
        isProcessingFrame = false;
        isTimerActive = false;
        listener.onStatusChanged(false);
        statusTv.setText("Status: Paused.");
        if(timer != null)
        timer.cancel();
    }


    private void processFrame() {
        isProcessingFrame = true;
        setCaptureMode(capture_mode_one_shot);
        Bitmap bitmap = textureView.getBitmap();//Bitmap.createScaledBitmap(textureView.getBitmap(), 120, 120, false);
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        BarcodeScanner scanner = BarcodeScanning.getClient();
        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        if (barcodes.size() > 0) {
                            Barcode barcode = barcodes.get(0);
                            handleBarcode(barcode.getRawValue());
                        }

//                        for (Barcode barcode: barcodes) {
//                            Log.i("scanForBarcode", barcode.getDisplayValue());
//                        }

//                        Rect bounds = barcode.getBoundingBox();
//                        Point[] corners = barcode.getCornerPoints();
//                        String rawValue = barcode.getRawValue();
//                        int valueType = barcode.getValueType();

//                        // See API reference for complete list of supported types
//                        switch (valueType) {
//                            case Barcode.TYPE_WIFI:
//                                String ssid = barcode.getWifi().getSsid();
//                                String password = barcode.getWifi().getPassword();
//                                int type = barcode.getWifi().getEncryptionType();
//                                break;
//                            case Barcode.TYPE_URL:
//                                String title = barcode.getUrl().getTitle();
//                                String url = barcode.getUrl().getUrl();
//                                break;
//                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        isProcessingFrame = false;
                        setCaptureMode(capture_mode_continuous);
                    }
                });

    }

    private void handleBarcode(String code) {
        if (!barcodeList.contains(code)) {
            userAlertClient.showDialogMessage("New Packet", "Packet with id: " + code + " scanned.", new UserActionHandler() {
                @Override
                public void onUserAction() {
                    barcodeList.add(code);
                    mScannerResultHandler.onScan(code);
                    stopBarcodeScan();
                }
            });
        } else {
            userAlertClient.showDialogMessage("Duplicate Packet", "Packet with id: " + code + "already scanned.", new UserActionHandler() {
                @Override
                public void onUserAction() {
                    isProcessingFrame = false;
                }
            });
        }
    }

    private void initScanner() {
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(
                                Barcode.FORMAT_QR_CODE,
                                Barcode.FORMAT_AZTEC)
                        .build();
    }
}
