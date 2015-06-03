package dhbw.mobile2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.opengl.Visibility;
import android.support.test.espresso.Espresso;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.parse.ParseUser;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * Created by christian on 29.05.15.
 */
public class ProfileFragmentTest extends ActivityInstrumentationTestCase2<MainScreen> {

    public ProfileFragmentTest() {
        super(MainScreen.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Check if the User is logged in.
        //If the user is not logged in, use the TestUser to login an perform the tests
        if(ParseUser.getCurrentUser()==null){
            ParseUser.logIn("TestUser", "1234");
        }

        getActivity();
        Espresso.closeSoftKeyboard();

    }

    public void testOwnProfileAllFieldsAreCorrect(){

        Fragment fragment = ProfileFragment.newInstance("mF3LMXzDIW");
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commitAllowingStateLoss();

        onView(withId(R.id.textView_change)).check(matches(isDisplayed()));
        onView(withId(R.id.editText_username)).check(matches(isDisplayed()));
        onView(withId(R.id.textView_reloadFacebookData)).check(matches(not(isDisplayed())));
    }

    public void testOtherUsersProfile(){

        Fragment fragment = ProfileFragment.newInstance("sGcknYlkNI");
        FragmentManager fragmentManager = getActivity().getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commitAllowingStateLoss();

        onView(withId(R.id.textView_change)).check(doesNotExist());
        onView(withId(R.id.editText_username)).check(matches(isDisplayed()));
        onView(withId(R.id.editText_about)).check(matches(isDisplayed()));
    }


}
