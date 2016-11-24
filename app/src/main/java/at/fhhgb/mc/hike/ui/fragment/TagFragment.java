package at.fhhgb.mc.hike.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.signature.StringSignature;
import com.theartofdev.edmodo.cropper.CropImage;

import org.honorato.multistatetogglebutton.MultiStateToggleButton;
import org.honorato.multistatetogglebutton.ToggleButton;

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
    final static String EXTRA_PHOTO = "extra.photo";
    final static String EXTRA_TITLE = "extra.title";
    final static String EXTRA_DESCRIPTION = "extra.desc";

    @BindView(R.id.tag_type)
    MultiStateToggleButton mTagType;

    @BindView(R.id.tag_title)
    EditText mTagTitle;

    @BindView(R.id.tag_description)
    EditText mTagDescription;

    @BindView(R.id.tag_photo)
    ImageButton mTagPhoto;

    @BindView(R.id.tag_clear_photo)
    ImageButton mClearPhoto;

    @BindView(R.id.tag_photo_holder)
    RelativeLayout mTagPhotoHolder;

    private String mTitle;
    private String mDescription;
    private String mPhoto;
    private Uri mPhotoUri;
    private HikeTag.TagType mType;

    final static String TAG = TagFragment.class.getSimpleName();

    public static TagFragment newInstance(){
        return new TagFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_PHOTO, mPhoto);
        outState.putString(EXTRA_TITLE, mTagTitle.getText().toString());
        outState.putString(EXTRA_DESCRIPTION, mTagDescription.getText().toString());
        Log.d(TAG, "saved state");
    }

    private void setOnClickListeners(){
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

        mTagType.setOnValueChangedListener(new ToggleButton.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                switch(value){
                    case 0:
                        switchToTitle();
                        break;
                    case 1:
                        switchToText();
                        break;
                    case 2:
                        switchToImage();
                        break;
                    case 3:
                        switchToPoi();
                        break;
                }
            }
        });
    }

    private void switchToTitle(){
        mType = HikeTag.TagType.Title;

        mTagPhoto.setEnabled(false);
        mTagDescription.setEnabled(false);
        mTagTitle.setEnabled(true);

        mTagTitle.setVisibility(View.VISIBLE);
        mTagDescription.setVisibility(View.GONE);
        mTagPhotoHolder.setVisibility(View.GONE);
    }

    private void switchToText(){
        mType = HikeTag.TagType.Text;

        mTagPhoto.setEnabled(false);
        mTagDescription.setEnabled(true);
        mTagTitle.setEnabled(true);

        mTagTitle.setVisibility(View.VISIBLE);
        mTagDescription.setVisibility(View.VISIBLE);
        mTagPhotoHolder.setVisibility(View.GONE);
    }

    private void switchToImage(){
        mType = HikeTag.TagType.Image;

        mTagPhoto.setEnabled(true);
        mTagDescription.setEnabled(true);
        mTagTitle.setEnabled(true);

        mTagTitle.setVisibility(View.VISIBLE);
        mTagDescription.setVisibility(View.VISIBLE);
        mTagPhotoHolder.setVisibility(View.VISIBLE);
    }

    private void switchToPoi(){
        switchToImage();
        mType = HikeTag.TagType.Poi;
    }

    private void recreateInstanceState(@Nullable Bundle savedInstanceState){
        //recreate state
        if(savedInstanceState != null){
            Log.d(TAG, "saved state found, recreating..");
            String title = savedInstanceState.getString(EXTRA_TITLE);
            String desc = savedInstanceState.getString(EXTRA_DESCRIPTION);
            String photo = savedInstanceState.getString(EXTRA_PHOTO);


            Log.d(TAG, "saved title: " + title);
            Log.d(TAG, "saved desc: " + desc);
            Log.d(TAG, "saved photo: " + photo);

            if(title != null) mTagTitle.setText(title);
            if(desc != null) mTagDescription.setText(desc);

            if(photo != null){
                mPhoto = photo;
                loadPhotoIntoButton();
            }

            //apply tag type again
            mTagType.setValue(mTagType.getValue());
        } else {
            Log.d(TAG, "no saved state found");
        }
    }

    public HikeTag getTagData(){
        HikeTag hikeTag = new HikeTag();
        hikeTag.setTagType(mType);
        hikeTag.setTitle(mTagTitle.getText().toString());

        if(mType != HikeTag.TagType.Title) hikeTag.setDescription(mTagDescription.getText().toString());
        if(mType == HikeTag.TagType.Image) hikeTag.setPhoto(mPhoto);

        return hikeTag;
    }

    private void showPhotoPicker(){
        Intent intent = CropImage.getPickImageChooserIntent(getContext());
        startActivityForResult(intent, REQUEST_CODE_IMAGE_CHOOSER);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "on create view called");
        setView(R.layout.fragment_tag, inflater, container);

        recreateInstanceState(savedInstanceState);
        setOnClickListeners();

        //set default type, when not defined
        if(mTagType.getValue() < 0){
            mTagType.setValue(0);
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

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContext().getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == REQUEST_CODE_IMAGE_CHOOSER && resultCode == Activity.RESULT_OK) {
            //TODO: could crop picture here
            mPhotoUri = CropImage.getPickImageResultUri(getContext(), data);
            Log.d(TAG, "image path: " + getRealPathFromURI(mPhotoUri));

            if (CropImage.isReadExternalStoragePermissionsRequired(getContext(), mPhotoUri)) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},  CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                mPhoto = getRealPathFromURI(mPhotoUri);
                loadPhotoIntoButton();
                mTagType.setValue(mTagType.getValue());
            }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mPhotoUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                mPhoto = getRealPathFromURI(mPhotoUri);
                loadPhotoIntoButton();
                mTagType.setValue(mTagType.getValue());
            } else {
                Toast.makeText(getContext(), "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }
}
