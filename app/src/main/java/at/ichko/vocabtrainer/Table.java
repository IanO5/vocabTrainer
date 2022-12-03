package at.ichko.vocabtrainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class Table {

    private Context context;
    private List<String> tableNames = new ArrayList<>();

    public Table(Context context){
        this.context = context;
        getTableNames();
    }

    public List<String> getTableNames(){
        tableNames.clear();
        SQLiteDatabase database = context.openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT IN ('android_metadata', 'sqlite_sequence', 'room_master_table') ",null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            tableNames.add(cursor.getString(0));
            cursor.moveToNext();
        }

        cursor.close();
        database.close();

        return tableNames;
    }

    public String get(int num){
        return tableNames.get(num);
    }

    public int size(){
        return tableNames.size();
    }

    public int getSize(){
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_TABLE_ID, Context.MODE_PRIVATE);
        SQLiteDatabase database = context.openOrCreateDatabase(Constants.DATABASE_NAME, Context.MODE_PRIVATE, null);
        int i = 0;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM " + tableNames.get(preferences.getInt(Constants.PREF_TABLE_ID, 0)), null);
            cursor.moveToLast();

            i = cursor.getInt(0) + 1;
        } catch (CursorIndexOutOfBoundsException ex){
            Log.d("ERROR: ", "No words found yet");
            i = 0;
        }

        cursor.close();
        database.close();

        return i;
    }

    public Integer getTableIndex(){
        SharedPreferences preferences = context.getSharedPreferences(Constants.PREF_TABLE_ID, Context.MODE_PRIVATE);
        return preferences.getInt(Constants.PREF_TABLE_ID, 0);
    }
}
