package gui;

import common.Data;
import common.ERSensorConfig;
import common.SimpleDebug;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by max on 2014-12-12.
 */
public class GraphForm extends JFrame implements ActionListener, ItemListener {

    private static final int PREF_X = 300;
    private static final int PREF_Y = 500;

    private JPanel comboBoxChoicePane = new JPanel();
    private JPanel comboBoxPane = new JPanel();
    private JPanel buttonPanel = new JPanel();

    private String currentSensor;
    private Map<String, ArrayList<JCheckBox>> sensorAttributeMap = new HashMap<>();

    public GraphForm() {

        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        /* Center in screen */
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width/2 - PREF_X/2, dim.height/2 - PREF_Y/2);

        /* Combobox setup */
        comboBoxChoicePane.setLayout(new CardLayout());
        ArrayList<String> comboBoxItems = new ArrayList<>();
        for (String sensor : Data.getInstance().getSensorNames()) {
            /* Remember name */
            comboBoxItems.add(sensor);
            /* Set up a sensor configuration */
            JPanel sensorPanel = new JPanel();
            sensorPanel.setLayout(new FlowLayout());
            /* Get all configured attributes */
            ArrayList<JCheckBox> attributes = new ArrayList<>();
            for (String attr : Data.getInstance().getAttributeNames(sensor)) {
                /* Add sensor attribute */
                JCheckBox checkBox = new JCheckBox(attr);
                attributes.add(checkBox);
                sensorPanel.add(checkBox);
            }
            sensorAttributeMap.put(sensor, attributes);
            comboBoxChoicePane.add(sensorPanel, sensor);
        }
        if (comboBoxItems.size() > 0) {
            currentSensor = comboBoxItems.get(0);
        }

        JComboBox comboBox = new JComboBox<>(comboBoxItems.toArray());
        comboBox.setEditable(false);
        comboBoxPane.add(comboBox);
        comboBox.addItemListener(this);

        /* Add choice button(s) */
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        JButton button = new JButton("Create");
        button.addActionListener(this);
        buttonPanel.add(button);

        add(comboBoxPane);
        add(comboBoxChoicePane);
        add(buttonPanel);

        setPreferredSize(new Dimension(PREF_X, PREF_Y));
        setVisible(true);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (!sensorAttributeMap.containsKey(currentSensor)) {
            SimpleDebug.err("GraphForm: Couldn't find sensor name");
            return;
        }
        ERSensorConfig config = new ERSensorConfig();
        config.sensor = currentSensor;
        ArrayList<JCheckBox> checkBoxes = sensorAttributeMap.get(currentSensor);
        ArrayList<String> attrArray = new ArrayList<>(checkBoxes.size());

        for (JCheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                attrArray.add(checkBox.getText());
            }
        }
        config.attributes = new String[attrArray.size()];
        config.attributes = attrArray.toArray(config.attributes);
        new XYSeriesFrame(config);
        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {
        CardLayout cl = (CardLayout)(comboBoxChoicePane.getLayout());
        currentSensor = (String) itemEvent.getItem();
        cl.show(comboBoxChoicePane, currentSensor);
    }
}
