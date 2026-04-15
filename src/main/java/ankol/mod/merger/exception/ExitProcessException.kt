package ankol.mod.merger.exception

/**
 * 用于退出进程的异常，一般抛出这个异常视为整个流程需要中止
 */
class ExitProcessException : RuntimeException {
    /**
     * 异常退出码
     */
    val exitCode: Int

    val errorMessage: String

    constructor(exitCode: Int, errorMessage: String) : super(errorMessage) {
        this.exitCode = exitCode
        this.errorMessage = errorMessage
    }
}
