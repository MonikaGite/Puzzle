package gr.sullenart.games.puzzles.gameengine;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;

/**
 * Abstract Puzzle Class. It encapsulates functionality common to all
 * puzzles. It implements the solve method, that iterates through all
 * possible moves to find a solution.
 */
public abstract class Puzzle {
	/** Parent context.*/
	Context context;

    /** Screen width*/
    protected int screenWidth;

    /** Screen height*/
    protected int screenHeight;

    /** The offset from the left side of the screen */
    protected int offsetX = 5;

    /** The offset from the top side of the screen */
    protected int offsetY = 5;

	/** The height of the top messages bar */
    protected int topBarHeight = 60;

    /** The height of the bottom toolbar */
	protected int bottomBarHeight = 60;
    
    /** Sets the top messages bar height
    *   @param h Height of the bar
    */    
	public void setTopBarHeight(int h) {
		topBarHeight = h;
	}

    /** Sets the bottom toolbbar height
    *   @param h Height of the bar
    */    
	public void setBottomBarHeight(int h) {
		bottomBarHeight = h;
	}	    
    
    /* Informs the puzzle of a screen size change
    *  @param w The screen's width
    *  @param h The screen's height
    */
    public void onSizeChanged(int w, int h) {
    	screenWidth = w;
    	screenHeight = h;
    }

	/**
	* Table for storing moves. When the user or the solver make a
	* move, they fill the next free element of movesTable with a value
	* describing the move. At the beginning the table is initialised with
	* value -1, which represents an empty element. To solve the puzzle a
	* total of movesTableSize moves must be made, filling the entire
	* table with values > -1.
	*/
	protected int [] movesTable;

	/** Size of movesTable.*/
	protected int movesTableSize = 0;

	/**
	* Table for storing solution's move. It is used to keep a
	* copy of the solution so that it can be replayed.
	*/
	protected int [] solMovesTable;

	/** Size of solMovesTable.*/
	protected int solMovesTableSize = 0;

	/**
	* When the solution is replayed the values from solMovesTable are used
	* one after another to make the proper moves. solMovesTableIndex points
	* to the position of the next element of solMovesTable to be used.
	*/
	protected int solMovesTableIndex;

	/** Puzzle's name.*/
	protected String name;

	/** Puzzle's family name */
	protected String family;

    /** Flag for enabling/disabling solve.*/
	protected boolean enableSolve = true;

	/** Flag for enabling/disabling undo.*/
	protected boolean enableUndo = true;

	/** Flag for enabling/disabling replay.*/
	protected boolean enableReplay = true;

	/** true if solving has started.*/
	protected boolean solverRunning = false;

	/** true if replay solution has started.*/
	protected boolean replayRunning = false;

	/** true if replay solution has stopped.*/
	protected boolean replayEnded = false;

	/** true if game has started */
	protected boolean isStarted = false;

	public enum MoveResult {
		MOVE_NOT_ALLOWED,
		MOVE_OUT_OF_BOUNDS,
		RIDDLE_SOLVED,
		MOVE_SUCCESSFUL,
		MOVE_EDIT,
		RIDDLE_UNSOLVABLE
	}
	
	/** Flag for allowing the creation of a custom board */
	protected boolean enableAdd = false;

	/** Getter for enableAdd flag
	 * 
	 * @return true, if adding a custom board is allowed
	 */
	public boolean isAddAllowed() {
		return enableAdd;
	}

	/**
	 * Returns true if game has started
	 * @return true if game has started
	 */
	public boolean isStarted() {
		return isStarted;
	}    
    
	/**
	 * @param context Parent context
	 */
	public Puzzle(Context context) {
		this.context = context;
	}

	/**
	 * Configures puzzle
	 * @param preferences A SharedPreferences object
	 * @return true if a configuration change was made
	 */
	abstract public boolean configure(SharedPreferences preferences);

	/**
	 * Returns puzzle's name.
	 *
	 * @return puzzle's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns puzzle's family name.
	 *
	 * @return puzzle's family name.
	 */
	public String getFamily() {
		return family;
	}

	/**
	 * Returns true if solving is supported.
	 *
	 * @return true if solving is supported.
	 */
	public boolean isSolvePermitted() {
		return enableSolve;
	}

	/**
	 * Returns true if solving has started.
	 *
	 * @return true if solving has started.
	 */
	public boolean isSolverRunning() {
		return solverRunning;
	}

	/**
	 * Set solver running state.
	 *
	 * @param val solver running state
	 */
	public void setSolverRunning(boolean val) {
		solverRunning = val;
	}	
	
	/**
	 * Returns true if replay solution has started.
	 *
	 * @return true if replay solution has started.
	 * */
	public boolean isReplayRunning() {
		return replayRunning;
	}

	/**
	 * Returns true if replay solution has stopped.
	 *
	 * @return true if replay solution has stopped.
	 * */
	public boolean isReplayEnded() {
		return replayEnded;
	}

	/**
	 * Sets whether replay is active
	 * @param value true if replay is active
	 */
	public void setReplayRunning(boolean value) {
		replayRunning = value;
	}

