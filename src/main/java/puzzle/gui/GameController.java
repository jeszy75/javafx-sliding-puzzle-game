package puzzle.gui;

import java.util.Optional;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import org.tinylog.Logger;

import puzzle.state.Direction;
import puzzle.state.Position;
import puzzle.state.PuzzleState;

public class GameController {

    @FXML
    private GridPane grid;

    @FXML
    private TextField numberOfMovesField;

    private ImageView[] pieceViews;

    private PuzzleState state;

    private IntegerProperty numberOfMoves = new SimpleIntegerProperty();
    private BooleanProperty gameOver = new SimpleBooleanProperty();

    @FXML
    private void initialize() {
        createBindings();
        loadImages();
        populateGrid();
        resetGame();
        registerHandlersAndListeners();
    }

    private void createBindings() {
        numberOfMovesField.textProperty().bind(numberOfMoves.asString());
    }

    private void resetGame() {
        state = new PuzzleState();
        numberOfMoves.set(0);
        gameOver.set(state.isGoal());
        clearGrid();
        showStateOnGrid();
    }

    private void registerHandlersAndListeners() {
        gameOver.addListener(this::handleGameOver);
        registerKeyEventHandler();
    }

    private void registerKeyEventHandler() {
        KeyCombination restartKeyCombination = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
        KeyCombination quitKeyCombination = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);
        // Must use Platform.runLater() because getScene() returns null when
        // this method is called from initialize()
        Platform.runLater(() -> grid.getScene().setOnKeyPressed(
                keyEvent -> {
                    if (restartKeyCombination.match(keyEvent)) {
                        Logger.debug("Restarting game...");
                        resetGame();
                    } else if (quitKeyCombination.match(keyEvent)) {
                        Logger.debug("Exiting...");
                        Platform.exit();
                    } else if (keyEvent.getCode() == KeyCode.UP) {
                        Logger.debug("Up arrow pressed");
                        performMove(Direction.UP);
                    } else if (keyEvent.getCode() == KeyCode.RIGHT) {
                        Logger.debug("Right arrow pressed");
                        performMove(Direction.RIGHT);
                    } else if (keyEvent.getCode() == KeyCode.DOWN) {
                        Logger.debug("Down arrow pressed");
                        performMove(Direction.DOWN);
                    } else if (keyEvent.getCode() == KeyCode.LEFT) {
                        Logger.debug("Left arrow pressed");
                        performMove(Direction.LEFT);
                    }
                }
        ));
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        var source = (Node) event.getSource();
        var row = GridPane.getRowIndex(source);
        var col = GridPane.getColumnIndex(source);
        Logger.debug("Click on square (%d,%d)", row, col);
        var direction = getDirectionFromClickPosition(row, col);
        direction.ifPresentOrElse(this::performMove,
                () -> Logger.warn("Click does not correspond to any direction"));
    }

    private void performMove(Direction direction) {
        if (state.canMove(direction)) {
            Logger.info("Move: {}", direction);
            var oldState = state.clone();
            state.move(direction);
            Logger.trace("New state: {}", state);
            updateStateOnGrid(oldState, state);
            numberOfMoves.set(numberOfMoves.get() + 1);
            if (state.isGoal()) {
                gameOver.set(true);
            }
        } else {
            Logger.warn("Invalid move: {}", direction);
        }
    }

    private void handleGameOver(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
        if (newValue) {
            var alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Game Over");
            alert.setContentText("Congratulations, you have solved the puzzle!");
            alert.showAndWait();
            resetGame();
        }
    }

    private void populateGrid() {
        for (var row = 0; row < grid.getRowCount(); row++) {
            for (var col = 0; col < grid.getColumnCount(); col++) {
                var square = new StackPane();
                square.getStyleClass().add("square");
                square.getStyleClass().add((row + col) % 2 == 0 ? "light": "dark");
                square.setOnMouseClicked(this::handleMouseClick);
                grid.add(square, col, row);
            }
        }
    }

    private void clearGrid() {
        for (var row = 0; row < 3; row++) {
            for (var col = 0; col < 3; col++) {
                getGridNodeAtPosition(grid, row, col)
                        .ifPresent(node -> ((StackPane) node).getChildren().clear());
            }
        }
    }

    private void showStateOnGrid() {
        for (var i = 0; i < 4; i++) {
            var pos = state.getPosition(i);
            var pieceView = pieceViews[i];
            getGridNodeAtPosition(grid, pos)
                    .ifPresent(node -> ((StackPane) node).getChildren().add(pieceView));
        }
    }

    private void updateStateOnGrid(PuzzleState oldState, PuzzleState newState) {
        for (var i = 0; i < 4; i++) {
            var oldPos = oldState.getPosition(i);
            var newPos = newState.getPosition(i);
            if (!newPos.equals(oldPos)) {
                Logger.trace("Piece {} has been moved from {} to {}", i, oldPos, newPos);
                movePieceOnGrid(i, oldPos, newPos);
            }
        }
    }

    private void movePieceOnGrid(int n, Position from, Position to) {
        var pieceView = pieceViews[n];
        getGridNodeAtPosition(grid, from)
                .ifPresent(node -> ((StackPane) node).getChildren().remove(pieceView));
        getGridNodeAtPosition(grid, to)
                .ifPresent(node -> ((StackPane) node).getChildren().add(pieceView));
    }

    private void loadImages() {
        pieceViews = Stream.of("block.png", "red-shoe.png", "blue-shoe.png", "black-shoe.png")
                .map(s -> "/images/" + s)
                .peek(s -> Logger.debug("Loading image resource {}", s))
                .map(s -> {
                    var image = new Image(s);
                    return new ImageView(image);
                })
                .toArray(ImageView[]::new);
    }

    private Optional<Direction> getDirectionFromClickPosition(int row, int col) {
        var blockPos = state.getPosition(PuzzleState.BLOCK);
        Direction direction = null;
        try {
            direction = Direction.of(row - blockPos.row(), col - blockPos.col());
        } catch (IllegalArgumentException e) {
        }
        return Optional.ofNullable(direction);
    }

    private static Optional<Node> getGridNodeAtPosition(GridPane gridPane, Position pos) {
        return getGridNodeAtPosition(gridPane, pos.row(), pos.col());
    }

    private static Optional<Node> getGridNodeAtPosition(GridPane gridPane, int row, int col) {
        return gridPane.getChildren().stream()
                .filter(child -> GridPane.getRowIndex(child) == row && GridPane.getColumnIndex(child) == col)
                .findFirst();
    }

}