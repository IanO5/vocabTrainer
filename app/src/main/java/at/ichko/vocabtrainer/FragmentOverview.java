package at.ichko.vocabtrainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;

public class FragmentOverview extends Fragment implements View.OnClickListener {

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

    Keyboard keyboard;

    Overview overview;
    Table table;
    LanguageSpinner spinner;

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

        overview = new Overview(lytMid, getActivity());
        table = new Table(getActivity());
        keyboard = new Keyboard(getContext(), getActivity());

        ItemNothingSelectExecution ex = new ItemNothingSelectExecution() {
            @Override
            public void nothingSelectedExecute() {
                overview.deleteScrollView();
                overview.getOverview(false);
            }
        };

        spinner = new LanguageSpinner(spSwitchLanguage, getActivity(), () -> {
            overview.deleteScrollView();
            overview.getOverview(false);
        }, ex);

        spinner.refresh();
        overview.getOverview(false);

        return rootView;
    }

    public void makeChangeInDatabase () {
        SQLiteDatabase database = getActivity().openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.PREF_TABLE_ID, Context.MODE_PRIVATE);

        database.execSQL("UPDATE " + table.get(table.getTableIndex()) + " SET word ='" + etWord.getText().toString() + "' WHERE id ='" + etId.getText().toString() + "'");
        database.execSQL("UPDATE " + table.get(table.getTableIndex()) + " SET translation ='" + etTranslation.getText().toString() + "' WHERE id ='" + etId.getText().toString() + "'");

        etId.setText("");
        etTranslation.setText("");
        etWord.setText("");

        database.close();

        overview.deleteScrollView();

        overview.getOverview(false);
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
                SQLiteDatabase database = getActivity().openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);

                try {
                    keyboard.hide();
                } catch (Exception ex) {
                    Log.d("ALARM: ", "Exception triggerd! Could not open keyboard.");
                }

                try {
                    if (Integer.parseInt(etId.getText().toString()) > 0 &&
                            Integer.parseInt(etId.getText().toString()) < table.getSize()) {
                        Cursor cursor = database.rawQuery("SELECT * FROM " + table.get(table.getTableIndex()) + " WHERE id ='" + etId.getText().toString() + "'", null);
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

                try {
                    keyboard.hide();
                } catch (Exception ex) {
                    Log.d("ALARM: ", "Exception triggerd! Could not open keyboard.");
                }

                btnEdit.setVisibility(View.VISIBLE);
                etTranslation.setVisibility(View.GONE);
                etWord.setVisibility(View.GONE);
                btnSubmit.setVisibility(View.GONE);
                break;
        }
    }
}