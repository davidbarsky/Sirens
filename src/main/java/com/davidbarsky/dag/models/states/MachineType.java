package com.davidbarsky.dag.models.states;

import com.davidbarsky.dag.DAGException;

import java.util.EnumMap;
import java.util.Map;

// Associated values are costs of the machine
public enum MachineType {
    SMALL, LARGE;

    public Integer getCost() {
        switch (this) {
            case SMALL: return 1;
            case LARGE: return 5;
            default: throw new DAGException("Invariant broken; MachineType is not defined");
        }
    }
    
    public static Map<MachineType, Integer> latencyMap(int lat) {
    	Map<MachineType, Integer> toR = new EnumMap<>(MachineType.class);
    	toR.put(SMALL, lat);
    	toR.put(LARGE, lat);
    	return toR;
    }

    public static MachineType[] bySize() {
        return new MachineType[] { SMALL, LARGE };
    }

}
