package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _SyncFilesForMasterDialog extends JDialog {
    protected JButton okButton;
    protected JButton cancelButton;
    protected JButton configButton;
    protected PairDifferentFileTabs tabs = new PairDifferentFileTabs();
    protected JPanel tabsContainer;
    private JPanel contentPanel;

    public _SyncFilesForMasterDialog() {
        setContentPane(contentPanel);
    }

    public void init() {
        tabsContainer.add(tabs.getContentPane());
    }

}
