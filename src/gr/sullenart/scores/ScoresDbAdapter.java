package gr.sullenart.scores;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ScoresDbAdapter {

	public static final String KEY_GAME = "game";
	public static final String KEY_GROUP = "category";
	public static final String KEY_PLAYER = "player";
	public static final String KEY_DATE = "date";
	public static final String KEY_SCORE = "score";
	public static final String KEY_ROWID = "_id";

	public static final int GAME_COLUMN_INDEX = 1;
	public static final int GROUP_COLUMN_INDEX = 2;
	public static final int PLAYER_COLUMN_INDEX = 3;
	public static final int DATE_COLUMN_INDEX = 4;
	public static final int SCORE_COLUMN_INDEX = 5;

	public static final String DATE_FORMAT = "yyyy/MM/dd hh:mm:ss";

	private static final String TAG = "ScoresDbAdapter";

	private DatabaseHelper dbHelper;

	private SQLiteDatabase db;

	private static final String DATABASE_CREATE =
	        "create table Scores (_id integer primary key autoincrement, " +
	        "game text not null, category text not null, player text not null, date text, " +
	        "score integer not null);";

	private static final String DATABASE_NAME = "Scores";
	private static final String DATABASE_TABLE = "Scores";
	private static final int DATABASE_VERSION = 1;

	private final Context context;

	private static class DatabaseHelper extends SQLiteOpenHelper {

	    DatabaseHelper(Context context) {
	        super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {

	        db.execSQL(DATABASE_CREATE);
	    }

	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
	                + newVersion + ", which will destroy all old data");
	        db.execSQL("DROP TABLE IF EXISTS Scores");
	        onCreate(db);
	    }
	}

	public ScoresDbAdapter(Context context) {
	    this.context = context;
	}

	public ScoresDbAdapter open() throws SQLException {
	    dbHelper = new DatabaseHelper(context);
	    db = dbHelper.getWritableDatabase();

	    return this;
	}

	public void close() {
	    dbHelper.close();
	}

	public long addScore(String game, String category, String player, int score) {
	    ContentValues initialValues = new ContentValues();

	    initialValues.put(KEY_GAME, game);
	    initialValues.put(KEY_GROUP, category);
	    initialValues.put(KEY_PLAYER, player);
	    initialValues.put(KEY_SCORE, score);

	    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
	    initialValues.put(KEY_DATE, formatter.format(new Date()));

	    return db.insert(DATABASE_TABLE, null, initialValues);
	}

	public boolean deleteScore(long rowId) {
	    return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public void deleteScoresLessThan(String game, int score) {
	    db.delete(DATABASE_TABLE,
	    		KEY_GAME + "=? AND " + KEY_SCORE + "<" + score, new String[] {game});
	}
	
	public void deleteAllScores() {
	    db.delete(DATABASE_TABLE, null, null);	
	}

	public void deleteScoresHigherThan(String game, int score) {
	    db.delete(DATABASE_TABLE,
	    		KEY_GAME + "=? AND " + KEY_SCORE + ">" + score, new String[] {game});
	}

	public Cursor getAllScores() {

	    return db.query(DATABASE_TABLE,
	    		new String[] {KEY_ROWID, KEY_GAME, KEY_GROUP, KEY_PLAYER, KEY_DATE, KEY_SCORE},
	    		null, null, null, null, null);
	}

	public Cursor getScoreById(long rowId) throws SQLException {

	    Cursor cursor =

	            db.query(true, DATABASE_TABLE,
	            		new String[] {KEY_ROWID, KEY_GAME, KEY_GROUP, KEY_PLAYER, KEY_DATE, KEY_SCORE},
	            		KEY_ROWID + "=" + rowId, null, null, null, null, null);
	    if (cursor != null) {
	        cursor.moveToFirst();
	    }
	    return cursor;
	}

	public Cursor getScoresByGame(String game, boolean desc) throws SQLException {
	    Cursor cursor =
	            db.query(true, DATABASE_TABLE,
	            		new String[] {KEY_ROWID, KEY_GAME, KEY_GROUP, KEY_PLAYER, KEY_DATE, KEY_SCORE},
	            		KEY_GAME + "=?", new String[] {game}, null, null,
	            		KEY_SCORE + (desc ? " DESC" : ""), null);
	    return cursor;
	}

	public Cursor getScoresByGroup(String category, boolean desc) throws SQLException {
	    Cursor cursor =
	            db.query(true, DATABASE_TABLE,
	            		new String[] {KEY_ROWID, KEY_GAME, KEY_GROUP, KEY_PLAYER, KEY_DATE, KEY_SCORE},
	            		KEY_GROUP + "=?", new String[] {category}, null, null,
	            		KEY_SCORE + (desc ? " DESC" : ""), null);
	    return cursor;
	}

	public Cursor getScoresByGroup(String category, boolean desc, int limit) throws SQLException {
	    Cursor cursor =

	            db.query(true, DATABASE_TABLE,
	            		new String[] {KEY_ROWID, KEY_GAME, KEY_GROUP, KEY_PLAYER, KEY_DATE, KEY_SCORE},
	            		KEY_GROUP + "=?", new String[] {category}, null, null,
	            		KEY_SCORE + (desc ? " DESC" : ""), String.format("%d", limit));
	    return cursor;
	}

	public Cursor getScoresByGame(String game, boolean desc, int limit) throws SQLException {
	    Cursor cursor =

	            db.query(true, DATABASE_TABLE,
	            		new String[] {KEY_ROWID, KEY_GAME, KEY_GROUP, KEY_PLAYER, KEY_DATE, KEY_SCORE},
	            		KEY_GAME + "=?", new String[] {game}, null, null,
	            		KEY_SCORE + (desc ? " DESC" : ""), String.format("%d", limit));
	    return cursor;
	}

	public Cursor getScoresByPlayer(String player) throws SQLException {

	    Cursor cursor =

	            db.query(true, DATABASE_TABLE,
	            		new String[] {KEY_ROWID, KEY_GAME, KEY_GROUP, KEY_PLAYER, KEY_DATE, KEY_SCORE},
	            		KEY_PLAYER + "=?", new String[] {player}, null, null, null, null);
	    return cursor;
	}

	public int getScoresCountByGame(String game) throws SQLException {
	    Cursor cursor = db.rawQuery("SELECT COUNT(game) FROM Scores WHERE game=?",
	    							new String[] {game});
	    if(cursor.moveToFirst()) {
	    	int result = cursor.getInt(0);
	    	cursor.close();
	        return result;
	    }
	    cursor.close();
	    return 0;
	}

	public int getScoresCountByPlayer(String player) throws SQLException {
	    Cursor cursor = db.rawQuery("SELECT COUNT(game) FROM Scores WHERE player=?",
	    							new String[] {player});
	    if(cursor.moveToFirst()) {
	    	int result = cursor.getInt(0);
	    	cursor.close();
	        return result;
	    }
	    cursor.close();
	    return 0;
	}

	public int deleteScores(String game) {
		return db.delete(DATABASE_TABLE, KEY_GAME + "=?", new String [] {game});
	}

	public boolean updateScore(long rowId, int score) {
	    ContentValues args = new ContentValues();
	    args.put(KEY_SCORE, score);

	    return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public int getAllScoresCount() {
	    Cursor cursor = db.rawQuery("SELECT COUNT(game) FROM Scores", null);
	    if(cursor.moveToFirst()) {
	        int result = cursor.getInt(0);
	        cursor.close();
	        return result;
	    }
	    cursor.close();
	    return 0;
	}

}
