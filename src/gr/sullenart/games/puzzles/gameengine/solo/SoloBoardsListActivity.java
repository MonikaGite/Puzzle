package gr.sullenart.games.puzzles.gameengine.solo;

import gr.sullenart.games.puzzles.R;
import gr.sullenart.games.puzzles.gameengine.solo.SoloGame.SoloGameType;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class SoloBoardsListActivity extends ListActivity {

	static final int DIALOG_CHOICES_ID = 0;
	static final int DIALOG_BOARD_SETTINGS_ID = 1;
	static final int DIALOG_ERROR_ALERT_ID = 2;
	
	private SoloPuzzleRepository puzzleRepository;

	private Dialog choicesDialog;

	private String boardSelected;
	
	private ArrayAdapter<String> arrayAdapter;
	
	private List<String> boards;
	
	private Dialog boardSettingsDialog;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	  	if (position < 1) {
					showDialog(DIALOG_BOARD_SETTINGS_ID);      		  
        	  	}
        	  	else {
        	  		boardSelected = ((TextView) view).getText().toString();
        	  		removeDialog(DIALOG_CHOICES_ID);
        	  		showDialog(DIALOG_CHOICES_ID);
        	  	}
          	}
        });
	}
	
    @Override
    public void onStart() {
    	super.onStart();
    	puzzleRepository = new SoloPuzzleRepository(getApplicationContext());
        updateList();
    }
	
	private void updateList() {
        boards = puzzleRepository.getCustomBoardNames();
        boards.add(0, "-- " + getResources().getString(R.string.new_board) + " --");
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 
        										boards);
        setListAdapter(arrayAdapter);		
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case DIALOG_CHOICES_ID:
        	dialog = createChoicesDialog();
            break;
        case DIALOG_BOARD_SETTINGS_ID:
        	dialog = createBoardSettingsDialog();
        	break;            
        case DIALOG_ERROR_ALERT_ID:
        	dialog = createErrorAlertDialog();
        	break;
        default:
            dialog = null;
        }

        return dialog;
    }
    
    private Dialog createErrorAlertDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(R.string.an_error_occurred)
    	       .setCancelable(false)
    	       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   finish();
    	           }
    	       });
    	AlertDialog alert = builder.create();
    	return alert;    	
    }
    
    private Dialog createChoicesDialog() {
        choicesDialog = new Dialog(this);
        choicesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        choicesDialog.setContentView(R.layout.edit_board);

        Button closeButton = (Button) choicesDialog.findViewById(R.id.close_edit_board);
        final Button editButton = (Button) choicesDialog.findViewById(R.id.edit_board_button);
        final Button deleteButton = (Button) choicesDialog.findViewById(R.id.delete_board_button);
        final Button renameButton = (Button) choicesDialog.findViewById(R.id.rename_board_button);
        Button deleteOkButton = (Button) choicesDialog.findViewById(R.id.delete_board_ok_button);
        Button deleteCancelButton = (Button) choicesDialog.findViewById(R.id.delete_board_cancel_button);        
        Button renameOkButton = (Button) choicesDialog.findViewById(R.id.rename_board_ok_button);
        Button renameCancelButton = (Button) choicesDialog.findViewById(R.id.rename_board_cancel_button);
        
        ((TextView) choicesDialog.findViewById(R.id.board_name_text)).setText(boardSelected);
        ((EditText) choicesDialog.findViewById(R.id.board_new_name)).setText(boardSelected);
        
        closeButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						choicesDialog.dismiss();
					}
        		});

        editButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				SoloCustomBoardActivity.showDirections = true;
				Intent addIntent  = new Intent("gr.sullenart.games.puzzles.SOLO_ADD_BOARD");
				SoloGame soloGame = puzzleRepository.getGame(boardSelected);
				int [] board = soloGame.getBoard();
				addIntent.putExtra("boardName", boardSelected);
				addIntent.putExtra("board", board);
				addIntent.putExtra("targetPosition", soloGame.getTargetPosition());
				addIntent.putExtra("boardType", soloGame.getType().getCode());
				addIntent.putExtra("boardSize", Solo.GET_BOARD_SIZE(board));
				startActivity(addIntent);
				
				choicesDialog.dismiss();
			}
		});
        
        deleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				editButton.setEnabled(false);
				renameButton.setEnabled(false);
				choicesDialog.findViewById(R.id.delete_confirm_toolbar).setVisibility(View.VISIBLE);
			}
		});
        
        deleteOkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				choicesDialog.dismiss();
				if (!puzzleRepository.deleteGame(boardSelected)) {
					showDialog(DIALOG_ERROR_ALERT_ID);
				}
				else {
					//boards.remove(boardSelected);
					arrayAdapter.notifyDataSetChanged();
				}
			}
		});  
        
        deleteCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				editButton.setEnabled(true);
				renameButton.setEnabled(true);
				choicesDialog.findViewById(R.id.delete_confirm_toolbar).setVisibility(View.GONE);
			}
		});          
        
        renameButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				editButton.setEnabled(false);
				deleteButton.setEnabled(false);				
				choicesDialog.findViewById(R.id.rename_toolbar).setVisibility(View.VISIBLE);
			}
		});
        
        renameOkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String newName = 
					((EditText) choicesDialog.findViewById(R.id.board_new_name)).getText().toString();
				if (puzzleRepository.nameExists(newName)) {
					choicesDialog.findViewById(R.id.new_name_exists_message).setVisibility(View.VISIBLE);
				}
				else {
					choicesDialog.dismiss();
					if (!puzzleRepository.renameGame(boardSelected, newName)) {
						showDialog(DIALOG_ERROR_ALERT_ID);
					}
					else {
						//int pos = boards.indexOf(boardSelected);
						//boards.set(pos, newName);
						arrayAdapter.notifyDataSetChanged();
					}
				}
			}
		}); 
        
        renameCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				editButton.setEnabled(true);
				deleteButton.setEnabled(true);
				choicesDialog.findViewById(R.id.new_name_exists_message).setVisibility(View.GONE);
				choicesDialog.findViewById(R.id.rename_toolbar).setVisibility(View.GONE);
			}
		});         
        return choicesDialog;
    }
    
    private Dialog createBoardSettingsDialog() {
		boardSettingsDialog = new Dialog(this);
		boardSettingsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		boardSettingsDialog.setContentView(R.layout.board_settings);
		
		Button okButton = (Button) 
			boardSettingsDialog.findViewById(R.id.new_board_ok_button);
		Button cancelButton = (Button) 
			boardSettingsDialog.findViewById(R.id.new_board_cancel_button);

		final Spinner typeSpinner = (Spinner)
			boardSettingsDialog.findViewById(R.id.board_type_spinner);
		
		final Spinner sizeSpinner = (Spinner)
			boardSettingsDialog.findViewById(R.id.board_size_spinner);		
		
		final CheckBox targetPositionCheckBox = (CheckBox)
			boardSettingsDialog.findViewById(R.id.target_position_check);
		
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int size = 5 + sizeSpinner.getSelectedItemPosition();
				int typeCode = typeSpinner.getSelectedItemPosition();
				boolean hasTargetPosition = targetPositionCheckBox.isChecked();
				int [] board = null;
				int targetPosition = -1;
				
				switch (SoloGameType.getGameType(typeCode)) {
				case SQUARE:
				case SQUARE_DIAGONAL:
					board = puzzleRepository.getDefaultSquareBoard(size);
					if (hasTargetPosition) {
						targetPosition = 
							puzzleRepository.getDefaultSquareTargetPosition(size);
					}
					break;
				case TRIANGULAR:
					board = puzzleRepository.getDefaultTriangularBoard(size);
					if (hasTargetPosition) {
						targetPosition = 
							puzzleRepository.getDefaultTriangularTargetPosition(size);
					}					
					break;
				}
				SoloCustomBoardActivity.showDirections = true;				
				Intent addIntent  = new Intent("gr.sullenart.games.puzzles.SOLO_ADD_BOARD");
				addIntent.putExtra("boardName", "");
				addIntent.putExtra("board", board);
				addIntent.putExtra("targetPosition", targetPosition);
				addIntent.putExtra("boardType", typeCode);
				addIntent.putExtra("boardSize", size);
				startActivity(addIntent);
				
				boardSettingsDialog.dismiss();
			}
		});
        
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				boardSettingsDialog.dismiss();
			}
		});        
		return boardSettingsDialog;
	}    

}
