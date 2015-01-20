package com.thoughtworks.pli.remotepair.idea.dialogs;

import javax.swing.*;
import java.util.List;

public class _DifferentFilesTabPane {
    private JPanel mainPanel;
    protected JList filesList;
    protected JLabel messageLabel;

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setFiles(List<String> files) {
        DefaultListModel model = new DefaultListModel();
        for (String file : files) {
            model.addElement(file);
        }
        filesList.setModel(model);
    }
}
