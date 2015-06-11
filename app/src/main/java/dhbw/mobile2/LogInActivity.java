package dhbw.mobile2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LogInActivity extends ActionBarActivity {


    private boolean errorOccured=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //Hide the ActionBar for the Login Screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    //Handles the callback from the FacebookSDK
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }


    //Is called by the User if he hits the Button FacebookLogin
    public void linkFacebookLogIn(View view) {
        final List<String> permissions = Arrays.asList("public_profile", "email");
              ParseFacebookUtils.logInWithReadPermissionsInBackground(LogInActivity.this, permissions, new LogInCallback() {
                  @Override
                  public void done(final ParseUser user, ParseException err) {
                      if (user == null) {
                          Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                          Toast.makeText(getApplicationContext(), "Please Log-in with Facebook!", Toast.LENGTH_LONG).show();
                          ParseUser.logOut();
                      } else if (user.isNew()) {
                          Log.d("MyApp", "User signed up and logged in through Facebook!");
                          ProgressDialog.show(LogInActivity.this, "Creating Account", "Please wait.."); //Show loading dialog until data has been pulled from parse
                          initializeNewUser();
                          ParseUser.getCurrentUser().pinInBackground();
                      } else {
                          ProgressDialog.show(LogInActivity.this, "Loading", "Please wait..");
                          Log.d("MyApp", "User logged in with Facebook!");
                          redirectToMainScreen();

                          ParseUser.getCurrentUser().pinInBackground();
                      }
                  }
              });
    }


    private void redirectToMainScreen(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
    /*
    This function calls a cloud code function which initializes a user and creates the default settings for a new user
     */
    private void initializeNewUser(){
        Map<String, Object> param = new HashMap<>();
        ParseCloud.callFunctionInBackground("initializeNewUser", param, new FunctionCallback<Object>() {
            @Override
            public void done(Object stringObjectMap, ParseException e) {
                if (e == null) {
                    Log.d("CreateUser", "successful");
                    //Fetch all userData from Parse Backend
                    ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            redirectToMainScreen();
                        }
                    });

                } else {
                    Toast.makeText(getApplicationContext(), "Error creating User. Try again!", Toast.LENGTH_LONG).show();
                    Log.e("CloudCode", e.getMessage());
                    //If the user could not be created correctly (initialize all settings and retrive all facebook data)
                    // delete the ParseUser to rerun the whole procedure
                    ParseUser.getCurrentUser().deleteInBackground();
                }
            }

        });
    }
}
