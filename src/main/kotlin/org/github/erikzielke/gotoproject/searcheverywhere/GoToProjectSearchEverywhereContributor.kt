package org.github.erikzielke.gotoproject.searcheverywhere

import com.intellij.ide.RecentProjectListActionProvider
import com.intellij.ide.ReopenProjectAction
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.impl.OpenProjectTask
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.wm.impl.ProjectWindowAction
import com.intellij.openapi.wm.impl.WindowDressing
import com.intellij.psi.codeStyle.MinusculeMatcher
import com.intellij.psi.codeStyle.NameUtil
import com.intellij.util.Processor
import org.github.erikzielke.gotoproject.GoToProjectApplicationComponent
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.ListCellRenderer

class GoToProjectSearchEverywhereContributor(private val initEvent: AnActionEvent) : SearchEverywhereContributor<Any> {
    override fun getSearchProviderId(): String {
        return javaClass.simpleName
    }

    override fun getGroupName() =  "Projects"

    override fun getSortWeight() = 300

    override fun showInFindResults() = false

    override fun isShownInSeparateTab() = GoToProjectApplicationComponent.instance.state.panelInSearchEverywhere

    override fun fetchElements(pattern: String, progressIndicator: ProgressIndicator, consumer: Processor<in Any>) {
        if(!GoToProjectApplicationComponent.instance.state.panelInSearchEverywhere) {
            return
        }

        val allRecentProjects = RecentProjectListActionProvider.getInstance().getActions(false)
        val windowActions = WindowDressing.windowActionGroup.getChildren(null)
        val openProjects = windowActions.filterIsInstance<ProjectWindowAction>()
        val openProjectLocations = openProjects.map { it.projectLocation }.toSet()

        val recentProjectsWithoutOpened: List<ReopenProjectAction> = allRecentProjects
            .map { it as ReopenProjectAction }
            .filter { it.projectPath !in openProjectLocations }

        val matcher = NameUtil.buildMatcher(pattern).build()
        matcher(matcher, consumer, recentProjectsWithoutOpened, openProjects)
    }

    private fun matcher(
        matcher: MinusculeMatcher,
        consumer: Processor<in Any>,
        recentProjectsWithoutOpened: List<ReopenProjectAction>,
        openProjects: List<ProjectWindowAction>
    ) {
        for (project in recentProjectsWithoutOpened) {
            if (matcher.matches(project.projectName) && !consumer.process(project)) {
                return
            }
        }
        for (window in openProjects) {
            if (matcher.matches(window.projectName) && !consumer.process(window)) {
                return
            }
        }
    }

    override fun processSelectedItem(selected: Any, modifiers: Int, searchText: String): Boolean {
        return when (selected) {
            is ReopenProjectAction -> reopenProject(selected)
            is ProjectWindowAction -> openSelectedProject(selected)
            else -> false
        }
    }

    private fun openSelectedProject(selected: ProjectWindowAction): Boolean {
        selected.setSelected(initEvent, true)
        return true
    }

    private fun reopenProject(selected: ReopenProjectAction): Boolean {
        val file: Path = Paths.get(selected.projectPath).normalize()
        val openProjectTask = OpenProjectTask.build().withProjectToClose(null)
        ProjectUtil.openOrImport(file, openProjectTask)
        return true
    }

    override fun getElementsRenderer(): ListCellRenderer<in Any> {
        return GoToProjectProjectListCellRenderer(this)
    }

    override fun getDataForItem(element: Any, dataId: String): Any? {
        return null
    }
}
