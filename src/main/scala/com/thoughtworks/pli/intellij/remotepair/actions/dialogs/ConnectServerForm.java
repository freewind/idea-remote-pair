package com.thoughtworks.pli.intellij.remotepair.actions.dialogs;

import javax.swing.*;

public class ConnectServerForm {

    private JTextField txtHost;
    private JTextField txtPort;
    private JTextField txtClientName;
    private JPanel main;
    private JLabel labelMessage;

    public void init(String serverIp, int port, String username) {
        this.txtHost.setText(serverIp);
        this.txtPort.setText(String.valueOf(port));
        this.txtClientName.setText(username);
    }

    public JPanel getMainPanel() {
        return main;
    }

    public String getHost() {
        return txtHost.getText();
    }

    public JTextField getServerHostField() {
        return txtHost;
    }

    public String getPort() {
        return txtPort.getText();
    }

    public JTextField getServerPortField() {
        return txtPort;
    }

    public JTextField getClientNameField() {
        return txtClientName;
    }

    public String getClientName() {
        return txtClientName.getText();
    }

    public void setMessage(String message) {
        labelMessage.setText(message);
    }

    public String getMessage() {
        return labelMessage.getText();
    }
}

