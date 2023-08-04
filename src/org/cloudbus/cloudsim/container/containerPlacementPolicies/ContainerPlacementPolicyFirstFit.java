package org.cloudbus.cloudsim.container.containerPlacementPolicies;


import org.cloudbus.cloudsim.container.core.ContainerPod;

import java.util.List;
import java.util.Set;

/**
 * Created by sareh fotuhi Piraghaj on 16/12/15.
 * For container placement First Fit policy.
 */

public class ContainerPlacementPolicyFirstFit extends ContainerPlacementPolicy {

    @Override
    public ContainerPod getContainerVm(List<ContainerPod> vmList, Object obj, Set<? extends ContainerPod> excludedVmList) {
        ContainerPod containerPod = null;
        for (ContainerPod containerPod1 : vmList) {
            if (excludedVmList.contains(containerPod1)) {
                continue;
            }
            containerPod = containerPod1;
            break;
        }
        return containerPod;
    }

}
