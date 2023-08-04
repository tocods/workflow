/**
 * Copyright 2019-2020 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.workflowsim.scheduling;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerCloudlet;
import org.cloudbus.cloudsim.container.core.ContainerPod;
import org.workflowsim.WorkflowSimTags;

/**
 * MaxMin algorithm.
 *
 * @author Arman Riazi
 * @since WorkflowSim Toolkit 1.0
 * @date March 29, 2020
 */
public class MaxMinSchedulingAlgorithm extends BaseSchedulingAlgorithm {

    /**
     * Initialize a MaxMin scheduler.
     */
    public MaxMinSchedulingAlgorithm() {
        super();
    }
    /**
     * the check point list.
     */
    private final List<Boolean> hasChecked = new ArrayList<>();

    @Override
    public void run() {


        //Log.printLine("Schedulin Cycle");
        int size = getCloudletList().size();
        hasChecked.clear();
        for (int t = 0; t < size; t++) {
            hasChecked.add(false);
        }
        for (int i = 0; i < size; i++) {
            int maxIndex = 0;
            ContainerCloudlet maxCloudlet = null;
            for (int j = 0; j < size; j++) {
                ContainerCloudlet cloudlet = (ContainerCloudlet) getCloudletList().get(j);
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
                ContainerCloudlet cloudlet = (ContainerCloudlet) getCloudletList().get(j);
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
            int podId = -1;
            int vmSize = getVmList().size();
            Container firstIdleContainer = null;//(CondorPod)getVmList().get(0);
            for (int j = 0; j < vmSize; j++) {
                ContainerPod vm = (ContainerPod) getVmList().get(j);
                for (Container c: vm.getContainerList()) {
                    if(c.getState() == WorkflowSimTags.VM_STATUS_IDLE) {
                        firstIdleContainer = c;
                        podId = vm.getId();
                        break;
                    }
                }
            }
            if (firstIdleContainer == null) {
                break;
            }
            for (int j = 0; j < vmSize; j++) {
                ContainerPod vm = (ContainerPod) getVmList().get(j);
                for( Container c: vm.getContainerList()) {
                    if(c.getState() == WorkflowSimTags.VM_STATUS_IDLE
                        && c.getCurrentRequestedTotalMips() > firstIdleContainer.getCurrentRequestedTotalMips()) {
                        firstIdleContainer = c;
                        podId = vm.getId();
                    }
                }
            }
            firstIdleContainer.setState(WorkflowSimTags.VM_STATUS_BUSY);
            maxCloudlet.setContainerId(firstIdleContainer.getId());
            maxCloudlet.setVmId(podId);
            getScheduledList().add(maxCloudlet);
            Log.printLine("Schedules " + maxCloudlet.getCloudletId() + " to Pod " + podId
                    + "'s container" + firstIdleContainer.getId());

        }
    }
}
