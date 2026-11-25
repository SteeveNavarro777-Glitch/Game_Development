/* FILE PURPOSE: APPLICATION | Launch the Swing window and game lifecycle.
 * Code labels and course examples: docs/CODE_GUIDE.md.
 */
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class Main {
    // [BLOCK: FUNCTION] Main.Main - Prevent instances of this static helper class.
    private Main(){}
    // [BLOCK: FUNCTION] Main.main - Entry point for this application.
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            try{
                GamePanel panel=new GamePanel();
                JFrame frame=new JFrame("Agyu: The Epic Hero of Bukidnon");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setResizable(false);frame.add(panel);frame.pack();frame.setLocationRelativeTo(null);
                frame.addWindowListener(new WindowAdapter(){
                    // [BLOCK: FUNCTION] Main.windowClosed - Handle window closed for Main.
                    @Override public void windowClosed(WindowEvent event){panel.stop();}
                });
                frame.setVisible(true);panel.requestFocusInWindow();panel.start();
            }catch(Exception exception){
                exception.printStackTrace();
                JOptionPane.showMessageDialog(null,exception.getMessage(),"Could not start Agyu",JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
