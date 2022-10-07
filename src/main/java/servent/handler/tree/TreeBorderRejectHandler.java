package servent.handler.tree;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.tree.FoundRegionMessage;
import servent.message.tree.TreeRejectMessage;
import servent.message.util.MessageUtil;

public class TreeBorderRejectHandler implements MessageHandler {

    private final Message clientMessage;

    public TreeBorderRejectHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TREE_BORDER_REJECT) {
            int senderId = clientMessage.getOriginalSenderInfo().getId();

            AppConfig.TREE_STATE.getBorderNeighbors().add(senderId);
            MessageUtil.sendMessage(new FoundRegionMessage(AppConfig.getInfoById(AppConfig.TREE_STATE.getRegionInitiator()), Integer.parseInt(clientMessage.getMessageText())));

            AppConfig.TREE_STATE.checkNodeLinked();
        }

    }

}
