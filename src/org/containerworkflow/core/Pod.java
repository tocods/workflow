package org.containerworkflow.core;

import org.cloudbus.cloudsim.container.schedulers.ContainerCloudletScheduler;

import java.util.ArrayList;
import java.util.List;

public class Pod extends Application{
    private List<Application> applications;

    /**
     * Creates a new Container object.
     *
     * @param id
     * @param userId
     * @param mips
     * @param numberOfPes
     * @param ram
     * @param bw
     * @param size
     * @param containerManager
     * @param containerCloudletScheduler
     * @param schedulingInterval
     */
    public Pod(int id, int userId, double mips, int numberOfPes, int ram, long bw, long size, String containerManager, ContainerCloudletScheduler containerCloudletScheduler, double schedulingInterval) {
        super(id, userId, mips, numberOfPes, ram, bw, size, containerManager, containerCloudletScheduler, schedulingInterval);
        this.applications = new ArrayList<>();
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public List<Application> getApplications() {
        return this.applications;
    }

}
