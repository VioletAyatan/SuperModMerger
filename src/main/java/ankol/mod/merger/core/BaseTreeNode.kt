package ankol.mod.merger.core

import com.fasterxml.jackson.annotation.JsonIgnore
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.misc.Interval

/**
 * 基础树节点
 * 
 * @author Ankol
 */
abstract class BaseTreeNode(
    /**
     * 当前节点签名（确保在同一树层级下保持唯一，方便进行多文件对比）
     */
    var signature: String,
    /**
     * 当前节点起始TOKEN索引
     */
    val startTokenIndex: Int,
    /**
     * 当前节点结束TOKEN索引
     */
    val stopTokenIndex: Int,
    /**
     * 当前行号
     */
    val lineNumber: Int,
    /**
     * Token流引用
     */
    @field:Transient
    @field:JsonIgnore
    val tokenStream: CommonTokenStream
) {
    /**
     * 当前节点存储的源文本
     * kt的lazy加载类型java的单例，这里主要是为了利用这个机制进行缓存，避免反复获取源文本
     * LazyThreadSafetyMode.PUBLICATION 允许多个线程同时初始化，但只会有一个结果被使用
     */
    val sourceText: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        val startIndex = tokenStream.get(startTokenIndex).startIndex
        val stopIndex = tokenStream.get(stopTokenIndex).stopIndex
        return@lazy tokenStream.getTokenSource().inputStream.getText(Interval(startIndex, stopIndex))
    }
}
