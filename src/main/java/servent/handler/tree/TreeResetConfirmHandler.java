package servent.handler.tree;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class TreeResetConfirmHandler implements MessageHandler {

    private final Message clientMessage;

    public TreeResetConfirmHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TREE_RESET_CONFIRM) {
            AppConfig.treeResetConfirmed++;
        }

    }

}
