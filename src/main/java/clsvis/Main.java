package clsvis;

import clsvis.gui.MainFrame;
import java.awt.EventQueue;

/**
 * Application main class.
 *
 * @author Jonatan Kazmierczak [Jonatan (at) Son-of-God.info]
 */
public class Main {

    public static void main(String[] args) {
        EventQueue.invokeLater( () -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible( true );
        } );
    }
}
