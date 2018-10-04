package player;

public class TargetedCell {
    int col;
    int raw;
    boolean shootStatus;

    TargetedCell (int col, int raw, boolean shootStatus) {
        this.col = col;
        this.raw = raw;
        this.shootStatus = shootStatus;
    }

    public boolean getShootStatus() {
        return shootStatus;
    }

    public int getCol() {
        return col;
    }

    public int getRaw() {
        return raw;
    }
}
