package org.github.erikzielke.gotoproject.focus

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.wm.WindowManager
import javax.swing.JFrame

class GoToProjectProjectManagerListener : ProjectActivity, ProjectManagerListener, DumbAware {

    override fun projectClosed(project: Project) {
        val listener = listeners[project]
        WindowManager.getInstance().getFrame(project)?.removeWindowFocusListener(listener)
    }

    override suspend fun execute(project: Project) {
        val projectFrame: JFrame? = WindowManager.getInstance().getFrame(project)
        val listener = ProjectWindowFocusListener(project)
        listeners[project] = listener
        projectFrame?.addWindowFocusListener(listener)
    }

}

val listeners = mutableMapOf<Project, ProjectWindowFocusListener>()