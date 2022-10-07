package app.snapshot_bitcake;

import app.AppConfig;
import app.NodeType;
import app.ServentInfo;
import servent.message.Message;
import servent.message.snapshot.LYRegionTransactionMessage;
import servent.message.tree.TreeQueryMessage;
import servent.message.tree.TreeResetMessage;
import servent.message.util.MessageUtil;

import javax.swing.plaf.synth.Region;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main snapshot collector class. Has support for Naive, Chandy-Lamport
 * and Lai-Yang snapshot algorithms.
 *
 * @author bmilojkovic
 */
public class SnapshotCollectorWorker implements SnapshotCollector {

    private volatile boolean working = true;

    private final AtomicBoolean collecting = new AtomicBoolean(false);

    private final Map<Integer, LYSnapshotResult> collectedLYValues = new ConcurrentHashMap<>();

    private int round = -1;
    private final Map<Integer, Integer> regionVersions = new ConcurrentHashMap<>();
    private final List<RegionInfo> redMessages = new ArrayList<>();
    private final Map<Integer, Map<Integer, LYSnapshotResult>> newRegionTransactionValues = new HashMap<>();
    private final Map<Integer, Map<Integer, LYSnapshotResult>> collectedRegionSenders = new HashMap<>();
    private final Set<Integer> collectedSenders = new HashSet<>();
    private final Set<Integer> sentResults = new HashSet<>();
    private final List<RegionInfo> infoToSend = new ArrayList<>();

    private final BitcakeManager bitcakeManager;

    public SnapshotCollectorWorker() {
        bitcakeManager = new LaiYangBitcakeManager();
    }

    @Override
    public BitcakeManager getBitcakeManager() {
        return bitcakeManager;
    }

    @Override
    public void run() {
        while (working) {
            /*
             * Not collecting yet - just sleep until we start actual work, or finish
             */
            while (!collecting.get()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!working) return;
            }

            /*
             * Collecting is done in three stages:
             * 1. Send messages asking for values
             * 2. Wait for all the responses
             * 3. Print result
             */

            AppConfig.TREE_STATE.setNodeType(NodeType.ROOT);
            AppConfig.TREE_STATE.setParentId(AppConfig.myServentInfo.getId());
            AppConfig.TREE_STATE.setRegionInitiator(AppConfig.myServentInfo.getId());

            for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                ServentInfo neighborInfo = AppConfig.getInfoById(neighbor);
                MessageUtil.sendMessage(new TreeQueryMessage(neighborInfo, AppConfig.TREE_STATE.getRegionInitiator()));
            }

            boolean waiting = true;
            AppConfig.timestampedStandardPrint("Waiting on all children to finish");
            while (waiting) {
                if (AppConfig.TREE_STATE.checkNodeLinked()) {
                    waiting = false;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!working) {
                    return;
                }
            }
            AppConfig.timestampedStandardPrint("All children finished");

            //1 send asks
            AppConfig.initiatorVersions.put(AppConfig.myServentInfo.getId(), AppConfig.initiatorVersions.get(AppConfig.myServentInfo.getId()) + 1);
            ((LaiYangBitcakeManager) bitcakeManager).markerEvent(AppConfig.myServentInfo.getId(), this);

            //2 wait for responses or finish
            boolean waitingResponses = true;
            AppConfig.timestampedStandardPrint("Waiting on all responses in my region.");
            while (waitingResponses) {
                AppConfig.timestampedStandardPrint("Amount of collected results: " + collectedLYValues.size());
//                if (collectedLYValues.size() == AppConfig.getServentCount()) {
                AppConfig.timestampedStandardPrint("Collected children " + AppConfig.collectedChildren);
                if (AppConfig.TREE_STATE.getChildren().size() == AppConfig.collectedChildren) {
                    waitingResponses = false;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!working) {
                    return;
                }
            }
            AppConfig.timestampedStandardPrint("All results from my region collected.");

