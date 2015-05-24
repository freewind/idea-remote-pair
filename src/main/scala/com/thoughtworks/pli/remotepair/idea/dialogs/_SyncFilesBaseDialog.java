package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _SyncFilesBaseDialog extends JDialog {
    protected JButton _okButton;
    protected JButton _cancelButton;
    protected JButton _configButton;
    protected PairDifferentFileTabs _tabs = new PairDifferentFileTabs();
    protected JPanel tabsContainer;
    private JPanel contentPanel;

    public _SyncFilesBaseDialog() {
        setContentPane(contentPanel);
        tabsContainer.add(_tabs.getContentPane());
    }

}
