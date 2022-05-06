package puzzle.state;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.StringJoiner;

/**
 * Represents the state of the puzzle.
 */
public class PuzzleState implements Cloneable {

    /**
     * The size of the board.
     */
    public static final int BOARD_SIZE = 3;

    /**
     * The index of the block.
     */
    public static final int BLOCK = 0;

    /**
     * The index of the red shoe.
     */
    public static final int RED_SHOE = 1;

    /**
     * The index of the blue shoe.
     */
    public static final int BLUE_SHOE = 2;

    /**
     * The index of the black shoe.
     */
    public static final int BLACK_SHOE = 3;

    private Position[] positions;

    /**
     * Creates a {@code PuzzleState} object that corresponds to the original
     * initial state of the puzzle.
     */
    public PuzzleState() {
        this(new Position(0, 0),
                new Position(2, 0),
                new Position(1, 1),
                new Position(0, 2)
        );
    }

    /**
     * Creates a {@code PuzzleState} object initializing the positions of the
     * pieces with the positions specified. The constructor expects an array of
     * four {@code Position} objects or four {@code Position} objects.
     *
     * @param positions the initial positions of the pieces
     */
    public PuzzleState(Position... positions) {
        checkPositions(positions);
        this.positions = deepClone(positions);
    }

    private void checkPositions(Position[] positions) {
        if (positions.length != 4) {
            throw new IllegalArgumentException();
        }
        for (var position : positions) {
            if (!isOnBoard(position)) {
                throw new IllegalArgumentException();
            }
        }
        if (positions[BLUE_SHOE].equals(positions[BLACK_SHOE])) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * {@return a copy of the position of the piece specified}
     *
     * @param n the number of a piece
     */
    public Position getPosition(int n) {
        return positions[n].clone();
    }

    /**
     * {@return whether the puzzle is solved}
     */
    public boolean isGoal() {
        return haveEqualPositions(RED_SHOE, BLUE_SHOE);
    }

    /**
     * {@return whether the block can be moved to the direction specified}
     *
     * @param direction a direction to which the block is intended to be moved
     */
    public boolean canMove(Direction direction) {
        return switch (direction) {
            case UP -> canMoveUp();
            case RIGHT -> canMoveRight();
            case DOWN -> canMoveDown();
            case LEFT -> canMoveLeft();
        };
    }

    private boolean canMoveUp() {
        return positions[BLOCK].row() > 0 && isEmpty(positions[BLOCK].getUp());
    }

    private boolean canMoveRight() {
        if (positions[BLOCK].col() == BOARD_SIZE - 1) {
            return false;
        }
        var right = positions[BLOCK].getRight();
        return isEmpty(right) || (positions[BLACK_SHOE].equals(right) && !haveEqualPositions(BLOCK, BLUE_SHOE));
    }

    private boolean canMoveDown() {
        if (positions[BLOCK].row() == BOARD_SIZE - 1) {
            return false;
        }
        var down = positions[BLOCK].getDown();
        if (isEmpty(down)) {
            return true;
        }
        if (haveEqualPositions(BLACK_SHOE, BLOCK) || positions[BLACK_SHOE].equals(down)) {
            return false;
        }
        return positions[BLUE_SHOE].equals(down) || (positions[RED_SHOE].equals(down) && !haveEqualPositions(BLUE_SHOE, BLOCK));
    }

    private boolean canMoveLeft() {
        return positions[BLOCK].col() > 0 && isEmpty(positions[BLOCK].getLeft());
    }

    /**
     * Moves the block to the direction specified.
     *
     * @param direction the direction to which the block is moved
     */
    public void move(Direction direction) {
        switch (direction) {
            case UP -> moveUp();
            case RIGHT -> moveRight();
            case DOWN -> moveDown();
            case LEFT -> moveLeft();
        }
    }

    private void moveUp() {
        if (haveEqualPositions(BLACK_SHOE, BLOCK)) {
            if (haveEqualPositions(RED_SHOE, BLOCK)) {
                positions[RED_SHOE].setUp();
            }
            positions[BLACK_SHOE].setUp();
        }
        positions[BLOCK].setUp();
    }

    private void moveRight() {
        move(Direction.RIGHT, RED_SHOE, BLUE_SHOE, BLACK_SHOE);
    }

    private void moveDown() {
        move(Direction.DOWN, RED_SHOE, BLUE_SHOE, BLACK_SHOE);
    }

    private void moveLeft() {
        move(Direction.LEFT, RED_SHOE, BLUE_SHOE);
    }

    /**
     * Moves the block to the direction specified and also any of the shoes
     * specified that are at the same position with the block.
     *
     * @param direction the direction to which the block is moved
     * @param shoes the shoes that must be moved together with the block
     */
    private void move(Direction direction, int... shoes) {
        for (var i : shoes) {
            if (haveEqualPositions(i, BLOCK)) {
                positions[i].setTo(direction);
            }
        }
        positions[BLOCK].setTo(direction);
    }

    /**
     * {@return the set of directions to which the block can be moved}
     */
    public EnumSet<Direction> getLegalMoves() {
        var legalMoves = EnumSet.noneOf(Direction.class);
        for (var direction : Direction.values()) {
            if (canMove(direction)) {
                legalMoves.add(direction);
            }
        }
        return legalMoves;
    }

    private boolean haveEqualPositions(int i, int j) {
        return positions[i].equals(positions[j]);
    }

    private boolean isOnBoard(Position position) {
        return position.row() >= 0 && position.row() < BOARD_SIZE &&
                position.col() >= 0 && position.col() < BOARD_SIZE;
    }

    private boolean isEmpty(Position position) {
        for (var p : positions) {
            if (p.equals(position)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        return (o instanceof PuzzleState ps) && Arrays.equals(positions, ps.positions);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(positions);
    }

    @Override
    public PuzzleState clone() {
        PuzzleState copy;
        try {
            copy = (PuzzleState) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
        copy.positions = deepClone(positions);
        return copy;
    }

    @Override
    public String toString() {
        var sj = new StringJoiner(",", "[", "]");
        for (var position : positions) {
           sj.add(position.toString());
        }
        return sj.toString();
    }

    private static Position[] deepClone(Position[] a) {
        Position[] copy = a.clone();
        for (var i = 0; i < a.length; i++) {
            copy[i] = a[i].clone();
        }
        return copy;
    }

}
