package org.cloudbus.cloudsim.container.containerPodProvisioners;

import org.cloudbus.cloudsim.container.core.ContainerPod;

/**
 * Created by sareh on 10/07/15.
 */
public abstract class ContainerPodRamProvisioner {

    /**
     * The ram.
     */
    private float ram;

    /**
     * The available availableRam.
     */
    private float availableRam;


    /**
     * Creates new Containervm Ram Provisioner
     *
     * @param availableContainerVmRam the vm ram
     */
    public ContainerPodRamProvisioner(float availableContainerVmRam) {
        setRam(availableContainerVmRam);
        setAvailableRam(availableContainerVmRam);
    }

    /**
     * allocate hosts ram to the VM
     *
     * @param containerPod the containerPod
     * @param ram       the ram
     * @return $true if successful
     */
    public abstract boolean allocateRamForContainerVm(ContainerPod containerPod, float ram);

    /**
     * Get the allocated ram of the containerPod
     *
     * @param containerPod the containerPod
     * @return the allocated ram of the containerVM
     */
    public abstract float getAllocatedRamForContainerVm(ContainerPod containerPod);

    /**
     * Release the allocated ram amount of the containerPod
     *
     * @param containerPod the containerPod
     */
    public abstract void deallocateRamForContainerVm(ContainerPod containerPod);

    /**
     * Release the allocated ram of the vm.
     */
    public void deallocateRamForAllContainerVms() {
        setAvailableRam(getRam());
    }


    /**
     * It checks whether or not the vm have enough ram for the containerPod
     *
     * @param containerPod the containerPod
     * @param ram       the vm's ram
     * @return $ture if it is suitable
     */
    public abstract boolean isSuitableForContainerVm(ContainerPod containerPod, float ram);

    /**
     * get the allocated ram of the Pod
     *
     * @return the used ram of the Pod
     */
    public float getUsedVmRam() {
        return getRam() - availableRam;
    }


    /**
     * get the available ram
     *
     * @return the available ram of the Pod
     */
    public float getAvailableRam() {
        return availableRam;
    }


    /**
     * It sets the available Ram of the virtual machine
     *
     * @param availableRam the availableRam
     */
    public void setAvailableRam(float availableRam) {
        this.availableRam = availableRam;
    }

    /**
     * @return the ram
     */
    public float getRam() {
        return ram;
    }

    /**
     * @param ram the ram to set
     */
    public void setRam(float ram) {
        this.ram = ram;
    }

}


