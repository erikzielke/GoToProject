package org.github.erikzielke.gotoproject;

import com.intellij.ide.RecentProjectsManager;
import com.intellij.ide.ReopenProjectAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.wm.impl.ProjectWindowAction;
import com.intellij.openapi.wm.impl.WindowDressing;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;

/**
 * This action adds an action to navigate to open projects.
 */
public class GoToProject extends AnAction {

    /**
     * Finds the open project window actions available, and shows them in a speed search enabled popup.
     *
     * @param event the action event.
     */
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        List<ProjectWindowAction> openedProjects = openedProjects();
        Set<String> openedProjectsNames = openedProjects.stream()
                                                        .map(ProjectWindowAction::getProjectName)
                                                        .collect(toSet());
        addSeparatedGroup(openedProjects, actionGroup, "Open Projects");
        List<? extends AnAction> recentProjects = skipOpened(openedProjectsNames, recentProjects());
        addSeparatedGroup(recentProjects, actionGroup, "Recent Projects");
        createPopup(event, actionGroup);
    }

    @NotNull
    private List<? extends AnAction> skipOpened(Set<String> openedProjectsNames, List<? extends AnAction> recentOpenedProjects) {
        return recentOpenedProjects
                       .stream()
                       .map(action -> (ReopenProjectAction) action)
                       .filter(projectAction -> !openedProjectsNames.contains(projectAction.getProjectName()))
                       .collect(Collectors.toList());
    }

    private void createPopup(AnActionEvent event, DefaultActionGroup actionGroup) {
        Project eventProject = getEventProject(event);
        if (eventProject == null) {
            return;
        }
        ListPopup popup = JBPopupFactory.getInstance()
                                        .createActionGroupPopup("Go To Project",
                                                                actionGroup,
                                                                event.getDataContext(),
                                                                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                                                                true);

        popup.showCenteredInCurrentWindow(eventProject);
    }

    private List<? extends AnAction> recentProjects() {
        return Optional
                       .ofNullable(GoToProjectApplicationComponent.getInstance())
                       .map(GoToProjectApplicationComponent::getState)
                       .filter(GoToProjectWindowSettings::isIncludeRecent)
                       .map(state -> RecentProjectsManager.getInstance()
                                                          .getRecentProjectsActions(false))
                       .map(Arrays::asList)
                       .orElse(emptyList());
    }

    private void addSeparatedGroup(List<? extends AnAction> actions,
                                   DefaultActionGroup actionGroup,
                                   String separatorText) {
        if (CollectionUtils.isNotEmpty(actions)) {
            actionGroup.addSeparator(separatorText);
            actionGroup.addAll(actions);
        }
    }

    @NotNull
    private List<ProjectWindowAction> openedProjects() {
        Project focusedBefore = GoToProjectApplicationComponent.getInstance()
                                                               .getFocusedBefore();
        List<ProjectWindowAction> projectWindows = new ArrayList<>();

        Arrays.stream(WindowDressing.getWindowActionGroup().getChildren(null))
              .filter(windowAction -> windowAction instanceof ProjectWindowAction)
              .map(windowAction -> (ProjectWindowAction) windowAction)
              .forEachOrdered(windowAction -> {
                  boolean isCurrentProject = focusedBefore != null
                                             && Objects.equals(windowAction.getProjectName(),
                                                               focusedBefore.getName());
                  if (isCurrentProject) {
                      projectWindows.add(0, windowAction);
                  } else {
                      projectWindows.add(windowAction);
                  }
              });
        return projectWindows;
    }

    @Override
    public boolean isDumbAware() {
        return true;
    }
}
