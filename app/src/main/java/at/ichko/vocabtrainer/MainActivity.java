package at.ichko.vocabtrainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.allyants.notifyme.NotifyMe;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity{
    BottomNavigationView navigationView;

    final String prefFirstStart = "firststart";
    final String databaseName = "languagedatabase.db";
    final String tableName = "firsttable";
    final String prefLastLogin = "lastlogin";
    final String prefStreak = "streakdays";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation_bar);
        navigationView.setBackground(null);
        navigationView.getMenu().getItem(2).setChecked(true);

        if(firstAppStart()){
            createDatabase();
        }

        getStreak();

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new FragmentLearn()).commit();
        navigationView.setOnItemSelectedListener(item -> {
            Fragment temp = null;
            switch (item.getItemId())
            {
                case R.id.mAdd: temp = new FragmentAdd();
                    break;
                case R.id.mOverview: temp = new FragmentOverview();
                    break;
                case R.id.mLearn: temp = new FragmentLearn();
                    break;
                case R.id.mRecord: temp = new FragmentRecord();
                    break;
                case R.id.mSettings: temp = new FragmentSettings();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,temp).commit();
            return true;
        });
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
        database.execSQL("CREATE TABLE " + tableName + " (id INTEGER, word TEXT, translation TEXT, strength TEXT)");

        database.close();
    }

    public void getStreak(){
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar calendarYesterday = Calendar.getInstance();
        Calendar calendarToday = Calendar.getInstance();
        calendarYesterday.add(Calendar.DATE, -1);
        String today = dateFormat.format(calendarToday.getTime());
        String yesterday = dateFormat.format(calendarYesterday.getTime());
        String lastLogin = getLastLogin();

        if(lastLogin == null) {
            increaseStreakDays();
        } else {
            if (lastLogin.equals(today)) {
                //do nothing
            } else if (lastLogin.equals(yesterday)) {
                increaseStreakDays();
            } else {
                resetStreak();
            }
        }

        setLastLoginDate(today);
    }

    public String getLastLogin(){
        SharedPreferences lastLogin = getSharedPreferences(prefLastLogin, MODE_PRIVATE);
        return lastLogin.getString(prefLastLogin, null);
    }

    public void increaseStreakDays(){
        SharedPreferences preferences = getSharedPreferences(prefStreak, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(prefStreak, preferences.getInt(prefStreak, 0) + 1);
        editor.commit();
    }

    public void resetStreak(){
        SharedPreferences preferences = getSharedPreferences(prefStreak, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(prefStreak, 1);
        editor.commit();
    }

    public void setLastLoginDate(String date){
        SharedPreferences lastLogin = getSharedPreferences(prefLastLogin, MODE_PRIVATE);
        SharedPreferences.Editor editor = lastLogin.edit();
        editor.putString(prefLastLogin, date);
        editor.commit();
    }

}
