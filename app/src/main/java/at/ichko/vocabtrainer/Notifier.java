package at.ichko.vocabtrainer;

import android.content.Context;
import android.content.Intent;

import com.allyants.notifyme.NotifyMe;

public class Notifier {

    private Context context;
    private NotifyMe.Builder notifyMe;

    public Notifier(Context context){
        this.context = context;
        prepare();
    }

    private void prepare(){
        notifyMe = new NotifyMe.Builder(context);

        Intent intent = context.getPackageManager().getLaunchIntentForPackage("at.ichko.vocabtrainer");

        notifyMe.title("Come back learning!");
        notifyMe.content("Learn some new words today and reach your goals.");
        notifyMe.delay(1000 * 60 * 60 * 12); // 12h delay
        notifyMe.rrule("FREQ=MINUTELY;INTERVAL=720;COUNT=2");
        notifyMe.small_icon(R.raw.alert);//Icon resource by ID
        notifyMe.addAction(intent, "Open App");
    }

    public void build(){
        notifyMe.build();
    }
}
