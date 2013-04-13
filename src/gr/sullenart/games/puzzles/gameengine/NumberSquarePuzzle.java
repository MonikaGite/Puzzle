package gr.sullenart.games.puzzles.gameengine;

import gr.sullenart.games.puzzles.R;

import java.util.Collections;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Rect;

/**
 * Class for Number Square Puzzle.
 * Objective of the game is to move through all the squares of the board.
 * You can move 3 squares afar in the same row or column or like the
 * knight moves in chess.
 */
public class NumberSquarePuzzle extends Puzzle {
    /** Board's size. Default value is 8. Must match value in XML file! */
    int boardSize = 8;

    /** Board's x size.*/
    int boardSizeX = 8;

    /** Board's y size.*/
    int boardSizeY = 8;

	final int MAX_SIZE = 12;

    /** Square's width.*/
    int squareWidth = 25;

    /** Value of last number placed.*/
    static int currentValue;

    /** Position of last number placed.*/
    static int currentPosition;

    /**
     * Holds the vertical distance (row offset) between the current
     * square and the next one, for all possible 12 moves.
     */
    int r_offset [] = { -2, -1,  1,  2,  2,  1, -1, -2,  3, -3,  0,  0};

    /**
     * Holds the horizontal distance (column offset) between the current
     * square and the next one, for all possible 12 moves.
     */
    int c_offset [] = {  1,  2,  2,  1, -1, -2, -2, -1,  0,  0, -3,  3};

    /**
     * Controls whether moves in the same row/column will be allowed.
     * Default value is true. Must match value in XML file!
     */
    boolean knightMovesOnly = true;

    /**
     * Controls whether the tour must be reentrant or not.
     * Default value is true. Must match value in XML file!
     */
    boolean reentrant = true;

    /**
    * Controls whether numbers will be drawn inside the squares..
    * Default value is true. Must match value in XML file!
    */
    boolean showNumbers = true;
    
    /**
     * The size of the line that connects the squares.
     * Default value is 0 (no line). Must match value in XML file!
     */    
     int lineSize = 0;
     
    /**
     * Number of allowed moves: 8 if knightMovesOnly is true, 12
     * otherwise.
     */
    int allowedMovesCount;

	/** Table for storing moves already tried out.*/
	boolean [][] lockedMovesArray;

    /** Row of first square when solving.*/
    int firstMoveRow = -1;

    /** Column of first square when solving.*/
    int firstMoveColumn = -1;

    /** Row of first square when solving - first point tested.*/
    int firstMoveRowStart = -1;

    /** Column of first square when solving - first point tested.*/
    int firstMoveColumnStart = -1;    
    
    /**
     * Table that stores mobility of squares (number of squares to which we
     * can move from each square).
     */
    int mobilityTable [];

    /**
     * In Knight's Tour the last move must be one knight's move
     * away from the first move. This table holds all possible
     * last moves. It is initialised after the first move.
     */
    Vector<Integer> lastMoveVector;

    private Paint textPaint;
    private Paint bigTextPaint;

    public NumberSquarePuzzle(Context context) {
		super(context);
    	family = "NumberSquarePuzzle";
    	updateName();
    }

	/**
	 * Configures the options of the puzzle:
     * <ul>
     *	 <li>Size of the board.</li>
     *	 <li>Reentrant</li>
     *   <li>Knight's moves only.</li>
     * </ul>
	 *
	 * @param options Options string.
	 */
	public boolean configure(SharedPreferences preferences) {
		boolean result = false;

		try {
			String boardSizeStr = preferences.getString("KT_Board_Size", null);
			int newBoardSize = Integer.parseInt(boardSizeStr);
			boolean newReentrant = preferences.getBoolean("Reentrant", true);
			boolean newKnightsMoveOnly = preferences.getBoolean("Knights_Move_Only", true);
            showNumbers = preferences.getBoolean("KT_Show_Numbers", true);
			String lineSizeStr = preferences.getString("KT_Line_Size", "0");
			lineSize = Integer.parseInt(lineSizeStr);

			if (newBoardSize != boardSize ||
					newReentrant != reentrant ||
					newKnightsMoveOnly != knightMovesOnly) {
				boardSize = newBoardSize;
				boardSizeX = boardSizeY = boardSize;
				reentrant = newReentrant;
				knightMovesOnly = newKnightsMoveOnly;
				init();
				onSizeChanged(screenWidth, screenHeight);
				result = true;
			}
		}
		catch (Exception e) {

		}

        return result;
    }

