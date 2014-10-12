package com.thoughtworks.pli.intellij.remotepair.settings;

import org.apache.commons.lang.StringUtils;

import javax.swing.*;

public class SettingsPanel {
    private JPanel panel;
    private JPanel serverSettingsPanel;
    private JPanel clientSettingsPanel;
    private JTextField txtPort;
    private JTextField txtUsername;
    private JTextArea txtDefaultIgnoredFiles;

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

    public String[] getDefaultIgnoredFiles() {
        return txtDefaultIgnoredFiles.getText().split("\n");
    }

    public void setDefaultIgnoredFiles(String[] defaultIgnoredFiles) {
        txtDefaultIgnoredFiles.setText(StringUtils.join(defaultIgnoredFiles, "\n"));
    }
}
