package gr.sullenart.games.puzzles.gameengine.solo;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SoloPuzzlesDbAdapter {
	
	public static final String KEY_NAME = "name";
	public static final String KEY_BOARD = "board";
	public static final String KEY_METADATA = "metadata";
	public static final String KEY_ROWID = "_id";

	public static final int NAME_COLUMN_INDEX = 1;
	public static final int BOARD_COLUMN_INDEX = 2;
	public static final int METADATA_COLUMN_INDEX = 3;

	private static final String TAG = "SoloPuzzlesDbAdapter";

	private DatabaseHelper dbHelper;

	private SQLiteDatabase db;

	private static final String TABLE_BOARDS_CREATE =
	        "create table SoloBoards (_id integer primary key autoincrement, " +
	        "name text not null, board text not null, metadata text not null); ";
	
	private static final String DATABASE_NAME = "SoloBoards";
	private static final String BOARDS_TABLE = "SoloBoards";
	private static final int DATABASE_VERSION = 2;

	private final Context context;	
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

	    DatabaseHelper(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	        db.execSQL(TABLE_BOARDS_CREATE);
	    }

        private Map<String, SoloGame> getOldGames(SQLiteDatabase db) {
            Map<String, SoloGame> oldGames = new HashMap<String, SoloGame>();
        
            try
            {
                Cursor cursor = db.query(true, BOARDS_TABLE,
                            null, null, null, null, null, null, null);
                
                while(cursor.moveToNext()) {
                    String name = cursor.getString(1);
                    oldGames.put(name, 
                                 SoloGameSerializer.deserializeWithNoMetadata(cursor.getString(2)));
                }

                cursor.close();
            }
            catch (SQLException ex)
            {
                Log.w("SoloDb", ex.getMessage());
            }

            return oldGames;
        }
        
        private void restoreOldGames(SQLiteDatabase db, Map<String, SoloGame> oldGames) {
            if (oldGames == null) {
                return;
            }

            try {
                for(String name: oldGames.keySet()) {
                    SoloGame soloGame = oldGames.get(name);
                    String board = SoloGameSerializer.serializeBoard(soloGame.getBoard());
                    String metadata = SoloGameSerializer.serializeMetadata(soloGame);

                    ContentValues initialValues = new ContentValues();
                    initialValues.put(KEY_NAME, name);
                    initialValues.put(KEY_BOARD, board);
                    initialValues.put(KEY_METADATA, metadata);

                    db.insert(BOARDS_TABLE, null, initialValues);
                }
            }
            catch(SQLException ex) {
                Log.w("SoloDb", ex.getMessage());
            }          
        }
        
	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	                + newVersion + ", which will destroy all old data");
                    
            // Get old games with no metadata information
            Map<String, SoloGame> oldGames = getOldGames(db);
                    
	        db.execSQL("DROP TABLE IF EXISTS " + BOARDS_TABLE);
	        onCreate(db);
            
            // Restore old games
            restoreOldGames(db, oldGames);
	    }
	}	
	
	public SoloPuzzlesDbAdapter(Context context) {
	    this.context = context;
	}
	
	public SoloPuzzlesDbAdapter open() throws SQLException {
	    dbHelper = new DatabaseHelper(context);
	    db = dbHelper.getWritableDatabase();

	    return this;
	}

	public void close() {
	    dbHelper.close();
	}
	
	public Cursor getBoards() throws SQLException {
	    Cursor cursor = db.query(true, BOARDS_TABLE,
            		null, null, null, null, null, null, null);
	    return cursor;		
	}
	
	public long addBoard(String name, String board, String metadata) {
	    ContentValues initialValues = new ContentValues();
        
	    initialValues.put(KEY_NAME, name);
	    initialValues.put(KEY_BOARD, board);
	    initialValues.put(KEY_METADATA, metadata);

	    return db.insert(BOARDS_TABLE, null, initialValues);
	}
	
	public int deleteBoard(String name) {
		return db.delete(BOARDS_TABLE, KEY_NAME + "=?", new String [] {name});
	}
	
	public int renameBoard(String name, String newName) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_NAME, newName);
		return db.update(BOARDS_TABLE, contentValues, KEY_NAME + "=?", new String [] {name});
	}
	
	public int updateBoard(String name, String board, String metadata) {
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_BOARD, board);
		contentValues.put(KEY_METADATA, metadata);
		return db.update(BOARDS_TABLE, contentValues, KEY_NAME + "=?", new String [] {name});
	}
	
	public boolean isNameInUse(String name) {
		boolean result = false;
	    Cursor cursor =
            db.query(true, BOARDS_TABLE,
            		null, KEY_NAME + "=?", new String [] {name}, null, null, null, null);
	    if (cursor.moveToFirst()){
	    	result = true;
	    }
	    cursor.close();
		return result;
	}
}
