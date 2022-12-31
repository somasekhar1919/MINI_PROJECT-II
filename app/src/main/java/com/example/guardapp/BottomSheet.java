package com.example.guardapp;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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


public class BottomSheet extends BottomSheetDialogFragment {


    private androidx.camera.view.PreviewView PreviewView;
    private ListenableFuture<ProcessCameraProvider> CameraProviderFuture;
    private final ImageAnalysis.Analyzer qrimageanalysis = new QRimageanalysis();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false);

        PreviewView = view.findViewById(R.id.ScanPreview);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        ExecutorService cameraService = Executors.newSingleThreadExecutor();
        CameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        CameraProviderFuture.addListener(() -> {
            try {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != (PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(requireActivity(),new String[]{Manifest.permission.CAMERA}, 101);

                } else {
                    ProcessCameraProvider Cameraprovider = CameraProviderFuture.get();
                    startCameraX(Cameraprovider);

                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecuter());
    }

    private Executor getExecuter () {
            return ContextCompat.getMainExecutor(requireContext());
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

        @SuppressLint("SetTextI18n")
        private void readerbarcode(List<Barcode> barcodes) {
            String testtext = "koooo";

            for (Barcode barcode: barcodes) {
                Log.d(TAG, "01:loop " );

                Rect bounds = barcode.getBoundingBox();
                Point[] corners = barcode.getCornerPoints();
                
                String[] rawValue ;
                try {

                    rawValue = barcode.getRawValue().split(",");
                    Log.d(TAG, "readerbarcode: " + rawValue);



//                    viewmodel = new ViewModelProvider(requireActivity()).get(ItemViewModel.class);
//                    viewmodel.setData(testtext);

                    Log.d(TAG, "901: " + rawValue[7]);

                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                    TextView Name       = getActivity().findViewById(R.id.Name);
                    TextView RollNumber = getActivity().findViewById(R.id.RollNumber);
                    TextView Branch     = getActivity().findViewById(R.id.Branch);
                    TextView Degree     = getActivity().findViewById(R.id.Degree);
                    TextView Mobile     = getActivity().findViewById(R.id.Mobile);

                    Button food = getActivity().findViewById(R.id.food_button);

                    Name.setVisibility(View.VISIBLE);
                    RollNumber.setVisibility(View.VISIBLE);
                    Branch.setVisibility(View.VISIBLE);
                    Degree.setVisibility(View.VISIBLE);
                    Mobile.setVisibility(View.VISIBLE);
                    Name.setText(rawValue[0]);
                    RollNumber.setText(rawValue[3]);
                    Branch.setText(rawValue[2]);
                    Degree.setText(rawValue[1]);
                    Mobile.setText(rawValue[7]);

                    LinearLayout Action = getActivity().findViewById(R.id.action);
                    Button outing_button = getActivity().findViewById(R.id.outing_button);

                    FirebaseDatabase database = FirebaseDatabase.getInstance("https://camerax-d6467-default-rtdb.firebaseio.com/");
                    DatabaseReference myRootRef = database.getReference();

                    DatabaseReference students = myRootRef.child("Students");
                    DatabaseReference student = students.child(RollNumber.getText().toString());
                    student.child("outing_records").orderByKey().limitToLast(1).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            String check = task.getResult().getValue() != null ? String.valueOf(task.getResult().getValue()) : "{0=current_status=Inside}";

                            if (check.substring(check.length() - 8, check.length() - 1).equals("OutSide")) {
                                outing_button.setText("Update Entry");
                                food.setVisibility(View.GONE);

                            }
                        }
                    });


                    Action.setVisibility(View.VISIBLE);
                    getActivity().getSupportFragmentManager().beginTransaction().remove(BottomSheet.this).commit();

                    break;
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }



}