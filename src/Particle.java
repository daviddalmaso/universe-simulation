import java.io.Serializable;
import java.util.List;

/**
 * Class to represent particles in a given universe.
 * There is no distinction between small and large particles in an individual process.
 * This is because the physics are the same between them.
 * Only the Universe objects contain different lists for small and large particles for the later purpose of
 * distinguishing them in the PPM file.
 */
public class Particle implements Serializable {
    double mass, radius, locX, locY, velocityX, velocityY, forceX, forceY;

    public Particle(double radius, double mass, double locX, double locY,
                    double velocityX, double velocityY, double forceX, double forceY) {
        this.radius = radius;
        this.mass = mass;
        this.locX = locX;
        this.locY = locY;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.forceX = forceX;
        this.forceY = forceY;
    }

    /**
     * Constructor for small particles.
     * Gives particles a random starting location and uses the radius and mass as specified in initialspec.txt file.
     * All small particles start with no velocity and no force acting on them.
     * @param radius
     * @param mass
     * @param gridSize
     * @param row
     * @param column
     * @return a Particle object representing a small particle
     */
    public static Particle createSmallParticle(double radius, double mass, double gridSize, int row, int column) {
        double locX = (Math.random() + column) * gridSize;
        double locY = (Math.random() + row) * gridSize;
        return new Particle(radius, mass, locX, locY, 0, 0, 0, 0);
    }

    /**
     * Constructor for large particles.
     * Creates the particles as specified in the initialspec.txt file
     * All large particles start with no velocity and no force acting on them.
     * @param specs
     * @param gridSize
     * @param row
     * @param column
     * @return a Particle object representing a large particle
     */
    public static Particle createLargeParticle(LargeParticleSpec specs, double gridSize, int row, int column) {
        double locX = specs.locX + (gridSize * column);
        double locY = specs.locY + (gridSize * row);
        System.out.println("Row: " + row + " Column: " + column + " LocX: " + locX + " LocY: " + locY);
        return new Particle(specs.radius, specs.mass, locX, locY, 0, 0, 0, 0);
    }

    /**
     * Updates the force on the particle in the x and y direction given a list of particles acting on the current particle.
     * @param particles
     */
    public void calculateForceOnParticle(List<Particle> particles) {
        double forceX = 0;
        double forceY = 0;
        for (Particle particle : particles) {
            if (particle == this) continue;
            double xLength = particle.locX - locX;
            double yLength = particle.locY - locY;
            double distance = Math.sqrt(Math.pow(xLength, 2) + Math.pow(yLength, 2));
            double massProduct = mass * particle.mass;
            double totalForce = 3 * massProduct / Math.pow(distance, 2);
            forceX += xLength / distance * totalForce;
            forceY += yLength / distance * totalForce;
        }
        this.forceX = forceX;
        this.forceY = forceY;
    }

    /**
     * Updates the location and velocity of the particle from the precomputed forceX and forceY values.
     * Factors in the initial state of the locX, locY, velocityX, and velocityY.
     * @param timeStep
     * @param gridSize
     * @param size
     */
    public void updateLocationAndVelocity(double timeStep, int gridSize, int size) {
        double accelerationX = forceX / mass;
        double accelerationY = forceY / mass;
        double bigGridSize = gridSize * (Math.sqrt(size));
        locX = (1/2) * accelerationX * Math.pow(timeStep, 2) + velocityX * timeStep + locX;
        locY = (1/2) * accelerationY * Math.pow(timeStep, 2) + velocityY * timeStep + locY;
        locX = locX % bigGridSize;
        locY = locY % bigGridSize;
        if (locX < 0) {
            locX += bigGridSize;
        }
        if (locY < 0) {
            locY += bigGridSize;
        }
        velocityX = velocityX + accelerationX * timeStep;
        velocityY = velocityY + accelerationY * timeStep;
    }

    @Override
    public String toString() {
        String formatted = "";
        formatted += "Mass: " + this.mass + "\n";
        formatted += "Radius: " + this.radius + "\n";
        formatted += "LocX: " + this.locX + "\n";
        formatted += "LocY: " + this.locY + "\n";
        formatted += "VelocityX: " + this.velocityX + "\n";
        formatted += "VelocityY: " + this.velocityY + "\n";
        return formatted;
    }
}
