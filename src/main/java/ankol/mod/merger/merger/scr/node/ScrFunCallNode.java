package ankol.mod.merger.merger.scr.node;

import lombok.Data;

import java.util.List;

@Data
public class ScrFunCallNode extends ScrNode {
    private final String functionName;
    private final List<String> arguments;

    public ScrFunCallNode(String signature, int startIndex, int stopIndex, int line, String sourceText, String functionName, List<String> arguments) {
        super(signature, startIndex, stopIndex, line, sourceText);
        this.functionName = functionName;
        this.arguments = arguments;
    }
}
