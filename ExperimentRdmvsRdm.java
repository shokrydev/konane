
public class ExperimentRdmvsRdm {

    public static void main(String[] args) {


        //MiniKonane miniGame;

        DBoard dBoard;


        int[][] starterStoneCoord;
        int[][][] secondStoneCoord;
        int[] starter;


        Move executeMove;
        System.out.println("simnum,turns,winner,peakmovablesblack,peakbranchingblack,peakmovableswhite,peakbranchingwhite,peakmovableswinner,peakbranchingwinner,peakmovablesloser,peakbranchingloser");
        int simulations = Integer.parseInt(args[0]);
        int simNumber = 0;

        int xSize = Integer.parseInt(args[1]);
        int ySize = Integer.parseInt(args[2]);


        starterStoneCoord = new int[][]{{0, 0}, {xSize / 2 - 1, ySize / 2 - 1}, {xSize / 2, ySize / 2}, {xSize - 1, ySize - 1}};
        secondStoneCoord = new int[][][]{
                {{0, 1}, {1, 0}},
                {{xSize / 2 - 1, ySize / 2 - 2}, {xSize / 2, ySize / 2 - 1}, {xSize / 2 - 1, ySize / 2}, {xSize / 2 - 2, ySize / 2 - 1}},
                {{xSize / 2, ySize / 2 - 1}, {xSize / 2 + 1, ySize / 2}, {xSize / 2, ySize / 2 + 1}, {xSize / 2 - 1, ySize / 2}},
                {{xSize - 2, ySize - 1}, {xSize - 1, ySize - 2}}
        };

        int peakMovablesBlack;
        int peakMovablesWhite;

        int peakBranchingBlack;
        int peakBranchingWhite;

// OPENING TURNS
        outerloop:
        for (int i = 0; i < simulations; i++) {
            for (int starterMoveIndex = 0; starterMoveIndex < 4; starterMoveIndex++) {
                starter = starterStoneCoord[starterMoveIndex];


                for (int[] second : secondStoneCoord[starterMoveIndex]) {
                    //WER FÄNGT AN
                    for (int whoStarts = 0; whoStarts < 2; whoStarts++) {


                        if (simNumber == simulations) {
                            break outerloop;
                        }

                        dBoard = new DBoard(xSize,ySize);

                        dBoard.removeStarterStone(starter[0], starter[1]);
                        dBoard.removeNeighborOfStarter(second[0], second[1], starter[0], starter[1]);


                        peakMovablesBlack = 0;
                        peakBranchingBlack = 0;
                        peakMovablesWhite = 0;
                        peakBranchingWhite = 0;
                        while (true) {


                            if (dBoard.blackMovesCounter > peakBranchingBlack){
                                peakBranchingBlack = dBoard.blackMovesCounter;
                            }
                            if (dBoard.blackMovableCounter > peakMovablesBlack){
                                peakMovablesBlack = dBoard.blackMovableCounter;
                            }
                            if (dBoard.whiteMovesCounter > peakBranchingWhite){
                                peakBranchingWhite = dBoard.whiteMovesCounter;
                            }
                            if (dBoard.whiteMovableCounter > peakMovablesWhite){
                                peakMovablesWhite = dBoard.whiteMovableCounter;
                            }


                            if(dBoard.gameOver()){

                                if(dBoard.getHalfmoveClock() <30){
                                    if (dBoard.getSideToMove() == DBoard.BLACK){
                                        System.out.println(simNumber + "," + dBoard.getHalfmoveClock() + ",WHITE," + peakMovablesBlack +","+peakBranchingBlack +","+peakMovablesWhite +","+peakBranchingWhite  +","+peakMovablesWhite +","+peakBranchingWhite + "," + peakMovablesBlack +","+peakBranchingBlack);
                                    } else {
                                        System.out.println(simNumber + "," + dBoard.getHalfmoveClock() + ",BLACK," + peakMovablesBlack +","+peakBranchingBlack +","+peakMovablesWhite +","+peakBranchingWhite + "," + peakMovablesBlack +","+peakBranchingBlack +","+peakMovablesWhite +","+peakBranchingWhite);
                                    }
                                    System.out.println(starter[0] + "," + starter[1] + "," + second[0] + "," + second[1]);
                                    System.out.println(dBoard.boardToString());
                                }

                                break;
                            }


                            dBoard.makeMove(dBoard.getSideToMove(),dBoard.getRandomMove(dBoard.getSideToMove()));

                        }
                        simNumber++;
                    }
                }
            }

        }
    }
}
