import java.io.Serializable;
import java.util.ArrayList;

/**
 * Serializable UniverseToSend class.
 * The purpose of this class is so that an individual process can send only the necessary information
 * to other processes in its horizon. Universe objects also receive UniverseToSend objects when other processes
 * are sending information to that object.
 */
public class UniverseToSend implements Serializable {
    public ArrayList<Particle> smallParticles;
    public ArrayList<Particle> largeParticles;
    public int rank;

    /**
     * Constructor for the UniverseToSend class.
     * Constructor makes a copy of the values in the smallParticles and largeParticles lists in order to prevent
     * concurrent modification exceptions being thrown.
     * @param smallParticles
     * @param largeParticles
     * @param rank
     */
    public UniverseToSend(ArrayList<Particle> smallParticles, ArrayList<Particle> largeParticles, int rank) {
        this.smallParticles = new ArrayList(smallParticles);
        this.largeParticles = new ArrayList(largeParticles);
        this.rank = rank;
    }
}
