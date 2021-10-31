package org.github.erikzielke.gotoproject

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@Service
@State(name = "GoToProject", storages = [Storage(value = "GoToProjectWindow.xml")])
class GoToProjectApplicationComponent : PersistentStateComponent<GoToProjectWindowSettings?> {
    private var state: GoToProjectWindowSettings = GoToProjectWindowSettings()
    var focusedBefore: Project? = null
    var lastFocusLost: Project? = null

    override fun getState(): GoToProjectWindowSettings = state

    override fun loadState(state: GoToProjectWindowSettings) {
        this.state = state
    }

    companion object {
        @JvmStatic
        val instance: GoToProjectApplicationComponent
            get() = ApplicationManager.getApplication().getService(GoToProjectApplicationComponent::class.java)
    }
}
