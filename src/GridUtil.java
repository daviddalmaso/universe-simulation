import java.util.ArrayList;

/**
 * Utility functions for keeping track of where processes are in relation to each other
 */
public class GridUtil {

    /**
     * Given a process's rank, the simulation horizon value, and the size of amount of processes made,
     * the function should return the rank values of the processes that the process with the given rank should communicate with.
     * @param rank Number of current process
     * @param horizon Horizon of communication between processes
     * @param size Number of processes
     * @return List of ranks that the process with the given rank should communicate with
     */
    public static ArrayList<Integer> shouldSend(int rank, int horizon, int size) {
        ArrayList<Integer> retList = new ArrayList<>();
        if (horizon < 1) return retList;
        int gridWidth = (int)Math.sqrt(size);
        for (int i = (rank + (-horizon * gridWidth)); i <= (rank + (horizon * gridWidth)); i += gridWidth) {
            int pivot = i;
            if (pivot < 0) {
                pivot = pivot % size;
                pivot += size;
            } else if (pivot >= size) {
                pivot = pivot % size;
            }
            int row = pivot / gridWidth;
            for (int j = (pivot - horizon); j <= (pivot + horizon); j++) {
                int rankToAdd = j;
                int currRow = (int)Math.floor((double)rankToAdd / (double)gridWidth);
                rankToAdd += ((row - currRow)*gridWidth);
                if (!retList.contains(new Integer(rankToAdd))) {
                    retList.add(rankToAdd);
                }
            }
        }
        if (retList.contains(rank)) {
            retList.remove(new Integer(rank));
        }
        return retList;
    }
}
