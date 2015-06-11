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
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LogInActivity extends ActionBarActivity {

    private boolean saveFacebookPicture=false;
    private boolean saveFacebookData=false;
    private boolean errorOccured=false;
    private boolean isLogin=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);



        //Hide the ActionBar for the Login Screen
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log_in, menu);
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Is called by the User if he hits the Button FacebookLogin
    public void linkFacebookLogIn(View view) {
        final List<String> permissions = Arrays.asList("public_profile", "email");
        isLogin=true;


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
                    inizialieSettings();
                    retriveFacebookInformation();
                    saveFacebookProfilePicture();
                    redirectToMainScreen();
                    //Redirect to MainActivity is executed in redirectToMainScreen()
                    //=> This task takes the longest and only if all Data is retrived its supposed to be redirected

                } else {
                    Log.d("MyApp", "User logged in with Facebook!");
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }


    /*
    This function calls a cloud code function which creates the default settings for a new user
     */
    private void inizialieSettings(){
        Map<String, Object> param = new HashMap<>();
        ParseCloud.callFunctionInBackground("initializeNewUser", param, new FunctionCallback<Map<String, Object>>() {
            @Override
            public void done(Map<String, Object> stringObjectMap, ParseException e) {
                if (e == null) {
                    Log.d("CreateUser", "successfull");
                } else {
                    Toast.makeText(getApplicationContext(), "error in CloudCode", Toast.LENGTH_LONG).show();
                    errorOccured=true;
                    //Delete created user to sign up again
                    Log.e("CloudCode", e.getMessage());
                }
            }

        });
    }

    /*
    This method checks if all callbacks retruned with a positive result and redirects to main screen
     */
    private void redirectToMainScreen(){
        if(errorOccured){
            //Reload Activity to start over with signup or login and delete user
            ParseUser.getCurrentUser().deleteInBackground();
            Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
            startActivity(intent);
        }else{
            if(saveFacebookData&&saveFacebookPicture&&isLogin){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        }
    }


    public void retriveFacebookInformation() {
        // make request to the /me API retrive and save Facebook User infos to Parse
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject user, GraphResponse response) {
                if (user != null) {
                    try {
                       ParseUser.getCurrentUser().put("username", user.getString("name"));
                        ParseUser.getCurrentUser().put("gender", user.getString("gender"));
                        ParseUser.getCurrentUser().put("aboutMe", "");
                        ParseUser.getCurrentUser().saveInBackground();
                        saveFacebookData=true;
                        redirectToMainScreen();

                    } catch (Exception e) {
                        errorOccured=true;
                        redirectToMainScreen();
                        Log.e("Error from FB Data", e.getMessage());
                    }
                }
            }
        }).executeAsync();



    }

    public void saveFacebookProfilePicture() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        String facebookId = accessToken.getUserId();
        Map<String, Object> param = new HashMap<>();
        param.put("url", "http://graph.facebook.com/"+facebookId+"/picture");


        //Call Cloud Function that downloads the Facebook picture and saves it to user in background
        ParseCloud.callFunctionInBackground("retriveFacebookPicture", param, new FunctionCallback<Object>() {
            @Override
            public void done(Object o, ParseException e) {
                        if (e == null) {
                            Log.d("savePorofilePicture", "successfull");
                            saveFacebookPicture = true;
                            redirectToMainScreen();
                        } else {
                            errorOccured = true;
                            redirectToMainScreen();
                            Log.e("CloudCode", e.getMessage());
                        }
                    }

                });


    }

}
