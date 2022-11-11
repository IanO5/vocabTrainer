package at.ichko.vocabtrainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

import java.io.File;
import java.util.ArrayList;

public class Overview {

    private final String databaseName = "languagedatabase.db";
    private final String prefTableId = "tableid";
    private Context context;
    private Table table;

    private NestedScrollView scrollView;
    private GridLayout gridLayout;
    private ConstraintLayout lytParent;

    public Overview(ConstraintLayout lytParent, Context context){
        this.context = context;
        this.lytParent = lytParent;
        table = new Table(this.context);
        table.getTableNames();
    }

    public void getOverview(boolean record){
        SQLiteDatabase database = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        SharedPreferences preferences = context.getSharedPreferences(prefTableId, Context.MODE_PRIVATE);
        int count = 0;
        Cursor cursorLength = null;

        try {
            cursorLength = database.rawQuery("SELECT * FROM " + table.get(preferences.getInt(prefTableId, 0)), null);
            cursorLength.moveToLast();

            count = cursorLength.getInt(0) + 1;
        } catch (CursorIndexOutOfBoundsException ex){
            Log.d("ERROR: ", "Noch keine Wörter vorhanden");
            count = 1;
        }

        ArrayList<String> allWords = new ArrayList<>();
        ArrayList<String> allTranslations = new ArrayList<>();
        ArrayList<String> allStrengths = new ArrayList<>();

        for(int i = 1; i < count ; i++){
            Cursor cursor = database.rawQuery("SELECT * FROM " + table.get(preferences.getInt(prefTableId, 0)) + " WHERE id = '" + i + "'", null);
            cursor.moveToFirst();

            allWords.add(cursor.getString(1));
            allTranslations.add(cursor.getString(2));
            allStrengths.add(cursor.getString(3));

            cursor.close();
        }

        database.close();
        generateGrid(allWords, allTranslations, allStrengths, record);
    }

    private void generateGrid(ArrayList<String> allWords, ArrayList<String> allTranslations, ArrayList<String> allStrengths, boolean record){
        String fileName;
        File file = null;
        scrollView = new NestedScrollView(context);
        gridLayout = new GridLayout(context);
        HorizontalScrollView scrollViewHorizontal = new HorizontalScrollView(context);

        scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 850));

        if(record)
            gridLayout.setColumnCount(3);
        else
            gridLayout.setColumnCount(4);

        gridLayout.setRowCount(table.getSize());
        gridLayout.setOrientation(GridLayout.HORIZONTAL);
        scrollViewHorizontal.addView(gridLayout);
        scrollView.addView(scrollViewHorizontal);
        lytParent.addView(scrollView, 0);

        for(int i = 0; i < allWords.size(); i++){
            TextView tvId = new TextView(context);
            TextView tvWordOverview = new TextView(context);
            TextView tvTranslationOverview = new TextView(context);
            TextView tvStrength = new TextView(context);

            if(record){
                fileName = context.getExternalCacheDir().getAbsolutePath() + "/RecordAudio" + table.get(table.getTableIndex()) + (i+1) + ".3gp";
                file = new File(fileName);
            }

            tvId.setTextSize(18);
            tvWordOverview.setTextSize(18);
            tvTranslationOverview.setTextSize(18);
            tvStrength.setTextSize(18);

            tvId.setPadding(20, 0, 15, 0);
            tvTranslationOverview.setPadding(20,0,10,20);
            tvWordOverview.setPadding(20, 0,0,0);
            tvWordOverview.setPadding(30, 0,0,0);

            tvId.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvWordOverview.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvTranslationOverview.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tvStrength.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            tvId.setText(i+1 + "");
            tvWordOverview.setText(allWords.get(i));
            tvTranslationOverview.setText(allTranslations.get(i));

            if(!record){
                tvStrength.setText(allStrengths.get(i));
                VocabStrength vocabStrength = VocabStrength.valueOf(allStrengths.get(i));
                switch (vocabStrength){
                    case MEDIUM:
                        tvStrength.setTextColor(Color.YELLOW);
                        break;
                    case LOW:
                    case FALSE:
                        tvStrength.setTextColor(Color.RED);
                        break;
                    case STRONG:
                        tvStrength.setTextColor(Color.GREEN);
                        break;
                }
            }

            if(record){
                if(file.exists()){
                    tvId.setBackgroundColor(Color.LTGRAY);
                }
            }

            gridLayout.addView(tvId);
            gridLayout.addView(tvWordOverview);
            gridLayout.addView(tvTranslationOverview);
            if(!record){
                gridLayout.addView(tvStrength);
            }
        }

        gridLayout.setVisibility(View.VISIBLE);
    }

    public void deleteScrollView(){
        ViewGroup parent = (ViewGroup) scrollView.getParent();
        parent.removeView(scrollView);
    }

}
