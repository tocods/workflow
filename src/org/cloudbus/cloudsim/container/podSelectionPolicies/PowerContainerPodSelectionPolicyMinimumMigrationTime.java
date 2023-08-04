package org.cloudbus.cloudsim.container.podSelectionPolicies;

import org.cloudbus.cloudsim.container.core.*;

import java.util.List;

/**
 * Created by sareh on 30/07/15.
 */
public class PowerContainerPodSelectionPolicyMinimumMigrationTime extends PowerContainerPodSelectionPolicy {



    @Override
    public ContainerPod getVmToMigrate(PowerContainerHost host) {
        List<PowerContainerPod> migratableVms = getMigratableVms(host);
        if (migratableVms.isEmpty()) {
            return null;
        }
        ContainerPod vmToMigrate = null;
        double minMetric = Double.MAX_VALUE;
        for (ContainerPod vm : migratableVms) {
            if (vm.isInMigration()) {
                continue;
            }
            double metric = vm.getRam();
            if (metric < minMetric) {
                minMetric = metric;
                vmToMigrate = vm;
            }
        }
        return vmToMigrate;
    }



}
