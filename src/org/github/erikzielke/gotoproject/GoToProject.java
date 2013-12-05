package org.github.erikzielke.gotoproject;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.wm.impl.ProjectWindowAction;
import com.intellij.openapi.wm.impl.WindowDressing;
import com.intellij.ui.components.JBList;

import javax.swing.*;
import java.util.ArrayList;

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
        for (AnAction windowAction : windowActions) {
            if (windowAction instanceof ProjectWindowAction) {
                projectWindows.add((ProjectWindowAction) windowAction);
            }
        }

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (ProjectWindowAction projectWindow : projectWindows) {
            actionGroup.add(projectWindow);
        }
        ListPopup actionGroupPopup = JBPopupFactory.getInstance().createActionGroupPopup("Go To Project Window",
                actionGroup, e.getDataContext(), JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true);
        actionGroupPopup.showCenteredInCurrentWindow(getEventProject(e));
    }
}
