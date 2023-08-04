package org.cloudbus.cloudsim.container.containerPodProvisioners;


import org.cloudbus.cloudsim.container.core.ContainerPod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sareh on 10/07/15.
 */
public class ContainerPodPeProvisionerSimple extends ContainerPodPeProvisioner {


    /** The pe table. */
    private Map<String, List<Double>> peTable;

    /**
     * Creates the PeProvisionerSimple object.
     *
     * @param availableMips the available mips
     *
     * @pre $none
     * @post $none
     */
    public ContainerPodPeProvisionerSimple(double availableMips) {
        super(availableMips);
        setPeTable(new HashMap<String, ArrayList<Double>>());
    }




    @Override
    public boolean allocateMipsForContainerVm(ContainerPod containerPod, double mips) {

        return allocateMipsForContainerVm(containerPod.getUid(), mips);
    }

    @Override
    public boolean allocateMipsForContainerVm(String containerVmUid, double mips) {
        if (getAvailableMips() < mips) {
            return false;
        }

        List<Double> allocatedMips;

        if (getPeTable().containsKey(containerVmUid)) {
            allocatedMips = getPeTable().get(containerVmUid);
        } else {
            allocatedMips = new ArrayList<>();
        }

        allocatedMips.add(mips);

        setAvailableMips(getAvailableMips() - mips);
        getPeTable().put(containerVmUid, allocatedMips);

        return true;
    }

    @Override
    public boolean allocateMipsForContainerVm(ContainerPod containerPod, List<Double> mips) {
        int totalMipsToAllocate = 0;
        for (double _mips : mips) {
            totalMipsToAllocate += _mips;
        }

        if (getAvailableMips() + getTotalAllocatedMipsForContainerVm(containerPod)< totalMipsToAllocate) {
            return false;
        }

        setAvailableMips(getAvailableMips() + getTotalAllocatedMipsForContainerVm(containerPod)- totalMipsToAllocate);

        getPeTable().put(containerPod.getUid(), mips);

        return true;
    }

    @Override
    public List<Double> getAllocatedMipsForContainerVm(ContainerPod containerPod) {
        if (getPeTable().containsKey(containerPod.getUid())) {
            return getPeTable().get(containerPod.getUid());
        }
        return null;
    }

    @Override
    public double getTotalAllocatedMipsForContainerVm(ContainerPod containerPod) {
        if (getPeTable().containsKey( containerPod.getUid())) {
            double totalAllocatedMips = 0.0;
            for (double mips : getPeTable().get(containerPod.getUid())) {
                totalAllocatedMips += mips;
            }
            return totalAllocatedMips;
        }
        return 0;
    }

    @Override
    public double getAllocatedMipsForContainerVmByVirtualPeId(ContainerPod containerPod, int peId) {
        if (getPeTable().containsKey(containerPod.getUid())) {
            try {
                return getPeTable().get(containerPod.getUid()).get(peId);
            } catch (Exception e) {
            }
        }
        return 0;
    }

    @Override
    public void deallocateMipsForContainerVm(ContainerPod containerPod) {
        if (getPeTable().containsKey(containerPod.getUid())) {
            for (double mips : getPeTable().get(containerPod.getUid())) {
                setAvailableMips(getAvailableMips() + mips);
            }
            getPeTable().remove(containerPod.getUid());
        }
    }

    @Override
    public void deallocateMipsForAllContainerVms() {
        super.deallocateMipsForAllContainerVms();
        getPeTable().clear();
    }
    /**
     * Gets the pe table.
     *
     * @return the peTable
     */
    protected Map<String, List<Double>> getPeTable() {
        return peTable;
    }

    /**
     * Sets the pe table.
     *
     * @param peTable the peTable to set
     */
    @SuppressWarnings("unchecked")
    protected void setPeTable(Map<String, ? extends List<Double>> peTable) {
        this.peTable = (Map<String, List<Double>>) peTable;
    }
}
