package gui;

import common.Config;
import common.SimpleDebug;
import dataSource.MetaLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * Created by max on 2015-04-03.
 */
public class ProjectForm extends JDialog {

    private static final String NO_PROJECT = "No projects";
    private static final String DONE = "Done";
    private static final String ADD_PROJECT = "New project";
    private static final String REMOVE_PROJECT = "Remove";
    private static final String CANCEL = "Cancel";

    private final ProjectForm _this = this;
    private final MetaLoader _metaLoader;

    protected final JList<String> projectList;

    public ProjectForm(final JFrame owner, String title, MetaLoader metaLoader) {
        super(owner, title, true);
        _metaLoader = metaLoader;

        /* Current project label */
        String currentProject = _metaLoader.getCurrentProject();
        if (currentProject == null) { currentProject = NO_PROJECT; }
        JLabel currentProjectLabel = new JLabel(currentProject);
        JPanel currentProjectPane = new JPanel();
        currentProjectPane.add(currentProjectLabel);

        /* All projects list */
        List<String> projects = _metaLoader.getProjectNames();
        String[] projectArray = new String[projects.size()];
        projectArray = projects.toArray(projectArray);
        projectList = new JList<>(projectArray);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        projectList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        projectList.setVisibleRowCount(-1);
        JScrollPane projectListScroller = new JScrollPane(projectList);
        projectListScroller.setPreferredSize(new Dimension(250, 150));
        JPanel projectListPane = new JPanel();
        projectListPane.add(projectListScroller);

        /* Buttons */
        JButton doneButton = new JButton(DONE);
        JButton addProjectButton = new JButton(ADD_PROJECT);
        JButton removeProjectButton = new JButton(REMOVE_PROJECT);
        JButton cancelButton = new JButton(CANCEL);
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(doneButton);
        buttonPane.add(addProjectButton);
        buttonPane.add(removeProjectButton);
        buttonPane.add(cancelButton);

        /* Button event handlers */
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String name = projectList.getSelectedValue();
                if (name != null) {
                    _metaLoader.changeProject(name);
                }
                _this.dispatchEvent(new WindowEvent(_this, WindowEvent.WINDOW_CLOSING));
            }
        });
        addProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String newProjectName = (String)JOptionPane.showInputDialog(
                        _this,
                        "Write name of new project:",
                        "New project",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "New project name");
                if (newProjectName != null) {
                    _metaLoader.changeProject(newProjectName);
                    new ProjectMetadataForm(owner, "Project settings", Config.META_LOADER);
                    _this.dispatchEvent(new WindowEvent(_this, WindowEvent.WINDOW_CLOSING));
                }
            }
        });
        removeProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SimpleDebug.err("Project removal not yet implemented");
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                _this.dispatchEvent(new WindowEvent(_this, WindowEvent.WINDOW_CLOSING));
            }
        });

        /* Set up frame, add components */
        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        /* Center in screen */
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width/2, dim.height/2);
        add(currentProjectPane);
        add(projectListPane);
        add(buttonPane);
        pack();
        setVisible(true);
    }
}
