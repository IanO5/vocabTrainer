package at.ichko.vocabtrainer;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class FragmentAdd extends Fragment implements View.OnClickListener {

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

    Keyboard keyboard;

    LanguageSpinner spinner;
    Table table;

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

        table = new Table(getActivity());
        spinner = new LanguageSpinner(spSelectedLanguage, getActivity(), () -> refreshVocabNr(), () -> refreshVocabNr());
        keyboard = new Keyboard(getContext(), getActivity());

        spinner.refresh();
        refreshVocabNr();

        return rootView;
    }

    public void addWord(String word, String translation){
        SQLiteDatabase database = getActivity().openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);
        int i = 0;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM " + table.get(table.getTableIndex()), null);
            cursor.moveToLast();

            i = cursor.getInt(0) + 1;
        } catch (CursorIndexOutOfBoundsException ex){
            Log.d("ERROR: ", "Noch keine Wörter vorhanden");
            i = 1;
        }

        database.execSQL("INSERT INTO " + table.get(table.getTableIndex()) + " VALUES('" + i + "', '" + word + "', '" + translation + "', '" + VocabStrength.LOW + "')");

        refreshVocabNr();

        cursor.close();
        database.close();
    }

    public void addLanguage(String languageName){
        SQLiteDatabase database = getActivity().openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE " + languageName + " (id INTEGER, word TEXT, translation TEXT, strength TEXT)");

        database.close();
    }

    public void refreshVocabNr(){
        table.getTableNames();
        SQLiteDatabase database = getActivity().openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);
        try {
            Cursor cursor = database.rawQuery("SELECT * FROM " + table.get(table.getTableIndex()), null);
            cursor.moveToLast();

            tvVocabNr.setText("Vocab Nr.: " + (cursor.getInt(0) + 1));

            cursor.close();
        } catch(CursorIndexOutOfBoundsException ex){
            Log.d("ALARM: ", "Noch keine Wörter vorhanden");
            tvVocabNr.setText("Vocab Nr.: 1");
        }

        database.close();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnAddToLibrary:
                if (etTranslation.getText().toString().length() > 0 && etWord.getText().toString().length() > 0) {
                    addWord(etWord.getText().toString(), etTranslation.getText().toString());
                    Toast.makeText(getActivity(), "Added successfully", Toast.LENGTH_SHORT).show();

                    try {
                        keyboard.hide();
                    } catch (Exception ex) {
                        Log.d("ALARM: ", "Exception triggerd! Could not open keyboard.");
                    }

                    etWord.setText("");
                    etTranslation.setText("");
                    refreshVocabNr();
                } else {
                    Toast.makeText(getActivity(), "Check your inputs", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btnAddNewLanguage:
                boolean existing = false;

                for(int i = 0; i < table.size(); i++){
                    if(table.get(i).equalsIgnoreCase(etNewLanguage.getText().toString())){
                        existing = true;
                        break;
                    }
                }

                if(etNewLanguage.getText().toString().length() > 0 && !existing){
                    addLanguage(etNewLanguage.getText().toString());
                    Toast.makeText(getActivity(), "Added successfully", Toast.LENGTH_SHORT).show();

                    try {
                        keyboard.hide();
                    } catch (Exception ex) {
                        Log.d("ALARM: ", "Exception triggerd! Could not open keyboard.");
                    }

                    etNewLanguage.setText("");
                    spinner.refresh();
                }
                else {
                    Toast.makeText(getActivity(), "Inputs invalid or Language already existing", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}