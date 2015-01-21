package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _SyncFilesForMasterDialog extends JDialog {
    protected JButton okButton;
    protected JButton buttonCancel;
    protected JButton configButton;
    protected PairDifferentFileTabs tabs = new PairDifferentFileTabs();
    protected JPanel tabsContainer;
    protected JPanel contentPanel;

    public void init() {
        tabsContainer.add(tabs.getContentPane());
    }

}
