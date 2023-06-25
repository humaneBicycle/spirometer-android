package com.humanebicycle.spirometer.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.humanebicycle.spirometer.R;
import com.humanebicycle.spirometer.data.XStreamSerializer;
import com.humanebicycle.spirometer.model.SpirometerTest;
import com.humanebicycle.spirometer.utils.CSVUtil;
import com.humanebicycle.spirometer.utils.FileUtil;

import java.io.File;
import java.io.IOException;

public class TestLongClickOptions extends BottomSheetDialogFragment {
    SpirometerTest test;
    RecordsAdapter recordsAdapter;
    public TestLongClickOptions(RecordsAdapter recordsAdapter,SpirometerTest test){
        this.test=test;
        this.recordsAdapter = recordsAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.test_long_click_options,container);

        MaterialButton exportCSVButton = v.findViewById(R.id.export_long_click);
        MaterialButton delete = v.findViewById(R.id.delete_test_long_click);

        exportCSVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = FileUtil.getAppStorageDirectoryForExports()+test.getId()+".csv";

                try {
                    if (CSVUtil.exportTestAsCSV(path, test)) {
                        Toast.makeText(getContext(), "Exported! " + test.getId() + ".csv", Toast.LENGTH_LONG).show();
                    }
                }catch (IOException e){
                    Toast.makeText(getContext(), "Error exporting CSV! "+e.getMessage(), Toast.LENGTH_LONG).show();

                }


            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(XStreamSerializer.getInstance().deleteTest(test)){
                    Toast.makeText(getContext(), "Successfully deleted!", Toast.LENGTH_SHORT).show();

                    //update UI
                    recordsAdapter.tests.remove(test);
                    recordsAdapter.notifyDataSetChanged();
                    dismiss();
                }else{
                    Toast.makeText(getContext(), "Something went wrong while deleting!", Toast.LENGTH_SHORT).show();

                }

            }
        });

        return v;
    }
}
