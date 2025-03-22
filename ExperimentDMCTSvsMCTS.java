
public class ExperimentDMCTSvsMCTS {

    public static void main(String[] args) {




        MiniKonane miniGame;
        MCTS2 mcts2;
        DMCTS dmcts;

        int winsOfMCTS2 = 0;
        int winsOfDMCTS = 0;

        int[][] starterStoneCoord;
        int[][][] secondStoneCoord;
        int mcts2Color;

        int[] starter;


        Move executeMove;
        System.out.println("match,runtimemicro,algo,turn,blackpiece,blackmovable,blackmove,whitepiece,whitemovable,whitemove,color,branchingfactor,visited,visitedsum,inheritedtreesize,betacutoffSum,rootvisit,windowcutsum,bestchildvisit,lowestratio,highestratio,rootratio,bettermovessum,runtimesum");

        //replaced newBetaCutoffs      inheritedTreeSize
        //replaced newwindowCutoff        root.visitcounter -> finalTreeSize
        //replaced bestmoveindex       bestRootchild.visitCounter
        //replaced worstscore          lowestRatio
        //replaced bestScore           highestRatio
        //replaced newbettermove       root ratio -> average child ratio

        int simulations = Integer.parseInt(args[0]);
        int simNumber = 0;

        int xSize = Integer.parseInt(args[1]);
        int ySize = Integer.parseInt(args[2]);

        long budget = Integer.parseInt(args[3]) * 1000000000L;


        starterStoneCoord = new int[][]{{0, 0}, {xSize / 2 - 1, ySize / 2 - 1}, {xSize / 2, ySize / 2}, {xSize - 1, ySize - 1}};
        secondStoneCoord = new int[][][]{
                {{0, 1}, {1, 0}},
                {{xSize / 2 - 1, ySize / 2 - 2}, {xSize / 2, ySize / 2 - 1}, {xSize / 2 - 1, ySize / 2}, {xSize / 2 - 2, ySize / 2 - 1}},
                {{xSize / 2, ySize / 2 - 1}, {xSize / 2 + 1, ySize / 2}, {xSize / 2, ySize / 2 + 1}, {xSize / 2 - 1, ySize / 2}},
                {{xSize - 2, ySize - 1}, {xSize - 1, ySize - 2}}
        };

// OPENING TURNS
        outerloop:
        for (int i = 0; i < 5; i++) {
            for (int starterMoveIndex = 0; starterMoveIndex < 4; starterMoveIndex++) {
                starter = starterStoneCoord[starterMoveIndex];


                for (int[] second : secondStoneCoord[starterMoveIndex]) {
                    //WER FÄNGT AN
                    for (int whoStarts = 0; whoStarts < 2; whoStarts++) {
                        if (whoStarts == 0) {
                            mcts2Color = MiniKonane.BLACK;
                        } else {
                            mcts2Color = MiniKonane.WHITE;
                        }


                        if (simNumber == simulations) {
                            break outerloop;
                        }

                        miniGame = new MiniKonane(xSize, ySize);

                        mcts2 = new MCTS2(miniGame,mcts2Color);
                        dmcts = new DMCTS(miniGame,-mcts2Color,0.7071,false, false, 1000, 1);

                        //EXECUTE OPENING TURN
                        miniGame.board[starter[0]][starter[1]] = 0;
                        miniGame.board[second[0]][second[1]] = 0;

                        mcts2.board.removeStarter(starter[0], starter[1]);
                        mcts2.board.removeStarter(second[0], second[1]);
                        dmcts.dBoard.removeStarterStone(starter[0], starter[1]);
                        dmcts.dBoard.removeNeighborOfStarter(second[0], second[1], starter[0], starter[1]);

                        while (true) {
                            System.out.print(simNumber + ",");
                            if (miniGame.activePlayer == mcts2Color) {

                                executeMove = mcts2.selectMove(budget);//führt den move auch direkt aus

                                if (executeMove == null) {
                                    winsOfDMCTS++;
                                    break;
                                }
                                dmcts.enemyMadeMove(executeMove);
                            } else {
                                executeMove = dmcts.selectMove(budget);//30000000000L

                                if (executeMove == null) {
                                    winsOfMCTS2++;
                                    break;
                                }
                                mcts2.enemyMadeMove(executeMove);//nur hier und nicht für beide weil selectMove auch den move direct ausführt

                            }

                            miniGame.makeMove(executeMove);

                        }
                        simNumber++;
                    }
                }
            }

        }
        System.out.print("MCTS2 wins " + winsOfMCTS2 + ",DMCTS wins " + winsOfDMCTS);

    }
}
