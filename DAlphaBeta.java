import java.util.List;

public class DAlphaBeta {

    static final int BLACK = -1; //@,left,first player
    static final int EMPTY = 0;
    static final int WHITE = 1;

    int searcherColor;
    DBoard dBoard;

    boolean windowMode;
    boolean sortMode;


    int betaCutoffCounter;
    int windowCutoffCounter;
    int visitedNodes;
    int foundBetterMove;
    long playedTime;



    public DAlphaBeta(MiniKonane newGame, int playerColor, boolean withWindows, boolean sorted){
        searcherColor = playerColor;
        dBoard = new DBoard(newGame.xDim(), newGame.yDim());

        windowMode = withWindows;
        sortMode = sorted;

        visitedNodes=0;
        betaCutoffCounter=0;
        windowCutoffCounter=0;
        foundBetterMove=0;

        playedTime = 0;
    }

    public void enemyMadeMove(Move m){
        dBoard.makeMove(-searcherColor,m);
    }
    public void executeOwnMove(Move m){
        dBoard.makeMove(searcherColor,m);
    }

    public Move calculateMove( int depthOrBudget){
        if (dBoard.noMovesLeft(searcherColor)){
            System.out.println( "0,DAB," + dBoard.getHalfmoveClock()            //clock without -1 because makeMove not called
                    + "," + dBoard.blackPiecesCounter + "," + dBoard.blackMovableCounter  + "," + dBoard.blackMovesCounter
                    + "," + dBoard.whitePiecesCounter + "," +  dBoard.whiteMovableCounter  + "," + dBoard.whiteMovesCounter
                    + "," + (searcherColor==BLACK?"BLACK":"WHITE") + ",0,0," + visitedNodes
                    + ",0," + betaCutoffCounter + ",0," + windowCutoffCounter
                    + ",,,,0," + foundBetterMove
                    + "," + playedTime);
            return null;
        }

        int oldBetaCutoffCounter = betaCutoffCounter;
        int oldVisitedNodes = visitedNodes;
        int oldWindowCutoffCounter = windowCutoffCounter;
        int oldFoundBetterMove = foundBetterMove;

        long start = System.nanoTime();

        List<Move> movesList = dBoard.getValidMoves(searcherColor);

        int bestScore = -1000000001;
        int worstScore = 1000000001;
        Move bestMove = movesList.get(0);
        int newScore;
        int bestMoveIndex=0;
        int currentIndex=0;
        if (movesList.size()>1) {
            for(Move moveCandidate: movesList){
                dBoard.makeMove(searcherColor,moveCandidate);
                newScore = -alphaBeta(-searcherColor,-1000000000,-bestScore,depthOrBudget-1);
                dBoard.undoMove(searcherColor,moveCandidate);
                if(newScore>bestScore){
                    foundBetterMove++;
                    bestScore = newScore;
                    bestMove = moveCandidate;
                    bestMoveIndex=currentIndex;
                }
                if (worstScore > newScore) {
                    worstScore = newScore;
                }
                currentIndex++;
            }
        } else {
            bestScore = heuristic_top3(searcherColor);
            worstScore = heuristic_top3(searcherColor);
        }

        dBoard.makeMove(searcherColor,bestMove);

        long finish = System.nanoTime();
        long timeElapsed = finish - start;

        playedTime += timeElapsed/1000;


        //match,runtimemicro,algo,turn,blackpiece,blackmovable,blackmove,whitepiece,whitemovable,whitemove,
        // color,branchingfactor,visited,visitedsum,
        // betacutoff,betacutoffSum,windowcutoff,windowcutsum,
        // bestmoveindex,worstscore,bestscore,bettermoves,bettermovessum,
        // runtimesum

        System.out.println(  timeElapsed/1000 + ",DAB," + dBoard.statsAfterMove()
                + "," + (searcherColor==BLACK?"BLACK":"WHITE") + "," + movesList.size() + "," + (visitedNodes - oldVisitedNodes)  + "," + visitedNodes
                + "," + (betaCutoffCounter - oldBetaCutoffCounter) + "," + betaCutoffCounter + "," + (windowCutoffCounter - oldWindowCutoffCounter) + "," + windowCutoffCounter
                + "," + bestMoveIndex + "," + worstScore + "," + bestScore + "," + (foundBetterMove - oldFoundBetterMove) + "," + foundBetterMove
                + "," + playedTime);

        dBoard.undoMove(searcherColor,bestMove);

        return bestMove;
    }

    Move selectMove(int depthOrBudget){
        Move m = calculateMove(depthOrBudget);
        if(m!=null){
            dBoard.makeMove(searcherColor,m);
        }
        return m;
    }