	/**
	 * Returns true if replay is supported.
	 *
	 * @return true if replay is supported.
	 */
	public boolean isReplayPermitted() {
		return enableReplay;
	}

	/**
	 * Returns true if undoing is supported.
	 *
	 * @return true if undoing is supported.
	 */
	public boolean isUndoPermitted() {
		return enableUndo;
	}
	
	/**
	 * 
	 * @return The table of moves made.
	 */
	public int [] getSolution() {
		return movesTable;
	}

	/**
	 * Dumps puzzle's status (moves made so far). Debugging only.
	 */
    public void dumpStatus() {
        for(int i=0; i < movesTableSize; i++)
            System.out.print(movesTable[i] + " ");
        System.out.print("\n");
    }

	/**
	 * Puzzle initialization.
	 */
	public void init() {
		if (movesTableSize > 0) {
			movesTable = new int[movesTableSize];
			for(int i =0; i<movesTableSize; i++)
				movesTable[i] = -1;
		}
		solverRunning = false;
		isStarted = false;
		replayEnded = false;		
	}
	

	/**
	 * Abstract method for handling mouse clicks.
	 *
	 * @param x Cursor's x position.
	 * @param y Cursor's y position.
	 */
    abstract public MoveResult onTouchEvent(float x, float y);

	/**
	 * Abstract method the undoes last move.
	 */
	abstract public void undoLastMove();

	/**
	 * Abstract methods that paints the board.
	 *
	 * @param canvas Canvas object.
	 */
	abstract public void draw(Canvas canvas);

    int totalCounter = 0;
    int counter = 0;

	/**
	 * Solves the puzzle.
	 *
	 * It solves the puzzle by iterating through all possible moves. Uses
	 * abstract methods movesMade, findNextMove, goBack.
	 *
	 * @param auto If true it runs until a solution is found. Otherwise
	 *             it makes a move every time is called.
	 * @return One of the following codes: MOVE_SUCCESSFUL, RIDDLE_SOLVED,
	 *         RIDDLE_UNSOLVABLE
	 */
	public MoveResult solve(boolean auto) {
        counter++;
        if (counter == 100) {
            counter = 0;
            totalCounter++;
        }

		boolean searched_all = false;

		if (!solverRunning) {
			solverRunning = true;
			initSolver();
		}

	    while(!searched_all && movesMade() != movesTableSize) {
            if (!findNextMove()) {
                searched_all = !goBack();
                //while(!searched_all && movesMade() == 0) {
                //    searched_all = !goBack();
                //}
            }
            if (!auto)
            	break;
	    }
	    if (searched_all) {
	        return MoveResult.RIDDLE_UNSOLVABLE;
	    }
	    if (movesMade() == movesTableSize) {
	    	solverRunning = false;
	    }
	    if (movesMade() == movesTableSize) {
	    	return MoveResult.RIDDLE_SOLVED;
	    }
	    return MoveResult.MOVE_SUCCESSFUL;
	}

	/**
	 * Replays current solution.
	 *
	 * Uses abstract method playNextMove.
	 *
	 * @return true if there isn't another move to replay
	 */
	public boolean replay() {
		boolean result;
        if (solMovesTableSize == 0) {
            result = true;
        }
        else {
        	result = !playNextMove();
        }

        if (result) {
        	replayEnded = true;
        }
        return result;
	}

	/**
	 * Copy current solution so that it can be replayed.
	 */
	public void copySolution() {
        solMovesTableSize = movesTableSize;

        if (movesTableSize == 0)
            return;

        solMovesTable = new int [movesTableSize];

        int i;
        for(i=0; i<movesTableSize && movesTable[i]!=-1; i++) {
            solMovesTable[i] = movesTable[i];
        }
        solMovesTableSize = i;
        solMovesTableIndex = 0;
    }

	/**
	 * Abstract method that initializes the solver.
	 */
	abstract protected void initSolver();

	/**
	 * Abstract method that returns the number of moves made so far.
	 *
	 * @return Number of moves made so far.
	 */
	abstract protected int movesMade();

	/**
	 * Abstract method that finds the next possible move.
	 *
	 * @return true if a move has been found.
	 */
	abstract protected boolean findNextMove();

	/**
	 * Abstract method that goes back (undo last move) when a deadlock
	 * is encountered.
	 *
	 * @return If it is impossible to go back false is returned. This
	 *         means that the puzzle cannot be solved.
	 */
	abstract protected boolean goBack();

	/**
	 * Abstract method that returns solver's status.
	 *
	 * @return true if the puzzle is solved.
	 */
	abstract public boolean isSolved();

	/**
	 * Abstract method that replays one move.
	 *
	 * @return false if there are no more moves to replay.
	 */
	abstract protected boolean playNextMove();
    
    /*
     * Abstract method that tests is a puzzle is not solvable. Returning false
     * doesn't necessarily mean that the puzzle can be solved. This method must
     * perform a basic test quickly in order to recognize obviously not solvable
     * puzzles. A basic implementation can always return false.
     *
     * @return true if there are no more moves left.
     */ 
    abstract public boolean areMovesLeft();

}
