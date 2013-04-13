package gr.sullenart.games.puzzles.gameengine.solo;

import gr.sullenart.games.puzzles.PuzzleView;
import gr.sullenart.games.puzzles.R;
import gr.sullenart.games.puzzles.gameengine.SoloPuzzle;
import gr.sullenart.games.puzzles.gameengine.solo.SoloGame.SoloGameType;
import gr.sullenart.time.TimeCounter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SoloCustomBoardActivity extends Activity {
	
	static final int DIALOG_DIRECTIONS_ALERT_ID = 0;
	static final int DIALOG_SAVE_BOARD_ID = 1;	
	
	private SoloPuzzle puzzle;
	
	private PuzzleView puzzleView;
	
	private SoloPuzzleRepository soloPuzzleRepository;
	
	private Dialog directionsDialog;
	private Dialog saveDialog;
	
	private String boardName;
	private int [] board;
	
	public static boolean showDirections = true;
	
	public static boolean boardsListUpdated = false;
	
	private SoloGameType boardType;
	private int targetPosition;	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        
        board = null;
        targetPosition = -1;
        
        if (bundle != null) {
        	board = bundle.getIntArray("board");
        	boardName = bundle.getString("boardName");
        	targetPosition = bundle.getInt("targetPosition");
        	boardType = SoloGameType.getGameType(bundle.getInt("boardType"));
        }
		
		setContentView(R.layout.custom_board);
		
		TextView editBoardMessage = (TextView) findViewById(R.id.custom_board_edit_message);
		if (boardName == null || board.length == 0) {
			editBoardMessage.setText(getResources().getString(R.string.edit_new_board));
		}
		else {
			editBoardMessage.setText(getResources().getString(R.string.edit_board) +
									 " " + boardName);
		}
		
    	// Create Puzzle View
    	//SharedPreferences preferences =
    	//	PreferenceManager.getDefaultSharedPreferences(getBaseContext());		
    	//String theme = preferences.getString("Solo_Theme", "wood");        
        puzzle = new SoloPuzzle(this);        
        
		SoloGame soloGame = new SoloGame(board, targetPosition, boardType);
		puzzle.setGame(soloGame);
		puzzle.init();
		puzzle.setEditMode(true);
		puzzleView = (PuzzleView) findViewById(R.id.edit_puzzle_view);
		puzzleView.setPuzzle(puzzle);
		puzzleView.setTimeCounter(new TimeCounter());	
    }
	
    @Override
    public void onStart() {
    	super.onStart();
    	SharedPreferences preferences =
    		PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        soloPuzzleRepository = new SoloPuzzleRepository(getApplicationContext());
        puzzle.setSoloPuzzleRepository(soloPuzzleRepository); 

		if (board == null && puzzle.configure(preferences)) {
    		puzzleView.invalidate();
    	}
    	
    	boolean showIntructionsFlag = 
    		preferences.getBoolean("Show_Custom_Board_Instructions", true);
    	
    	if (showIntructionsFlag && showDirections) {
    		showDirections = false;
    		showDialog(DIALOG_DIRECTIONS_ALERT_ID);
    		directionsDialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
    	}
    	boardsListUpdated = false;
    }	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.custom_board_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);

		MenuItem saveMenuItem = menu.findItem(R.id.save);
		boolean savingEnabled = false;
		int size = Solo.GET_BOARD_SIZE(puzzle.getBoardTable());
		SoloBoard soloBoard = new SoloBoard(puzzle.getBoardTable(),size,size,
									boardType, targetPosition);
		savingEnabled = soloBoard.isBoardValid() && !soloBoard.isNotSolvable();
		saveMenuItem.setEnabled(savingEnabled);
		
		MenuItem targetPositionMenuItem = menu.findItem(R.id.board_target_position);
		targetPositionMenuItem.setVisible(targetPosition >= 0);
		
		if (puzzle.isInSetTargetMode()) {
			targetPositionMenuItem.setTitle(R.string.edit_board);
		}
		else {
			targetPositionMenuItem.setTitle(R.string.target_position);
		}
		
		MenuItem optionsMenuItem = menu.findItem(R.id.board_options);
		optionsMenuItem.setVisible(false);
		return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.save:
        	if (boardName == null || boardName.length() == 0) {
        		showDialog(DIALOG_SAVE_BOARD_ID);
        		saveDialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
        	}
        	else {
                targetPosition = puzzle.getTargetPosition();
        		if (!soloPuzzleRepository.updateGame(boardName, 
        				new SoloGame(puzzle.getBoardTable(), targetPosition, boardType))) {
        			Toast toast = Toast.makeText(getApplicationContext(), R.string.an_error_occurred, Toast.LENGTH_LONG);
        			toast.show();
        		}
        		else {
        			saveNewBoardAsSelected();
        			finish();
        		}
        	}
        	break;
        case R.id.board_target_position:
        	if (puzzle.isInSetTargetMode()) {
        		puzzle.setInSetTargetMode(false);
        	}
        	else {
        		puzzle.setInSetTargetMode(true);
        	}
        	break;
        case R.id.board_options:       	       	
        	break;
        }
        return true;
    }
    
    private void saveNewBoardAsSelected() {
        final SharedPreferences preferences =
    		PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("Solo_Puzzle_Type", boardName);
		editor.commit();
		boardsListUpdated = true;
    }    

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch(id) {
        case DIALOG_DIRECTIONS_ALERT_ID:
        	dialog = createDirectionsAlertDialog();
            break;
        case DIALOG_SAVE_BOARD_ID:
        	dialog = createSaveBoardDialog();
        	break;
        }

        return dialog;
    }    
    
    private Dialog createDirectionsAlertDialog() {
    	directionsDialog = new Dialog(this);
    	directionsDialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
    	directionsDialog.setContentView(R.layout.instructions_dialog);
    	directionsDialog.setTitle(R.string.app_name);
    	
        Button okButton = (Button) directionsDialog.findViewById(R.id.instructions_ok_button);

        CheckBox instructionsCheckbox = (CheckBox)
        	directionsDialog.findViewById(R.id.show_instructions_again);
        
        instructionsCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        final SharedPreferences preferences =
		    		PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("Show_Custom_Board_Instructions", !isChecked);
				editor.commit();				
			}
		});
        
        okButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				directionsDialog.dismiss();
			}
        });
 	
    	return directionsDialog;
    }
    
    private Dialog createSaveBoardDialog() {
        saveDialog = new Dialog(this);
    	saveDialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        saveDialog.setContentView(R.layout.new_board_dialog);
        saveDialog.setTitle(R.string.app_name);

        Button okButton = (Button) saveDialog.findViewById(R.id.new_board_ok_button);

        okButton.setOnClickListener(new OnClickListener() {
        			public void onClick(View v) {
        				boardName = ((EditText) saveDialog.findViewById(R.id.board_name_prompt))
        										.getText().toString();
        				if (boardName == null || boardName.length() == 0) {
        					TextView boardErrorText = (TextView) saveDialog.findViewById(R.id.add_board_error);
        					boardErrorText.setVisibility(View.VISIBLE);
        					boardErrorText.setText(R.string.name_cant_be_empty);
        				}
        				else if (soloPuzzleRepository.nameExists(boardName) ||
        				    !soloPuzzleRepository.addGame(boardName, 
        				    		new SoloGame(puzzle.getBoardTable(), targetPosition, boardType))) {
        					TextView boardErrorText = (TextView) saveDialog.findViewById(R.id.add_board_error);
        					boardErrorText.setVisibility(View.VISIBLE);
        					boardErrorText.setText(R.string.name_already_in_use);
        				}
        				else {
        					saveNewBoardAsSelected();
        					saveDialog.dismiss();
        					finish();
        				}
        			}
        		});         
        return saveDialog;    	
    }
}
