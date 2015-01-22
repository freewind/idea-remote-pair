package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;

public class _ChooseIgnoreDialog extends JDialog {
    private JPanel contentPanel;
    protected JButton guessFromGitignoreButton;
    protected JTree workingTree;
    protected JList ignoredList;
    protected JButton moveToIgnoredButton;
    protected JButton restoreFromIgnoredButton;
    protected JButton okButton;
    protected JButton closeButton;

    public _ChooseIgnoreDialog() {
        setContentPane(contentPanel);
    }
}
