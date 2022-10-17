package com.example.camerax;


import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.Image;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity{
    private ListenableFuture <ProcessCameraProvider> CameraProviderFuture;
    private PreviewView PreviewView;
    private ExecutorService CameraService;
    private TextView Name,RollNumber,Branch,Degree;
    private QRimageanalysis qrimageanalysis = new QRimageanalysis();



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Name = findViewById(R.id.Name);
        RollNumber = findViewById(R.id.RollNumber);
        Branch = findViewById(R.id.Branch);
        Degree = findViewById(R.id.Degree);

        PreviewView = findViewById(R.id.ScanPreview);
        this.getWindow().setFlags(1024,1024);



        CameraService = Executors.newSingleThreadExecutor();
        CameraProviderFuture = ProcessCameraProvider.getInstance(this);
        CameraProviderFuture.addListener(() ->{
            try {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!= (PackageManager.PERMISSION_GRANTED)){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},101);

                }else {
                ProcessCameraProvider Cameraprovider = CameraProviderFuture.get();
                startCameraX(Cameraprovider);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        },getExecuter());
    }

    private Executor getExecuter() {
        return ContextCompat.getMainExecutor(this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0) {
            ProcessCameraProvider Cameraprovider = null;
            try {
                Cameraprovider = CameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            assert Cameraprovider != null;
            startCameraX(Cameraprovider);
        }
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraprovider) {
        cameraprovider.unbindAll();

        //Cameraselecter use case
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        //Preview usecase
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(PreviewView.getSurfaceProvider());


//imageAnalysis usecase
        ImageAnalysis imageanalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(320,720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER).build();

        imageanalysis.setAnalyzer(getExecuter(),qrimageanalysis);



        cameraprovider.bindToLifecycle(this, cameraSelector, preview,imageanalysis);
    }

    public class QRimageanalysis implements ImageAnalysis.Analyzer{

        @Override
        public void analyze(@NonNull ImageProxy image) {

           ScanImageForBarcodes(image);


        }

        private void ScanImageForBarcodes(ImageProxy image) {
            @SuppressLint("UnsafeOptInUsageError")
            Image image1 = image.getImage();
            if(image1 != null) {
                InputImage inputImage = InputImage.fromMediaImage(image1, image.getImageInfo().getRotationDegrees());

                BarcodeScannerOptions options =
                        new BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(
                                        Barcode.FORMAT_QR_CODE)     // for all formats
                                .build();

                BarcodeScanner scanner = BarcodeScanning.getClient(options);

                Task<List<Barcode>> result = scanner.process(inputImage);
                    result.addOnSuccessListener(this::readerbarcode)                      // if image scanned smoothly
                            .addOnFailureListener(e -> Log.d(TAG, "onFailure: scan failed"))
                            .addOnCompleteListener(task -> image.close());
            }

        }

        private void readerbarcode(List<Barcode> barcodes) {

            for (Barcode barcode: barcodes) {

                Rect bounds = barcode.getBoundingBox();
                Point[] corners = barcode.getCornerPoints();


                String[] rawValue = new String[0];
                try {
                    rawValue = barcode.getRawValue().split(",");
                    Log.d(TAG, "readerbarcode: " + rawValue);

                    Log.d(TAG, "9010: " + rawValue);
                    Name.setText(rawValue[0]);
                    RollNumber.setText(rawValue[3]);
                    Branch.setText(rawValue[2]);
                    Degree.setText(rawValue[1]);
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                    CallBottomsheet(new BottomSheet());

//                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                DatabaseReference myRef = database.getReferenceFromUrl("https://camerax-d6467-default-rtdb.firebaseio.com/");
//                DatabaseReference table = myRef.child("Records");
//                String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
//
//                table.child(rawValue[3] + "/" + currentDateTimeString).setValue("IN");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

            }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }

        private void CallBottomsheet(BottomSheet bottomSheet) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.ScanPreview,bottomSheet);
            fragmentTransaction.commit();
        }
        }

    }

