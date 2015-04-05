package gui;

import common.Config;
import common.Data;
import dataSource.Loader;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: Jakob lövhall
 * Date: 2013-10-12
 * Time: 20:57
 */
public class ERLoggerFrame extends JFrame {

    private static final int MENU_ROWS = 13;
    private static final String META_DATA_DIALOGUE_TITLE = "Enter info";
    private static final String CURRENT_PROJECT_PREFIX = "Current project: ";

    private final JFrame _this = this;

    private HostTextField hostTextField = new HostTextField(Config.SERVER_HOST + ":" + Config.SERVER_PORT);
    private JLabel currentProjectLabel = new JLabel("No project selected");
    private Loader loader;

    public ERLoggerFrame()
    {
        //make sure the program exits when the frame closes
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Elith Racing logger");

        RefineryUtilities.centerFrameOnScreen(this);

        loader = Config.DATA_LOADER;

        final JButton yamsConnectButton = new JButton("Connect to yamsLog");
        final JButton disConnectButton = new JButton("Disconnect");
        final JButton changeProjectButton = new JButton("Change project");
        final JButton setProjectMetadataButton = new JButton("Change project settings");
        final JButton dataRequestButton = new JButton("Request data");
        final JButton noDataRequestButton = new JButton("Request data stop");
        final JButton startButton = new JButton("Start data logging");
        final JButton stopButton = new JButton("Stop data logging");
        final JButton quitButton = new JButton("Quit");
        final JButton addGraphButton = new JButton("Add Graph");
        final JButton resetLocalData = new JButton("Reset local data (debug)");

        yamsConnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(loader.connect()) {
                    yamsConnectButton.setEnabled(false);
                    disConnectButton.setEnabled(true);
                    changeProjectButton.setEnabled(true);
                }
            }
        });

        disConnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startButton.isEnabled()) {
                    loader.disconnect();
                }
                stopButton.setEnabled(false);
                startButton.setEnabled(false);
                noDataRequestButton.setEnabled(false);
                dataRequestButton.setEnabled(false);
                changeProjectButton.setEnabled(false);
                setProjectMetadataButton.setEnabled(false);
                disConnectButton.setEnabled(false);
                yamsConnectButton.setEnabled(true);
            }
        });

        changeProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new ProjectForm(_this, META_DATA_DIALOGUE_TITLE, Config.META_LOADER);
                String currentProject = Config.META_LOADER.getCurrentProject();
                if (currentProject == null) {
                    setProjectMetadataButton.setEnabled(false);
                    dataRequestButton.setEnabled(false);
                    currentProjectLabel.setText("No project selected");
                }
                else {
                    setProjectMetadataButton.setEnabled(true);
                    dataRequestButton.setEnabled(true);
                    currentProjectLabel.setText(CURRENT_PROJECT_PREFIX + currentProject);
                }
            }
        });

        setProjectMetadataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ProjectMetadataForm(_this, META_DATA_DIALOGUE_TITLE, Config.META_LOADER);
            }
        });

        dataRequestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                loader.requestData();
                dataRequestButton.setEnabled(false);
                noDataRequestButton.setEnabled(true);
                startButton.setEnabled(true);
            }
        });

        noDataRequestButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                loader.requestDataStop();
                noDataRequestButton.setEnabled(false);
                dataRequestButton.setEnabled(true);
                startButton.setEnabled(false);
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                /* Request a name for data collection */
                String dataCollName = (String)JOptionPane.showInputDialog(
                        _this,
                        "Write name of new data collection:",
                        "New data collection",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "Name here");
                if (dataCollName != null) {
                    loader.start(dataCollName);
                    startButton.setEnabled(false);
                    stopButton.setEnabled(true);
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loader.stop();
                noDataRequestButton.doClick();
                stopButton.setEnabled(false);
                startButton.setEnabled(false);
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (yamsConnectButton.isEnabled()) {
                    if(stopButton.isEnabled()) {
                        try {
                            loader.stop();
                        }catch (Exception e1) { }
                    }
                    try {
                        loader.disconnect();
                    } catch (Exception e2) { }
                }
                try {
                    loader.dispose();
                } catch (Exception e3) { }
                System.exit(0);
            }
        });

        addGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new GraphForm();
            }
        });

        resetLocalData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Data.getInstance().purgeData();
            }
        });

        yamsConnectButton.setEnabled(true);
        disConnectButton.setEnabled(false);
        changeProjectButton.setEnabled(false);
        setProjectMetadataButton.setEnabled(false);
        dataRequestButton.setEnabled(false);
        noDataRequestButton.setEnabled(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(false);
        // Should always be enabled
        quitButton.setEnabled(true);
        addGraphButton.setEnabled(true);
        resetLocalData.setEnabled(true);

        // Server address label
        add(hostTextField);
        // Current project label
        add(currentProjectLabel);

        //adds all the buttons
        add(yamsConnectButton);
        add(disConnectButton);
        add(changeProjectButton);
        add(setProjectMetadataButton);
        add(dataRequestButton);
        add(noDataRequestButton);
        add(startButton);
        add(stopButton);
        add(quitButton);
        add(addGraphButton);
        add(resetLocalData);

        setLayout(new GridLayout(MENU_ROWS,1));

        pack();
        setVisible(true);
    }
}