package org.github.erikzielke.gotoproject.git

import org.github.erikzielke.gotoproject.git.GitWorktreeResolver.Worktree
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
class GitWorktreeResolverTest {
    @Test
    fun `returns empty list outside a git repository`() {
        assertTrue(GitWorktreeResolver.listWorktrees(createTempDir("no-git").toString()).isEmpty())
        assertTrue(GitWorktreeResolver.listWorktrees(null).isEmpty())
        assertTrue(GitWorktreeResolver.listWorktrees(" ").isEmpty())
    }

    @Test
    fun `lists only the main worktree for a plain repository`() {
        val repo = createRepo("plain", "main")

        val result = GitWorktreeResolver.listWorktrees(repo.toString())

        assertEquals(listOf(worktree(repo, "main", isMain = true)), result.normalized())
    }

    @Test
    fun `lists main and linked worktrees from the main checkout`() {
        val root = createTempDir("linked")
        val repo = createRepo(root, "repo", "main")
        val wtB = addWorktree(repo, root / "b-checkout", "b", "feature-b")
        val wtA = addWorktree(repo, root / "a-checkout", "a", "feature-a")

        val result = GitWorktreeResolver.listWorktrees(repo.toString())

        assertEquals(
            listOf(
                worktree(repo, "main", isMain = true),
                worktree(wtA, "feature-a", isMain = false),
                worktree(wtB, "feature-b", isMain = false),
            ),
            result.normalized(),
        )
    }

    @Test
    fun `lists the same worktrees from inside a linked worktree`() {
        val root = createTempDir("from-linked")
        val repo = createRepo(root, "repo", "main")
        val wt = addWorktree(repo, root / "wt", "wt", "feature")

        val result = GitWorktreeResolver.listWorktrees((wt / "src").createDirectories().toString())

        assertEquals(
            listOf(worktree(repo, "main", isMain = true), worktree(wt, "feature", isMain = false)),
            result.normalized(),
        )
    }

    @Test
    fun `resolves relative commondir and gitdir paths`() {
        val root = createTempDir("relative")
        val repo = createRepo(root, "repo", "main")
        val adminDir = (repo / ".git" / "worktrees" / "wt").createDirectories()
        (adminDir / "HEAD").writeText("ref: refs/heads/rel\n")
        (adminDir / "commondir").writeText("../..\n")
        (adminDir / "gitdir").writeText("../../../../wt/.git\n")
        val wt = (root / "wt").createDirectories()
        (wt / ".git").writeText("gitdir: ../repo/.git/worktrees/wt\n")

        val result = GitWorktreeResolver.listWorktrees(wt.toString())

        assertEquals(
            listOf(worktree(repo, "main", isMain = true), worktree(wt, "rel", isMain = false)),
            result.normalized(),
        )
    }

    @Test
    fun `skips linked worktrees whose checkout was deleted`() {
        val root = createTempDir("stale")
        val repo = createRepo(root, "repo", "main")
        val adminDir = (repo / ".git" / "worktrees" / "gone").createDirectories()
        (adminDir / "HEAD").writeText("ref: refs/heads/gone\n")
        (adminDir / "gitdir").writeText("${root / "gone" / ".git"}\n")

        val result = GitWorktreeResolver.listWorktrees(repo.toString())

        assertEquals(listOf(worktree(repo, "main", isMain = true)), result.normalized())
    }

    @Test
    fun `omits main worktree for a bare repository`() {
        val root = createTempDir("bare")
        val bare = (root / "repo.git").createDirectories()
        (bare / "HEAD").writeText("ref: refs/heads/main\n")
        val adminDir = (bare / "worktrees" / "wt").createDirectories()
        (adminDir / "HEAD").writeText("ref: refs/heads/feature\n")
        (adminDir / "commondir").writeText("../..\n")
        val wt = (root / "wt").createDirectories()
        (wt / ".git").writeText("gitdir: $adminDir\n")
        (adminDir / "gitdir").writeText("${wt / ".git"}\n")

        val result = GitWorktreeResolver.listWorktrees(wt.toString())

        assertEquals(listOf(worktree(wt, "feature", isMain = false)), result.normalized())
    }

    @Test
    fun `finds the worktree root from a nested directory`() {
        val repo = createRepo("root", "main")
        val nested = (repo / "a" / "b").createDirectories()

        assertEquals(repo.toRealPath(), GitWorktreeResolver.findWorktreeRoot(nested.toString())?.toRealPath())
        assertNull(GitWorktreeResolver.findWorktreeRoot(createTempDir("none").toString()))
        assertNull(GitWorktreeResolver.findWorktreeRoot(null))
    }

    private fun createRepo(
        prefix: String,
        branch: String,
    ): Path = createRepo(createTempDir(prefix), "repo", branch)

    private fun createRepo(
        parent: Path,
        name: String,
        branch: String,
    ): Path {
        val repo = (parent / name).createDirectories()
        val gitDir = (repo / ".git").createDirectories()
        (gitDir / "HEAD").writeText("ref: refs/heads/$branch\n")
        return repo
    }

    private fun addWorktree(
        repo: Path,
        checkout: Path,
        name: String,
        branch: String,
    ): Path {
        val adminDir = (repo / ".git" / "worktrees" / name).createDirectories()
        (adminDir / "HEAD").writeText("ref: refs/heads/$branch\n")
        (adminDir / "commondir").writeText("../..\n")
        (adminDir / "gitdir").writeText("${checkout / ".git"}\n")
        checkout.createDirectories()
        (checkout / ".git").writeText("gitdir: $adminDir\n")
        return checkout
    }

    private fun List<Worktree>.normalized() = map { it.copy(path = it.path.toRealPath()) }

    private fun worktree(
        path: Path,
        branch: String?,
        isMain: Boolean,
    ) = Worktree(path.toRealPath(), branch, isMain)

    private fun createTempDir(prefix: String): Path =
        Files.createTempDirectory(prefix).also {
            it.toFile().deleteOnExit()
        }
}
