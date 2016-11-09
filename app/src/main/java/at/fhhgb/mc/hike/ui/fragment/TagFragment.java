package at.fhhgb.mc.hike.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.signature.StringSignature;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

import at.fhhgb.mc.hike.R;
import at.fhhgb.mc.hike.model.database.HikeTag;
import at.fhhgb.mc.hike.ui.activity.ImageActivity;
import at.flosch.logwrap.Log;
import butterknife.BindView;

/**
 * @author Florian Schrofner
 */

public class TagFragment extends GlobalFragment {
    final static int REQUEST_CODE_IMAGE_CHOOSER = 22352;
    @BindView(R.id.tag_title)
    EditText mTagTitle;

    @BindView(R.id.tag_description)
    EditText mTagDescription;

    @BindView(R.id.tag_photo)
    ImageButton mTagPhoto;

    @BindView(R.id.tag_clear_photo)
    ImageButton mClearPhoto;

    public String mPhoto;

    final static String TAG = TagFragment.class.getSimpleName();

    public static TagFragment newInstance(){
        return new TagFragment();
    }

    public HikeTag getTagData(){
        HikeTag hikeTag = new HikeTag();
        hikeTag.setDescription(mTagDescription.getText().toString());
        hikeTag.setTitle(mTagTitle.getText().toString());
        hikeTag.setPhoto(mPhoto);
        return hikeTag;
    }

    private void showPhotoPicker(){
        Intent intent = CropImage.getPickImageChooserIntent(getContext());
        startActivityForResult(intent, REQUEST_CODE_IMAGE_CHOOSER);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "on create view called");
        setView(R.layout.fragment_tag, inflater, container);

        mTagPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPhotoPicker();
            }
        });

        mClearPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearPhotoFromButton();
            }
        });

        if(mPhoto != null){
            loadPhotoIntoButton();
        } else {
            Log.d(TAG, "no photo selected before");
            //TODO: save mPhoto before fragment gets redrawn
        }
        return mRootView;
    }

    private void clearPhotoFromButton(){
        mTagPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_add_a_photo_white_48dp, null));
        mClearPhoto.setVisibility(View.GONE);

        mTagPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPhotoPicker();
            }
        });
    }

    private void loadPhotoIntoButton(){
        StringSignature signature = new StringSignature(String.valueOf(System.currentTimeMillis()));
        Glide.with(getContext()).load(new File(mPhoto)).asBitmap().signature(signature).centerCrop().into(new BitmapImageViewTarget(mTagPhoto){
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                //circularBitmapDrawable.setCircular(true);
                circularBitmapDrawable.setCornerRadius(getResources().getDimension(R.dimen.round_edit_text_radius));
                mTagPhoto.setImageDrawable(circularBitmapDrawable);
            }
        });

        mClearPhoto.setVisibility(View.VISIBLE);

        mTagPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getGlobalActivity(), ImageActivity.class);
                intent.putExtra(ImageActivity.IMAGE_EXTRA, mPhoto);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == REQUEST_CODE_IMAGE_CHOOSER && resultCode == Activity.RESULT_OK) {
            //TODO: load with glide
            //TODO: could crop picture here
            Uri imageUri = CropImage.getPickImageResultUri(getContext(), data);
            Log.d(TAG, "image path: " + imageUri.getPath());
            mPhoto = imageUri.getPath();
            loadPhotoIntoButton();
            //CropImage.activity(imageUri).setAspectRatio(1,1).start(getContext(), this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            //CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //if (resultCode == Activity.RESULT_OK) {
                //Uri resultUri = result.getUri();
                //ImageUtils.with(getContext()).load(new File(resultUri.getPath())).into(mImage);
                //showImage();
                //mPhoto = resultUri.getPath();
            //}
        }
    }
}
