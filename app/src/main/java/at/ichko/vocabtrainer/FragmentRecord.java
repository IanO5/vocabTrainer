package at.ichko.vocabtrainer;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.IOException;

public class FragmentRecord extends Fragment implements View.OnClickListener {

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

    boolean allowed, recording;

    ConstraintLayout lytMid;

    MediaRecorder recorder;

    String fileName;

    Spinner spLanguageSelect;
    Keyboard keyboard;

    ImageButton btnRecord;

    EditText etRecordId;

    Overview overview;
    Table table;
    LanguageSpinner spinner;

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

        overview = new Overview(lytMid, getActivity());
        table = new Table(getActivity());
        spinner = new LanguageSpinner(spLanguageSelect, getActivity(), () -> {
            overview.deleteScrollView();
            overview.getOverview(true);
        }, () -> {
            overview.deleteScrollView();
            overview.getOverview(true);
        });
        keyboard = new Keyboard(getContext(), getActivity());

        table.getTableNames();
        spinner.refresh();
        overview.getOverview(true);

        return rootView;
    }

    public void startRecording(){
        recorder = new MediaRecorder();

        fileName = getActivity().getExternalCacheDir().getAbsolutePath();
        fileName += "/RecordAudio" + table.get(table.getTableIndex()) + etRecordId.getText().toString() + ".3gp";

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
                           Log.d("RECORD ID: ", Integer.parseInt(etRecordId.getText().toString()) + "");
                           if (Integer.parseInt(etRecordId.getText().toString()) > 0 &&
                                   Integer.parseInt(etRecordId.getText().toString()) < table.getSize()) {
                               startRecording();
                               etRecordId.setEnabled(false);

                               try {
                                   keyboard.hide();
                               } catch (Exception ex) {
                                   Log.d("ALARM: ", "Exception triggerd! Could not open keyboard.");
                               }

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
                    etRecordId.setEnabled(true);
                    etRecordId.setText("");
                    btnRecord.setImageResource(R.raw.mic);
                    Toast.makeText(getActivity().getApplicationContext(), "Successfully Saved", Toast.LENGTH_SHORT).show();

                    overview.deleteScrollView();
                    overview.getOverview(true);
                }
                break;
        }
    }
}