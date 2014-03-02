package org.github.erikzielke.gotoproject;

import javax.swing.*;

/**
 * Created by Erik on 10-02-14.
 */
public class GotToProjectWindowSettingsForm {
    private JCheckBox includeRecent;
    private JPanel rootComponent;

    public JCheckBox getIncludeRecent() {
        return includeRecent;
    }

    public JPanel getRootComponent() {
        return rootComponent;
    }

    public boolean isModified(GoToProjectWindowSettings state) {
        return state.isIncludeRecent() != includeRecent.isSelected();
    }

    public void setData(GoToProjectWindowSettings data) {
        includeRecent.setSelected(data.isIncludeRecent());
    }


    public void getData(GoToProjectWindowSettings state) {
        state.setIncludeRecent(includeRecent.isSelected());
    }
}
