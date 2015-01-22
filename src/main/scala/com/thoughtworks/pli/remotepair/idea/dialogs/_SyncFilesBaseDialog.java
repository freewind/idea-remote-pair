package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _SyncFilesBaseDialog extends JDialog {
    protected JButton okButton;
    protected JButton cancelButton;
    protected JButton configButton;
    protected PairDifferentFileTabs tabs = new PairDifferentFileTabs();
    protected JPanel tabsContainer;
    private JPanel contentPanel;

    public _SyncFilesBaseDialog() {
        setContentPane(contentPanel);
        tabsContainer.add(tabs.getContentPane());
    }

}
