package ankol.mod.merger.tools

import java.util.*

/**
 * Matches archive entry paths using Ant-style wildcards.
 *
 * Supported wildcards:
 * - `?` matches one character within a path segment.
 * - `*` matches zero or more characters within a path segment.
 * - `**` matches zero or more complete path segments.
 *
 * Archive paths are matched case-insensitively and accept either slash style.
 */
internal object AntPathMatcher {
    fun matches(pattern: String, path: String): Boolean {
        val patternSegments = normalize(pattern).split('/')
        val pathSegments = normalize(path).split('/')
        val results = mutableMapOf<Pair<Int, Int>, Boolean>()

        fun matchesFrom(patternIndex: Int, pathIndex: Int): Boolean =
            results.getOrPut(patternIndex to pathIndex) {
                when {
                    patternIndex == patternSegments.size -> pathIndex == pathSegments.size
                    patternSegments[patternIndex] == "**" ->
                        matchesFrom(patternIndex + 1, pathIndex) ||
                                (pathIndex < pathSegments.size && matchesFrom(patternIndex, pathIndex + 1))

                    pathIndex == pathSegments.size -> false
                    matchesSegment(patternSegments[patternIndex], pathSegments[pathIndex]) ->
                        matchesFrom(patternIndex + 1, pathIndex + 1)

                    else -> false
                }
            }

        return matchesFrom(0, 0)
    }

    private fun matchesSegment(pattern: String, value: String): Boolean {
        val results = mutableMapOf<Pair<Int, Int>, Boolean>()

        fun matchesFrom(patternIndex: Int, valueIndex: Int): Boolean =
            results.getOrPut(patternIndex to valueIndex) {
                when {
                    patternIndex == pattern.length -> valueIndex == value.length
                    pattern[patternIndex] == '*' ->
                        matchesFrom(patternIndex + 1, valueIndex) ||
                                (valueIndex < value.length && matchesFrom(patternIndex, valueIndex + 1))

                    valueIndex == value.length -> false
                    pattern[patternIndex] == '?' || pattern[patternIndex] == value[valueIndex] ->
                        matchesFrom(patternIndex + 1, valueIndex + 1)

                    else -> false
                }
            }

        return matchesFrom(0, 0)
    }

    private fun normalize(path: String): String = path
        .replace('\\', '/')
        .trimStart('/')
        .lowercase(Locale.ROOT)
}
