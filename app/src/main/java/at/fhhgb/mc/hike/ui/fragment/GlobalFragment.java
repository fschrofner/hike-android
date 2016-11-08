package at.fhhgb.mc.hike.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.ui.activity.GlobalActivity;
import butterknife.ButterKnife;

/**
 * @author Florian Schrofner
 */

public class GlobalFragment extends Fragment{
    @MenuRes
    private int mMenuToInflate = R.menu.empty_menu;
    private String FRAGMENT_NAME;
    protected View mRootView;

    public String getFragmentName(){
        return FRAGMENT_NAME;
    }

    protected void setView(@LayoutRes int layout, LayoutInflater inflater, ViewGroup container){
        mRootView = inflater.inflate(layout, container, false);
        ButterKnife.bind(this, mRootView);
    }

    @Override
    public void onAttach(Context context) {
        FRAGMENT_NAME = this.getClass().getSimpleName();
        super.onAttach(context);
    }

    public GlobalActivity getGlobalActivity(){
        Activity activity = getActivity();
        if(activity != null){
            return (GlobalActivity)getActivity();
        } else {
            return null;
        }
    }
}
