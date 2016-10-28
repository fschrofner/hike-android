package at.fhhgb.mc.hike.app;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import at.fhhgb.mc.hike.model.database.DatabaseException;
import at.fhhgb.mc.hike.model.database.HikeRoute;

/**
 * @author Florian Schrofner
 */

public class Database {
    private static DB mDatabase;

    public static void saveHikeRouteInDatabase(HikeRoute route) throws DatabaseException {
        saveInDatabase(String.valueOf(route.getUniqueId()), route);
    }

    public static HikeRoute getHikeRouteFromDatabase(long uniqueId) throws DatabaseException {
        return (HikeRoute) getFromDatabase(String.valueOf(uniqueId));
    }

    private static void saveInDatabase(String key, Object value) throws DatabaseException {
        try {
            DB database = getDatabase();
            database.put(key, value);
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new DatabaseException();
        }
    }

    private static Object getFromDatabase(String key) throws DatabaseException {
        try {
            DB database = getDatabase();
            return database.get(key);
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
        try {
            getDatabase().close();
        } catch (SnappydbException e) {
            e.printStackTrace();
            throw new DatabaseException();
        }
    }
}
