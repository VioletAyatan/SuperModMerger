package ankol.mod.merger.exception

/**
 * Business exception for domain logic errors
 */
class BusinessException : RuntimeException {
    constructor()

    constructor(message: String) : super(message)
}