    Move calculateWindowMove(int depthOrBudget,int windowRadius){
        if (dBoard.noMovesLeft(searcherColor)){
            System.out.println( "0,WDAB," + dBoard.getHalfmoveClock()            //clock without -1 because makeMove not called
                    + "," + dBoard.blackPiecesCounter + "," + dBoard.blackMovableCounter  + "," + dBoard.blackMovesCounter
                    + "," + dBoard.whitePiecesCounter + "," +  dBoard.whiteMovableCounter  + "," + dBoard.whiteMovesCounter
                    + "," + (searcherColor==BLACK?"BLACK":"WHITE") + ",0,0," + visitedNodes
                    + ",0," + betaCutoffCounter + ",0," + windowCutoffCounter
                    + ",,,,0," + foundBetterMove
                    + "," + playedTime);
            return null;
        }

        int oldBetaCutoffCounter = betaCutoffCounter;
        int oldVisitedNodes = visitedNodes;
        int oldWindowCutoffCounter = windowCutoffCounter;
        int oldFoundBetterMove = foundBetterMove;

        long start = System.nanoTime();

        List<Move> movesList = dBoard.getValidMoves(searcherColor);

        int bestScore = -1000000001;
        int worstScore = 1000000001;
        Move bestMove = movesList.get(0);
        int bestMoveIndex=0;
        int currentIndex=0;
        int newScore;
        if (movesList.size()>1) {
            for(Move moveCandidate: movesList){
                dBoard.makeMove(searcherColor,moveCandidate);
                newScore = -windowAlphaBeta(-searcherColor,-1000000000,-bestScore,depthOrBudget-1,dBoard.getWindowPinpointsForMove(moveCandidate,windowRadius));
                dBoard.undoMove(searcherColor,moveCandidate);
                if(newScore>bestScore){
                    foundBetterMove++;
                    bestScore = newScore;
                    bestMove = moveCandidate;
                    bestMoveIndex=currentIndex;
                }
                if (worstScore > newScore) {
                    worstScore = newScore;
                }
                currentIndex++;
            }
        } else {
            bestScore = heuristic_top3(searcherColor);
            worstScore = heuristic_top3(searcherColor);
        }

        dBoard.makeMove(searcherColor,bestMove);
        long finish = System.nanoTime();
        long timeElapsed = finish - start;

        playedTime += timeElapsed/1000;


        //match,runtimemicro,algo,turn,blackpiece,blackmovable,blackmove,whitepiece,whitemovable,whitemove,
        // color,branchingfactor,visited,visitedsum,
        // betacutoff,betacutoffSum,windowcutoff,windowcutsum,
        // bestmoveindex,worstscore,bestscore,bettermoves,bettermovessum,
        // runtimesum

        System.out.println(  timeElapsed/1000 + ",WDAB," + dBoard.statsAfterMove()
                + "," + (searcherColor==BLACK?"BLACK":"WHITE") + "," + movesList.size() + "," + (visitedNodes - oldVisitedNodes)  + "," + visitedNodes
                + "," + (betaCutoffCounter - oldBetaCutoffCounter) + "," + betaCutoffCounter + "," + (windowCutoffCounter - oldWindowCutoffCounter) + "," + windowCutoffCounter
                + "," + bestMoveIndex + "," + worstScore + "," + bestScore + "," + (foundBetterMove - oldFoundBetterMove) + "," + foundBetterMove
                + "," + playedTime);

        dBoard.undoMove(searcherColor,bestMove);

        return bestMove;
    }

    Move selectWindowMove(int depthOrBudget,int windowRadius){
        Move m = calculateWindowMove(depthOrBudget,windowRadius);
        if(m!=null){
            dBoard.makeMove(searcherColor,m);
        }
        return m;
    }

