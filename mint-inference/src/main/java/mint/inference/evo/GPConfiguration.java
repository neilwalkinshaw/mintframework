package mint.inference.evo;

/**
 * Data class to store GP configuration data.
 */
public class GPConfiguration {

    private final int populationSize;
    private final double crossOver;
    private final double mutation;
    private final int depth;
    private final int tournamentSize;


    public GPConfiguration(int populationSize, double crossOver, double mutation, int depth, int tournamentSize) {
        this.populationSize = populationSize;
        this.crossOver = crossOver;
        this.mutation = mutation;
        this.depth = depth;
        this.tournamentSize = tournamentSize;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public double getCrossOver() {
        return crossOver;
    }

    public double getMutation() {
        return mutation;
    }

    public int getDepth() {
        return depth;
    }

    public int getTournamentSize() {
        return tournamentSize;
    }
}
