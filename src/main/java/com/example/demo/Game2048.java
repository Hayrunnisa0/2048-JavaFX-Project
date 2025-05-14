package com.example.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Game2048 extends Application {
    private static final int SIZE = 4; //4x4 board
    private int[][] board = new int[SIZE][SIZE];
    private Label[][] labels = new Label[SIZE][SIZE];
    private int score = 0;
    private Label scoreLabel;
    private GridPane grid;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(5);
        grid.setHgap(5);
        grid.setPadding(new Insets(10));

        scoreLabel = new Label("Score: 0");
        scoreLabel.setFont(Font.font(24));
        HBox scoreBox = new HBox(scoreLabel);
        scoreBox.setAlignment(Pos.CENTER);

        Button restartButton = new Button("Start Again");

        VBox root = new VBox(scoreBox, grid, restartButton);
        root.setSpacing(10);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #faf8ef; -fx-padding: 20;");

        restartButton.setOnAction(e -> {
            resetGame();
            root.requestFocus();
        });

        Scene scene = new Scene(root, 400, 500);
        scene.setOnKeyPressed(this::handleKeyPress);

        primaryStage.setTitle("2048");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        resetGame();
    }

    private void resetGame() {
        score = 0;
        updateScore();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = 0;
                if (labels[i][j] == null) {
                    labels[i][j] = createTile();
                    grid.add(labels[i][j], j, i);
                }
                updateTile(i, j);
            }
        }
        addRandomTile();
        addRandomTile();
    }

    private Label createTile() {
        Label label = new Label();
        label.setPrefSize(80, 80);
        label.setFont(Font.font(20));
        label.setAlignment(Pos.CENTER);
        label.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        return label;
    }

    private void updateTile(int row, int col) {
        int value = board[row][col];
        Label label = labels[row][col];
        label.setText(value > 0 ? String.valueOf(value) : "");
        label.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-alignment: center;" +
                "-fx-background-color: " + getTileColor(value) + ";");
    }

    private String getTileColor(int value) {
        return switch (value) {
            case 2 -> "#eee4da";
            case 4 -> "#ede0c8";
            case 8 -> "#f2b179";
            case 16 -> "#f59563";
            case 32 -> "#f67c5f";
            case 64 -> "#f65e3b";
            case 128 -> "#edcf72";
            case 256 -> "#edcc61";
            case 512 -> "#edc850";
            case 1024 -> "#edc53f";
            case 2048 -> "#edc22e";
            default -> "#cdc1b4";
        };
    }

    private void handleKeyPress(KeyEvent event) {
        boolean moved = switch (event.getCode()) {
            case UP -> moveUp();
            case DOWN -> moveDown();
            case LEFT -> moveLeft();
            case RIGHT -> moveRight();
            default -> false;
        };

        if (moved) {
            addRandomTile();
            updateBoard();
            if (isGameOver()) {
                showGameOver();
            }
        }
    }

    private void updateBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                updateTile(i, j);
            }
        }
        updateScore();
    }

    private void updateScore() {
        scoreLabel.setText("Score: " + score);
    }

    private void showGameOver() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game is over");
        alert.setHeaderText(null);
        alert.setContentText("Game is over! Your score is: " + score);
        alert.showAndWait();
    }

    private void addRandomTile() {
        int emptyCount = 0;
        for (int[] row : board) {
            for (int val : row) {
                if (val == 0) emptyCount++;
            }
        }
        if (emptyCount == 0) return;

        int index = (int) (Math.random() * emptyCount);
        int count = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) {
                    if (count == index) {
                        board[i][j] = Math.random() < 0.9 ? 2 : 4;
                        return;
                    }
                    count++;
                }
            }
        }
    }

    private boolean moveLeft() {
        boolean moved = false;
        for (int i = 0; i < SIZE; i++) {
            int[] row = board[i];
            int[] newRow = new int[SIZE];
            int index = 0;
            boolean merged = false;

            for (int j = 0; j < SIZE; j++) {
                if (row[j] == 0) continue;
                if (!merged && index > 0 && newRow[index - 1] == row[j]) {
                    newRow[index - 1] *= 2;
                    score += newRow[index - 1];
                    merged = true;
                    moved = true;
                } else {
                    newRow[index++] = row[j];
                    merged = false;
                    if (j != index - 1) moved = true;
                }
            }
            board[i] = newRow;
        }
        return moved;
    }

    private boolean moveRight() {
        reverseRows();
        boolean moved = moveLeft();
        reverseRows();
        return moved;
    }

    private boolean moveUp() {
        transpose();
        boolean moved = moveLeft();
        transpose();
        return moved;
    }

    private boolean moveDown() {
        transpose();
        reverseRows();
        boolean moved = moveLeft();
        reverseRows();
        transpose();
        return moved;
    }

    private void transpose() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = i + 1; j < SIZE; j++) {
                int temp = board[i][j];
                board[i][j] = board[j][i];
                board[j][i] = temp;
            }
        }
    }

    private void reverseRows() {
        for (int[] row : board) {
            for (int i = 0, j = SIZE - 1; i < j; i++, j--) {
                int temp = row[i];
                row[i] = row[j];
                row[j] = temp;
            }
        }
    }

    private boolean isGameOver() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) return false;
                if (j < SIZE - 1 && board[i][j] == board[i][j + 1]) return false;
                if (i < SIZE - 1 && board[i][j] == board[i + 1][j]) return false;
            }
        }
        return true;
    }
}
