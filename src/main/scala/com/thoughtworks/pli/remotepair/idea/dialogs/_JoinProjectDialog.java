package com.thoughtworks.pli.remotepair.idea.dialogs;

import com.thoughtworks.pli.remotepair.idea.dialogs.ProjectWithMemberNames;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class _JoinProjectDialog extends JDialog {

    protected JRadioButton radioNewProject;
    protected JTextField txtNewProjectName;
    protected JPanel existingProjectPanel;
    protected JTextField txtClientName;
    protected JLabel lblErrorMessage;
    protected JButton btnOk;
    private ButtonGroup buttonGroup = new ButtonGroup();
    protected List<JRadioButton> projectRadios = new ArrayList<JRadioButton>();

    protected void init() {
        this.existingProjectPanel.setLayout(new BoxLayout(existingProjectPanel, BoxLayout.Y_AXIS));
        buttonGroup.add(radioNewProject);
        this.radioNewProject.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                boolean enable = radioNewProject.isSelected();
                txtNewProjectName.setEnabled(enable);
            }
        });

        if (projectRadios.isEmpty()) {
            radioNewProject.setSelected(true);
            txtNewProjectName.requestFocus();
        }
    }

    protected void addExistingProject(ProjectWithMemberNames project) {
        JRadioButton radio = new JRadioButton(project.projectName());
        JLabel label = new JLabel(project.memberNames().mkString(" : ", ",", ""));
        existingProjectPanel.add(newPanel(radio, label));
        projectRadios.add(radio);
        buttonGroup.add(radio);
    }

    private JPanel newPanel(JRadioButton radio, JLabel label) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(radio);
        panel.add(label);
        return panel;
    }

    public void showErrorMessage(String message) {
        lblErrorMessage.setText("Error: " + message);
        lblErrorMessage.setVisible(true);
    }

    public void hidePreErrorMessage() {
        lblErrorMessage.setVisible(false);
    }

}
