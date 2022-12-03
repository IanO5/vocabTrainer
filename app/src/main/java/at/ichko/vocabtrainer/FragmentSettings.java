package at.ichko.vocabtrainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

public class FragmentSettings extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    Switch swAnimation;
    Switch swSound;
    Switch swPlayRecord;

    SharedPreferences prefSound;
    SharedPreferences pref;
    SharedPreferences prefPlayRecord;

    TextView tvStreak;

    public FragmentSettings() {

    }

    public static FragmentSettings newInstance(String param1, String param2) {
        FragmentSettings fragment = new FragmentSettings();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        tvStreak = rootView.findViewById(R.id.tvStreak);

        swAnimation = rootView.findViewById(R.id.swPlayAnimation);
        swSound = rootView.findViewById(R.id.swSound);
        swPlayRecord = rootView.findViewById(R.id.swRecorded);

        swAnimation.setOnClickListener(this);
        swSound.setOnClickListener(this);
        swPlayRecord.setOnClickListener(this);

        pref = getActivity().getSharedPreferences(Constants.PREF_SETTING_ANIMATION, Context.MODE_PRIVATE);
        prefSound = getActivity().getSharedPreferences(Constants.PREF_SETTING_SOUND, Context.MODE_PRIVATE);
        prefPlayRecord = getActivity().getSharedPreferences(Constants.PERF_SETTING_PLAY_RECORD, Context.MODE_PRIVATE);

        if(!pref.getBoolean(Constants.PREF_SETTING_ANIMATION, true))
            swAnimation.setChecked(false);
        if(!prefSound.getBoolean(Constants.PREF_SETTING_SOUND, true))
            swSound.setChecked(false);
        if(!prefPlayRecord.getBoolean(Constants.PERF_SETTING_PLAY_RECORD, true))
            swPlayRecord.setChecked(false);

        tvStreak.setText(streakDays().toString());

        return rootView;
    }

    public void saveSettings(){
        SharedPreferences.Editor editor = pref.edit();
        SharedPreferences.Editor editorSound = prefSound.edit();
        SharedPreferences.Editor editorPlayRecord = prefPlayRecord.edit();

        editor.putBoolean(Constants.PREF_SETTING_ANIMATION, swAnimation.isChecked());
        editor.commit();

        editorSound.putBoolean(Constants.PREF_SETTING_SOUND, swSound.isChecked());
        editorSound.commit();

        editorPlayRecord.putBoolean(Constants.PERF_SETTING_PLAY_RECORD, swPlayRecord.isChecked());
        editorPlayRecord.commit();
    }

    public Integer streakDays(){
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.PREF_STREAK, Context.MODE_PRIVATE);
        return preferences.getInt(Constants.PREF_STREAK, 1);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.swPlayAnimation:
            case R.id.swSound:
            case R.id.swRecorded:
                saveSettings();
                break;
        }
    }
}