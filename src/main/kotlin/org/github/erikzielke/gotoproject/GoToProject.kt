package org.github.erikzielke.gotoproject

import com.intellij.ide.RecentProjectListActionProvider
import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.SPEEDSEARCH
import com.intellij.openapi.wm.impl.ProjectWindowAction
import com.intellij.openapi.wm.impl.WindowDressing
import org.github.erikzielke.gotoproject.GoToProjectApplicationComponent.Companion.instance

/**
 * This action adds an action to navigate to open projects.
 */
class GoToProject : AnAction() {
    /**
     * Finds the open project window actions available and shows them in a speed-search-enabled popup.
     *
     * @param event the action event.
     */
    override fun actionPerformed(event: AnActionEvent) {
        val actionGroup = DefaultActionGroup()
        val openProjects = addOpenProjects(actionGroup)
        addRecentProjects(instance, openProjects, actionGroup)
        showPopup(actionGroup, event)
    }

    private fun showPopup(
        actionGroup: DefaultActionGroup,
        event: AnActionEvent,
    ) {
        val eventProject = getEventProject(event) ?: return

        val jbPopupFactory = JBPopupFactory.getInstance()
        val actionGroupPopup =
            jbPopupFactory.createActionGroupPopup(
                "Go To Project Window",
                actionGroup,
                event.dataContext,
                SPEEDSEARCH,
                true,
            )
        actionGroupPopup.showCenteredInCurrentWindow(eventProject)
    }

    private fun addOpenProjects(actionGroup: DefaultActionGroup): Set<String> {
        val windowActions = WindowDressing.getWindowActionGroup().getChildren(null)
        val projectWindows = windowActions.filterIsInstance<ProjectWindowAction>().toMutableList()
        moveLastActiveToTop(projectWindows)
        actionGroup.addSeparator("Open Projects")
        actionGroup.addAll(projectWindows)
        return projectWindows.map { it.projectLocation }.toSet()
    }

    private fun moveLastActiveToTop(projectWindows: MutableList<ProjectWindowAction>) {
        val lastActive = findLastActive(projectWindows) ?: return
        projectWindows.remove(lastActive)
        projectWindows.add(0, lastActive)
    }

    private fun findLastActive(projectWindows: List<ProjectWindowAction>): ProjectWindowAction? {
        return projectWindows.find { it.projectLocation == instance.focusedBefore?.presentableUrl }
    }

    private fun addRecentProjects(
        instance: GoToProjectApplicationComponent,
        openProjects: Set<String>,
        actionGroup: DefaultActionGroup,
    ) {
        if (!instance.state.isIncludeRecent) {
            return
        }
        actionGroup.addSeparator("Recent Projects")
        val allRecentProjects = RecentProjectListActionProvider.getInstance().getActions(false)
        val recentProjectsWithoutOpened =
            allRecentProjects.map { it as ReopenProjectAction }.filter { it.projectPath !in openProjects }
        actionGroup.addAll(recentProjectsWithoutOpened)
    }

    override fun isDumbAware() = true
}
