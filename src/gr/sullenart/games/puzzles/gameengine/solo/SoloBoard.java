package gr.sullenart.games.puzzles.gameengine.solo;

import gr.sullenart.games.puzzles.gameengine.solo.SoloGame.SoloGameType;

public class SoloBoard {
	
	private int [] board;

	private int columnCount;

	private int rowCount;

	private int boardSize;

	private int targetPosition;
	
	private int [] removedPosOffsets;

	private int [] newPosOffsets;
	
	private boolean [] allowedMoves;
	
	public int[] getBoard() {
		return board;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public int getRowCount() {
		return rowCount;
	}

	public int getBoardSize() {
		return boardSize;
	}
    
	private SoloGameType soloGameType;
	
    public boolean isTriangular() {
        return soloGameType == SoloGameType.TRIANGULAR;
    }
    
	public void setTargetPosition(int newPos) {
		targetPosition = newPos;
	}    
	
	public SoloBoard(int [] boardInit, int columnCount, int rowCount, 
					SoloGameType soloGameType, int finalPosition) {
		this.board = boardInit;
		this.columnCount = columnCount;
		this.rowCount = rowCount;

		this.targetPosition = finalPosition;
		this.soloGameType = soloGameType;
		
		if (soloGameType == SoloGameType.SQUARE) {
			allowedMoves = new boolean[] {true,true,true,true,false,false,false,false};
		}
		else if (soloGameType == SoloGameType.SQUARE_DIAGONAL) {
			allowedMoves = new boolean[] {true,true,true,true,true,true,true,true};
		}		
		else if (soloGameType == SoloGameType.TRIANGULAR) {
			allowedMoves = new boolean [] {true,true,true,true,false,true,false,true};
		}
		
		boardSize = columnCount * rowCount;

		removedPosOffsets = new int [] 
		       {1, columnCount, -1, -columnCount,
				-columnCount+1, columnCount+1, columnCount-1, -columnCount-1};
		newPosOffsets = new int [] 
		       {2, 2*columnCount, -2, -2*columnCount,
				-2*columnCount+2, 2*columnCount+2, 2*columnCount-2, -2*columnCount-2};
	}

	public int getTargetPosition() {
		return targetPosition;
	}

	public boolean isInBoard(int pos) {
		return pos < boardSize && pos >= 0;
	}

	public int getJumpedPegPos(int boardPos, int dir) {
		return boardPos + removedPosOffsets[dir];
	}

	public int getNewPos(int boardPos, int dir) {
		return boardPos + newPosOffsets[dir];
	}

	public void makeMove(int pos, int dir) {
        board[pos] = 0;
        board[pos + removedPosOffsets[dir]] = 0;
        board[pos + newPosOffsets[dir]] = 1;
	}

	public void undoMove(int pos, int dir) {
        board[pos] = 1;
        board[pos + removedPosOffsets[dir]] = 1;
        board[pos + newPosOffsets[dir]] = 0;
	}
    
    public boolean isAllowedJumpPosition(int selPos, int r, int c) {
        for(int dir=0;dir<Solo.DIRECTIONS_COUNT;dir++) {            
            int newBoardPos = r*columnCount + c;
            if (board[newBoardPos] != 0) {
                continue;
            }
            
            if (!allowedMoves[dir]) {
                continue;
            }

            if (selPos + newPosOffsets[dir] == newBoardPos &&
                    board[selPos + removedPosOffsets[dir]] == 1) {
                int selPosColumn = selPos % columnCount;
                switch(dir) {
                    case Solo.EAST:
                    case Solo.NORTH_EAST:
                    case Solo.SOUTH_EAST:
                         if (c == selPosColumn + 2) {
                            return true;
                         }
                         break;
                    case Solo.WEST:
                    case Solo.SOUTH_WEST:
                    case Solo.NORTH_WEST:                    
                         if (c == selPosColumn - 2) {
                            return true;
                         }
                         break;
                    case Solo.SOUTH:                    
                    case Solo.NORTH:
                         if (c == selPosColumn) {
                            return true;
                         }
                         break;
                }
            }
        }
        return false;
    }
	
	public boolean isMoveAvailable(int boardPos) {
        int r = boardPos / columnCount;
        int c = boardPos % columnCount;

        if  (!isInBoard(boardPos) || board[boardPos] != 1 )
            return false;
        
        if (allowedMoves[Solo.EAST] &&
        		!(c >= columnCount - 2 || board[boardPos+1] != 1 || board[boardPos+2] != 0))
            return true;
        if (allowedMoves[Solo.SOUTH] &&
        		!(r >= rowCount - 2 || board[boardPos + columnCount] != 1 || board[boardPos + 2*columnCount] != 0))
            return true;
        if (allowedMoves[Solo.WEST] &&
        		!(c <= 1 || board[boardPos-1] != 1 || board[boardPos-2] != 0))
            return true;
        if (allowedMoves[Solo.NORTH] &&
        		!(r <= 1 || board[boardPos - columnCount] != 1 || board[boardPos - 2*columnCount] != 0))
            return true;
    	if (allowedMoves[Solo.NORTH_EAST] &&
    			!(r <= 1 || c >= columnCount - 2 ||
    			  board[boardPos-columnCount+1] != 1 ||
    			  board[boardPos-2*columnCount+2] != 0)) {
    		return true;
        	}
    	if (allowedMoves[Solo.SOUTH_EAST] &&
    			!(r >= rowCount - 2 || c >= columnCount - 2 ||
    			  board[boardPos+columnCount+1] != 1 ||
    			  board[boardPos+2*columnCount+2] != 0)) {
    		return true;
    	}        	
        if (allowedMoves[Solo.SOUTH_WEST] &&
        		!(r >= rowCount - 2 || c <= 1 ||
        		  board[boardPos+columnCount-1] != 1 ||
        		  board[boardPos+2*columnCount-2] != 0)) {
        		return true;
        	}	
    	if (allowedMoves[Solo.NORTH_WEST] &&
    			!(r <= 1 || c <= 1 ||
    			  board[boardPos-columnCount-1] != 1 ||
    			  board[boardPos-2*columnCount-2] != 0)) {
    		return true;
    	}
        
        
        return false;
	}

    public boolean isMoveLegal(int val) {
        int boardPos = Solo.GET_BOARD_POS(val);
        int dir = Solo.GET_DIR(val);

        if (!allowedMoves[dir])
        	return false;        
        
        int r = boardPos / columnCount;
        int c = boardPos % columnCount;

        if  (!isInBoard(boardPos) || board[boardPos] != 1 )
            return false;

        if (dir == Solo.EAST) {
            if (c >= columnCount - 2 || board[boardPos+1] != 1 || board[boardPos+2] != 0)
                return false;
        }
        else if (dir == Solo.SOUTH) {
            if (r >= rowCount - 2 || board[boardPos + columnCount] != 1 || board[boardPos + 2*columnCount] != 0)
                return false;
        }
        else if (dir == Solo.WEST) {
            if (c <= 1 || board[boardPos-1] != 1 || board[boardPos-2] != 0)
                return false;
        }
        else if (dir == Solo.NORTH) {
            if (r <= 1 || board[boardPos - columnCount] != 1 || board[boardPos - 2*columnCount] != 0)
                return false;
        }
        else if (dir == Solo.NORTH_EAST) {
        	if (r <= 1 || c >= columnCount - 2 ||
        			board[boardPos-columnCount+1] != 1 ||
        			board[boardPos-2*columnCount+2] != 0) {
        		return false;
        	}
        }
        else if (dir == Solo.SOUTH_EAST) {
        	if (r >= rowCount - 2 || c >= columnCount - 2 ||
        			board[boardPos+columnCount+1] != 1 ||
        			board[boardPos+2*columnCount+2] != 0) {
        		return false;
        	}        	
        }
        else if (dir == Solo.SOUTH_WEST) {
        	if (r >= rowCount - 2 || c <= 1 ||
        			board[boardPos+columnCount-1] != 1 ||
        			board[boardPos+2*columnCount-2] != 0) {
        		return false;
        	}	
        }
        else if (dir == Solo.NORTH_WEST) {
        	if (r <= 1 || c <= 1 ||
        			board[boardPos-columnCount-1] != 1 ||
        			board[boardPos-2*columnCount-2] != 0) {
        		return false;
        	}
        } 

        return true;
    }

	public int getDir(int oldPos, int newPos) {
		int r_old = oldPos / columnCount,
	    	c_old = oldPos % columnCount,
	    	r = newPos / columnCount,
	    	c = newPos % columnCount;

		if (r == r_old - 2) {
			if (c == c_old) {
				return Solo.NORTH;
			}
			if (c == c_old - 2) {
				return Solo.NORTH_WEST;
			}
			if (c == c_old + 2) {
				return Solo.NORTH_EAST;
			}			
		}

		if (r == r_old + 2) {
			if (c == c_old) {
				return Solo.SOUTH;
			}
			if (c == c_old - 2) {
				return Solo.SOUTH_WEST;
			}
			if (c == c_old + 2) {
				return Solo.SOUTH_EAST;
			}			
		}		
		
		if (r == r_old) {
			if (c == c_old + 2)
				return Solo.EAST;
			else if (c == c_old - 2)
				return Solo.WEST;
		}
	
		return -1;
	}
	
	public int getMoveCount(int [] vals) {
		int moveCount = 0;
		int prev = -1;
		int i = 0;
		
		while (i < vals.length && vals[i] >= 0) {
			int boardPos = Solo.GET_BOARD_POS(vals[i]);
			int dir = Solo.GET_DIR(vals[i]);
			if (boardPos != prev) {
				moveCount++;
			}
			prev = boardPos + newPosOffsets[dir];
			i++;
		}
		
		return moveCount;
	}	

    public boolean isNotSolvable() {
    	int pegCount = 0;
        for(int pos=0; pos<board.length; pos++) {
            if (isMoveAvailable(pos)) {
                return false;
            }
            if (board[pos] == 1) {
            	pegCount++;
            }
        }
        return pegCount > 1;
    }

	public boolean isBoardValid() {		
		int holesCount = 0;
		int pegsCount = 0;
		for (int pos = 0;pos<board.length;pos++) {
			if (board[pos]==-1)
				continue;
			else if (board[pos]==0) {
				holesCount++;
			}
			else if (board[pos]==1) {
				pegsCount++;
			}			
			
			int r = pos / columnCount;
			int c = pos % columnCount;
			if (r > 0) {
				if (board[pos-columnCount]>=0)
					continue;
			}
			if (r < columnCount - 1) {
				if (board[pos+columnCount]>=0)
					continue;				
			}
			if (c > 0) {
				if (board[pos-1]>=0)
					continue;				
			}
			if (c < rowCount - 1) {
				if (board[pos+1]>=0)
					continue;				
			}
			return false;
		}
		return pegsCount > 1 && holesCount > 0;
	}    
    
    public void print() {
		int i = 0;
		String str;
		for (int c=0; c<columnCount; c++) {
			str = "";
			for (int r=0; r<rowCount; r++) {
				str += board[i] >=0 ? "" + board[i] : " ";
				i++;
			}
			System.out.println(str);
		}
		System.out.println("--------------");
	}

}
