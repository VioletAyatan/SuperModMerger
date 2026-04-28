package ankol.mod.merger.exception

/**
 * Exception for exiting the process. Throwing this exception is generally considered as a signal to terminate the whole process.
 */
class ExitProcessException : RuntimeException {
    /**
     * Exit code for the exception
     */
    val exitCode: Int

    val errorMessage: String

    constructor(exitCode: Int, errorMessage: String) : super(errorMessage) {
        this.exitCode = exitCode
        this.errorMessage = errorMessage
    }
}
