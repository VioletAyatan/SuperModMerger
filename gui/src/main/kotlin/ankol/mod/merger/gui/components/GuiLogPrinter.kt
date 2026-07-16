package ankol.mod.merger.gui.components

import ankol.mod.merger.api.ColorPrinter
import ankol.mod.merger.api.MergeProgressCallback
import ankol.mod.merger.gui.MergeViewModel
import ankol.mod.merger.tools.Tools

/**
 * GUI版本的日志打印器
 * @author Ankol
 */
class GuiLogPrinter(private val vm: MergeViewModel) : ColorPrinter {

    override fun blue(message: String) {
        vm.addLog(MergeProgressCallback.Level.DEBUG, message)
    }

    override fun blue(format: String, vararg args: Any) {
        TODO("Not yet implemented")
    }

    override fun cyan(message: String) {
        TODO("Not yet implemented")
    }

    override fun cyan(format: String, vararg args: Any) {
        TODO("Not yet implemented")
    }

    override fun success(message: String) {
        vm.addLog(MergeProgressCallback.Level.SUCCESS, message)
    }

    override fun success(format: String, vararg args: Any) {
        vm.addLog(MergeProgressCallback.Level.SUCCESS, Tools.format(format, *args))
    }

    override fun warning(message: String) {
        TODO("Not yet implemented")
    }

    override fun warning(format: String, vararg args: Any) {
        TODO("Not yet implemented")
    }

    override fun error(message: String) {
        TODO("Not yet implemented")
    }

    override fun error(format: String, vararg args: Any?) {
        TODO("Not yet implemented")
    }

    override fun debug(message: String) {
        TODO("Not yet implemented")
    }

    override fun debug(format: String, vararg args: Any) {
        TODO("Not yet implemented")
    }

    override fun print(message: String) {
        TODO("Not yet implemented")
    }

    override fun print(format: String, vararg args: Any) {
        TODO("Not yet implemented")
    }

    override fun bold(message: String) {
        TODO("Not yet implemented")
    }

    override fun bold(format: String, vararg args: Any) {
        TODO("Not yet implemented")
    }

    override fun highlight(message: String) {
        TODO("Not yet implemented")
    }

    override fun highlight(format: String, vararg args: Any) {
        TODO("Not yet implemented")
    }
}
