package ankol.mod.merger.merger.scr.node;

import lombok.Data;

import java.util.List;

@Data
public class ScrFunCallScriptNode extends ScrScriptNode {
    private final String functionName;
    private final List<String> arguments;

    public ScrFunCallScriptNode(String signature, int startIndex, int stopIndex, int line, String sourceText, String functionName, List<String> arguments) {
        super(signature, startIndex, stopIndex, line, sourceText);
        this.functionName = functionName;
        this.arguments = arguments;
    }
}
