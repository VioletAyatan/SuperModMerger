package ankol.mod.merger.merger.scr.news.node;

import lombok.Data;

import java.util.List;

@Data
public class ScrFunCallNode extends ScrNode {
    private final List<String> arguments;

    public ScrFunCallNode(String signature, int startIndex, int stopIndex, int line, String sourceText, List<String> arguments) {
        super(signature, startIndex, stopIndex, line, sourceText);
        this.arguments = arguments;
    }
}
