package com.thoughtworks.pli.remotepair.idea.settings;

import javax.swing.*;

public class SettingsPanel {
    private JPanel panel;
    private JTextField txtPort;
    private JTextField txtUsername;

    public JPanel getPanel() {
        return panel;
    }

    public int getPort() {
        return Integer.parseInt(txtPort.getText());
    }

    public void setPort(int port) {
        this.txtPort.setText("" + port);
    }

    public String getUsername() {
        return txtUsername.getText();
    }

    public void setUsername(String username) {
        txtUsername.setText(username);
    }

}
