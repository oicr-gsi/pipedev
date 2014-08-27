package ca.on.oicr.pde.diff;

import de.danielbechler.diff.node.Node;
import de.danielbechler.diff.visitor.ToMapPrintingVisitor;

class MinimalToMapPrintingVisitor extends ToMapPrintingVisitor {

    public MinimalToMapPrintingVisitor(final Object working, final Object base) {
        super(working, base);
    }

    @Override
    protected String differenceToString(final Node node, final Object base, final Object modified) {
        String text = super.differenceToString(node, base, modified);
        text = text.replaceAll("Property at path ", "");
        getMessages().put(node.getPropertyPath(), text);
        return text;
    }
    
}
