package com.example.guardapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;


public class FoodFragement extends Fragment {

    private EditText RestaurantName;
    private EditText FoodName;
    private Button Submit;

    FirebaseDatabase database = FirebaseDatabase.getInstance();




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View FoodView =  inflater.inflate(R.layout.fragment_food_fragement, container, false);

        RestaurantName = FoodView.findViewById(R.id.RestaurentName);
        FoodName = FoodView.findViewById(R.id.FoodName);
        Submit = FoodView.findViewById(R.id.Fsubmit);

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String sRestaurantName = RestaurantName.getText().toString();
                String sFoodName = FoodName.getText().toString();
                String DateNow = new Date().toString();

                Restaurant restaurant = new Restaurant(sRestaurantName, sFoodName,DateNow);
                sendtofirebase(restaurant);
            }
        });
        return FoodView;

    }
    public class Restaurant {
        public String RestaurantName;
        public String food;
        public String time;

        public Restaurant() {
            // Default constructor required for calls to DataSnapshot.getValue(Restaurant.class)
        }

        public Restaurant(String name, String food, String time) {
            this.RestaurantName = name;
            this.food = food;
            this.time = time;
        }
    }


    private void sendtofirebase(Restaurant restaurent) {
        TextView RollNumber = getActivity().findViewById(R.id.RollNumber);


        FirebaseDatabase database = FirebaseDatabase.getInstance("https://camerax-d6467-default-rtdb.firebaseio.com/");
        DatabaseReference myRootRef = database.getReference();
        DatabaseReference students = myRootRef.child("Students");
        DatabaseReference student = students.child(RollNumber.getText().toString());
        DatabaseReference food_records = student.child("food_records");
        food_records.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase1", "Error getting data", task.getException());
                }
                else {

                    String count = String.valueOf(task.getResult().getChildrenCount()+1);
//                    Context context = getApplicationContext();
                    DatabaseReference FOOD_record_ID = student.child("food_records").child(count);
                    FOOD_record_ID.setValue(restaurent);
                    getActivity().getSupportFragmentManager().beginTransaction().remove(FoodFragement.this).commit();
                    getActivity().finish();



//                    Toast.makeText(context, "sucess sent to DB",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}