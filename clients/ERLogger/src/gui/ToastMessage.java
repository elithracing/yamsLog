package gui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ToastMessage extends JDialog {

    static final int MAX_ROW_WIDTH = 600;
    static final int CHAR_WIDTH = 10;
    static final int CHAR_HEIGHT = 15;
    static final int VERTICAL_PADDING = 50;
    static final int HORIZONTAL_PADDING = 20;


    static final int DEFAULT_TIMEOUT = 1000;

    static private ToastMessage instance = null;

    private int milliseconds;
    private JLabel lblToastString;
    private StringBuilder text;

    static public ToastMessage getInstance() {
        if (instance == null) {
            instance = new ToastMessage();
        }
        return instance;
    }

    private ToastMessage() {
        this.milliseconds = 0;

        setBounds(0, 0, 0, HORIZONTAL_PADDING);
        setUndecorated(true);
        getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        panel.setBackground(Color.GRAY);
        panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 2));
        getContentPane().add(panel, BorderLayout.CENTER);

        lblToastString = new JLabel("");
        lblToastString.setFont(new Font("Dialog", Font.BOLD, 12));
        lblToastString.setForeground(Color.WHITE);

        text = new StringBuilder("<html>");

        setAlwaysOnTop(true);

        // Set pos at about center of screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int y = dim.height/2-MAX_ROW_WIDTH/2;
        int half = y/2;
        setLocation(dim.width/2-VERTICAL_PADDING, y+half);
        panel.add(lblToastString);
        setVisible(true);

        new Thread(){
            // TODO fix interrupt instead
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(DEFAULT_TIMEOUT);
                        milliseconds -= DEFAULT_TIMEOUT;
                        if (milliseconds < 0) {
                            setVisible(false);
                            // TODO don't allocate every time
                            text = new StringBuilder("<html>");
                            setBounds(getX(), getY(), 0, HORIZONTAL_PADDING);
                        } else {
                            setVisible(true);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    public void addMessage(String toastString) {
        int width = Math.max(getWidth(), toastString.length() * CHAR_WIDTH + VERTICAL_PADDING);
        int height = getHeight() + CHAR_HEIGHT;
        if(width > MAX_ROW_WIDTH) {
            width = MAX_ROW_WIDTH;
            //height += CHAR_HEIGHT;
        }
        setBounds(getX(), getY(), width, height);

        text.insert(text.length(), toastString);
        text.insert(text.length(), "<br/>");

        lblToastString.setText(text.toString());

        milliseconds = 3000;
    }
}
