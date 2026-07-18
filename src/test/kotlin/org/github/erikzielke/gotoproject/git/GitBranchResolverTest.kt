package org.github.erikzielke.gotoproject.git

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GitBranchResolverTest {
    @Test
    fun `returns null for a directory that is not a git repository`() {
        val projectDir = createTempDir("no-git")

        val result = GitBranchResolver.resolve(projectDir.toString())

        assertNull(result)
    }

    @Test
    fun `returns null for a blank or null path`() {
        assertNull(GitBranchResolver.resolve(null))
        assertNull(GitBranchResolver.resolve(""))
        assertNull(GitBranchResolver.resolve("   "))
    }

    @Test
    fun `resolves branch name from a regular git directory`() {
        val projectDir = createTempDir("regular-repo")
        val gitDir = (projectDir / ".git").createDirectories()
        (gitDir / "HEAD").writeText("ref: refs/heads/feature/my-branch\n")

        val result = GitBranchResolver.resolve(projectDir.toString())

        assertEquals("feature/my-branch", result?.branchName)
        assertEquals(false, result?.isWorktree)
    }

    @Test
    fun `resolves short hash for a detached HEAD`() {
        val projectDir = createTempDir("detached-repo")
        val gitDir = (projectDir / ".git").createDirectories()
        (gitDir / "HEAD").writeText("abcdef1234567890\n")

        val result = GitBranchResolver.resolve(projectDir.toString())

        assertEquals("abcdef1", result?.branchName)
    }

    @Test
    fun `resolves branch and worktree flag for a linked worktree`() {
        val mainRepo = createTempDir("main-repo")
        val mainGitDir = (mainRepo / ".git").createDirectories()
        val worktreeGitDir = (mainGitDir / "worktrees" / "my-worktree").createDirectories()
        (worktreeGitDir / "HEAD").writeText("ref: refs/heads/worktree-branch\n")

        val worktreeDir = createTempDir("my-worktree-checkout")
        (worktreeDir / ".git").writeText("gitdir: ${worktreeGitDir}\n")

        val result = GitBranchResolver.resolve(worktreeDir.toString())

        assertEquals("worktree-branch", result?.branchName)
        assertTrue(result?.isWorktree == true)
    }

    @Test
    fun `finds git dir from a nested project subdirectory`() {
        val projectDir = createTempDir("nested-repo")
        val gitDir = (projectDir / ".git").createDirectories()
        (gitDir / "HEAD").writeText("ref: refs/heads/main\n")
        val subDir = (projectDir / "sub" / "module").createDirectories()

        val result = GitBranchResolver.resolve(subDir.toString())

        assertEquals("main", result?.branchName)
    }

    private fun createTempDir(prefix: String): Path =
        Files.createTempDirectory(prefix).also {
            it.toFile().deleteOnExit()
        }
}
