package org.cloudbus.cloudsim.container.hostSelectionPolicies;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.core.ContainerHost;

import java.util.List;
import java.util.Set;

public class HostSelectionPolicyK8s extends HostSelectionPolicy{


    private double leastRequestedPriority(ContainerHost host) {
        int free_pes = host.getNumberOfFreePes();
        int total_pes = host.getNumberOfPes();
        double cpu_score = 10 * free_pes / total_pes;
        float free_ram = host.getContainerVmRamProvisioner().getAvailableRam();
        float total_ram = host.getContainerVmRamProvisioner().getRam();
        double ram_score = 10 * free_ram / total_ram;
        return (cpu_score + ram_score) / 2;
    }

    private double balancedResourceAllocation(ContainerHost host) {
        double cpu_fraction = (host.getNumberOfPes() - host.getNumberOfFreePes()) / host.getNumberOfPes();
        double ram_fraction = (host.getRam() - host.getContainerVmRamProvisioner().getAvailableRam()) / host.getRam();
        double storage_fraction = host.getStorage() / host.getTotal_storage();
        double mean = (cpu_fraction + ram_fraction + storage_fraction) / 3;
        double variance = ((cpu_fraction - mean)*(cpu_fraction - mean)
                + (ram_fraction - mean)*(ram_fraction - mean)
                + (storage_fraction - mean)*(storage_fraction - mean)) / 3;
        return 10 - variance * 10;
    }

    private double getScore(ContainerHost host) {
        return (balancedResourceAllocation(host) + leastRequestedPriority(host)) / 2;
    }


    @Override
    public ContainerHost getHost(List<ContainerHost> hostList, Object obj, Set<? extends ContainerHost> excludedHostList) {
        double maxScore = Double.MIN_VALUE;
        ContainerHost selectedHost = null;
        for (ContainerHost host: hostList) {
            double score;
            if (excludedHostList.contains(host)) {
                continue;
            }
            score = getScore(host);
            Log.printLine("host " + host.getId() + " 's score: " + score);
            if(score > maxScore) {
                maxScore = score;
                selectedHost = host;
            }
        }
        return selectedHost;
    }
}
