package com.humanebicycle.spirometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.humanebicycle.spirometer.fragments.ListFragment;
import com.humanebicycle.spirometer.fragments.TestFragment;
import com.humanebicycle.spirometer.fragments.UserFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        Log.d("abh", "onCreate: "+FirebaseAuth.getInstance().getCurrentUser().getEmail());

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navbar);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, TestFragment.newInstance()).commit();
        bottomNavigationView.setSelectedItemId(R.id.home_nav_button);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.list_nav_button:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, ListFragment.newInstance()).commit();
                        bottomNavigationView.getMenu().getItem(1).setChecked(true);
                        break;
                    case R.id.home_nav_button:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, TestFragment.newInstance()).commit();
                        bottomNavigationView.getMenu().getItem(0).setChecked(false);
                        break;
                    case R.id.profile_nav_button:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, UserFragment.newInstance()).commit();
                        bottomNavigationView.getMenu().getItem(2).setChecked(false);
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
    }
}