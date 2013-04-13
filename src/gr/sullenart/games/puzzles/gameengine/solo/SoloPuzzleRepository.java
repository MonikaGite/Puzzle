package gr.sullenart.games.puzzles.gameengine.solo;

import gr.sullenart.games.puzzles.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

public class SoloPuzzleRepository {
	
	public class PuzzleKeyName{
		private String key;
		private String name;
		
		public PuzzleKeyName(String key, String name) {
			this.key = key;
			this.name = name;
		}
		
		public String getKey() {
			return key;
		}
		public String getName() {
			return name;
		}
	}
	
	private int defaultBoard7 [] =
	   {-1, -1,  1,  1,  1, -1, -1,
        -1, -1,  1,  1,  1, -1, -1,
         1,  1,  1,  1,  1,  1,  1,
         1,  1,  1,  0,  1,  1,  1,
         1,  1,  1,  1,  1,  1,  1,
        -1, -1,  1,  1,  1, -1, -1,
        -1, -1,  1,  1,  1, -1, -1};	
	
	private int defaultBoard9 [] =
	   {-1, -1, -1,  1,  1,  1, -1, -1, -1,
        -1, -1, -1,  1,  1,  1, -1, -1, -1,
        -1, -1, -1,  1,  1,  1, -1, -1, -1,
         1,  1,  1,  1,  1,  1,  1,  1,  1,
         1,  1,  1,  1,  0,  1,  1,  1,  1,
         1,  1,  1,  1,  1,  1,  1,  1,  1,
        -1, -1, -1,  1,  1,  1, -1, -1, -1,
        -1, -1, -1,  1,  1,  1, -1, -1, -1,
        -1, -1, -1,  1,  1,  1, -1, -1, -1};	
	
	int boardInitTableClassic [] = 
		   {-1, -1,  1,  1,  1, -1, -1,
            -1, -1,  1,  1,  1, -1, -1,
             1,  1,  1,  1,  1,  1,  1,
             1,  1,  1,  0,  1,  1,  1,
             1,  1,  1,  1,  1,  1,  1,
            -1, -1,  1,  1,  1, -1, -1,
            -1, -1,  1,  1,  1, -1, -1};

	int boardInitTableCross [] = 
		 {-1, -1,  0,  0,  0, -1, -1,
          -1, -1,  0,  1,  0, -1, -1,
           0,  0,  0,  1,  0,  0,  0,
           0,  1,  1,  1,  1,  1,  0,
           0,  0,  0,  1,  0,  0,  0,
          -1, -1,  0,  1,  0, -1, -1,
          -1, -1,  0,  0,  0, -1, -1};
	
	int boardInitTableFireplace [] = 
	   {-1, -1,  1,  1,  1, -1, -1,
        -1, -1,  1,  1,  1, -1, -1,
         0,  0,  1,  1,  1,  0,  0,
         0,  0,  1,  0,  1,  0,  0,
         0,  0,  0,  0,  0,  0,  0,
        -1, -1,  0,  0,  0, -1, -1,
        -1, -1,  0,  0,  0, -1, -1};
	
	int boardInitTableDiamond [] = 
	   {-1, -1,  0,  0,  0, -1, -1,
        -1, -1,  0,  1,  0, -1, -1,
         0,  0,  1,  1,  1,  0,  0,
         0,  1,  1,  0,  1,  1,  0,
         0,  0,  1,  1,  1,  0,  0,
        -1, -1,  0,  1,  0, -1, -1,
        -1, -1,  0,  0,  0, -1, -1};
	
	int boardInitTableBigDiamond [] = 
	   {-1, -1,  0,  1,  0, -1, -1,
        -1, -1,  1,  1,  1, -1, -1,
         0,  1,  1,  1,  1,  1,  0,
         1,  1,  1,  0,  1,  1,  1,
         0,  1,  1,  1,  1,  1,  0,
        -1, -1,  1,  1,  1, -1, -1,
        -1, -1,  0,  1,  0, -1, -1};
	
	int boardInitTableMoved [] = 
	   {-1, -1,  1,  1,  1, -1, -1,
		-1, -1,  1,  1,  1, -1, -1,
		 1,  1,  1,  1,  1,  1,  1,
		 1,  1,  0,  1,  1,  1,  1,
		 1,  1,  1,  1,  1,  1,  1,
		-1, -1,  1,  1,  1, -1, -1,
		-1, -1,  1,  1,  1, -1, -1};

    int boardTriangular4 [] =
    { 1,-1,-1,-1,
      0, 1,-1,-1,
      1, 1, 1,-1,
      1, 1, 1, 1,
    };		
	
    int boardTriangular5 [] =
    { 0,-1,-1,-1,-1,
      1, 1,-1,-1,-1,
      1, 1, 1,-1,-1,
      1, 1, 1, 1,-1,
      1, 1, 1, 1, 1,
    };	
	
