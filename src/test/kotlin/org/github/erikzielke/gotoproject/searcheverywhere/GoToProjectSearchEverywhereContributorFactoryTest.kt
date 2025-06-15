package org.github.erikzielke.gotoproject.searcheverywhere

import com.intellij.openapi.actionSystem.AnActionEvent
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertTrue

class GoToProjectSearchEverywhereContributorFactoryTest {
    @Test
    fun `createContributor should return GoToProjectSearchEverywhereContributor`() {
        // Arrange
        val factory = GoToProjectSearchEverywhereContributorFactory()
        val mockEvent = mock<AnActionEvent>()

        // Act
        val contributor = factory.createContributor(mockEvent)

        // Assert
        assertTrue(contributor is GoToProjectSearchEverywhereContributor)
    }
}
