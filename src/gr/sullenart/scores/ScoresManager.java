package gr.sullenart.scores;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

public class ScoresManager {

	private final String LOG_TAG = ScoresManager.class.getName();

	public int highScoresPerGame = 10;

	private boolean higherIsBetter = true;

	public ScoresDbAdapter scoresDbAdapter;

	public int getHighScoresPerGame() {
		return highScoresPerGame;
	}

	public void setHighScoresPerGame(int scoresPerGame) {
		this.highScoresPerGame = scoresPerGame;
	}

	public void setScoresDbAdapter(ScoresDbAdapter scoresDbAdapter) {
		this.scoresDbAdapter = scoresDbAdapter;
	}

	public boolean getHigherIsBetter() {
		return higherIsBetter;
	}

	public void setHigherIsBetter(boolean higherIsBetter) {
		this.higherIsBetter = higherIsBetter;
	}

	public boolean addScore(String game, String group, String player, int score) {
		try {
			scoresDbAdapter.open();

			int count = scoresDbAdapter.getScoresCountByGame(game);

			boolean result = true;
			if (count > highScoresPerGame) {
				Cursor cursor = scoresDbAdapter.getScoresByGame(game,
						higherIsBetter, highScoresPerGame);
				if (cursor.moveToLast()) {
					int worseScore = cursor.getInt(ScoresDbAdapter.SCORE_COLUMN_INDEX);
					if (higherIsBetter) {
						scoresDbAdapter.deleteScoresLessThan(game, worseScore);
						result = worseScore < score;
					}
					else {
						scoresDbAdapter.deleteScoresHigherThan(game, worseScore);
						result = worseScore > score;
					}
				}
				cursor.close();
			}
			if (result) {
				long id = scoresDbAdapter.addScore(game, group, player, score);
				if (id < 0) {
					result =  false;
				}
			}
			scoresDbAdapter.close();

			return result;
		}
		catch (SQLException e) {
			Log.e(LOG_TAG, "Error adding score: " + e.getMessage());
		}
		return false;
	}

	public boolean isHighScore(String game, int score) {
		try {
			scoresDbAdapter.open();

			int count = scoresDbAdapter.getScoresCountByGame(game);

			if (count < highScoresPerGame) {
				scoresDbAdapter.close();
				return true;
			}

			Cursor cursor = scoresDbAdapter.getScoresByGame(game , higherIsBetter, highScoresPerGame);
			if (cursor.moveToLast()) {
				boolean result;
				int worseScore  = cursor.getInt(ScoresDbAdapter.SCORE_COLUMN_INDEX);
				if (higherIsBetter) {
					result = score > worseScore;
				}
				else {
					result = score < worseScore;
				}
				cursor.close();
				scoresDbAdapter.close();
				return result;
			}
			cursor.close();
			scoresDbAdapter.close();
			return false;
		}
		catch (SQLException e) {
			Log.e(LOG_TAG, "Error checking high score: " + e.getMessage());
		}
		return false;
	}

	public List<Score> getScores(String game) {
		List<Score> scores = new ArrayList<Score>();

		try {
			scoresDbAdapter.open();
			Cursor cursor = scoresDbAdapter.getScoresByGame(game, higherIsBetter);
			if (cursor.moveToFirst()) {
				do {
					Score score = new Score();
					score.setGame(game);
					score.setPlayer(cursor.getString(ScoresDbAdapter.PLAYER_COLUMN_INDEX));
					score.setScore(cursor.getInt(ScoresDbAdapter.SCORE_COLUMN_INDEX));
					score.setGroup(cursor.getString(ScoresDbAdapter.GROUP_COLUMN_INDEX));

					String dateStr = cursor.getString(ScoresDbAdapter.DATE_COLUMN_INDEX);

				    ParsePosition pos = new ParsePosition(0);
				    SimpleDateFormat formatter = new SimpleDateFormat(ScoresDbAdapter.DATE_FORMAT);
				    try {
				    	Date date = formatter.parse(dateStr, pos);
				    	score.setDate(date);
				    } catch (IllegalArgumentException e) {

				    }

				    scores.add(score);
				} while (cursor.moveToNext());
			}
			cursor.close();
			scoresDbAdapter.close();
		}
		catch (SQLException e) {
			Log.e(LOG_TAG, "Error retrieving scores: " + e.getMessage());
		}
		return scores;
	}

	public List<Score> getScoresByGroup(String group) {
		List<Score> scores = new ArrayList<Score>();

		try {
			scoresDbAdapter.open();
			Cursor cursor = scoresDbAdapter.getScoresByGroup(group, higherIsBetter);
			if (cursor.moveToFirst()) {
				do {
					Score score = new Score();
					score.setGame(cursor.getString(ScoresDbAdapter.GAME_COLUMN_INDEX));
					score.setPlayer(cursor.getString(ScoresDbAdapter.PLAYER_COLUMN_INDEX));
					score.setScore(cursor.getInt(ScoresDbAdapter.SCORE_COLUMN_INDEX));
					score.setGroup(cursor.getString(ScoresDbAdapter.GROUP_COLUMN_INDEX));

					String dateStr = cursor.getString(ScoresDbAdapter.DATE_COLUMN_INDEX);

				    ParsePosition pos = new ParsePosition(0);
				    SimpleDateFormat formatter = new SimpleDateFormat(ScoresDbAdapter.DATE_FORMAT);
				    try {
				    	Date date = formatter.parse(dateStr, pos);
				    	score.setDate(date);
				    } catch (IllegalArgumentException e) {

				    }

				    scores.add(score);
				} while (cursor.moveToNext());
			}
			cursor.close();
			scoresDbAdapter.close();
		}
		catch (SQLException e) {
			Log.e(LOG_TAG, "Error retrieving scores: " + e.getMessage());
		}
		
		return scores;
	}

}
