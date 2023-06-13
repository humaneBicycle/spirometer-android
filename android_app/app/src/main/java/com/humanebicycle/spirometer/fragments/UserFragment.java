package com.humanebicycle.spirometer.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.humanebicycle.spirometer.LoginActivity;
import com.humanebicycle.spirometer.R;
import com.humanebicycle.spirometer.data.XStreamSerializer;
import com.humanebicycle.spirometer.model.SpirometerTest;
import com.humanebicycle.spirometer.tools.CircleTransform;
import com.humanebicycle.spirometer.utils.CSVUtil;
import com.humanebicycle.spirometer.utils.FileUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
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
    private ImageView profileImg;
    MaterialButton backupTestsButton, exportAllTestsButton;

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
        profileImg = view.findViewById(R.id.displayImg);
        exportAllTestsButton = view.findViewById(R.id.export_all_tests);

        backupTestsButton = view.findViewById(R.id.backup_tests);
        backupTestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backupTestsButton.setEnabled(false);
                Snackbar snackbar = Snackbar.make(view,"currently not working!",Snackbar.LENGTH_LONG);
                snackbar.show();

//                FirebaseFirestore.getInstance().collection("Tests").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                if(task.isSuccessful()){
//                                    String serialisedTestString = task.getResult().toObject(String.class);
//                                    List<SpirometerTest> tests;
//                                    tests = (List<SpirometerTest>) XStreamSerializer.getInstance().deSerialize(serialisedTestString);
//
//                                    try {
//                                        List<SpirometerTest> currentTests = XStreamSerializer.getInstance().getPreviousTests();
//                                        List<SpirometerTest> listToUpload = generateFinalList(tests,currentTests);
//                                        String strToUpload = XStreamSerializer.getInstance().serialize(listToUpload);
//                                        FirebaseFirestore.getInstance().collection("Tests").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                                                    .set(strToUpload).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                        @Override
//                                                        public void onComplete(@NonNull Task<Void> task) {
//                                                            if(task.isSuccessful()){
//                                                                Snackbar snackbar = Snackbar.make(view.findViewById(R.id.scroll_view_user_frag),getString(R.string.sync_complete),Snackbar.LENGTH_LONG);
//                                                                snackbar.show();
//                                                            }else{
//                                                                Snackbar snackbar = Snackbar.make(view.findViewById(R.id.scroll_view_user_frag),getString(R.string.something_went_wrong),Snackbar.LENGTH_LONG);
//                                                                snackbar.show();
//                                                            }
//                                                        }
//                                                    });
//
//                                    }catch (Exception e){
//                                            Log.e("abh", "onComplete: Something wrong with string conversion!"+e.toString());
//                                    }
//
//
//
//                                }else{
//                                    Toast.makeText(getContext(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
            }
        });

        exportAllTestsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String exportedFileName = getString(R.string.tests_before)+" "+
                        DateFormat.getDateTimeInstance().format(System.currentTimeMillis())+".csv";
                String allTestCSVFileName = FileUtil.getAppStorageDirectoryForExports()+ File.separator+exportedFileName;

                try {
                    if(CSVUtil.exportAllTestsAsCSV(allTestCSVFileName)){
                        Toast.makeText(getActivity(), "Exported! "+exportedFileName, Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Export Failed!"+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
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

    private List<SpirometerTest> generateFinalList(List<SpirometerTest> online, List<SpirometerTest> offline){
        List<SpirometerTest> finalList = new ArrayList<>();


        

        return finalList;
    }
}