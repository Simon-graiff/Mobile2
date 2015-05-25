package dhbw.mobile2;

import android.test.ActivityInstrumentationTestCase2;

import com.parse.ParseUser;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Christian on 25.05.15.
 */
public class LoginTest extends ActivityInstrumentationTestCase2<MainScreen> {
    public LoginTest() {
        super(MainScreen.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
        ParseUser.logOut();
    }

    /**
     * Tests if the the AutoLogin works and if the user is redirected automatically without any errors
     */
    public void testAutoLoginBySession() throws Exception{

        //If the user is not logged in login with testUser
        if(ParseUser.getCurrentUser()==null){
            ParseUser.logIn("TestUser", "1234");
        }
        onView(withText("WhereU")).check(matches(isDisplayed()));

    }

    public void testLoginScreenIfNotLoggedIn() throws Exception{
        onView(withText("Login with Facebook")).check(matches(isDisplayed()));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ParseUser.logOut();
    }
}