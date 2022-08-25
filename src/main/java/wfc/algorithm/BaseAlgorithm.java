package wfc.algorithm;

import java.util.ArrayList;
import java.util.Stack;

public class BaseAlgorithm {
    int width;
    int height;
    int dimensions;
    int tileSize;

    int tiles;
    ArrayList<Integer> frequencyHints;
    ArrayList<ArrayList<ArrayList<Integer>>> adjacencyRules;

    boolean[][] grid;
    int[][][] tileEnablers;

    double[] weightLogWeights;
    double sumOfWeights;
    double sumOfWeightLogWeights;
    double startingEntropy;

    int[] possibleTilesPerCell;
    double[] cachedWeights;
    double[] cachedWeightLogWeights;
    double[] entropies;

    int[] output;

    int[] distribution;
    Stack<int[]> removalStack;

    boolean initialized;
    boolean completed;

    public static final int[] offsetsX = {-1, 0, 1, 0};
    public static final int[] offsetsY = {0, 1, 0, -1};
    public static final int[] oppositeDirs = {2, 3, 0, 1};

    /*
    Status Codes:
    0 - Generation In Progress
    1 - Failed Generation
    2 - Successful Generation
     */

    boolean run() {
        if (!this.initialized) this.initialize();
        this.clear();

        while (true) {
            int res = this.runOnce();

            if (res != 0) {
                return res != 1;
            }
        }
    }

    boolean runMultiple(int iterations) {
        if (!initialized) {
            this.initialize();
            this.clear();
        }

        for (int i = 0; i < iterations; i++) {
            int res = runOnce();

            if (res != 0) {
                return res != 1;
            }
        }

        return true;
    }

    int runOnce() {
        if (!initialized) {
            this.initialize();
            this.clear();
        }

        int collapse = collapse();

        if (collapse != 0) {
            this.completed = true;
            this.initialized = false;

            return collapse;
        }

        this.propagate();

        return 0;
    }

    void initialize() {
        this.grid = new boolean[this.dimensions][this.tiles];
        this.tileEnablers = new int[this.dimensions][this.tiles][4];

        this.weightLogWeights = new double[this.tiles];
        this.sumOfWeights = 0;
        this.sumOfWeightLogWeights = 0;
        for (int tile = 0; tile < this.tiles; tile++) {
            this.weightLogWeights[tile] = this.frequencyHints.get(tile) * Math.log(this.frequencyHints.get(tile));
            this.sumOfWeights += this.frequencyHints.get(tile);
            this.sumOfWeightLogWeights += this.weightLogWeights[tile];
        }

        this.startingEntropy = Math.log(this.sumOfWeights) - this.sumOfWeightLogWeights / this.sumOfWeights;

        this.possibleTilesPerCell = new int[this.dimensions];
        this.cachedWeights = new double[this.dimensions];
        this.cachedWeightLogWeights = new double[this.dimensions];
        this.entropies = new double[this.dimensions];

        this.distribution = new int[this.tiles];

        this.removalStack = new Stack<>();
    }

    int collapse() {
        double minEntropy = 1000d;
        int minCellIndex = -1;

        for (int cell = 0; cell < this.dimensions; cell++) {
            int possibleTiles = this.possibleTilesPerCell[cell];

            if (possibleTiles == 0) return 1;

            double entropy = this.entropies[cell];

            if (possibleTiles > 1 && entropy <= minEntropy) {
                double noise = 0.000001 * Math.random();

                if (entropy + noise < minEntropy) {
                    minEntropy = entropy + noise;
                    minCellIndex = cell;
                }
            }
        }

        if (minCellIndex == -1) {
            this.output = new int[this.dimensions];
            for (int cell = 0; cell < this.dimensions; cell++) {
                for (int tile = 0; tile < this.tiles; tile++) {
                    if (this.grid[cell][tile]) {
                        this.output[cell] = tile;
                        break;
                    }
                }
            }

            return 2;
        }

        for (int tile = 0; tile < this.tiles; tile++) {
            this.distribution[tile] = this.grid[minCellIndex][tile] ? this.frequencyHints.get(tile) : 0;
        }

        int randomTile = pickRandomly();

        for (int tile = 0; tile < this.tiles; tile++) {
            if (tile != randomTile) this.ban(minCellIndex, tile, false);
        }

        return 0;
    }

    int pickRandomly() {
        double randomSpot = Math.random();

        double sum = 0;
        double remaining = 0;
        int index = 0;

        for (int j : this.distribution) {
            sum += j;
        }

        randomSpot *= sum;

        while (index < this.distribution.length) {
            remaining += this.distribution[index];
            if (randomSpot <= remaining) {
                return index;
            }
            index++;
        }

        return 0;
    }

    void propagate() {
        while (this.removalStack.size() > 0) {
            int[] removal = this.removalStack.pop();

            int cell1 = removal[0];
            int x1 = cell1 % this.width;
            int y1 = cell1 / this.width;

            for (int direction = 0; direction < 4; direction++) {
                int dx = offsetsX[direction];
                int dy = offsetsY[direction];

                int x2 = x1 + dx;
                int y2 = y1 + dy;

                if (x2 < 0) x2 += this.width;
                else if (x2 >= this.width) x2 -= this.width;
                if (y2 < 0) y2 += this.height;
                else if (y2 >= this.height) y2 -= this.height;

                int cell2 = y2 * this.width + x2;
                ArrayList<Integer> adjacentTiles = this.adjacencyRules.get(direction).get(removal[1]);
                int[][] enablers = this.tileEnablers[cell2];

                for (int tile2 : adjacentTiles) {
                    int[] enabler = enablers[tile2];
                    enabler[oppositeDirs[direction]] -= 1;
                    if (enabler[oppositeDirs[direction]] == 0) this.ban(cell2, tile2, true);
                }
            }
        }
    }

    void ban(int cell, int tile, boolean recalculate) {
        if (!this.grid[cell][tile]) return;

        int[] enablers = this.tileEnablers[cell][tile];

        for (int direction = 0; direction < 4; direction++) {
            enablers[direction] = 0;
        }

        this.grid[cell][tile] = false;

        this.removalStack.push(new int[]{cell, tile});

        this.possibleTilesPerCell[cell] -= 1;

        if (recalculate) {
            this.cachedWeights[cell] -= this.frequencyHints.get(tile);
            this.cachedWeightLogWeights[cell] -= this.weightLogWeights[tile];

            double sum = this.cachedWeights[cell];

            this.entropies[cell] = Math.log(sum) - this.cachedWeightLogWeights[cell] / sum;
        }
    }

    void clear() {
        for (int cell = 0; cell < this.dimensions; cell++) {
            for (int tile = 0; tile < this.tiles; tile++) {
                this.grid[cell][tile] = true;

                for (int direction = 0; direction < 4; direction++) {
                    this.tileEnablers[cell][tile][direction] = this.adjacencyRules.get(direction).get(tile).size();
                }
            }

            this.possibleTilesPerCell[cell] = this.tiles;
            this.cachedWeights[cell] = this.sumOfWeights;
            this.cachedWeightLogWeights[cell] = this.sumOfWeightLogWeights;
            this.entropies[cell] = this.startingEntropy;
        }

        this.initialized = true;
        this.completed = false;
    }
}
