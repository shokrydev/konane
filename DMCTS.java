import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class DMCTS {

    int myColor;
    DBoard dBoard;

    TreeNode root;
    int allSimulations; //wichtig für ucb;
    int allRollouts;
    long playedTime;

    int inheritedTreeSize;

    Random rand;


    double ucbFactor;
    boolean movablesScoreMode; //0 oder 1 bzw enemyMovables oder ownMovables bzw heuristik
    boolean sortedRollout;
    int rolloutDepth; // 1000 ist code für full rollout
    int rolloutsPerSimulation;

    DMCTS(MiniKonane newGame, int color, double ucbF, boolean movableScoreEvaluationMode, boolean sortMode, int rollDepth, int rollPerSim){
        myColor = color;
        dBoard = new DBoard(newGame.xDim(),newGame.yDim());

        allSimulations = 0; // wichtig für ucb
        allRollouts = 0;

        playedTime = 0;

        inheritedTreeSize = 0;

        rand = new Random();

        if (ucbF <= 0 ){
            ucbFactor = 1.414213562;
        } else {
            ucbFactor = ucbF;
        }
        movablesScoreMode = movableScoreEvaluationMode;
        sortedRollout = sortMode;
        rolloutDepth = rollDepth;
        rolloutsPerSimulation = rollPerSim;
    }

    private class TreeNode{
        LinkedList<Move> unexploredMoves;
        TreeNode parent;
        Move parentMove;
        ArrayList<TreeNode> children;
        int visitCounter;
        double scoreForMCTS; //vorher scoreForParentMove
        int sideToMove;

        TreeNode(){
            unexploredMoves = dBoard.getValidMoves(myColor);
            children = new ArrayList<>();
            visitCounter = 0;
            scoreForMCTS = 0;
            sideToMove = myColor;
        }

        TreeNode(DBoard dBoardCopy,TreeNode expander, Move expansionMove){
            unexploredMoves = dBoardCopy.getValidMoves(-expander.sideToMove);
            parent = expander;
            parentMove = expansionMove;
            children = new ArrayList<>();
            visitCounter = 0;
            scoreForMCTS = 0;
            sideToMove = -expander.sideToMove;
        }


        double ucb1(){ // call function
            return (scoreForMCTS/visitCounter)+ucbFactor *Math.sqrt(Math.log(parent.visitCounter)/visitCounter); //NaN wenn allsimulations 0 ist  //logn(MUSS parents.visits nicht root.viits!!!!)
        }


        double myUct(int selectingPlayer){ // call function
            double firstComponent = scoreForMCTS/visitCounter;
            double secondComponent = 2*Math.sqrt(2*Math.log(parent.visitCounter)/visitCounter);// log(root.visits) war falsch!!

            if(selectingPlayer == myColor){
                return firstComponent + ucbFactor * secondComponent;
            }

            return -firstComponent + ucbFactor * secondComponent; //NaN wenn allsimulations 0 ist
        }

    }

    void enemyMadeMove(Move m){
        dBoard.makeMove(-myColor,m);

        if(null != root){
            TreeNode oldRoot = root;
            for (TreeNode child: oldRoot.children) {
                if (child.parentMove.sameMove(m)){
                    root = child;
                    break;
                }
            }
            if (root==oldRoot){
                root = null;
            }
        }
    }

    Move selectMove(long resources){
        int roundSimulations = 0;
        int roundRollouts = 0;

        long startTime = System.nanoTime();


        if(null == root){
            //System.out.println("neuer Tree");
            root = new TreeNode();
        }

        inheritedTreeSize = root.visitCounter;

        if (root.unexploredMoves.isEmpty() & root.children.isEmpty()){

            //match,runtimemicro,algo,turn,blackpiece,blackmovable,blackmove,whitepiece,whitemovable,whitemove,
            // color,branchingfactor,visited,visitedsum,
            // betacutoff,betacutoffSum,windowcutoff,windowcutsum,
            // bestmoveindex,worstscore,bestscore,bettermoves,bettermovessum,
            // runtimesum
            System.out.println(  "0,DMCTS," + dBoard.getHalfmoveClock()
                    + "," + dBoard.blackPiecesCounter + "," + dBoard.blackMovableCounter  + "," + dBoard.blackMovesCounter
                    + "," + dBoard.whitePiecesCounter + "," +  dBoard.whiteMovableCounter  + "," + dBoard.whiteMovesCounter
                    + "," + (myColor==DBoard.BLACK?"BLACK":"WHITE") + ",0,0," + allSimulations
                    + ",1,,1,"
                    + ",,,,0,"
                    + "," + playedTime);

            //statt newBetaCutoffs      inheritedTreeSize
            //statt newwindowCutoff        root.visitcounter -> finalTreeSize
            //statt bestmoveindex       bestRootchild.visitCounter
            //statt worstscore          lowestRatio
            //statt bestScore           highestRatio
            //statt newbettermove       root ratio -> average child ratio

            return null;
        }


        TreeNode selectedNode;
        TreeNode expandedNode;
        TreeNode bestRootChild = null;
        double bestRatio = -Double.MAX_VALUE; // nicht Double.MIN_VALUE weil es ist der kleinste positive wert
        double lowestRatio = Double.MAX_VALUE;

        double mySimulationScore;


        DBoard dBoardCopy;

        int selectedDepth;

        do{
            dBoardCopy = new DBoard(dBoard);
            selectedNode = select(dBoardCopy);


            if(selectedNode.unexploredMoves.isEmpty() & selectedNode.children.isEmpty()){
                if(!dBoardCopy.noMovesLeft(selectedNode.sideToMove)){
                    throw new RuntimeException("STOP why does non terminal node have neithe unexplored nor explored children");
                }

                selectedDepth = dBoardCopy.getHalfmoveClock() - dBoard.getHalfmoveClock();
                /*je tiefer der node um so unwahrscheinlicher wird er erreicht und um so unwichtiger ist der terminal node
                        wir beachten diese sonderfälle weil sie im konteext des spielens gegen einen minimaxplayer der gute züge wählt optimierungen sind, minimax hat diese art von optimierungen nicht nötig weil er ohnehin
                        entsprechend des worstcases moves wählt */
                if( selectedDepth == 1) {
                    selectedNode.scoreForMCTS = Double.MAX_VALUE;
                    selectedNode.visitCounter = Integer.MAX_VALUE/2;
                    break;
                } else if (selectedDepth == 2) {
                    selectedNode.visitCounter = Integer.MAX_VALUE/2;
                    selectedNode.parent.visitCounter = Integer.MAX_VALUE/2;
                    continue;
                } else if (selectedDepth%2 == 1) {
                    mySimulationScore = 100.0 * rolloutsPerSimulation / selectedDepth;
                    if(movablesScoreMode){
                        mySimulationScore *= dBoardCopy.getMovesCounter(myColor);//was ...(selectedNode.parent.sideToMove)
                    }
                } else{// hier gilt selectedDepth%2 != 0 und selectedNode.sideToMove == myColor
                    mySimulationScore = 0;
                }
                //backpropagate(selectedNode, mySimulationScore);
                TreeNode currentNode = selectedNode;
                while(null != currentNode){
                    currentNode.visitCounter += 100 * rolloutsPerSimulation / selectedDepth;
                    currentNode.scoreForMCTS += mySimulationScore;
                    currentNode = currentNode.parent;
                }
                continue;
            }
            expandedNode = expand(selectedNode, dBoardCopy);
            mySimulationScore = simulate(dBoardCopy);

            roundSimulations ++;                                              //wird geskipt wenn terminalnode selected
            roundRollouts += rolloutsPerSimulation;
            backpropagate(expandedNode, mySimulationScore);
        }while(System.nanoTime() < startTime + resources);

        for (TreeNode child: root.children) {
            if (bestRatio < child.scoreForMCTS/ child.visitCounter){
                bestRatio = child.scoreForMCTS/ child.visitCounter;
                bestRootChild = child;
            }
            if(child.scoreForMCTS/ child.visitCounter < lowestRatio ){
                lowestRatio = child.scoreForMCTS/ child.visitCounter;
            }
        }


        if(bestRatio == -Double.MAX_VALUE){
            throw new RuntimeException("WIESO?");
        }

        playedTime += resources/1000;
        allSimulations += roundSimulations;
        allRollouts += roundRollouts;

        dBoard.makeMove(myColor,bestRootChild.parentMove);
        //match,runtimemicro,algo,turn,blackpiece,blackmovable,blackmove,whitepiece,whitemovable,whitemove,
        // color,branchingfactor,visited,visitedsum,
        // betacutoff,betacutoffSum,windowcutoff,windowcutsum,
        // bestmoveindex,worstscore,bestscore,bettermoves,bettermovessum,
        // runtimesum
        System.out.println(  resources/1000 + ",DMCTS," + dBoard.statsAfterMove()
                + "," + (myColor==DBoard.BLACK?"BLACK":"WHITE") + "," + root.children.size() + "," + roundSimulations  + "," + allSimulations
                + "," + inheritedTreeSize + ",," + root.visitCounter + ","
                + "," + bestRootChild.visitCounter + "," + lowestRatio + "," + bestRatio + "," + root.scoreForMCTS/ root.visitCounter + ","
                + "," + playedTime);

            //statt newBetaCutoffs      inheritedTreeSize
            //statt newwindowCutoff        root.visitcounter -> finalTreeSize
            //statt bestmoveindex       bestRootchild.visitCounter
            //statt worstscore          lowestRatio
            //statt bestScore           highestRatio
            //statt newbettermove       root ratio -> average child ratio


        root = bestRootChild; //WICHTIG!!!! hoher score der children ist gut für parent weil er wählt child mit dem größten score/ucb aus

        return bestRootChild.parentMove;
    }


    TreeNode select(DBoard dBoardCopy){
        TreeNode currentNode = root;
        double newUcb;
        double highestUcb;
        TreeNode highestUcbNode = root;

        while(currentNode.unexploredMoves.isEmpty() & 0 < currentNode.children.size()){
            highestUcb = -Double.MAX_VALUE; // nicht Double.MIN_VALUE weil es ist der kleinste positive wert
            for (TreeNode child: currentNode.children) {
                newUcb = child.myUct(currentNode.sideToMove);
                if(newUcb > highestUcb){
                    highestUcbNode = child;
                    highestUcb = newUcb;
                }
            }
            if(highestUcb == -Double.MAX_VALUE){
                throw new RuntimeException("WIESO?");
            }
            dBoardCopy.makeMove(currentNode.sideToMove,highestUcbNode.parentMove);
            currentNode = highestUcbNode;
        }

        return highestUcbNode;
    }


    TreeNode expand(TreeNode selectedNode, DBoard dBoardCopy){

        Move expandedMove = selectedNode.unexploredMoves.removeFirst(); // nicht wichtig -> vorsortieren/randomisieren - oder getValidMoves in TreeNode consructor vorsortieren/randomisieren


        dBoardCopy.makeMove(selectedNode.sideToMove,expandedMove);
        TreeNode expandedNewChild = new TreeNode(dBoardCopy, selectedNode, expandedMove);
        selectedNode.children.add(expandedNewChild);

        return expandedNewChild;
    }




    double simulate(DBoard dBoardCopy){
        LinkedList<Move> validMoves;
        Move moveToPerform;

        //movables before simulation
        int mineBefore =  dBoardCopy.getMovableCounter(myColor);//was (expandedNode.parent.sideToMove); // was "expanderScore"
        int enemyBefore = dBoardCopy.getMovableCounter(-myColor);//was (expandedNode.sideToMove);

        //moveables after simulation
        double mineAfter;
        double enemyAfter;


        int depthLimit = rolloutDepth + dBoardCopy.getHalfmoveClock();
        double myPoints = 0; // was "pointsInExpanded = "

        DBoard rollOutBoard;


        for (int rollCounter = 0; rollCounter < rolloutsPerSimulation; rollCounter++) {
            if(rollCounter+1 == rolloutsPerSimulation) {
                rollOutBoard = dBoardCopy;
            } else {
                rollOutBoard = new DBoard(dBoardCopy);
            }


            while(!rollOutBoard.gameOver() && rollOutBoard.getHalfmoveClock() < depthLimit){
                if (sortedRollout){
                    validMoves = rollOutBoard.getSimpleSortedValidMovesInWindow(rollOutBoard.getSideToMove(),null,false);
                    moveToPerform = validMoves.get(rand.nextInt(validMoves.size()));
                }else{
                    moveToPerform = rollOutBoard.getRandomMove(rollOutBoard.getSideToMove());
                }
                rollOutBoard.makeMove(rollOutBoard.getSideToMove(),moveToPerform); // TODO wäre vllt schneller ohne randomisierung
            }


            if(movablesScoreMode){
                mineAfter = rollOutBoard.getMovableCounter(myColor);//anstatt vorher ...(expandedNode.parent.sideToMove)
                enemyAfter = rollOutBoard.getMovableCounter(-myColor);//anstatt ...(expandedNode.sideToMove)


                //was ist mit terminal nodes wenn beide 0 moves haben und man selbst drann wäre -> dann returnt getMovesCounter 0 -> richtig
                if (enemyAfter == 0) {
                    myPoints = rollOutBoard.getMovesCounter(myColor);//was getMovesCounter(selectedNode.parent.sideToMove) // Hier moves nicht movables
                } else {
                    myPoints = (mineAfter) / (enemyAfter*3); //vorher mit gewichtung weil immer myColorr genutzt; evaluatingPlayer*searcherColor*
                }


            } else {
                if (rollOutBoard.gameOver()){
                    if(rollOutBoard.getSideToMove() == myColor){ //vorher ...== expandedNode.sideToMove; score aus perspektive vom MCTS spieler
                        myPoints += 0;//vorher pointsInExpanded += 1 //myColor verloren -> enemy gewinnt durch expanded -> expanded kriegt punkt
                    } else {
                        myPoints += 1;//vorher pointsInExpanded +=0
                    }
                }
                else{//if(rolloutDepth!=1000){
                    //vorher if(rollOutBoard.getMovesCounter(expandedNode.parent.sideToMove) - expanderScore > rollOutBoard.getMovesCounter(expandedNode.sideToMove) - enemyScore){
                    if(rollOutBoard.getMovableCounter(myColor) - mineBefore > rollOutBoard.getMovableCounter(-myColor) - enemyBefore){
                        myPoints += 1;//was pointsInExpanded += 1;
                    } else{
                        myPoints += 0;//was pointsInExpanded += 0;
                    }
                }
            }

        }

        return myPoints;// was pointsInExpanded;
    }

    void backpropagateOld(TreeNode expandedNode, double mySimulationScore){ //int mySimulationScore){
        TreeNode currentNode = expandedNode;
        while(null != currentNode){
            currentNode.visitCounter += 1;
            if(-myColor == currentNode.sideToMove){            // WENN SIDETOMOVE = GEGNER ist ist großer score im Node gut für MyColor weil MyColor den Node mit dem größten score wählt
                currentNode.scoreForMCTS += mySimulationScore;
            } else {
                currentNode.scoreForMCTS += 1- mySimulationScore;
            }
            currentNode = currentNode.parent;
        }
    }

    void backpropagate(TreeNode expandedNode, double mySimulationScore){
        TreeNode currentNode = expandedNode;
        while(null != currentNode){
            currentNode.visitCounter += rolloutsPerSimulation;
            currentNode.scoreForMCTS += mySimulationScore;
            currentNode = currentNode.parent;
        }
    }

    public static void main(String[] args) {
    }
}

