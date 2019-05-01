package org.github.erikzielke.gotoproject;

import lombok.Getter;

import javax.swing.*;

/**
 * Created by Erik on 10-02-14.
 */
@Getter
public class GotToProjectWindowSettingsForm {

    private JCheckBox includeRecent;
    private JPanel rootComponent;

    public boolean isModified(GoToProjectWindowSettings state) {
        return state != null && state.isIncludeRecent() != includeRecent.isSelected();
    }

    public void setData(GoToProjectWindowSettings data) {
        if (data != null) {
            includeRecent.setSelected(data.isIncludeRecent());
        }
    }


    public void getData(GoToProjectWindowSettings state) {
        if (state == null) {
            return;
        }
        state.setIncludeRecent(includeRecent.isSelected());
    }
}
