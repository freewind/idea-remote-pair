package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _WatchFilesDialog extends JDialog {
    private JPanel contentPanel;
    protected JTree _workingTree;
    protected JList _watchingList;
    protected JButton _watchButton;
    protected JButton _deWatchButton;
    protected JButton _okButton;
    protected JButton _closeButton;

    public _WatchFilesDialog() {
        setContentPane(contentPanel);
    }
}
