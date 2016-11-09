package at.fhhgb.mc.hike.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ui.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;

import at.fhhgb.mc.hike.R;

/**
 * @author Florian Schrofner
 */

public class SplashActivity extends GlobalActivity {
    private static final int SIGN_IN_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView(R.layout.activity_splash);

        //login
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivityForResult(
                    // Get an instance of AuthUI based on the default app
                    AuthUI.getInstance().createSignInIntentBuilder().build(),
                    SIGN_IN_REQUEST_CODE);
        } else {
            showMainActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //TODO: Maybe do something with the response
        if(requestCode==SIGN_IN_REQUEST_CODE){
            if (resultCode == RESULT_OK) {
                showMainActivity();
            }

            // Sign in canceled
            if (resultCode == RESULT_CANCELED) {
            }

            // No network
            if (resultCode == ResultCodes.RESULT_NO_NETWORK) {

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showMainActivity(){
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
