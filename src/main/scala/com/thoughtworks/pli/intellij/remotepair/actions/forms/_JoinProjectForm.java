package com.thoughtworks.pli.intellij.remotepair.actions.forms;

import javax.swing.*;

public class _JoinProjectForm {
    private JRadioButton radioNewProject;
    private JTextField txtNewProjectName;
    private JPanel existingProjectPanel;
    private JPanel mainPanel;

    public JPanel getExistingProjectPanel() {
        System.out.println("########## existingProjectPanel : " + existingProjectPanel);
        return existingProjectPanel;
    }

    public JTextField getTxtNewProjectName() {
        return txtNewProjectName;
    }

    public JRadioButton getRadioNewProject() {
        return radioNewProject;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

}
