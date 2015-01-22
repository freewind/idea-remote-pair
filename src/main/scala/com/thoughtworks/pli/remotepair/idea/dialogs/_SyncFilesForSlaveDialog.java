package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _SyncFilesForSlaveDialog extends JDialog {
    private JPanel contentPanel;
    protected JButton okButton;
    protected JButton cancelButton;
    protected JButton configButton;
    protected JTabbedPane tabbedPane;
    protected JList filesList;

    protected PairDifferentFileTabs tabs = new PairDifferentFileTabs();

    public _SyncFilesForSlaveDialog() {
        setContentPane(contentPanel);
    }
}
