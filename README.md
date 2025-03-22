##### Content of rows logged by minimax players
matchnumber,runtimemicroseconds,algoname,turn,blackpieces,blackmovables,blackmoves,whitepieces,whitemovables,whitemoves,color,branchingfactor,visited,visitedsum,betacutoff,betacutoffSum,windowcutoff,windowcutsum,bestmoveindex,worstscore,bestscore,bettermoves,bettermovessum,runtimesum

##### Content of rows logged by mcts players
matchnumber,runtimemicroseconds,algoname,turn,blackpieces,blackmovables,blackmoves,whitepieces,whitemovables,whitemoves,color,branchingfactor,visited,visitedsum,inheritedtreesize,betacutoffSum,rootvisit,windowcutsum,bestchildvisit,lowestratio,highestratio,rootratio,bettermovessum,runtimesum

Some mcts log fields hold no information and were kept only to facilitate scanning the log as a csv file

*********************************************************************************************************************

### Comparing minimax players AB,DAB and DABS with player WDABS

class: ExperimentABuDABuDABSvsWDABS\
arguments: repetitions x-boardsize y-boardsize benchmarkDepth windowDepth windowPadding

##### commands:
java ExperimentABuDABuDABSvsWDABS 100 8 8 6 8 4\
java ExperimentABuDABuDABSvsWDABS 100 8 8 7 8 5\
java ExperimentABuDABuDABSvsWDABS 100 14 17 6 8 4



### Comparing DMCTS and MCTS

class: ExperimentDMCTSvsMCTS\
arguments: repetitions x-boardsize y-boardsize budgetSeconds

##### command:
java ExperimentDMCTSvsMCTS 100 8 8 5


### Comparing DMCTS and DABS

class: ExperimentDMCTSvsDAB\
arguments: repetitions x-boardsize y-boardsize depthlimit windowMode windowPadding budgetMultiplier

##### commands:
java ExperimentDMCTSvsDAB 100 8 8 2 false -1 1\
java ExperimentDMCTSvsDAB 100 8 8 2 false -1 2\
java ExperimentDMCTSvsDAB 100 8 8 2 false -1 4\
java ExperimentDMCTSvsDAB 100 8 8 2 false -1 8\
java ExperimentDMCTSvsDAB 100 8 8 2 false -1 16\
java ExperimentDMCTSvsDAB 100 8 8 2 false -1 32\
java ExperimentDMCTSvsDAB 100 8 8 2 false -1 64

java ExperimentDMCTSvsDAB 100 8 8 4 false -1 1\
java ExperimentDMCTSvsDAB 100 8 8 4 false -1 2\
java ExperimentDMCTSvsDAB 100 8 8 4 false -1 4\
java ExperimentDMCTSvsDAB 100 8 8 4 false -1 8\
java ExperimentDMCTSvsDAB 100 8 8 4 false -1 16\
java ExperimentDMCTSvsDAB 100 8 8 4 false -1 32

java ExperimentDMCTSvsDAB 100 8 8 6 false -1 1\
java ExperimentDMCTSvsDAB 100 8 8 6 false -1 2\
java ExperimentDMCTSvsDAB 100 8 8 6 false -1 4\
java ExperimentDMCTSvsDAB 100 8 8 6 false -1 8\
java ExperimentDMCTSvsDAB 100 8 8 6 false -1 16

java ExperimentDMCTSvsDAB 100 8 8 8 false -1 1\
java ExperimentDMCTSvsDAB 100 8 8 8 false -1 2\
java ExperimentDMCTSvsDAB 100 8 8 8 false -1 4\
java ExperimentDMCTSvsDAB 100 8 8 8 false -1 8

java ExperimentDMCTSvsDAB 100 8 8 10 false -1 1\
java ExperimentDMCTSvsDAB 100 8 8 10 false -1 2\
java ExperimentDMCTSvsDAB 100 8 8 10 false -1 4
