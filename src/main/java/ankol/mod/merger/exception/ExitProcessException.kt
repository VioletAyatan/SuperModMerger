package ankol.mod.merger.exception

class ExitProcessException : RuntimeException {
    /**
     * 异常退出码
     */
    private val exitCode: Int

    constructor(exitCode: Int, errorMessage: String) : super(errorMessage) {
        this.exitCode = exitCode
    }
}
