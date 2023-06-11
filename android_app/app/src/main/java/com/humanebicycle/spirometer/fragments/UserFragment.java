package com.humanebicycle.spirometer.fragments;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.humanebicycle.spirometer.LoginActivity;
import com.humanebicycle.spirometer.R;
import com.humanebicycle.spirometer.helper.XStreamSerializer;
import com.humanebicycle.spirometer.livedata.UserData;
import com.humanebicycle.spirometer.model.SpirometerTest;
import com.humanebicycle.spirometer.model.User;
import com.humanebicycle.spirometer.tools.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserFragment extends Fragment {

    public UserFragment() {
        // Required empty public constructor
    }

    public static UserFragment newInstance() {
        UserFragment fragment = new UserFragment();
        return fragment;
    }

    private TextView nameTxt;
    private TextView phoneTxt;
    private TextView signUpTxt;
    private LinearLayout userInfoLayout;
    private TextView editProfileTxt;
    private ImageView profileImg;
    MaterialButton backupTestsButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_user, container, false);
        findViews(v);
        setUpViews();


        return v;
    }


    private void findViews(View view) {
        MaterialButton logoutBtn = view.findViewById(R.id.logOutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmLogout();
            }
        });

        userInfoLayout = view.findViewById(R.id.userInfoLayout);
        nameTxt = view.findViewById(R.id.nameTxt);
        phoneTxt = view.findViewById(R.id.phoneNumTxt);
        signUpTxt = view.findViewById(R.id.signUpTxt);
        editProfileTxt = view.findViewById(R.id.editProfileTxt);
        profileImg = view.findViewById(R.id.displayImg);

        backupTestsButton = view.findViewById(R.id.backup_tests);
        backupTestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backupTestsButton.setEnabled(false);
                Snackbar snackbar = Snackbar.make(view.findViewById(R.id.scroll_view_user_frag),getString(R.string.backing_up),Snackbar.LENGTH_LONG);
                snackbar.show();

                FirebaseFirestore.getInstance().collection("Tests").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    String serialisedTestString = task.getResult().toObject(String.class);
                                    List<SpirometerTest> tests;
                                    if(serialisedTestString.equals("") || serialisedTestString.isEmpty() || serialisedTestString==null){
                                        tests = new ArrayList<>();
                                    }else{
                                        try {
                                            tests = (List<SpirometerTest>) XStreamSerializer.getInstance().deSerialize(serialisedTestString);
                                            List<SpirometerTest> currentTests = XStreamSerializer.getInstance().getPreviousTests(getContext());
                                            for(int i =0;i<tests.size();i++){
                                                int j;
                                                SpirometerTest testNotBackedUp;
                                                for(j=0;j<currentTests.size();j++){
                                                    if(tests.get(i).getId()==currentTests.get(j).getId()){
                                                        break;
                                                    }
                                                }
                                                if(j==currentTests.size()-1){
                                                    tests.add(testNotBackedUp);
                                                }
                                            }
                                        }catch (Exception e){
                                            Log.e("abh", "onComplete: Something wrong with string conversion!"+e.toString());
                                        }
                                    }


                                }else{
                                    Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }

    private void confirmLogout() {
        String message = "Are you sure you want to logout?";
        if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
            message = message + "\nWARNING: You are logged in as anonymous user. You will loose all your progress once you logout.";
        }
        new AlertDialog.Builder(getActivity())
                .setTitle("Log out")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        logout();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();

    }

    private void setUpViews() {
        if (FirebaseAuth.getInstance().getCurrentUser().isAnonymous()){
            editProfileTxt.setVisibility(View.GONE);
            phoneTxt.setVisibility(View.GONE);
            userInfoLayout.setOnClickListener(view -> login());
        } else {
            signUpTxt.setVisibility(View.GONE);
            if (FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null && !FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString().isEmpty())
                Picasso.get()
                        .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl())
                        .transform(new CircleTransform())
                        .into(profileImg);
            nameTxt.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            phoneTxt.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }
    }

    private void login() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.putExtra("convertToPermanent", true);
        startActivity(intent);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }
}