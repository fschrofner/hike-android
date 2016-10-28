package at.fhhgb.mc.hike.app;

import android.util.Log;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.io.Serializable;

import at.fhhgb.mc.hike.model.database.DatabaseException;
import at.fhhgb.mc.hike.model.database.HikeRoute;

/**
 * @author Florian Schrofner
 */

public class Database {
    final static String TAG = Database.class.getSimpleName();
    private static DB mDatabase;

    public static void saveHikeRouteInDatabase(HikeRoute route) throws DatabaseException {
        Log.d(TAG, "saving hike with id: " + route.getUniqueId() + " in database");
        saveInDatabase(String.valueOf(route.getUniqueId()), route);
    }

    public static HikeRoute getHikeRouteFromDatabase(long uniqueId) throws DatabaseException {
        Log.d(TAG, "loading hike with id: " + uniqueId + " from database");
        return getFromDatabase(String.valueOf(uniqueId), HikeRoute.class);
    }

    private static void saveInDatabase(String key, Serializable value) throws DatabaseException {
        try {
            DB database = getDatabase();
            database.put(key, value);
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new DatabaseException();
        }
    }

    private static <T extends Serializable> T getFromDatabase(String key, Class<T> valueClass) throws DatabaseException {
        Log.d(TAG, "getting class from database: " + valueClass.getSimpleName());
        try {
            DB database = getDatabase();
            return database.get(key, valueClass);
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new DatabaseException();
        }
    }

    private static DB getDatabase() throws SnappydbException {
        if(mDatabase == null){
            mDatabase = DBFactory.open(AppClass.getAppContext());
        }
        return mDatabase;
    }

    public static void close() throws DatabaseException {
        Log.d(TAG, "database closed");
        try {
            getDatabase().close();
            mDatabase = null;
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new DatabaseException();
        }
    }
}
