package org.github.erikzielke.gotoproject;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Erik on 10-02-14.
 */
@State(name = "GoToProject", storages = {@Storage("$APP_CONFIG$/GoToProjectWindow.xml")})
public class GoToProjectApplicationComponent implements PersistentStateComponent<GoToProjectWindowSettings>, BaseComponent {

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

    static GoToProjectApplicationComponent getInstance() {
        return ApplicationManager.getApplication()
                                 .getComponent(GoToProjectApplicationComponent.class);
    }

    @NotNull
    public String getComponentName() {
        return "Go To Project";
    }


    Project getFocusedBefore() {
        return focusedBefore;
    }

    @Nullable
    @Override
    public GoToProjectWindowSettings getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull GoToProjectWindowSettings goToProjectWindowSettings) {
        this.state = goToProjectWindowSettings;
    }

    void setFocusedBefore(Project focusedBefore) {
        this.focusedBefore = focusedBefore;
    }
}
