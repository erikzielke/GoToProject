package org.github.erikzielke.gotoproject

import com.intellij.ide.RecentProjectListActionProvider
import com.intellij.ide.ReopenProjectAction
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupFactory.ActionSelectionAid.SPEEDSEARCH
import com.intellij.openapi.wm.impl.ProjectWindowAction
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
        val openProjects = ProjectManager.getInstance().openProjects

        val findLastActive: Project? = findLastActive(openProjects.toList())
        val sortedProjects =
            openProjects.sortedWith(
                compareByDescending {
                    // You can implement custom sorting logic here, e.g., by last access time
                    it.presentableUrl == findLastActive?.presentableUrl
                },
            )

        val projectPaths = mutableSetOf<String>()

        actionGroup.addSeparator("Open Projects")

        for (project in sortedProjects) {
            if (!project.isDisposed) {
                val projectPath = project.basePath ?: continue
                projectPaths.add(projectPath)

                // Create and add action for this project
                val action = ProjectWindowAction(project.name, projectPath, null)
                actionGroup.add(action)
            }
        }

        return projectPaths
    }

    private fun findLastActive(locations: List<Project>): Project? =
        locations.find {
            it.presentableUrl == instance.focusedBefore?.presentableUrl
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
