package com.mattfred.musicaid.unitTests;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.ListView;

import com.mattfred.musicaid.R;
import com.mattfred.musicaid.Transpose;

/**
 * Created by matthewfrederick on 8/14/15.
 */
public class TransposeTest extends ActivityUnitTestCase<Transpose>
{
    private Intent mLaunchIntent;

    public TransposeTest()
    {
        super(Transpose.class);
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        mLaunchIntent = new Intent(getInstrumentation()
                .getTargetContext(), Transpose.class);
    }

    @MediumTest
    public void testActivityWasLaunchedWithIntent()
    {
        startActivity(mLaunchIntent, null, null);
        ListView mDrawerList = (ListView) getActivity().findViewById(R.id.drawer_layout);
        mDrawerList.performItemClick(mDrawerList, 0, mDrawerList.getSelectedItemId());

        final Intent intent = getStartedActivityIntent();
        assertNotNull("Launched Intent was null", intent);
        assertTrue(isFinishCalled());
    }
}
