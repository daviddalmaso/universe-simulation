import java.io.Serializable;

/**
 * Class to hold specs about a large particle.
 * The LargeParticleSpec objects are constructed when parsing the initialspec.txt file
 */
public class LargeParticleSpec implements Serializable {
    double radius, mass, locX, locY;

    public LargeParticleSpec(double radius, double mass, double locX, double locY) {
        this.radius = radius;
        this.mass = mass;
        this.locX = locX;
        this.locY = locY;
    }

    @Override
    public String toString() {
        String formatted = "";
        formatted += "Radius: " + this.radius + "\n";
        formatted += "Mass: " + this.mass + "\n";
        formatted += "LocX: " + this.locX + "\n";
        formatted += "LocY: " + this.locY + "\n";
        return formatted;
    }
}
