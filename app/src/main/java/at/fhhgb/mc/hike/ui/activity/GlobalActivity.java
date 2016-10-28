package at.fhhgb.mc.hike.ui.activity;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.ui.fragment.GlobalFragment;
import butterknife.ButterKnife;

/**
 * @author Florian Schrofner
 */

public class GlobalActivity extends AppCompatActivity {
    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getSupportFragmentManager();
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
