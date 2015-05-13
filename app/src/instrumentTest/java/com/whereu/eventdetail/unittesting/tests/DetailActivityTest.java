package com.whereu.eventdetail.unittesting.tests;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;
import android.widget.TextView;

import com.parse.ParseObject;

import dhbw.mobile2.EventDetailActivity;
import dhbw.mobile2.R;

/**
 * Created by Vincent on 13.05.2015.
 */
public class DetailActivityTest extends ActivityInstrumentationTestCase2<EventDetailActivity>{

    EventDetailActivity activity;


    public DetailActivityTest(){
        super(EventDetailActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        activity = getActivity();
    }

    @SmallTest
    public void textViewNotNull(){
        TextView textView = (TextView) activity.findViewById(R.id.detail_category_dynamic);
        assertNotNull(textView);
    }

    public void testFillSimpleType(){
        ParseObject event = new ParseObject("Event");
        event.put("title", "Runde Flunkiball auf dem Campus");
        activity.setEventObject(event);
        TextView testTextView = (TextView) activity.findViewById(R.id.detail_category_dynamic);
        activity.fillSimpleType("title", testTextView);
        assertEquals("Runde Flunkiball auf dem Campus", testTextView.getText());

    }

}
