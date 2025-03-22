import java.util.Random;

public class MiniKonane {
    int[][] board;

    static int BLACK = -1; //@,left,first player
    static int EMPTY = 0;
    static int WHITE = 1; //O,right,second player
    int activePlayer;

    public MiniKonane(int x, int y) {
        board = new int[x][y];
        activePlayer = BLACK;

        // x spalten mit jeweils y einträgen
        int flip= 0;
        for(int i = 0; i < x; i++) {
            flip = i%2==0?BLACK:WHITE; //abwechselnde Startsteine, oben links ist -1 bzw
            for (int j = 0; j < y; j++) {
                board[i][j] = flip ;
                flip *= -1;
            }
        }
    }

    void switchPlayer(){
        activePlayer = -activePlayer;
    } //next players turn

    public int xDim(){
        return board.length;
    }

    public int yDim(){
        return board[0].length;
    }

    public int[][] getBoard() {
        return board;
    }

    public boolean validCoordinates(int x, int y) {
        return 0 <= x && x < xDim() && 0 <= y && y < yDim();
    }

    public void makeMove(Move m) {
        if(!validCoordinates(m.startX,m.startY) | !validCoordinates(m.targetX,m.targetY)){
            return;
        }
        for (int i = 1; i <= m.jumps; i++) {
            if (board[m.startX+(2*i-1)*m.Xdirection][m.startY+(2*i-1)*m.Ydirection] != -activePlayer
                | board[m.startX+i*2*m.Xdirection][m.startY+i*2*m.Ydirection] != EMPTY){
                System.out.println("MK: Invalid Move" + activePlayer + " " + m);
                return;
            }
        }
        board[m.startX][m.startY] = EMPTY;
        for (int i = 1; i <= m.jumps; i++) {
            board[m.startX+(2*i-1)*m.Xdirection][m.startY+(2*i-1)*m.Ydirection] = EMPTY; // remove jumped over pieces
        }
        board[m.targetX][m.targetY] = activePlayer;

        switchPlayer();
    }


    public String boardToString() {
        StringBuilder visualisierung = new StringBuilder();
        for(int i = 0; i < yDim(); i++) {
            for  (int j = 0; j < xDim(); j++){
                if (board[j][i] == -1){
                    visualisierung.append(" @");
                } else if (board[j][i] == 0) {
                    visualisierung.append(" .");
                } else if (board[j][i] == 1) {
                    visualisierung.append(" O");
                }
            }
            visualisierung = new StringBuilder(visualisierung.toString().concat("\n"));
        }
        return visualisierung.toString();
    }

    public static void main(String[] args) {
    }
}

