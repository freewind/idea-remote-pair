package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _SyncFilesOptionDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    protected JButton btnIgnore;
    protected JPanel pairClientsToDiff;

    @Override
    public JPanel getContentPane() {
        return contentPane;
    }

    public JButton getButtonOK() {
        return buttonOK;
    }

    public JButton getButtonCancel() {
        return buttonCancel;
    }

}
