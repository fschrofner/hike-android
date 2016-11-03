package at.fhhgb.mc.hike.ui.activity;

import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.ui.fragment.TagFragment;

/**
 * @author Florian Schrofner
 */

public class TagActivity extends GlobalActivity {
    private int mMenuToInflate = R.menu.tag_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView(R.layout.activity_tag);
        addFragment(TagFragment.newInstance(), false);
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
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
