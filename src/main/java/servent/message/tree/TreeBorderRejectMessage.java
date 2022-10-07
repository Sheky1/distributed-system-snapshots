package servent.message.tree;

import app.AppConfig;
import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class TreeBorderRejectMessage extends BasicMessage {

    private static final long serialVersionUID = 6552725236272271201L;

    public TreeBorderRejectMessage(ServentInfo receiver, Integer regionInitiator) {
        super(MessageType.TREE_BORDER_REJECT, AppConfig.myServentInfo, receiver, String.valueOf(regionInitiator));
    }
}
