package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _ConnectServerDialog extends JDialog {

    protected JTextField hostTextField;
    protected JTextField portTextField;
    protected JButton createProjectButton;
    protected JLabel message;
    private JPanel contentPanel;
    private JTabbedPane tabbedPane1;
    protected JTextField joinUrlField;
    protected JButton joinButton;
    protected JTextField clientNameInJoinField;
    protected JTextField clientNameInCreationField;

    public _ConnectServerDialog() {
        setContentPane(contentPanel);
    }

    protected void init() {
        message.setVisible(false);
        this.hostTextField.requestFocus();
    }

}
