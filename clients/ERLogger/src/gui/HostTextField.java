package gui;

import common.Config;
import common.SimpleDebug;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by max on 2014-12-07.
 */
public class HostTextField extends JTextField implements ActionListener {

    private static final int COLUMNS = 20;

    public HostTextField(String text) {
        super(text, COLUMNS);
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String text = this.getText();
        String parsed[] = text.split(":");

        if (parsed.length == 2) {
            Config.SERVER_HOST = parsed[0];
            Config.SERVER_PORT = Integer.parseInt(parsed[1]);
        }
        else {
            SimpleDebug.err("Unrecognised hostname");
        }
    }
}

