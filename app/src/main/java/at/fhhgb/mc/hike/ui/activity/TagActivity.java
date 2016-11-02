package at.fhhgb.mc.hike.ui.activity;

import android.os.Bundle;

import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.ui.fragment.TagFragment;

/**
 * @author Florian Schrofner
 */

public class TagActivity extends GlobalActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView(R.layout.activity_tag);
        addFragment(TagFragment.newInstance(), false);
    }
}
