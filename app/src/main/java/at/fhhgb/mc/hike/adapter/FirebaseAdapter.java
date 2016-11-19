package at.fhhgb.mc.hike.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.text.InputType;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.Map;

import at.fhhgb.mc.hike.model.database.HikeRoute;

/**
 * Created by Niklas on 09.11.2016.
 */
public class FirebaseAdapter {

    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private StorageReference storageRef;

    private static FirebaseAdapter instance;

    public static FirebaseAdapter getInstance() {
        if (instance == null) {
            instance = new FirebaseAdapter();
        }
        return instance;
    }

    private FirebaseAdapter() {
        auth = FirebaseAuth.getInstance();
        auth.signInAnonymously();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://hiking-journal.appspot.com");
    }

    public boolean uploadHike(HikeRoute hike) {

        if (auth.getCurrentUser() == null) {
            return false;
        }

        //check if the hike already has a key, if not create one
        if (!hike.hasFirebaseId()) {
            String key = dbRef.child("hikes").push().getKey();
            hike.setmFirebaseId(key);
        }

        Map<String, Object> map = hike.toKeyValueMap();
        map.put("author_uid", auth.getCurrentUser().getUid());
        //TODO: Check if this is working
        map.put("author_name", auth.getCurrentUser().getDisplayName());
        dbRef.child("hikes").child(hike.getmFirebaseId()).setValue(map);
        return true;
    }

    public void uploadImageAndGetUri(String hikeId, String fileName, InputStream stream, OnSuccessListener<UploadTask.TaskSnapshot> successListener, OnFailureListener onFailureListener){
        StorageReference img = storageRef.child("hikes").child(hikeId).child(fileName);
        img.putStream(stream).addOnSuccessListener(successListener).addOnFailureListener(onFailureListener);
    }

    /**
     * Use this method if FirebaseUI does not provide
     * @param ctx
     */
    public void askForName(Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Please enter your name:");

        final EditText input = new EditText(ctx);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserProfileChangeRequest.Builder udBuilder = new UserProfileChangeRequest.Builder();
                udBuilder.setDisplayName(input.getText().toString());
                auth.getCurrentUser().updateProfile(udBuilder.build());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