            round = 0;
            sentResults.add(AppConfig.myServentInfo.getId());
            if (AppConfig.regions.size() > 0) {
                while (true) {
                    AppConfig.timestampedStandardPrint("Round starting " + round);
                    AppConfig.timestampedStandardPrint("Info to send: " + infoToSend);
                    AppConfig.timestampedStandardPrint("Current blanks: " + AppConfig.blanks);
                    AppConfig.timestampedStandardPrint("Red messages: " + redMessages);
                    for (Integer senderId : AppConfig.regions) {
                        if (redMessages.contains(new RegionInfo(round, senderId, 0, new HashMap<>(), false))) {
                            addResults(redMessages.get(redMessages.indexOf(new RegionInfo(round, senderId, 0, new HashMap<>(), false))));
                        }
                    }
                    boolean allBlanks = true;
                    for (Integer regionInitiator : AppConfig.regions) {
                        AppConfig.timestampedStandardPrint("Current blanks " + AppConfig.blanks);
                        if (AppConfig.blanks.containsKey(round - 1) && !AppConfig.blanks.get(round - 1).get(regionInitiator)) {
                            allBlanks = false;
                            break;
                        }
                    }
                    if (allBlanks && infoToSend.size() == 0 && round != 0) {
                        List<RegionInfo> blankList = new ArrayList<>();
                        blankList.add(new RegionInfo(round, AppConfig.myServentInfo.getId(), -1, new HashMap<>(), true));
                        for (Integer region : AppConfig.regions) {
                            LYRegionTransactionMessage lyRegionTransactionMessage =
                                    new LYRegionTransactionMessage(AppConfig.myServentInfo, AppConfig.getInfoById(region), blankList, AppConfig.myServentInfo.getId());
                            MessageUtil.sendMessage(lyRegionTransactionMessage);
                        }
                        AppConfig.timestampedStandardPrint("Sending blank info before breaking: " + blankList);
                        break;
                    }
                    if (infoToSend.isEmpty() && round != 0) {
                        List<RegionInfo> blankList = new ArrayList<>();
                        blankList.add(new RegionInfo(round, AppConfig.myServentInfo.getId(), -1, new HashMap<>(), true));
                        for (Integer region : AppConfig.regions) {
                            LYRegionTransactionMessage lyRegionTransactionMessage =
                                    new LYRegionTransactionMessage(AppConfig.myServentInfo, AppConfig.getInfoById(region), blankList, AppConfig.myServentInfo.getId());
                            MessageUtil.sendMessage(lyRegionTransactionMessage);
                        }
                        AppConfig.timestampedStandardPrint("Sending blank info: " + blankList);
                    } else {
                        if (round == 0) {
                            List<RegionInfo> myInfoList = new ArrayList<>();
                            myInfoList.add(new RegionInfo(round, AppConfig.myServentInfo.getId(), AppConfig.myServentInfo.getId(), collectedLYValues, false));
                            AppConfig.timestampedStandardPrint("Sending in round 0: " + myInfoList);
                            for (Integer region : AppConfig.regions) {
                                LYRegionTransactionMessage lyRegionTransactionMessage =
                                        new LYRegionTransactionMessage(AppConfig.myServentInfo, AppConfig.getInfoById(region), myInfoList, AppConfig.myServentInfo.getId());
                                MessageUtil.sendMessage(lyRegionTransactionMessage);
                            }
                        } else {
                            AppConfig.timestampedStandardPrint("Sending new infoTosend: " + infoToSend);
                            for (Integer region : AppConfig.regions) {
                                LYRegionTransactionMessage lyRegionTransactionMessage =
                                        new LYRegionTransactionMessage(AppConfig.myServentInfo, AppConfig.getInfoById(region), new ArrayList<>(infoToSend), AppConfig.myServentInfo.getId());
                                MessageUtil.sendMessage(lyRegionTransactionMessage);
                            }
                        }
                        for (RegionInfo regionInfo : infoToSend) {
                            sentResults.add(regionInfo.getRegionId());
                        }
                    }

                    if (round != 0) infoToSend.clear();

                    boolean blankWait = true;
                    while (blankWait) {
                        AppConfig.timestampedStandardPrint(AppConfig.regions + " " + AppConfig.blanks);
                        if (AppConfig.regions.size() == AppConfig.blanks.get(round).size()) {
                            blankWait = false;
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (!working) {
                            return;
                        }
                    }
                    AppConfig.timestampedStandardPrint("Dobio rezultate od svih suseda");

                    round += 1;
                    if (!AppConfig.blanks.containsKey(round)) AppConfig.blanks.put(round, new HashMap<>());
                }
            }

            // region transactions
//            for (Integer region : AppConfig.regions) {
//                regionVersions.put(region, 0);
//            }
//            newRegionTransactionValues.put(AppConfig.myServentInfo.getId(), collectedLYValues);
//            if (AppConfig.regions.size() > 0) {
//                while (true) {
//                    for (Integer regionInitiator : AppConfig.regions) {
//                        AppConfig.timestampedStandardPrint("Sending my new region info. " + newRegionTransactionValues);
//                        Message regionTransactionMessage = new LYRegionTransactionMessage(AppConfig.myServentInfo, AppConfig.getInfoById(regionInitiator), newRegionTransactionValues);
//                        MessageUtil.sendMessage(regionTransactionMessage);
//                    }
//
//                    boolean line = true;
//                    AppConfig.timestampedStandardPrint("Waiting for all regions to send results.");
//                    while (line) {
//                        AppConfig.timestampedStandardPrint(AppConfig.regions + " " + AppConfig.blanks);
//                        AppConfig.timestampedStandardPrint(AppConfig.regions.size() + " " + AppConfig.blanks.size());
//                        if (AppConfig.regions.size() == AppConfig.blanks.size()) {
//                            line = false;
//                        }
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//                        if (!working) {
//                            return;
//                        }
//                    }
//                    AppConfig.timestampedStandardPrint("All regions have sent the results.");
//
//                    boolean allBlanks = true;
//                    Map<Integer, Boolean> currentBlanks = new ConcurrentHashMap<>(AppConfig.blanks);
//                    AppConfig.blanks = new ConcurrentHashMap<>();
//                    AppConfig.timestampedStandardPrint(AppConfig.regions + " " + currentBlanks);
//                    for (Integer regionInitiator : AppConfig.regions) {
//                        if (!currentBlanks.get(regionInitiator)) {
//                            allBlanks = false;
//                            break;
//                        }
//                    }
//                    if (allBlanks) break;
//                    newRegionTransactionValues.clear();
//                }
//            }
//
//            for (Integer regionInitiator : collectedRegionSenders.keySet()) {
//                collectedLYValues.putAll(collectedRegionSenders.get(regionInitiator));
//            }

            AppConfig.timestampedStandardPrint("GOT TO THE END " + collectedLYValues);

            //print
            int sum;
            sum = 0;
            for (Entry<Integer, LYSnapshotResult> nodeResult : collectedLYValues.entrySet()) {
                sum += nodeResult.getValue().getRecordedAmount();
                AppConfig.timestampedStandardPrint("Recorded bitcake amount for " + nodeResult.getKey() + " = " + nodeResult.getValue().getRecordedAmount());
            }
            AppConfig.timestampedStandardPrint("All of the results collected: " + collectedLYValues);
            for (int i = 0; i < AppConfig.getServentCount(); i++) {
                for (int j = 0; j < AppConfig.getServentCount(); j++) {
                    if (i != j) {
                        for (int version = 0; version <= AppConfig.initiatorVersions.get(AppConfig.myServentInfo.getId()); version++) {
                            if (AppConfig.getInfoById(i).getNeighbors().contains(j) &&
                                    AppConfig.getInfoById(j).getNeighbors().contains(i)) {
                                if (collectedLYValues.get(i).getGiveHistory().get(new HistoryKey(AppConfig.myServentInfo.getId(), version)) != null &&
                                        collectedLYValues.get(j).getGetHistory().get(new HistoryKey(AppConfig.myServentInfo.getId(), version)) != null) {
                                    int ijAmount = collectedLYValues.get(i).getGiveHistory().get(new HistoryKey(AppConfig.myServentInfo.getId(), version)).get(j);
                                    int jiAmount = collectedLYValues.get(j).getGetHistory().get(new HistoryKey(AppConfig.myServentInfo.getId(), version)).get(i);

                                    if (ijAmount != jiAmount) {
                                        String outputString = String.format(
                                                "Unreceived bitcake amount: %d from servent %d to servent %d",
                                                ijAmount - jiAmount, i, j);
                                        AppConfig.timestampedStandardPrint(outputString);
                                        sum += ijAmount - jiAmount;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AppConfig.timestampedStandardPrint("System bitcake count: " + sum);

            resetState();
        }
    }

    @Override
    public void addResults(RegionInfo regionInfo) {
        if (round == regionInfo.getRound()) {
            AppConfig.timestampedStandardPrint("Processing new result: " + regionInfo);
            collectedSenders.add(regionInfo.getSenderId());

            Map<Integer, Boolean> newBlankValue = AppConfig.blanks.get(round);
            if (!newBlankValue.containsKey(regionInfo.getSenderId()))
                newBlankValue.put(regionInfo.getSenderId(), regionInfo.isBlank());
            if (!AppConfig.blanks.containsKey(round)) AppConfig.blanks.put(round, new HashMap<>());
            AppConfig.blanks.put(round, newBlankValue);
            if (regionInfo.isBlank()) {
                return;
            }

            RegionInfo newRegionInfo = new RegionInfo(round + 1, AppConfig.myServentInfo.getId(), regionInfo.getRegionId(), regionInfo.getRegionResults(), false);
            for (Integer node : regionInfo.getRegionResults().keySet()) {
                if (!collectedLYValues.containsKey(node)) {
                    collectedLYValues.put(node, regionInfo.getRegionResults().get(node));
                    newRegionInfo.getRegionResults().put(node, regionInfo.getRegionResults().get(node));
                }
            }
            if (!sentResults.contains(regionInfo.getRegionId())) infoToSend.add(newRegionInfo);
        } else {
            AppConfig.timestampedStandardPrint("Adding result to the queue: " + regionInfo);
            redMessages.add(regionInfo);
        }
    }

    @Override
    public boolean addRegionTransactionValue(Map<Integer, Map<Integer, LYSnapshotResult>> regionTransactionValue) {
        boolean foundNew = false;
        for (Integer regionInitiator : regionTransactionValue.keySet()) {
            if (!collectedRegionSenders.containsKey(regionInitiator)) {
                foundNew = true;
                collectedRegionSenders.put(regionInitiator, regionTransactionValue.get(regionInitiator));
                newRegionTransactionValues.put(regionInitiator, regionTransactionValue.get(regionInitiator));
            }
        }
        return foundNew;
    }

    private void resetState() {
        for (Integer initiator : AppConfig.regions) {
            AppConfig.initiatorVersions.put(initiator, AppConfig.initiatorVersions.get(initiator) + 1);
        }
        for (Integer child : AppConfig.TREE_STATE.getChildren()) {
            Message treeResetMessage = new TreeResetMessage(AppConfig.getInfoById(child), AppConfig.regions);
            MessageUtil.sendMessage(treeResetMessage);
        }

        boolean resetEnd = true;
        while (resetEnd) {
            if (AppConfig.TREE_STATE.getChildren().size() == AppConfig.treeResetConfirmed) {
                resetEnd = false;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (!working) {
                return;
            }
        }

        AppConfig.TREE_STATE.reset();
        AppConfig.blanks.clear();
        AppConfig.regions.clear();
        AppConfig.collectedChildren = 0;
        regionVersions.clear();

        collectedLYValues.clear(); //reset for next invocation
        collecting.set(false);
    }

    @Override
    public void addLYSnapshotInfo(int id, LYSnapshotResult lySnapshotResult) {
        collectedLYValues.put(id, lySnapshotResult);
    }

    @Override
    public void startCollecting() {
        boolean oldValue = this.collecting.getAndSet(true);
        if (oldValue) AppConfig.timestampedErrorPrint("Tried to start collecting before finished with previous.");
    }

    @Override
    public Map<Integer, LYSnapshotResult> getCollectedLYValues() {
        return collectedLYValues;
    }

    @Override
    public void addAllLYSnapshotInfo(Map<Integer, LYSnapshotResult> newCollectedLYValues) {
        collectedLYValues.putAll(newCollectedLYValues);
    }

    @Override
    public void stop() {
        working = false;
    }

    @Override
    public Map<Integer, Map<Integer, LYSnapshotResult>> getCollectedRegionTransactionValues() {
        return collectedRegionSenders;
    }
}
