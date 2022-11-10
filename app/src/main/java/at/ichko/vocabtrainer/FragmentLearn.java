package at.ichko.vocabtrainer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;

import java.io.IOException;
import java.util.ArrayList;

public class FragmentLearn extends Fragment implements View.OnClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    TextView tvVocabStrength;
    TextView tvVocabNr;
    TextView tvWord;
    TextView tvTranslation;
    TextView tvScore;

    LottieAnimationView avCorrect;
    LottieAnimationView avFalse;

    ImageView ivTarget;

    Spinner spLanguageSelect;

    Button btnShowTranslation;
    Button btnCorrect;
    Button btnFalse;

    MediaPlayer vocabSound;

    int[] vocabStrength;
    ArrayList<Integer> saveFalseId = new ArrayList<>();

    int idOfFalse = 0;
    int currentId;
    int maxScore, score;

    boolean wasFalse = false;
    boolean isSwitched = false;

    final String databaseName = "languagedatabase.db";
    final String prefSettingPlayRecord = "settingplayrecord";
    final String prefSettingAnimation = "settinganimation";
    final String prefSettingSound = "settingsound";

    LanguageSpinner spinner;
    Table table;

    public FragmentLearn() {

    }
    public static FragmentLearn newInstance(String param1, String param2) {
        FragmentLearn fragment = new FragmentLearn();
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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_learn, container, false);
        tvVocabStrength = rootView.findViewById(R.id.tvVocabStrength);
        tvVocabNr = rootView.findViewById(R.id.tvVoabNr);
        tvWord = rootView.findViewById(R.id.tvWord);
        tvTranslation = rootView.findViewById(R.id.tvTranslation);
        tvScore = rootView.findViewById(R.id.tvScore);

        avCorrect = rootView.findViewById(R.id.avCorrect);
        avFalse = rootView.findViewById(R.id.avFalse);

        ivTarget = rootView.findViewById(R.id.ivTarget);
        ivTarget.setImageResource(R.raw.target);

        spLanguageSelect = rootView.findViewById(R.id.spLanguagesLearn);

        btnShowTranslation = rootView.findViewById(R.id.btnShowTranslation);
        btnCorrect = rootView.findViewById(R.id.btnCorrect);
        btnFalse = rootView.findViewById(R.id.btnFalse);

        btnShowTranslation.setOnClickListener(this);
        btnCorrect.setOnClickListener(this);
        btnFalse.setOnClickListener(this);

        spinner = new LanguageSpinner(spLanguageSelect, getActivity(), () -> {
            btnCorrect.setVisibility(View.GONE);
            btnFalse.setVisibility(View.GONE);
            learn();}, () -> learn());

        table = new Table(getActivity());

        table.getTableNames();
        spinner.refresh();
        //learn();

        return rootView;
    }

    public void learn() {
        SQLiteDatabase database = getActivity().openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        if (table.getSize() > 0) {

            btnShowTranslation.setVisibility(View.VISIBLE);
            SharedPreferences prefPlayRecord = getActivity().getSharedPreferences(prefSettingPlayRecord, Context.MODE_PRIVATE);
            vocabStrength = new int[table.getSize() - 1];

            boolean foundNewWord = false;
            int id = 0;

            while (!foundNewWord) {
                if (!saveFalseId.isEmpty() && Math.random() > 0.5) {
                    id = saveFalseId.get((int) (Math.random() * (saveFalseId.size() - 1)));
                    idOfFalse = saveFalseId.indexOf(id);
                    wasFalse = true;
                    foundNewWord = true;
                } else {
                    id = (int) ((Math.random() * (table.getSize() - 1)) + 1);
                    wasFalse = false;
                    if (vocabStrength[id - 1] > 5) {
                        foundNewWord = false;
                    } else if (vocabStrength[id - 1] > 3) {
                        if (Math.random() > 0.5) {
                            foundNewWord = true;
                        }
                    } else {
                        foundNewWord = true;
                    }
                }
            }

            currentId = id;
            tvVocabNr.setText("Vocab Nr.: " + id);

            Cursor cursor = database.rawQuery("SELECT * FROM " + table.get(table.getTableIndex()) + " WHERE id = '" + id + "'", null);
            cursor.moveToFirst();

            if (Math.random() > 0.5) {
                isSwitched = true;
            } else {
                isSwitched = false;
            }

            if (!isSwitched) {
                tvWord.setText(cursor.getString(1));
                tvTranslation.setText(cursor.getString(2));
            } else {
                tvWord.setText(cursor.getString(2));
                tvTranslation.setText(cursor.getString(1));
                if (prefPlayRecord.getBoolean(prefSettingPlayRecord, true)) {
                    vocabSound = new MediaPlayer();

                    try {
                        table.getTableNames();
                        vocabSound.setDataSource(getActivity().getExternalCacheDir().getAbsolutePath() + "/RecordAudio" + table.get(table.getTableIndex()) + id + ".3gp");
                        vocabSound.prepare();
                        vocabSound.start();
                    } catch (IOException e) {
                        Log.d("ERROR: ", "No recorded File found");
                    }
                }
            }

            tvScore.setText("Score: " + score + "/" + maxScore);

            if (maxScore > 0) {
                if ((score * 100) / maxScore >= 70) {
                    tvScore.setTextColor(Color.GREEN);
                } else {
                    tvScore.setTextColor(Color.RED);
                }
            }

            cursor.close();
        }
        else {
            tvWord.setText("Word");
            tvTranslation.setText("");
            tvScore.setText("ADD VOCABS TO\nSTART LEARNING");
            tvScore.setTextColor(Color.GREEN);
            btnShowTranslation.setVisibility(View.GONE);
        }

        database.close();
    }

    public void falseTranslation() {
        SQLiteDatabase database = getActivity().openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        SharedPreferences pref = getActivity().getSharedPreferences(prefSettingAnimation, Context.MODE_PRIVATE);
        SharedPreferences prefSound = getActivity().getSharedPreferences(prefSettingSound, Context.MODE_PRIVATE);
        Cursor cursor;

        if (!isSwitched) {
            cursor = database.rawQuery("SELECT * FROM " + table.get(table.getTableIndex()) + " WHERE word = '" + tvWord.getText() + "'", null);

        } else {
            cursor = database.rawQuery("SELECT * FROM " + table.get(table.getTableIndex()) + " WHERE word = '" + tvTranslation.getText() + "'", null);
        }

        cursor.moveToFirst();

        saveFalseId.add(Integer.parseInt(cursor.getString(0)));

        maxScore++;

        cursor.close();
        database.close();

        if (prefSound.getBoolean(prefSettingSound, true)) {
            MediaPlayer incorrectSound = MediaPlayer.create(getActivity(), R.raw.incorrectsound);
            incorrectSound.start();
        }

        if (pref.getBoolean(prefSettingAnimation, true)) {
            playAnimation(false);
        } else {
            learn();
        }
    }

    public void correctTranslation(){
        SharedPreferences pref = getActivity().getSharedPreferences(prefSettingAnimation, Context.MODE_PRIVATE);
        SharedPreferences prefSound = getActivity().getSharedPreferences(prefSettingSound, Context.MODE_PRIVATE);

        if(wasFalse) {
            saveFalseId.remove(idOfFalse);
        } else {
            vocabStrength[currentId-1]++;
        }
        maxScore++;
        score++;

        if(prefSound.getBoolean(prefSettingSound, true)){
            MediaPlayer correctSound = MediaPlayer.create(getActivity(), R.raw.correctsound);
            correctSound.start();
        }

        if(pref.getBoolean(prefSettingAnimation, true)){
            playAnimation(true);
        }
        else {
            learn();
        }
    }

    public void playAnimation(boolean animationCorrect){
        tvVocabNr.setVisibility(View.GONE);
        tvVocabStrength.setVisibility(View.GONE);
        tvWord.setVisibility(View.GONE);
        tvTranslation.setVisibility(View.INVISIBLE);
        btnShowTranslation.setVisibility(View.GONE);
        btnFalse.setVisibility(View.GONE);
        btnCorrect.setVisibility(View.GONE);

        if(animationCorrect){
            avCorrect.setVisibility(View.VISIBLE);
            avCorrect.playAnimation();
        }
        else {
            avFalse.setVisibility(View.VISIBLE);
            avFalse.playAnimation();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (avCorrect.getVisibility() != View.GONE || avFalse.getVisibility() != View.GONE) {
                        avFalse.setVisibility(View.GONE);
                        avCorrect.setVisibility(View.GONE);
                        tvVocabNr.setVisibility(View.VISIBLE);
                        tvVocabStrength.setVisibility(View.VISIBLE);
                        tvWord.setVisibility(View.VISIBLE);
                        tvTranslation.setVisibility(View.INVISIBLE);
                        btnShowTranslation.setVisibility(View.VISIBLE);
                        btnFalse.setVisibility(View.GONE);
                        btnCorrect.setVisibility(View.GONE);

                        learn();
                    }
                } catch (Exception ex){
                    Log.d("ERROR: ", "Animation was played, while switching in the menu");
                }
            }
        }, 1500);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnShowTranslation:
                SharedPreferences prefPlayRecord = getActivity().getSharedPreferences(prefSettingPlayRecord, getActivity().MODE_PRIVATE);
                btnShowTranslation.setVisibility(View.GONE);
                tvTranslation.setVisibility(View.VISIBLE);
                btnCorrect.setVisibility(View.VISIBLE);
                btnFalse.setVisibility(View.VISIBLE);
                if (!isSwitched && prefPlayRecord.getBoolean(prefSettingPlayRecord, true)) {
                    vocabSound = new MediaPlayer();

                    try {
                        table.getTableNames();
                        vocabSound.setDataSource(getActivity().getExternalCacheDir().getAbsolutePath() + "/RecordAudio" + table.get(table.getTableIndex()) + currentId + ".3gp");
                        vocabSound.prepare();
                        vocabSound.start();
                    } catch (IOException e) {
                        Log.d("ERROR: ", "No recorded File found");
                    }
                }
                break;
            case R.id.btnCorrect:
                btnShowTranslation.setVisibility(View.VISIBLE);
                tvTranslation.setVisibility(View.INVISIBLE);
                btnCorrect.setVisibility(View.GONE);
                btnFalse.setVisibility(View.GONE);
                correctTranslation();
                break;
            case R.id.btnFalse:
                btnShowTranslation.setVisibility(View.VISIBLE);
                tvTranslation.setVisibility(View.INVISIBLE);
                btnCorrect.setVisibility(View.GONE);
                btnFalse.setVisibility(View.GONE);
                falseTranslation();
                break;
        }

    }

}