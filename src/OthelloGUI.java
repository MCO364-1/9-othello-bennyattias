import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
// import java.io.IOException;
// import java.io.File;

public class OthelloGUI extends JFrame {
    private static final int BOARD_SIZE = 8;
    private static final int CELL_SIZE = 65; // Slightly larger for better proportions
    private static final int BOARD_PIXEL_SIZE = BOARD_SIZE * CELL_SIZE;

    // Classic Othello colors
    private static final Color OTHELLO_GREEN = new Color(34, 139, 34); // Classic green
    private static final Color BOARD_COLOR = OTHELLO_GREEN;
    private static final Color GRID_COLOR = Color.BLACK;
    private static final Color VALID_MOVE_COLOR = new Color(255, 255, 0, 100); // Semi-transparent yellow
    // private static final Color HINT_COLOR = new Color(255, 0, 0, 150); // Semi-transparent red
    private static final Color BUTTON_PRIMARY = new Color(70, 130, 180); // Steel blue
    private static final Color BUTTON_DANGER = new Color(231, 76, 60); // Red
    private static final Color BUTTON_WARNING = new Color(241, 196, 15); // Yellow
    private static final Color TEXT_PRIMARY = Color.WHITE;
    private static final Color WHITE_PIECE = Color.WHITE;
    private static final Color BLACK_PIECE = Color.BLACK;

    private OthelloModel model;
    private JPanel boardPanel;
    private JLabel statusLabel;
    private JLabel scoreLabel;
    // private int[] hintMove = null; // Store the current hint move

    public OthelloGUI() {
        model = new OthelloModel();
        setupGUI();
        updateDisplay();
    }

    private void setupGUI() {
        setTitle("Othello Game - Human vs Computer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(OTHELLO_GREEN);

        // Board panel
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
            }
        };
        boardPanel.setPreferredSize(new Dimension(BOARD_PIXEL_SIZE, BOARD_PIXEL_SIZE));
        boardPanel.setBackground(OTHELLO_GREEN);
        boardPanel.addMouseListener(new BoardMouseListener());

        // Control panel (classic layout)
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlPanel.setBackground(OTHELLO_GREEN);

        // Create buttons
        // JButton hintButton = createModernButton("Show Best Move", BUTTON_WARNING);
        // JButton saveButton = createModernButton("Save Game", BUTTON_PRIMARY);
        // JButton loadButton = createModernButton("Load Game", BUTTON_PRIMARY);
        JButton newGameButton = createModernButton("New Game", BUTTON_PRIMARY);

        // Add action listeners
        // hintButton.addActionListener(e -> showBestMove()); // ADVANCED AI: Shows best strategic move as hint
        // saveButton.addActionListener(e -> saveGame());
        // loadButton.addActionListener(e -> loadGame());
        newGameButton.addActionListener(e -> newGame());

        // Add buttons to control panel
        // controlPanel.add(hintButton);
        // controlPanel.add(saveButton);
        // controlPanel.add(loadButton);
        controlPanel.add(newGameButton);

        // Status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.setBackground(OTHELLO_GREEN);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statusLabel = new JLabel("Black's turn");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(Color.WHITE);

        scoreLabel = new JLabel("Black: 2  White: 2");
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        scoreLabel.setForeground(Color.WHITE);

        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(scoreLabel, BorderLayout.SOUTH);

