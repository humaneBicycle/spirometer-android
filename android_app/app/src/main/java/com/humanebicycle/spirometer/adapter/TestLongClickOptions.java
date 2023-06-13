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
import com.humanebicycle.spirometer.model.SpirometerTest;
import com.humanebicycle.spirometer.utils.CSVUtil;
import com.humanebicycle.spirometer.utils.FileUtil;

import java.io.File;
import java.io.IOException;

public class TestLongClickOptions extends BottomSheetDialogFragment {
    SpirometerTest test;
    public TestLongClickOptions(SpirometerTest test){
        this.test=test;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.test_long_click_options,container);

        MaterialButton exportCSVButton = v.findViewById(R.id.export_long_click);
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

        return v;
    }
}
