import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to parse the initialspec.txt file
 */
public class InitialSpec {
    int timeSlots, horizon, gridSize, numberOfSmallParticles, numberOfLargeParticles;
    double timeStep, smallParticleMass, smallParticleRadius;
    List<LargeParticleSpec> largeParticleSpecs = new ArrayList<LargeParticleSpec>();

    /**
     * The constructor takes in the filename (i.e. initialspec.txt) and parses the file for information about how
     * to run the simulation.
     * @param fileName
     */
    public InitialSpec(String fileName) {
        try {
            FileReader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(" ");
                if (i < 8) {
                    String value = splitLine[1];
                    if (i == 0) {
                        this.timeSlots = Integer.parseInt(value);
                    } else if (i == 1) {
                        this.timeStep = Double.parseDouble(value);
                    } else if (i == 2) {
                        this.horizon = Integer.parseInt(value);
                    } else if (i == 3) {
                        this.gridSize = Integer.parseInt(value);
                    } else if (i == 4) {
                        this.numberOfSmallParticles = Integer.parseInt(value);
                    } else if (i == 5) {
                        this.smallParticleMass = Double.parseDouble(value);
                    } else if (i == 6) {
                        this.smallParticleRadius = Double.parseDouble(value);
                    } else if (i == 7) {
                        this.numberOfLargeParticles = Integer.parseInt(value);
                    }
                } else {
                    double radius = Double.parseDouble(splitLine[0]);
                    double mass = Double.parseDouble(splitLine[1]);
                    double locX = Double.parseDouble(splitLine[2]);
                    double locY = Double.parseDouble(splitLine[3]);
                    LargeParticleSpec spec = new LargeParticleSpec(radius, mass, locX, locY);
                    largeParticleSpecs.add(spec);
                }
                i++;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public String toString() {
        String formatted = "";
        formatted += "Time Slots: " + this.timeSlots + "\n";
        formatted += "Time Step: " + this.timeStep + "\n";
        formatted += "Horizon: " + this.horizon + "\n";
        formatted += "Grid Size: " + this.gridSize + "\n";
        formatted += "Number of Small Particles: " + this.numberOfSmallParticles + "\n";
        formatted += "Small Particle Mass: " + this.smallParticleMass + "\n";
        formatted += "Small Particle Radius: " + this.smallParticleRadius + "\n";
        formatted += "Number of Large Particles: " + this.numberOfLargeParticles + "\n";
        return formatted;
    }
}
