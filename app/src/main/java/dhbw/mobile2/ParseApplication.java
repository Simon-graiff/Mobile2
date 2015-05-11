package dhbw.mobile2;

/**
 * Created by Christian on 11.05.15.
 */

import android.app.Application;

import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;

public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "XtayL1TkW4GF7OyZPikWIRvdWlr4LrnOs1OjsFCO", "Z29mPZXCfSFlJfk8UcqG6AMouPKgckuzE7sfl2XV");
        FacebookSdk.sdkInitialize(getApplicationContext());
        ParseFacebookUtils.initialize(getApplicationContext());
    }
}