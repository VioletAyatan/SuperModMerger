package ankol.mod.merger.tools

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 获取指定类型的日志记录器
 *
 * 扩展函数，返回与当前类相关联的 Logger 实例。使用了 reified 泛型类型参数，使得在运行时可以获取类型信息，并为该类型生成对应的日志记录器。
 *
 * @param T 泛型类型，表示调用此扩展函数的具体类。
 * @return 该类对应的 Logger 实例，用于记录日志。
 *
 * 用法示例：
 * ```
 * class User {
 *     private val log = logger() //返回关联这个类的日志记录器
 * }
 *
 * //或者，还可以通过变量直接获取对应类的日志记录器
 *
 * val user = User("Alice", 30)
 * val log = user.logger()  // 返回与 User 类关联的 Logger
 * log.cyan("Logging some information")
 *
 * ```
 */
inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

/**
 * 获取指定类型的日志记录器
 *
 * 扩展函数，返回与当前类相关联的 Logger 实例。使用了 reified 泛型类型参数，使得在运行时可以获取类型信息，并为该类型生成对应的日志记录器。
 *
 * @param T 泛型类型，表示调用此扩展函数的具体类。
 * @return 该类对应的 Logger 实例，用于记录日志。
 *
 * 用法示例：
 * ```
 * class User {
 *     private val log = logger<User>() //返回关联这个类的日志记录器
 * }
 *
 * ```
 */
inline fun <reified T> logger(): Logger = LoggerFactory.getLogger(T::class.java)