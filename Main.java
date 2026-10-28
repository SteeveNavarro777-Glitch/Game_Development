import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                GamePanel panel = new GamePanel();
                JFrame f = new JFrame("Agyu");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setResizable(false);
                f.add(panel);
                f.pack();
                f.setLocationRelativeTo(null);
                f.setVisible(true);
                panel.requestFocusInWindow();
                panel.start();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Could not start", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
