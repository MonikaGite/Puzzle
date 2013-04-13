package gr.sullenart.games.puzzles;

import java.util.List;

import gr.sullenart.games.puzzles.gameengine.solo.SoloPuzzleRepository;
import gr.sullenart.games.puzzles.gameengine.solo.SoloPuzzleRepository.PuzzleKeyName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class PuzzleOptionsActivity extends PreferenceActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            int gameResources = bundle.getInt("GameResources", 0);
            if (gameResources !=  R.xml.solo_preferences) {
            	addPreferencesFromResource(gameResources);
            }
            else {
            	addPreferencesFromResource(gameResources);
            	
            	SoloPuzzleRepository puzzlesRepository =
            		new SoloPuzzleRepository(getApplicationContext());
            	
            	List<PuzzleKeyName> boards = puzzlesRepository.getBoards();
            	
            	ListPreference listPreferenceCategory = (ListPreference) findPreference("Solo_Puzzle_Type");
            	if (listPreferenceCategory != null) {
            		CharSequence [] entries = new CharSequence[boards.size()];
            		CharSequence [] entryValues = new CharSequence[boards.size()];      		
            		int i = 0;
            		for(PuzzleKeyName board: boards) {
            			entries[i] = board.getName();
            			entryValues[i] = board.getKey();
            			i++;
            		}
            	    listPreferenceCategory.setEntries(entries);
            	    listPreferenceCategory.setEntryValues(entryValues);
            	}
            }
    }
}
