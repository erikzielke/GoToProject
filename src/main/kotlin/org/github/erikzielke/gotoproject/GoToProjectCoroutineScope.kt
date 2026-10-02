package org.github.erikzielke.gotoproject

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import kotlinx.coroutines.CoroutineScope

/**
 * Application-wide coroutine scope for work that must outlive the UI that triggered it,
 * e.g. opening a project after the Search Everywhere popup (and its coroutine scope) has been closed.
 */
@Service
class GoToProjectCoroutineScope(
    val scope: CoroutineScope,
) {
    companion object {
        @JvmStatic
        val instance: GoToProjectCoroutineScope
            get() = ApplicationManager.getApplication().getService(GoToProjectCoroutineScope::class.java)
    }
}
