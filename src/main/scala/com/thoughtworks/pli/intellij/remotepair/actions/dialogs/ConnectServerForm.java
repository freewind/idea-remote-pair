package com.thoughtworks.pli.intellij.remotepair.actions.dialogs;

import javax.swing.*;

public class ConnectServerForm {

    private JTextField txtIp;
    private JTextField txtPort;
    private JTextField txtUsername;
    private JPanel main;

    public void init(String serverIp, int port, String username) {
        this.txtIp.setText(serverIp);
        this.txtPort.setText(String.valueOf(port));
        this.txtUsername.setText(username);
    }

    public JPanel getMain() {
        return main;
    }

    public String getIp() {
        return txtIp.getText();
    }

    public int getPor() {
        return Integer.parseInt(txtPort.getText());
    }

    public String getUsername() {
        return txtUsername.getText();
    }
}

