import java.util.*;

public class DBoard {
    static final int BLACK = -1; //@,left,first player
    static final int EMPTY = 0;
    static final int WHITE = 1;

    private final int xSize;
    private final int ySize;
    private final Field[][] fields;

    private int halfmoveClock; // plyClock
    private int sideToMove;


    int blackPiecesCounter;
    int whitePiecesCounter;

    int blackMovableCounter;
    int whiteMovableCounter;

    int blackMovesCounter;
    int whiteMovesCounter;
    private Field whiteMoveListFirstMove;
    private Field blackMoveListFirstMove;



    Random rand;

    private class Field {
        int value;
        int color;
        int xCoord;
        int yCoord;

        int[] reach;// Nord Ost Süd West wie Move.dirs


        Field previous;
        Field next;

        boolean noReachAtAll() {
            return (reach[0] == 0 && reach[1] == 0 && reach[2] == 0 && reach[3] == 0);
        }

        //int zobristNum;

        Field(int stone, int x, int y) {

            value = stone;
            color = stone;
            xCoord = x;
            yCoord = y;

            reach = new int[4];//default value 0

            previous = null;
            next = null;
        }

        Field(Field toCopy){

            value = toCopy.value;
            color = toCopy.color;
            xCoord = toCopy.xCoord;
            yCoord = toCopy.yCoord;

            reach = new int[]{toCopy.reach[0], toCopy.reach[1], toCopy.reach[2], toCopy.reach[3]};//default value 0

            previous = null;    // these are set when added to list
            next = null;
        }
    }


    DBoard(int xDim, int yDim) {
        rand = new Random();

        xSize = xDim;
        ySize = yDim;
        fields = new Field[xSize][ySize];

        halfmoveClock = 1;
        sideToMove = BLACK;

        int boardSize = xSize * ySize;
        //Top left is black
        blackPiecesCounter = boardSize/2;
        if(boardSize%2!=0) blackPiecesCounter++;
        whitePiecesCounter = boardSize/2;

        blackMovableCounter = 0;
        whiteMovableCounter = 0;

        blackMovesCounter = 0;
        whiteMovesCounter = 0;

        // x columns with y entries each
        int flip;//= 0;
        for(int i = 0; i < xSize; i++) {
            flip = i%2==0?BLACK:WHITE; //color of first stone in row alternating
            for (int j = 0; j < ySize; j++) {
                fields[i][j] = new Field(flip,i,j);
                flip *= -1;
            }
        }

    }

    DBoard(DBoard toCopy) {
        rand = toCopy.rand;


        xSize = toCopy.xSize;
        ySize = toCopy.ySize;
        fields = new Field[xSize][ySize];

        halfmoveClock = toCopy.halfmoveClock;
        sideToMove = toCopy.sideToMove;


        blackPiecesCounter = toCopy.blackPiecesCounter;
        whitePiecesCounter = toCopy.whitePiecesCounter;

        blackMovesCounter = toCopy.blackMovesCounter;
        whiteMovesCounter = toCopy.whiteMovesCounter;

        // x columns of y entires each
        for(int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                fields[i][j] = new Field(toCopy.fields[i][j]);
            }
        }


