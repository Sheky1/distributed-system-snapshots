package servent.message.tree;

import app.AppConfig;
import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class FoundRegionMessage extends BasicMessage {

    private static final long serialVersionUID = 6552725236272271201L;

    public FoundRegionMessage(ServentInfo receiver, Integer regionInitiator) {
        super(MessageType.FOUND_REGION, AppConfig.myServentInfo, receiver, String.valueOf(regionInitiator));
    }
}
