import java.util.List;

public class AlphaBeta {

    int myColor;
    Board simulation;


    int visitedNodes;
    int cutoffCounter;
    int foundBetterMove;
    long playedTime;

    public AlphaBeta(MiniKonane newGame,int color){
        myColor = color;
        simulation = new Board(newGame.xDim(),newGame.yDim());

        visitedNodes = 0;
        cutoffCounter = 0;
        foundBetterMove = 0;

        playedTime = 0;
    }

    void removeStarter(int x, int y){
        simulation.removeStarter(x,y);
    }
    void enemyMadeMove(Move m){
        simulation.makeMove(m);
    }

    void executeOwnMove(Move m){
        simulation.makeMove(m);
    }

    public Move calculateMove(int depth){
        int oldCutoffCounter = cutoffCounter;
        int oldVisitedNodes = visitedNodes;
        int oldFoundBetterMove = foundBetterMove;

        long start = System.nanoTime();


        List<Move> movesList = simulation.getValidMoves(myColor);
        if (movesList.isEmpty()){            //clock without -1 because makeMove not called
            System.out.println( 0 + ",AB," + simulation.getHalfmoveClock() + ",,,,,,"
                    + "," + (myColor==Board.BLACK?"BLACK":"WHITE") + "," + 0 + "," + 0  + "," + visitedNodes
                    + "," + 0 + "," + cutoffCounter + "," + 0 + "," + 0
                    + ",,,,," + foundBetterMove
                    + "," + playedTime);
            return null;
        }

        int worstScore = 1000000001;
        int bestScore = -1000000001;
        Move bestMove = movesList.get(0);
        int newScore;
        int bestMoveIndex=0;
        int currentIndex=0;
        for(Move moveCandidate: movesList){
            simulation.makeMove(moveCandidate);
            newScore = -alphaBeta(-myColor,-1000000000,-bestScore, depth-1);
            simulation.undoMove(moveCandidate);
            if(newScore>bestScore){
                foundBetterMove++;
                bestScore = newScore;
                bestMove = moveCandidate;
                bestMoveIndex = currentIndex;
            }
            if (worstScore > newScore) {
                worstScore = newScore;
            }
            currentIndex++;
        }

        simulation.makeMove(bestMove);

        long finish = System.nanoTime();
        long timeElapsed = finish - start;

        playedTime += timeElapsed/1000;


        //match,runtimemicro,algo,turn,blackpiece,blackmovable,blackmove,whitepiece,whitemovable,whitemove,
        // color,branchingfactor,visited,visitedsum,
        // betacutoff,betacutoffSum,windowcutoff,windowcutsum,
        // bestmoveindex,worstscore,bestscore,bettermoves,bettermovessum,
        // runtimesum

        System.out.println( timeElapsed/1000 + ",AB," + (simulation.getHalfmoveClock()-1) + ",,,,,,"  //clock -1 because makeMove incremented clock
                + "," + (myColor==Board.BLACK?"BLACK":"WHITE") + "," + movesList.size() + "," + (visitedNodes - oldVisitedNodes)  + "," + visitedNodes
                + "," + (cutoffCounter - oldCutoffCounter) + "," + cutoffCounter + ",0,0"
                + "," + bestMoveIndex + "," + worstScore + "," + bestScore + "," + (foundBetterMove - oldFoundBetterMove) + "," + foundBetterMove
                + "," + playedTime);

        simulation.undoMove(bestMove);

        return bestMove;
    }

    Move selectMove(int depthOrBudget){
        Move m = calculateMove(depthOrBudget);
        if(m!=null){
            simulation.makeMove(m);
        }
        return m;
    }

    int evaluation_a(){
        return simulation.getPiecesCounter(myColor);
    }
    int evaluation_b(){
        return simulation.getPiecesCounter(-myColor);
    }
    int evaluation_c(){
        return evaluation_a() - evaluation_b();
    }
    int evaluation_d(){
        return evaluation_a() - (evaluation_b()*3);
    }
    int evaluation_e(){
        return evaluation_a() / evaluation_b();
    }
    int evaluation_f(){
        return evaluation_a() / (evaluation_b()*3);
    }

    int evaluation_g(){
        return simulation.getValidMoves(myColor).size();
    }
    int evaluation_h(){
        return simulation.getValidMoves(-myColor).size();
    }
    int evaluation_i(){
        return evaluation_g() - evaluation_h();
    }
    int evaluation_j(){
        return evaluation_g() - (evaluation_h()*3);
    }
    int evaluation_k(){
        return evaluation_g() / evaluation_h();
    }
    int evaluation_l(){
        return evaluation_g() / (evaluation_h()*3);
    }

    int heuristic_top3(int evaluatingPlayer){//mal hundert damit man nicht double benutzen muss
        int[] movableCounters = simulation.numberOfMovablePieces(myColor);
        int numberSimulatorMovables = movableCounters[0];
        int numberEnemyMovables = movableCounters[1];

        if (numberEnemyMovables == 0) {
            return evaluatingPlayer*myColor*numberSimulatorMovables*1000;
        }

        return (evaluatingPlayer*myColor* numberSimulatorMovables*1000)/numberEnemyMovables;//(3*numberEnemyMovables);
    }


    public int alphaBeta(int player, int alpha, int beta, int depthleft ) {
        visitedNodes++;
        int score;
        if( depthleft <= 0 ) {
            return heuristic_top3(player);
        }
        List<Move> moves = simulation.getValidMoves(player);
        if(moves.isEmpty()) {
            return depthleft*-1000000;
        }
        for (Move m : moves) {
            simulation.makeMove(m);
            score = -alphaBeta(-player, -beta, -alpha, depthleft - 1 );
            simulation.undoMove(m);
            if( score >= beta ) {
                foundBetterMove++;
                cutoffCounter++;
                return beta;
            }
            if( score > alpha ){
                foundBetterMove++;
                alpha = score;
            }
        }
        return alpha;
    }

    public static void main(String[] args){
    }
}

