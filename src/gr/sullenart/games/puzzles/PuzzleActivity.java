package gr.sullenart.games.puzzles;

import gr.sullenart.games.puzzles.gameengine.NumberSquarePuzzle;
import gr.sullenart.games.puzzles.gameengine.Puzzle;
import gr.sullenart.games.puzzles.gameengine.Puzzle.MoveResult;
import gr.sullenart.games.puzzles.gameengine.Q8Puzzle;
import gr.sullenart.games.puzzles.gameengine.SoloPuzzle;
import gr.sullenart.games.puzzles.gameengine.solo.SoloPuzzleRepository;
import gr.sullenart.scores.ScoresManager;
import gr.sullenart.scores.ScoresManagerFactory;
import gr.sullenart.time.TimeCounter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PuzzleActivity extends Activity {

	static final int DIALOG_FINISH_GAME_ALERT_ID = 0;
	static final int DIALOG_PLEASE_WAIT_ID = 1;
	static final int DIALOG_HIGH_SCORE_NAME = 2;

	private Puzzle puzzle;
	private PuzzleView puzzleView;

	private Dialog pleaseWaitDialog;

	private SolverTask solverTask;

	private boolean cancelSolveFlag;

	private int gameType = 0;
	private int optionResources [] = { R.xml.q8_preferences,
									   R.xml.knights_tour_preferences,
									   R.xml.solo_preferences} ;

	private boolean timerIsRunning = false;
	private Handler timerHandler;

	private TimeCounter timeCounter;

	private boolean isSolverUsed = false;
    
    private boolean menuFocus = false;

	private Runnable timerRunnable = new Runnable() {
		@Override
		public void run() {
			if (puzzle != null && puzzleView != null &&
					(puzzle.isStarted() || puzzle.isSolved()) &&
					!puzzle.isSolverRunning() ) {
				puzzleView.invalidate();
			}
			timerHandler.postDelayed(timerRunnable, 1000);
		}
	};

	private int replayIntervalMs = 500;
	private boolean replayTimerRunning = false;
	private Handler replayHandler;

	private ScoresManager scoresManager;

	private int score;

	private Button undoButton;
	private Button restartButton;
	private Button solveButton;
	private Button stopButton;
	private Button replayButton;
	
	private TextView noMoreMovesMessage;
	
	private Runnable replayRunnable = new Runnable() {
		@Override
		public void run() {
			if (!puzzle.replay()) {
				replayHandler.postDelayed(replayRunnable, replayIntervalMs);
			}
			else {
				replayTimerRunning = false;
				puzzle.setReplayRunning(false);
				updateButtons();
			}
			puzzleView.invalidate();
		}
	};

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        gameType = bundle.getInt("GameType", 0);

        switch(gameType) {
        case 0:
        	puzzle = new Q8Puzzle(this);
        	break;
        case 1:
        	puzzle = new NumberSquarePuzzle(this);
        	break;
        case 2:
        	puzzle = new SoloPuzzle(this);
        	break;
        }

        timeCounter = new TimeCounter();
		puzzle.init();
		
        setContentView(R.layout.game);
        puzzleView = (PuzzleView) findViewById(R.id.game_view);		
		puzzleView.setPuzzle(puzzle);
		puzzleView.setTimeCounter(timeCounter);

		undoButton = (Button) findViewById(R.id.game_button_undo);
		restartButton = (Button) findViewById(R.id.game_button_restart);
		solveButton = (Button) findViewById(R.id.game_button_solve);
		stopButton = (Button) findViewById(R.id.game_button_stop);
		replayButton = (Button) findViewById(R.id.game_button_replay);
		
		noMoreMovesMessage = (TextView) findViewById(R.id.no_more_moves_message);
	
		undoButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				doUndo();
			}
		});
		restartButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				doRestart();
			}
		});
		solveButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				doSolve();
			}
		});
		stopButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				doStop();
			}
		});
		replayButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				doReplay();
			}
		});		
		
		updateButtons();		
		
		timerHandler = new Handler();
		replayHandler = new Handler();

		scoresManager = ScoresManagerFactory.getScoresManager(getApplicationContext());
    }

    @Override
    public void onStart() {
    	super.onStart();
    	SharedPreferences preferences =
    		PreferenceManager.getDefaultSharedPreferences(getBaseContext());

    	if (puzzle instanceof SoloPuzzle) {
    		SoloPuzzleRepository soloPuzzleRepository = 
    			new SoloPuzzleRepository(getApplicationContext());
	    	((SoloPuzzle) puzzle).setSoloPuzzleRepository(soloPuzzleRepository);
    	}    	
    	
    	if (puzzle.configure(preferences)) {
    		timeCounter.reset();
    		puzzleView.invalidate();
    	}
    	updateButtons();
    }

	private void puzzlePause() {
		if (puzzle != null && puzzleView != null &&
				puzzle.isStarted() && !puzzle.isSolved() &&
				!puzzle.isSolverRunning() ) {
    		timeCounter.pause();
    		puzzleView.setPaused(true);				
		}			
	}
	
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	if (!hasFocus && !menuFocus) {
			puzzlePause();
    	}
    	else if (menuFocus) {
    		menuFocus = false;
    	}
    }    
    
    @Override
    public void onPause() {
    	super.onPause();

    	puzzlePause();

    	if (timerIsRunning) {
    		timerHandler.removeCallbacks(timerRunnable);
    	}
    	if (replayTimerRunning) {
    		replayHandler.removeCallbacks(replayRunnable);
    	}
    }

    @Override
    public void onResume() {
    	super.onResume();
    	puzzleView.invalidate();
    	
    	if (timerIsRunning) {
    		timerHandler.postDelayed(timerRunnable, 1000);
    	}
    	if (replayTimerRunning) {
    		replayHandler.postDelayed(replayRunnable , replayIntervalMs);
    	}     	
    }

    public void resume() {
    	timeCounter.resume();   	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);

		MenuItem customBoardsMenuItem = menu.findItem(R.id.custom_boards);
		customBoardsMenuItem.setVisible(puzzle.isAddAllowed() && !puzzle.isStarted());    	
    	
		MenuItem undoMenuItem = menu.findItem(R.id.undo);
		undoMenuItem.setVisible(isUndoVisible());

		MenuItem solveMenuItem = menu.findItem(R.id.solve);
		solveMenuItem.setVisible(isSolveVisible());

		MenuItem replayMenuItem = menu.findItem(R.id.replay);
		replayMenuItem.setVisible(isReplayVisible());

		MenuItem stopMenuItem = menu.findItem(R.id.stop);
		stopMenuItem.setVisible(isStopVisible());

    	MenuItem restartMenuItem = menu.findItem(R.id.restart);
    	restartMenuItem.setVisible(isRestartVisible());
    	menuFocus = true;
    	return true;
    }

    private boolean isUndoVisible() {
    	return puzzle.isUndoPermitted() && puzzle.isStarted() &&
				!puzzle.isSolved();
    }    
    
	private boolean isRestartVisible() {
		return puzzle.isStarted() || puzzle.isSolved() &&
				 !puzzle.isReplayRunning();
	}

	private boolean isReplayVisible() {
		return puzzle.isReplayPermitted() && puzzle.isSolved() &&
				 				  !puzzle.isReplayRunning();
	}

	private boolean isSolveVisible() {
		return puzzle.isSolvePermitted() && !puzzle.isSolved() &&
								 !puzzle.isReplayRunning();
	}
	
	private boolean isStopVisible() {
		return puzzle.isReplayRunning();
	}

    public void updateButtons() {
    	undoButton.setVisibility(isUndoVisible() ? View.VISIBLE : View.GONE);
    	restartButton.setVisibility(isRestartVisible() ? View.VISIBLE : View.GONE);
    	solveButton.setVisibility(isSolveVisible() ? View.VISIBLE : View.GONE);
    	stopButton.setVisibility(isStopVisible() ? View.VISIBLE : View.GONE);
    	replayButton.setVisibility(isReplayVisible() ? View.VISIBLE : View.GONE);
    	
    	noMoreMovesMessage.setVisibility(puzzle.areMovesLeft() && !puzzle.isSolverRunning() ? 
    									 View.VISIBLE : View.GONE) ;
    }
    
    public void hideButtons() {
    	undoButton.setVisibility(View.GONE);
    	restartButton.setVisibility(View.GONE);
    	solveButton.setVisibility(View.GONE);
    	stopButton.setVisibility(View.GONE);
    	replayButton.setVisibility(View.GONE);
    }

	private void doSolve() {
    	isSolverUsed = true;
    	showDialog(DIALOG_PLEASE_WAIT_ID);
    	pleaseWaitDialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
    	cancelSolveFlag = false;
    	timeCounter.reset();
    	timeCounter.setStopTimerOnPause(false);
    	timeCounter.start();
    	startTimer();
    	solverTask = new SolverTask();
    	solverTask.execute();	
	}

	private void doStop() {
		replayHandler.removeCallbacks(replayRunnable);
		puzzle.setReplayRunning(false);
		replayTimerRunning = false;
        updateButtons();
	}

	private void doRestart() {
    	stopTimer();
    	isSolverUsed = false;
    	timeCounter.reset();
    	puzzle.init();
    	puzzleView.invalidate();
        updateButtons();
	}

	private void doReplay() {
    	timeCounter.reset();
    	puzzle.copySolution();
    	puzzle.init();
    	puzzle.setReplayRunning(true);
    	replayTimerRunning = true;
    	replayHandler.postDelayed(replayRunnable , replayIntervalMs);
        updateButtons();
	}

	private void doUndo() {
		puzzle.undoLastMove();
		puzzleView.invalidate();
        updateButtons();
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.options:
			Intent intent  = new Intent("gr.sullenart.games.puzzles.OPTIONS");
			intent.putExtra("GameResources", optionResources[gameType]);
			startActivity(intent);
        	break;
        case R.id.undo:
    		doUndo();
        	break;
        case R.id.replay:
        	doReplay();
            break;
        case R.id.restart:
        	doRestart();
        	break;
        case R.id.stop:
        	doStop();
        	break;
        case R.id.solve:
        	doSolve();
            break;
        case R.id.custom_boards:
        	if (puzzle instanceof SoloPuzzle) {
            	Intent editIntent  = new Intent("gr.sullenart.games.puzzles.SOLO_EDIT_BOARDS");
            	startActivity(editIntent);
        	}
        	break;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	if (puzzle.isStarted() && !puzzle.isSolved()) {
            	showDialog(DIALOG_FINISH_GAME_ALERT_ID);
            	return false;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case DIALOG_FINISH_GAME_ALERT_ID:
        	dialog = createAskUserToFinishGameAlertDialog();
            break;
        case DIALOG_PLEASE_WAIT_ID:
        		pleaseWaitDialog = createPleaseWaitDialog();
        		return pleaseWaitDialog;
        case DIALOG_HIGH_SCORE_NAME:
        	dialog = createHighScoreNameDialog();
        	break;
        default:
            dialog = null;
        }

        return dialog;
    }

    private Dialog createAskUserToFinishGameAlertDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(R.string.finish_game_alert_text)
    	       .setCancelable(false)
    	       .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   finish();
    	           }
    	       })
    	       .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       });
    	AlertDialog alert = builder.create();
    	return alert;
    }

    private Dialog createPleaseWaitDialog() {
        pleaseWaitDialog = new Dialog(this);
        pleaseWaitDialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        pleaseWaitDialog.setContentView(R.layout.please_wait_dialog);
        pleaseWaitDialog.setTitle(R.string.app_name);
        pleaseWaitDialog.setCancelable(false);

        Button button = (Button) pleaseWaitDialog.findViewById(R.id.cancel_button);
        TextView messageText = (TextView) pleaseWaitDialog.findViewById(R.id.messageTextView);
        
        if (puzzle instanceof SoloPuzzle) {
        	messageText.setText(R.string.please_wait_long_message);
        }
        else {
        	messageText.setText(R.string.please_wait);
        }

        button.setOnClickListener(new OnClickListener() {
        			public void onClick(View v) {
        				stopTimer();
        				if (solverTask != null) {
        					cancelSolveFlag = true;
        					puzzle.setSolverRunning(false);
        				}
        			}
        		});

        return pleaseWaitDialog;
    }

    public void startTimer() {
    	if (!timerIsRunning) {
    		Log.d("PuzzleActivity", "-- Start timer -- ");
    		timerHandler.postDelayed(timerRunnable, 1000);
    		timerIsRunning = true;
    	}
    }

    public void stopTimer() {
    	if (timerIsRunning) {
    		Log.d("PuzzleActivity", "-- Stop timer -- ");
    		timerIsRunning = false;
    		timerHandler.removeCallbacks(timerRunnable);
    	}
    }

    public void onPuzzleSolvedByUser() {
    	if (!isSolverUsed) {
    		score = timeCounter.getTimeSeconds();
    		stopTimer();
    		if (scoresManager.isHighScore(puzzle.getName(), score)) {
    			showDialog(DIALOG_HIGH_SCORE_NAME);
    		}
    		else {
	    		String str = String.format(
	    				getResources().getString(R.string.solved_in_seconds, (Object []) null), score);
				Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    		}
    	}
    }

    private Dialog createHighScoreNameDialog() {
        final Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.high_score_name_dialog);
        dialog.setTitle(R.string.app_name);

        Button okButton = (Button) dialog.findViewById(R.id.high_score_name_ok_button);

        final SharedPreferences preferences =
    		PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String oldPlayersName = preferences.getString("players_name", "");

        EditText playersNameEditText = (EditText) dialog.findViewById(R.id.players_name);
        playersNameEditText.setText(oldPlayersName);

        okButton.setOnClickListener(new OnClickListener() {
        			public void onClick(View v) {
        				dialog.dismiss();
        				String playerName = ((EditText) dialog.findViewById(R.id.players_name))
        										.getText().toString();
        				scoresManager.addScore(puzzle.getName(), puzzle.getFamily(),
        									   playerName, score);
        				SharedPreferences.Editor editor = preferences.edit();
        				editor.putString("players_name", playerName);
        				editor.commit();
        			}
        		});
        return dialog;
    }

    private class SolverTask extends AsyncTask<Void, Void, MoveResult> {

		@Override
		protected MoveResult doInBackground(Void... arg0) {
        	MoveResult result = puzzle.solve(false);

        	while(result != MoveResult.RIDDLE_SOLVED &&
        			result != MoveResult.RIDDLE_UNSOLVABLE &&
        			!cancelSolveFlag) {
        		result = puzzle.solve(false);
        	}

			return result;
		}

		@Override
		protected void onPostExecute(MoveResult result) {
			stopTimer();
			puzzleView.invalidate();
			pleaseWaitDialog.dismiss();
			updateButtons();
			if (result == MoveResult.RIDDLE_UNSOLVABLE) {
				Toast.makeText(PuzzleActivity.this,
						R.string.riddle_cant_be_solved, Toast.LENGTH_SHORT).show();
			}
		}
    }

}
