package org.github.erikzielke.gotoproject;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by ez on 02/05/16.
 */
public class GotoProjectApplicationConfigurable  implements Configurable {
    private GotToProjectWindowSettingsForm settingsForm;


    @Nls
    @Override
    public String getDisplayName() {
        return "Go To Project Window";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }


    @Nullable
    @Override
    public JComponent createComponent() {
        if (settingsForm == null) {
            settingsForm = new GotToProjectWindowSettingsForm();
        }
        return settingsForm.getRootComponent();
    }

    @Override
    public boolean isModified() {
        GoToProjectWindowSettings state = GoToProjectApplicationComponent.getInstance().getState();
        return  settingsForm.isModified(state);
    }


    @Override
    public void apply() throws ConfigurationException {
        if (settingsForm != null) {
            GoToProjectWindowSettings state = GoToProjectApplicationComponent.getInstance().getState();

            settingsForm.getData(state);
        }
    }

    @Override
    public void reset() {
        if (settingsForm != null) {
            GoToProjectWindowSettings state = GoToProjectApplicationComponent.getInstance().getState();

            settingsForm.setData(state);
        }
    }

    @Override
    public void disposeUIResources() {
        settingsForm = null;
    }



}
