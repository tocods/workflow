package org.cloudbus.cloudsim.container.containerPodProvisioners;

import org.cloudbus.cloudsim.container.core.ContainerPod;

import java.util.List;

/**
 * Created by sareh on 10/07/15.
 */
public abstract class ContainerPodPeProvisioner {


    /** The mips. */
    private double mips;

    /** The available mips. */
    private double availableMips;

    /**
     * Creates the new PeProvisioner.
     *
     * @param mips overall amount of MIPS available in the Pe
     *
     * @pre mips>=0
     * @post $none
     */
    public ContainerPodPeProvisioner(double mips) {
        // TODO Auto-generated constructor stub
        setMips(mips);
        setAvailableMips(mips);
    }
    /**
     * Allocates MIPS for a given containerPod.
     *
     * @param containerPod virtual machine for which the MIPS are being allocated
     * @param mips the mips
     *
     * @return $true if the MIPS could be allocated; $false otherwise
     *
     * @pre $none
     * @post $none
     */
    public abstract boolean allocateMipsForContainerVm(ContainerPod containerPod, double mips);

    /**
     * Allocates MIPS for a given VM.
     *
     * @param containerVmUid the containerVmUid
     * @param mips the mips
     *
     * @return $true if the MIPS could be allocated; $false otherwise
     *
     * @pre $none
     * @post $none
     */
    public abstract boolean allocateMipsForContainerVm(String containerVmUid, double mips);

    /**
     * Allocates MIPS for a given VM.
     *
     * @param containerPod virtual machine for which the MIPS are being allocated
     * @param mips the mips for each virtual Pe
     *
     * @return $true if the MIPS could be allocated; $false otherwise
     *
     * @pre $none
     * @post $none
     */
    public abstract boolean allocateMipsForContainerVm(ContainerPod containerPod, List<Double> mips);

    /**
     * Gets allocated MIPS for a given VM.
     *
     * @param containerPod virtual machine for which the MIPS are being allocated
     *
     * @return array of allocated MIPS
     *
     * @pre $none
     * @post $none
     */
    public abstract List<Double> getAllocatedMipsForContainerVm(ContainerPod containerPod);

    /**
     * Gets total allocated MIPS for a given VM for all PEs.
     *
     * @param containerPod virtual machine for which the MIPS are being allocated
     *
     * @return total allocated MIPS
     *
     * @pre $none
     * @post $none
     */
    public abstract double getTotalAllocatedMipsForContainerVm(ContainerPod containerPod);

    /**
     * Gets allocated MIPS for a given VM for a given virtual Pe.
     *
     * @param containerPod virtual machine for which the MIPS are being allocated
     * @param peId the pe id
     *
     * @return allocated MIPS
     *
     * @pre $none
     * @post $none
     */
    public abstract double getAllocatedMipsForContainerVmByVirtualPeId(ContainerPod containerPod, int peId);

    /**
     * Releases MIPS used by a VM.
     *
     * @param containerPod the containerPod
     *
     * @pre $none
     * @post none
     */
    public abstract void deallocateMipsForContainerVm(ContainerPod containerPod);

    /**
     * Releases MIPS used by all VMs.
     *
     * @pre $none
     * @post none
     */
    public void deallocateMipsForAllContainerVms() {
        setAvailableMips(getMips());
    }

    /**
     * Gets the MIPS.
     *
     * @return the MIPS
     */
    public double getMips() {
        return mips;
    }

    /**
     * Sets the MIPS.
     *
     * @param mips the MIPS to set
     */
    public void setMips(double mips) {
        this.mips = mips;
    }

    /**
     * Gets the available MIPS in the PE.
     *
     * @return available MIPS
     *
     * @pre $none
     * @post $none
     */
    public double getAvailableMips() {
        return availableMips;
    }

    /**
     * Sets the available MIPS.
     *
     * @param availableMips the availableMips to set
     */
    protected void setAvailableMips(double availableMips) {
        this.availableMips = availableMips;
    }

    /**
     * Gets the total allocated MIPS.
     *
     * @return the total allocated MIPS
     */
    public double getTotalAllocatedMips() {
        double totalAllocatedMips = getMips() - getAvailableMips();
        if (totalAllocatedMips > 0) {
            return totalAllocatedMips;
        }
        return 0;
    }

    /**
     * Gets the utilization of the Pe in percents.
     *
     * @return the utilization
     */
    public double getUtilization() {
        return getTotalAllocatedMips() / getMips();
    }


}