    int boardTriangular6 [] =
    { 0,-1,-1,-1,-1,-1,
      1, 1,-1,-1,-1,-1,
      1, 1, 1,-1,-1,-1,
      1, 1, 1, 1,-1,-1,
      1, 1, 1, 1, 1,-1,
      1, 1, 1, 1, 1, 1
    };
    
	private Map<String, SoloGame> boardsMap;

	private List<PuzzleKeyName> puzzles;
	
	SoloPuzzlesDbAdapter dbAdapter;
	
	private CharSequence [] predefinedPuzzleValues;

	private CharSequence[] predefinedPuzzleNames;

	private List<String> customBoardNames;
	
	public SoloPuzzleRepository(Context context) {
		
		dbAdapter = new SoloPuzzlesDbAdapter(context);
		
		predefinedPuzzleValues = 
			context.getResources().getTextArray(R.array.solo_puzzle_values);
		
		predefinedPuzzleNames = 
			context.getResources().getTextArray(R.array.solo_puzzles);
		
		boardsMap = new HashMap<String, SoloGame>();
		puzzles = new ArrayList<PuzzleKeyName>();
		
		customBoardNames = new ArrayList<String>();
		
		int [][] boardTables = {boardInitTableClassic, boardInitTableCross, boardInitTableDiamond,
								boardInitTableBigDiamond, boardInitTableMoved, boardTriangular4,
								boardTriangular5, boardTriangular6};
		boolean [] isTriangular = {false, false, false, false, false, true, true, true};
		int [] targetPositions= {-1, -1, -1, -1, -1, -1, 0, -1};
		
		for(int i=0;i<predefinedPuzzleValues.length;i++) {
			SoloGame soloGame = new SoloGame(boardTables[i]);
            if (isTriangular[i]) {
                soloGame.setType(SoloGame.SoloGameType.TRIANGULAR);
            }
            if (targetPositions[i] >= 0) {
            	soloGame.setTargetPosition(targetPositions[i]);
            }
            boardsMap.put(predefinedPuzzleValues[i].toString(), soloGame);
			
			puzzles.add(new PuzzleKeyName(predefinedPuzzleValues[i].toString(), 
										  predefinedPuzzleNames[i].toString()));			
		}		
		
        try
		{
			dbAdapter.open();
			Cursor cursor = dbAdapter.getBoards();
			
			while(cursor.moveToNext()) {
				String name = cursor.getString(1);
				boardsMap.put(name, new SoloGame(
						SoloGameSerializer.deserialize(cursor.getString(2),
						    		 				   cursor.getString(3))));
				puzzles.add(new PuzzleKeyName(name, name));
				customBoardNames.add(name);
			}
			
			cursor.close();
			dbAdapter.close();
		}
		catch (SQLException ex)
		{
			Log.w("SoloDb", ex.getMessage());
		}
	}
	
	public int [] getDefaultSquareBoard(int size) {
		int [] board = new int[size*size];
		if (size == 7) {
			System.arraycopy(defaultBoard7, 0, board, 0, board.length);
		}
		else if (size == 9) {
			System.arraycopy(defaultBoard9, 0, board, 0, board.length);
		}
		else {
			for (int i=0;i<board.length;i++) {
				board[i] = 1;
			}
		}
		return board;
	}
	
	public int getDefaultSquareTargetPosition(int size) {
		return size*size/2;
	}
	
	public int [] getDefaultTriangularBoard(int size) {
		int [] board = new int[size*size];
		int i = 0;
		for(int r=0;r<size;r++) {
			for(int c=0;c<size;c++) {
				board[i] = (c <= r) ? 1 : -1;
				i++;
			}
		}
		board[0] = 0;
		return board;
	}

	public int getDefaultTriangularTargetPosition(int size) {
		return 0;
	}	
	
	public SoloGame getGame(String key) {
		if (boardsMap.containsKey(key)){
			return boardsMap.get(key);
		}
		return new SoloGame(boardInitTableClassic);
	}
	
	public String getSerializedBoard(String key) {
		if (boardsMap.containsKey(key)){
			return SoloGameSerializer.serializeBoard(boardsMap.get(key).getBoard());
		}
		return null;
	}

	public boolean nameExists(String name) {
		for(CharSequence predefinedName: predefinedPuzzleNames) {
			if (predefinedName.equals(name)) {
				return true;
			}
		}
		
		return boardsMap.containsKey(name);
	}
	
	public List<PuzzleKeyName> getBoards() {
		return puzzles;
	}
	
	public List<String> getCustomBoardNames() {
		return customBoardNames;
	}
	
	public int getCustomBoardsCount() {
		return customBoardNames.size();
	}	
	
	public boolean addGame(String name, SoloGame soloGame) {
		for(int i=0;i<predefinedPuzzleValues.length;i++){
			String n = predefinedPuzzleValues[i].toString();
			if (n.equals(name)) {
				return false;
			}
		}
		try {
			dbAdapter.open();
			if (dbAdapter.isNameInUse(name)) {
				dbAdapter.close();
				return false;
			}			
			long result = dbAdapter.addBoard(name, 
					SoloGameSerializer.serializeBoard(soloGame.getBoard()),
					SoloGameSerializer.serializeMetadata(soloGame));
			dbAdapter.close();
			boardsMap.put(name, new SoloGame(soloGame));
			puzzles.add(new PuzzleKeyName(name, name));
			customBoardNames.add(name);
			return result > 0;
		}
		catch(SQLException ex) {
			Log.w("SoloDb", ex.getMessage());
		}
		return false;
	}
	
