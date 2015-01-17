package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _SyncProgressDialog extends JDialog {
    private JPanel contentPane;
    protected JProgressBar progressBar;
    protected JLabel text;

    public _SyncProgressDialog() {
        setContentPane(contentPane);
        setModal(true);
    }

}
