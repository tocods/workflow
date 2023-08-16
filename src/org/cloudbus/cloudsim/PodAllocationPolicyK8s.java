package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PodAllocationPolicyK8s extends PodAllocationPolicy{
    /** The map between each VM and its allocated host.
     * The map key is a VM UID and the value is the allocated host for that VM. */
    private Map<String, Host> vmTable;

    /** The map between each VM and the number of Pes used.
     * The map key is a VM UID and the value is the number of used Pes for that VM. */
    private Map<String, Integer> usedPes;

    /** The number of free Pes for each host from {@link #getHostList() }. */
    private List<Integer> freePes;

    /**
     * Creates a new PodAllocationPolicySimple object.
     *
     * @param list the list of hosts
     * @pre $none
     * @post $none
     */
    public PodAllocationPolicyK8s(List<? extends Host> list) {
        super(list);

        setFreePes(new ArrayList<Integer>());
        for (Host host : getHostList()) {
            getFreePes().add(host.getNumberOfPes());

        }

        setVmTable(new HashMap<String, Host>());
        setUsedPes(new HashMap<String, Integer>());
    }

    private double leastRequestedPriority(Host host) {
        int free_pes = host.getNumberOfFreePes();
        int total_pes = host.getNumberOfPes();
        double cpu_score = 10 * free_pes / total_pes;
        int free_ram = host.getRamProvisioner().getAvailableRam();
        int total_ram = host.getRamProvisioner().getRam();
        double ram_score = 10 * free_ram / total_ram;
        return (cpu_score + ram_score) / 2;
    }

    private double balancedResourceAllocation(Host host) {
        double cpu_fraction = (host.getNumberOfPes() - host.getNumberOfFreePes()) / host.getNumberOfPes();
        double ram_fraction = (host.getRam() - host.getRamProvisioner().getAvailableRam()) / host.getRam();
        double storage_fraction = host.getStorage() / host.getTotal_storage();
        double mean = (cpu_fraction + ram_fraction + storage_fraction) / 3;
        double variance = ((cpu_fraction - mean)*(cpu_fraction - mean)
                + (ram_fraction - mean)*(ram_fraction - mean)
                + (storage_fraction - mean)*(storage_fraction - mean)) / 3;
        return 10 - variance * 10;
    }

    private double getScore(Host host) {
        return (balancedResourceAllocation(host) + leastRequestedPriority(host)) / 2;
    }

    /**
     * Allocates the host with less PEs in use for a given VM.
     *
     * @param pod {@inheritDoc}
     * @return {@inheritDoc}
     * @pre $none
     * @post $none
     */
    @Override
    public boolean allocateHostForVm(Pod pod) {
        int requiredPes = pod.getNumberOfPes();
        boolean result = false;
        int tries = 0;
        List<Integer> freePesTmp = new ArrayList<Integer>();
        for (Integer freePes : getFreePes()) {
            freePesTmp.add(freePes);
        }

        if (!getVmTable().containsKey(pod.getUid())) { // if this pod was not created
            do {// we still trying until we find a host or until we try all of them
                double score = Double.MIN_VALUE;
                int idx = -1;

                // we want the host with less pes in use
                /*for (int i = 0; i < freePesTmp.size(); i++) {
                    if (freePesTmp.get(i) > moreFree) {
                        moreFree = freePesTmp.get(i);
                        idx = i;
                    }
                }*/
                for(int i = 0; i < getHostList().size(); i++) {
                    double score_ = getScore(getHostList().get(i));
                    if(score_ > score) {
                        score = score_;
                        idx = i;
                    }
                }


                Host host = getHostList().get(idx);
                result = host.vmCreate(pod);

                if (result) { // if pod were succesfully created in the host
                    getVmTable().put(pod.getUid(), host);
                    getUsedPes().put(pod.getUid(), requiredPes);
                    getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
                    result = true;
                    break;
                } else {
                    freePesTmp.set(idx, Integer.MIN_VALUE);
                }
                tries++;
            } while (!result && tries < getFreePes().size());

        }

        return result;
    }

    @Override
    public void deallocateHostForVm(Pod pod) {
        Host host = getVmTable().remove(pod.getUid());
        int idx = getHostList().indexOf(host);
        int pes = getUsedPes().remove(pod.getUid());
        if (host != null) {
            host.vmDestroy(pod);
            getFreePes().set(idx, getFreePes().get(idx) + pes);
        }
    }

    @Override
    public Host getHost(Pod pod) {
        return getVmTable().get(pod.getUid());
    }

    @Override
    public Host getHost(int vmId, int userId) {
        return getVmTable().get(Pod.getUid(userId, vmId));
    }

    /**
     * Gets the vm table.
     *
     * @return the vm table
     */
    public Map<String, Host> getVmTable() {
        return vmTable;
    }

    /**
     * Sets the vm table.
     *
     * @param vmTable the vm table
     */
    protected void setVmTable(Map<String, Host> vmTable) {
        this.vmTable = vmTable;
    }

    /**
     * Gets the used pes.
     *
     * @return the used pes
     */
    protected Map<String, Integer> getUsedPes() {
        return usedPes;
    }

    /**
     * Sets the used pes.
     *
     * @param usedPes the used pes
     */
    protected void setUsedPes(Map<String, Integer> usedPes) {
        this.usedPes = usedPes;
    }

    /**
     * Gets the free pes.
     *
     * @return the free pes
     */
    protected List<Integer> getFreePes() {
        return freePes;
    }

    /**
     * Sets the free pes.
     *
     * @param freePes the new free pes
     */
    protected void setFreePes(List<Integer> freePes) {
        this.freePes = freePes;
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Pod> vmList) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean allocateHostForVm(Pod pod, Host host) {
        if (host.vmCreate(pod)) { // if pod has been succesfully created in the host
            getVmTable().put(pod.getUid(), host);

            int requiredPes = pod.getNumberOfPes();
            int idx = getHostList().indexOf(host);
            getUsedPes().put(pod.getUid(), requiredPes);
            getFreePes().set(idx, getFreePes().get(idx) - requiredPes);

            Log.formatLine(
                    "%.2f: VM #" + pod.getId() + " has been allocated to the host #" + host.getId(),
                    CloudSim.clock());
            return true;
        }

        return false;
    }
}
