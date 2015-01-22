package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _SyncProgressDialog extends JDialog {
    private JPanel contentPanel;
    protected JProgressBar progressBar;
    protected JLabel messageLabel;
    protected JButton closeButton;

    public _SyncProgressDialog() {
        setContentPane(contentPanel);
    }

}
