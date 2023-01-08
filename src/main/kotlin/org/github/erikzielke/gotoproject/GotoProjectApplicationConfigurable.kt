package org.github.erikzielke.gotoproject

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class GotoProjectApplicationConfigurable : Configurable {
    private var settingsForm: DialogPanel? = null

    override fun getDisplayName(): String = "Go To Project Window"

    override fun getHelpTopic(): String? = null

    override fun createComponent(): JComponent? {
        val state = GoToProjectApplicationComponent.instance.state
        if (settingsForm == null) {
            settingsForm = createSettingsForm(state)
        }
        return settingsForm
    }

    private fun createSettingsForm(state: GoToProjectWindowSettings) = panel {
        row {
            checkBox("Include recently opened projects").bindSelected(state::isIncludeRecent)
        }
        row {
            checkBox("Panel in search everywhere").bindSelected(state::panelInSearchEverywhere)
        }
    }

    override fun isModified(): Boolean {
        return settingsForm?.isModified() ?: false
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        settingsForm?.apply()
    }

    override fun reset() {
        settingsForm?.reset()
    }

    override fun disposeUIResources() {
        settingsForm = null
    }

}