    Move combineCalculateWindowMove(int depthOrBudget, int windowRadius){
        if (dBoard.noMovesLeft(searcherColor)){
            System.out.println( "0," + (windowMode?"WDABS,":"DABS,") + dBoard.getHalfmoveClock()            //clock without -1 because makeMove not called
                    + "," + dBoard.blackPiecesCounter + "," + dBoard.blackMovableCounter  + "," + dBoard.blackMovesCounter
                    + "," + dBoard.whitePiecesCounter + "," +  dBoard.whiteMovableCounter  + "," + dBoard.whiteMovesCounter
                    + "," + (searcherColor==BLACK?"BLACK":"WHITE") + ",0,0," + visitedNodes
                    + ",0," + betaCutoffCounter + ",0," + windowCutoffCounter
                    + ",,,,0," + foundBetterMove
                    + "," + playedTime);
            return null;
        }

        int oldBetaCutoffCounter = betaCutoffCounter;
        int oldVisitedNodes = visitedNodes;
        int oldWindowCutoffCounter = windowCutoffCounter;
        int oldFoundBetterMove = foundBetterMove;

        long start = System.nanoTime();

        List<Move> movesList = dBoard.getSimpleSortedValidMovesInWindow(searcherColor,null,false);

        int bestScore = -1000000001;
        int worstScore = 1000000001;
        Move bestMove = movesList.get(0);
        int bestMoveIndex=0;
        int currentIndex=0;
        int newScore;
        if (movesList.size()>1) {
            for(Move moveCandidate: movesList){
                dBoard.makeMove(searcherColor,moveCandidate);
                newScore = -combineAlphaBeta(-searcherColor,-1000000000,-bestScore,depthOrBudget-1,dBoard.getWindowPinpointsForMove(moveCandidate,windowRadius));
                //System.out.print(" ,"  + newScore + "," + moveCandidate.orderScore );
                dBoard.undoMove(searcherColor,moveCandidate);
                if(newScore>bestScore){
                    foundBetterMove++;
                    bestScore = newScore;
                    bestMove = moveCandidate;
                    bestMoveIndex=currentIndex;
                }if (worstScore > newScore) {
                    worstScore = newScore;
                }
                currentIndex++;
            }
        } else {
            bestScore = heuristic_top3(searcherColor);
            worstScore = heuristic_top3(searcherColor);
        }

        dBoard.makeMove(searcherColor,bestMove);


        long finish = System.nanoTime();
        long timeElapsed = finish - start;

        playedTime += timeElapsed/1000;

        //match,runtimemicro,algo,turn,blackpiece,blackmovable,blackmove,whitepiece,whitemovable,whitemove,
        // color,branchingfactor,visited,visitedsum,
        // betacutoff,betacutoffSum,windowcutoff,windowcutsum,
        // bestmoveindex,worstscore,bestscore,bettermoves,bettermovessum,
        // runtimesum

        System.out.println(  timeElapsed/1000 + (windowMode?",WDABS,":",DABS,") + dBoard.statsAfterMove()
                + "," + (searcherColor==BLACK?"BLACK":"WHITE") + "," + movesList.size() + "," + (visitedNodes - oldVisitedNodes)  + "," + visitedNodes
                + "," + (betaCutoffCounter - oldBetaCutoffCounter) + "," + betaCutoffCounter + "," + (windowCutoffCounter - oldWindowCutoffCounter) + "," + windowCutoffCounter
                + "," + bestMoveIndex + "," + worstScore + "," + bestScore + "," + (foundBetterMove - oldFoundBetterMove) + "," + foundBetterMove
                + "," + playedTime);

        dBoard.undoMove(searcherColor,bestMove);

        return bestMove;
    }

    Move combineSelectWindowMove(int depthOrBudget, int windowRadius){
        Move m = combineCalculateWindowMove(depthOrBudget,windowRadius);
        if(m!=null){
            dBoard.makeMove(searcherColor,m);
        }
        return m;
    }

    int evaluation_a(int evaluatingPlayer){
        return dBoard.getPiecesCounter(evaluatingPlayer);
    }
    int evaluation_b(int evaluatingPlayer){
        return dBoard.getPiecesCounter(-evaluatingPlayer);
    }
    int evaluation_c(int evaluatingPlayer){
        return evaluation_a(evaluatingPlayer) - evaluation_b(evaluatingPlayer);
    }
    int evaluation_d(int evaluatingPlayer){
        return evaluation_a(evaluatingPlayer) - (evaluation_b(evaluatingPlayer)*3);
    }
    int evaluation_e(int evaluatingPlayer){
        return evaluation_a(evaluatingPlayer) / evaluation_b(evaluatingPlayer);
    }
    int evaluation_f(int evaluatingPlayer){
        return evaluation_a(evaluatingPlayer) / (evaluation_b(evaluatingPlayer)*3);
    }
    int evaluation_g(int evaluatingPlayer){
        return dBoard.getMovesCounter(evaluatingPlayer);
    }
    int evaluation_h(int evaluatingPlayer){
        return dBoard.getMovesCounter(-evaluatingPlayer);
    }

    int evaluation_i(int evaluatingPlayer){
        return evaluation_g(evaluatingPlayer) - evaluation_h(evaluatingPlayer);
    }
    int evaluation_j(int evaluatingPlayer){
        return evaluation_g(evaluatingPlayer) - (evaluation_h(evaluatingPlayer)*3);
    }
    int evaluation_k(int evaluatingPlayer){
        return evaluation_g(evaluatingPlayer) / evaluation_h(evaluatingPlayer);
    }
    int evaluation_l(int evaluatingPlayer){
        return evaluation_g(evaluatingPlayer) / (evaluation_h(evaluatingPlayer)*3);
    }
    int evaluation_m(int evaluatingPlayer){
        return dBoard.getMovableCounter(evaluatingPlayer);
    }
    int evaluation_n(int evaluatingPlayer){
        return dBoard.getMovableCounter(-evaluatingPlayer);
    }

