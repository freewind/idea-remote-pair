package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _SyncFilesForMasterDialog extends JDialog {
    protected JButton okButton;
    protected JButton buttonCancel;
    protected JButton configButton;
    protected PairDifferentFileTabs tabs = new PairDifferentFileTabs();
    private JPanel tabsContainer;

    public void init() {
        tabsContainer.add(tabs.getContentPane());
    }

}
