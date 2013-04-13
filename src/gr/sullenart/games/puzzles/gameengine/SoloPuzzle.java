package gr.sullenart.games.puzzles.gameengine;

import gr.sullenart.games.graphics.ImageResizer;
import gr.sullenart.games.puzzles.R;
import gr.sullenart.games.puzzles.gameengine.solo.Solo;
import gr.sullenart.games.puzzles.gameengine.solo.SoloBoard;
import gr.sullenart.games.puzzles.gameengine.solo.SoloBoardPosition;
import gr.sullenart.games.puzzles.gameengine.solo.SoloCustomBoardActivity;
import gr.sullenart.games.puzzles.gameengine.solo.SoloGame;
import gr.sullenart.games.puzzles.gameengine.solo.SoloPuzzleRepository;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Class for Solo Puzzle.
 * Objective of the game is to remove all the pegs off the board,
 * except of the last one. A peg can move to a free position, two
 * positions at the left, right, up or down, jumping over another peg.
 * The peg that was jumped over is removed.
 *
 */
public class SoloPuzzle extends Puzzle {
	/** Number of moves so far (0..TILES).*/
	int moveCount;

	/** Position of selected square 0..BOARD_SIZE. */
	int selPos;

	/** True if user picked a tile to move. */
	boolean fired;
   
	/** Board's rows.*/
    int boardRows;

    /** Board's columns.*/
    int boardColumns;

    /** Number of tiles on the board.*/
    int tiles;

    /** Table with board layout.*/
    int boardTable[];

    /** Initial board table. Used for resetting the board. */
	private int[] boardInitTable;

    /**Size of boardTable.*/
    int boardTableSize;

	/** Code for START state.*/
	static final int START = 0;

	/** Code for PLAYING state.*/
	static final int PLAYING = 1;

	/** Code for WON state.*/
	static final int WON = 2;

	/** Variable for storing game's state*/
	int gameState;

	/** Tile's size.*/
	int tileSize = 20;

	/** Image for free position.*/
	Bitmap freePosImage;

	/** Image for tile.*/
	Bitmap tileImage;

	/** Image for unselected tile.*/
	Bitmap tileSelectedImage;

	/** Image for empty space.*/
	Bitmap emptyImage;

	Bitmap freePosAllowedMoveImage;

	ImageResizer imageResizer;

	private int lastMoveUndone = -1;
	
	private boolean isInEditMode = false;
	
	private boolean isInSetTargetMode = false;
	
	/** Display theme. Default value is 'wood'. Must match value in XML file! */
	private String theme = "wood";

	/** Puzzle type. Default value is 'classic'. Must match value in XML file!  */
	private String type = "classic";

	private SoloBoard soloBoard;

	private SoloPuzzleRepository puzzleRepository;
	
	private List<SoloBoardPosition> boardPositions;	
	
    private int soloMovesCount;

	private SoloGame soloGame = null;
    
	public int getSoloMovesCount() {
        soloMovesCount = soloBoard.getMoveCount(movesTable);
		return soloMovesCount;
	}    
    
	public void setSoloPuzzleRepository(SoloPuzzleRepository puzzleRepository) {
		this.puzzleRepository = puzzleRepository;
	}	
	
	public int[] getBoardTable() {
		return boardTable;
	}
	
	public void setEditMode(boolean isInEditMode) {
		this.isInEditMode = isInEditMode;
	}
	
	public void setInSetTargetMode(boolean isInSetTargetMode) {
		this.isInSetTargetMode = isInSetTargetMode;
	}	
	
	public boolean isInSetTargetMode() {
		return isInSetTargetMode;
	}	

	public int getTargetPosition() {
		return soloBoard.getTargetPosition();
	}	
	
	public SoloPuzzle(Context context) {
		super(context);
		family = "Solo";
		enableAdd = true;
	}

	public void setGame(SoloGame soloGame) {
		this.soloGame  = soloGame;
	}	