    int evaluation_o(int evaluatingPlayer){
        return evaluation_m(evaluatingPlayer) - evaluation_n(evaluatingPlayer);
    }
    int evaluation_p(int evaluatingPlayer){
        return evaluation_m(evaluatingPlayer) - (evaluation_n(evaluatingPlayer)*3);
    }
    int evaluation_q(int evaluatingPlayer){
        return evaluation_m(evaluatingPlayer) / evaluation_n(evaluatingPlayer);
    }
    int evaluation_r(int evaluatingPlayer){
        return evaluation_m(evaluatingPlayer) / (evaluation_n(evaluatingPlayer)*3);
    }

    int heuristic_top3(int evaluatingPlayer){//mal hundert damit man nicht double benutzen muss
        int numberSearcherMovables = evaluation_m(searcherColor);
        int numberEnemyMovables = evaluation_n(searcherColor);

        if (numberEnemyMovables == 0) {
            return evaluatingPlayer*searcherColor*dBoard.getMovableCounter(searcherColor) * 1000; // Hier moves nicht movables
        }
        return (evaluatingPlayer*searcherColor* numberSearcherMovables*1000 ) / numberEnemyMovables; //(numberEnemyMovables*3);
    }


    public int alphaBeta( int player, int alpha, int beta, int depthleft ) {
        visitedNodes++;
        int score;// !!overflow!! -Integer.MIN_VALUE==Integer.MIN_VALUE
        if(dBoard.noMovesLeft(player)){
            return depthleft*-1000000;//early wins better
        }else if( depthleft <= 0 ) {
            return heuristic_top3(player);
        }
        List<Move> moves = dBoard.getValidMoves(player);
        for (Move m : moves) {
            dBoard.makeMove(player, m);
            score = -alphaBeta(-player, -beta, -alpha, depthleft-1);
            dBoard.undoMove(player,m);
            if( score >= beta ) {
                foundBetterMove++;
                betaCutoffCounter++;
                return beta;
            }
            if( score > alpha ) {
                foundBetterMove++;
                alpha = score;
            }
        }
        return alpha;
    }

    public int windowAlphaBeta( int player, int alpha, int beta, int depthleft, int[][] pinpoints) {//FIXME UNSORTED
        visitedNodes++;
        int score;// !!overflow!! -Integer.MIN_VALUE==Integer.MIN_VALUE
        if (dBoard.noMovesLeft(player)){
            return depthleft*-1000000;//early wins better
        } else if (depthleft <= 0) {
            return heuristic_top3(player);//quiesce( alpha, beta );
        }

        List<Move> windowMoves = dBoard.getValidMovesInWindow(player,pinpoints,!windowMode);//FIXME UNSORTED
        windowCutoffCounter += dBoard.getMovesCounter(player) - windowMoves.size();
        if(windowMoves.isEmpty()) {
            return heuristic_top3(player);
        }

        for (Move m : windowMoves) {
            dBoard.makeMove(player,m);
            score = -windowAlphaBeta(-player,-beta,-alpha, depthleft-1,pinpoints);
            dBoard.undoMove(player,m);
            if( score >= beta ) {
                foundBetterMove++;
                betaCutoffCounter++;
                return beta;
            }
            if( score > alpha ){
                foundBetterMove++;
                alpha = score;
            }
        }
        return alpha;
    }

    public int combineAlphaBeta( int player, int alpha, int beta, int depthleft, int[][] pinpoints) {
        visitedNodes++;
        int score;// !!overflow!! -Integer.MIN_VALUE==Integer.MIN_VALUE
        List<Move> moves;
        if (dBoard.noMovesLeft(player)){
            return depthleft*-1000000;//early wins
        } else if (depthleft <= 0) {
            return heuristic_top3(player);
        } else if (windowMode){
            moves = dBoard.getSimpleSortedValidMovesInWindow(player,pinpoints,windowMode);
            windowCutoffCounter += dBoard.getMovesCounter(player) - moves.size();
            if(moves.isEmpty()) {
                return heuristic_top3(player);
            }
        } else {
            moves = dBoard.getSimpleSortedValidMovesInWindow(player,null,false);
        }

        for (Move m : moves) {
            dBoard.makeMove(player,m);
            score = -combineAlphaBeta(-player,-beta,-alpha, depthleft-1,pinpoints);

            dBoard.undoMove(player,m);
            if( score >= beta ) {
                foundBetterMove++;
                betaCutoffCounter++;
                return beta;
            }
            if( score > alpha ){
                foundBetterMove++;
                alpha = score;
            }
        }
        return alpha;
    }

    public static void main(String[] args) {
    }
}

