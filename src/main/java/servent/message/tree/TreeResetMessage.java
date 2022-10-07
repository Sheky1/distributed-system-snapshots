package servent.message.tree;

import app.AppConfig;
import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreeResetMessage extends BasicMessage {

    private static final long serialVersionUID = 4632948952510475549L;
    private final Set<Integer> regions;

    public TreeResetMessage(ServentInfo receiver, Set<Integer> regions) {
        super(MessageType.TREE_RESET, AppConfig.myServentInfo, receiver);
        this.regions = regions;
    }

    private TreeResetMessage(MessageType messageType, ServentInfo sender, ServentInfo receiver, boolean white, List<ServentInfo> routeList, String messageText,
                          int messageId, Set<Integer> regions, Map<Integer, Integer> initiatorVersions) {
        super(messageType, sender, receiver, routeList, messageText, messageId, initiatorVersions);
        this.regions = regions;
    }

    public Set<Integer> getRegions() {
        return regions;
    }

    @Override
    public Message setInitiatorVersions() {
        return new TreeResetMessage(getMessageType(), getOriginalSenderInfo(), getReceiverInfo(), true, getRoute(), getMessageText(), getMessageId(), getRegions(), AppConfig.initiatorVersions);
    }
}
