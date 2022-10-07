package servent.handler.tree;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class TreeRejectHandler implements MessageHandler {

    private final Message clientMessage;

    public TreeRejectHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TREE_REJECT) {
            int senderId = clientMessage.getOriginalSenderInfo().getId();

            AppConfig.TREE_STATE.getUnrelated().add(senderId);

            AppConfig.TREE_STATE.checkNodeLinked();
        }
    }
}
