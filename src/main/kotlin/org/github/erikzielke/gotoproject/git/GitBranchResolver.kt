package org.github.erikzielke.gotoproject.git

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

/**
 * Resolves the current git branch (or detached HEAD short hash) for a project path, without
 * depending on the Git4Idea plugin. Understands linked worktrees, whose `.git` entry is a file
 * pointing at `<main-repo>/.git/worktrees/<name>` rather than a directory.
 */
object GitBranchResolver {
    data class BranchInfo(
        val branchName: String,
        val isWorktree: Boolean,
    )

    private const val CACHE_TTL_MILLIS = 5000L
    private const val SHORT_HASH_LENGTH = 7
    private const val GIT_DIR_PREFIX = "gitdir:"
    private const val REF_PREFIX = "ref:"
    private const val HEADS_PREFIX = "refs/heads/"

    private val cache = ConcurrentHashMap<String, CacheEntry>()

    private data class CacheEntry(
        val info: BranchInfo?,
        val timestamp: Long,
    )

    fun resolve(projectPath: String?): BranchInfo? {
        if (projectPath.isNullOrBlank()) return null
        val now = System.currentTimeMillis()
        val cached = cache[projectPath]
        return if (cached != null && now - cached.timestamp < CACHE_TTL_MILLIS) {
            cached.info
        } else {
            val info = doResolve(projectPath)
            cache[projectPath] = CacheEntry(info, now)
            info
        }
    }

    @Suppress("SwallowedException")
    private fun doResolve(projectPath: String): BranchInfo? =
        try {
            val gitDir = findGitDir(Paths.get(projectPath))
            if (gitDir == null) {
                null
            } else {
                val headFile: Path = gitDir.resolve("HEAD")
                val branch = if (Files.isRegularFile(headFile)) parseHead(Files.readString(headFile).trim()) else null
                branch?.let { BranchInfo(it, isWorktree(gitDir)) }
            }
        } catch (e: IOException) {
            // Not a readable git repository (permissions, unusual layout, etc.) - just show no branch.
            null
        }

    private fun isWorktree(gitDir: Path): Boolean = gitDir.invariantPathString().contains("/worktrees/")

    private fun findGitDir(start: Path): Path? =
        generateSequence(start.toAbsolutePath().normalize()) { it.parent }.firstNotNullOfOrNull { dir ->
            val gitPath = dir.resolve(".git")
            when {
                Files.isDirectory(gitPath) -> gitPath
                Files.isRegularFile(gitPath) -> resolveGitFile(gitPath, dir)
                else -> null
            }
        }

    private fun resolveGitFile(
        gitFile: Path,
        projectDir: Path,
    ): Path? {
        val content = Files.readString(gitFile).trim()
        if (!content.startsWith(GIT_DIR_PREFIX)) return null
        val rawPath = content.removePrefix(GIT_DIR_PREFIX).trim()
        val resolved = Paths.get(rawPath)
        val gitDir = if (resolved.isAbsolute) resolved else projectDir.resolve(resolved)
        return gitDir.normalize().takeIf { Files.isDirectory(it) }
    }

    private fun parseHead(content: String): String? {
        if (content.isEmpty()) return null
        return if (content.startsWith(REF_PREFIX)) {
            content.removePrefix(REF_PREFIX).trim().removePrefix(HEADS_PREFIX)
        } else {
            // Detached HEAD: content is a commit hash, show it short.
            content.take(SHORT_HASH_LENGTH)
        }
    }

    private fun Path.invariantPathString(): String = toString().replace('\\', '/')
}
