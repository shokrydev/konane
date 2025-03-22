import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomPlayer {
    Random rand = new Random();
    MiniKonane game;
    int[][] board;
    int player;


    RandomPlayer(MiniKonane newGame, int playerNumber){
        game = newGame;
        board = game.getBoard();
        player = playerNumber;
    }

    public int xDim(){
        return board.length;
    }

    public int yDim(){
        return board[0].length;
    }

    public boolean validCoordinates(int x, int y) {
        if(0 <= x && x < xDim() && 0 <= y && y < yDim()) return true;
        return false;
    }

    //depends on implemented x and y dimensions
    public List<Move> getValidMoves(){
        List<Move> validMoves= new ArrayList<Move>();
        int steps;
        for(int i = 0; i < xDim(); i++) {
            for (int j = 0; j < yDim(); j++) {
                if(board[i][j]==player) {
                    for (int[] dir: Move.directions) {
                        steps = 1; // 2er schritte
                        while(validCoordinates(i+steps*2*dir[0], j+steps*2*dir[1]) //Landing square still on Board ?
                                && 0==board[i+steps*2*dir[0]][j+steps*2*dir[1]] //Landing square empty ?
                                && -player==board[i+(2*steps-1)*dir[0]][j+(2*steps-1)*dir[1]] // space between current and target square belongs to enemy
                        ){
                            validMoves.add(new Move(i,j,i+steps*2*dir[0],j+steps*2*dir[1], steps, dir[0], dir[1],-1));
                            steps++; // 2er schritte
                        }
                    }
                }
            }
        }
        return validMoves;
    }

    public Move selectMove(){
        List<Move> validMoves = getValidMoves();
        return validMoves.get(rand.nextInt(validMoves.size()));
    }

    public String validMovesToString(int player) {
        String movesString = "";
        for (Move m: getValidMoves()) {
            movesString += m.toString() + "\n";
        }
        return movesString;
    }
}
