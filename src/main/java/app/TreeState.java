package app;

import servent.message.Message;
import servent.message.tree.TreeResetConfirmMessage;
import servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeState {

    private NodeType nodeType = NodeType.NONE;
    private int parentId = -1;
    private int regionInitiator = -1;
    private boolean treeDone = false;
    public static final Object treeLock = new Object();

    private final Set<Integer> children = new HashSet<>();
    private final Set<Integer> unrelated = new HashSet<>();
    private final Set<Integer> borderNeighbors = new HashSet<>();

    public boolean checkNodeLinked() {
        List<Integer> cUu = new ArrayList<>();

        cUu.addAll(children);
        cUu.addAll(unrelated);
        cUu.addAll(borderNeighbors);
        if (parentId != -1 && parentId != AppConfig.myServentInfo.getId()) {
            cUu.add(parentId);
        }

        Collections.sort(cUu);

        treeDone = cUu.equals(AppConfig.myServentInfo.getNeighbors());
        AppConfig.timestampedStandardPrint("SIZES: " + AppConfig.myServentInfo.getNeighbors().size() + " " + cUu.size());
        AppConfig.timestampedStandardPrint("LISTS: " + AppConfig.myServentInfo.getNeighbors() + " " + cUu);

        if (treeDone) {
            if (children.size() == 0) {
                AppConfig.timestampedStandardPrint("I AM A LEAF!");
                nodeType = NodeType.LEAF;
            }
            AppConfig.timestampedStandardPrint("Node done.");
            AppConfig.timestampedStandardPrint("Parent: " + parentId);
            AppConfig.timestampedStandardPrint("Region: " + regionInitiator);
            AppConfig.timestampedStandardPrint("Children: " + children);
            AppConfig.timestampedStandardPrint("Unrelated: " + unrelated);
            AppConfig.timestampedStandardPrint("Border neighbors: " + borderNeighbors);
        }

        return treeDone;
    }

    public void reset() {
        regionInitiator = -1;
        treeDone = false;
        children.clear();
        unrelated.clear();
        borderNeighbors.clear();
        nodeType = NodeType.NONE;

        Message treeResetConfirmMessage = new TreeResetConfirmMessage(AppConfig.getInfoById(parentId));
        MessageUtil.sendMessage(treeResetConfirmMessage);
        parentId = -1;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public int getParentId() {
        return parentId;
    }

    public boolean isTreeDone() {
        return treeDone;
    }

    public Set<Integer> getChildren() {
        return children;
    }

    public Set<Integer> getUnrelated() {
        return unrelated;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getRegionInitiator() {
        return regionInitiator;
    }

    public void setRegionInitiator(int regionInitiator) {
        this.regionInitiator = regionInitiator;
    }

    public Set<Integer> getBorderNeighbors() {
        return borderNeighbors;
    }
}
