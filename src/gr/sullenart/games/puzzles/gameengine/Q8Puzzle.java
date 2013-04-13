package gr.sullenart.games.puzzles.gameengine;

import gr.sullenart.games.graphics.ImageResizer;
import gr.sullenart.games.puzzles.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/**
 * Q8Puzzle Class. The player must place 8 queen on the chess board,
 * without two or more queens to share the same row, column or diagonal.
 */
public class Q8Puzzle extends Puzzle
{
    /** Square's width*/
    private int squareWidth = 30;

    /** Board size. Default value is 8. Must match value in XML file!  */
    private int boardSize = 8;

	/**
	 * The image for queen
	 */
	Bitmap queenImage;
	Bitmap whiteSquare;
	Bitmap blackSquare;
	Bitmap bgTile;

	public Q8Puzzle(Context context) {
		super(context);
		family = "Q8Puzzle";
		name = "" + boardSize + " " +
			   context.getResources().getString(R.string.queens);
	}

	/**
	 * Configures the options of the puzzle (void).
	 *
	 * @param options Options string.
	 */
	public boolean configure(SharedPreferences preferences) {
		String boardSizeStr = preferences.getString("Q8_Board_Size", null);
		if (boardSizeStr == null) {
			return false;
		}
		try {
			int newBoardSize = Integer.parseInt(boardSizeStr);
			if (newBoardSize != boardSize) {
				boardSize = newBoardSize;
				onSizeChanged(screenWidth, screenHeight);
				init();
				return true;
			}
		}
		catch(NumberFormatException e) {

		}
		return false;
	}

	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);

		if (w == 0 || h == 0) {
			return;
		}

		int sizeX = (w - 10) / boardSize;
		int sizeY = (h - 10 - topBarHeight - bottomBarHeight) / boardSize;
		
		squareWidth = sizeX < sizeY ? sizeX : sizeY;
		
		offsetX = (screenWidth - squareWidth*boardSize)/2;
		offsetY = (screenHeight - squareWidth*boardSize)/2;
		
		if (offsetY < topBarHeight + 5) {
			offsetY = topBarHeight + 5;
		}		

		ImageResizer imageResizer = new ImageResizer();
		queenImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.queen);
		imageResizer.init(queenImage.getWidth(), queenImage.getHeight(), 
						  squareWidth - 2, squareWidth - 2);
		queenImage = imageResizer.resize(queenImage);

		blackSquare = BitmapFactory.decodeResource(context.getResources(), R.drawable.black_square);
		imageResizer.init(blackSquare.getWidth(), blackSquare.getHeight(), 
				  		  squareWidth, squareWidth);		
		blackSquare = imageResizer.resize(blackSquare);

		whiteSquare = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_square);
		whiteSquare = imageResizer.resize(whiteSquare);

		bgTile = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_tile);

	}

    /**
     * Initialize puzzle's context - default size.
     */
    @Override
	public void init() {
        movesTableSize = boardSize;
        super.init();
        name = "" + boardSize + " " + context.getResources().getString(R.string.queens);
        enableUndo = false;
    }

	/**
	 * Processes input from user).
	 *
	 * @param x Cursor's x position.
	 * @param y Cursor's y position.
	 * @return Return code for mouse click. One from RIDDLE_SOLVED,
	 *         MOVE_OUT_OF_BOUNDS, MOVE_NOT_ALLOWED, MOVE_SUCCESSFUL.
	 */
    public MoveResult onTouchEvent(float x, float y) {
        // Figure out the row/column
        int c = (int) (x - offsetX) / squareWidth;
        int r = (int) (y - offsetY) / squareWidth;
		r = boardSize - 1 - r;

        MoveResult result = makeMove(r, c);
        if (result == MoveResult.MOVE_SUCCESSFUL) {
			if (!isStarted) {
				isStarted = true;
			}
        }

        return result;
    }

    /**
     * Makes the move at the selected square.
     */
    public MoveResult makeMove(int r, int c) {
		if (c < boardSize && r < boardSize) {
			if (movesTable[c] == -1) {
				movesTable[c] = r;
				if (movesMade()!=0) {
					if (movesMade()==boardSize) {
						return MoveResult.RIDDLE_SOLVED;
					}
					return MoveResult.MOVE_SUCCESSFUL;
				}
				else {
					movesTable[c] = -1;
					return MoveResult.MOVE_NOT_ALLOWED;
				}
			}
			else if (movesTable[c] == r) {
				movesTable[c] = -1;
				return MoveResult.MOVE_SUCCESSFUL;
			}
			else {
				return MoveResult.MOVE_NOT_ALLOWED;
			}
		}
		return MoveResult.MOVE_OUT_OF_BOUNDS;
    }

    /**
     * Undoes last move.
     */
    public void undoLastMove() {

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

	/**
	 * Paints the puzzle's board.
	 *
	 * @param g Graphics object.
	 */
    public void draw(Canvas canvas) {
    	drawBackgroundRepeat(canvas, bgTile);

    	float left = 0, top = 0;

    	Paint paint = new Paint();
    	paint.setStrokeWidth(2);
    	paint.setStyle(Style.STROKE);
    	paint.setAntiAlias(true);
    	paint.setColor(Color.argb(0xFF, 0xC9, 0x7F, 0x2A));
    	canvas.drawRect(offsetX-2, offsetY-2,
    			offsetX + boardSize*squareWidth + 2,
    			offsetY + boardSize*squareWidth + 2, paint);

		for(int row=0; row<boardSize; row++) {
			for(int column=0;column<boardSize;column++) {
				Bitmap squareImage;
				if((row+column)%2==0) {
					// White square
			    	//paint.setColor(0xFFFFFFFF);
			    	squareImage = whiteSquare;
				}
				else {
					// Black square
					//paint.setColor(0xFF000000);
					squareImage = blackSquare;
				}
				left = offsetX + column*squareWidth;
				top = offsetY + row*squareWidth;
		    	//canvas.drawRect(left, top,
		    	//		   	    left + squareWidth, top + squareWidth, paint);
				canvas.drawBitmap(squareImage, left, top, null);
				if((boardSize - 1 - movesTable[column])==row) {
					canvas.drawBitmap(queenImage, left, top, null);
				}
			}
		}
    }

	/**
	 * Returns moves made so far.
	 *
	 * @return Returns moves made so far.
	 */
    protected int movesMade() {
    	int i, j, queens = 0;

    	for(i=0;i<boardSize;i++) {
    		if (movesTable[i]!=-1) {
    			queens++;
    			for(j=i+1;j<boardSize;j++) {
	    			if (movesTable[j]==-1)
	    				continue;
	    			if (movesTable[i] == movesTable[j])
	    				return(0);
	    			if (i + movesTable[i] == j + movesTable[j])
	    				return(0);
	    			if (i - movesTable[i] == j - movesTable[j])
	    				return(0);
	    		}
    		}
    	}
    	return(queens);
    }

    // Solving functionality

    /*
     * Number of queens placed. Queens are placed from column
     * 0 to column boardSize.
     */
    int queensPlaced;

	/**
	 * Initialises solver.
	 */
	protected void initSolver() {
		int i;

		queensPlaced = 0;
		for (i = 0; i < boardSize; i++) {
			if (movesTable[i]!= -1)
				queensPlaced = i + 1;
		}

		// Remove queens placed in subsequent columns
		for (; i < boardSize; i++) {
			movesTable[i]= -1;
		}
	}

	/**
	 * Returns puzzle's status.
	 *
	 * @return true if the puzzle is solved.
	 */
    public boolean isSolved() {
        return(movesMade() == movesTableSize);
    }

	/**
	 * Finds the next possible move.
	 *
	 * @return true if a move has been found.
	 */
    protected boolean findNextMove() {
    	if (queensPlaced == 0) {
    		movesTable[0] = 0;
    		queensPlaced++;
    		return(true);
    	}
    	else {
    		int r, c = queensPlaced;
    		for(r = 0; r < boardSize; r++) {
    			MoveResult result = makeMove(r,c);

    			if (result == MoveResult.MOVE_SUCCESSFUL ||
    				result == MoveResult.RIDDLE_SOLVED) {
    				queensPlaced++;
    				return(true);
    			}
    		}
    	}
        return(false);
    }

	/**
	 * Replays one move.
	 *
	 * @return false if there are no more moves to replay.
	 */
    protected boolean playNextMove()
    {
        if (solMovesTableIndex >= solMovesTableSize)
            return(false);

        makeMove(solMovesTable[solMovesTableIndex], solMovesTableIndex);
        solMovesTableIndex++;

        if (solMovesTableIndex == solMovesTableSize)
            return(false);

        return(true);
    }

	/**
	 * Goes back (undo last move) when a deadlock is encountered.
	 *
	 * @return If it is impossible to go back false is returned. This
	 *         means that the puzzle cannot be solved.
	 */
    protected boolean goBack()
    {
        while(queensPlaced > 0) {
        	int c = queensPlaced - 1;
        	int r = movesTable[c];
        	makeMove(r, c);	// Undo last move
        	queensPlaced--;

        	for(r=r+1; r<boardSize; r++) {
        		if (makeMove(r,c) == MoveResult.MOVE_SUCCESSFUL) {
        			queensPlaced++;
        			return(true);
        		}
        	}
        }

    	return(false);
    }

	@Override
	public boolean areMovesLeft() {
		if (isSolved()) {
			return false;
		}
		
		int currentMoves = movesMade();
		
		for(int r=0;r<boardSize;r++) {
			for(int c=0;c<boardSize;c++) {
				if (movesTable[c] >= 0) {
					continue;
				}
				movesTable[c] = r;
				if (movesMade() > currentMoves) {
					movesTable[c] = -1;
					return false;
				}
				movesTable[c] = -1;
			}
		}
		return true;
	}
}

