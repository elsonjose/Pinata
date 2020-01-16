package com.simple.pinata;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;


public class MainActivity extends AppCompatActivity {



    private Button AddBdayBtn,ViewBdayBtn;
    private RelativeLayout Root;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Root = findViewById(R.id.main_root);
        AddBdayBtn = findViewById(R.id.main_add_bday_btn);
        ViewBdayBtn = findViewById(R.id.main_view_bday_btn);

        AddBdayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Snackbar.make(Root,"Go online for seamless device accesses.",Snackbar.LENGTH_SHORT).show();

            }
        });

        ViewBdayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Snackbar.make(Root,"Go online for seamless device accesses.",Snackbar.LENGTH_SHORT).show();

            }
        });

    }




}
