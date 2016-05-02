package org.github.erikzielke.gotoproject;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

/**
 * Created by Erik on 10-02-14.
 */
@State(name = "GoToProject", storages = {@Storage(id = "GoToProject", file = "$APP_CONFIG$/GoToProjectWindow.xml")})
public class GoToProjectApplicationComponent implements  ApplicationComponent, PersistentStateComponent<GoToProjectWindowSettings> {

    private GoToProjectWindowSettings state;
    private Project focusedBefore;


    public void initComponent() {
        if (state == null) {
            state = new GoToProjectWindowSettings();
        }
        if (state.isIncludeRecent()) {
            WindowManager.getInstance().findVisibleFrame();
        }

    }

    public void disposeComponent() {

    }

    public static GoToProjectApplicationComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(GoToProjectApplicationComponent.class);
    }


    @NotNull
    public String getComponentName() {
        return "Go To Project Window";
    }


    public Project getFocusedBefore() {
        return focusedBefore;
    }



    @Nullable
    @Override
    public GoToProjectWindowSettings getState() {
        return state;
    }

    @Override
    public void loadState(GoToProjectWindowSettings goToProjectWindowSettings) {
        this.state = goToProjectWindowSettings;
    }


    public void setFocusedBefore(Project focusedBefore) {
        this.focusedBefore = focusedBefore;
    }
}
