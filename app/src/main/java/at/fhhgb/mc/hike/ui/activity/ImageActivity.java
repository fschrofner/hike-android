package at.fhhgb.mc.hike.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;

import at.fhhgb.mc.hike.R;
import butterknife.BindView;

/**
 * @author Florian Schrofner
 */

public class ImageActivity extends GlobalActivity{
    public final static String IMAGE_EXTRA = "image.extra";

    @BindView(R.id.image)
    GestureImageView mImageView;

    @BindView(R.id.close_button)
    ImageButton mCloseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView(R.layout.activity_image);

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Bundle bundle = getIntent().getExtras();

        if(bundle != null && bundle.containsKey(IMAGE_EXTRA)){
            String image = bundle.getString(IMAGE_EXTRA);
            StringSignature signature = new StringSignature(String.valueOf(System.currentTimeMillis()));
            Glide.with(this).load(image).signature(signature).into(mImageView);
            mImageView.getController().getSettings().setMaxZoom(20f).setOverscrollDistance(500f, 500f);
        }
    }
}
