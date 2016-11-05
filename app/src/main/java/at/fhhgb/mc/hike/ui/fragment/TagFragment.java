package at.fhhgb.mc.hike.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.model.database.HikeTag;
import at.flosch.logwrap.Log;
import butterknife.BindView;

/**
 * @author Florian Schrofner
 */

public class TagFragment extends GlobalFragment {
    @BindView(R.id.tag_description)
    EditText mTagDescription;

    final static String TAG = TagFragment.class.getSimpleName();

    public static TagFragment newInstance(){
        return new TagFragment();
    }

    public HikeTag getTagData(){
        HikeTag hikeTag = new HikeTag();
        hikeTag.setDescription(mTagDescription.getText().toString());
        return hikeTag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "on create view called");
        setView(R.layout.fragment_tag, inflater, container);
        return mRootView;
    }
}
