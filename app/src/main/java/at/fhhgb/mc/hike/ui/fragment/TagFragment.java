package at.fhhgb.mc.hike.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import at.fhhgb.mc.hike.R;
import at.flosch.logwrap.Log;

/**
 * @author Florian Schrofner
 */

public class TagFragment extends GlobalFragment {
    final static String TAG = TagFragment.class.getSimpleName();

    public static TagFragment newInstance(){
        return new TagFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "on create view called");
        setView(R.layout.fragment_tag, inflater, container);
        return mRootView;
    }
}