        Field copyCoords = toCopy.blackMoveListFirstMove;
        while (null != copyCoords){
            addThisFieldToList(fields[copyCoords.xCoord][copyCoords.yCoord]);
            copyCoords = copyCoords.next;
        }
        copyCoords = toCopy.whiteMoveListFirstMove;
        while (null != copyCoords){
            addThisFieldToList(fields[copyCoords.xCoord][copyCoords.yCoord]);
            copyCoords = copyCoords.next;
        }
    }

    int getSideToMove(){
        return sideToMove;
    }

    boolean gameOver(){
        if(sideToMove == BLACK){
            return noMovesLeft(BLACK);
        }else{
            return noMovesLeft(WHITE);
        }
    }

    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    int getPiecesCounter(int color){
        if(color == BLACK) return blackPiecesCounter;
        return whitePiecesCounter;
    }

    int getMovableCounter(int color){
        if(color == BLACK) return blackMovableCounter;
        return whiteMovableCounter;
    }

    int getMovesCounter(int color){
        if(color == BLACK) return blackMovesCounter;
        return whiteMovesCounter;
    }


        // value var is set to 0 before call thus value is always null and conditions skipped
    void removeFieldFromList(Field thisField){
        if(thisField.next==null && thisField.previous==null && thisField != blackMoveListFirstMove && thisField != whiteMoveListFirstMove){ // not necessary updateReach checks the same
            throw new RuntimeException("trying to remove field that is not in list");
        }

        if(null==thisField.previous){
            if(BLACK==thisField.color){
                blackMoveListFirstMove = thisField.next;
            } else {
                whiteMoveListFirstMove = thisField.next;
            }
        } else {
            thisField.previous.next = thisField.next;
        }

        if (thisField.next!=null){
            thisField.next.previous = thisField.previous;
        }

        if(BLACK==thisField.color){
            blackMovableCounter--;
        } else {
            whiteMovableCounter--;
        }

        thisField.previous = null;
        thisField.next = null;
    }

    void addThisFieldToList(Field thisField){
        if(BLACK==thisField.color){
            thisField.next = blackMoveListFirstMove;
            blackMoveListFirstMove = thisField;

            blackMovableCounter++;
        } else {
            thisField.next = whiteMoveListFirstMove;
            whiteMoveListFirstMove = thisField;

            whiteMovableCounter++;
        }

        if(thisField.next!=null){
            thisField.next.previous=thisField;
        }
    }

    boolean noMovesLeft(int player){
        if(player==BLACK){
            return blackMoveListFirstMove == null;
        } else {
            return whiteMoveListFirstMove == null;
        }
    }

    void updateReachInDirOffset(Field field, int xOffset, int yOffset, int xDir, int yDir){
        updateReachInDir(field.xCoord+xOffset, field.yCoord+yOffset, xDir, yDir);
    }

    void updateReachInDir(int thisX, int thisY, int xDir, int yDir){ //How often can this piece jump in dir
        Field thisField = fields[thisX][thisY];
        int value = thisField.value;
        int[] reach = thisField.reach;


        if (value==EMPTY){
            if(thisField.color == BLACK){
                blackMovesCounter -= reach[0]+reach[1]+reach[2]+reach[3];
            } else {
                whiteMovesCounter -= reach[0]+reach[1]+reach[2]+reach[3];
            }
            reach[0]=0;
            reach[1]=0;
            reach[2]=0;
            reach[3]=0;
            if(thisField.previous!=null || blackMoveListFirstMove==thisField || whiteMoveListFirstMove==thisField){
                removeFieldFromList(thisField);
            }

            return;
        }

        int steps = 1; // 2-steps
        while(validCoordinates(thisX+steps*2*xDir, thisY+steps*2*yDir) //Landing square still on Board ?
                && EMPTY==whoOwns(thisX+steps*2*xDir, thisY+steps*2*yDir) //Landing square empty ?
                && -value==whoOwns(thisX+(2*steps-1)*xDir, thisY+(2*steps-1)*yDir) // space between current and target square belongs to enemy
        ){
            steps++; // 2-steps
        }
        steps--;

        if(thisField.noReachAtAll()){
            if (steps>0){
                addThisFieldToList(thisField);
            }
            else {
                return;
            }
        }

        int reachDiff = steps;
        if(yDir==-1){
            reachDiff -= reach[0];
            reach[0] = steps;
        }
        if(xDir==1){
            reachDiff -= reach[1];
            reach[1] = steps;
        }
        if(yDir==1){
            reachDiff -= reach[2];
            reach[2] = steps;
        }
        if(xDir==-1){
            reachDiff -= reach[3];
            reach[3] = steps;
        }

        if(BLACK==thisField.color){
            blackMovesCounter += reachDiff;
        } else {
            whiteMovesCounter += reachDiff;
        }

        if(thisField.noReachAtAll()) {
                removeFieldFromList(thisField);
        }
    }

    boolean validCoordinates(int x, int y) {
        return 0 <= x && x < xSize && 0 <= y && y < ySize;
    }

    boolean validAdjacent(Field thisField, int xOffset, int yOffset) {
        return validCoordinates(thisField.xCoord + xOffset,thisField.yCoord + yOffset );
    }

    int whoOwns(int x, int y){
        return fields[x][y].value;
    }

    boolean neighborExists(Field thisField,int xOffset,int yOffset){
        return EMPTY != neighbor(thisField, xOffset,yOffset).value;
    }

    void stoneRemoved(Field eatenField, int[] dir){ //check in orthogonal line if reach change caused
        //bessere variante zum implementieren weil ketten in getrenten cases sind oder?!?
        //gegner ist hier aus perspektive vom removed piece aber in der score berechnung ist es meistens anders rum
            /*ist orthDir belegt?
                    JA
                        ist -orthDir leer?
                            JA:decrease orthDir neighbor
                        check eigene kette(aus gegnersteinen also gegner des entfernten steines ;entfernter ist -sideToMove wenn aufgerufen in loop und sideToMove wenn aufgerufen in leftInDir) zum increasen bis eigener kettenspringer ähnlich wie stoneLeftInDir
                    NEIN
                        increase nicht mehr möglich!
                        check gegner kette(aus eigenen steinen) zum decreasen*/
        int[][] orthogonalDirections = {{dir[1],dir[0]},{-dir[1],-dir[0]}};

        for (int[] orthDir: orthogonalDirections) {
            if( validCoordinates(eatenField.xCoord+orthDir[0], eatenField.yCoord+orthDir[1])
                    && EMPTY != whoOwns(eatenField.xCoord+orthDir[0],eatenField.yCoord+orthDir[1])
            ){
                if( validCoordinates(eatenField.xCoord - orthDir[0], eatenField.yCoord - orthDir[1])
                        && EMPTY == whoOwns(eatenField.xCoord - orthDir[0],eatenField.yCoord - orthDir[1])
                ){
                    //decrease diesen neighbor
                    updateReachInDirOffset(eatenField,orthDir[0],orthDir[1],-orthDir[0],-orthDir[1]);
                }
                //check eigene kette(aus gegnersteinen) zum increasen
                int steps = 1;
                while (validCoordinates(eatenField.xCoord+steps*2*orthDir[0], eatenField.yCoord+steps*2*orthDir[1])//offset beachten gegnerfarbe soll kette bilden zwischen freien feldern vom entfernten
                        && EMPTY != whoOwns(eatenField.xCoord+(2*steps-1)*orthDir[0],eatenField.yCoord+(2*steps-1)*orthDir[1])
                ){
                    if(EMPTY == whoOwns(eatenField.xCoord+(steps*2*orthDir[0]),eatenField.yCoord+steps*2*orthDir[1])){
                        steps++;
                    }else {//Kettenspringer gefunden, eigene kette aus removersteinen wurde verlängert durch stoneRemove
                        updateReachInDirOffset(eatenField,steps * 2 * orthDir[0], steps * 2 * orthDir[1], -orthDir[0], -orthDir[1]);
                        break;
                    }
                }
            }else{
                int steps = 1;
                while (validCoordinates(eatenField.xCoord+(2*steps + 1)*orthDir[0], eatenField.yCoord+(2*steps + 1)*orthDir[1])//offset beachten, gegnerfarbe lässt freie fehlder zwischen stein vom entfernten
                        && EMPTY != whoOwns(eatenField.xCoord+steps*2*orthDir[0],eatenField.yCoord+steps*2*orthDir[1])
                ){
                    if(EMPTY == whoOwns(eatenField.xCoord+(2*steps + 1)*orthDir[0], eatenField.yCoord+(2*steps + 1)*orthDir[1])){
                        steps++;
                    } else {//Kettenspringer gefunden, gegnerkette aus steinen des removten wurde verkürzt
                        updateReachInDirOffset(eatenField,(2*steps + 1)*orthDir[0],(2*steps + 1)*orthDir[1], -orthDir[0],-orthDir[1]);
                        break;
                    }
                }
            }
        }
    }

    void stoneLeftInDir(Field leftField, int[] dir){
        int[] oppositeDir = {-dir[0],-dir[1]};

        //thisField und moveDir waren beide belegt und blockierten jeden move in die richtung
        int steps = 1;
        while (validCoordinates(leftField.xCoord+steps*2*oppositeDir[0], leftField.yCoord+steps*2*oppositeDir[1])
                && EMPTY != whoOwns(leftField.xCoord+(2*steps-1)*oppositeDir[0],leftField.yCoord+(2*steps-1)*oppositeDir[1]) //vorher -sideToMove =
        ){
            if(EMPTY == whoOwns(leftField.xCoord+steps*2*oppositeDir[0],leftField.yCoord+steps*2*oppositeDir[1])){
                steps++;
            }else {//Kettenspringer gefunden
                updateReachInDirOffset(leftField, steps * 2 * oppositeDir[0], steps * 2 * oppositeDir[1], dir[0], dir[1]);
                break;
            }
        }

        stoneRemoved(leftField, dir);
    }

    void landInField(Field landField, int[] dir){
        /* ist dir belegt?            (Beachte für orthDir=dir wird automatisch -dir leer also board[field-orthDir]==EMPTY angenommen damit undoMove richtig nachahmt wenn makeMove geupdated hat )
        *       JA
        *           check (gegnerischen)increase für diesen neighbor in dir (also ob -dir leer)
        *           & check eigene blockierte kette; decrease(bei springer) für stein(kette) der über ihn gesprungen wäre
        *       NEIN
        *           decrease(bei springer) nicht mehr möglich
        *           ist -dir leer?
        *               JA: check increase kette für gegner
        *               NEIN:end*/
        int[][] orthogonalDirections = {{dir[1],dir[0]},{-dir[1],-dir[0]},dir};//nicht nur orthogonal

        for (int[] orthDir: orthogonalDirections) {//nicht nur orthogonal

            if( validCoordinates(landField.xCoord+orthDir[0], landField.yCoord+orthDir[1])
                && EMPTY != whoOwns(landField.xCoord+orthDir[0], landField.yCoord+orthDir[1])) {
                if (  orthDir==dir || (validCoordinates(landField.xCoord-orthDir[0], landField.yCoord-orthDir[1])
                                        && EMPTY == whoOwns(landField.xCoord-orthDir[0], landField.yCoord-orthDir[1])
                                        )
                ){
                        //siehe FIX ME oben
                    updateReachInDirOffset(landField,orthDir[0],orthDir[1],-orthDir[0],-orthDir[1]);
                }
                int steps = 1;
                while (validCoordinates(landField.xCoord+steps*2*orthDir[0], landField.yCoord+steps*2*orthDir[1])//offset beachten gegner soll kette bilden
                        && EMPTY != whoOwns(landField.xCoord+(2*steps-1)*orthDir[0],landField.yCoord+(2*steps-1)*orthDir[1])  //vorher -sideToMove =
                ){
                    if(EMPTY == whoOwns(landField.xCoord+(steps*2*orthDir[0]),landField.yCoord+steps*2*orthDir[1])){
                        steps++;
                    }else {//Kettenspringer gefunden
                        updateReachInDirOffset(landField,steps * 2 * orthDir[0],steps * 2 * orthDir[1],-orthDir[0],-orthDir[1]);
                        break;
                    }
                }
            } else {
                if ( orthDir==dir || (validCoordinates(landField.xCoord-orthDir[0], landField.yCoord-orthDir[1])
                                        &&  EMPTY == whoOwns(landField.xCoord-orthDir[0], landField.yCoord-orthDir[1])
                                        )
                ){
                        //siehe FIX ME oben
                    //check gegner kette(aus eigenen steinen) zum increasen

                    int steps = 1;
                    while (validCoordinates(landField.xCoord+(2*steps+1)*orthDir[0], landField.yCoord+(2*steps+1)*orthDir[1])//offset beachten, aus diesem feld kann springer springen
                            && EMPTY != whoOwns(landField.xCoord+steps*2*orthDir[0],landField.yCoord+steps*2*orthDir[1])
                    ){
                        if(EMPTY == whoOwns(landField.xCoord+(2*steps+1)*orthDir[0], landField.yCoord+(2*steps+1)*orthDir[1])){
                            steps++;
                        } else {//Kettenspringer gefunden
                            updateReachInDirOffset(landField,(2*steps+1)*orthDir[0],(2*steps+1)*orthDir[1],-orthDir[0],-orthDir[1]);
                            break;
                        }
                    }
                }
            }
        }
    }

    void previousPlayer(){
        sideToMove *= -1;
        halfmoveClock--;
    }

    void nextPlayer(){
        sideToMove *= -1;
        halfmoveClock++;
    }


    public void removeStarterStone(int x, int y){
        // position to remove not valid
        /*
        if( !validCoordinates(x,y) || (fields[x][y].value != BLACK) ||
                !( (x==0 && y==0) || (x==xSize/2-1 && y==ySize/2-1) || (x==xSize/2 && y==ySize/2)  || (x==xSize-1 && y==ySize-1) )
        ){
            throw new RuntimeException("cant remove this starter stone");
        }*/
        fields[x][y].value = EMPTY;
        //unsetBitInMap(x,y);
        blackPiecesCounter--;

        for(int[] dir: Move.directions){
            if(validCoordinates(x+2*dir[0],y+2*dir[1])){
                updateReachInDir(x+2*dir[0],y+2*dir[1],-dir[0],-dir[1]);
            }
        }
        nextPlayer();
    }

    public void removeNeighborOfStarter(int xSecond, int ySecond, int xOfRemovedStarter, int yOfRemovedStarter){
        /*if( !validCoordinates(xSecond,ySecond) || (fields[xSecond][ySecond].value != WHITE) || !validCoordinates(xOfRemovedStarter, yOfRemovedStarter) || EMPTY!=whoOwns(xOfRemovedStarter, yOfRemovedStarter)  ){
            throw new RuntimeException("cant remove this second stone");
        }*/
        fields[xSecond][ySecond].value = EMPTY;
        //unsetBitInMap(xSecond,ySecond);
        whitePiecesCounter--;
        updateReachInDir(xSecond,ySecond,1,0);//set reach to 0

        int[] neighborDir = {xSecond-xOfRemovedStarter,ySecond-yOfRemovedStarter}; //direction from first removed to second removed
        int[][] orthogonalDirs = {{neighborDir[1],neighborDir[0]},{-neighborDir[1],-neighborDir[0]}};

        updateReachInDir(xSecond+neighborDir[0],ySecond+neighborDir[1],-neighborDir[0],-neighborDir[1]);
        updateReachInDir(xSecond+2*neighborDir[0],ySecond+2*neighborDir[1],-neighborDir[0],-neighborDir[1]);

        for(int[] dir: orthogonalDirs){
            if(validCoordinates(xSecond + 2*dir[0],ySecond + 2*dir[1])){
                updateReachInDir(xSecond + 2*dir[0],ySecond + 2*dir[1],-dir[0],-dir[1]);
            }
        }

        nextPlayer();
    }



    //abhängig von array zu x y ausrichtung
    public LinkedList<Move> getValidMoves(int forPlayer) {
        Field workOn;
        LinkedList<Move> validMoves = new LinkedList<>();
        if(forPlayer==BLACK){
            workOn = blackMoveListFirstMove;
        } else {
            workOn = whiteMoveListFirstMove;
        }
        while (workOn!=null){
            for(int i =0; i<4; i++){
                if(workOn.reach[i]>0){
                    for (int j = workOn.reach[i]; j > 0; j--) {// long moves first so that ordering algorithm has to do less work
                        validMoves.add(new Move( workOn.xCoord, workOn.yCoord,
                                workOn.xCoord + 2*j*Move.directions[i][0],
                                workOn.yCoord + 2*j*Move.directions[i][1],
                                j,
                                Move.directions[i][0],
                                Move.directions[i][1],
                                -1)
                        );
                    }
                }
            }
            workOn = workOn.next;
        }
        return validMoves;
    }

    int[][] getWindowPinpointsForMove(Move mainMove, int radius){
        int smallerX;
        int biggerX;
        int smallerY;
        int biggerY;
        if (mainMove.startX < mainMove.targetX){
            smallerX = mainMove.startX;
            biggerX = mainMove.targetX;
        } else {
            smallerX = mainMove.targetX;
            biggerX = mainMove.startX;
        }
        if (mainMove.startY < mainMove.targetY){
            smallerY = mainMove.startY;
            biggerY = mainMove.targetY;
        } else {
            smallerY = mainMove.targetY;
            biggerY = mainMove.startY;
        }
        int[] smallPinpoint = {Math.max(0,smallerX-radius),Math.max(0,smallerY-radius)};
        int[] bigPinpoint = {Math.min(biggerX+radius,xSize),Math.min(biggerY+radius,ySize)};
        return new int[][]{smallPinpoint,bigPinpoint}; // in any case either smallerX == biggerX or smallerY == biggerY because moves are horizontal or vertical
    }

    public LinkedList<Move> getValidMovesInWindow(int forPlayer,int[][] pinpoints, boolean noWindow){ //argument radius wurde hier entfernt, in folge dessen auch bei getValidMovesInWindow,windowEvaluation,
        Field workOn;
        LinkedList<Move> validWindowMoves = new LinkedList<>();
        if(forPlayer==BLACK){
            workOn = blackMoveListFirstMove;
        } else {
            workOn = whiteMoveListFirstMove;
        }
        while (workOn!=null){
            if(noWindow || (pinpoints[0][0] <= workOn.xCoord && workOn.xCoord <= pinpoints[1][0]
                    && pinpoints[0][1] <= workOn.yCoord && workOn.yCoord <= pinpoints[1][1])) {
                for (int i = 0; i < 4; i++) {
                    if (workOn.reach[i] > 0) {
                        for (int j = workOn.reach[i]; j > 0; j--) {// long moves first so that ordering algorithm has to do less work
                            validWindowMoves.add(new Move(workOn.xCoord, workOn.yCoord,
                                    workOn.xCoord + 2 * j * Move.directions[i][0],
                                    workOn.yCoord + 2 * j * Move.directions[i][1],
                                    j,
                                    Move.directions[i][0],
                                    Move.directions[i][1],
                                    0)
                            );
                        }
                    }
                }
            }
            workOn = workOn.next;
        }
        return validWindowMoves;
    }

    int numberOfReaches(Field thisField){
        int number = 0;
        for (int reach: thisField.reach) {
            if (reach>0){
                number++;
            }
        }
        return number;
    }


    Field neighbor(Field thisField, int xOffset, int yOffset){
        return fields[thisField.xCoord+xOffset][thisField.yCoord+yOffset];
    }

    int impact(Field thisField){
        return 4-numberOfReaches(thisField);
    }

    int scoreStoneRemoved(Field workOn,int xDir, int yDir, boolean leftOrlanded){
        /*      a'
        *       a
        *     workOn     dir->
        *       b'
        *       b'
        */
        boolean aValid = validAdjacent(workOn, yDir, xDir);
        boolean aFilled = aValid && neighborExists(workOn, yDir, xDir);
        boolean aBarValid = validAdjacent(workOn,2 * yDir,2 * xDir);
        boolean aBarFilled = aBarValid && neighborExists(workOn, 2 * yDir, 2 * xDir);

        boolean bValid = validAdjacent(workOn, -yDir, -xDir);
        boolean bFilled = bValid && neighborExists(workOn, -yDir, -xDir);
        boolean bBarValid = validAdjacent(workOn,-2 * yDir,-2 * xDir);
        boolean bBarFilled = bBarValid && neighborExists(workOn,-2 * yDir,-2 * xDir);

        int leaverWeight = leftOrlanded?3:1;
        int nonLeaverWeight = leftOrlanded?1:3;

        int pointsForRemoved =nonLeaverWeight* -numberOfReaches(workOn) ; // wegen verlorener reach vom removten

        if(aValid && bValid && aFilled!=bFilled){
            if (aFilled){
                pointsForRemoved +=leaverWeight* impact(neighbor(workOn, yDir, xDir));
            } else {
                pointsForRemoved +=leaverWeight* impact(neighbor(workOn, -yDir, -xDir));
            }
        }

        if (aBarValid && aFilled && aBarFilled){
            pointsForRemoved +=nonLeaverWeight* impact(neighbor(workOn,2 * yDir,2 * xDir));
        }

        if (bBarValid && bFilled && bBarFilled){
            pointsForRemoved +=nonLeaverWeight* impact(neighbor(workOn,-2 * yDir,-2 * xDir));
        }

        return pointsForRemoved;
    }

    int scoreLeftInDir(Field workOn,int xDir, int yDir){
        int pointsForLeaver = scoreStoneRemoved( workOn, xDir, yDir,true); // enthält -numberOfReaches(workOn) wegen verlorener reach vom gemovten

        if(validAdjacent(workOn,-2*xDir,-2*yDir) && neighborExists(workOn,-xDir,-yDir) && neighborExists(workOn,-2*xDir,-2*yDir)){
            pointsForLeaver += impact(neighbor(workOn,-2*xDir,-2*yDir));
        }

        return pointsForLeaver;
    }

    int scoreLandInField(Field workOn,int xDir, int yDir, boolean completeReach){
        int pointsForLander = -scoreStoneRemoved( workOn, xDir, yDir,true); // -numberOfReaches(workOn) schadet nicht weil zielfeld während scoring leer ist

        //entstehende side Reach gut für lander
        if(validAdjacent(workOn,2*yDir,2*xDir) && !neighborExists(workOn,2*yDir,2*xDir) && neighborExists(workOn,yDir,xDir)){
            pointsForLander += impact(workOn)/2; // HALBIERT WEIL ER SONST ZU VIEL EINFLUSS HAT
        }
        if(validAdjacent(workOn,-2*yDir,-2*xDir) && !neighborExists(workOn,-2*yDir,-2*xDir) && neighborExists(workOn,-yDir,-xDir)){
            pointsForLander += impact(workOn)/2; // HALBIERT WEIL ER SONST ZU VIEL EINFLUSS HAT
        }

        if(validAdjacent(workOn,xDir,yDir) && neighborExists(workOn,xDir,yDir)){
            if(completeReach){
                pointsForLander -= 3* impact(neighbor(workOn,xDir,yDir));// schlecht für jumper daher -
                if (validAdjacent(workOn,2*xDir,2*yDir)){// das auskommentierte gilt automatisch sonst wäre nicht complete reach: && neighborExists(workOn,2*xDir,2*yDir)){
                    pointsForLander -= impact(neighbor(workOn,2*xDir,2*yDir));
                }
            } else {
                pointsForLander += impact(workOn)/2; // HALBIERT WEIL ER SONST ZU VIEL EINFLUSS HAT , kann restliche reach in dir nutzen
                pointsForLander -= 3* impact(neighbor(workOn,xDir,yDir));// der überespringbare mit restreach kann auch über mich springen weil ich hinter mir platz gemacht habe
            }
        }
        return pointsForLander;
    }


    public Move getRandomMove(int forPlayer){
        int randomMovableIndexInList = rand.nextInt(getMovableCounter(forPlayer));
        Field workOn;
        if(forPlayer==BLACK){
            workOn = blackMoveListFirstMove;
        } else {
            workOn = whiteMoveListFirstMove;
        }
        for (int i = 0; i < randomMovableIndexInList; i++) {
            workOn = workOn.next;
        }

        int checkFirst = rand.nextInt(4);

        for (int i = 0; i < 4; i++) {
            if (workOn.reach[checkFirst] > 0){
                break;
            }
            checkFirst++;
            if (checkFirst == 4) {
                checkFirst = 0;
            }
        }
        int randomReach;

        if (workOn.reach[checkFirst]>1){
            randomReach =1+ rand.nextInt(workOn.reach[checkFirst]-1);
        } else {
            randomReach = workOn.reach[checkFirst];
        }

        return new Move(workOn.xCoord, workOn.yCoord,
                workOn.xCoord + 2 * randomReach * Move.directions[checkFirst][0],
                workOn.yCoord + 2 * randomReach * Move.directions[checkFirst][1],
                randomReach,
                Move.directions[checkFirst][0],
                Move.directions[checkFirst][1],
                -1);
    }

    //+

    LinkedList<Move> getSimpleSortedValidMovesInWindow(int forPlayer,int[][] pinpoints, boolean windowMode){
        Field workOn;
        LinkedList<Move> validWindowMoves = new LinkedList<>();
        if(forPlayer==BLACK){
            workOn = blackMoveListFirstMove;
        } else {
            workOn = whiteMoveListFirstMove;
        }
        int effectPoints=0;
        while (workOn!=null){
            if(!windowMode || (pinpoints[0][0] <= workOn.xCoord && workOn.xCoord <= pinpoints[1][0]
                            && pinpoints[0][1] <= workOn.yCoord && workOn.yCoord <= pinpoints[1][1]
                            )
            ) {
                for (int i = 0; i < 4; i++) {
                    if (workOn.reach[i] > 0) {

                        //score funktionen enthalten auch die verlorenen moves vom entfernten stein und die gewonnenen moves vom landenden stein

                        // increase weil stoneLeftInDir ist gut für mover
                        effectPoints += scoreLeftInDir(workOn, Move.directions[i][0], Move.directions[i][0]);
                        for (int j = 1; j <= workOn.reach[i]; j++) {
                            //decrease weil stoneRemoved ist gut für removten also schlecht für mover
                            effectPoints -= scoreStoneRemoved(neighbor(workOn,(2 * j - 1) * Move.directions[i][0],(2 * j - 1) * Move.directions[i][1]), Move.directions[i][0], Move.directions[i][0],true);
                            validWindowMoves.add(new Move(workOn.xCoord, workOn.yCoord,
                                    workOn.xCoord + 2 * j * Move.directions[i][0],
                                    workOn.yCoord + 2 * j * Move.directions[i][1],
                                    j,
                                    Move.directions[i][0],
                                    Move.directions[i][1],
                                    //es fehlen die vom gemovten stein entstehenden moves
                                    effectPoints
                                            + scoreLandInField(neighbor(workOn,(2 * j) * Move.directions[i][0],(2 * j) * Move.directions[i][1]), Move.directions[i][0], Move.directions[i][1], j == workOn.reach[i])
                            ));
                        }
                    }
                }
            }
            workOn = workOn.next;
        }
        validWindowMoves.sort(Collections.reverseOrder());
        return validWindowMoves;
    }


    public void makeMove(int mover, Move m) {
        if (mover != sideToMove){
            throw new RuntimeException("player " + mover + " cant move while his enemy is sideToMove!");
        }


        int[] moveDirection = new int[]{m.Xdirection,m.Ydirection};

        fields[m.startX][m.startY].value = EMPTY;

        updateReachInDir(m.startX,m.startY,1,0); // dir egal weil setze reach auf 0 in alle dir
        fields[m.startX+m.Xdirection][m.startY+m.Ydirection].value = EMPTY;

        stoneLeftInDir(fields[m.startX][m.startY], moveDirection);
        for (int i = 1; i <= m.jumps; i++) {
            fields[m.startX+(2*i-1)*m.Xdirection][m.startY+(2*i-1)*m.Ydirection].value = EMPTY;

            updateReachInDir(m.startX+(2*i-1)*m.Xdirection,m.startY+(2*i-1)*m.Ydirection,1,0); //beliebige richtung zum reach auf 0 setzen
            stoneRemoved(fields[m.startX+(2*i-1)*m.Xdirection][m.startY+(2*i-1)*m.Ydirection],moveDirection);// remove jumped over pieces
        }

        fields[m.targetX][m.targetY].value = sideToMove;

        landInField(fields[m.targetX][m.targetY],moveDirection);

        for (int[] allDir:Move.directions) {
            updateReachInDir(m.targetX,m.targetY,allDir[0],allDir[1]);
        }

        if(sideToMove == BLACK){
            whitePiecesCounter-=m.jumps;
        } else {
            blackPiecesCounter-=m.jumps;
        }
        nextPlayer();
    }

    // value updates nicht in den reach update funktionen machen sondern in makeMove und undo move damit die reach update funktionen nicht für beide fälle jeweils implementiert werden müssen
    //special cases bei updates in move richtung und gegenrichtung beim startfeld ändern sich
    public void undoMove(int unmover, Move m) { //similar to makeMove
        //System.out.println(3);
        //checkReachAndMovableListWholeField();
        if (unmover == sideToMove){
            throw new RuntimeException("player " + sideToMove + " cant undo his enemies move!");
        }

        previousPlayer(); // undo player and clock change from at end of makeMove()
        int[] moveDirection = new int[]{m.Xdirection,m.Ydirection};

        fields[m.startX][m.startY].value = sideToMove; //oder ... = -board[m.startX][m.startY].value
        //setBitInMap(m.startX,m.startY);
        for (int i = 1; i <= m.jumps; i++) {
            fields[m.startX+(2*i-1)*m.Xdirection][m.startY+(2*i-1)*m.Ydirection].value = -sideToMove;
            //setBitInMap(m.startX+(2*i-1)*m.Xdirection,m.startY+(2*i-1)*m.Ydirection);
        }
        fields[m.targetX][m.targetY].value = EMPTY;
        //unsetBitInMap(m.targetX,m.targetY);

        for (int[] allDir:Move.directions) {
            updateReachInDir(m.startX,m.startY,allDir[0],allDir[1]);
        }
        updateReachInDir(m.targetX,m.targetY,1,0);

        stoneLeftInDir(fields[m.startX][m.startY],moveDirection);
        for (int i = 1; i <= m.jumps; i++) {
            stoneRemoved(fields[m.startX+(2*i-1)*m.Xdirection][m.startY+(2*i-1)*m.Ydirection],moveDirection);// remove jumped over pieces
            for (int[] allDir:Move.directions) {
                updateReachInDir(m.startX+(2*i-1)*m.Xdirection,m.startY+(2*i-1)*m.Ydirection,allDir[0],allDir[1]);
            }
        }
        landInField(fields[m.targetX][m.targetY],moveDirection);

        if(sideToMove == BLACK){
            whitePiecesCounter+=m.jumps;
        } else {
            blackPiecesCounter+=m.jumps;
        }

    }

    void checkReachInAllDir(Field thisField){
        for (int dirIndex = 0; dirIndex < 4; dirIndex++) {
            for (int i = 1; i <= thisField.reach[dirIndex]; i++) {
                if(fields[thisField.xCoord+2*i*Move.directions[dirIndex][0]][thisField.yCoord+2*i*Move.directions[dirIndex][1]].value!=EMPTY
                        || fields[thisField.xCoord+(2*i-1)*Move.directions[dirIndex][0]][thisField.yCoord+(2*i-1)*Move.directions[dirIndex][1]].value==EMPTY
                ){
                    System.out.println("last field \n" + boardToString());
                    throw new RuntimeException("Invalid reach x:" + thisField.xCoord + " y:" + thisField.yCoord + " clock:" + halfmoveClock + " dirIndex:" + dirIndex);
                }
            }
        }
    }

    String statsAfterMove(){
        return ((halfmoveClock-1)  //clock -1 because makeMove incremented clock
                + "," + blackPiecesCounter + "," + blackMovableCounter  + "," + blackMovesCounter
                + "," + whitePiecesCounter + "," +  whiteMovableCounter  + "," + whiteMovesCounter);
    }


    void checkReachAndMovableListWholeField(){
        int blackStones = 0;
        int whiteStones = 0;
        int blackMovable = 0;
        int whiteMovable = 0;
        int blackMovableListSize = 0;
        int whiteMovableListSize = 0;
        for (Field[] col: fields){
            for (Field square: col) {
                if(square.value == EMPTY){
                    if(!square.noReachAtAll() || square.next != null || square.previous != null){
                        throw new RuntimeException("empty fields cant have reach or next or previous");
                    }
                } else {

                    if (square.value == BLACK){
                        blackStones++;
                    } else if (square.value == WHITE){
                        whiteStones++;
                    } else {
                        throw new RuntimeException("fields must be empty ");
                    }

                    if(square.noReachAtAll()) {
                        if(square.next!=null || square.previous!=null){
                            throw new RuntimeException("non movables cant have previous or next");
                        }
                    } else {
                        if (square.value == BLACK){
                            blackMovable++;
                        } else{
                            whiteMovable++;
                        }
                        if (blackMoveListFirstMove!=square && whiteMoveListFirstMove!=square && square.previous==null && square.next == null){
                            throw new RuntimeException("movable stone must be in list");
                        }//garantiert nicht komplett dass element in liste ist
                        checkReachInAllDir(square);
                    }
                }

                if(square.previous != null){
                    if(square.previous.next != square){
                        throw new RuntimeException("double links on the left must be parallel");
                    }
                }
                if (square.next != null){
                    if (square.next.previous != square){
                        throw new RuntimeException("double links on the left must be parallel");
                    }
                }
            }
        }

        Field checkThisList = blackMoveListFirstMove;
        while(checkThisList != null){
            blackMovableListSize++;
            if(checkThisList.value != BLACK || checkThisList.noReachAtAll()){
                throw new RuntimeException("only movable black stones are allowed in this list");
            }
            checkThisList = checkThisList.next;
        }

        checkThisList = whiteMoveListFirstMove;
        while(checkThisList != null){
            whiteMovableListSize++;
            if(checkThisList.value != WHITE || checkThisList.noReachAtAll()){
                throw new RuntimeException("only movable white stones are allowed in this list");
            }
            checkThisList = checkThisList.next;
        }

        if (blackMovable != blackMovableListSize || whiteMovable != whiteMovableListSize){
            throw new RuntimeException("List does not include all movable stones of its color or counters are wrong");
        }

        if (blackStones != blackPiecesCounter || whiteStones != whitePiecesCounter){
            throw new RuntimeException("Wrong counter variable values");
        }

        if (getValidMoves(BLACK).size() != blackMovesCounter || getValidMoves(WHITE).size() != whiteMovesCounter){
            throw new RuntimeException("Move list size does not mache valid moves counter");
        }
    }


    public String boardToString() {
        StringBuilder visualisierung = new StringBuilder();
        for(int i = 0; i < ySize; i++) {
            for  (int j = 0; j < xSize; j++){
                if (fields[j][i].value == -1){
                    visualisierung.append(" @");
                } else if (fields[j][i].value == 0) {
                    visualisierung.append(" .");
                } else if (fields[j][i].value == 1) {
                    visualisierung.append(" O");
                }
            }
            visualisierung.append("\n");
        }
        return visualisierung.toString();
    }

    public String validMovesToString(int player) { // getValidWindow moves braucht eigene
        StringBuilder movesString = new StringBuilder("Moves for " + player + " in halfround " + halfmoveClock + "\n");
        for (Move m: getValidMoves(player)) {
            movesString.append(m.toString()).append("\n");
        }
        return movesString.toString();
    }
}
