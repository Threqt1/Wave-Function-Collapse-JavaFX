package wfc.algorithm.overlapping;

public class Pattern {
    int[] pattern;
    int tileSize;

    public Pattern(int[] pattern, int tileSize) {
        this.pattern = pattern;
        this.tileSize = tileSize;
    }

    public int[] getPattern() {
        return this.pattern;
    }

    public int getTopLeft() {
        return this.pattern[0];
    }

    public Pattern rotate() {
        int[] newPattern = new int[this.pattern.length];
        for (int y = 0; y < tileSize; y++) {
            for (int x = 0; x < tileSize; x++) {
                newPattern[y * tileSize + x] = this.pattern[x * tileSize + (tileSize - 1 - y)];
            }
        }
        return new Pattern(newPattern, tileSize);
    }

    public Pattern reflect() {
        int[] newPattern = new int[this.pattern.length];
        for (int y = 0; y < tileSize; y++) {
            for (int x = 0; x < tileSize; x++) {
                newPattern[y * tileSize + x] = this.pattern[y * tileSize + (tileSize - 1 - x)];
            }
        }
        return new Pattern(newPattern, tileSize);
    }

    public boolean equal(Pattern other) {
        int[] otherPattern = other.getPattern();
        for (int i = 0; i < this.pattern.length; i++) {
            if (this.pattern[i] != otherPattern[i]) return false;
        }
        return true;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public boolean adjacent(Pattern other, int offsetX, int offsetY) {
        int[] otherPattern = other.getPattern();

        int xmin = Math.max(offsetX, 0);
        int xmax = offsetX < 0 ? offsetX + tileSize : tileSize;
        int ymin = Math.max(offsetY, 0);
        int ymax = offsetY < 0 ? offsetY + tileSize : tileSize;

        for (int y = ymin; y < ymax; y++) {
            for (int x = xmin; x < xmax; x++) {
                if (this.pattern[y * tileSize + x] != otherPattern[(y - offsetY) * tileSize + (x - offsetX)])
                    return false;
            }
        }

        return true;
    }
}
