package org.containerworkflow.clustering;


import org.containerworkflow.core.Application;
import org.containerworkflow.core.Pod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PodClustering {

    private List<Application> containerList;

    private final List<Pod> podList;

    private Map<Integer, Integer> podId2ListId;


    public PodClustering() {
        this.containerList = new ArrayList<>();
        this.podList = new ArrayList<>();
        this.podId2ListId = new HashMap<>();
    }

    public final void setContainerList(List<Application> containers) {
        this.containerList = containers;
    }

    public final List<Application> getContainerList() {
        return this.containerList;
    }

    public final List<Pod> getPodList() {
        return this.podList;
    }

    protected final Pod assContainer2Pod(List<Application> containerList) {
        if (containerList == null || containerList.size() == 0) {
            return null;
        }
        return null;
    }

}
