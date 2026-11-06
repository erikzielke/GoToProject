package org.github.erikzielke.gotoproject.git

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

/**
 * Lists all worktrees (the main checkout plus linked worktrees) of the git repository that contains
 * a project path, by reading the repository files directly instead of depending on Git4Idea.
 *
 * Layout used:
 * - A linked worktree's `.git` file points at `<common>/worktrees/<name>`, which has a `commondir`
 *   file pointing back at the shared git dir.
 * - Each `<common>/worktrees/<name>` directory has a `gitdir` file holding the path of that
 *   worktree's `.git` file, and its own `HEAD`.
 * - The main worktree is the parent of the shared git dir, unless the repository is bare.
 */
object GitWorktreeResolver {
    data class Worktree(
        val path: Path,
        val branchName: String?,
        val isMain: Boolean,
    )

    private const val CACHE_TTL_MILLIS = 5000L
    private const val DOT_GIT = ".git"

    private val cache = ConcurrentHashMap<Path, CacheEntry>()

    private data class CacheEntry(
        val worktrees: List<Worktree>,
        val timestamp: Long,
    )

    /**
     * Returns the root of the worktree containing [projectPath] (the directory holding `.git`),
     * or null when the path is not inside a git repository.
     */
    fun findWorktreeRoot(projectPath: String?): Path? {
        if (projectPath.isNullOrBlank()) return null
        return ioOrNull { GitBranchResolver.findWorkTreeAndGitDir(Paths.get(projectPath)) }?.first
    }

    /**
     * Returns every worktree of the repository containing [projectPath], main worktree first and
     * linked worktrees sorted by path. Returns an empty list when the path is not in a repository.
     */
    fun listWorktrees(projectPath: String?): List<Worktree> {
        val commonDir =
            projectPath
                ?.takeIf { it.isNotBlank() }
                ?.let { path -> ioOrNull { GitBranchResolver.findGitDir(Paths.get(path))?.let(::resolveCommonDir) } }
        return commonDir?.let(::cachedWorktrees).orEmpty()
    }

    private fun cachedWorktrees(commonDir: Path): List<Worktree> {
        val now = System.currentTimeMillis()
        val cached = cache[commonDir]
        return if (cached != null && now - cached.timestamp < CACHE_TTL_MILLIS) {
            cached.worktrees
        } else {
            val worktrees = ioOrNull { readWorktrees(commonDir) }.orEmpty()
            cache[commonDir] = CacheEntry(worktrees, now)
            worktrees
        }
    }

    private fun resolveCommonDir(gitDir: Path): Path {
        val commonDirFile = gitDir.resolve("commondir")
        if (!Files.isRegularFile(commonDirFile)) return gitDir
        val raw = Paths.get(Files.readString(commonDirFile).trim())
        return (if (raw.isAbsolute) raw else gitDir.resolve(raw)).normalize()
    }

    private fun readWorktrees(commonDir: Path): List<Worktree> {
        val main = mainWorktree(commonDir)
        val linked =
            commonDir
                .resolve("worktrees")
                .takeIf { it.isDirectory() }
                ?.listDirectoryEntries()
                ?.filter { it.isDirectory() }
                ?.mapNotNull(::linkedWorktree)
                ?.sortedBy { it.path.toString() }
                .orEmpty()
        return listOfNotNull(main) + linked
    }

    private fun mainWorktree(commonDir: Path): Worktree? =
        commonDir
            // A bare repository has no main working tree.
            .takeIf { it.fileName?.toString() == DOT_GIT }
            ?.parent
            ?.let { root -> Worktree(root, readBranch(commonDir), isMain = true) }

    private fun linkedWorktree(adminDir: Path): Worktree? {
        val gitdirFile = adminDir.resolve("gitdir")
        val dotGit =
            gitdirFile
                .takeIf { Files.isRegularFile(it) }
                ?.let { Paths.get(Files.readString(it).trim()) }
                ?.let { raw -> (if (raw.isAbsolute) raw else adminDir.resolve(raw)).normalize() }
                // Skip stale entries whose checkout was deleted without `git worktree prune`.
                ?.takeIf { Files.exists(it) }
        return dotGit?.parent?.let { root -> Worktree(root, readBranch(adminDir), isMain = false) }
    }

    private fun readBranch(gitDir: Path): String? {
        val head = gitDir.resolve("HEAD")
        return if (Files.isRegularFile(head)) GitBranchResolver.parseHead(Files.readString(head).trim()) else null
    }

    @Suppress("SwallowedException")
    private inline fun <T> ioOrNull(block: () -> T?): T? =
        try {
            block()
        } catch (e: IOException) {
            // Unreadable or unusual repository layout - treat as "no worktrees".
            null
        }
}
