package app.snapshot_bitcake;

import java.io.Serializable;
import java.util.Objects;

public class HistoryKey implements Serializable {
    private int initiator;
    private int version;

    public HistoryKey(int neighbour, int version) {
        this.initiator = neighbour;
        this.version = version;
    }

    public int getInitiator() {
        return initiator;
    }

    public void setInitiator(int initiator) {
        this.initiator = initiator;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HistoryKey that = (HistoryKey) o;
        return initiator == that.initiator && version == that.version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(initiator, version);
    }

    @Override
    public String toString() {
        return "HistoryKey{" +
                "initiator=" + initiator +
                ", version=" + version +
                '}';
    }
}
