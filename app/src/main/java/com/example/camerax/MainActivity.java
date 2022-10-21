package com.example.camerax;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.concurrent.ExecutorService;

public class MainActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> CameraProviderFuture;
    private PreviewView PreviewView;
    private ExecutorService CameraService;
    private TextView Name, RollNumber, Branch, Degree;

    private Button scan_button,outing_button,food_button;
    private ItemViewModel viewmodel;

    public MainActivity(){
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scan_button = findViewById(R.id.scan_button);
        outing_button = findViewById(R.id.outing_button);
        food_button = findViewById(R.id.food_button);


        scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallBottomsheet(new BottomSheet());

            }
        });
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReferenceFromUrl("https://camerax-d6467-default-rtdb.firebaseio.com/");
        DatabaseReference table = myRef.child("Records");
        String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());
        outing_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        food_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });





    }

    private void CallBottomsheet(BottomSheet bottomSheet) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view,BottomSheet.class, null)
//                .addToBackStack(null)
                .commit();
        scan_button.setVisibility(View.GONE);

    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container_view);
        if(fragment!=null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
            scan_button.setVisibility(View.VISIBLE);

        }else{
                super.onBackPressed();

            }
        }
}