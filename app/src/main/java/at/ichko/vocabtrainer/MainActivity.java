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

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Calendar today = Calendar.getInstance();
        String strToday = dateFormat.format(today.getTime());

        if(!getLastLogin().equals(strToday)){
            Notifier notifier = new Notifier(getApplicationContext());
            notifier.build();
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
        SharedPreferences preferences = getSharedPreferences(Constants.PREF_FIRST_START, MODE_PRIVATE);

        if (preferences.getBoolean(Constants.PREF_FIRST_START, true)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.PREF_FIRST_START, false);
            editor.commit();

            return true;
        } else {
            return false;
        }
    }

    public void createDatabase () {
        SQLiteDatabase database = openOrCreateDatabase(Constants.DATABASE_NAME, MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE " + Constants.TABLE_NAME + " (id INTEGER, word TEXT, translation TEXT, strength TEXT)");

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
        SharedPreferences lastLogin = getSharedPreferences(Constants.PREF_LAST_LOGIN, MODE_PRIVATE);
        return lastLogin.getString(Constants.PREF_LAST_LOGIN, null);
    }

    public void increaseStreakDays(){
        SharedPreferences preferences = getSharedPreferences(Constants.PREF_STREAK, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.PREF_STREAK, preferences.getInt(Constants.PREF_STREAK, 0) + 1);
        editor.commit();
    }

    public void resetStreak(){
        SharedPreferences preferences = getSharedPreferences(Constants.PREF_STREAK, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Constants.PREF_STREAK, 1);
        editor.commit();
    }

    public void setLastLoginDate(String date){
        SharedPreferences lastLogin = getSharedPreferences(Constants.PREF_LAST_LOGIN, MODE_PRIVATE);
        SharedPreferences.Editor editor = lastLogin.edit();
        editor.putString(Constants.PREF_LAST_LOGIN, date);
        editor.commit();
    }

}
