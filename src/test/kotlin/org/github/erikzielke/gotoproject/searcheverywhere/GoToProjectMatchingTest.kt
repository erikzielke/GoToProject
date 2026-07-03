package org.github.erikzielke.gotoproject.searcheverywhere

import com.intellij.psi.codeStyle.NameUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GoToProjectMatchingTest : BasePlatformTestCase() {
    fun testFuzzyMatching() {
        val pattern = "mep"
        val matcher =
            NameUtil
                .buildMatcher("*$pattern")
                .withCaseSensitivity(NameUtil.MatchingCaseSensitivity.NONE)
                .build()

        assertTrue(matcher.matches("my-example-project"))
        assertTrue(matcher.matches("MY-EXAMPLE-PROJECT"))
        assertTrue(matcher.matches("Some My Example Project"))
    }

    fun testFuzzyMatchingWithMiddleOfWord() {
        val pattern = "ject"
        val matcher =
            NameUtil
                .buildMatcher("*$pattern")
                .withCaseSensitivity(NameUtil.MatchingCaseSensitivity.NONE)
                .build()

        assertTrue(matcher.matches("my-example-project"))
        assertTrue(matcher.matches("project"))
    }

    fun testFuzzyMatchingCaseInsensitive() {
        val pattern = "EXAMPLE"
        val matcher =
            NameUtil
                .buildMatcher("*$pattern")
                .withCaseSensitivity(NameUtil.MatchingCaseSensitivity.NONE)
                .build()

        assertTrue(matcher.matches("my-example-project"))
    }
}
