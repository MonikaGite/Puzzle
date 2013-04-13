package gr.sullenart.games.puzzles.gameengine.solo;

public class SoloGame {
	public enum SoloGameType {
		SQUARE(0),
		SQUARE_DIAGONAL(1),
		TRIANGULAR(2);
		
		private int code;
		
		public int getCode() {
			return code;
		}
		
		public static SoloGameType getGameType(int code) {
			switch(code) {
			case 0:
				return SQUARE;
			case 1:
				return SQUARE_DIAGONAL;
			case 2:
				return TRIANGULAR;
			}
			throw new ArrayIndexOutOfBoundsException(code);
		}
		
		SoloGameType(int code) {
			this.code = code;
		}
	}
	
	private int [] board;
	
	private int targetPosition;
	
	private SoloGameType type;

	public int [] getBoard() {
		return board;
	}

	public void setBoard(int [] board) {
		this.board = board;
	}

	public int getTargetPosition() {
		return targetPosition;
	}

	public void setTargetPosition(int targetPosition) {
		this.targetPosition = targetPosition;
	}

	public SoloGameType getType() {
		return type;
	}

	public void setType(SoloGameType type) {
		this.type = type;
	}

	public boolean isDiagonalMoveAllowed() {
		return type == SoloGameType.SQUARE_DIAGONAL;
	}
	
	public SoloGame(int [] board) {
		this.board = board;
		targetPosition = -1;
		type = SoloGameType.SQUARE;
	}
	
	public SoloGame(int [] board, int finalPosition, SoloGameType type) {
		this.board = board;
		this.targetPosition = finalPosition;
		this.type = type;
	}
	
	public SoloGame(SoloGame soloGame) {
		this.board = soloGame.getBoard();
		this.targetPosition = soloGame.getTargetPosition();
		this.type = soloGame.type;
	}	
}
