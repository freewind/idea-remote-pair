package com.thoughtworks.pli.intellij.remotepair.actions.forms;

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
