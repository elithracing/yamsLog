package common;

import gui.ERLoggerFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author jonasbromo
 * edited by Jakob lövhall
 */
public class ERLogger implements ActionListener {

    private  Timer timer;

    public ERLogger() {
        int delay=100;

        // Make sure Data singleton is created
        Data.getInstance();
        new ERLoggerFrame();

        timer = new Timer(delay, this);
        timer.start();
     }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | UnsupportedLookAndFeelException ex) {
                /*java.util.logging.Logger.getLogger(ERJFrame.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);*/
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ERLogger();
            }
        });
    }

    /**
     * Timer action. Read new input.
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        Data.getInstance().readInput();
    }
}
