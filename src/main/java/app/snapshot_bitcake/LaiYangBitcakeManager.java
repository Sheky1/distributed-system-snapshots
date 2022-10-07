package app.snapshot_bitcake;

import app.AppConfig;
import app.NodeType;
import servent.message.Message;
import servent.message.snapshot.LYMarkerMessage;
import servent.message.snapshot.LYTellMessage;
import servent.message.util.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class LaiYangBitcakeManager implements BitcakeManager {

    private final AtomicInteger currentAmount = new AtomicInteger(1000);

    public void takeSomeBitcakes(int amount) {
        currentAmount.getAndAdd(-amount);
        AppConfig.timestampedStandardPrint("Current amount after removing: " + currentAmount);
    }

    public void addSomeBitcakes(int amount) {
        currentAmount.getAndAdd(amount);
        AppConfig.timestampedStandardPrint("Current amount after adding: " + currentAmount);
    }

    public int getCurrentBitcakeAmount() {
        return currentAmount.get();
    }

    private final Map<HistoryKey, Map<Integer, Integer>> giveHistory = new ConcurrentHashMap<>();
    private final Map<HistoryKey, Map<Integer, Integer>> getHistory = new ConcurrentHashMap<>();

//    private final Map<Integer, Integer> giveHistory = new ConcurrentHashMap<>();
//    private final Map<Integer, Integer> getHistory = new ConcurrentHashMap<>();

    public LaiYangBitcakeManager() {
        for(Integer initiator: AppConfig.initiatorVersions.keySet()) {
            Map<Integer, Integer> neighbourMapGive = new ConcurrentHashMap<>();
            Map<Integer, Integer> neighbourMapGet = new ConcurrentHashMap<>();
            for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
//            giveHistory.put(neighbor, 0);
//            getHistory.put(neighbor, 0);
                neighbourMapGive.put(neighbor, 0);
                neighbourMapGet.put(neighbor, 0);
            }
            giveHistory.put(new HistoryKey(initiator, 0), neighbourMapGive);
            getHistory.put(new HistoryKey(initiator, 0), neighbourMapGet);
        }
    }

    /*
     * This value is protected by AppConfig.colorLock.
     * Access it only if you have the blessing.
     */
    public int recordedAmount = 0;

    public void markerEvent(int collectorId, SnapshotCollector snapshotCollector) {
        synchronized (AppConfig.colorLock) {
            recordedAmount = getCurrentBitcakeAmount();
            LYSnapshotResult snapshotResult = new LYSnapshotResult(AppConfig.myServentInfo.getId(), recordedAmount, giveHistory, getHistory, AppConfig.initiatorVersions);

            if (collectorId == AppConfig.myServentInfo.getId())
                snapshotCollector.addLYSnapshotInfo(AppConfig.myServentInfo.getId(), snapshotResult);
            else {
                AppConfig.timestampedStandardPrint("SENDING MY MAPS IN TELL " + snapshotResult);
//                Message tellMessage = new LYTellMessage(AppConfig.myServentInfo, AppConfig.getInfoById(collectorId), snapshotResult);
                if(AppConfig.TREE_STATE.getNodeType() == NodeType.LEAF) {
                    Message tellMessage = new LYTellMessage(AppConfig.myServentInfo, AppConfig.getInfoById(AppConfig.TREE_STATE.getParentId()), snapshotResult);
                    MessageUtil.sendMessage(tellMessage);
                } else {
                    snapshotCollector.addLYSnapshotInfo(AppConfig.myServentInfo.getId(), snapshotResult);
                }
                AppConfig.timestampedStandardPrint("UPDATING THE VALUE OF INIT VERSION + 1");
                AppConfig.initiatorVersions.put(collectorId, AppConfig.initiatorVersions.get(collectorId) + 1);
                AppConfig.timestampedStandardPrint("NOW MY MAP IS " + AppConfig.initiatorVersions.toString());
            }

//            for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                for (Integer child : AppConfig.TREE_STATE.getChildren()) {
                Message clMarker = new LYMarkerMessage(AppConfig.myServentInfo, AppConfig.getInfoById(child), collectorId);
                MessageUtil.sendMessage(clMarker);
                try {
                    /*
                     * This sleep is here to artificially produce some white node -> red node messages.
                     * Not actually recommended, as we are sleeping while we have colorLock.
                     */
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class MapValueUpdater implements BiFunction<Integer, Integer, Integer> {

        private final int valueToAdd;

        public MapValueUpdater(int valueToAdd) {
            this.valueToAdd = valueToAdd;
        }

        @Override
        public Integer apply(Integer key, Integer oldValue) {
            return oldValue + valueToAdd;
        }
    }

    public void recordGiveTransaction(int neighbor, int amount) {
        for(Map.Entry<Integer, Integer> pair: AppConfig.initiatorVersions.entrySet()) {
            if(!giveHistory.containsKey(new HistoryKey(pair.getKey(), pair.getValue()))){
                Map<Integer, Integer> neighbourMap = new ConcurrentHashMap<>();
                for(Integer neighbour: AppConfig.myServentInfo.getNeighbors()) {
                    neighbourMap.put(neighbour, 0);
                }
                giveHistory.put(new HistoryKey(pair.getKey(), pair.getValue()), neighbourMap);
            }
            giveHistory.get(new HistoryKey(pair.getKey(), pair.getValue())).compute(neighbor, new MapValueUpdater(amount));
        }
        AppConfig.timestampedStandardPrint("Giving away bits, current giveHistory: " + giveHistory);
    }

    public void recordGetTransaction(int neighbor, int amount) {
        for(Map.Entry<Integer, Integer> pair: AppConfig.initiatorVersions.entrySet()) {
            if(!getHistory.containsKey(new HistoryKey(pair.getKey(), pair.getValue()))){
                Map<Integer, Integer> neighbourMap = new ConcurrentHashMap<>();
                for(Integer neighbour: AppConfig.myServentInfo.getNeighbors()) {
                    neighbourMap.put(neighbour, 0);
                }
                getHistory.put(new HistoryKey(pair.getKey(), pair.getValue()), neighbourMap);
            }
            getHistory.get(new HistoryKey(pair.getKey(), pair.getValue())).compute(neighbor, new MapValueUpdater(amount));
        }
        AppConfig.timestampedStandardPrint("Getting bits, current getHistory: " + getHistory);
    }
}
