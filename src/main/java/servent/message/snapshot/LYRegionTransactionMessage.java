package servent.message.snapshot;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.RegionInfo;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

import java.util.List;
import java.util.Map;

public class LYRegionTransactionMessage extends BasicMessage {

    private static final long serialVersionUID = 3116394054726162318L;

    private final List<RegionInfo> regionTransactionValue;

    public LYRegionTransactionMessage(ServentInfo sender, ServentInfo receiver, List<RegionInfo> regionTransactionValue, int senderId) {
        super(MessageType.LY_REGION_TRANSACTION, sender, receiver, String.valueOf(senderId));
        this.regionTransactionValue = regionTransactionValue;
    }

    private LYRegionTransactionMessage(MessageType messageType, ServentInfo sender, ServentInfo receiver, boolean white, List<ServentInfo> routeList, String messageText,
                                       int messageId, List<RegionInfo> regionTransactionValue, Map<Integer, Integer> initiatorVersions) {
        super(messageType, sender, receiver, routeList, messageText, messageId, initiatorVersions);
        this.regionTransactionValue = regionTransactionValue;
    }

    public List<RegionInfo> getRegionTransactionValue() {
        return regionTransactionValue;
    }

    @Override
    public Message setInitiatorVersions() {
        return new LYRegionTransactionMessage(getMessageType(), getOriginalSenderInfo(), getReceiverInfo(), true, getRoute(), getMessageText(), getMessageId(), getRegionTransactionValue(), AppConfig.initiatorVersions);
    }
}
