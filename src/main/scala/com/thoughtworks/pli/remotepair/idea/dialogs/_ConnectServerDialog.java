package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _ConnectServerDialog extends JDialog {

    protected JTextField txtHost;
    protected JTextField txtPort;
    protected JButton connectButton;
    protected JButton closeButton;
    protected JLabel message;
    protected JPanel contentPanel;

    protected void init() {
        setContentPane(contentPanel);
        this.txtHost.requestFocus();
    }

    public String getHost() {
        return txtHost.getText().trim();
    }

    public String getPort() {
        return txtPort.getText().trim();
    }

}
