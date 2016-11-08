package at.fhhgb.mc.hike.ui.activity;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;

import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.ui.fragment.GlobalFragment;
import butterknife.ButterKnife;

/**
 * @author Florian Schrofner
 */

public class GlobalActivity extends AppCompatActivity {
    @MenuRes
    private int mMenuToInflate = R.menu.empty_menu;
    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getSupportFragmentManager();

        //disable actionbar shadow
        if(getSupportActionBar() != null){
            getSupportActionBar().setElevation(0);
        }
    }

    public void setTitle(String title){
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(mMenuToInflate, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void changeMenu(@MenuRes int menuToInflate){
        mMenuToInflate = menuToInflate;
        invalidateOptionsMenu();
    }

    public void setView(@LayoutRes int layout){
        setContentView(layout);
        ButterKnife.bind(this);
    }

    public void addFragment(@NonNull GlobalFragment fragment, boolean addToBackStack){
        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        transaction.replace(R.id.fragment_holder, fragment, fragment.getFragmentName());

        if (addToBackStack) {
            transaction.addToBackStack(fragment.getFragmentName());
        }

        transaction.commitAllowingStateLoss();
    }

    public void popFragment(){
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStackImmediate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