	public boolean deleteGame(String name) {
		try {
			dbAdapter.open();	
			long result = dbAdapter.deleteBoard(name);
			dbAdapter.close();
			boardsMap.remove(name);
			for(int i=0;i<puzzles.size();i++){
				if (puzzles.get(i).getKey().equals(name)) {
					puzzles.remove(i);
					break;
				}
			}
			customBoardNames.remove(name);
			return result > 0;
		}
		catch(SQLException ex) {
			Log.w("SoloDb", ex.getMessage());
		}
		return false;		
	}
	
	public boolean renameGame(String name, String newName) {
		try {
			dbAdapter.open();	
			long result = dbAdapter.renameBoard(name, newName);
			dbAdapter.close();
			SoloGame board = boardsMap.get(name);
			boardsMap.remove(name);
			boardsMap.put(newName, board);
			int index = -1;
			for(int i=0;i<puzzles.size();i++){
				if (puzzles.get(i).getKey().equals(name)) {
					index = i;
					break;
				}
			}
			if (index > 0) {
				puzzles.set(index, new PuzzleKeyName(newName, newName));
			}
			index = customBoardNames.indexOf(name);
			if (index > 0) {
				customBoardNames.set(index, newName);
			}
			return result > 0;
		}
		catch(SQLException ex) {
			Log.w("SoloDb", ex.getMessage());
		}
		return false;		
	}	
	
	public boolean updateGame(String name, SoloGame game) {
		try {
			dbAdapter.open();
			long result = dbAdapter.updateBoard(name, 
					SoloGameSerializer.serializeBoard(game.getBoard()),
					SoloGameSerializer.serializeMetadata(game));
			dbAdapter.close();
			SoloGame previous = boardsMap.put(name, game);
			if (previous == null) {
				Log.w("SoloDb", "Update board: old board not found");
			}
			return result > 0;
		}
		catch(SQLException ex) {
			Log.w("SoloDb", ex.getMessage());
		}
		return false;		
	}	
	
	/*private String serializeBoard(int [] board) {
		StringBuilder builder = new StringBuilder();
		for(int i: board) {
			switch(i){
			case -1:
				builder.append('a');
				break;
			case 0:
				builder.append('b');
				break;
			case 1:
				builder.append('c');
				break;
			}
		}
		return builder.toString();
	}
	
	private int [] deserializeBoard(String boardStr){
		int [] board = new int[boardStr.length()];
		for(int i=0;i<boardStr.length();i++){
			switch(boardStr.codePointAt(i)){
			case 'a':
				board[i] = -1;
				break;
			case 'b':
				board[i] = 0;
				break;
			case 'c':
				board[i] = 1;
				break;
			}
		}
		return board;
	}*/

	/*public boolean isBoardValid(int[] boardTable) {
		int boardSize = Solo.GET_BOARD_SIZE(boardTable);
		int columnCount = boardSize;
		int rowCount = boardSize;
		
		int holesCount = 0;
		int pegsCount = 0;
		for (int pos = 0;pos<boardTable.length;pos++) {
			if (boardTable[pos]==-1)
				continue;
			else if (boardTable[pos]==0) {
				holesCount++;
			}
			else if (boardTable[pos]==1) {
				pegsCount++;
			}			
			
			int r = pos / columnCount;
			int c = pos % columnCount;
			if (r > 0) {
				if (boardTable[pos-columnCount]>=0)
					continue;
			}
			if (r < columnCount - 1) {
				if (boardTable[pos+columnCount]>=0)
					continue;				
			}
			if (c > 0) {
				if (boardTable[pos-1]>=0)
					continue;				
			}
			if (c < rowCount - 1) {
				if (boardTable[pos+1]>=0)
					continue;				
			}
			return false;
		}
		return pegsCount > 0 && holesCount > 0;
	}*/
	
	/*private int metadataVersion = 0;
	
	private String serializeMetadata(SoloGame soloGame) {
		return String.format("%02d%03d%d", metadataVersion,
							 soloGame.getTargetPosition() + 1,
							 soloGame.getType().getCode());
	}
	
	private SoloGame deserializeGame(String boardStr, String metadataStr) {
		SoloGame soloGame = new SoloGame(deserializeBoard(boardStr));
		
		int finalPosition = Integer.parseInt(metadataStr.substring(2, 5)) - 1;
		int typeCode = Integer.parseInt(metadataStr.substring(5, 6));
		
		soloGame.setTargetPosition(finalPosition);
		soloGame.setType(SoloGameType.getGameType(typeCode));
		return soloGame;
	}*/
}
