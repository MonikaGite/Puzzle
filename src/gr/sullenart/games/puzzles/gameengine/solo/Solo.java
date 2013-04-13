package gr.sullenart.games.puzzles.gameengine.solo;

public class Solo {
	public static final int DIRECTIONS_COUNT = 8;
	
	public static final int EAST = 0;
	public static final int SOUTH = 1;
	public static final int WEST = 2;
	public static final int NORTH = 3;
	
	public static final int NORTH_EAST  = 4;
	public static final int SOUTH_EAST  = 5;
	public static final int SOUTH_WEST  = 6;
	public static final int NORTH_WEST  = 7;

	public static int MAKE_VAL(int boardPos, int dir) {
		return ((boardPos)*10 + (dir));
	}

	public static int GET_BOARD_POS(int val) {
		return ((val) / 10);
	}

	public static int GET_DIR(int val){
		return ((val) % 10);
	}

	public static int REVERSE_DIR(int dir) {
		if (dir % 4 > 1) return dir - 2;
		else return dir + 2;
	}

	public static int GET_BOARD_SIZE(int [] board) {
		int length = board.length;
		switch(length){
			case   9: return 3;
			case  16: return 4;
			case  25: return 5;
			case  36: return 6;
			case  49: return 7;
			case  64: return 8;
			case  81: return 9;
			case 100: return 10;
			case 121: return 11;
			case 144: return 12;
		}
		throw new ArrayIndexOutOfBoundsException(length);
	}
	
	public static String serializeSolution(int [] solution) {
		StringBuilder builder = new StringBuilder();
		builder.append('1');
		for(int i=0;i<solution.length;i++){
			int v = solution[i];
			builder.append('0' + v/100);
			v = v % 100;
			builder.append('0' + v/10);
			v = v % 10;
			builder.append('0' + v);
		}
		return builder.toString();
	}	

}
