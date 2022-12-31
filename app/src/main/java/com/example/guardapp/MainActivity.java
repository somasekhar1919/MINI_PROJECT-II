package com.example.guardapp;


import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.concurrent.ExecutorService;

public class MainActivity extends AppCompatActivity {
    private ListenableFuture<ProcessCameraProvider> CameraProviderFuture;
    private PreviewView PreviewView;
    private ExecutorService CameraService;
    TextView Name, RollNumber, Branch, Degree,Mobile;


    private Button scan_button,outing_button,food_button;

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://camerax-d6467-default-rtdb.firebaseio.com/");

    private DatabaseReference myRootRef = database.getReference();



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
        RollNumber = findViewById(R.id.RollNumber);
        Name = findViewById(R.id.Name);
        Mobile = findViewById(R.id.Mobile);





        scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallBottomsheet(new BottomSheet());


            }
        });






        String currentDateTimeString = java.text.DateFormat.getDateTimeInstance().format(new Date());

        outing_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference students = myRootRef.child("Students");
                DatabaseReference student = students.child(RollNumber.getText().toString());
                DatabaseReference outing_records = myRootRef.child("records");
                student.child("outing_records").orderByKey().limitToLast(1).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {

                        String check = task.getResult().getValue()!=null?String.valueOf(task.getResult().getValue()):"{0=current_status=Inside}";

                        if(check.equals("[null, current_status=OutSide]")){
                            check ="{1=current_status=OutSide}";
                        }
                        Log.d(TAG, "checkforpending:"+ check);
                        Log.d(TAG, "checkforpending:"+ task.getResult().getValue());

                        if(check.substring(check.length()-8,check.length()-1).equals("OutSide")){

                            String finalCheck = check;
                            outing_records.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (!task.isSuccessful()) {
                                        Log.e("firebase1", "Error getting data", task.getException());

                                    }
                                    else {

                                        String count= finalCheck.substring(1, finalCheck.length() - 24);
                                        Context context = getApplicationContext();
                                        DatabaseReference OUT_record_ID = student.child("outing_records").child(count);
                                        String DateNow = new Date().toString();
                                        OUT_record_ID.setValue("current_status=Inside");
//
//
                                        Toast.makeText(context, "sucess sent to DB",Toast.LENGTH_SHORT).show();
                                        outing_records.child(count).child("ID").setValue(count);
                                        outing_records.child(count).child("IN").setValue(DateNow);
                                        finish();
                                    }
                                }
                            });


                        }else{
                            outing_records.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (!task.isSuccessful()) {
                                        Log.e("firebase1", "Error getting data", task.getException());
                                    }
                                    else {

                                        String count = String.valueOf(task.getResult().getChildrenCount()+1);
                                        Context context = getApplicationContext();
                                        DatabaseReference OUT_record_ID = student.child("outing_records").child(count);
                                        String DateNow = new Date().toString();
                                        OUT_record_ID.setValue("current_status=OutSide");


                                        outing_records.child(count).child("ID").setValue(count);
                                        outing_records.child(count).child("OUT").setValue(DateNow);
                                        outing_records.child(count).child("rollnumber").setValue(RollNumber.getText().toString());
                                        outing_records.child(count).child("name").setValue(Name.getText().toString());
                                        outing_records.child(count).child("rollnumber").setValue(RollNumber.getText().toString());
                                        outing_records.child(count).child("mobile").setValue(Mobile.getText().toString());

                                        Toast.makeText(context, "sucess sent to DB",Toast.LENGTH_SHORT).show();

                                        finish();
                                    }
                                }
                            });

                        }

                    }
                });
            }
        });
        food_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallFoodFragment(new FoodFragement());

            }
        });

    }

    private void CallFoodFragment(FoodFragement foodFragement) {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view,FoodFragement.class, null)
//                .addToBackStack(null)
                .commit();
        food_button.setVisibility(View.GONE);
        outing_button.setVisibility(View.GONE);

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