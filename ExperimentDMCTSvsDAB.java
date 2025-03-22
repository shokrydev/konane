
public class ExperimentDMCTSvsDAB {

    public static void main(String[] args) {

        long timerABstart = 0;
        long timerABfinish = 250100100;
        long budget;


        MiniKonane miniGame;
        DAlphaBeta ab;
        DMCTS mcts;

        int winsOfAB = 0;
        int winsOfMCTS = 0;

        int[][] starterStoneCoord;
        int[][][] secondStoneCoord;
        int dabSearcherColor;

        int[] starter;


        Move executeMove;
        System.out.println("match,runtimemicro,algo,turn,blackpiece,blackmovable,blackmove,whitepiece,whitemovable,whitemove,color,branchingfactor,visited,visitedsum,betacutoff,betacutoffSum,windowcutoff,windowcutsum,bestmoveindex,worstscore,bestscore,bettermoves,bettermovessum,runtimesum");


        int simulations = Integer.parseInt(args[0]);
        int simNumber = 0;

        int xSize = Integer.parseInt(args[1]);
        int ySize = Integer.parseInt(args[2]);

        int abDepth = Integer.parseInt(args[3]);
        boolean windowMode = Boolean.parseBoolean(args[4]);
        int windowRadius = Integer.parseInt(args[5]);

        int budgetMultiplier = Integer.parseInt(args[6]);


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
                            dabSearcherColor = MiniKonane.BLACK;
                        } else {
                            dabSearcherColor = MiniKonane.WHITE;
                        }


                        if (simNumber == simulations) {
                            break outerloop;
                        }

                        miniGame = new MiniKonane(xSize, ySize);
                        ab = new DAlphaBeta(miniGame, dabSearcherColor, windowMode, true);
                        mcts = new DMCTS(miniGame, -dabSearcherColor, 0.7071, false, false, 1000, 1);


                        //EXECUTE OPENING TURN
                        miniGame.board[starter[0]][starter[1]] = 0;
                        miniGame.board[second[0]][second[1]] = 0;
                        ab.dBoard.removeStarterStone(starter[0], starter[1]);
                        ab.dBoard.removeNeighborOfStarter(second[0], second[1], starter[0], starter[1]);
                        mcts.dBoard.removeStarterStone(starter[0], starter[1]);
                        mcts.dBoard.removeNeighborOfStarter(second[0], second[1], starter[0], starter[1]);

                        while (true) {
                            System.out.print(simNumber + ",");
                            if (miniGame.activePlayer == dabSearcherColor) {
                                timerABstart = System.nanoTime();

                                executeMove = ab.combineSelectWindowMove(abDepth, windowRadius);//führt den move auch direkt aus

                                timerABfinish = System.nanoTime();
                                if (executeMove == null) {
                                    winsOfMCTS++;
                                    break;
                                }
                                mcts.enemyMadeMove(executeMove);
                            } else {
                                budget = timerABfinish - timerABstart;

                                //System.out.println("Budget" + (timerABfinish - timerABstart));
                                executeMove = mcts.selectMove(budget * budgetMultiplier);//30000000000L

                                if (executeMove == null) {
                                    winsOfAB++;
                                    break;
                                }
                                ab.enemyMadeMove(executeMove);//nur hier und nicht für beide weil selectMove auch den move direct ausführt

                            }

                            miniGame.makeMove(executeMove);

                        }
                        simNumber++;
                        //System.out.println(miniGame.boardToString());
                    }
                }
            }

        }
        System.out.print("AB wins " + winsOfAB + ",MCTS wins " + winsOfMCTS);

    }
}
