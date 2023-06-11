package com.humanebicycle.spirometer.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.humanebicycle.spirometer.R;
import com.humanebicycle.spirometer.Spirometer;
import com.humanebicycle.spirometer.adapter.RecordsAdapter;
import com.humanebicycle.spirometer.helper.XStreamSerializer;
import com.humanebicycle.spirometer.model.SpirometerTest;

import java.util.List;

public class ListFragment extends Fragment {

    RecyclerView recyclerView;
    TextView noTestsTextView;

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

        List<SpirometerTest> tests = XStreamSerializer.getInstance().getPreviousTests(getContext());
        if(tests.size()==0){
            noTestsTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }else {
            RecordsAdapter adapter = new RecordsAdapter(getContext(), tests);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        return view;
    }
}