        // Combine control and status panels
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(OTHELLO_GREEN);
        bottomPanel.add(controlPanel, BorderLayout.NORTH);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);

        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
    }

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(color.brighter());
                } else {
                    g2d.setColor(color);
                }

                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));

                // Draw text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);

                g2d.dispose();
            }
        };

        button.setPreferredSize(new Dimension(120, 40));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);

        return button;
    }

    private void drawBoard(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw grid
        g2d.setColor(GRID_COLOR);
        g2d.setStroke(new BasicStroke(2));

        for (int i = 0; i <= BOARD_SIZE; i++) {
            g2d.drawLine(i * CELL_SIZE, 0, i * CELL_SIZE, BOARD_PIXEL_SIZE);
            g2d.drawLine(0, i * CELL_SIZE, BOARD_PIXEL_SIZE, i * CELL_SIZE);
        }

        // Draw pieces
        int[][] board = model.getBoard();
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                int x = col * CELL_SIZE + CELL_SIZE / 2;
                int y = row * CELL_SIZE + CELL_SIZE / 2;

                if (board[row][col] == OthelloModel.BLACK) {
                    drawPiece(g2d, x, y, BLACK_PIECE);
                } else if (board[row][col] == OthelloModel.WHITE) {
                    drawPiece(g2d, x, y, WHITE_PIECE);
                }
            }
        }

        // Highlight valid moves for current player
        if (!model.isGameOver()) {
            List<int[]> validMoves = model.getValidMoves(model.getCurrentPlayer());
            g2d.setColor(VALID_MOVE_COLOR);
            for (int[] move : validMoves) {
                int x = move[1] * CELL_SIZE + CELL_SIZE / 2;
                int y = move[0] * CELL_SIZE + CELL_SIZE / 2;
                g2d.fillOval(x - 15, y - 15, 30, 30);
            }
        }

        // Highlight hint move if available
        /*
        if (hintMove != null && !model.isGameOver()) {
            int x = hintMove[1] * CELL_SIZE + CELL_SIZE / 2;
            int y = hintMove[0] * CELL_SIZE + CELL_SIZE / 2;

            // Draw a red circle around the hint move
            g2d.setColor(HINT_COLOR);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(x - 20, y - 20, 40, 40);

            // Draw a smaller filled circle
            g2d.setColor(new Color(255, 0, 0, 80));
            g2d.fillOval(x - 15, y - 15, 30, 30);
        }
        */
    }

    private void drawPiece(Graphics2D g2d, int x, int y, Color color) {
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillOval(x - 18, y - 18, 36, 36);

        // Draw piece
        g2d.setColor(color);
        g2d.fillOval(x - 16, y - 16, 32, 32);

        // Draw highlight
        g2d.setColor(color.brighter());
        g2d.fillOval(x - 12, y - 12, 8, 8);

        // Draw border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawOval(x - 16, y - 16, 32, 32);
    }

    private void updateDisplay() {
        int[] score = model.getScore();
        scoreLabel.setText(String.format("Black: %d  White: %d", score[0], score[1]));

        if (model.isGameOver()) {
            int winner = model.getWinner();
            if (winner == OthelloModel.BLACK) {
                statusLabel.setText("Game Over! Black wins!");
            } else if (winner == OthelloModel.WHITE) {
                statusLabel.setText("Game Over! White wins!");
            } else {
                statusLabel.setText("Game Over! It's a tie!");
            }
        } else {
            if (model.getCurrentPlayer() == OthelloModel.BLACK) {
                if (model.currentPlayerHasNoValidMoves()) {
                    statusLabel.setText("Black has no valid moves - White's turn (Computer)");
                } else {
                    statusLabel.setText("Black's turn (Human)");
                }
            } else {
                if (model.currentPlayerHasNoValidMoves()) {
                    statusLabel.setText("White has no valid moves - Black's turn (Human)");
                } else {
                    statusLabel.setText("White's turn (Computer)");
                }
            }
        }

        boardPanel.repaint();
    }

    /*
    // ADVANCED AI: Shows the best strategic move as a hint to the player
    // Uses the model's getBestMove method to calculate the optimal move and displays it as a red highlight
    private void showBestMove() {
        if (model.isGameOver()) {
            return;
        }

        int currentPlayer = model.getCurrentPlayer();
        int[] bestMove = model.getBestMove(currentPlayer);

        if (bestMove != null) {
            hintMove = bestMove;
            boardPanel.repaint(); // Redraw to show the hint
        }
    }
    */

    /*
    private void saveGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Othello Game");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Properties files", "properties"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String filename = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filename.endsWith(".properties")) {
                    filename += ".properties";
                }
                model.saveGame(filename);
                JOptionPane.showMessageDialog(
                    this,
                    "Game saved successfully!",
                    "Save Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error saving game: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    */

    /*
    private void loadGame() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Othello Game");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Properties files", "properties"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                String filename = fileChooser.getSelectedFile().getAbsolutePath();
                model.loadGame(filename);
                // hintMove = null; // Clear hint when loading new game
                updateDisplay();
                JOptionPane.showMessageDialog(
                    this,
                    "Game loaded successfully!",
                    "Load Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error loading game: " + e.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    */

    private void newGame() {
        model = new OthelloModel();
        // hintMove = null; // Clear hint
        updateDisplay();
    }

    private void makeHumanMove(int row, int col) {
        if (model.getCurrentPlayer() == OthelloModel.BLACK && !model.isGameOver()) {
            // Check if human has valid moves
            if (model.currentPlayerHasNoValidMoves()) {
                // Human has no valid moves - show message and let computer play
                statusLabel.setText("Black has no valid moves - White's turn (Computer)");
                SwingUtilities.invokeLater(() -> {
                    makeComputerMove();
                });
                return;
            }

            if (model.makeMove(row, col, OthelloModel.BLACK)) {
                updateDisplay();

                // Check if game is over after human move
                if (!model.isGameOver()) {
                    // Computer's turn
                    SwingUtilities.invokeLater(() -> {
                        makeComputerMove();
                    });
                }
            }
        }
    }

    private void makeComputerMove() {
        if (model.getCurrentPlayer() == OthelloModel.WHITE && !model.isGameOver()) {
            // Add a small delay to make the computer move visible
            Timer timer = new Timer(800, e -> {
                // Check if computer has valid moves
                if (model.currentPlayerHasNoValidMoves()) {
                    // Computer has no valid moves - check if game is over
                    if (model.isGameOver()) {
                        // Game is over, update display will show the winner
                        updateDisplay();
                    } else {
                        // Computer skips turn, it's human's turn again
                        updateDisplay();
                        // Show a message that computer skipped turn
                        statusLabel.setText("Computer has no valid moves - Black's turn (Human)");
                    }
                } else {
                    // Computer has valid moves - make the move
                    int[] computerMove = model.getGreedyMove(OthelloModel.WHITE);
                    if (computerMove != null) {
                        model.makeMove(computerMove[0], computerMove[1], OthelloModel.WHITE);
                    }
                    updateDisplay();
                }
            });
            timer.setRepeats(false);
            timer.start();
        }
    }

    private class BoardMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (model.isGameOver()) {
                return;
            }

            int col = e.getX() / CELL_SIZE;
            int row = e.getY() / CELL_SIZE;

            if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                makeHumanMove(row, col);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // Add hover effect for valid moves
            if (!model.isGameOver()) {
                int col = e.getX() / CELL_SIZE;
                int row = e.getY() / CELL_SIZE;

                if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                    List<int[]> validMoves = model.getValidMoves(model.getCurrentPlayer());
                    for (int[] move : validMoves) {
                        if (move[0] == row && move[1] == col) {
                            setCursor(new Cursor(Cursor.HAND_CURSOR));
                            return;
                        }
                    }
                }
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            OthelloGUI gui = new OthelloGUI();
            gui.setVisible(true);
        });
    }
}