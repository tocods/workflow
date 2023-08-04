package org.containerworkflow.scheduling;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.core.ContainerCloudlet;
import org.cloudbus.cloudsim.container.core.ContainerPod;
import org.workflowsim.CondorPod;
import org.workflowsim.WorkflowSimTags;

import java.util.ArrayList;
import java.util.List;

public class MaxMinSchedulingAlgorithm {
    /**
     * the job list
     */
    private List<? extends Cloudlet> cloudletList;

    /**
     * the host list
     */
    private List<? extends Host> hostList;

    /**
     * the scheduled job list
     */
    private List<Cloudlet> scheduledList;

    private final List<Boolean> hasChecked = new ArrayList<>();

    public MaxMinSchedulingAlgorithm() {
        this.scheduledList = new ArrayList<>();
    }

    public void run() {
        int size = this.cloudletList.size();
        hasChecked.clear();
        for(int t= 0; t < size; t ++) {
            hasChecked.add(false);
        }
        for(int i = 0; i < size; i ++) {
            int maxIndex = 0;
            ContainerCloudlet maxCloudlet = null;
            for (int j = 0; j < size; j++) {
                ContainerCloudlet cloudlet = (ContainerCloudlet) this.cloudletList.get(j);
                if (!hasChecked.get(j)) {
                    maxCloudlet = cloudlet;
                    maxIndex = j;
                    break;
                }
            }
            if (maxCloudlet == null) {
                break;
            }

            for (int j = 0; j < size; j++) {
                ContainerCloudlet cloudlet = (ContainerCloudlet) this.cloudletList.get(j);
                if (hasChecked.get(j)) {
                    continue;
                }
                long length = cloudlet.getCloudletLength();
                if (length > maxCloudlet.getCloudletLength()) {
                    maxCloudlet = cloudlet;
                    maxIndex = j;
                }
            }
            hasChecked.set(maxIndex, true);
            int hostSize = this.hostList.size();
            Host firstIdleHost = (Host) this.hostList.get(0);

            for (int j = 0; j < hostSize; j++) {
                Host host = (Host) this.hostList.get(j);
                if (host.getVmScheduler().getAvailableMips() > firstIdleHost.getVmScheduler().getAvailableMips()) {
                    firstIdleHost = host;

                }
            }
            // firstIdleHost.setState(WorkflowSimTags.VM_STATUS_BUSY);
            maxCloudlet.setVmId(firstIdleHost.getId());
            this.scheduledList.add(maxCloudlet);
            Log.printLine("Schedules " + maxCloudlet.getCloudletId() + " with "
                    + maxCloudlet.getCloudletLength() + " to host " + firstIdleHost.getId()
                    + " with " + firstIdleHost.getVmScheduler().getAvailableMips());
        }
    }
}
