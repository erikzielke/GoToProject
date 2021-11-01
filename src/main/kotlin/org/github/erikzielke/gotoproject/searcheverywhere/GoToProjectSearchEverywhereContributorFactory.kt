package org.github.erikzielke.gotoproject.searcheverywhere

import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.openapi.actionSystem.AnActionEvent

class GoToProjectSearchEverywhereContributorFactory: SearchEverywhereContributorFactory<Any> {
    override fun createContributor(initEvent: AnActionEvent): SearchEverywhereContributor<Any> {
        return GoToProjectSearchEverywhereContributor(initEvent)
    }
}
