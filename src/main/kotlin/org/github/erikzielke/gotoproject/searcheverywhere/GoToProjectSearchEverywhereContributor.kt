package org.github.erikzielke.gotoproject.searcheverywhere

import com.intellij.ide.RecentProjectListActionProvider
import com.intellij.ide.ReopenProjectAction
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.impl.OpenProjectTask
import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.codeStyle.MinusculeMatcher
import com.intellij.psi.codeStyle.NameUtil
import com.intellij.util.BitUtil.isSet
import com.intellij.util.BitUtil.set
import com.intellij.util.Processor
import org.github.erikzielke.gotoproject.GoToProjectApplicationComponent
import org.github.erikzielke.gotoproject.focus.projects
import java.awt.Frame
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.ListCellRenderer

class GoToProjectSearchEverywhereContributor : SearchEverywhereContributor<Any> {
    override fun getSearchProviderId(): String = javaClass.simpleName

    override fun getGroupName() = "Projects"

    override fun getSortWeight() = 300

    override fun showInFindResults() = false

    override fun isShownInSeparateTab() = GoToProjectApplicationComponent.instance.state.showTabInSearchEverywhere

    override fun isEmptyPatternSupported() = true

    override fun fetchElements(
        pattern: String,
        progressIndicator: ProgressIndicator,
        consumer: Processor<in Any>,
    ) {
        if (!GoToProjectApplicationComponent.instance.state.showTabInSearchEverywhere) {
            return
        }

        val allRecentProjects = RecentProjectListActionProvider.getInstance().getActions(false)

        val openProjectLocations = projects.map { it.basePath }

        val recentProjectsWithoutOpened: List<ReopenProjectAction> =
            allRecentProjects
                .map { it as ReopenProjectAction }
                .filter { it.projectPath !in openProjectLocations }

        val matcher = NameUtil.buildMatcher(pattern).build()
        matcher(matcher, consumer, recentProjectsWithoutOpened, projects)
    }

    override fun processSelectedItem(
        selected: Any,
        modifiers: Int,
        searchText: String,
    ): Boolean =
        when (selected) {
            is ReopenProjectAction -> reopenProject(selected)
            is Project -> focusOpeProject(selected)
            else -> false
        }

    override fun getElementsRenderer(): ListCellRenderer<in Any> = GoToProjectProjectListCellRenderer(this)

    override fun getDataForItem(
        element: Any,
        dataId: String,
    ): Any? = null

    private fun matcher(
        matcher: MinusculeMatcher,
        consumer: Processor<in Any>,
        recentProjectsWithoutOpened: List<ReopenProjectAction>,
        openProjects: List<Project>,
    ) {
        for (project in recentProjectsWithoutOpened) {
            if (matcher.matches(project.projectName ?: "* Unknown *") && !consumer.process(project)) {
                return
            }
        }
        for (window in openProjects) {
            if (matcher.matches(window.name) && !consumer.process(window)) {
                return
            }
        }
    }

    fun focusOpeProject(project: Project): Boolean {
        val projectFrame = WindowManager.getInstance().getFrame(project) ?: return true
        val frameState = projectFrame.extendedState
        if (isSet(frameState, Frame.ICONIFIED)) {
            // restore the frame if it is minimized
            projectFrame.setExtendedState(set(frameState, Frame.ICONIFIED, false))
        }
        projectFrame.toFront()
        return true
    }

    private fun reopenProject(selected: ReopenProjectAction): Boolean {
        val file: Path = Paths.get(selected.projectPath).normalize()
        val openProjectTask = OpenProjectTask.build().withProjectToClose(null)
        ProjectUtil.openOrImport(file, openProjectTask)
        return true
    }
}
