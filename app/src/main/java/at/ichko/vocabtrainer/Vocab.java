package at.ichko.vocabtrainer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class Vocab {

    private Context context;
    private List<Integer> lowVocab;
    private List<Integer> midVocab;
    private List<Integer> strongVocab;
    private List<Integer> falseVocab;

    public Vocab(Context context){
        this.context = context;
        this.lowVocab = new ArrayList<>();
        this.midVocab = new ArrayList<>();
        this.strongVocab = new ArrayList<>();
        this.falseVocab = new ArrayList<>();
    }

    public int getRandom() {
        fillUpArrays();

        boolean foundNewWord = false;
        int id = 0;

        while (!foundNewWord) {
            double random = Math.random();
            if (!strongVocab.isEmpty() && random < 0.03) {
                id = strongVocab.get((int) (Math.random() * (strongVocab.size() - 1)));
                foundNewWord = true;
            } else if(!midVocab.isEmpty() && random < 0.15){
                id = midVocab.get((int) (Math.random() * (midVocab.size() - 1)));
                foundNewWord = true;
            } else if(!lowVocab.isEmpty() && random < 0.6){
                id = lowVocab.get((int) (Math.random() * (lowVocab.size() - 1)));
                foundNewWord = true;
            } else if(!falseVocab.isEmpty()){
                id = falseVocab.get((int) (Math.random() * (falseVocab.size() - 1)));
                foundNewWord = true;
            }
        }

        return id;
    }

    private void fillUpArrays(){
        SQLiteDatabase database = context.openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);
        Table table = new Table(context);

        lowVocab.clear();
        midVocab.clear();
        strongVocab.clear();
        falseVocab.clear();
        Log.d("ALARM: " , table.getSize() +"");
        Cursor cursor = database.rawQuery("SELECT * FROM " + table.get(table.getTableIndex()) + " WHERE strength='" + VocabStrength.LOW + "'", null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            lowVocab.add(cursor.getInt(0));
            cursor.moveToNext();
        }

        cursor = null;
        cursor = database.rawQuery("SELECT * FROM " + table.get(table.getTableIndex()) + " WHERE strength='" + VocabStrength.MEDIUM + "'", null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            midVocab.add(cursor.getInt(0));
            cursor.moveToNext();
        }

        cursor = null;
        cursor = database.rawQuery("SELECT * FROM " + table.get(table.getTableIndex()) + " WHERE strength='" + VocabStrength.STRONG + "'", null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            strongVocab.add(cursor.getInt(0));
            cursor.moveToNext();
        }

        cursor = null;
        cursor = database.rawQuery("SELECT * FROM " + table.get(table.getTableIndex()) + " WHERE strength='" + VocabStrength.FALSE + "'", null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            falseVocab.add(cursor.getInt(0));
            cursor.moveToNext();
        }

        cursor.close();
        database.close();
    }

    public void setStrength(VocabStrength strength, int id){
        SQLiteDatabase database = context.openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);
        Table table = new Table(context);
        String currentTable = table.get(table.getTableIndex());

        database.execSQL("UPDATE " + currentTable + " SET strength ='" + strength + "' WHERE id ='" + id + "'");

        database.close();
    }

    public VocabStrength getStrength(int id){
        SQLiteDatabase database = context.openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);
        Table table = new Table(context);
        StringBuilder value = new StringBuilder();
        Cursor cursor = database.rawQuery("SELECT * FROM " + table.get(table.getTableIndex()) + " WHERE id ='" + id + "'", null);
        cursor.moveToFirst();

        value.append(cursor.getString(3));

        cursor.close();
        database.close();

        return VocabStrength.valueOf(value.toString());
    }
}
