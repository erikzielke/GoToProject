package org.github.erikzielke.gotoproject.searcheverywhere

import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.ide.ReopenProjectAction
import com.intellij.ide.actions.SearchEverywherePsiRenderer
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import javax.swing.JList

class GoToProjectProjectListCellRenderer(
    disposable: Disposable,
) : SearchEverywherePsiRenderer(disposable) {
    override fun customizeNonPsiElementLeftRenderer(
        renderer: ColoredListCellRenderer<*>?,
        list: JList<*>?,
        value: Any?,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean,
    ): Boolean {
        if (renderer == null || list == null) return false

        val result =
            when (value) {
                is ReopenProjectAction -> {
                    renderRecentProject(list, renderer, value)
                    true
                }

                is Project -> {
                    renderOpenProject(list, renderer, value)
                    true
                }

                else -> {
                    false
                }
            }
        return result
    }

    private fun renderOpenProject(
        list: JList<*>,
        renderer: ColoredListCellRenderer<*>,
        project: Project,
    ) {
        renderProject(list, project.name, project.basePath.toString(), renderer)
    }

    private fun renderRecentProject(
        list: JList<*>,
        renderer: ColoredListCellRenderer<*>,
        value: ReopenProjectAction,
    ) {
        renderProject(list, value.projectName ?: "* Unknown *", value.projectPath, renderer)
    }

    private fun renderProject(
        list: JList<*>,
        projectName: String,
        projectLocation: String,
        renderer: ColoredListCellRenderer<*>,
    ) {
        appendName(list, renderer, projectName)
        appendLocation(projectLocation, renderer)
        val projectOrAppIcon = RecentProjectsManagerBase.getInstanceEx().getProjectIcon(projectLocation, true)
        renderer.icon = projectOrAppIcon
    }

    private fun appendName(
        list: JList<*>,
        renderer: ColoredListCellRenderer<*>,
        projectName: String,
    ) {
        val color = list.foreground
        val nameAttributes = SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, color)
        renderer.append("$projectName ", nameAttributes)
    }

    private fun appendLocation(
        projectLocation: String,
        renderer: ColoredListCellRenderer<*>,
    ) {
        if (!StringUtil.isEmpty(projectLocation)) {
            renderer.append(projectLocation, SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, JBColor.GRAY))
        }
    }
}
