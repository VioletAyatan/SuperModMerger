package ankol.mod.merger.api

/**
 * 合并进度回调接口
 * GUI 可实现此接口以展示实时进度和日志
 */
interface MergeProgressCallback {

    /** 日志级别 */
    enum class Level { DEBUG, INFO, WARN, ERROR, SUCCESS }

    /**
     * 日志输出
     * @param level 日志级别
     * @param message 日志消息
     */
    fun onLog(level: Level, message: String)

    /**
     * 进度更新
     * @param current 当前处理到的文件序号
     * @param total 总文件数
     * @param fileName 当前正在处理的文件名
     */
    fun onProgress(current: Int, total: Int, fileName: String)

    /**
     * 错误报告
     * @param fileName 出问题的文件名
     * @param message 错误描述
     */
    fun onError(fileName: String, message: String)

    /**
     * 合并完成
     * @param totalProcessed 总处理文件数
     * @param mergedCount 成功合并文件数
     * @param pathCorrectionCount 路径修正数
     * @param errorCount 错误数
     */
    fun onComplete(totalProcessed: Int, mergedCount: Int, pathCorrectionCount: Int, errorCount: Int)
}
