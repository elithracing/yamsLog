package gui;

import common.SimpleDebug;
import dataSource.MetaLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

/**
 * Created by max on 2015-03-23.
 *
 * TODO: Show current settings
 */
public class ProjectMetadataForm extends JDialog {

    private static final int TEXT_WIDTH = 20;

    private static final String DATE_PREFIX = "Date: ";
    private static final String DESCRIPTION = "Enter description";
    private static final String EMAIL = "Enter email";
    private static final String TAGS_LABEL = "Enter tags:";
    private static final String TESTERS_LABEL = "Enter testers:";
    private static final String DONE = "Done";
    private static final String DELIMS = "\n\r,";

    private JDialog _myself = this;
    private MetaLoader metaLoader;

    //private final Date date;
    private final JTextArea descrTextArea, emailTextArea, tagsTextArea, testersTextArea;

    public ProjectMetadataForm(JFrame owner, String title, final MetaLoader metaLoader) {
        super(owner, title, true);

        this.metaLoader = metaLoader;

        /*date = new Date();
        JLabel dateLabel = new JLabel(DATE_PREFIX + date.toString());
        JPanel dateLabelPane = new JPanel();
        dateLabelPane.add(dateLabel);*/

        /* Create text field for data collection description */
        descrTextArea = new JTextArea(DESCRIPTION, 1, TEXT_WIDTH);
        JPanel descriptionPane = new JPanel();
        descriptionPane.add(descrTextArea);

        /* Create text field for data collection description */
        emailTextArea = new JTextArea(EMAIL, 1, TEXT_WIDTH);
        JPanel emailPane = new JPanel();
        emailPane.add(emailTextArea);

        /* Create label for tags text area */
        final JLabel tagsLabel = new JLabel(TAGS_LABEL);
        JPanel tagsLabelPane = new JPanel();
        tagsLabelPane.add(tagsLabel);

        /* Create text area for tags*/
        tagsTextArea = new JTextArea(1, TEXT_WIDTH);
        JPanel tagsTextAreaPane = new JPanel();
        tagsTextAreaPane.add(tagsTextArea);

        /* Create label for testers text area */
        final JLabel testersLabel = new JLabel(TESTERS_LABEL);
        JPanel testersLabelPane = new JPanel();
        testersLabelPane.add(testersLabel);

        /* Create testers text area */
        testersTextArea = new JTextArea(1, TEXT_WIDTH);
        JPanel testersTextAreaPane = new JPanel();
        testersTextAreaPane.add(testersTextArea);

        /* Create finalize button */
        JButton doneButton = new JButton(DONE);
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String error = validateInput();
                if (error != null) {
                    SimpleDebug.err(error);
                    return;
                }

                parseInput();
                _myself.dispatchEvent(new WindowEvent(_myself, WindowEvent.WINDOW_CLOSING));
            }
        });
        JPanel doneButtonPane = new JPanel();
        doneButtonPane.add(doneButton);

        /* Set main layout and add components */
        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width / 2, dim.height / 2);
        //add(dateLabelPane);
        add(descriptionPane);
        add(emailPane);
        add(tagsLabelPane);
        add(tagsTextAreaPane);
        add(testersLabelPane);
        add(testersTextAreaPane);
        add(doneButtonPane);
        pack();
        setVisible(true);
    }

    /**
     * Parses text in text fields and sends to handler
     */
    private void parseInput() {
        //metaLoader.setDate(date);
        metaLoader.setDescription(descrTextArea.getText());
        metaLoader.setEmail(emailTextArea.getText());
        String[] tags = tagsTextArea.getText().split(DELIMS);
        for (String tag : tags) {
            metaLoader.addTag(tag);
        }
        String[] testers = testersTextArea.getText().split(DELIMS);
        for (String tester : testers) {
            metaLoader.addTester(tester);
        }
        metaLoader.commitMetaData();
    }

    /**
     * Checks validity of input in text fields.
     * @return Error message or null if valid
     */
    private String validateInput() {
        // TODO: Stub
        return null;
    }
}
