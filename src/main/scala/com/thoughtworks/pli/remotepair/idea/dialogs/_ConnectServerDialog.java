package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _ConnectServerDialog extends JDialog {

    protected JTextField _hostTextField;
    protected JTextField _portTextField;
    protected JButton _createProjectButton;
    protected JLabel _message;
    private JPanel contentPanel;
    private JTabbedPane tabbedPane1;
    protected JTextField _joinUrlField;
    protected JButton _joinProjectButton;
    protected JTextField _clientNameInJoinField;
    protected JTextField _clientNameInCreationField;
    protected JCheckBox _readonlyCheckBox;

    public _ConnectServerDialog() {
        setContentPane(contentPanel);
    }

}
