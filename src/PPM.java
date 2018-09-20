import java.util.ArrayList;
import java.util.List;

/**
 * Class to help with the construction of a PPM file to display the state of a process's particle locations
 */
public class PPM {
    private ArrayList<ArrayList<int[]>> values; // rgb values in a 2D grid
    private String matrix; // values as a String
    private String img; // final String to pass into the output file
    private int gridSize; // gridSize of the process
    private int row; // row of the process when laid out in a square grid with the other processes
    private int column; // column of the process when laid out in a square grid with the other processes

    /**
     * Constructor for PPM file
     * @param gridSize
     * @param smallParticles
     * @param largeParticles
     * @param row
     * @param column
     */
    public PPM(int gridSize, List<Particle> smallParticles, List<Particle> largeParticles, int row, int column) {
        this.gridSize = gridSize;
        this.row = row;
        this.column = column;
        this.values = initValues();
        addLargeParticles(largeParticles);
        addSmallParticles(smallParticles);
        createMatrix();
        createImg();
    }

    /**
     * Initializes the gridSize x gridSize grid of rgb values to all 0 0 0 (r, g, b).
     * @return initialized values variable.
     */
    private ArrayList initValues() {
        ArrayList<ArrayList<int[]>> vals = new ArrayList();
        for (int i = 0; i < gridSize; i++) {
            ArrayList currRow = new ArrayList();
            for (int j = 0; j < gridSize; j++) {
                int[] rgb = new int[3];
                rgb[0] = 0;
                rgb[1] = 0;
                rgb[2] = 0;
                currRow.add(rgb);
            }
            vals.add(currRow);
        }
        return vals;
    }

    /**
     * Adds the large particles to the values. Sets the blue value within its radius to 255
     * @param largeParticles
     */
    private void addLargeParticles(List<Particle> largeParticles) {
        for (Particle particle : largeParticles) {
            double x = particle.locX - (gridSize * column);
            double y = particle.locY - (gridSize * row);
            double radius = particle.radius;
            for (double i = (x - radius); i < (x + radius); i++) {
                for (double j = (y - radius); j < (y + radius); j++) {
                    if (i >= 0 && j >= 0 && i <= gridSize && j <= gridSize) {
                        double xLength = Math.abs(i - x);
                        double yLength = Math.abs(j - y);
                        if ((Math.pow(xLength, 2) + Math.pow(yLength, 2)) < Math.pow(radius, 2)) {
                            this.values.get((int)j).get((int)i)[2] = 255;
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds the small particles to the values. Adds a value of 1 to the pixel that the small particle is in.
     * @param smallParticles
     */
    private void addSmallParticles(List<Particle> smallParticles) {
        for (Particle particle : smallParticles) {
            int x = (int)(particle.locX - (gridSize * column));
            int y = (int)(particle.locY - (gridSize * row));
            if (x >= 0 && y >= 0 && x < gridSize && y < gridSize && this.values.get(y).get(x)[2] != 255) {
                this.values.get(y).get(x)[0] = Math.min(this.values.get(y).get(x)[0] + 1, 255);
            }
        }
    }

    /**
     * Turns the current state of the values variable to a string.
     */
    private void createMatrix() {
        String formatted = "";
        for (ArrayList<int[]> row : values) {
            for (int[] rgb : row) {
                formatted += "" + rgb[0] + " " + rgb[1] + " " + rgb[2] + " ";
            }
            formatted += "\n";
        }
        this.matrix = formatted;
    }

    /**
     * Formats the matrix string into a valid String for a PPM file.
     */
    private void createImg() {
        String headers = "";
        headers += "P3\n";
        headers += "" + gridSize + " " + gridSize + "\n";
        headers += "255\n";
        this.img = headers + this.matrix;
    }

    /**
     * @return current state of img variable
     */
    @Override
    public String toString() {
        return img;
    }
}