package com.humanebicycle.spirometer.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.humanebicycle.spirometer.AnalyticsActivity;
import com.humanebicycle.spirometer.Constants;
import com.humanebicycle.spirometer.R;
import com.humanebicycle.spirometer.data.XStreamSerializer;
import com.humanebicycle.spirometer.model.SpirometerTest;

import java.text.DateFormat;
import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordHolder> {

    List<SpirometerTest> tests;
    Context context;

    public RecordsAdapter (Context context, List<SpirometerTest> tests){
        this.tests=tests;
        this.context=context;
        Log.d("abh", "RecordsAdapter size: "+tests.size());
    }

    @NonNull
    @Override
    public RecordHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.test_record,new LinearLayout(context),false);

        return new RecordHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordHolder holder, int position) {
        SpirometerTest test = tests.get(tests.size()-position-1);
        holder.heading.setText(test.getName());
        holder.subHeading.setText(String.valueOf(DateFormat.getDateTimeInstance().format(test.getTime())));

        holder.testRecordItemParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AnalyticsActivity.class);
                String testToAnalyze = XStreamSerializer.getInstance().serialize(test);
                intent.putExtra(Constants.CURRENT_TEST,testToAnalyze);
                context.startActivity(intent);
            }
        });

        holder.testRecordItemParent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                TestLongClickOptions testLongClickOptions = new TestLongClickOptions(test);
                FragmentManager manager = ((AppCompatActivity)context).getSupportFragmentManager();
                testLongClickOptions.show(manager,"testLongClickOptions");
                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return tests.size();
    }

    public class RecordHolder extends RecyclerView.ViewHolder{
        TextView heading, subHeading;
        LinearLayout testRecordItemParent;

        public RecordHolder(@NonNull View itemView) {
            super(itemView);
            heading = itemView.findViewById(R.id.record_test_heading);
            subHeading = itemView.findViewById(R.id.record_test_sub_heading);
            testRecordItemParent = itemView.findViewById(R.id.test_record_item_parent);
        }
    }

}
