package servent.handler.tree;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class TreeAcceptHandler implements MessageHandler {

    private final Message clientMessage;

    public TreeAcceptHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TREE_ACCEPT) {
            int senderId = clientMessage.getOriginalSenderInfo().getId();

            AppConfig.TREE_STATE.getChildren().add(senderId);

            AppConfig.TREE_STATE.checkNodeLinked();
        }

    }

}
