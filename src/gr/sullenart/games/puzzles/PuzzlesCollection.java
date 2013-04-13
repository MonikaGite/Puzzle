package gr.sullenart.games.puzzles;

import gr.sullenart.ads.AdsManager;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class PuzzlesCollection extends Activity {
	static final int DIALOG_SELECT_GAME_TYPE_ID = 0;
	static final int DIALOG_ABOUT_ID = 1;

	private Dialog aboutDialog;

	private final int START_GAME_ACTION = 0;
	private final int SHOW_SCORES_ACTION = 1;
	private int action;

	private PopupWindow gameSelectPopupWindow = null;
	
	private AdsManager adsManager = new AdsManager();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final Button newGameButton = (Button) findViewById(R.id.newGameButton);
        final Button scoresButton = (Button) findViewById(R.id.scoresButton);
        Button helpButton = (Button) findViewById(R.id.helpButton);

        newGameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				action = START_GAME_ACTION;
				//showDialog(DIALOG_SELECT_GAME_TYPE_ID);
				showGameSelectPopup((View) newGameButton);
			}
		});

        helpButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent  = new Intent("gr.sullenart.games.puzzles.WEB_VIEW");
				startActivity(intent);
			}
		});

        scoresButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				action = SHOW_SCORES_ACTION;
				showGameSelectPopup((View) scoresButton);
				//showDialog(DIALOG_SELECT_GAME_TYPE_ID);
			}
		});

        LinearLayout layout = (LinearLayout)findViewById(R.id.banner_layout);
        adsManager.addAdsView(this, layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.about:
        	showDialog(DIALOG_ABOUT_ID);
        	break;
        case R.id.help:
        	Intent intent  = new Intent("gr.sullenart.games.puzzles.WEB_VIEW");
			startActivity(intent);
        	break;
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case DIALOG_SELECT_GAME_TYPE_ID:
        	dialog = createSelectGameTypeDialog();
        	break;
        case DIALOG_ABOUT_ID:
        	dialog = createAboutDialog();
        	aboutDialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.icon);
        	break;
        default:
            dialog = null;
        }

        return dialog;
    }

    @Override
    public void onStop() {
    	if (gameSelectPopupWindow != null) {
    		gameSelectPopupWindow.dismiss();
    	}
    	super.onStop();
    }
    
    private void showGameSelectPopup(View parentView) {
		LayoutInflater inflater = (LayoutInflater)
					getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.puzzle_select, null, false);
		
		gameSelectPopupWindow = new PopupWindow(this);
		gameSelectPopupWindow.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					gameSelectPopupWindow.dismiss();
					gameSelectPopupWindow = null;
					return true;
				}
				return false;
			}
		});
		gameSelectPopupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		gameSelectPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		gameSelectPopupWindow.setTouchable(true);
		gameSelectPopupWindow.setFocusable(true);
		gameSelectPopupWindow.setOutsideTouchable(true);
		gameSelectPopupWindow.setContentView(layout);
		
		int i = 0;
		final Map<View, Integer> gameButtonsMap = new HashMap<View, Integer>();
		gameButtonsMap.put(layout.findViewById(R.id.queens_select), i++);
		gameButtonsMap.put(layout.findViewById(R.id.solo_select), i++);
		
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int gameType = gameButtonsMap.get(v);
				gameSelectPopupWindow.dismiss();
				if (action == START_GAME_ACTION) {
					Intent intent  = new Intent("gr.sullenart.games.puzzles.PUZZLES");
					intent.putExtra("GameType", gameType);
					startActivity(intent);
				}
				else if (action == SHOW_SCORES_ACTION) {
					Intent intent  = new Intent("gr.sullenart.games.puzzles.SCORES");
					intent.putExtra("GameType", gameType);
					startActivity(intent);
				}
			}
		};
		for(View view: gameButtonsMap.keySet()) {
			view.setOnClickListener(clickListener);
		}
		
		int [] location = new int [] {0, 0};
		parentView.getLocationInWindow(location);
		int w = parentView.getWidth();
		// Pop-up width is 1.5x the size of the button
		int x = location[0] - w/4;
		int y = location[1] + parentView.getHeight();
		
		gameSelectPopupWindow.setAnimationStyle(R.style.AnimationPopup);
		gameSelectPopupWindow.showAtLocation(layout, Gravity.NO_GRAVITY, x, y);     	
    }   
    
    private Dialog createSelectGameTypeDialog() {
    	final CharSequence[] items = getResources().getTextArray(R.array.game_types);

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.game_type);
    	builder.setItems(items, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
				if (action == START_GAME_ACTION) {
					Intent intent  = new Intent("gr.sullenart.games.puzzles.PUZZLES");
					intent.putExtra("GameType", item);
					startActivity(intent);
				}
				else if (action == SHOW_SCORES_ACTION) {
					Intent intent  = new Intent("gr.sullenart.games.puzzles.SCORES");
					intent.putExtra("GameType", item);
					startActivity(intent);
				}
    	    }
    	});
    	AlertDialog alert = builder.create();
    	return alert;
    }

    private Dialog createAboutDialog() {
        aboutDialog = new Dialog(this);
        aboutDialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
        aboutDialog.setContentView(R.layout.about_dialog);
        aboutDialog.setTitle(R.string.app_name);

        Button button = (Button) aboutDialog.findViewById(R.id.help_ok_button);

        button.setOnClickListener(new OnClickListener() {
        			public void onClick(View v) {
        				aboutDialog.dismiss();
        			}
        		});

        return aboutDialog;
    }

}