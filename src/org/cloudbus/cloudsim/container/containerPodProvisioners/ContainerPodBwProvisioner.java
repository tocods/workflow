package org.cloudbus.cloudsim.container.containerPodProvisioners;


import org.cloudbus.cloudsim.container.core.ContainerPod;

/**
 * Created by sareh on 10/07/15.
 */
public abstract class ContainerPodBwProvisioner {

    /**
     * The bw.
     */
    private long bw;

    /**
     * The available bw.
     */
    private long availableBw;

    /**
     * Creates the new BwProvisioner.
     *
     * @param bw overall amount of bandwidth available in the host.
     * @pre bw >= 0
     * @post $none
     */
    public ContainerPodBwProvisioner(long bw) {
        setBw(bw);
        setAvailableBw(bw);
    }

    /**
     * Allocates BW for a given VM.
     *
     * @param containerPod virtual machine for which the bw are being allocated
     * @param bw          the bw
     * @return $true if the bw could be allocated; $false otherwise
     * @pre $none
     * @post $none
     */
    public abstract boolean allocateBwForContainerVm(ContainerPod containerPod, long bw);

    /**
     * Gets the allocated BW for VM.
     *
     * @param containerPod the VM
     * @return the allocated BW for vm
     */
    public abstract long getAllocatedBwForContainerVm(ContainerPod containerPod);

    /**
     * Releases BW used by a VM.
     *
     * @param containerPod the vm
     * @pre $none
     * @post none
     */
    public abstract void deallocateBwForContainerVm(ContainerPod containerPod);

    /**
     * Releases BW used by a all VMs.
     *
     * @pre $none
     * @post none
     */
    public void deallocateBwForAllContainerVms() {
        setAvailableBw(getBw());
    }

    /**
     * Checks if BW is suitable for vm.
     *
     * @param containerPod the vm
     * @param bw          the bw
     * @return true, if BW is suitable for vm
     */
    public abstract boolean isSuitableForContainerVm(ContainerPod containerPod, long bw);

    /**
     * Gets the bw.
     *
     * @return the bw
     */
    public long getBw() {
        return bw;
    }

    /**
     * Sets the bw.
     *
     * @param bw the new bw
     */
    protected void setBw(long bw) {
        this.bw = bw;
    }

    /**
     * Gets the available BW in the host.
     *
     * @return available bw
     * @pre $none
     * @post $none
     */
    public long getAvailableBw() {
        return availableBw;
    }

    /**
     * Gets the amount of used BW in the host.
     *
     * @return used bw
     * @pre $none
     * @post $none
     */
    public long getUsedBw() {
        return bw - availableBw;
    }

    /**
     * Sets the available bw.
     *
     * @param availableBw the new available bw
     */
    protected void setAvailableBw(long availableBw) {
        this.availableBw = availableBw;
    }
}
