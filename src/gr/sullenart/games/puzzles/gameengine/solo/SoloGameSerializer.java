package gr.sullenart.games.puzzles.gameengine.solo;

import gr.sullenart.games.puzzles.gameengine.solo.SoloGame.SoloGameType;

public class SoloGameSerializer {
        
	private static int metadataVersion = 0;
	
	public static String serializeMetadata(SoloGame soloGame) {
		return String.format("%02d%03d%d", metadataVersion,
							 soloGame.getTargetPosition() + 1,
							 soloGame.getType().getCode());
	}
    
	public static String serializeBoard(int [] board) {
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
	
	public static SoloGame deserialize(String boardStr, String metadataStr) {
		SoloGame soloGame = new SoloGame(deserializeBoard(boardStr));
		
		int targetPosition = Integer.parseInt(metadataStr.substring(2, 5)) - 1;
		int typeCode = Integer.parseInt(metadataStr.substring(5, 6));
		
		soloGame.setTargetPosition(targetPosition);
		soloGame.setType(SoloGameType.getGameType(typeCode));
		return soloGame;
	}
    
    public static SoloGame deserializeWithNoMetadata(String boardStr) {
		SoloGame soloGame = new SoloGame(deserializeBoard(boardStr));
		return soloGame;    
    }
	
	private static int [] deserializeBoard(String boardStr){
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
	}
    

}