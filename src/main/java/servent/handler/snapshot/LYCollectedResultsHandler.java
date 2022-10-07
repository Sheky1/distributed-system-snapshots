package servent.handler.snapshot;

import app.AppConfig;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.snapshot.LYCollectedResultsMessage;
import servent.message.util.MessageUtil;

public class LYCollectedResultsHandler implements MessageHandler {

    private final Message clientMessage;
    private final SnapshotCollector snapshotCollector;

    public LYCollectedResultsHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.LY_COLLECTED_RESULTS) {
            LYCollectedResultsMessage lyTellMessage = (LYCollectedResultsMessage) clientMessage;
            snapshotCollector.addAllLYSnapshotInfo(lyTellMessage.getCollectedLYValues());
            AppConfig.collectedChildren += 1;
            if (AppConfig.TREE_STATE.getChildren().size() == AppConfig.collectedChildren && AppConfig.TREE_STATE.getParentId() != AppConfig.myServentInfo.getId()) {
                Message collectedResultsMessage = new LYCollectedResultsMessage(AppConfig.myServentInfo, AppConfig.getInfoById(AppConfig.TREE_STATE.getParentId()), snapshotCollector.getCollectedLYValues());
                MessageUtil.sendMessage(collectedResultsMessage);
            }
        } else {
            AppConfig.timestampedErrorPrint("Tell amount handler got: " + clientMessage);
        }

    }
}