package ankol.mod.merger.merger.scr.node;

import ankol.mod.merger.core.BaseTreeNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ScrFunCallScriptNode extends BaseTreeNode {
    private final String functionName;
    private final List<String> arguments;

    public ScrFunCallScriptNode(String signature, int startIndex, int stopIndex, int startTokenIndex, int stopTokenIndex, int line, String sourceText, String functionName, List<String> arguments) {
        super(signature, startTokenIndex, stopTokenIndex, line, sourceText);
        this.functionName = functionName;
        this.arguments = arguments;
    }
}
