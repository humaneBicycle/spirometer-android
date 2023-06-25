package com.humanebicycle.spirometer.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.humanebicycle.spirometer.R;
import com.humanebicycle.spirometer.adapter.RecordsAdapter;
import com.humanebicycle.spirometer.data.XStreamSerializer;
import com.humanebicycle.spirometer.model.SpirometerTest;

import java.io.IOException;
import java.util.List;

public class ListFragment extends Fragment {

    RecyclerView recyclerView;
    TextView noTestsTextView;
    List<SpirometerTest> tests;
    ProgressBar progressBar;


    public ListFragment() {
    }

    public static ListFragment newInstance() {
        ListFragment fragment = new ListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_list, container, false);
        noTestsTextView = view.findViewById(R.id.no_tests_recorded_tv_list_frag);
        recyclerView = view.findViewById(R.id.records_rv);
        progressBar = view.findViewById(R.id.progress_list_frag);


        Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                    tests = XStreamSerializer.getInstance().getPreviousTests();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //update ui. test list fetched
                            progressBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);

                            if(tests.size()==0){
                                noTestsTextView.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }else {
                                RecordsAdapter adapter = new RecordsAdapter(getContext(), tests);

                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            }
                        }
                    });
                    } catch (Exception e) {
                        Log.d("abh", "run: "+e);;
                    }
                }
        };

        new Thread(runnable).start();


        return view;
    }
}