package at.ichko.vocabtrainer;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

public class Keyboard {

    private Context context;
    private Activity activity;

    public Keyboard(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
    }

    public void hide() throws Exception{
        InputMethodManager imm = (InputMethodManager)context.getSystemService(context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }
}
