package gr.sullenart.games.puzzles;

import gr.sullenart.ads.AdsManager;
import gr.sullenart.scores.Score;
import gr.sullenart.scores.ScoreItemAdapter;
import gr.sullenart.scores.ScoresManager;
import gr.sullenart.scores.ScoresManagerFactory;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ScoresActivity extends ListActivity  {
	private ScoresManager scoresManager;

	public static String [] gameGroups = {"Q8Puzzle", "NumberSquarePuzzle",  "Solo"};
	
	private String gameGroup = "";

	private TextView emptyView;

	private AdsManager adsManager = new AdsManager();

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);

    	setContentView(R.layout.scores);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        int type =  bundle.getInt("GameType");
        if (type >= 0 && type < gameGroups.length) {
        	 gameGroup = gameGroups[type];
        }

        emptyView = (TextView) findViewById(R.id.empty);

        final ListView listView = getListView();
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setEmptyView(emptyView);

        scoresManager = ScoresManagerFactory.getScoresManager(getApplicationContext());

        LinearLayout layout = (LinearLayout)findViewById(R.id.banner_layout_scores);
        adsManager.addAdsView(this, layout);
    }

	   @Override
	    protected void onResume() {
	        super.onResume();

	        List<Score> scores = scoresManager.getScoresByGroup(gameGroup);
	        setListAdapter(new ScoreItemAdapter(this, scores));
	    }
}
