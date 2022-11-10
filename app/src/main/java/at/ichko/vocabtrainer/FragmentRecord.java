package at.ichko.vocabtrainer;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class FragmentRecord extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    boolean allowed, recording = false;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    checkPermission();
                }
            });

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    ArrayList<String> tableNames = new ArrayList<>();

    ConstraintLayout lytMid;

    NestedScrollView scrollView;
    GridLayout gridLayout;

    final String databaseName = "languagedatabase.db";
    final String prefTableId = "tableid";

    MediaRecorder recorder;

    String fileName; //File Name of the Sound file that gets recorded

    Spinner spLanguageSelect;

    ImageButton btnRecord;

    EditText etRecordId;

    public FragmentRecord() {

    }

    public static FragmentRecord newInstance(String param1, String param2) {
        FragmentRecord fragment = new FragmentRecord();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("ResourceType")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_record, container, false);

        lytMid = rootView.findViewById(R.id.lytMidRecord);

        btnRecord = rootView.findViewById(R.id.btnRecord);
        btnRecord.setImageResource(R.raw.mic);

        spLanguageSelect = rootView.findViewById(R.id.spLanguagesRecord);

        etRecordId = rootView.findViewById(R.id.etIdRecord);

        btnRecord.setOnClickListener(this);

        spLanguageSelect.setOnItemSelectedListener(this);

        getTableNames();
        refreshDropdown();
        getOverview();

        return rootView;
    }

    public void getOverview(){
        SQLiteDatabase database = getActivity().openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        ArrayList<String> allWords = new ArrayList<String>();
        ArrayList<String> allTranslations = new ArrayList<String>();

        for(int i = 1; i < getTableSize(); i++){
            Cursor cursor = database.rawQuery("SELECT * FROM " + tableNames.get(getTableIndex()) + " WHERE id = '" + i + "'", null);
            cursor.moveToFirst();

            allWords.add(cursor.getString(1));
            allTranslations.add(cursor.getString(2));

            cursor.close();
        }

        database.close();
        generateGrid(allWords, allTranslations);
    }

    public void generateGrid(ArrayList<String> allWords, ArrayList<String> allTranslations){
        scrollView = new NestedScrollView(getActivity());
        gridLayout = new GridLayout(getActivity());
        HorizontalScrollView scrollViewHorizontal = new HorizontalScrollView(getActivity());

        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500));

        gridLayout.setColumnCount(3);
        gridLayout.setRowCount(getTableSize());
        gridLayout.setOrientation(GridLayout.HORIZONTAL);
        scrollViewHorizontal.addView(gridLayout);
        scrollView.addView(scrollViewHorizontal);
        lytMid.addView(scrollView, 0);

        for(int i = 0; i < allWords.size(); i++){
            String fileName;
            File file;
            TextView tvId = new TextView(getActivity());
            TextView tvWordOverview = new TextView(getActivity());
            TextView tvTranslationOverview = new TextView(getActivity());

            fileName = getActivity().getExternalCacheDir().getAbsolutePath() + "/RecordAudio" + tableNames.get(getTableIndex()) + (i+1) + ".3gp";
            file = new File(fileName);

            tvId.setTextSize(18);
            tvWordOverview.setTextSize(18);
            tvTranslationOverview.setTextSize(18);

            tvId.setPadding(20, 0, 15, 0);
            tvTranslationOverview.setPadding(20,0,10,20);
            tvWordOverview.setPadding(20, 0,0,0);

            tvId.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvWordOverview.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvTranslationOverview.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            tvId.setText(i+1 + "");
            tvWordOverview.setText(allWords.get(i));
            tvTranslationOverview.setText(allTranslations.get(i));

            if(file.exists()){
                tvId.setBackgroundColor(Color.LTGRAY);
            }

            gridLayout.addView(tvId);
            gridLayout.addView(tvWordOverview);
            gridLayout.addView(tvTranslationOverview);
        }

        gridLayout.setVisibility(View.VISIBLE);
    }

    public void startRecording(){
        getTableNames();
        recorder = new MediaRecorder();

        fileName = getActivity().getExternalCacheDir().getAbsolutePath();
        fileName += "/RecordAudio" + tableNames.get(getTableIndex()) + etRecordId.getText().toString() + ".3gp";


        recorder.setAudioChannels(1);
        recorder.setAudioEncodingBitRate(16);
        recorder.setAudioSamplingRate(8000);

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        recorder.setOutputFile(fileName);

        try{
            recorder.prepare();
        } catch(IOException ex){
            Log.d("ERROR: ", "Preparing of Recording failed");
        }

        recorder.start();
        recording = true;
    }

    public void stopRecording(){
        recorder.stop();

        recorder.release();
        recorder = null;
        recording = false;
    }

    public void checkPermission(){
        if((ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
            allowed = true;
        } else {
            allowed = false;
            requestPermission();
        }
    }

    private void requestPermission () {
        if (ContextCompat.checkSelfPermission(
                getActivity().getApplicationContext(), RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(RECORD_AUDIO);
        }

        if (ContextCompat.checkSelfPermission(
                getActivity().getApplicationContext(), READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE);
        }


        if (ContextCompat.checkSelfPermission(
                getActivity().getApplicationContext(), WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(WRITE_EXTERNAL_STORAGE);
        }

    }

    public void getTableNames(){
        tableNames.clear();
        SQLiteDatabase database = getActivity().openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT IN ('android_metadata', 'sqlite_sequence', 'room_master_table') ",null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            tableNames.add(cursor.getString(0));
            cursor.moveToNext();
        }

        cursor.close();
        database.close();
    }

    public Integer getTableSize(){
        SharedPreferences preferences = getActivity().getSharedPreferences(prefTableId, Context.MODE_PRIVATE);
        SQLiteDatabase database = getActivity().openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        int i = 0;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM " + tableNames.get(preferences.getInt(prefTableId, 0)), null);
            cursor.moveToLast();

            i = cursor.getInt(0) + 1;
        } catch (CursorIndexOutOfBoundsException ex){
            Log.d("ERROR: ", "Noch keine Wörter vorhanden");
            i = 0;
        }

        cursor.close();
        database.close();

        return i;
    }

    public Integer getTableIndex(){
        SharedPreferences preferences = getActivity().getSharedPreferences(prefTableId, Context.MODE_PRIVATE);
        return preferences.getInt(prefTableId, 0);
    }

    public void refreshDropdown(){
        getTableNames();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, tableNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLanguageSelect.setAdapter(adapter);

        spLanguageSelect.setSelection(getTableIndex());

    }

    @SuppressLint("ResourceType")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnRecord:
                checkPermission();

                if(!recording){
                    if(!allowed)
                        Toast.makeText(getActivity().getApplicationContext(), "Permition requierd", Toast.LENGTH_SHORT).show();
                    else{
                       try {
                           if (Integer.parseInt(etRecordId.getText().toString()) > 0 &&
                                   Integer.parseInt(etRecordId.getText().toString()) < getTableSize()) {
                               startRecording();
                               btnRecord.setImageResource(R.drawable.ic_baseline_stop_24);
                           } else {
                               Toast.makeText(getActivity().getApplicationContext(), "Check your inputs", Toast.LENGTH_SHORT).show();
                           }
                       } catch (Exception ex){
                           Toast.makeText(getActivity().getApplicationContext(), "Check your inputs", Toast.LENGTH_SHORT).show();
                       }
                    }
                }
                else {
                    stopRecording();
                    etRecordId.setText("");
                    btnRecord.setImageResource(R.raw.mic);
                    Toast.makeText(getActivity().getApplicationContext(), "Successfully Saved", Toast.LENGTH_SHORT).show();

                    ViewGroup parent = (ViewGroup) scrollView.getParent();
                    parent.removeView(scrollView);

                    getOverview();
                }


                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SharedPreferences prefTable = getActivity().getSharedPreferences(prefTableId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefTable.edit();

        editor.putInt(prefTableId, i);
        editor.commit();

        spLanguageSelect.setSelection(i);

        ViewGroup parent = (ViewGroup) scrollView.getParent();
        parent.removeView(scrollView);

        getOverview();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        ViewGroup parent = (ViewGroup) scrollView.getParent();
        parent.removeView(scrollView);

        getOverview();
    }
}