package org.cloudbus.cloudsim.container.containerPlacementPolicies;

import org.cloudbus.cloudsim.container.core.ContainerPod;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh fotuhi Piraghaj on 16/12/15.
 * For container placement Most-Full policy.
 */
public class ContainerPlacementPolicyMostFull extends ContainerPlacementPolicy {

    @Override
    public ContainerPod getContainerVm(List<ContainerPod> vmList, Object obj, Set<? extends ContainerPod> excludedVmList) {
        ContainerPod selectedVm = null;
        double maxMips = Double.MIN_VALUE;

        for (ContainerPod containerPod1 : vmList) {
            if (excludedVmList.contains(containerPod1)) {
                continue;
            }

            double containerUsage = containerPod1.getContainerScheduler().getAvailableMips();
            if ( containerUsage > maxMips) {
                maxMips = containerUsage;
                selectedVm = containerPod1;

            }
        }

        return selectedVm;
    }
}
