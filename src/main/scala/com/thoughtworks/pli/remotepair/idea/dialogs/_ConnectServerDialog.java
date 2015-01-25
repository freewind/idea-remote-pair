package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _ConnectServerDialog extends JDialog {

    protected JTextField hostTextField;
    protected JTextField portTextField;
    protected JButton connectButton;
    protected JButton closeButton;
    protected JLabel message;
    private JPanel contentPanel;

    public _ConnectServerDialog() {
        setContentPane(contentPanel);
    }

    protected void init() {
        message.setVisible(false);
        this.hostTextField.requestFocus();
    }

    public String getHost() {
        return hostTextField.getText().trim();
    }

    public String getPort() {
        return portTextField.getText().trim();
    }

}
