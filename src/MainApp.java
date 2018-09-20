import mpi.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

public class MainApp {

    public static void main(String... args) throws MPIException, IOException, ClassNotFoundException {

        MPI.Init(args);
        int myRank = MPI.COMM_WORLD.getRank();
        int mySize = MPI.COMM_WORLD.getSize();

        // check to make sure that the number of processes is a square number to ensure an N x N grid can be made
        if (Math.sqrt(mySize) != (double)(int)Math.sqrt(mySize)) {
            if (myRank == 0) System.out.println("Number of processors must be a square (i.e. 1, 4, 9, 16, ...)");
            System.exit(1);
        }

        // exit if initialspec.txt file is not given
        if (args.length < 1 ) {
            System.exit(1);
        }

        // initialize specs
        InitialSpec specs = new InitialSpec(args[0]);

        // create universe per processor given specs, rank, and size
        Universe universe = new Universe(specs, myRank, mySize);

        // generate initial PPM files
        universe.generatePPM("initialbrd.ppm");

        // main loop for simulations. Simulates as many times as specified in the Time Slots field of the initialspec file
        for (int i = 0; i < specs.timeSlots; i++) {
            if (myRank == 0) {
                System.out.println("Starting iteration " + i);
            }

            // at the start of every loop, synchronize data across processors within each others horizon
            for (int j = 0; j < mySize; j++) {
                try {
                    universe.syncData(j);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

            // once data is synchronized, we can simulate the iteration
            universe.simulateIteration(specs.timeStep);

            // clear data each universe received and increase current iteration
            universe.clearData();
            universe.increaseIteration();
        }

        if (args.length > 1) {
            universe.generatePPM(args[1]);
        }

        MPI.Finalize();
    }
}
