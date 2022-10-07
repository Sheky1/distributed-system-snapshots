package servent.message.tree;

import app.AppConfig;
import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class TreeAcceptMessage extends BasicMessage {

    private static final long serialVersionUID = -3891383848924403186L;

    public TreeAcceptMessage(ServentInfo receiver) {
        super(MessageType.TREE_ACCEPT, AppConfig.myServentInfo, receiver);
    }
}