	public boolean configure(SharedPreferences preferences) {
		boolean modified = false;
		String newTheme = preferences.getString("Solo_Theme", theme);
		String newType = preferences.getString("Solo_Puzzle_Type", type);
		if (SoloCustomBoardActivity.boardsListUpdated) {
			SoloCustomBoardActivity.boardsListUpdated = false;
			soloGame = null;
		}
		
		if (!newTheme.equals(theme) || !newType.equals(type) || soloGame == null) {
			theme = newTheme;
			if (!newType.equals(type) || soloGame == null) {
				type = newType;
				soloGame = puzzleRepository.getGame(newType);
				init();
				modified = true;
			}
			onSizeChanged(screenWidth, screenHeight);
		}

		return modified;
	}

	
	private void updateName() {
		boolean found = false;
		CharSequence[] names = context.getResources().getTextArray(R.array.solo_puzzles);
		CharSequence[] keys = context.getResources().getTextArray(R.array.solo_puzzle_values);

		for(int i=0; i<keys.length; i++) {
			String key = keys[i].toString();
			if (key.equals(type)) {
				name = names[i].toString();
				found = true;
				break;
			}
		}
		if (!found) {
			name = type;
		}
	}

	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);

		int boardSize = (boardRows > boardColumns) ? boardRows : boardColumns;
		if (w==0 || h == 0 || boardSize == 0) {
			return;
		}
		int sizeX = (w - 10) / boardSize;
		int sizeY = (h - 10 - topBarHeight - bottomBarHeight) / boardSize;
		
		tileSize = sizeX < sizeY ? sizeX : sizeY;
		
		offsetX = (screenWidth - tileSize*boardColumns)/2;
		offsetY = (screenHeight - tileSize*boardRows)/2;
		
		if (offsetY < topBarHeight + 5) {
			offsetY = topBarHeight + 5;
		}

		imageResizer = new ImageResizer();

		if (theme.equals("marble")) {
			emptyImage = BitmapFactory.decodeResource(context.getResources(),
							R.drawable.marble_tile);
			tileImage = BitmapFactory.decodeResource(context.getResources(),
							R.drawable.glass);
			tileSelectedImage = BitmapFactory.decodeResource(context.getResources(),
							R.drawable.glass_selected);
		}
		else if (theme.equals("wood2")) {
			emptyImage = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.wood);
			tileImage = BitmapFactory.decodeResource(context.getResources(),
							R.drawable.marble_sphere);
			tileSelectedImage = BitmapFactory.decodeResource(context.getResources(),
							R.drawable.marble_sphere_selected);
		}
		else {
			emptyImage = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.wood);
			tileImage = BitmapFactory.decodeResource(context.getResources(),
							R.drawable.glass);
			tileSelectedImage = BitmapFactory.decodeResource(context.getResources(),
							R.drawable.glass_selected);			
		}

		freePosImage = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.hole);
		freePosAllowedMoveImage  = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.hole_selected);

		//imageResizer.init(emptyImage.getWidth(), emptyImage.getHeight(), tileSize, tileSize);
		//emptyImage = imageResizer.resize(emptyImage);
		imageResizer.init(freePosImage.getWidth(), freePosImage.getHeight(), tileSize, tileSize);
		freePosImage = imageResizer.resize(freePosImage);
		tileImage = imageResizer.resize(tileImage);
		tileSelectedImage = imageResizer.resize(tileSelectedImage);
		freePosAllowedMoveImage = imageResizer.resize(freePosAllowedMoveImage);
	}

    /**
     * Initialize puzzle's context. 
     */
	@Override
    public void init() {
    	moveCount = 0;

    	if (soloGame != null) {
			int [] board = soloGame.getBoard();
			boardInitTable = new int [board.length];
			boardTable = new int [board.length];
			System.arraycopy(board, 0,
					boardTable, 0, boardTable.length);
			System.arraycopy(board, 0,
					boardInitTable, 0, boardTable.length);
			int size = Solo.GET_BOARD_SIZE(boardTable);
			soloBoard = new SoloBoard(boardTable, size, size,
									  soloGame.getType(),
									  soloGame.getTargetPosition());
			boardColumns = size;
			boardRows = size;
			
	    	boardTableSize = boardTable.length;
	        tiles = 0;
	    	for(int i =0; i<boardTableSize; i++) {
	    		if (boardTable[i] == 1)
	    		    tiles++;
	    	}
	    	movesTableSize = tiles - 1;			
    	}

    	selPos = -1;
    	fired = false;

		// set up initial state
        soloMovesCount = 0;
		gameState = START;
		
		updateName();
        super.init();
    }

    private void drawBackgroundRepeat(Canvas canvas, Bitmap bgTile) {
    	float left = 0, top = 0;
    	float bgTileWidth = bgTile.getWidth();
    	float bgTileHeight = bgTile.getWidth();

    	while (left < screenWidth) {
    		while (top < screenHeight) {
    			canvas.drawBitmap(bgTile, left, top, null);
    			top += bgTileHeight;
    		}
    		left += bgTileWidth;
    		top = 0;
    	}
    }
    
    public void draw(Canvas canvas) {
		int index;
		Bitmap im;

		Paint targetPaint = new Paint();
		targetPaint.setColor(0xE0C0C0C0);
		targetPaint.setStyle(Paint.Style.FILL);
		
		drawBackgroundRepeat(canvas, emptyImage);

		for(int r = 0; r < boardRows; r++) {
			for(int c = 0; c < boardColumns; c++) {
				index = r * boardColumns + c;
				
				if (boardTable[index] == 0) {
                    if (fired && soloBoard.isAllowedJumpPosition(selPos, r, c)) {
                        im = freePosAllowedMoveImage;
					}
					else {
						im = freePosImage;
					}
				}
				else if (boardTable[index] == 1) {
					if (fired && index == selPos)
						im = tileSelectedImage;
					else
						im = tileImage;
				}
				else {
					continue;
				}
                int x = offsetX + c*tileSize;
                int y = offsetY + r*tileSize;
                
                if (soloBoard.isTriangular()) {
                    x+= ((boardColumns - r - 1)*tileSize)/2;
                }
                
				if (index == soloBoard.getTargetPosition()) {
					canvas.drawRect(x, y, x+tileSize, y+tileSize, 
								    targetPaint);
				}                
                
				canvas.drawBitmap(im, x, y, null);
			}
    	}
    }

	/**
	 * Processes input from user (mouse clicks).
	 *
	 * @param x Cursor's x position.
	 * @param y Cursor's y position.
	 * @return Return code for mouse click. One from RIDDLE_SOLVED,
	 *         MOVE_OUT_OF_BOUNDS, MOVE_NOT_ALLOWED, MOVE_SUCCESSFUL.
	 */
	public MoveResult onTouchEvent(float x, float y) {
        // Figure out the row/column
        int r = ((int) y - offsetY) / tileSize;                
        if (soloBoard.isTriangular()) {
        	x-= ((boardColumns - r - 1)*tileSize)/2;
        }
        int c = ((int) x - offsetX) / tileSize;
        int newPos = r*boardColumns + c;
        
        if (isInEditMode) {
        	if (newPos >= 0 && newPos < boardTable.length) {
	        	if (isInSetTargetMode) {
	        		if (boardTable[newPos] >= 0) {
	        			soloBoard.setTargetPosition(newPos);
	        		}
	        	}
	        	else {
		        	if (boardTable[newPos] == 1)
		        		boardTable[newPos] = -1;
		        	else if (boardTable[newPos] == -1)
		        		boardTable[newPos] = 0;
		        	else if (boardTable[newPos] == 0)
		        		boardTable[newPos] = 1; 
	        	}
        	}
        	return MoveResult.MOVE_EDIT;
        }
        
        
		if (gameState == WON) {
			return (MoveResult.RIDDLE_SOLVED);
		}

		if (newPos < 0 || newPos >= boardTableSize)
			return (MoveResult.MOVE_OUT_OF_BOUNDS);

		if (!fired) {
			if (boardTable[newPos]!=1)
				return(MoveResult.MOVE_NOT_ALLOWED);

			selPos = newPos;			
			if (soloBoard.isMoveAvailable(selPos)){
				fired = true;
				if (!isStarted) {
					isStarted = true;
				}
				return(MoveResult.MOVE_SUCCESSFUL);
			}
		}
		else {
			if (newPos == selPos) {
				fired = false;
				if (!isStarted) {
					isStarted = true;
				}
				return(MoveResult.MOVE_SUCCESSFUL);
			}
			else {
				if (boardTable[newPos]!=0)
					return(MoveResult.MOVE_NOT_ALLOWED);
				
				int dir = soloBoard.getDir(selPos, newPos);

				if (dir >= 0 && soloBoard.isMoveLegal(Solo.MAKE_VAL(selPos, dir))) {
					makeMove(dir);
                    soloMovesCount = soloBoard.getMoveCount(movesTable);
					fired = false;
					if (isSolved()) {
						gameState = WON;
						return (MoveResult.RIDDLE_SOLVED);
					}
					if (!isStarted) {
						isStarted = true;
					}
					return(MoveResult.MOVE_SUCCESSFUL);
				}
			}
		}
		return(MoveResult.MOVE_NOT_ALLOWED);
    }


	/**
	 * Makes move. Uses selPos static variable. Updates board's table,
	 * registes move, so that undoing is possible.
	 *
	 * @param dir Direction of move.
	 */
    public void makeMove(int dir) {
    	soloBoard.makeMove(selPos, dir);
		movesTable[moveCount] = Solo.MAKE_VAL(selPos, dir);

		moveCount++;

		if (gameState == START) {
			gameState = PLAYING;
		}
    }

	/**
	 * Returns true if undoing is supported and allowed.
	 *
	 * @return true if undoing is supported and allowed.
	 */    
    @Override
	public boolean isUndoPermitted() {
		return enableUndo && moveCount > 0 && gameState != WON;
	}    
    
    /**
     * Undoes last move.
     */
    public void undoLastMove() {
		if (moveCount == 0 || gameState == WON)
		    return;

		moveCount--;
		int pos = Solo.GET_BOARD_POS(movesTable[moveCount]),
		    dir = Solo.GET_DIR(movesTable[moveCount]);
		lastMoveUndone = movesTable[moveCount];


		movesTable[moveCount] = -1;
		soloBoard.undoMove(pos, dir);
		selPos = pos;

		if (moveCount == 0) {
			gameState = START;
		}

    	fired = false;
    }

	/**
	 * Returns puzzle's status.
	 *
	 * @return true if the puzzle is solved.
	 */
	public boolean isSolved() {
		return(moveCount == movesTableSize);
	}

	/**
	 * Initializes solver.
	 */
	protected void initSolver() {
		boardPositions = new ArrayList<SoloBoardPosition>();
		for(int i=0;i<boardTable.length;i++) {
			if (boardTable[i] >= 0) {
				boardPositions.add(new SoloBoardPosition(i));
			}
		}
	}

	/**
	 * Returns moves made so far.
	 *
	 * @return Returns moves made so far.
	 */
	protected int movesMade() {
		return(moveCount);
	}
	
	private int solvedDepth = 0;
	
	/**
	 * Finds the next possible move.
	 *
	 * @return true if a move has been found.
	 */
	protected boolean findNextMove() {
		int pos = 0;
		int dir = 0;
		
		if (lastMoveUndone >= 0) {
			pos = Solo.GET_BOARD_POS(lastMoveUndone);
			dir = 1 + Solo.GET_DIR(lastMoveUndone);
		}
		
		for(int i=pos; i<boardTableSize; i++) {
			for(int j=dir; j<Solo.DIRECTIONS_COUNT; j++) {
				selPos = i;
				if(soloBoard.isMoveLegal(Solo.MAKE_VAL(selPos, j)) &&
						isTargetPositionHit(selPos, j)) {
					lastMoveUndone = -1;
					makeMove(j);
					
					if (moveCount > solvedDepth) {
						solvedDepth = moveCount;
						//debugPrintBoard();
					}
					
					return(true);
				}
			}
			dir = 0;
		}

		return(false);
	}
	
	private boolean isTargetPositionHit(int selPos, int dir) {
		if (moveCount < movesTableSize - 1)
			return true;
		int targetPosition = soloBoard.getTargetPosition();
		if (targetPosition < 0)
			return true;
		int lastPos = soloBoard.getNewPos(selPos, dir);
		return targetPosition == lastPos;
	}	

	
	private boolean replayShowSelectedPeg = false;
	
	/**
	 * Replays one move.
	 *
	 * @return false if there are no more moves to replay.
	 */
    protected boolean playNextMove() {
        if (solMovesTableIndex >= solMovesTableSize)
            return(false);
    	
    	if (!replayShowSelectedPeg) {
    		replayShowSelectedPeg = true;
    		fired = true;
	
	        int pos = Solo.GET_BOARD_POS(solMovesTable[solMovesTableIndex]);
	
	        selPos = pos;
	
	        if (solMovesTableIndex >= solMovesTableSize)
	            return false;
    	}
    	else {
    		replayShowSelectedPeg = false;
    		fired = false;
	        int dir = Solo.GET_DIR(solMovesTable[solMovesTableIndex]);
	        solMovesTableIndex++;
	        makeMove(dir);
            soloMovesCount = soloBoard.getMoveCount(movesTable);
    	}
    	return true;
    }

	/**
	 * Goes back (undo last move) when a deadlock is encountered.
	 *
	 * @return If it is impossible to go back false is returned. This
	 *         means that the puzzle cannot be solved.
	 */
	protected boolean goBack()	{

		if (gameState == START)
			return(false);

		undoLastMove();
		
		return(true);
	}
    
    public boolean areMovesLeft() {
    	if (soloBoard == null) {
    		return false;
    	}    	
        return soloBoard.isNotSolvable();
    }

}


