package dhbw.mobile2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.test.espresso.Espresso;
import android.test.ActivityInstrumentationTestCase2;

import com.parse.ParseUser;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


/**
 * Created by Christian on 19.05.15.
 */
public class LogoutFragmentTest extends ActivityInstrumentationTestCase2<MainScreen> {

    public LogoutFragmentTest() {
        super(MainScreen.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
        Espresso.closeSoftKeyboard();

        // Check if the User is logged in.
        //If the user is not logged in, use the TestUser to login an perform the tests
        if(ParseUser.getCurrentUser()==null){
            ParseUser.logIn("TestUser", "1234");
        }
    }


    public void testLogoutCorrectRedirectIfLogoutContinue(){

        Fragment fragment = new LogoutFragment();
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commitAllowingStateLoss();
        onView(withText("Yes")).perform(click());
        onView(withText("Login with Facebook")).check(matches(isDisplayed()));
    }

    public void testLogoutCorrectRedirectIfLogoutCanceled(){

        Fragment fragment = new LogoutFragment();
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        fragmentManager.beginTransaction().add(R.id.frame_container, fragment).commitAllowingStateLoss();
        onView(withText("No")).perform(click());
    }





}
