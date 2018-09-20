import mpi.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The Universe class represents an individual processor's grid with its own particles
 */
public class Universe {
    private ArrayList<Particle> smallParticles; // small particles in current universe
    private ArrayList<Particle> largeParticles; // large particles in current universe
    private ArrayList<UniverseToSend> data; // array to collect data being sent between processes
    private UniverseToSend universeToSend; // serializable object consisting of small and large particles and rank
    private int gridSize; // gridSize as specified in initalspec.txt file
    private int horizon; // horizon as specified in initalspec.txt file
    public int rank; // rank of process
    private int size; // size of processes being run
    private int row; // the row of the process in the grid of processes
    private int column; // the column of the process in the grid of processes
    private int iteration; // current iteration of the simulation
    private int timeSlots; // number of iterations to run
    public ArrayList<Integer> ranksInHorizon; // list of process ranks to communicate with at each iteration of the simulation

    /**
     * Constructor for Universe objects
     * @param specs InitialSpec object containing values from the parsed initialspec.txt file
     * @param rank Rank of the process
     * @param size Number of processes in the simulation
     */
    public Universe(InitialSpec specs, int rank, int size) {
        this.rank = rank;
        this.size = size;
        this.row = (int)(rank / Math.sqrt((double)size));
        this.column = (int)(rank % Math.sqrt((double)size));
        this.gridSize = specs.gridSize;
        this.horizon = specs.horizon;
        this.timeSlots = specs.timeSlots;
        this.iteration = 1;
        this.data = new ArrayList<>();
        this.smallParticles = generateSmallParticles(specs);
        this.largeParticles = generateLargeParticles(specs);
        this.universeToSend = new UniverseToSend(smallParticles, largeParticles, rank);
        this.ranksInHorizon = GridUtil.shouldSend(rank, horizon, size);
    }

    /**
     * Method to initialize location of small particles in the process's universe
     * @param specs InitialSpec object created from parsing the initialspec.txt file
     * @return returns a list of Particle objects representing the small particles in the process's universe
     */
    private ArrayList<Particle> generateSmallParticles(InitialSpec specs) {
        ArrayList<Particle> particles = new ArrayList<>();
        for (int i = 0; i < specs.numberOfSmallParticles; i++) {
            Particle smallParticle = Particle.createSmallParticle(
                    specs.smallParticleRadius, specs.smallParticleMass, specs.gridSize, row, column);
            particles.add(smallParticle);
        }
        return particles;
    }

    /**
     * Method to initialize large particles in the process's universe
     * @param specs InitialSpec object created from parsing the initialspec.txt file
     * @return returns a list of Particle objects representing the large particles in the process's universe
     */
    private ArrayList<Particle> generateLargeParticles(InitialSpec specs) {
        ArrayList<Particle> particles = new ArrayList<>();
        for (int i = 0; i < specs.numberOfLargeParticles; i++) {
            Particle largeParticle = Particle.createLargeParticle(
                    specs.largeParticleSpecs.get(i), gridSize, row, column);
            particles.add(largeParticle);
        }
        return particles;
    }

    /**
     * Generates PPM file with the filename specified preceded by the process's rank
     * i.e. process with rank 2 will create file 2finalbrd.ppm if given filename is finalbrd.ppm
     * @param filename
     */
    public void generatePPM(String filename) {
        PPM ppm = new PPM(gridSize, smallParticles, largeParticles, row, column);
        filename = "" + rank + filename;
        try (PrintWriter out = new PrintWriter(filename)) {
            out.println(ppm.toString());
            out.close();
        } catch (Exception e) {
            System.out.println("Cannot open file");
        }
    }

