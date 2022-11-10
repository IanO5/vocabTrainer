package at.ichko.vocabtrainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class FragmentAdd extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    TextView tvVocabNr;

    EditText etWord;
    EditText etTranslation;
    EditText etNewLanguage;

    Button btnAdd;
    Button btnAddLanguage;

    Spinner spSelectedLanguage;

    SharedPreferences preferences;

    ArrayList<String> tableNames = new ArrayList<>();

    final String databaseName = "languagedatabase.db";
    final String tableName = "firsttable";
    final String prefIndex = "indexpref";
    final String prefTableId = "tableid";

    public FragmentAdd() {

    }

    public static FragmentAdd newInstance(String param1, String param2) {
        FragmentAdd fragment = new FragmentAdd();
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
        View rootView = inflater.inflate(R.layout.fragment_add, container, false);

        tvVocabNr = rootView.findViewById(R.id.tvVoabNrAdd);

        etWord = rootView.findViewById(R.id.etWord);
        etTranslation = rootView.findViewById(R.id.etTranslation);
        etNewLanguage = rootView.findViewById(R.id.etNewLanguageName);

        btnAdd = rootView.findViewById(R.id.btnAddToLibrary);
        btnAddLanguage = rootView.findViewById(R.id.btnAddNewLanguage);

        spSelectedLanguage = rootView.findViewById(R.id.spLanguagesAdd);

        btnAdd.setOnClickListener(this);
        btnAddLanguage.setOnClickListener(this);

        spSelectedLanguage.setOnItemSelectedListener(this);

        refreshDropdown();
        refreshVocabNr();

        return rootView;
    }

    public void addWord(String word, String translation){
        SQLiteDatabase database = getActivity().openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        int i = 0;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM " + tableNames.get(getTableIndex()), null);
            cursor.moveToLast();

            i = cursor.getInt(0) + 1;
        } catch (CursorIndexOutOfBoundsException ex){
            Log.d("ERROR: ", "Noch keine Wörter vorhanden");
            i = 1;
        }

        database.execSQL("INSERT INTO " + tableNames.get(getTableIndex()) + " VALUES('" + i + "', '" + word + "', '" + translation + "')");

        refreshVocabNr();

        cursor.close();
        database.close();
    }

    public void addLanguage(String languageName){
        SQLiteDatabase database = getActivity().openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE " + languageName + " (id INTEGER, word TEXT, translation TEXT)");

        database.close();
    }

    public void refreshDropdown(){
        getTableNames();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, tableNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSelectedLanguage.setAdapter(adapter);

        spSelectedLanguage.setSelection(getTableIndex());

    }

    public void refreshVocabNr(){
        SQLiteDatabase database = getActivity().openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM " + tableNames.get(getTableIndex()), null);
            cursor.moveToLast();

            tvVocabNr.setText("Vocab Nr.: " + (cursor.getInt(0) + 1));

            cursor.close();
        } catch(CursorIndexOutOfBoundsException ex){
            Log.d("ALARM: ", "Noch keine Wörter vorhanden");
            tvVocabNr.setText("Vocab Nr.: 1");
        }

        database.close();
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

    public Integer getTableIndex(){
        SharedPreferences preferences = getActivity().getSharedPreferences(prefTableId, Context.MODE_PRIVATE);
        return preferences.getInt(prefTableId, 0);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnAddToLibrary:
                if (etTranslation.getText().toString().length() > 0 && etWord.getText().toString().length() > 0) {
                    addWord(etWord.getText().toString(), etTranslation.getText().toString());
                    Toast.makeText(getActivity(), "Added successfully", Toast.LENGTH_SHORT).show();
                    etWord.setText("");
                    etTranslation.setText("");
                    refreshVocabNr();
                } else {
                    Toast.makeText(getActivity(), "Check your inputs", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btnAddNewLanguage:
                boolean existing = false;

                for(int i = 0; i < tableNames.size(); i++){
                    if(tableNames.get(i).equalsIgnoreCase(etNewLanguage.getText().toString())){
                        existing = true;
                        break;
                    }
                }

                if(etNewLanguage.getText().toString().length() > 0 && !existing){
                    addLanguage(etNewLanguage.getText().toString());
                    Toast.makeText(getActivity(), "Added successfully", Toast.LENGTH_SHORT).show();
                    etNewLanguage.setText("");
                    refreshDropdown();
                }
                else {
                    Toast.makeText(getActivity(), "Inputs invalid or Language already existing", Toast.LENGTH_SHORT).show();
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

        spSelectedLanguage.setSelection(i);
        refreshVocabNr();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        refreshVocabNr();
    }
}