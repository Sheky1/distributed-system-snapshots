package servent.handler.snapshot;

import app.AppConfig;
import app.snapshot_bitcake.RegionInfo;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.snapshot.LYRegionTransactionMessage;

public class LYRegionTransactionHandler implements MessageHandler {

    private final Message clientMessage;
    private final SnapshotCollector snapshotCollector;

    public LYRegionTransactionHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.LY_REGION_TRANSACTION) {
            LYRegionTransactionMessage lyRegionTransactionMessage = (LYRegionTransactionMessage) clientMessage;
            if(lyRegionTransactionMessage.getRegionTransactionValue().size() == 0) {
                AppConfig.timestampedStandardPrint("Got an empty list, filling blank. " + lyRegionTransactionMessage.getRegionTransactionValue());
            }
            for(RegionInfo regionInfo: lyRegionTransactionMessage.getRegionTransactionValue()) {
                AppConfig.timestampedStandardPrint("Calling addresults for " + regionInfo);
                snapshotCollector.addResults(regionInfo);
            }
//            AppConfig.timestampedStandardPrint("I've got this from the other region: " + lyRegionTransactionMessage.getRegionTransactionValue());
//            snapshotCollector.addRegionTransactionValue(lyRegionTransactionMessage.getRegionTransactionValue());
//            if(lyRegionTransactionMessage.getRegionTransactionValue().size() == 0) AppConfig.blanks.put(lyRegionTransactionMessage.getOriginalSenderInfo().getId(), true);
//            else AppConfig.blanks.put(lyRegionTransactionMessage.getOriginalSenderInfo().getId(), false);
//            Message collectedResultsMessage = new LYBlankStatusMessage(AppConfig.myServentInfo, AppConfig.getInfoById(lyRegionTransactionMessage.getOriginalSenderInfo().getId()), foundNew);
//            MessageUtil.sendMessage(collectedResultsMessage);

        } else {
            AppConfig.timestampedErrorPrint("Tell amount handler got: " + clientMessage);
        }

    }
}