    /**
     * Main function to handle the iterations of the simulation.
     * There are 5 main things that need to be done at each iteration.
     * 1. Add particles that entered current universe
     * 2. Update forces on particles in the current universe
     * 3. Update locations of the particles in the current universe
     * 4. Update UniverseToSend object with the latest state of the current universe
     * 5. Remove any particles from current universe that exited during the iteration
     * @param timeStep
     */
    public void simulateIteration(double timeStep) {
        try {
            addEnteredParticles();
            updateForcesOnParticles();
            updateLocationsOfParticles(timeStep);
            updateUniverseToSend();
            if (iteration < timeSlots) {
                removeExitedParticles(); // remove particles only when it's not on the last iteration of the simulation
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Updates current serializable universe to send to other processes within the horizon
     */
    private void updateUniverseToSend() {
        this.universeToSend = new UniverseToSend(smallParticles, largeParticles, rank);
    }

    /**
     * Updates the locations of the particles in the universe
     * @param timeStep The time step to perform the physics calculations with
     */
    private void updateLocationsOfParticles(double timeStep) {
        for (Particle particle : largeParticles) {
            if (isInCurrentUniverse(particle)) {
                particle.updateLocationAndVelocity(timeStep, gridSize, size);
            }
        }
        for (Particle particle : smallParticles) {
            if (isInCurrentUniverse(particle)) {
                particle.updateLocationAndVelocity(timeStep, gridSize, size);
            }
        }
    }

    /**
     * Updates the forces on the particles in the current universe at current iteration
     */
    private void updateForcesOnParticles() {
        List<Particle> allParticles = new ArrayList<>();
        allParticles.addAll(largeParticles);
        allParticles.addAll(smallParticles);
        for (UniverseToSend otherUniverse : data) {
            allParticles.addAll(otherUniverse.largeParticles);
            allParticles.addAll(otherUniverse.smallParticles);
        }
        for (Particle particle : largeParticles) {
            if (isInCurrentUniverse(particle)) {
                particle.calculateForceOnParticle(allParticles);
            }
        }
        for (Particle particle : smallParticles) {
            if (isInCurrentUniverse(particle)) {
                particle.calculateForceOnParticle(allParticles);
            }
        }
    }

    /**
     * If any of the data received from other processes contains particles that have moved universes into current one,
     * they should be added to the small or large particle list
     */
    private void addEnteredParticles() {
        for (UniverseToSend universe : data) {
            for (Particle smallParticle : universe.smallParticles) {
                if (isInCurrentUniverse(smallParticle)) {
                    smallParticles.add(smallParticle);
                }
            }
            for (Particle largeParticle : universe.largeParticles) {
                if (isInCurrentUniverse(largeParticle)) {
                    System.out.println("Adding a large particle to process " + rank);
                    largeParticles.add(largeParticle);
                }
            }
        }
    }

    /**
     * If a particle in the universe exited after updating their locations,
     * they should no longer be accounted for by current universe, so they must be removed from small or large particle list.
     */
    private void removeExitedParticles() {
        for (Particle largeParticle : largeParticles) {
            // if large particle is out of current universe, remove it from largeParticles set
            if (!isInCurrentUniverse(largeParticle)) {
                System.out.println("Removing a large particle from process " + rank);
                largeParticles = new ArrayList<>(largeParticles);
                largeParticles.remove(largeParticle);
            }
        }
        for (Particle smallParticle : smallParticles) {
            // if small particle is out of current universe, remove it from smallParticles set
            if (!isInCurrentUniverse(smallParticle)) {
                smallParticles = new ArrayList<>(smallParticles);
                smallParticles.remove(smallParticle);
            }
        }
    }

    /**
     * Helper function to check if a given particle is contained in the current universe
     * @param particle Particle object to check
     * @return true or false depending on if given Particle is in the current universe or not, respectively
     */
    private boolean isInCurrentUniverse(Particle particle) {
        double xLowerBound = column * gridSize;
        double xUpperBound = (column + 1) * gridSize;
        double yLowerBound = row * gridSize;
        double yUpperBound = (row + 1) * gridSize;
        if (particle.locX < xLowerBound || particle.locX >= xUpperBound) return false;
        if (particle.locY < yLowerBound || particle.locY >= yUpperBound) return false;
        return true;
    }

    /**
     * Function to handle where data should be sent to and received from.
     * @param rankToSync current rank to update
     * @throws Exception
     */
    public void syncData(int rankToSync) throws Exception {
        if (rankToSync != rank && ranksInHorizon.contains(new Integer(rankToSync))) {
            sendData(rankToSync);
        } else if (rankToSync == rank) {
            int numReceives = ranksInHorizon.size();
            for (int i = 0; i < numReceives; i++) {
                receiveData();
            }
        }
    }

    /**
     * Method to send data from a process to the process with given rank.
     * The data sent is the object's current state of the UniverseToSend object
     * @param receivingRank
     * @throws MPIException
     * @throws IOException
     */
    private void sendData(int receivingRank) throws MPIException, IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            UniverseToSend newUniverse = new UniverseToSend(
                    this.universeToSend.smallParticles, this.universeToSend.largeParticles, this.universeToSend.rank);
            out = new ObjectOutputStream(bos);
            out.writeObject(newUniverse);
            out.flush();
            byte[] universeObject = bos.toByteArray();
            MPI.COMM_WORLD.send(universeObject, universeObject.length, MPI.BYTE, receivingRank, 0);
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * Method to receive data from a process. The function starts a receive without specifying where the data needs to come from.
     * After receiving the UniverseToSend object, it is added to the `data` array of UniverseToSend objects
     * @throws MPIException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void receiveData() throws MPIException, IOException, ClassNotFoundException {
        byte[] universeObject = new byte[10000000]; // buffer to receive the UniverseToSend object
        Status status = MPI.COMM_WORLD.recv(universeObject, universeObject.length, MPI.BYTE, MPI.ANY_SOURCE, 0);
        ByteArrayInputStream bis = new ByteArrayInputStream(universeObject);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            Object receivedUniverse = in.readObject();
            UniverseToSend addedUniverse = new UniverseToSend(
                    ((UniverseToSend)receivedUniverse).smallParticles, ((UniverseToSend)receivedUniverse).largeParticles, ((UniverseToSend)receivedUniverse).rank);
            data.add(addedUniverse);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    /**
     * public method to clear other universes' data held in the current one
     */
    public void clearData() {
        this.data.clear();
    }

    /**
     * public method to increase the iteration of the simulation
     */
    public void increaseIteration() {
        this.iteration++;
    }
}
