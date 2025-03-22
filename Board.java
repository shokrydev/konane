import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Board {
    static int BLACK = -1; //@,left,first player
    static int EMPTY = 0; //@,left,first player
    static int WHITE = 1; //0,right,second player

    private final int xSize;
    private final int ySize;
    private final int[][] fields;

    private int halfmoveClock; // plyClock
    private int sideToMove;

    private int blackPiecesCounter;
    private int whitePiecesCounter;


    Random rand;

    Board(int xDim, int yDim) {


        xSize = xDim;
        ySize = yDim;
        fields = new int[xDim][yDim];

        halfmoveClock = 1;
        sideToMove = BLACK;

        int boardSize = xSize * ySize;
        //Top left is black
        blackPiecesCounter = boardSize/2;
        if(boardSize%2!=0) blackPiecesCounter++;
        whitePiecesCounter = boardSize/2;

        rand = new Random();

        // x columns with y entries each
        int flip= 0;
        for(int i = 0; i < xSize; i++) {
            flip = i%2==0?BLACK:WHITE; //color of first stone in row alternating
            for (int j = 0; j < ySize; j++) {
                fields[i][j] = flip ;
                flip *= -1;
            }
        }
    }

    Board( Board original) {

        xSize = original.xSize;
        ySize = original.ySize;
        fields = new int[original.xSize][original.ySize];

        halfmoveClock = original.getHalfmoveClock();
        sideToMove = original.sideToMove;

        int boardSize = xSize * ySize;
        //Top left is black
        blackPiecesCounter = original.blackPiecesCounter;
        whitePiecesCounter = original.whitePiecesCounter;

        rand = original.rand;

        // x columns with y entries each
        for(int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                fields[i][j] = original.fields[i][j];
            }
        }
    }

    int getPiecesCounter(int color){
        if(color == BLACK) return blackPiecesCounter;
        return whitePiecesCounter;
    }

    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    public int getSideToMove() {
        return sideToMove;
    }

    void removeStarter(int x, int y){
        fields[x][y] = EMPTY;
        nextPlayer();
    }

    void previousPlayer(){
        sideToMove *= -1;
        halfmoveClock--;
    }

    void nextPlayer(){
        sideToMove *= -1;
        halfmoveClock++;
    }

    public int xDim(){
        return fields.length;
    }

    public int yDim(){
        return fields[0].length;
    }

    public int[][] getFields() {
        return fields;
    }

    public boolean validCoordinates(int x, int y) {
        if(0 <= x && x < xDim() && 0 <= y && y < yDim()) return true;
        return false;
    }

    //abhängig von array zu x y ausrichtung
    public LinkedList<Move> getValidMoves(int player) { // dynamisch - updaten NACH EINZELNEN SPIELSCHRITTEN
        LinkedList<Move> validMoves= new LinkedList<Move>();
        int steps;
        for(int i = 0; i < xDim(); i++) {
            for (int j = 0; j < yDim(); j++) {
                if(fields[i][j]==player) {
                    for (int[] dir: Move.directions) {
                        steps = 1; // 2er schritte
                        while(validCoordinates(i+steps*2*dir[0], j+steps*2*dir[1]) //Landing square still on Board ?
                                && EMPTY== fields[i+steps*2*dir[0]][j+steps*2*dir[1]] //Landing square empty ?
                                && -player== fields[i+(2*steps-1)*dir[0]][j+(2*steps-1)*dir[1]] // space between current and target square belongs to enemy
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

    public Move getRandomMoveOrNull(int player) { // dynamisch - updaten NACH EINZELNEN SPIELSCHRITTEN
        int randomStartX = rand.nextInt(xDim());
        int randomStartY = rand.nextInt(yDim());
        int randomStartDir;
        int randomSteps;

        int[] dir;

        int steps;
        for(int i = 0; i < xDim(); i++) {
            for (int j = 0; j < yDim(); j++) {


                randomStartDir = rand.nextInt(4);

                if(fields[randomStartX][randomStartY]==player) {


                    for (int k = 0; k < 4; k++) {

                        dir = Move.directions[randomStartDir];

                        steps = 1; // 2er schritte
                        while(validCoordinates(randomStartX+steps*2*dir[0], randomStartY+steps*2*dir[1]) //Landing square still on Board ?
                                && EMPTY== fields[randomStartX+steps*2*dir[0]][randomStartY+steps*2*dir[1]] //Landing square empty ?
                                && -player== fields[randomStartX+(2*steps-1)*dir[0]][randomStartY+(2*steps-1)*dir[1]] // space between current and target square belongs to enemy
                        ){
                            steps++; // 2er schritte
                        }
                        if (steps > 1){
                            if (steps-1>1){
                                randomSteps =1+ rand.nextInt(steps-2);
                            } else {
                                randomSteps = steps-1;
                            }
                            return new Move(randomStartX,randomStartY,
                                    randomStartX+randomSteps*2*dir[0],randomStartY+randomSteps*2*dir[1],
                                    randomSteps, dir[0], dir[1],-1);
                        }


                        randomStartDir++;
                        if (randomStartDir == 4) {
                            randomStartDir = 0;
                        }
                    }

                }


                randomStartX++;
                if (randomStartX == xDim()){
                    randomStartX = 0;
                    randomStartY++;
                }
                if (randomStartY == yDim()){
                    randomStartY = 0;
                }

            }
        }
        return null;
    }

    public int[] numberOfMovablePieces(int player) { // dynamisch - updaten NACH EINZELNEN SPIELSCHRITTEN
        int[] counters = {0,0};//{#movables for player,movables for -player}
        for(int i = 0; i < xDim(); i++) {
            for (int j = 0; j < yDim(); j++) {
                if(fields[i][j]!=EMPTY) {
                    for (int[] dir: Move.directions) {
                        if(validCoordinates(i+2*dir[0],j+2*dir[1]) //Landing square still on Board ?
                                && EMPTY== fields[i+2*dir[0]][j+2*dir[1]] //Landing square empty ?
                                && EMPTY!= fields[i+dir[0]][j+dir[1]] // space between current and target square belongs to enemy
                        ){
                            if (player== fields[i][j]){
                                counters[0] = counters[0] +1;
                            } else {
                                counters[1] = counters[1]+1;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return counters;
    }

    //abhängig von array zu x y ausrichtung
    public void makeMove(Move m) {
        //improvements: check if start and destination valid positions, check if fields to jump over are alternatingly taken by enemy then free
        //make Moves reversible to enable update based moves
        fields[m.startX][m.startY] = 0;
        for (int i = 1; i <= m.jumps; i++) {
            fields[m.startX+(2*i-1)*m.Xdirection][m.startY+(2*i-1)*m.Ydirection] = 0; // remove jumped over pieces
            if(sideToMove == BLACK){
                whitePiecesCounter--;
            } else {
                blackPiecesCounter--;
            }
        }
        fields[m.targetX][m.targetY] = sideToMove;

        nextPlayer();
    }

    public void undoMove(Move m) { //similar to makeMove
        previousPlayer();
        fields[m.startX][m.startY] = sideToMove;
        for (int i = 1; i <= m.jumps; i++) {
            fields[m.startX+(2*i-1)*m.Xdirection][m.startY+(2*i-1)*m.Ydirection] = -sideToMove;
            if(sideToMove == BLACK){
                whitePiecesCounter++;
            } else {
                blackPiecesCounter++;
            }
        }
        fields[m.targetX][m.targetY] = 0;
    }

    String statsAfterMove(){
        return ((halfmoveClock-1)
                + "," + blackPiecesCounter + ",-1,-1"
                + "," + whitePiecesCounter + ",-1,-1");
    }

    public String boardToString() {
        StringBuilder visualisierung = new StringBuilder();
        for(int i = 0; i < yDim(); i++) {
            for  (int j = 0; j < xDim(); j++){
                if (fields[j][i] == -1){
                    visualisierung.append(" @");
                } else if (fields[j][i] == 0) {
                    visualisierung.append(" .");
                } else if (fields[j][i] == 1) {
                    visualisierung.append(" O");
                }
            }
            visualisierung = new StringBuilder(visualisierung.toString().concat("\n"));
        }
        return visualisierung.toString();
    }
    public String validMovesToString(int player) {
        StringBuilder movesString = new StringBuilder();
        for (Move m: getValidMoves(player)) {
            movesString.append(m.toString()).append("\n");
        }
        return movesString.toString();
    }

    public static void main(String[] args) {
    }


}

