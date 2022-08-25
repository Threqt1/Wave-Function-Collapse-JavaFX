package wfc.algorithm;

import wfc.WFCImage;
import wfc.algorithm.overlapping.Pattern;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Overlapping extends BaseAlgorithm {
    WFCImage rawImage;
    int dataWidth;
    int dataHeight;
    int canvasPixelSize;
    int setVariation;
    int ground;
    ArrayList<int[]> colors;
    Map<String, Integer> colorMap;
    ArrayList<Pattern> patterns;
    boolean preProcessed;

    public Overlapping(WFCImage rawImage) {
        this.rawImage = rawImage;
        this.preProcessed = true;
    }

    public void loadNew(int tileSize, int canvasPixelSize, int variation, int ground, int outputWidth, int outputHeight) {
        this.canvasPixelSize = canvasPixelSize;
        this.setVariation = variation;
        this.width = outputWidth;
        this.height = outputHeight;
        this.tileSize = tileSize;
        this.ground = ground;
        this.preProcessed = false;
    }

    public boolean generate(int iterations) {
        if (!preProcessed) preProcessImage();
        if (iterations == 0) {
            return this.run();
        } else {
            return this.runMultiple(iterations);
        }
    }

    public void preProcessImage() {
        this.dataWidth = rawImage.getWidth();
        this.dataHeight = rawImage.getHeight();
        int imgDimensions = this.dataWidth * this.dataHeight;
        this.dimensions = this.width * this.height;

        int[] rawPixelArray = rawImage.getPixelData();
        int rawPixelArrayWidth = rawImage.getArrWidth();

        int[] sample = new int[imgDimensions];
        this.colors = new ArrayList<>();
        this.colorMap = new HashMap<>();
        for (int y = 0; y < this.dataHeight; y++) {
            for (int x = 0; x < this.dataWidth; x++) {
                int pixelIndex = (y * rawPixelArrayWidth + x) * 4;
                int[] color = {
                        rawPixelArray[pixelIndex],
                        rawPixelArray[pixelIndex + 1],
                        rawPixelArray[pixelIndex + 2],
                        rawPixelArray[pixelIndex + 3]
                };
                String colorIndex = "" + color[0] + "-" + color[1] + "-" + color[2] + "-" + color[3];
                if (!colorMap.containsKey(colorIndex)) {
                    colorMap.put(colorIndex, this.colors.size());
                    this.colors.add(color);
                }

                sample[y * this.dataWidth + x] = colorMap.get(colorIndex);
            }
        }

        this.patterns = new ArrayList<>();
        this.frequencyHints = new ArrayList<>();
        int patternSize = this.tileSize * this.tileSize;
        for (int y = 0; y < this.dataHeight; y++) {
            for (int x = 0; x < this.dataWidth; x++) {
                int[] rawPattern = new int[patternSize];
                for (int y1 = 0; y1 < this.tileSize; y1++) {
                    for (int x1 = 0; x1 < this.tileSize; x1++) {
                        rawPattern[y1 * tileSize + x1] = sample[((y + y1) % this.dataHeight) * this.dataWidth + ((x + x1) % this.dataWidth)];
                    }
                }

                Pattern[] variations = new Pattern[8];
                variations[0] = new Pattern(rawPattern, this.tileSize);
                variations[1] = variations[0].reflect();
                variations[2] = variations[0].rotate();
                variations[3] = variations[2].reflect();
                variations[4] = variations[2].rotate();
                variations[5] = variations[4].reflect();
                variations[6] = variations[4].rotate();
                variations[7] = variations[6].reflect();

                for (int variation = 0; variation < setVariation; variation++) {
                    boolean unique = true;
                    for (int pattern = 0; pattern < patterns.size(); pattern++) {
                        if (variations[variation].equal(this.patterns.get(pattern))) {
                            this.frequencyHints.set(pattern, this.frequencyHints.get(pattern) + 1);
                            unique = false;
                        }
                    }
                    if (!unique) continue;
                    this.frequencyHints.add(1);
                    this.patterns.add(variations[variation]);
                }
            }
        }

        this.tiles = this.patterns.size();

        this.adjacencyRules = new ArrayList<>(4);

        for (int direction = 0; direction < 4; direction++) {
            this.adjacencyRules.add(new ArrayList<>(this.tiles));
            for (int tile = 0; tile < this.tiles; tile++) {
                ArrayList<Integer> adjacent = new ArrayList<>();

                for (int tile2 = 0; tile2 < this.tiles; tile2++) {
                    if (this.patterns.get(tile).adjacent(this.patterns.get(tile2), BaseAlgorithm.offsetsX[direction], BaseAlgorithm.offsetsY[direction])) {
                        adjacent.add(tile2);
                    }
                }

                this.adjacencyRules.get(direction).add(adjacent);
            }
        }

        preProcessed = true;
    }

    @Override
    void clear() {
        super.clear();

        if (this.ground != 0) {
            for (int x = 0; x < this.width; x++) {
                for (int tile = 0; tile < this.tiles; tile++) {
                    if (tile != this.ground) {
                        this.ban((this.height - 1) * this.width + x, tile, true);
                    }
                }

                for (int y = 0; y < this.height - 1; y++) {
                    this.ban(y * this.width + x, this.ground, true);
                }
            }

            this.propagate();
        }
    }

    public ArrayList<Pattern> getPatterns() {
        if (!this.preProcessed) return null;
        return this.patterns;
    }

    public Map<String, Integer> getColorMap() {
        return this.colorMap;
    }

    public int getPatternIndex(Pattern pattern) {
        if (!this.preProcessed) return -1;
        for (int i = 0; i < this.patterns.size(); i++) {
            if (this.patterns.get(i).equal(pattern)) {
                return i;
            }
        }
        return -1;
    }

    public BufferedImage postProcessImage(int pixelSize) {
        if (!this.completed) return null;
        if (pixelSize == -1) pixelSize = this.canvasPixelSize;
        BufferedImage finalImage = new BufferedImage(width * pixelSize, height * pixelSize, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0, y1 = 0; y < this.height; y++, y1 += pixelSize) {
            for (int x = 0, x1 = 0; x < this.width; x++, x1 += pixelSize) {
                int[] color = this.colors.get(this.patterns.get(this.output[y * this.width + x]).getTopLeft());
                int ARGB = (color[0] << 16) | (color[1] << 8) | (color[2]) | (color[3] << 24);
                int endY = y1 + pixelSize;
                int endX = x1 + pixelSize;
                for (int y2 = y1; y2 < endY; y2++) {
                    for (int x2 = x1; x2 < endX; x2++) {
                        finalImage.setRGB(x2, y2, ARGB);
                    }
                }
            }
        }
        return finalImage;
    }

    public BufferedImage postProcessForSaving() {
        if (!this.completed) return null;
        BufferedImage finalImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int[] color = this.colors.get(this.patterns.get(this.output[y * this.width + x]).getTopLeft());
                int ARGB = (color[0] << 16) | (color[1] << 8) | (color[2]) | (color[3] << 24);
                finalImage.setRGB(x, y, ARGB);
            }
        }
        return finalImage;
    }

    public static BufferedImage processImportedPattern(BufferedImage initial, int canvasPixelSize) {
        BufferedImage finalImage = new BufferedImage(initial.getWidth() * canvasPixelSize, initial.getHeight() * canvasPixelSize, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0, y1 = 0; y < initial.getHeight(); y++, y1 += canvasPixelSize) {
            for (int x = 0, x1 = 0; x < initial.getWidth(); x++, x1 += canvasPixelSize) {
                int ARGB = initial.getRGB(x, y);
                int endY = y1 + canvasPixelSize;
                int endX = x1 + canvasPixelSize;
                for (int y2 = y1; y2 < endY; y2++) {
                    for (int x2 = x1; x2 < endX; x2++) {
                        finalImage.setRGB(x2, y2, ARGB);
                    }
                }
            }
        }
        return finalImage;
    }
}
