package org.cloudbus.cloudsim.container.containerPodProvisioners;

import org.cloudbus.cloudsim.container.core.ContainerPod;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sareh on 10/07/15.
 */
public class ContainerPodBwProvisionerSimple extends ContainerPodBwProvisioner {

    /**
     * The bw table.
     */
    private Map<String, Long> bwTable;

    /**
     * Instantiates a new bw provisioner simple.
     *
     * @param bw the bw
     */
    public ContainerPodBwProvisionerSimple(long bw) {
        super(bw);
        setBwTable(new HashMap<String, Long>());
    }


    @Override
    public boolean allocateBwForContainerVm(ContainerPod containerPod, long bw) {
        deallocateBwForContainerVm(containerPod);

        if (getAvailableBw() >= bw) {
            setAvailableBw(getAvailableBw() - bw);
            getBwTable().put(containerPod.getUid(), bw);
            containerPod.setCurrentAllocatedBw(getAllocatedBwForContainerVm(containerPod));
            return true;
        }

        containerPod.setCurrentAllocatedBw(getAllocatedBwForContainerVm(containerPod));
        return false;
    }

    @Override
    public long getAllocatedBwForContainerVm(ContainerPod containerPod) {
        if (getBwTable().containsKey(containerPod.getUid())) {
            return getBwTable().get(containerPod.getUid());
        }
        return 0;
    }

    @Override
    public void deallocateBwForContainerVm(ContainerPod containerPod) {
        if (getBwTable().containsKey(containerPod.getUid())) {
            long amountFreed = getBwTable().remove(containerPod.getUid());
            setAvailableBw(getAvailableBw() + amountFreed);
            containerPod.setCurrentAllocatedBw(0);
        }

    }

    /*
         * (non-Javadoc)
         * ContainerPodBwProvisioner#deallocateBwForAllContainerVms
         */
    @Override
    public void deallocateBwForAllContainerVms() {
        super.deallocateBwForAllContainerVms();
        getBwTable().clear();
    }

    @Override
    public boolean isSuitableForContainerVm(ContainerPod containerPod, long bw) {
        long allocatedBw = getAllocatedBwForContainerVm(containerPod);
        boolean result = allocateBwForContainerVm(containerPod, bw);
        deallocateBwForContainerVm(containerPod);
        if (allocatedBw > 0) {
            allocateBwForContainerVm(containerPod, allocatedBw);
        }
        return result;
    }


    /**
     * Gets the bw table.
     *
     * @return the bw table
     */
    protected Map<String, Long> getBwTable() {
        return bwTable;
    }

    /**
     * Sets the bw table.
     *
     * @param bwTable the bw table
     */
    protected void setBwTable(Map<String, Long> bwTable) {
        this.bwTable = bwTable;
    }
}
