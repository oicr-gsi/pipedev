package ca.on.oicr.pde.diff;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.path.NodePath;

import java.util.Map;

public class ObjectDiff {

    public static <T> Map diff(T actual, T expected) {
        DiffNode root = ObjectDifferBuilder.buildDefault().compare(actual, expected);
        MinimalToMapPrintingVisitor v = new MinimalToMapPrintingVisitor(actual, expected);
        root.visit(v);
        return v.getMessages();
    }

    public static <T> String diffReportSummary(T actual, T expected, int maxChangesToPrint) {
        Map<NodePath, String> differences = ObjectDiff.diff(actual, expected);
        StringBuilder sb = new StringBuilder();
        switch (differences.size()) {
            case 0:
                sb.append("There are no changes");
                break;
            case 1:
                sb.append("There is 1 change:");
                break;
            default:
                sb.append(String.format("There are %s changes:", differences.size()));
                break;
        }
        int count = 0;
        for (Map.Entry<NodePath, String> e : differences.entrySet()) {
            if (++count > maxChangesToPrint) {
                sb.append("\n... ").append(differences.size() - maxChangesToPrint).append(" more");
                break;
            }
            sb.append("\n").append(e.getValue());
        }
        return sb.toString();
    }
    
}
