import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class Main {
    private Main(){}
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            try{
                GamePanel panel=new GamePanel();
                JFrame frame=new JFrame("Agyu: Guardians of the Lost Grove");
                frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                frame.setResizable(false);frame.add(panel);frame.pack();frame.setLocationRelativeTo(null);
                frame.addWindowListener(new WindowAdapter(){
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
