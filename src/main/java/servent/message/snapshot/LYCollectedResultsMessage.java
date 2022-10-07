package servent.message.snapshot;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.LYSnapshotResult;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

import java.util.List;
import java.util.Map;

public class LYCollectedResultsMessage extends BasicMessage {

    private static final long serialVersionUID = 3116394054726162318L;

    private final Map<Integer, LYSnapshotResult> collectedLYValues;

    public LYCollectedResultsMessage(ServentInfo sender, ServentInfo receiver, Map<Integer, LYSnapshotResult> collectedLYValues) {
        super(MessageType.LY_COLLECTED_RESULTS, sender, receiver);

        this.collectedLYValues = collectedLYValues;
    }

    private LYCollectedResultsMessage(MessageType messageType, ServentInfo sender, ServentInfo receiver, boolean white, List<ServentInfo> routeList, String messageText,
                          int messageId, Map<Integer, LYSnapshotResult> collectedLYValues, Map<Integer, Integer> initiatorVersions) {
        super(messageType, sender, receiver, routeList, messageText, messageId, initiatorVersions);
        this.collectedLYValues = collectedLYValues;
    }

    public Map<Integer, LYSnapshotResult> getCollectedLYValues() {
        return collectedLYValues;
    }

    @Override
    public Message setInitiatorVersions() {
        return new LYCollectedResultsMessage(getMessageType(), getOriginalSenderInfo(), getReceiverInfo(), true, getRoute(), getMessageText(), getMessageId(), getCollectedLYValues(), AppConfig.initiatorVersions);
    }
}
