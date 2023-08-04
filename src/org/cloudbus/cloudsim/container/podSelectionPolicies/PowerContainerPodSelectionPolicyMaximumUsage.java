package org.cloudbus.cloudsim.container.podSelectionPolicies;

import org.cloudbus.cloudsim.container.core.ContainerPod;
import org.cloudbus.cloudsim.container.core.PowerContainerHost;
import org.cloudbus.cloudsim.container.core.PowerContainerPod;

import java.util.List;

/**
 * Created by sareh on 16/11/15.
 */
public class PowerContainerPodSelectionPolicyMaximumUsage extends PowerContainerPodSelectionPolicy {
    /*
     * (non-Javadoc)
     * @see
     * org.cloudbus.cloudsim.experiments.power.PowerPodSelectionPolicy#getVmsToMigrate(org.cloudbus
     * .cloudsim.power.PowerHost)
     */
    @Override
    public ContainerPod getVmToMigrate(PowerContainerHost host) {
        List<PowerContainerPod> migratableContainers = getMigratableVms(host);
        if (migratableContainers.isEmpty()) {
            return null;
        }
        ContainerPod VmsToMigrate = null;
        double maxMetric = Double.MIN_VALUE;
        for (ContainerPod vm : migratableContainers) {
            if (vm.isInMigration()) {
                continue;
            }
            double metric = vm.getCurrentRequestedTotalMips();
            if (maxMetric < metric) {
                maxMetric = metric;
                VmsToMigrate = vm;
            }
        }
//        Log.formatLine("The Container To migrate is #%d from VmID %d from host %d", containerToMigrate.getId(),containerToMigrate.getVm().getId(), host.getId());
        return VmsToMigrate;
    }


}
