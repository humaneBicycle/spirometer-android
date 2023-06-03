package com.humanebicycle.spirometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.humanebicycle.spirometer.fragments.AnalyticsFragment;
import com.humanebicycle.spirometer.fragments.ListFragment;
import com.humanebicycle.spirometer.fragments.TestFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navbar);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, TestFragment.newInstance()).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.list_nav_button){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, ListFragment.newInstance()).commit();
                }else if(item.getItemId()==R.id.home_nav_button){
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, TestFragment.newInstance()).commit();
                }else{
                    Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
}