package ankol.mod.merger.tools

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Get a logger for the specified class type
 *
 * Extension function that returns a Logger instance associated with the current class. Uses reified generic type parameter to obtain class information at runtime and generate the corresponding logger.
 *
 * @param T Generic type, represents the specific class calling this extension function.
 * @return Logger instance for the class, used for logging.
 *
 * Usage example:
 * ```
 * class User {
 *     private val log = logger() // Returns the logger associated with this class
 * }
 *
 * // Or, you can also get the logger for a class via a variable
 *
 * val user = User("Alice", 30)
 * val log = user.logger()  // Returns the logger associated with User class
 * log.cyan("Logging some information")
 *
 * ```
 */
inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

/**
 * Get a logger for the specified class type
 *
 * Extension function that returns a Logger instance associated with the current class. Uses reified generic type parameter to obtain class information at runtime and generate the corresponding logger.
 *
 * @param T Generic type, represents the specific class calling this extension function.
 * @return Logger instance for the class, used for logging.
 *
 * Usage example:
 * ```
 * class User {
 *     private val log = logger<User>() // Returns the logger associated with this class
 * }
 *
 * ```
 */
inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)
