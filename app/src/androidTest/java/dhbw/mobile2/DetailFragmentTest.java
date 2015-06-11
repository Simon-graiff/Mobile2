package dhbw.mobile2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.test.ActivityInstrumentationTestCase2;

import com.parse.ParseUser;

import dalvik.annotation.TestTargetClass;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Vincent on 20.05.2015.
 */
public class DetailFragmentTest extends ActivityInstrumentationTestCase2<MainScreen> {
        public DetailFragmentTest() {
            super(MainScreen.class);
        }


        @Override
        protected void setUp() throws Exception {
            super.setUp();
            getActivity();

            if(ParseUser.getCurrentUser()==null){
                ParseUser.logIn("TestUser", "1234");
            }
        }

        public void testCreateFragment(){
            Fragment fragment = new EventDetailFragment();
            FragmentManager fragmentManager = getActivity().getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commitAllowingStateLoss();
        }

        public void testReceiveParseData(){

        }


        @Override
        protected void tearDown() throws Exception {
            super.tearDown();
            ParseUser.logOut();
        }

}
