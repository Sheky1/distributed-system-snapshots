package servent.handler.tree;

import app.AppConfig;
import app.TreeState;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.tree.TreeAcceptMessage;
import servent.message.tree.TreeBorderRejectMessage;
import servent.message.tree.TreeQueryMessage;
import servent.message.tree.TreeRejectMessage;
import servent.message.util.MessageUtil;

public class TreeQueryHandler implements MessageHandler {

    private final Message clientMessage;

    public TreeQueryHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TREE_QUERY) {
            int senderId = clientMessage.getOriginalSenderInfo().getId();
            synchronized(TreeState.treeLock) {
                if (AppConfig.TREE_STATE.getParentId() == -1) {
                    AppConfig.timestampedStandardPrint("Sending ACCEPT to my new parent: " + senderId);
                    MessageUtil.sendMessage(new TreeAcceptMessage(clientMessage.getOriginalSenderInfo()));
                    AppConfig.TREE_STATE.setParentId(senderId);
                    AppConfig.TREE_STATE.setRegionInitiator(Integer.parseInt(clientMessage.getMessageText()));

                    for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                        if (neighbor != senderId) {
                            MessageUtil.sendMessage(new TreeQueryMessage(AppConfig.getInfoById(neighbor), AppConfig.TREE_STATE.getRegionInitiator()));
                        }
                    }

                    AppConfig.TREE_STATE.checkNodeLinked();
                } else {
                    if(AppConfig.TREE_STATE.getRegionInitiator() == Integer.parseInt(clientMessage.getMessageText())) {
                        AppConfig.timestampedStandardPrint("Already got parent: " + AppConfig.TREE_STATE.getParentId() + ". Sending REJECT to " + senderId);
                        MessageUtil.sendMessage(new TreeRejectMessage(clientMessage.getOriginalSenderInfo()));
                    } else {
                        AppConfig.timestampedStandardPrint("Node from a different region. My region is " + AppConfig.TREE_STATE.getRegionInitiator() + " and the request is sent from region " + clientMessage.getMessageText() + ". Sending BORDER_REJECT to " + senderId);
                        MessageUtil.sendMessage(new TreeBorderRejectMessage(clientMessage.getOriginalSenderInfo(), AppConfig.TREE_STATE.getRegionInitiator()));
                    }
                }
            }
        }

    }

}
