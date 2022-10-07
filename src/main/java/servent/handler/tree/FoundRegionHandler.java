package servent.handler.tree;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class FoundRegionHandler implements MessageHandler {

    private final Message clientMessage;

    public FoundRegionHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.FOUND_REGION) {
            AppConfig.regions.add(Integer.parseInt(clientMessage.getMessageText()));

            AppConfig.timestampedStandardPrint("The tree was found a new region: " + clientMessage.getMessageText() + ". I currently know of regions " + AppConfig.regions);
        }

    }

}
