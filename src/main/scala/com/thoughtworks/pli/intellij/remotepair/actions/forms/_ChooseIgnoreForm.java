package com.thoughtworks.pli.intellij.remotepair.actions.forms;

import javax.swing.*;

public class _ChooseIgnoreForm {
    private JButton guessFromGitignoreButton;
    private JTree workingTree;
    private JList ignoredList;
    private JPanel mainPanel;
    private JButton btnMoveToIgnored;
    private JButton btnRestoreFromIgnored;
    private JLabel lblTitle;

    public JButton getGuessFromGitignoreButton() {
        return guessFromGitignoreButton;
    }

    public JTree getWorkingTree() {
        return workingTree;
    }

    public JList getIgnoredList() {
        return ignoredList;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JButton getBtnMoveToIgnored() {
        return btnMoveToIgnored;
    }

    public JButton getBtnRestoreFromIgnored() {
        return btnRestoreFromIgnored;
    }

    public void setTitle(String title) {
        lblTitle.setText(title);
    }
}
