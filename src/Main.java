public class Main {
    public static void main(String[] args) {
        // Launch the Othello game
        javax.swing.SwingUtilities.invokeLater(() -> {
            OthelloGUI game = new OthelloGUI();
            game.setVisible(true);
        });
    }
}