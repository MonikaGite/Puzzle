package gr.sullenart.scores;

import gr.sullenart.games.puzzles.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ScoreItemAdapter extends BaseAdapter {

    private final Context context;
    private final List<GameScores> scores;

    class GameScores {
    	private String name;
    	private List<Score> scores;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<Score> getScores() {
			return scores;
		}
		public void setScores(List<Score> scores) {
			this.scores = scores;
		}
    }

    public ScoreItemAdapter(Context context, List<Score> scores) {
        this.context = context;

        Map<String, GameScores> scoresMap = new HashMap<String, GameScores>();
        for(Score score: scores) {
        	if (!scoresMap.containsKey(score.getGame())) {
        		GameScores gameScores = new GameScores();
        		gameScores.setName(score.getGame());
        		gameScores.setScores(new ArrayList<Score> ());

        		scoresMap.put(score.getGame(), gameScores);
        	}
        	scoresMap.get(score.getGame()).getScores().add(score);
        }

        this.scores = new ArrayList<GameScores>(scoresMap.values());
		
        Collections.sort(this.scores, new Comparator<GameScores> () {
			@Override
			public int compare(GameScores score1, GameScores score2) {
				return score1.getName().compareTo(score2.getName());
			}
		});
    }

	@Override
	public int getCount() {
		return this.scores.size();
	}

	@Override
	public Object getItem(int position) {
		return this.scores.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GameScores gameScores = this.scores.get(position);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
											Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout)
				inflater.inflate(R.layout.score_item, null, false);

		((TextView) view.findViewById(R.id.score_game_type)).setText(gameScores.getName());

		List<Score> highScores = gameScores.getScores();
		TextView textViewName1 = ((TextView) view.findViewById(R.id.score_1));
		TextView textViewName2 = ((TextView) view.findViewById(R.id.score_2));
		TextView textViewName3 = ((TextView) view.findViewById(R.id.score_3));
		TextView textViewTime1 = ((TextView) view.findViewById(R.id.score_1_time));
		TextView textViewTime2 = ((TextView) view.findViewById(R.id.score_2_time));
		TextView textViewTime3 = ((TextView) view.findViewById(R.id.score_3_time));	
		TableRow row1 = (TableRow) view.findViewById(R.id.score_1_row);
		TableRow row2 = (TableRow) view.findViewById(R.id.score_2_row);
		TableRow row3 = (TableRow) view.findViewById(R.id.score_3_row);
		
		TextView [] textViewNames = {textViewName1, textViewName2, textViewName3};
		TextView [] textViewTimes = {textViewTime1, textViewTime2, textViewTime3};
		TableRow [] rows = {row1, row2, row3};
		for(int i=0; i<textViewNames.length; i++) {
			if (highScores.size() > i) {
				Score score = highScores.get(i);
				textViewNames[i].setText(getPlayersDisplayName(score.getPlayer()));
				textViewTimes[i].setText(
						String.format("%02d:%02d", score.getScore()/60, score.getScore()%60));
			}
			else {
				rows[i].setVisibility(View.GONE);
			}
		}

		return view;
	}

	private final int MAX_ALLOWED_NAME_LENGTH = 20;
	
	private String getPlayersDisplayName(String name) {
		if (name.length() > MAX_ALLOWED_NAME_LENGTH) {
			return name.substring(0, MAX_ALLOWED_NAME_LENGTH) + ".. ";
		}
		else {
			String delimiter = "";
			for(int i=name.length(); i<13; i++) {
				delimiter += " ";
			}
			return name + delimiter;
		}
	}

}