	private void updateName() {
		String difficulty = "";
		Resources resources = context.getResources();
		if (!reentrant && !knightMovesOnly) {
			difficulty = resources.getString(R.string.very_easy);
		}
		else if (reentrant && !knightMovesOnly) {
			difficulty = resources.getString(R.string.easy);
		}
		else if (!reentrant && knightMovesOnly) {
			difficulty = resources.getString(R.string.medium);
		}
		else if (reentrant && knightMovesOnly) {
			difficulty = resources.getString(R.string.hard);
		}

		name = difficulty + " " + boardSize;
	}

    /**
     * Initialize puzzle's context - default size.
     */
    @Override
	public void init() {
        movesTableSize = boardSize*boardSize;
        allowedMovesCount = (knightMovesOnly) ? 8 : 12;
        super.init();

        mobilityTable = new int [movesTableSize];

        currentPosition = -1;
        currentValue = 0;

        updateName();
    }

	public void onSizeChanged(int w, int h) {
		super.onSizeChanged(w, h);

		if (w==0 || h == 0 || boardSize == 0) {
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
		
		textPaint = new Paint();
    	textPaint.setStrokeWidth(1);
    	textPaint.setStyle(Style.FILL_AND_STROKE);
    	textPaint.setAntiAlias(true);
    	textPaint.setColor(Color.BLACK);
    	textPaint.setTextAlign(Align.CENTER);
    	resizeFontToFit(textPaint, squareWidth - 2);

		bigTextPaint = new Paint();
		bigTextPaint.setStrokeWidth(1);
		bigTextPaint.setStyle(Style.FILL_AND_STROKE);
		bigTextPaint.setAntiAlias(true);
		bigTextPaint.setColor(Color.BLACK);
		bigTextPaint.setTextAlign(Align.CENTER);
    	resizeFontToFit(bigTextPaint, "144", squareWidth - 2);
	}

    /**
     * Initialize puzzle's context.
     *
     * @param size Board's size.
     */
    public void init(int size) {
        boardSize = size;
        boardSizeX = size;
        boardSizeY = size;
        init();
    }

    /**
     * Checks if the move at the selected square is allowed.
     *
     * @param r Row of selected square.
     * @param c Column of selected square.
     * @return true is move is allowed.
     */
    private boolean isMoveAllowed(int r, int c) {
        if (c >= boardSizeX || r >= boardSizeY ||
            c < 0 || r < 0)
            return(false);

        if (movesTable[c+r*boardSizeX] != -1)
            return(false);

        if (currentPosition == -1)
            return(true);

        int currentPosition_r = currentPosition / boardSizeX;
        int currentPosition_c = currentPosition % boardSizeX;

        for(int i=0; i<allowedMovesCount; i++){
            if (r == currentPosition_r + r_offset[i] &&
                c ==  currentPosition_c + c_offset[i] &&
                	movesTable[r*boardSizeX + c] == -1) {
                return(true);
            }
        }

        return(false);
    }

    /**
     * Checks if the requirement to end one knight's move away from the.
     * beginning can still be fulfilled. It is used only when knightsMoveOnly
     * is true.
     *
     * @param r Row of selected square.
     * @param c Column of selected square.
     * @return true if there is at least one free square, that is one
     *         knight's move away from the first move.
     */
    private boolean isLastMovePossible(int r, int c) {
        if (reentrant && knightMovesOnly &&
        		currentPosition >= 0 && currentValue < (movesTableSize - 1))
        	{
            if (lastMoveVector.contains(Integer.valueOf(r*boardSize + c))) {
                // A square that is one move away from the first square is
                // selected.
                for (Integer ipos:  lastMoveVector) {
                    int pos = ipos.intValue();
                    if (pos != (r*boardSize + c) && movesTable[pos] == -1) {
                        // There is still at least one more square that is
                        // one move away from the first one.
                        return(true);
                    }
                }
                return(false);
            }
        }
        return(true);
    }

    /**
     * Calculates the mobility of all free squares. The mobility
     * of one square is counted as the number of other free squares, to
     * which we can move from that square.
     */

    private void buildMobibilityTable() {
        int currentPositionCopy = currentPosition,
            currentValueCopy = movesTable[currentPosition];
        int i, j;

        for(i=0; i<movesTableSize; i++) {
            mobilityTable[i] = 0;
        }

        movesTable[currentPosition] = -1;
        for(i=0; i<movesTableSize; i++) {
            if ((movesTable[i] == -1) && (i != currentPositionCopy)) {
                currentPosition = i;
                for (j=0; j<allowedMovesCount; j++) {
                    int r_new = currentPosition/boardSizeX + r_offset[j];
                    int c_new = currentPosition%boardSizeX + c_offset[j];

                    if (isMoveAllowed(r_new, c_new)) {
                        mobilityTable[i]++;
                    }
                }
            }
        }
        currentPosition = currentPositionCopy;
        movesTable[currentPosition] = currentValueCopy;
    }


    /**
     * Checks if there are unreachable squares.
     *
     * @return false if there is one or more free squares that are unreachable.
     */
    private boolean checkMobibility()
    {
        buildMobibilityTable();

        Vector<Integer> deadEndSquares = new Vector<Integer>();
        for(int i=0; i<movesTableSize; i++) {
            if (movesTable[i] == -1) {
                if (mobilityTable[i] == 1) {
                    deadEndSquares.add(Integer.valueOf(i));
                }
                else if (mobilityTable[i] == 0) {
                    return(false);
                }
            }
        }

        if (deadEndSquares.size()>1)
            return(false);
        else if (deadEndSquares.size() == 1) {
            Integer deadEndPos = (Integer) deadEndSquares.elementAt(0);

            if (!lastMoveVector.contains(deadEndPos))
                return(false);
        }

        return(true);
    }

    /**
     * Prints mobility table. Debugging only.
     */
    /*private void printMobibility()
    {
        buildMobibilityTable();

        for(int i=0; i<boardSizeY; i++) {
            for(int j=0; j<boardSizeX; j++) {
                if (movesTable[i*boardSizeX + j] == -1)
                    System.out.print(mobilityTable[i*boardSizeX + j] + " ");
                else
                    System.out.print("x ");
            }
            System.out.println();
        }
        System.out.println();
    }*/

	/**
	 * Processes input from user (mouse clicks). Calls makeMove method.
	 *
	 * @param x Cursor's x position.
	 * @param y Cursor's y position.
	 * @return Return code for mouse click. One from RIDDLE_SOLVED,
	 *         MOVE_OUT_OF_BOUNDS, MOVE_NOT_ALLOWED, MOVE_SUCCESSFUL.
	 */
    public MoveResult onTouchEvent(float x, float y)
    {
        // Figure out the row/column
        int c = ((int) x - offsetX) / squareWidth;
        int r = ((int) y - offsetY) / squareWidth;

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
     *
     * @param r Row of selected square.
     * @param c Column of selected square.
	 * @return Return code for mouse click. One from RIDDLE_SOLVED,
	 *         MOVE_OUT_OF_BOUNDS, MOVE_NOT_ALLOWED, MOVE_SUCCESSFUL.
     */
    public MoveResult makeMove(int r, int c)
    {    	
    	if (c < boardSizeX && r < boardSizeY && c >= 0 && r >= 0) {
            if (isMoveAllowed(r, c)) {
                if (knightMovesOnly) {
                    if (currentValue == 0) {
                        // Build the lastMoveVector
                    	firstMoveRow = r;
                    	firstMoveColumn = c;
                    	
                    	if (!isSolverRunning()) {
                    		firstMoveColumnStart = c;
                    		firstMoveRowStart = r;
                    	}

                        lastMoveVector = new Vector<Integer>();
                        for(int i = 0; i < allowedMovesCount; i++) {
                            if (isMoveAllowed(r + r_offset[i], c + c_offset[i])) {
                                int pos = (r + r_offset[i])*boardSizeX +
                                          c + c_offset[i];
                                lastMoveVector.add(Integer.valueOf(pos));
                            }
                        }
                    }
                    else {
                        if (!isLastMovePossible(r, c))
                            return(MoveResult.MOVE_NOT_ALLOWED);
                    }
                }

                movesTable[c+r*boardSizeX] = currentValue++;
                currentPosition = c+r*boardSizeX;

                if (currentValue == movesTableSize) {
                	return MoveResult.RIDDLE_SOLVED;
                }
                else {
                    return MoveResult.MOVE_SUCCESSFUL;
                }
            }
            else if (movesTable[c+r*boardSizeX] == currentValue-1) {
                // Undo move
                movesTable[c+r*boardSizeX] = -1;
                currentValue--;
                currentPosition = -1;
                if (currentValue > 0)
                    for(int i=0; i<movesTableSize; i++) {
                        if (movesTable[i] == currentValue-1) {
                            currentPosition = i;
                            break;
                        }
                    }
                return(MoveResult.MOVE_SUCCESSFUL);
            }
            else {
                return(MoveResult.MOVE_NOT_ALLOWED);
            }
        }
        return(MoveResult.MOVE_OUT_OF_BOUNDS);
    }

	/**
	 * Returns true if undoing is supported and allowed.
	 *
	 * @return true if undoing is supported and allowed.
	 */    
    @Override
	public boolean isUndoPermitted() {
		return enableUndo && currentValue > 0;
	}      
    
    /**
     * Undoes last move.
     */
    public void undoLastMove() {
        if (currentValue > 0) {
            makeMove(currentPosition / boardSizeX,
                     currentPosition % boardSizeX);
        }
    }

    private float resizeFontToFit(Paint paint, int textHeight) {
        if (textHeight > 0) {
            float size = paint.getTextSize();
            float minTextSize = 0;
            float maxTextSize = 30;
            FontMetricsInt fm = paint.getFontMetricsInt();
            int lineHeight = -fm.top + fm.bottom;

            while (lineHeight > textHeight && size > minTextSize) {
            	size-= 1;
            	paint.setTextSize(size);
            	fm = paint.getFontMetricsInt();
                lineHeight = -fm.top + fm.bottom;
            }

            while (lineHeight < textHeight && size < maxTextSize) {
            	size+= 1;
            	paint.setTextSize(size);
            	fm = paint.getFontMetricsInt();
                lineHeight = -fm.top + fm.bottom;
            }
            return size;
        }
        return paint.getTextSize();
    }

    private float resizeFontToFit(Paint paint, String text, int textWidth) {
        if (textWidth > 0) {
            float size = paint.getTextSize();
            float minTextSize = 0;
            float maxTextSize = 30;
            float width = paint.measureText(text);

            while (width > textWidth && size > minTextSize) {
            	size-= 1;
            	paint.setTextSize(size);
            	width = paint.measureText(text);
            }

            while (width < textWidth && size < maxTextSize) {
            	size+= 1;
            	paint.setTextSize(size);
            	width = paint.measureText(text);
            }
            return size;
        }
        return paint.getTextSize();
    }


	/**
	 * Paints the puzzle's board.
	 *
	 * @param g Graphics object.
	 */
    public void draw(Canvas canvas) {
    	Paint paint = new Paint();
    	paint.setStrokeWidth(1);
    	paint.setStyle(Style.FILL);
    	paint.setAntiAlias(true);
    	paint.setColor(Color.WHITE);

    	FontMetricsInt fm = textPaint.getFontMetricsInt();

    	Rect rect = new Rect(0, 0, screenWidth, screenHeight);
    	canvas.drawRect(rect, paint);

    	paint.setStyle(Style.STROKE);
    	paint.setColor(Color.BLACK);

        float [] pts = new float[currentValue > 1 ? (currentValue-1)*4 : 0];
        
        for(int r = 0; r < boardSizeY; r++) {
            for(int c = 0; c < boardSizeX; c++) {

            	if (currentPosition != -1 && isMoveAllowed(r, c) && isLastMovePossible(r, c)) {
            		// Draw highlighted rectangular
            		paint.setStrokeWidth(3);
            	}
            	else {
            		paint.setStrokeWidth(1);
            	}
                float left = offsetX+c*squareWidth;
                float top = offsetY+r*squareWidth;
            	canvas.drawRect(left, top,
            			left + squareWidth, top + squareWidth, paint);

            	paint.setStrokeWidth(1);

                // Draw numbers
                if (movesTable[r*boardSizeX+c]!=-1) {
                    int val = movesTable[r*boardSizeX+c];
                    String str = String.format("%d", val + 1);
                    if (currentValue > 1) {
                        if (val == 0) {
                        	if (pts != null && pts.length > 1) {
	                            pts[0] = left + squareWidth/2;
	                            pts[1] = top + squareWidth/2;
                        	}
                        }
                        else if (val == currentValue - 1) {
                        	if (pts != null && pts.length > 1) {
	                            pts[pts.length - 2] = left + squareWidth/2;
	                            pts[pts.length - 1] = top + squareWidth/2;
                        	}
                        }
                        else {
                        	int start = 4*val - 2;
                        	if (pts != null && pts.length > start + 3) {
	                            pts[start] = left + squareWidth/2;
	                            pts[start+1] = top + squareWidth/2;
	                            pts[start+2] = left + squareWidth/2;
	                            pts[start+3] = top + squareWidth/2; 
                        	}
                        }
                    }
                    if (showNumbers || lineSize == 0) {
	                    canvas.drawText(str,
	                    		left + squareWidth/2,
	                    		top + squareWidth/2 + fm.bottom,
	                    		str.length() > 2 ? bigTextPaint : textPaint);
                    }
                }
            }
        }
        
        if (lineSize > 0) {
            paint.setColor(Color.RED);
            paint.setStrokeWidth(lineSize);
            canvas.drawLines(pts, paint);
        }

        // Highlight current position
        if (currentPosition >= 0) {
            int r = currentPosition/boardSizeX;
            int c = currentPosition%boardSizeX;

            paint.setColor(Color.RED);
            paint.setStrokeWidth(2);
            float left = offsetX+c*squareWidth;
            float top = offsetY+r*squareWidth;
        	canvas.drawRect(left, top,
        			left + squareWidth, top + squareWidth, paint);
            paint.setColor(Color.BLACK);
        }
    }

    // Solving functionality

	/**
	 * Initialises solver.
	 */
	protected void initSolver() {
		lockedMovesArray = new boolean[movesTableSize][allowedMovesCount];
		for(int i = 0; i < movesTableSize; i++)
			for(int j = 0; j < allowedMovesCount; j++)
				lockedMovesArray[i][j] = false;

        //firstMoveRow = firstMoveColumn = -1;
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
	 * Returns moves made so far.
	 *
	 * @return Returns moves made so far.
	 */
	protected int movesMade() {
	    return(currentValue);
	}

	/**
	 * Finds the next possible move. All possible moves are founded and
	 * are sorted based to the distance of the next square from the center
	 * of the board. Squares that are farer from the center are tried first.
	 *
	 * @return true if a move has been found.
	 */
    protected boolean findNextMove() {
        if (currentPosition==-1) {
        	if (firstMoveRow < 0) {
        		firstMoveRow = firstMoveColumn = boardSize/2;
            	firstMoveColumnStart = firstMoveColumn;
            	firstMoveRowStart = firstMoveRow; 
            	
        		makeMove(firstMoveRow, firstMoveColumn);
        		return true;
        	}
        	else {        		
        		firstMoveColumn++;
        		if (firstMoveColumn > boardSize) {
        			firstMoveColumn = 0;
        			firstMoveRow++;
        			if (firstMoveRow > boardSize) {
        				firstMoveRow = 0;
        			}
        		}
        		if (firstMoveRow == firstMoveRowStart && 
        					firstMoveColumn == firstMoveColumnStart) {
        			return false;
        		}
        		else {
        			//Log.d("NumberSquarePuzzle", "Trying " + firstMoveRow + ", " + firstMoveColumn);
        			makeMove(firstMoveRow, firstMoveColumn);
            		return true;	
        		}
        	}
        }

        int i;
        Vector<NextSquare> v  = new Vector<NextSquare>();

        int currentPositionRow = currentPosition/boardSizeX;
        int currentPositionColumn = currentPosition%boardSizeX;

        // Create table with distances
        for(i=0;i<allowedMovesCount;i++) {
            if (!knightMovesOnly) {
                NextSquare nS = new
                                NextSquare(boardSize,
                                      currentPositionRow + r_offset[i],
                                      currentPositionColumn + c_offset[i]);
                nS.index = i;
                v.add(nS);
            }
            else {
                int mobility = 0;

                NextSquare nS = new NextSquare(mobility);
                nS.index = i;
                v.add(nS);
            }
        }

        // Sort table
        if(!knightMovesOnly) {
            Collections.sort(v);
        }

        for (NextSquare nextSquare: v ) {
            i = nextSquare.index;
            int r_new =  currentPositionRow+ r_offset[i],
                c_new = currentPositionColumn + c_offset[i],
                pos_old = currentPosition;

            if (isMoveAllowed(r_new, c_new) &&
                isLastMovePossible(r_new, c_new) &&
                	!lockedMovesArray[currentPosition][i]) {
                MoveResult result = makeMove(r_new, c_new);
                if (result == MoveResult.MOVE_SUCCESSFUL) {
                    if (!knightMovesOnly || checkMobibility()) {
                        lockedMovesArray[pos_old][i] = true;
                        return(true);
                    }
                    else {
                        // Undo move
                        makeMove(r_new, c_new);
                        return(false);
                    }
                }
                else if (result == MoveResult.RIDDLE_SOLVED) {
                    return true;
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
    protected boolean playNextMove() {
        if (solMovesTableIndex >= solMovesTableSize)
            return(false);

        for(int i = 0; i < solMovesTableSize; i++) {
            if (solMovesTable[i] == solMovesTableIndex) {
                makeMove(i/boardSize, i%boardSize);
                break;
            }
        }
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
    protected boolean goBack() {
        if (currentPosition==-1) {
        	// The puzzle is unsolvable if we are forced to undo
        	// first move.
        	solverRunning = false;
        	return false;

        	// The squares marked below with x (for size 6), are tried
        	// as first moves.
        	//
        	//  x x x 0 0 0
        	//  0 x x 0 0 0
        	//  0 0 x 0 0 0
        	//  0 0 0 0 0 0
        	//  0 0 0 0 0 0
        	//  0 0 0 0 0 0
        }

        int r_last = currentPosition/boardSizeX,
            c_last = currentPosition%boardSizeX;

        for(int i = 0; i< allowedMovesCount; i++) {
            lockedMovesArray[currentPosition][i] = false;
        }

        // Undo last move
        makeMove(r_last, c_last);

        return(true);
    }

    /** Class for sorting distances of squares from the center.
    *   Implements comparable interface.
    */
    class NextSquare implements Comparable<NextSquare> {
        /**
         * Index (0..11) of move required to get from current square
         * to this next square.
         *
         * @see #r_offset
         * @see #c_offset
         */
        int index;

        /**
         * Integer Object holding the distance of nextSquare from the
         * center of the board.
         */
        private Integer distance;

        /**
        * Class constructor.
        *
        * @param size Board's size.
        * @param row Row of next square.
        * @param column Column of next square.
        */
        public NextSquare(int size, int row, int column) {
            // Distance (size = 10)
            //
            //    index: 0 1 2 3 4 5 6 7 8 9
            // distance: 4 3 2 1 0 0 1 2 3 4

            int halfSize = size/2;
            int rowDist = (row < halfSize) ? (halfSize - row - 1) :
                                             (row - halfSize);
            int colDist = (column < halfSize) ? (halfSize - column - 1) :
                                                (column - halfSize);

            distance = Integer.valueOf(rowDist > colDist ? rowDist : colDist);
        }

        /**
        * Class constructor.
        *
        * @param mobility Square's mobility
        */
        public NextSquare(int mobility) {
            distance = Integer.valueOf(mobility);
        }

        /**
        * compareTo method.
        */
        public int compareTo(NextSquare o) {
            NextSquare that = (NextSquare) o;
            return -this.distance.compareTo(that.distance);
        }
    }

	@Override
	public boolean areMovesLeft() {
		if (isSolved()) {
			return false;
		}
		
		for(int r=0;r<boardSizeY;r++) {
			for(int c=0;c<boardSizeX;c++) {
				if (isMoveAllowed(r, c)) {
					return false;
				}
			}
		}
		return true;
	}
}
