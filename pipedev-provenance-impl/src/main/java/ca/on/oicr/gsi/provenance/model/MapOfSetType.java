package ca.on.oicr.gsi.provenance.model;

import java.util.ArrayList;
import java.util.List;

public class MapOfSetType {

    private List<MapOfSetEntryType> entry = new ArrayList<>();

    public List<MapOfSetEntryType> getEntry() {
        return entry;
    }

    public void setEntry(List<MapOfSetEntryType> entry) {
        this.entry = entry;
    }
}
