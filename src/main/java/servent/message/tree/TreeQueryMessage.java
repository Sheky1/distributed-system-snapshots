package servent.message.tree;

import app.AppConfig;
import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class TreeQueryMessage extends BasicMessage {

    private static final long serialVersionUID = 6552725236272271201L;

    public TreeQueryMessage(ServentInfo receiver, Integer regionInitiator) {
        super(MessageType.TREE_QUERY, AppConfig.myServentInfo, receiver, String.valueOf(regionInitiator));
    }
}
