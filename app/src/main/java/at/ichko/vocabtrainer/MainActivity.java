package at.ichko.vocabtrainer;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_APN_SETTINGS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.usage.ExternalStorageStats;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.CacheRequest;
import java.util.ArrayList;
import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    boolean allowed;

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    checkPermission();
                }
            });


    LinearLayout lytMain;
    LinearLayout lytHome;
    LinearLayout lytAdd;
    LinearLayout lytLearn;
    LinearLayout lytCorrectFalse;
    LinearLayout lytSettings;
    LinearLayout lytOverview;
    LinearLayout lytId; //Get the Layout of the Id that will change in the Overview Tab
    LinearLayout lytTypeChange; //Get the type that should be changed in Database through Overview Tab (Decision: Word or Translation)
    LinearLayout lytSubmitChanges; //Last Layout of the change Cycle in the Overview tab, here you can Submit your Changes
    LinearLayout lytRecordId; //Layout for getting the Id which should get Recorded

    ScrollView scrollView;
    GridLayout gridLayout;

    Button btnAdd;
    Button btnSubmit;
    Button btnShow;
    Button btnCorrect;
    Button btnFalse;
    Button btnLearn;
    Button btnOverview;
    Button btnSettings;
    Button btnChangeData; //Request Change of Data in Database in the Overview Tab
    Button btnSubmitId; //Submit the Id that should be changed in Database through Overview Tab
    Button btnChangeWord; //Depending on which Button is pressed (changeWord or changeTranslation) this type will be change in the db
    Button btnChangeTranslation;
    Button btnSubmitChange; //Button to submit changes in db
    Button btnRecordId;     //Button for getting Id of Word to Record
    Button btnSubmitRecordId; //Button that saves the Id of the Word that should get Recorded
    Button btnRecord; //Button to start recording
    Button btnStopRecording; //Button that stops Recording
    Button btnBack;

    Switch swAnimation;
    Switch swSound;
    Switch swPlayRecord;

    TextView tvWord;
    TextView tvTranslation;
    TextView tvScore;

    EditText etWord;
    EditText etTranslation;
    EditText etChangeDataId; //Getting the Id of the Data that should be change in the Overview Tab
    EditText etChange; //Text that will replace the old data
    EditText etRecordId; //Id that will get a Record of Sound

    LottieAnimationView avCorrect;
    LottieAnimationView avIncorrect;

    MediaPlayer correctSound;
    MediaPlayer vocabSound;
    MediaRecorder recorder;

    ArrayList<Integer> saveFalseId = new ArrayList<Integer>();
    int[] vocabStrength;

    int idOfFalse = 0;
    int maxScore, score;
    int currentId;

    final String prefFirstStart = "firststart";
    final String databaseName = "languagedatabase.db";
    final String tableName = "firsttable";
    final String prefIndex = "indexpref";
    final String prefSettingAnimation = "settinganimation";
    final String prefSettingSound = "settingsound";
    final String prefSettingPlayRecord = "settingplayrecord";

    String fileName; //File Name of the Sound file that gets recorded

    boolean wasFalse = false;
    boolean isSwitched = false;
    boolean isOverview = false;
    boolean isWord = false; //Boolean that saves if the word that will be changed in the db through
                            //Overview Tab is the Type of Word or the Type of Translation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lytMain = findViewById(R.id.lytMain);
        lytHome = findViewById(R.id.lytTwoButtons);
        lytAdd = findViewById(R.id.lytAdd);
        lytLearn = findViewById(R.id.lytLearn);
        lytCorrectFalse = findViewById(R.id.lytCorrectFalse);
        lytSettings = findViewById(R.id.lytSettings);
        lytOverview = findViewById(R.id.lytOverview);
        lytId = findViewById(R.id.lytId);
        lytTypeChange = findViewById(R.id.lytTypeChange);
        lytSubmitChanges = findViewById(R.id.lytSubmitChanges);
        lytRecordId = findViewById(R.id.lytRecordId);

        btnAdd = findViewById(R.id.btnAdd);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnShow = findViewById(R.id.btnShowTranslation);
        btnCorrect = findViewById(R.id.btnCorrect);
        btnFalse = findViewById(R.id.btnFalse);
        btnLearn = findViewById(R.id.btnLearn);
        btnOverview = findViewById(R.id.btnOverview);
        btnSettings = findViewById(R.id.btnSettings);
        btnChangeData = findViewById(R.id.btnChangeData);
        btnSubmitId = findViewById(R.id.btnSubmitId);
        btnChangeWord = findViewById(R.id.btnChangeWord);
        btnChangeTranslation = findViewById(R.id.btnChangeTranslation);
        btnSubmitChange = findViewById(R.id.btnSubmitChange);
        btnRecordId = findViewById(R.id.btnRecordId);
        btnSubmitRecordId = findViewById(R.id.btnSubmitRecordId);
        btnRecord = findViewById(R.id.btnRecord);
        btnStopRecording = findViewById(R.id.btnStopRecording);
        btnBack = findViewById(R.id.btnBack);

        swAnimation = findViewById(R.id.swAnimation);
        swSound = findViewById(R.id.swSound);
        swPlayRecord = findViewById(R.id.swRecordedLines);

        tvWord = findViewById(R.id.tvWord);
        tvTranslation = findViewById(R.id.tvTranslation);
        tvScore = findViewById(R.id.tvScore);

        etWord = findViewById(R.id.etWord);
        etTranslation = findViewById(R.id.etTranslation);
        etChangeDataId = findViewById(R.id.etChangeDataId);
        etChange = findViewById(R.id.etChange);
        etRecordId = findViewById(R.id.etRecordId);

        avCorrect = findViewById(R.id.avCorrect);
        avIncorrect = findViewById(R.id.avIncorrect);

        btnAdd.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        btnShow.setOnClickListener(this);
        btnCorrect.setOnClickListener(this);
        btnFalse.setOnClickListener(this);
        btnLearn.setOnClickListener(this);
        btnOverview.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnChangeData.setOnClickListener(this);
        btnSubmitId.setOnClickListener(this);
        btnChangeWord.setOnClickListener(this);
        btnChangeTranslation.setOnClickListener(this);
        btnSubmitChange.setOnClickListener(this);
        btnRecordId.setOnClickListener(this);
        btnSubmitRecordId.setOnClickListener(this);
        btnRecord.setOnClickListener(this);
        btnStopRecording.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        swAnimation.setOnClickListener(this);
        swSound.setOnClickListener(this);
        swPlayRecord.setOnClickListener(this);

        if(firstAppStart()){
            createDatabase();
        }
    }

    public void addWord(String word, String translation){
        SQLiteDatabase database = openOrCreateDatabase(databaseName, MODE_PRIVATE, null);
        SharedPreferences preferences = getSharedPreferences(prefIndex, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        int i = preferences.getInt(prefIndex, 1);

        database.execSQL("INSERT INTO " + tableName + " VALUES('" + i + "', '" + word + "', '" + translation + "')");

        editor.putInt(prefIndex, i+1);
        editor.commit();

        database.close();
    }

    public void learn() {
        SQLiteDatabase database = openOrCreateDatabase(databaseName, MODE_PRIVATE, null);
        SharedPreferences preferences = getSharedPreferences(prefIndex, MODE_PRIVATE);
        if (preferences.getInt(prefIndex, 0) > 0) {

            SharedPreferences prefPlayRecord = getSharedPreferences(prefSettingPlayRecord, MODE_PRIVATE);
            vocabStrength = new int[preferences.getInt(prefIndex, 1) - 1];

            boolean foundNewWord = false;
            int id = 0;

            while (!foundNewWord) {
                if (!saveFalseId.isEmpty() && Math.random() > 0.5) {
                    id = saveFalseId.get((int) (Math.random() * (saveFalseId.size() - 1)));
                    idOfFalse = saveFalseId.indexOf(id);
                    wasFalse = true;
                    foundNewWord = true;
                } else {
                    id = (int) ((Math.random() * (preferences.getInt(prefIndex, 1) - 1)) + 1);
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

            Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE id = '" + id + "'", null);
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
                        vocabSound.setDataSource(getExternalCacheDir().getAbsolutePath() + "/RecordAudio" + id + ".3gp");
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
            database.close();
        }
    }

    public void falseTranslation() {
        SQLiteDatabase database = openOrCreateDatabase(databaseName, MODE_PRIVATE, null);
        SharedPreferences pref = getSharedPreferences(prefSettingAnimation, MODE_PRIVATE);
        SharedPreferences prefSound = getSharedPreferences(prefSettingSound, MODE_PRIVATE);
        Cursor cursor;

        if (!isSwitched) {
            cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE word = '" + tvWord.getText() + "'", null);

        } else {
            cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE word = '" + tvTranslation.getText() + "'", null);
        }

        cursor.moveToFirst();

        saveFalseId.add(Integer.parseInt(cursor.getString(0)));

        maxScore++;

        cursor.close();
        database.close();

        if (prefSound.getBoolean(prefSettingSound, true)) {
            MediaPlayer incorrectSound = MediaPlayer.create(this, R.raw.incorrectsound);
            incorrectSound.start();
        }

        if (pref.getBoolean(prefSettingAnimation, true)) {
            playAnimation(false);
        } else {
            learn();
        }
    }

    public void correctTranslation(){
        SharedPreferences pref = getSharedPreferences(prefSettingAnimation, MODE_PRIVATE);
        SharedPreferences prefSound = getSharedPreferences(prefSettingSound, MODE_PRIVATE);

        if(wasFalse) {
            saveFalseId.remove(idOfFalse);
        } else {
            vocabStrength[currentId-1]++;
        }
        maxScore++;
        score++;

        if(prefSound.getBoolean(prefSettingSound, true)){
            correctSound = MediaPlayer.create(this, R.raw.correctsound);
            correctSound.start();
        }

        if(pref.getBoolean(prefSettingAnimation, true)){
            playAnimation(true);
        }
        else {
            learn();
        }
    }

    public void saveSettings(){
        SharedPreferences pref = getSharedPreferences(prefSettingAnimation, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        SharedPreferences prefSound = getSharedPreferences(prefSettingSound, MODE_PRIVATE);
        SharedPreferences.Editor editorSound = prefSound.edit();

        SharedPreferences prefPlayRecord = getSharedPreferences(prefSettingPlayRecord, MODE_PRIVATE);
        SharedPreferences.Editor editorPlayRecord = prefPlayRecord.edit();

        editor.putBoolean(prefSettingAnimation, swAnimation.isChecked());
        editor.commit();

        editorSound.putBoolean(prefSettingSound, swSound.isChecked());
        editorSound.commit();

        editorPlayRecord.putBoolean(prefSettingPlayRecord, swPlayRecord.isChecked());
        editorPlayRecord.commit();
    }

    public void getOverview(){
        SQLiteDatabase database = openOrCreateDatabase(databaseName, MODE_PRIVATE, null);
        SharedPreferences preferences = getSharedPreferences(prefIndex, MODE_PRIVATE);
        ArrayList<String> allWords = new ArrayList<String>();
        ArrayList<String> allTranslations = new ArrayList<String>();

        for(int i = 1; i < preferences.getInt(prefIndex, 1); i++){
            Cursor cursor = database.rawQuery("SELECT * FROM " + tableName + " WHERE id = '" + i + "'", null);
            cursor.moveToFirst();

            allWords.add(cursor.getString(1));
            allTranslations.add(cursor.getString(2));

            cursor.close();
        }

        database.close();
        generateGrid(allWords, allTranslations);
    }

    public void generateGrid(ArrayList<String> allWords, ArrayList<String> allTranslations){
        SharedPreferences preferences = getSharedPreferences(prefIndex, MODE_PRIVATE);
        scrollView = new ScrollView(this);
        gridLayout = new GridLayout(this);
        HorizontalScrollView scrollViewHorizontal = new HorizontalScrollView(this);

        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 700));

        gridLayout.setColumnCount(3);
        gridLayout.setRowCount(preferences.getInt(prefIndex, 1));
        gridLayout.setOrientation(GridLayout.HORIZONTAL);
        scrollViewHorizontal.addView(gridLayout);
        scrollView.addView(scrollViewHorizontal);
        lytOverview.addView(scrollView, 0);

        for(int i = 0; i < allWords.size(); i++){
            String fileName;
            File file;
            TextView tvId = new TextView(this);
            TextView tvWordOverview = new TextView(this);
            TextView tvTranslationOverview = new TextView(this);

            fileName = getExternalCacheDir().getAbsolutePath() + "/RecordAudio" + (i+1) + ".3gp";
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
        isOverview = true;
    }

    public void playAnimation(boolean animationCorrect){
        lytLearn.setVisibility(View.GONE);

        if(animationCorrect){
            avCorrect.setVisibility(View.VISIBLE);
            avCorrect.playAnimation();
        }
        else {
            avIncorrect.setVisibility(View.VISIBLE);
            avIncorrect.playAnimation();
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(avCorrect.getVisibility() != View.GONE || avIncorrect.getVisibility() != View.GONE){
                    lytLearn.setVisibility(View.VISIBLE);
                    avIncorrect.setVisibility(View.GONE);
                    avCorrect.setVisibility(View.GONE);

                    learn();
                }
            }
        }, 1500);
    }

    public void startRecording(){
        recorder = new MediaRecorder();

        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/RecordAudio" + etRecordId.getText().toString() + ".3gp";


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
    }

    public void stopRecording(){
        recorder.stop();

        recorder.release();
        recorder = null;
    }

    public void checkPermission(){
        if((ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)){
            allowed = true;
        } else {
            allowed = false;
            requestPermission();
        }
    }

    private void requestPermission () {
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(RECORD_AUDIO);
        }

        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE);
        }


        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(WRITE_EXTERNAL_STORAGE);
        }

    }

    public void makeChangeInDatabase () {
            SQLiteDatabase database = openOrCreateDatabase(databaseName, MODE_PRIVATE, null);

            if (isWord) {
                database.execSQL("UPDATE " + tableName + " SET word ='" + etChange.getText().toString() + "' WHERE id ='" + etChangeDataId.getText().toString() + "'");
            } else {
                database.execSQL("UPDATE " + tableName + " SET translation ='" + etChange.getText().toString() + "' WHERE id ='" + etChangeDataId.getText().toString() + "'");
            }

            etChange.setText("");
            etChangeDataId.setText("");

            database.close();
        }

    public boolean firstAppStart () {
            SharedPreferences preferences = getSharedPreferences(prefFirstStart, MODE_PRIVATE);

            if (preferences.getBoolean(prefFirstStart, true)) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(prefFirstStart, false);
                editor.commit();

                return true;
            } else {
                return false;
            }
        }

    public void createDatabase () {
            SQLiteDatabase database = openOrCreateDatabase(databaseName, MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE " + tableName + " (id INTEGER, word TEXT, translation TEXT)");

            database.close();
        }

    public void clickHome () {
            if (isOverview) {
                ViewGroup parent = (ViewGroup) scrollView.getParent();
                parent.removeView(scrollView);
                isOverview = false;
            }
            lytHome.setVisibility(View.VISIBLE);
            lytCorrectFalse.setVisibility(View.GONE);
            btnShow.setVisibility(View.VISIBLE);
            btnOverview.setVisibility(View.VISIBLE);
            btnSettings.setVisibility(View.VISIBLE);
            avIncorrect.setVisibility(View.GONE);
            avCorrect.setVisibility(View.GONE);
            lytSettings.setVisibility(View.GONE);
            lytAdd.setVisibility(View.GONE);
            lytLearn.setVisibility(View.GONE);
            lytOverview.setVisibility(View.GONE);
            lytSubmitChanges.setVisibility(View.GONE);
            lytId.setVisibility(View.GONE);
            lytTypeChange.setVisibility(View.GONE);
            btnChangeData.setVisibility(View.VISIBLE);
            btnRecordId.setVisibility(View.VISIBLE);
            btnStopRecording.setVisibility(View.GONE);
            lytRecordId.setVisibility(View.GONE);
            btnRecord.setVisibility(View.GONE);
            btnStopRecording.setVisibility(View.GONE);
            etChangeDataId.setText("");
            etChange.setText("");
            etWord.setText("");
            etChange.setText("");
            etRecordId.setText("");
            tvTranslation.setVisibility(View.GONE);
            btnBack.setVisibility(View.GONE);
        }

    @Override
    public void onClick (View view){
            switch (view.getId()) {
                case R.id.btnAdd:
                    lytHome.setVisibility(View.GONE);
                    lytAdd.setVisibility(View.VISIBLE);
                    btnOverview.setVisibility(View.GONE);
                    btnSettings.setVisibility(View.GONE);
                    btnBack.setVisibility(View.VISIBLE);
                    break;
                case R.id.btnSubmit:
                    if (etTranslation.getText().toString().length() > 0 && etWord.getText().toString().length() > 0) {
                        addWord(etWord.getText().toString(), etTranslation.getText().toString());
                        clickHome();
                        Toast.makeText(getApplicationContext(), "Added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Check your inputs", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case R.id.btnLearn:
                    lytLearn.setVisibility(View.VISIBLE);
                    lytHome.setVisibility(View.GONE);
                    btnOverview.setVisibility(View.GONE);
                    btnSettings.setVisibility(View.GONE);
                    btnBack.setVisibility(View.VISIBLE);
                    learn();
                    break;
                case R.id.btnShowTranslation:
                    SharedPreferences prefPlayRecord = getSharedPreferences(prefSettingPlayRecord, MODE_PRIVATE);
                    btnShow.setVisibility(View.GONE);
                    lytCorrectFalse.setVisibility(View.VISIBLE);
                    tvTranslation.setVisibility(View.VISIBLE);
                    if (!isSwitched && prefPlayRecord.getBoolean(prefSettingPlayRecord, true)) {
                        vocabSound = new MediaPlayer();

                        try {
                            vocabSound.setDataSource(getExternalCacheDir().getAbsolutePath() + "/RecordAudio" + currentId + ".3gp");
                            vocabSound.prepare();
                            vocabSound.start();
                        } catch (IOException e) {
                            Log.d("ERROR: ", "No recorded File found");
                        }
                    }
                    break;
                case R.id.btnCorrect:
                    btnShow.setVisibility(View.VISIBLE);
                    lytCorrectFalse.setVisibility(View.GONE);
                    tvTranslation.setVisibility(View.GONE);
                    correctTranslation();
                    break;
                case R.id.btnFalse:
                    btnShow.setVisibility(View.VISIBLE);
                    lytCorrectFalse.setVisibility(View.GONE);
                    tvTranslation.setVisibility(View.GONE);
                    falseTranslation();
                    break;

                case R.id.btnOverview:
                    lytHome.setVisibility(View.GONE);
                    btnOverview.setVisibility(View.GONE);
                    btnSettings.setVisibility(View.GONE);
                    btnBack.setVisibility(View.VISIBLE);
                    lytOverview.setVisibility(View.VISIBLE);
                    getOverview();
                    break;

                case R.id.btnBack:
                    clickHome();
                    break;
                case R.id.btnSettings:
                    SharedPreferences pref = getSharedPreferences(prefSettingAnimation, MODE_PRIVATE);
                    SharedPreferences prefSound = getSharedPreferences(prefSettingSound, MODE_PRIVATE);
                    SharedPreferences prefPlayRecord2 = getSharedPreferences(prefSettingPlayRecord, MODE_PRIVATE);
                    swSound.setChecked(prefSound.getBoolean(prefSettingSound, true));
                    swAnimation.setChecked(pref.getBoolean(prefSettingAnimation, true));
                    swPlayRecord.setChecked(prefPlayRecord2.getBoolean(prefSettingPlayRecord, true));
                    lytSettings.setVisibility(View.VISIBLE);
                    lytHome.setVisibility(View.GONE);
                    btnSettings.setVisibility(View.GONE);
                    btnOverview.setVisibility(View.GONE);
                    btnBack.setVisibility(View.VISIBLE);
                    break;
                case R.id.swRecordedLines:
                case R.id.swAnimation:
                case R.id.swSound:
                    saveSettings();
                    break;
                case R.id.btnChangeData:
                    btnChangeData.setVisibility(View.GONE);
                    btnRecordId.setVisibility(View.GONE);
                    lytId.setVisibility(View.VISIBLE);
                    break;
                case R.id.btnSubmitId:
                    SharedPreferences preferences = getSharedPreferences(prefIndex, MODE_PRIVATE);
                    if (Integer.parseInt(etChangeDataId.getText().toString()) > 0 &&
                            Integer.parseInt(etChangeDataId.getText().toString()) < preferences.getInt(prefIndex, 0)) {
                        lytTypeChange.setVisibility(View.VISIBLE);
                        lytId.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getApplicationContext(), "Check your inputs", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btnChangeTranslation:
                    lytTypeChange.setVisibility(View.GONE);
                    isWord = false;
                    lytSubmitChanges.setVisibility(View.VISIBLE);
                    lytTypeChange.setVisibility(View.GONE);
                    break;
                case R.id.btnChangeWord:
                    lytTypeChange.setVisibility(View.GONE);
                    isWord = true;
                    lytSubmitChanges.setVisibility(View.VISIBLE);
                    lytTypeChange.setVisibility(View.GONE);
                    break;
                case R.id.btnSubmitChange:
                    makeChangeInDatabase();
                    Toast.makeText(getApplicationContext(), "Changed Successfully", Toast.LENGTH_SHORT).show();
                    clickHome();
                    lytHome.setVisibility(View.GONE);
                    btnOverview.setVisibility(View.GONE);
                    btnSettings.setVisibility(View.GONE);
                    lytOverview.setVisibility(View.VISIBLE);
                    btnBack.setVisibility(View.VISIBLE);
                    getOverview();
                    break;
                case R.id.btnRecordId:
                    checkPermission();

                    if(allowed){
                        btnChangeData.setVisibility(View.GONE);
                        btnRecordId.setVisibility(View.GONE);
                        lytRecordId.setVisibility(View.VISIBLE);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Permition requierd", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case R.id.btnSubmitRecordId:
                    SharedPreferences preferencesRecord = getSharedPreferences(prefIndex, MODE_PRIVATE);
                    if (Integer.parseInt(etRecordId.getText().toString()) > 0 &&
                            Integer.parseInt(etRecordId.getText().toString()) < preferencesRecord.getInt(prefIndex, 0)) {
                        btnRecord.setVisibility(View.VISIBLE);
                        lytRecordId.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getApplicationContext(), "Check your inputs", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btnRecord:
                    btnRecord.setVisibility(View.GONE);
                    btnStopRecording.setVisibility(View.VISIBLE);
                    btnBack.setVisibility(View.GONE);
                    btnChangeData.setVisibility(View.GONE);
                    startRecording();
                    break;
                case R.id.btnStopRecording:
                    stopRecording();
                    Toast.makeText(getApplicationContext(), "Successfully Saved", Toast.LENGTH_SHORT).show();
                    clickHome();
                    break;
            }
        }
    }