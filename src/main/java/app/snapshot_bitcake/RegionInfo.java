package app.snapshot_bitcake;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class RegionInfo implements Serializable {
    private final int round;
    private final int senderId;
    private final int regionId;
    private final Map<Integer, LYSnapshotResult> regionResults;
    private final boolean blank;

    public RegionInfo(int round, int senderId, int regionId, Map<Integer, LYSnapshotResult> regionResults, boolean blank) {
        this.round = round;
        this.senderId = senderId;
        this.regionId = regionId;
        this.regionResults = regionResults;
        this.blank = blank;
    }

    public int getRound() {
        return round;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getRegionId() {
        return regionId;
    }

    public boolean isBlank() {
        return blank;
    }

    public Map<Integer, LYSnapshotResult> getRegionResults() {
        return regionResults;
    }

    @Override
    public String toString() {
        return "RegionInfo{" +
                "round=" + round +
                ", senderId=" + senderId +
                ", regionId=" + regionId +
                ", regionResults=" + regionResults +
                ", blank=" + blank +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionInfo that = (RegionInfo) o;
        return round == that.round && senderId == that.senderId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(round, senderId);
    }
}
