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

@Suppress("TooManyFunctions")
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

    @Test
    fun `keeps full ref when HEAD points outside refs heads`() {
        val projectDir = createTempDir("non-heads-ref")
        val gitDir = (projectDir / ".git").createDirectories()
        (gitDir / "HEAD").writeText("ref: refs/remotes/origin/main\n")

        val result = GitBranchResolver.resolve(projectDir.toString())

        assertEquals("refs/remotes/origin/main", result?.branchName)
    }

    @Test
    fun `returns null when HEAD file is empty`() {
        val projectDir = createTempDir("empty-head")
        val gitDir = (projectDir / ".git").createDirectories()
        (gitDir / "HEAD").writeText("\n")

        val result = GitBranchResolver.resolve(projectDir.toString())

        assertNull(result)
    }

    @Test
    fun `returns null when git directory has no HEAD file`() {
        val projectDir = createTempDir("missing-head")
        (projectDir / ".git").createDirectories()

        val result = GitBranchResolver.resolve(projectDir.toString())

        assertNull(result)
    }

    @Test
    fun `returns null when git file does not start with gitdir prefix`() {
        val projectDir = createTempDir("invalid-git-file")
        (projectDir / ".git").writeText("this is not a git file\n")

        val result = GitBranchResolver.resolve(projectDir.toString())

        assertNull(result)
    }

    @Test
    fun `returns null when git file points to a missing directory`() {
        val projectDir = createTempDir("dangling-git-file")
        (projectDir / ".git").writeText("gitdir: ${projectDir / "does-not-exist"}\n")

        val result = GitBranchResolver.resolve(projectDir.toString())

        assertNull(result)
    }

    @Test
    fun `resolves git file with a relative gitdir path`() {
        val root = createTempDir("relative-gitdir")
        val worktreeGitDir = (root / "main" / ".git" / "worktrees" / "wt").createDirectories()
        (worktreeGitDir / "HEAD").writeText("ref: refs/heads/relative-branch\n")
        val worktreeDir = (root / "wt").createDirectories()
        (worktreeDir / ".git").writeText("gitdir: ../main/.git/worktrees/wt\n")

        val result = GitBranchResolver.resolve(worktreeDir.toString())

        assertEquals("relative-branch", result?.branchName)
        assertTrue(result?.isWorktree == true)
    }

    @Test
    fun `returns cached result within the cache ttl`() {
        val projectDir = createTempDir("cached-repo")
        val gitDir = (projectDir / ".git").createDirectories()
        val headFile = gitDir / "HEAD"
        headFile.writeText("ref: refs/heads/first\n")

        val first = GitBranchResolver.resolve(projectDir.toString())
        headFile.writeText("ref: refs/heads/second\n")
        val second = GitBranchResolver.resolve(projectDir.toString())

        assertEquals("first", first?.branchName)
        assertEquals(first, second)
    }

    private fun createTempDir(prefix: String): Path =
        Files.createTempDirectory(prefix).also {
            it.toFile().deleteOnExit()
        }
}
