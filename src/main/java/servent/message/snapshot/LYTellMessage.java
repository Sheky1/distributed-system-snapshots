package servent.message.snapshot;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.LYSnapshotResult;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

import java.util.List;
import java.util.Map;

public class LYTellMessage extends BasicMessage {

    private static final long serialVersionUID = 3116394054726162318L;

    private final LYSnapshotResult lySnapshotResult;

    public LYTellMessage(ServentInfo sender, ServentInfo receiver, LYSnapshotResult lySnapshotResult) {
        super(MessageType.LY_TELL, sender, receiver);

        this.lySnapshotResult = lySnapshotResult;
    }

    private LYTellMessage(MessageType messageType, ServentInfo sender, ServentInfo receiver, boolean white, List<ServentInfo> routeList, String messageText,
                          int messageId, LYSnapshotResult lySnapshotResult, Map<Integer, Integer> initiatorVersions) {
        super(messageType, sender, receiver, routeList, messageText, messageId, initiatorVersions);
        this.lySnapshotResult = lySnapshotResult;
    }

    public LYSnapshotResult getLYSnapshotResult() {
        return lySnapshotResult;
    }

    @Override
    public Message setInitiatorVersions() {
        return new LYTellMessage(getMessageType(), getOriginalSenderInfo(), getReceiverInfo(), true, getRoute(), getMessageText(), getMessageId(), getLYSnapshotResult(), AppConfig.initiatorVersions);
    }
}
