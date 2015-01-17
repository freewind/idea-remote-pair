package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _ConnectServerDialog extends JDialog {

    protected JTextField txtHost;
    protected JTextField txtPort;
    protected JButton connectButton;
    protected JButton closeButton;
    protected JLabel message;

    protected void init() {
        this.txtHost.requestFocus();
    }

    public String getHost() {
        return txtHost.getText().trim();
    }

    public String getPort() {
        return txtPort.getText().trim();
    }

}
