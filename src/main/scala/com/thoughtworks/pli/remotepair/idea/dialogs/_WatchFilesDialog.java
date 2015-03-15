package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _WatchFilesDialog extends JDialog {
    private JPanel contentPanel;
    protected JTree workingTree;
    protected JList watchingList;
    protected JButton watchButton;
    protected JButton deWatchButton;
    protected JButton okButton;
    protected JButton closeButton;

    public _WatchFilesDialog() {
        setContentPane(contentPanel);
    }
}
