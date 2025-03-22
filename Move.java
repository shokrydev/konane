public class Move implements Comparable<Move>{
    int startX;
    int startY;
    int targetX;
    int targetY;
    int jumps;
    int Xdirection; // multiples of  2
    int Ydirection; // multiples 2

    int orderScore;

    static int[][] directions = {{0,-1},{1,0},{0,1},{-1,0}}; // Nord Ost Süd West

    public Move(int currentX, int currentY, int destinationX, int destinationY, int jumpsCount, int Xdir, int Ydir, int score){
        startX = currentX;
        startY = currentY;
        targetX = destinationX;
        targetY = destinationY;
        jumps = jumpsCount;
        Xdirection = Xdir;
        Ydirection = Ydir;
        orderScore = score;
    }

    boolean sameMove(Move m){
        return startX==m.startX && startY==m.startY && targetX==m.targetX && targetY==m.targetY;
    }

    @Override
    public int compareTo(Move otherMove) {
        return orderScore - otherMove.orderScore;
    }

    @Override
    public String toString() {
        return "Move{" +
                "startX=" + startX +
                ", startY=" + startY +
                ", targetX=" + targetX +
                ", targetY=" + targetY +
                ", Xdirection=" + Xdirection +
                ", Ydirection=" + Ydirection +
                ", jumps=" + jumps +
                ", score=" + orderScore +
                '}';
    }
}
