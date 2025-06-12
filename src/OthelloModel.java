import java.util.ArrayList;
import java.util.List;
// import java.util.Properties;
// import java.io.FileInputStream;
// import java.io.FileOutputStream;
// import java.io.IOException;

public class OthelloModel {
    // Board constants
    public static final int BOARD_SIZE = 8;
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;

    // Direction arrays for checking all 8 directions
    private static final int[] ROW_DIRECTIONS = {-1, -1, -1, 0, 0, 1, 1, 1};
    private static final int[] COL_DIRECTIONS = {-1, 0, 1, -1, 1, -1, 0, 1};
    private static final int NUM_DIRECTIONS = 8;
    private static final int NUM_CORNERS = 4;

    private int[][] board;
    private int currentPlayer;
    private boolean gameOver;

    // Cache for valid moves to improve performance
    private List<int[]> cachedValidMoves = null;
    private int cachedPlayer = -1;

    public OthelloModel() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
        currentPlayer = BLACK; // Black goes first
        gameOver = false;
        invalidateCache(); // Ensure clean cache state
    }

    private void initializeBoard() {
        // Initialize all cells as empty
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = EMPTY;
            }
        }

        // Set up initial pieces
        int center = BOARD_SIZE / 2;
        board[center - 1][center - 1] = WHITE;
        board[center - 1][center] = BLACK;
        board[center][center - 1] = BLACK;
        board[center][center] = WHITE;
    }

    public int[][] getBoard() {
        int[][] copy = new int[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            copy[i] = board[i].clone();
        }
        return copy;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public List<int[]> getValidMoves(int player) {
        // Check if we have cached results for this player
        if (cachedValidMoves != null && cachedPlayer == player) {
            return new ArrayList<>(cachedValidMoves); // Return a copy
        }

        // Cache miss - calculate and store
        List<int[]> validMoves = new ArrayList<>();

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == EMPTY && isValidMove(row, col, player)) {
                    validMoves.add(new int[]{row, col});
                }
            }
        }

        // Cache the result
        cachedValidMoves = new ArrayList<>(validMoves);
        cachedPlayer = player;
        return validMoves;
    }

    private boolean isValidMove(int row, int col, int player) {
        if (!isValidPosition(row, col) || board[row][col] != EMPTY) {
            return false;
        }
        // Check all 8 directions
        for (int i = 0; i < NUM_DIRECTIONS; i++) {
            int newRow = row + ROW_DIRECTIONS[i];
            int newCol = col + COL_DIRECTIONS[i];
            if (!isValidPosition(newRow, newCol)) continue;
            // Check if we can flip pieces in this direction
            if (canFlipInDirection(row, col, ROW_DIRECTIONS[i], COL_DIRECTIONS[i], player)) {
                return true;
            }
        }
        return false;
    }

    private boolean canFlipInDirection(int startRow, int startCol, int rowDir, int colDir, int player) {
        if (!isValidPosition(startRow, startCol)) {
            return false;
        }
        int opponent = (player == BLACK) ? WHITE : BLACK;
        int currentRow = startRow + rowDir;
        int currentCol = startCol + colDir;
        // Must have at least one opponent piece adjacent
        if (!isValidPosition(currentRow, currentCol) || board[currentRow][currentCol] != opponent) {
            return false;
        }
        // Continue in the direction until we find our own piece or an empty cell
        while (isValidPosition(currentRow, currentCol)) {
            if (board[currentRow][currentCol] == EMPTY) {
                return false;
            }
            if (board[currentRow][currentCol] == player) {
                return true;
            }
            currentRow += rowDir;
            currentCol += colDir;
        }
        return false;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    public boolean makeMove(int row, int col, int player) {
        if (!isValidPosition(row, col) || gameOver ||
                player != currentPlayer || board[row][col] != EMPTY ||
                !isValidMove(row, col, player)) {
            return false;
        }
        // Place the piece
        board[row][col] = player;
        // Flip opponent pieces
        flipPieces(row, col, player);
        // Invalidate cache since board changed
        invalidateCache();
        // Switch players
        currentPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
        // Check if game is over
        checkGameOver();
        return true;
    }

    private void flipPieces(int row, int col, int player) {
        int opponent = (player == BLACK) ? WHITE : BLACK;

        // Check all 8 directions
        for (int i = 0; i < NUM_DIRECTIONS; i++) {
            int rowDir = ROW_DIRECTIONS[i];
            int colDir = COL_DIRECTIONS[i];

            if (canFlipInDirection(row, col, rowDir, colDir, player)) {
                // Flip pieces in this direction
                int currentRow = row + rowDir;
                int currentCol = col + colDir;

                while (board[currentRow][currentCol] == opponent) {
                    board[currentRow][currentCol] = player;
                    currentRow += rowDir;
                    currentCol += colDir;
                }
            }
        }
    }

    private void checkGameOver() {
        // Check if current player has no valid moves
        if (!hasValidMoves(currentPlayer)) {
            // Switch to the other player to see if they have moves
            int otherPlayer = (currentPlayer == BLACK) ? WHITE : BLACK;
            if (!hasValidMoves(otherPlayer)) {
                // Neither player has valid moves - game is over
                gameOver = true;
            } else {
                // Current player has no moves, but other player does - skip turn
                currentPlayer = otherPlayer;
            }
        }
    }

    /**
     * Check if the current player has no valid moves available
     * @return true if current player has no valid moves, false otherwise
     */
    public boolean currentPlayerHasNoValidMoves() {
        if (gameOver) return true;
        return !hasValidMoves(currentPlayer);
    }

    public int[] getGreedyMove(int player) {
        List<int[]> validMoves = getValidMoves(player);

        if (validMoves.isEmpty()) {
            return null;
        }

        int[] bestMove = null;
        int maxFlips = -1;

        for (int[] move : validMoves) {
            int flips = countFlipsForMove(move[0], move[1], player);
            if (flips > maxFlips) {
                maxFlips = flips;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private int countFlipsForMove(int row, int col, int player) {
        int opponent = (player == BLACK) ? WHITE : BLACK;
        int totalFlips = 0;

        // Check all 8 directions
        for (int i = 0; i < NUM_DIRECTIONS; i++) {
            int rowDir = ROW_DIRECTIONS[i];
            int colDir = COL_DIRECTIONS[i];

            if (canFlipInDirection(row, col, rowDir, colDir, player)) {
                // Count pieces that would be flipped in this direction
                int currentRow = row + rowDir;
                int currentCol = col + colDir;
                int directionFlips = 0;

                while (board[currentRow][currentCol] == opponent) {
                    directionFlips++;
                    currentRow += rowDir;
                    currentCol += colDir;
                }

                totalFlips += directionFlips;
            }
        }

        return totalFlips;
    }

    public int[] getScore() {
        int blackCount = 0;
        int whiteCount = 0;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == BLACK) {
                    blackCount++;
                } else if (board[i][j] == WHITE) {
                    whiteCount++;
                }
            }
        }

        return new int[]{blackCount, whiteCount};
    }

    public int getWinner() {
        if (!gameOver) {
            return EMPTY;
        }

        int[] score = getScore();
        if (score[0] > score[1]) {
            return BLACK;
        } else if (score[1] > score[0]) {
            return WHITE;
        } else {
            return EMPTY; // Tie
        }
    }

    public void endGame() {
        gameOver = true;
    }

    /*
    // ADVANCED AI: Scores a board position based on strategic factors
    // Corner pieces: +25 points (very valuable in Othello)
    // Edge pieces: +3 points each
    // Mobility: +2 points per valid move available
    // Piece count: +0.5 points per piece (less important than position)
    private double evaluatePosition(int[][] boardState, int player) {
        double score = 0.0;
        int opponent = (player == BLACK) ? WHITE : BLACK;

        // Corner bonus (corners are very valuable in Othello)
        int[] corners = {0, 0, 0, 7, 7, 0, 7, 7};
        for (int i = 0; i < NUM_CORNERS * 2; i += 2) {
            if (boardState[corners[i]][corners[i+1]] == player) {
                score += 25.0;
            } else if (boardState[corners[i]][corners[i+1]] == opponent) {
                score -= 25.0;
            }
        }

        // Edge bonus
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (boardState[0][i] == player) score += 3.0;
            else if (boardState[0][i] == opponent) score -= 3.0;

            if (boardState[7][i] == player) score += 3.0;
            else if (boardState[7][i] == opponent) score -= 3.0;

            if (boardState[i][0] == player) score += 3.0;
            else if (boardState[i][0] == opponent) score -= 3.0;

            if (boardState[i][7] == player) score += 3.0;
            else if (boardState[i][7] == opponent) score -= 3.0;
        }

        // Mobility bonus (number of valid moves)
        int playerMoves = countValidMoves(boardState, player);
        int opponentMoves = countValidMoves(boardState, opponent);
        score += (playerMoves - opponentMoves) * 2.0;

        // Piece count (less important than position)
        int playerCount = 0, opponentCount = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (boardState[i][j] == player) playerCount++;
                else if (boardState[i][j] == opponent) opponentCount++;
            }
        }
        score += (playerCount - opponentCount) * 0.5;

        return score;
    }
    */

    // ADVANCED AI: Counts valid moves for a player on a given board state
    // Used by the position evaluation function to calculate mobility bonus
    // private int countValidMoves(int[][] boardState, int player) { ... }

    // ADVANCED AI: Checks if a move is valid on a given board state
    // Used by countValidMoves to validate moves on board copies
    // private boolean isValidMoveOnBoard(int[][] boardState, int row, int col, int player) { ... }

    // ADVANCED AI: Checks if pieces can be flipped in a direction on a given board state
    // Used by isValidMoveOnBoard to validate moves on board copies
    // private boolean canFlipInDirectionOnBoard(int[][] boardState, int startRow, int startCol, int rowDir, int colDir, int player) { ... }

    /*
    public void saveGame(String filename) throws IOException {
        Properties props = new Properties();

        // Save board state
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                props.setProperty("board." + i + "." + j, String.valueOf(board[i][j]));
            }
        }

        // Save game state
        props.setProperty("currentPlayer", String.valueOf(currentPlayer));
        props.setProperty("gameOver", String.valueOf(gameOver));

        // Save to file
        try (FileOutputStream out = new FileOutputStream(filename)) {
            props.store(out, "Othello Game State");
        }
    }
    */

    /*
    public void loadGame(String filename) throws IOException {
        Properties props = new Properties();

        // Load from file
        try (FileInputStream in = new FileInputStream(filename)) {
            props.load(in);
        }

        // Load board state
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = Integer.parseInt(props.getProperty("board." + i + "." + j));
            }
        }

        // Load game state
        currentPlayer = Integer.parseInt(props.getProperty("currentPlayer"));
        gameOver = Boolean.parseBoolean(props.getProperty("gameOver"));

        // Invalidate cache since board changed
        invalidateCache();
    }
    */

    private void invalidateCache() {
        cachedValidMoves = null;
        cachedPlayer = -1;
    }

    private boolean hasValidMoves(int player) {
        if (gameOver) return false;
        // Quick check - just find the first valid move
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == EMPTY && isValidMove(row, col, player)) {
                    return true; // Found one, that's enough!
                }
            }
        }
        return false;
    }
} 