package org.cloudbus.cloudsim.container.containerPodProvisioners;

import org.cloudbus.cloudsim.container.core.ContainerPod;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sareh on 10/07/15.
 */
public class ContainerPodRamProvisionerSimple extends ContainerPodRamProvisioner {

    /**
     * The RAM table.
     */
    private Map<String, Float> containerVmRamTable;

    /**
     * @param availableRam the available ram
     */
    public ContainerPodRamProvisionerSimple(int availableRam) {
        super(availableRam);
        setContainerVmRamTable(new HashMap<String, Float>());
    }

    @Override
    public boolean allocateRamForContainerVm(ContainerPod containerPod, float ram) {
        float maxRam = containerPod.getRam();

        if (ram >= maxRam) {
            ram = maxRam;
        }

        deallocateRamForContainerVm(containerPod);

        if (getAvailableRam() >= ram) {
            setAvailableRam(getAvailableRam() - ram);
            getContainerVmRamTable().put(containerPod.getUid(), ram);
            containerPod.setCurrentAllocatedRam(getAllocatedRamForContainerVm(containerPod));
            return true;
        }

        containerPod.setCurrentAllocatedRam(getAllocatedRamForContainerVm(containerPod));

        return false;
    }

    @Override
    public float getAllocatedRamForContainerVm(ContainerPod containerPod) {
        if (getContainerVmRamTable().containsKey(containerPod.getUid())) {
            return getContainerVmRamTable().get(containerPod.getUid());
        }
        return 0;
    }

    @Override
    public void deallocateRamForContainerVm(ContainerPod containerPod) {
        if (getContainerVmRamTable().containsKey(containerPod.getUid())) {
            float amountFreed = getContainerVmRamTable().remove(containerPod.getUid());
            setAvailableRam(getAvailableRam() + amountFreed);
            containerPod.setCurrentAllocatedRam(0);
        }

    }


    @Override
    public void deallocateRamForAllContainerVms() {
        super.deallocateRamForAllContainerVms();
        getContainerVmRamTable().clear();
    }

    @Override
    public boolean isSuitableForContainerVm(ContainerPod containerPod, float ram) {
        float allocatedRam = getAllocatedRamForContainerVm(containerPod);
        boolean result = allocateRamForContainerVm(containerPod, ram);
        deallocateRamForContainerVm(containerPod);
        if (allocatedRam > 0) {
            allocateRamForContainerVm(containerPod, allocatedRam);
        }
        return result;
    }


    /**
     * @return the containerVmRamTable
     */
    protected Map<String, Float> getContainerVmRamTable() {
        return containerVmRamTable;
    }

    /**
     * @param containerVmRamTable the containerVmRamTable to set
     */
    protected void setContainerVmRamTable(Map<String, Float> containerVmRamTable) {
        this.containerVmRamTable = containerVmRamTable;
    }

}
