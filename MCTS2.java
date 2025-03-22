import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class MCTS2 {

    int myColor;
    Board board;

    TreeNode root;

    //stats
    int allSimulations; //wichtig für ucb;
    int roundSimulations;
    long playedTime;

    int inheritedTreeSize;

    Random rand;

    MCTS2(MiniKonane newGame, int color){
        myColor = color;
        board = new Board(newGame.xDim(),newGame.yDim());

        allSimulations = 0; // wichtig für ucb
        roundSimulations = 0;

        playedTime = 0;

        inheritedTreeSize = 0;

        rand = new Random();
    }

    private class TreeNode{
        LinkedList<Move> unexploredMoves;
        TreeNode parent;
        Move parentMove;
        ArrayList<TreeNode> children;
        int visitCounter;
        int numWins;
        int sideToMove;

        TreeNode(){
            unexploredMoves = board.getValidMoves(myColor); // SORTED DOCH NICHT NÜTZLICH
            children = new ArrayList<>();
            visitCounter = 0;
            numWins = 0;
            sideToMove = myColor; //diese korrektur ist falsch =mycolor ist doch richtig -> //dBoard.getSideToMove(); // VORHER WAR ES sideToMove = myColor; aber wie in MCTS.enemyMadeMove in der letzten IF condition bemerkt muss manchmal ein neuer baum gestartet werden weil neue root nichtmal im alten baum erreicht wurde
        }

        TreeNode(Board boardCopy,TreeNode expander, Move expansionMove){
            unexploredMoves = boardCopy.getValidMoves(-expander.sideToMove); // SORTED DOCH NICHT NÜTZLICH
            parent = expander;
            parentMove = expansionMove;
            children = new ArrayList<>();
            visitCounter = 0;
            numWins = 0;
            sideToMove = -expander.sideToMove;
        }


        double uct(){ // call function
            return ((double)numWins/visitCounter)+Math.sqrt(4 * Math.log(parent.visitCounter)/visitCounter); //NaN wenn allsimulations 0 ist
        }                 //da entsprechend Kocsis Szepesvari  c = 1/root(2) kommt 2*c= root(2) raus und das wird in wurzel dezogen und mal die 2 drinn genommen


    }

    void enemyMadeMove(Move m){
        board.makeMove(m);

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
        long startTime = System.nanoTime();

        if(null == root){
            root = new TreeNode();
        }

        roundSimulations = 0;

        inheritedTreeSize = root.visitCounter;

        if (root.unexploredMoves.isEmpty() & root.children.isEmpty()){

            //match,runtimemicro,algo,turn,blackpiece,blackmovable,blackmove,whitepiece,whitemovable,whitemove,
            // color,branchingfactor,visited,visitedsum,
            // betacutoff,betacutoffSum,windowcutoff,windowcutsum,
            // bestmoveindex,worstscore,bestscore,bettermoves,bettermovessum,
            // runtimesum
            System.out.println(  "0,MCTS," + board.getHalfmoveClock() + ",,,,,,"
                    + "," + (myColor==DBoard.BLACK?"BLACK":"WHITE") + ",0," + roundSimulations  + "," + allSimulations
                    + "," + inheritedTreeSize + ",," + root.visitCounter + ","
                    + ",,,,,"
                    + "," + playedTime);

            //statt newBetaCutoffs      inheritedTreeSize
            //statt newwindowCutoff        root.visitcounter -> finalTreeSize
            //statt bestmoveindex       bestRootchild.visitCounter
            //statt worstscore          lowestRatio
            //statt bestScore           highestRatio
            //statt newbettermove       root ratio -> average child ratio

            return null;
        }

        playedTime += resources/1000;

        TreeNode selectedNode;
        TreeNode expandedNode;
        TreeNode bestRootChild = null;
        double bestRatio = -Double.MAX_VALUE;
        double lowestRatio = Double.MAX_VALUE;

        int mySimulationScore;


        Board boardCopy;

        do{
            boardCopy = new Board(board);
            selectedNode = select(root,boardCopy);
            if(selectedNode.unexploredMoves.isEmpty() & selectedNode.children.isEmpty()){
                backpropagate(selectedNode,
                        selectedNode.sideToMove == myColor ? 0 : 1
                        );
                continue;
            }
            expandedNode = expand(selectedNode, boardCopy);
            mySimulationScore = simulate(boardCopy);
            roundSimulations++;
            allSimulations++;
            backpropagate(expandedNode, mySimulationScore);
        }while(System.nanoTime() < startTime + resources);

        for (TreeNode child: root.children) {
            if (bestRatio < (double)child.numWins/ child.visitCounter){
                bestRatio = (double)child.numWins/ child.visitCounter;
                bestRootChild = child;
            }
            if((double)child.numWins/ child.visitCounter < lowestRatio ){
                lowestRatio =(double) child.numWins/ child.visitCounter;
            }
        }

        board.makeMove(bestRootChild.parentMove);

        //match,runtimemicro,algo,turn,blackpiece,blackmovable,blackmove,whitepiece,whitemovable,whitemove,
        // color,branchingfactor,visited,visitedsum,
        // betacutoff,betacutoffSum,windowcutoff,windowcutsum,
        // bestmoveindex,worstscore,bestscore,bettermoves,bettermovessum,
        // runtimesum
        System.out.println(  resources/1000 + ",MCTS," + (board.getHalfmoveClock()-1) + ",,,,,,"
                + "," + (myColor==DBoard.BLACK?"BLACK":"WHITE") + "," + root.children.size() + "," + roundSimulations  + "," + allSimulations
                + "," + inheritedTreeSize + ",," + root.visitCounter + ","
                + "," + bestRootChild.visitCounter + "," + lowestRatio + "," + bestRatio + "," + ((root.visitCounter-root.numWins)/ root.visitCounter) + ","
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
/*
    selection

    expansion

    simulation

    backpropagation*/

    TreeNode select(TreeNode root, Board boardCopy){
        TreeNode currentNode = root;
        double highestUcb;
        TreeNode highestUcbNode = root;
        double newUcb;

        while(currentNode.unexploredMoves.isEmpty() & 0 < currentNode.children.size()){
            highestUcb = -1;
            for (TreeNode child: currentNode.children) {
                //System.out.println(child.parentMove);
                newUcb = child.uct();
                if(newUcb > highestUcb){ //setzt voraus dass ucb immer mindestens 0 ist also auch dass die scores
                    highestUcbNode = child;
                    highestUcb = newUcb;
                }
            }
            boardCopy.makeMove(highestUcbNode.parentMove);
            currentNode = highestUcbNode;
        }

        // DER VOM SELECTED NODE AUSGEHENDE MOVE WIRD ZUFÄLLIG IN EXPAND AUSGESUCHT UND IN DBOARDCOPY AUSGEFÜHRT

        return highestUcbNode;
    }

    //    Unless the node we end up at is a terminating state,,   Expand this node by applying one available action (as defined by the MDP) from the nodeand creating new nodes using the action outcomes.
    TreeNode expand(TreeNode selectedNode, Board boardCopy){
        Move expandedMove = selectedNode.unexploredMoves.removeFirst();
        boardCopy.makeMove(expandedMove);
        TreeNode expandedNewChild = new TreeNode(boardCopy, selectedNode, expandedMove);
        selectedNode.children.add(expandedNewChild);

        return expandedNewChild;
    }

    //From one of the outcomes of the expanded, perform a complete random simulation of the MDP to a terminating state. This therefore assumes that the simulation is finite, but versions of MCTS exist in which we just execute for some time and then estimate the outcome.
    int simulate(Board boardCopy){

        Move randomMove = boardCopy.getRandomMoveOrNull(myColor);

        while( randomMove  != null){
            boardCopy.makeMove(randomMove);
            randomMove = boardCopy.getRandomMoveOrNull(boardCopy.getSideToMove());
        }


        if(boardCopy.getSideToMove() == myColor){ // score aus perspektive vom MCTS spieler
            return 0;
        } else {
            return 1;
        }
    }

    void backpropagate(TreeNode expandedNode, int mySimulationScore){
        TreeNode currentNode = expandedNode;
        while(null != currentNode){
            currentNode.visitCounter += 1;
            if(-myColor == currentNode.sideToMove){            // WENN SIDETOMOVE = GEGNER ist ist großer score im Node gut für MyColor weil MyColor den Node mit dem größten score wählt
                currentNode.numWins += mySimulationScore;
            } else {
                currentNode.numWins += 1- mySimulationScore;
            }
            currentNode = currentNode.parent;
        }
    }

    public static void main(String[] args) {
    }
}

