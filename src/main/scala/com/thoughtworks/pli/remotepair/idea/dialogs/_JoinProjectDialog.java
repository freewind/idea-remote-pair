package com.thoughtworks.pli.remotepair.idea.dialogs;

import com.thoughtworks.pli.remotepair.idea.core.ProjectWithMemberNames;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class _JoinProjectDialog extends JDialog {

    private JPanel contentPanel;
    protected JRadioButton newProjectRadio;
    protected JTextField newProjectNameTextField;
    protected JPanel existingProjectPanel;
    protected JTextField clientNameTextField;
    protected JLabel errorMessageLabel;
    protected JButton okButton;
    private ButtonGroup buttonGroup = new ButtonGroup();
    protected List<JRadioButton> projectRadios = new ArrayList<JRadioButton>();

    public _JoinProjectDialog() {
        setContentPane(contentPanel);
    }

    protected void init() {
        errorMessageLabel.setVisible(false);
        this.existingProjectPanel.setLayout(new BoxLayout(existingProjectPanel, BoxLayout.Y_AXIS));
        buttonGroup.add(newProjectRadio);
        this.newProjectRadio.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                boolean enable = newProjectRadio.isSelected();
                newProjectNameTextField.setEnabled(enable);
            }
        });

        if (projectRadios.isEmpty()) {
            newProjectRadio.setSelected(true);
            newProjectNameTextField.requestFocus();
        }
    }

    protected void generateRadio(ProjectWithMemberNames project) {
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
        errorMessageLabel.setText("Error: " + message);
        errorMessageLabel.setVisible(true);
    }

    public void hidePreErrorMessage() {
        errorMessageLabel.setVisible(false);
    }

}
