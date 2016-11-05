package at.fhhgb.mc.hike.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.model.database.HikeTag;
import at.fhhgb.mc.hike.ui.fragment.TagFragment;

/**
 * @author Florian Schrofner
 */

public class TagActivity extends GlobalActivity {
    public final static int RESULT_CODE_TAG_CREATED = 83635;
    public final static int RESULT_CODE_TAG_FAILED = 83544;
    public final static String EXTRA_CREATED_TAG = "created.tag";

    private int mMenuToInflate = R.menu.tag_menu;
    private TagFragment mTagFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView(R.layout.activity_tag);
        mTagFragment = TagFragment.newInstance();
        addFragment(mTagFragment, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(mMenuToInflate, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void changeMenu(@MenuRes int menuToInflate){
        mMenuToInflate = menuToInflate;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_save) {
            if(mTagFragment != null){
                Intent data = new Intent();

                //put in the tag created in the fragment
                HikeTag hikeTag = mTagFragment.getTagData();
                data.putExtra(EXTRA_CREATED_TAG, hikeTag);

                setResult(RESULT_CODE_TAG_CREATED, data);
            } else {
                setResult(RESULT_CODE_TAG_FAILED);
            }

            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
