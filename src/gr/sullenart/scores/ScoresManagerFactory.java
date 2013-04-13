package gr.sullenart.scores;

import android.content.Context;

public class ScoresManagerFactory {

	private static ScoresDbAdapter scoresDbAdapter = null;
	private static ScoresManager scoresManager = null;

	public static ScoresManager getScoresManager(Context context) {
		if (scoresManager != null) {
			return scoresManager;
		}

        scoresDbAdapter = new ScoresDbAdapter(context);

        scoresManager = new ScoresManager();
        scoresManager.setScoresDbAdapter(scoresDbAdapter);
        scoresManager.setHigherIsBetter(false);
        scoresManager.setHighScoresPerGame(3);

        return scoresManager;
	}

}
