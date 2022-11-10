package at.ichko.vocabtrainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class FragmentOverview extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    Button btnEdit;
    Button btnSubmitId;
    Button btnSubmit;

    EditText etId;
    EditText etWord;
    EditText etTranslation;

    ConstraintLayout lytMid;

    Spinner spSwitchLanguage;

    NestedScrollView scrollView;
    GridLayout gridLayout;

    ArrayList<String> tableNames = new ArrayList<>();

    final String databaseName = "languagedatabase.db";
    final String prefTableId = "tableid";

    public FragmentOverview() {

    }

    public static FragmentOverview newInstance(String param1, String param2) {
        FragmentOverview fragment = new FragmentOverview();
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
        View rootView = inflater.inflate(R.layout.fragment_overview, container, false);

        lytMid = rootView.findViewById(R.id.lytMidOverview);

        btnEdit = rootView.findViewById(R.id.btnEdit);
        btnSubmitId = rootView.findViewById(R.id.btnSubmitId);
        btnSubmit = rootView.findViewById(R.id.btnSubmit);

        etId = rootView.findViewById(R.id.etIdEdit);
        etTranslation = rootView.findViewById(R.id.etTranslationEdit);
        etWord = rootView.findViewById(R.id.etWordEdit);

        spSwitchLanguage = rootView.findViewById(R.id.spLanguagesOverview);

        btnEdit.setOnClickListener(this);
        btnSubmitId.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

        getTableNames();
        refreshDropdown();
        getOverview();

        spSwitchLanguage.setOnItemSelectedListener(this);

        return rootView;
    }

    public void getOverview(){
        SQLiteDatabase database = getActivity().openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        SharedPreferences preferences = getActivity().getSharedPreferences(prefTableId, Context.MODE_PRIVATE);
        int count = 0;
        Cursor cursorLength = null;

        try {
            cursorLength = database.rawQuery("SELECT * FROM " + tableNames.get(preferences.getInt(prefTableId, 0)), null);
            cursorLength.moveToLast();

            count = cursorLength.getInt(0) + 1;
        } catch (CursorIndexOutOfBoundsException ex){
            Log.d("ERROR: ", "Noch keine Wörter vorhanden");
            count = 1;
        }

        ArrayList<String> allWords = new ArrayList<String>();
        ArrayList<String> allTranslations = new ArrayList<String>();

        for(int i = 1; i < count ; i++){
            Cursor cursor = database.rawQuery("SELECT * FROM " + tableNames.get(preferences.getInt(prefTableId, 0)) + " WHERE id = '" + i + "'", null);
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

        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 850));


        gridLayout.setColumnCount(3);
        gridLayout.setRowCount(getTableSize());
        gridLayout.setOrientation(GridLayout.HORIZONTAL);
        scrollViewHorizontal.addView(gridLayout);
        scrollView.addView(scrollViewHorizontal);
        lytMid.addView(scrollView, 0);

        for(int i = 0; i < allWords.size(); i++){
            TextView tvId = new TextView(getActivity());
            TextView tvWordOverview = new TextView(getActivity());
            TextView tvTranslationOverview = new TextView(getActivity());

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

            gridLayout.addView(tvId);
            gridLayout.addView(tvWordOverview);
            gridLayout.addView(tvTranslationOverview);
        }

        gridLayout.setVisibility(View.VISIBLE);
    }

    public void makeChangeInDatabase () {
        SQLiteDatabase database = getActivity().openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        SharedPreferences preferences = getActivity().getSharedPreferences(prefTableId, Context.MODE_PRIVATE);

        database.execSQL("UPDATE " + tableNames.get(preferences.getInt(prefTableId, 0)) + " SET word ='" + etWord.getText().toString() + "' WHERE id ='" + etId.getText().toString() + "'");
        database.execSQL("UPDATE " + tableNames.get(preferences.getInt(prefTableId, 0)) + " SET translation ='" + etTranslation.getText().toString() + "' WHERE id ='" + etId.getText().toString() + "'");

        etId.setText("");
        etTranslation.setText("");
        etWord.setText("");

        database.close();

        ViewGroup parent = (ViewGroup) scrollView.getParent();
        parent.removeView(scrollView);

        getOverview();
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
        spSwitchLanguage.setAdapter(adapter);

        spSwitchLanguage.setSelection(getTableIndex());
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btnEdit:
                btnEdit.setVisibility(View.GONE);
                etId.setVisibility(View.VISIBLE);
                btnSubmitId.setVisibility(View.VISIBLE);
                break;
            case R.id.btnSubmitId:
                SQLiteDatabase database = getActivity().openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
                SharedPreferences preferences = getActivity().getSharedPreferences(prefTableId, Context.MODE_PRIVATE);

                try {
                    if (Integer.parseInt(etId.getText().toString()) > 0 &&
                            Integer.parseInt(etId.getText().toString()) < getTableSize()) {
                        Cursor cursor = database.rawQuery("SELECT * FROM " + tableNames.get(preferences.getInt(prefTableId, 0)) + " WHERE id ='" + etId.getText().toString() + "'", null);
                        cursor.moveToFirst();

                        etWord.setText(cursor.getString(1));
                        etTranslation.setText(cursor.getString(2));

                        etId.setVisibility(View.GONE);
                        btnSubmitId.setVisibility(View.GONE);

                        etTranslation.setVisibility(View.VISIBLE);
                        etWord.setVisibility(View.VISIBLE);
                        btnSubmit.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Check your inputs", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex){
                    Toast.makeText(getActivity().getApplicationContext(), "Check your inputs", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnSubmit:
                makeChangeInDatabase();
                Toast.makeText(getActivity().getApplicationContext(), "Changed Successfully", Toast.LENGTH_SHORT).show();
                btnEdit.setVisibility(View.VISIBLE);
                etTranslation.setVisibility(View.GONE);
                etWord.setVisibility(View.GONE);
                btnSubmit.setVisibility(View.GONE);
                break;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        SharedPreferences prefTable = getActivity().getSharedPreferences(prefTableId, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefTable.edit();

        editor.putInt(prefTableId, i);
        editor.commit();

        spSwitchLanguage.setSelection(i);

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