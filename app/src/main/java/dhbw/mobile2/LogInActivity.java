package dhbw.mobile2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LogInActivity extends ActionBarActivity {

    private int callbackCount = 0;
    private boolean errorOccured=false;


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
                    retriveFacebookData();
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
                    ParseUser.getCurrentUser().deleteInBackground();
                    Log.e("CloudCode", e.getMessage());
                }
            }

        });
    }

    /*
    This method checks if all callbacks retruned with a positive result and redirects to main screen
     */
    private void redirectToMainScreen(){
        callbackCount++;
                if(callbackCount >3 && !errorOccured){
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }else{
            //Reload Activity to start over with signup or login
            Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
            startActivity(intent);
        }
    }

    public void retriveFacebookData() {
        //Retrive Facebook ProfilePicutre from Facebook API & Save it to Parse
        saveFacebookProfilePicture();

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
                        redirectToMainScreen();

                    } catch (Exception e) {
                        errorOccured=true;
                        Log.e("Error from FB Data", e.getMessage());
                    }
                }
            }
        }).executeAsync();



    }

    private void saveFacebookProfilePicture() {
        // make request to the /me API
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GraphRequest graph = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject user, GraphResponse response) {
                if (user != null) {
                    try {
                        //Retrive URL from Facebook profile picture
                        String pictureURL = user.getJSONObject("picture").getJSONObject("data").getString("url");
                        URL url = new URL(pictureURL);
                        //Download the Picture from provided URL and Save it as ByteArray to Parse
                        new DownloadPictureTask().execute(url);
                        redirectToMainScreen();
                    } catch (Exception e) {
                        errorOccured=true;
                        Log.e("Error from FB Data", e.getMessage());
                    }
                }
            }
        });

        // Set paramaters for MeRequest to retrive the profile pricutre in correct size.
        Bundle bundle = new Bundle();
        bundle.putString("fields", "name,picture.width(800).height(800)");
                graph.setParameters(bundle);
        graph.executeAsync();


    }

    /**
     * This Method starts a Thread to download the pictures from the provided URLs and saves
     * them as a File attached to the ParseUser.
     */
    private class DownloadPictureTask extends AsyncTask<URL, Void, Void> {
        @Override
        protected Void doInBackground(URL... urls) {
            int count = urls.length;
            for (int i = 0; i < count; i++) {

                InputStream in = null;
                try {
                    in = new BufferedInputStream(urls[i].openStream());
                    //Transfer InputStream in ByteArray
                    byte[] data = IOUtils.toByteArray(in);

                    //Saves the ByteArray to Parse as a File
                    ParseFile file = new ParseFile("profilepicture.jpg", data);
                    ParseUser.getCurrentUser().put("profilepicture", file);
                    ParseUser.getCurrentUser().saveInBackground();
                    redirectToMainScreen();

                } catch (IOException e) {
                    errorOccured=true;
                    e.printStackTrace();
                }
                if (isCancelled()) break;
            }
            return null;
        }
    }


}
