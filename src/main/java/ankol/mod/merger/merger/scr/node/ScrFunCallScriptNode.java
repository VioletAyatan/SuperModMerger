package ankol.mod.merger.merger.scr.node;

import ankol.mod.merger.core.BaseTreeNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;

@Getter
@Setter
@ToString
public class ScrFunCallScriptNode extends BaseTreeNode {
    private final String functionName;
    private final List<String> arguments;

    public ScrFunCallScriptNode(String signature, int startTokenIndex, int stopTokenIndex, int line, CommonTokenStream tokenStream, String functionName, List<String> arguments) {
        super(signature, startTokenIndex, stopTokenIndex, line, tokenStream);
        this.functionName = functionName;
        this.arguments = arguments;
    }
}
