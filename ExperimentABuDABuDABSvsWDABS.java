
public class ExperimentABuDABuDABSvsWDABS {

    public static void main(String[] args) {

        MiniKonane miniGame;
        AlphaBeta ab;
        DAlphaBeta dab;
        DAlphaBeta dabs;
        DAlphaBeta wdabs;

        int winsOfDABS = 0;
        int winsOfWDABS = 0;

        int[][] starterStoneCoord;
        int[][][] secondStoneCoord;
        int dabsSearcherColor;

        int[] starter;


        Move executeMove;
        System.out.println("match,runtimemicro,algo,turn,blackpiece,blackmovable,blackmove,whitepiece,whitemovable,whitemove,color,branchingfactor,visited,visitedsum,betacutoff,betacutoffSum,windowcutoff,windowcutsum,bestmoveindex,worstscore,bestscore,bettermoves,bettermovessum,runtimesum");


        int simulations = Integer.parseInt(args[0]);
        int simNumber = 0;

        int xSize = Integer.parseInt(args[1]);
        int ySize = Integer.parseInt(args[2]);

        int dabsDepth = Integer.parseInt(args[3]);
        int wdabsDepth = Integer.parseInt(args[4]);
        //boolean sortedMode = Boolean.parseBoolean(args[5]);
        //boolean windowMode = Boolean.parseBoolean(args[6]);
        int windowRadius = Integer.parseInt(args[5]);

        //int benchmarkDepth = Integer.parseInt(args[8]);

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
                            dabsSearcherColor = MiniKonane.BLACK;
                        } else {
                            dabsSearcherColor = MiniKonane.WHITE;
                        }


                        if (simNumber == simulations) {
                            break outerloop;
                        }

                        miniGame = new MiniKonane(xSize, ySize);

                        ab = new AlphaBeta(miniGame, dabsSearcherColor);
                        dab = new DAlphaBeta(miniGame, dabsSearcherColor, false, false);
                        dabs = new DAlphaBeta(miniGame, dabsSearcherColor, false, true);

                        //OPPONENT COLOR
                        wdabs = new DAlphaBeta(miniGame, -dabsSearcherColor, true, true);

                        miniGame.board[starter[0]][starter[1]] = 0;
                        miniGame.board[second[0]][second[1]] = 0;

                        ab.removeStarter(starter[0], starter[1]);
                        ab.removeStarter(second[0], second[1]);

                        dab.dBoard.removeStarterStone(starter[0], starter[1]);
                        dab.dBoard.removeNeighborOfStarter(second[0], second[1], starter[0], starter[1]);

                        dabs.dBoard.removeStarterStone(starter[0], starter[1]);
                        dabs.dBoard.removeNeighborOfStarter(second[0], second[1], starter[0], starter[1]);

                        wdabs.dBoard.removeStarterStone(starter[0], starter[1]);
                        wdabs.dBoard.removeNeighborOfStarter(second[0], second[1], starter[0], starter[1]);

                        while (true) {
                            System.out.print(simNumber + ",");
                            if (miniGame.activePlayer == dabsSearcherColor) {

                                ab.calculateMove(dabsDepth);// DO NOT EXECUTE MOVE
                                System.out.print(simNumber + ",");
                                dab.calculateMove(dabsDepth);
                                System.out.print(simNumber + ",");

                                executeMove = dabs.combineSelectWindowMove(dabsDepth,100);
                                if (executeMove == null) {
                                    winsOfWDABS++;
                                    break;
                                }


                                ab.executeOwnMove(executeMove);
                                dab.executeOwnMove(executeMove);

                                wdabs.enemyMadeMove(executeMove);
                            } else {
                                executeMove = wdabs.combineSelectWindowMove(wdabsDepth,windowRadius);
                                if (executeMove == null) {
                                    winsOfDABS++;
                                    break;
                                }

                                ab.enemyMadeMove(executeMove);
                                dab.enemyMadeMove(executeMove);
                                dabs.enemyMadeMove(executeMove);

                            }

                            miniGame.makeMove(executeMove);

                        }
                        simNumber++;
                    }
                }
            }
        }
        System.out.println("DABS wins " + winsOfDABS + ",WDABS wins " + winsOfWDABS);
    }
}
