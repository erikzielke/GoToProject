package org.github.erikzielke.gotoproject;

import com.intellij.ide.RecentProjectsManager;
import com.intellij.ide.RecentProjectsManagerBase;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.wm.impl.ProjectWindowAction;
import com.intellij.openapi.wm.impl.WindowDressing;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This action adds an action to navigate to open projects.
 */
public class GoToProject extends AnAction {
    /**
     * Finds the open project window actions available, and shows them in a speed search enabled popup.
     *
     * @param e the action event.
     */
    public void actionPerformed(AnActionEvent e) {
        AnAction[] windowActions = WindowDressing.getWindowActionGroup().getChildren(null);
        ArrayList<ProjectWindowAction> projectWindows = new ArrayList<ProjectWindowAction>();

        Project focusedBefore = GoToProjectApplicationComponent.getInstance().getFocusedBefore();

        for (AnAction windowAction : windowActions) {
            if (windowAction instanceof ProjectWindowAction) {
                ProjectWindowAction windowAction1 = (ProjectWindowAction) windowAction;
                if (focusedBefore != null && windowAction1.getProjectName().equals(focusedBefore.getName())) {
                    projectWindows.add(0, windowAction1);
                } else {
                    projectWindows.add(windowAction1);
                }
            }
        }

        DefaultActionGroup actionGroup = new DefaultActionGroup();

        actionGroup.addSeparator("Open Projects");

        for (ProjectWindowAction projectWindow : projectWindows) {
            actionGroup.add(projectWindow);
        }

        GoToProjectApplicationComponent instance = GoToProjectApplicationComponent.getInstance();
        GoToProjectWindowSettings state = instance.getState();
        if (instance != null && state != null && state.isIncludeRecent()) {
            actionGroup.addSeparator("Recent Projects");
            RecentProjectsManager projectsManager = RecentProjectsManager.getInstance();
            final AnAction[] recentProjectActions = projectsManager.getRecentProjectsActions(false);
            ArrayList<AnAction> list = new ArrayList<AnAction>(Arrays.asList(recentProjectActions));
            for (AnAction action : list) {
                actionGroup.add(action);
            }
        }

        ListPopup actionGroupPopup = JBPopupFactory.getInstance().createActionGroupPopup("Go To Project Window",
                actionGroup, e.getDataContext(), JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);

        actionGroupPopup.showCenteredInCurrentWindow(getEventProject(e));
    }

    @Nullable
    private ProjectWindowAction findWindowAction(String projectLocation) {
        if (projectLocation == null) {
            return null;
        }
        AnAction[] windowActions = WindowDressing.getWindowActionGroup().getChildren(null);
        for (AnAction child : windowActions) {
            if (!(child instanceof ProjectWindowAction)) {
                continue;
            }
            final ProjectWindowAction windowAction = (ProjectWindowAction) child;
            if (projectLocation.equals(windowAction.getProjectLocation())) {
                return windowAction;
            }
        }
        return null;
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
