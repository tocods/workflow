package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.container.containerPodProvisioners.ContainerPodBwProvisioner;
import org.cloudbus.cloudsim.container.containerPodProvisioners.ContainerPodPe;
import org.cloudbus.cloudsim.container.containerPodProvisioners.ContainerPodRamProvisioner;
import org.cloudbus.cloudsim.container.lists.ContainerPodPeList;
import org.cloudbus.cloudsim.container.schedulers.ContainerPodScheduler;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import java.util.ArrayList;
import java.util.List;
import org.wfc.core.WFCDatacenter;

/**
 * Created by sareh on 10/07/15.
 */
public class ContainerHost {


    /**
     * The id.
     */
    private int id;

    /**
     * The storage.
     */
    private long storage;

    private long total_storage;

    /**
     * The ram provisioner.
     */
    private ContainerPodRamProvisioner containerPodRamProvisioner;

    /**
     * The bw provisioner.
     */
    private ContainerPodBwProvisioner containerPodBwProvisioner;

    /**
     * The allocation policy.
     */
    private ContainerPodScheduler containerPodScheduler;

    /**
     * The vm list.
     */
    private final List<? extends ContainerPod> vmList = new ArrayList<>();
    /**
     * The vm list.
     */

    /**
     * The pe list.
     */
    private List<? extends ContainerPodPe> peList;

    /**
     * Tells whether this machine is working properly or has failed.
     */
    private boolean failed;

    /**
     * The vms migrating in.
     */
    private final List<ContainerPod> vmsMigratingIn = new ArrayList<>();
    /**
     * The datacenter where the host is placed.
     */
    private WFCDatacenter datacenter;

    /**
     * Instantiates a new host.
     *
     * @param id             the id
     * @param containerPodRamProvisioner the ram provisioner
     * @param containerPodBwProvisioner  the bw provisioner
     * @param storage        the storage
     * @param peList         the pe list
     * @param containerPodScheduler    the vm scheduler
     */
    public ContainerHost(
            int id,
            
            ContainerPodRamProvisioner containerPodRamProvisioner,
            ContainerPodBwProvisioner containerPodBwProvisioner,
            long storage,
            List<? extends ContainerPodPe> peList,
            ContainerPodScheduler containerPodScheduler) {
        this.total_storage = storage;
        setId(id);
        setContainerVmRamProvisioner(containerPodRamProvisioner);
        setContainerVmBwProvisioner(containerPodBwProvisioner);
        setStorage(storage);
        setContainerVmScheduler(containerPodScheduler);
        setPeList(peList);
        setFailed(false);

    }

    public long getTotal_storage() {
        return this.total_storage;
    }

    /**
     * Requests updating of processing of cloudlets in the VMs running in this host.
     *
     * @param currentTime the current time
     * @return expected time of completion of the next cloudlet in all VMs in this host.
     * Double.MAX_VALUE if there is no future events expected in this host
     * @pre currentTime >= 0.0
     * @post $none
     */
    public double updateContainerVmsProcessing(double currentTime) {
        double smallerTime = Double.MAX_VALUE;

        for (ContainerPod containerPod : getVmList()) {
            double time = containerPod.updateVmProcessing(currentTime, getContainerVmScheduler().getAllocatedMipsForContainerVm(containerPod));
            if (time > 0.0 && time < smallerTime) {
                smallerTime = time;
            }
        }

        return smallerTime;
    }

    /**
     * Adds the migrating in vm.
     *
     * @param containerPod the vm
     */
    public void addMigratingInContainerVm(ContainerPod containerPod) {
        //Log.printLine("Host: addMigratingInContainerVm:......");
        containerPod.setInMigration(true);

        if (!getVmsMigratingIn().contains(containerPod)) {
            if (getStorage() < containerPod.getSize()) {
                Log.printConcatLine("[PodScheduler.addMigratingInContainerVm] Allocation of VM #", containerPod.getId(), " to Host #",
                        getId(), " failed by storage");
                System.exit(0);
            }

            if (!getContainerVmRamProvisioner().allocateRamForContainerVm(containerPod, containerPod.getCurrentRequestedRam())) {
                Log.printConcatLine("[PodScheduler.addMigratingInContainerVm] Allocation of VM #", containerPod.getId(), " to Host #",
                        getId(), " failed by RAM");
                System.exit(0);
            }

            if (!getContainerVmBwProvisioner().allocateBwForContainerVm(containerPod, containerPod.getCurrentRequestedBw())) {
                Log.printConcatLine("[PodScheduler.addMigratingInContainerVm] Allocation of VM #", containerPod.getId(), " to Host #",
                        getId(), " failed by BW");
                System.exit(0);
            }

            getContainerVmScheduler().getVmsMigratingIn().add(containerPod.getUid());
            if (!getContainerVmScheduler().allocatePesForVm(containerPod, containerPod.getCurrentRequestedMips())) {
                Log.printConcatLine("[PodScheduler.addMigratingInContainerVm] Allocation of VM #", containerPod.getId(), " to Host #",
                        getId(), " failed by MIPS");
                System.exit(0);
            }

            setStorage(getStorage() - containerPod.getSize());

            getVmsMigratingIn().add(containerPod);
            getVmList().add(containerPod);
            updateContainerVmsProcessing(CloudSim.clock());
            containerPod.getHost().updateContainerVmsProcessing(CloudSim.clock());
        }
    }

        /**
         * Removes the migrating in vm.
         *
         * @param vm the vm
         */
        public void removeMigratingInContainerVm(ContainerPod vm) {
            containerVmDeallocate(vm);
            getVmsMigratingIn().remove(vm);
            getVmList().remove(vm);
            getContainerVmScheduler().getVmsMigratingIn().remove(vm.getUid());
            vm.setInMigration(false);
        }

    /**
     * Reallocate migrating in vms.
     */
    public void reallocateMigratingInContainerVms() {
        for (ContainerPod containerPod : getVmsMigratingIn()) {
            if (!getVmList().contains(containerPod)) {
                getVmList().add(containerPod);
            }
            if (!getContainerVmScheduler().getVmsMigratingIn().contains(containerPod.getUid())) {
                getContainerVmScheduler().getVmsMigratingIn().add(containerPod.getUid());
            }
            getContainerVmRamProvisioner().allocateRamForContainerVm(containerPod, containerPod.getCurrentRequestedRam());
            getContainerVmBwProvisioner().allocateBwForContainerVm(containerPod, containerPod.getCurrentRequestedBw());
            getContainerVmScheduler().allocatePesForVm(containerPod, containerPod.getCurrentRequestedMips());
            setStorage(getStorage() - containerPod.getSize());
        }
    }

    /**
     * Checks if is suitable for vm.
     *
     * @param vm the vm
     * @return true, if is suitable for vm
     */
    public boolean isSuitableForContainerVm(ContainerPod vm) {
        //Log.printLine("Host: Is suitable for VM???......");
        return (getContainerVmScheduler().getPeCapacity() >= vm.getCurrentRequestedMaxMips()
                && getContainerVmScheduler().getAvailableMips() >= vm.getCurrentRequestedTotalMips()
                && getContainerVmRamProvisioner().isSuitableForContainerVm(vm, vm.getCurrentRequestedRam()) && getContainerVmBwProvisioner()
                .isSuitableForContainerVm(vm, vm.getCurrentRequestedBw()));
    }

    /**
     * Allocates PEs and memory to a new VM in the Host.
     *
     * @param vm Pod being started
     * @return $true if the VM could be started in the host; $false otherwise
     * @pre $none
     * @post $none
     */
    public boolean containerVmCreate(ContainerPod vm) {
        //Log.printLine("Host: Create VM???......" + vm.getId());
        if (getStorage() < vm.getSize()) {
            Log.printConcatLine("[PodScheduler.containerVmCreate] Allocation of VM #", vm.getId(), " to Host #", getId(),
                    " failed by storage");
            return false;
        }

        if (!getContainerVmRamProvisioner().allocateRamForContainerVm(vm, vm.getCurrentRequestedRam())) {
            Log.printConcatLine("[PodScheduler.containerVmCreate] Allocation of VM #", vm.getId(), " to Host #", getId(),
                    " failed by RAM");
            return false;
        }

        if (!getContainerVmBwProvisioner().allocateBwForContainerVm(vm, vm.getCurrentRequestedBw())) {
            Log.printConcatLine("[PodScheduler.containerVmCreate] Allocation of VM #", vm.getId(), " to Host #", getId(),
                    " failed by BW");
            getContainerVmRamProvisioner().deallocateRamForContainerVm(vm);
            return false;
        }

        if (!getContainerVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips())) {
            Log.printConcatLine("[PodScheduler.containerVmCreate] Allocation of VM #", vm.getId(), " to Host #", getId(),
                    " failed by MIPS");
            getContainerVmRamProvisioner().deallocateRamForContainerVm(vm);
            getContainerVmBwProvisioner().deallocateBwForContainerVm(vm);
            return false;
        }

        setStorage(getStorage() - vm.getSize());
        getVmList().add(vm);
        vm.setHost(this);
        return true;
    }

    /**
     * Destroys a VM running in the host.
     *
     * @param containerPod the VM
     * @pre $none
     * @post $none
     */
    public void containerVmDestroy(ContainerPod containerPod) {
        //Log.printLine("Host:  Destroy Pod:.... " + containerPod.getId());
        if (containerPod != null) {
            containerVmDeallocate(containerPod);
            getVmList().remove(containerPod);
            containerPod.setHost(null);
        }
    }

    /**
     * Destroys all VMs running in the host.
     *
     * @pre $none
     * @post $none
     */
    public void containerVmDestroyAll() {
        //Log.printLine("Host: Destroy all Vms");
        containerVmDeallocateAll();
        for (ContainerPod containerPod : getVmList()) {
            containerPod.setHost(null);
            setStorage(getStorage() + containerPod.getSize());
        }
        getVmList().clear();
    }

    /**
     * Deallocate all hostList for the VM.
     *
     * @param containerPod the VM
     */
    protected void containerVmDeallocate(ContainerPod containerPod) {
        //Log.printLine("Host: Deallocated the VM:......" + containerPod.getId());
        getContainerVmRamProvisioner().deallocateRamForContainerVm(containerPod);
        getContainerVmBwProvisioner().deallocateBwForContainerVm(containerPod);
        getContainerVmScheduler().deallocatePesForVm(containerPod);
        setStorage(getStorage() + containerPod.getSize());
    }

    /**
     * Deallocate all hostList for the VM.
     */
    protected void containerVmDeallocateAll() {
        //Log.printLine("Host: Deallocate all the Vms......");
        getContainerVmRamProvisioner().deallocateRamForAllContainerVms();
        getContainerVmBwProvisioner().deallocateBwForAllContainerVms();
        getContainerVmScheduler().deallocatePesForAllContainerVms();
    }

    /**
     * Returns a VM object.
     *
     * @param vmId   the vm id
     * @param userId ID of VM's owner
     * @return the virtual machine object, $null if not found
     * @pre $none
     * @post $none
     */
    public ContainerPod getContainerVm(int vmId, int userId) {
        //Log.printLine("Host: get the vm......" + vmId);
        //Log.printLine("Host: the vm list size:......" + getVmList().size());
        for (ContainerPod containerPod : getVmList()) {
            if (containerPod.getId() == vmId && containerPod.getUserId() == userId) {
                return containerPod;
            }
        }
        return null;
    }

    /**
     * Gets the pes number.
     *
     * @return the pes number
     */
    public int getNumberOfPes() {
        //Log.printLine("Host: get the peList Size......" + getPeList().size());
        return getPeList().size();
    }

    /**
     * Gets the free pes number.
     *
     * @return the free pes number
     */
    public int getNumberOfFreePes() {
        //Log.printLine("Host: get the free Pes......" + ContainerPodPeList.getNumberOfFreePes(getPeList()));
        return ContainerPodPeList.getNumberOfFreePes(getPeList());
    }

    /**
     * Gets the total mips.
     *
     * @return the total mips
     */
    public int getTotalMips() {
        //Log.printLine("Host: get the total mips......" + ContainerPodPeList.getTotalMips(getPeList()));
        return ContainerPodPeList.getTotalMips(getPeList());
    }

    /**
     * Allocates PEs for a VM.
     *
     * @param containerPod        the vm
     * @param mipsShare the mips share
     * @return $true if this policy allows a new VM in the host, $false otherwise
     * @pre $none
     * @post $none
     */
    public boolean allocatePesForContainerVm(ContainerPod containerPod, List<Double> mipsShare) {
        //Log.printLine("Host: allocate Pes for Pod:......" + containerPod.getId());
        return getContainerVmScheduler().allocatePesForVm(containerPod, mipsShare);
    }

    /**
     * Releases PEs allocated to a VM.
     *
     * @param containerPod the vm
     * @pre $none
     * @post $none
     */
    public void deallocatePesForContainerVm(ContainerPod containerPod) {
        //Log.printLine("Host: deallocate Pes for Pod:......" + containerPod.getId());
        getContainerVmScheduler().deallocatePesForVm(containerPod);
    }

    /**
     * Returns the MIPS share of each Pe that is allocated to a given VM.
     *
     * @param containerPod the vm
     * @return an array containing the amount of MIPS of each pe that is available to the VM
     * @pre $none
     * @post $none
     */
    public List<Double> getAllocatedMipsForContainerVm(ContainerPod containerPod) {
        //Log.printLine("Host: get allocated Pes for Pod:......" + containerPod.getId());
        return getContainerVmScheduler().getAllocatedMipsForContainerVm(containerPod);
    }

    /**
     * Gets the total allocated MIPS for a VM over all the PEs.
     *
     * @param containerPod the vm
     * @return the allocated mips for vm
     */
    public double getTotalAllocatedMipsForContainerVm(ContainerPod containerPod) {
        //Log.printLine("Host: total allocated Pes for Pod:......" + containerPod.getId());
        return getContainerVmScheduler().getTotalAllocatedMipsForContainerVm(containerPod);
    }

    /**
     * Returns maximum available MIPS among all the PEs.
     *
     * @return max mips
     */
    public double getMaxAvailableMips() {
        //Log.printLine("Host: Maximum Available Pes:......");
        return getContainerVmScheduler().getMaxAvailableMips();
    }

    /**
     * Gets the free mips.
     *
     * @return the free mips
     */
    public double getAvailableMips() {
        //Log.printLine("Host: Get available Mips");
        return getContainerVmScheduler().getAvailableMips();
    }

    /**
     * Gets the machine bw.
     *
     * @return the machine bw
     * @pre $none
     * @post $result > 0
     */
    public long getBw() {
        //Log.printLine("Host: Get BW:......" + getContainerVmBwProvisioner().getBw());
        return getContainerVmBwProvisioner().getBw();
    }

    /**
     * Gets the machine memory.
     *
     * @return the machine memory
     * @pre $none
     * @post $result > 0
     */
    public float getRam() {
        //Log.printLine("Host: Get Ram:......" + getContainerVmRamProvisioner().getRam());

        return getContainerVmRamProvisioner().getRam();
    }

    /**
     * Gets the machine storage.
     *
     * @return the machine storage
     * @pre $none
     * @post $result >= 0
     */
    public long getStorage() {
        return storage;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    protected void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the ram provisioner.
     *
     * @return the ram provisioner
     */
    public ContainerPodRamProvisioner getContainerVmRamProvisioner() {
        return containerPodRamProvisioner;
    }

    /**
     * Sets the ram provisioner.
     *
     * @param containerPodRamProvisioner the new ram provisioner
     */
    protected void setContainerVmRamProvisioner(ContainerPodRamProvisioner containerPodRamProvisioner) {
        this.containerPodRamProvisioner = containerPodRamProvisioner;
    }

    /**
     * Gets the bw provisioner.
     *
     * @return the bw provisioner
     */
    public ContainerPodBwProvisioner getContainerVmBwProvisioner() {
        return containerPodBwProvisioner;
    }

    /**
     * Sets the bw provisioner.
     *
     * @param containerPodBwProvisioner the new bw provisioner
     */
    protected void setContainerVmBwProvisioner(ContainerPodBwProvisioner containerPodBwProvisioner) {
        this.containerPodBwProvisioner = containerPodBwProvisioner;
    }

    /**
     * Gets the VM scheduler.
     *
     * @return the VM scheduler
     */
    public ContainerPodScheduler getContainerVmScheduler() {
        return containerPodScheduler;
    }

    /**
     * Sets the VM scheduler.
     *
     * @param vmScheduler the vm scheduler
     */
    protected void setContainerVmScheduler(ContainerPodScheduler vmScheduler) {
        this.containerPodScheduler = vmScheduler;
    }

    /**
     * Gets the pe list.
     *
     * @param <T> the generic type
     * @return the pe list
     */
    @SuppressWarnings("unchecked")
    public <T extends ContainerPodPe> List<T> getPeList() {
        return (List<T>) peList;
    }

    /**
     * Sets the pe list.
     *
     * @param <T>    the generic type
     * @param containerVmPeList the new pe list
     */
    protected <T extends ContainerPodPe> void setPeList(List<T> containerVmPeList) {
        this.peList = containerVmPeList;
    }

    /**
     * Gets the vm list.
     *
     * @param <T> the generic type
     * @return the vm list
     */
    @SuppressWarnings("unchecked")
    public <T extends ContainerPod> List<T> getVmList() {
        return (List<T>) vmList;
    }

    /**
     * Sets the storage.
     *
     * @param storage the new storage
     */
    protected void setStorage(long storage) {
        this.storage = storage;
    }

    /**
     * Checks if is failed.
     *
     * @return true, if is failed
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * Sets the PEs of this machine to a FAILED status. NOTE: <tt>resName</tt> is used for debugging
     * purposes, which is <b>ON</b> by default. Use {@link #setFailed(boolean)} if you do not want
     * this information.
     *
     * @param resName the name of the resource
     * @param failed  the failed
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    public boolean setFailed(String resName, boolean failed) {
        // all the PEs are failed (or recovered, depending on fail)
        this.failed = failed;
        ContainerPodPeList.setStatusFailed(getPeList(), resName, getId(), failed);
        return true;
    }

    /**
     * Sets the PEs of this machine to a FAILED status.
     *
     * @param failed the failed
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     */
    public boolean setFailed(boolean failed) {
        // all the PEs are failed (or recovered, depending on fail)
        this.failed = failed;
        ContainerPodPeList.setStatusFailed(getPeList(), failed);
        return true;
    }

    /**
     * Sets the particular Pe status on this Machine.
     *
     * @param peId   the pe id
     * @param status Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
     * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt> otherwise (Pe id might not
     * be exist)
     * @pre peID >= 0
     * @post $none
     */
    public boolean setPeStatus(int peId, int status) {
        return ContainerPodPeList.setPeStatus(getPeList(), peId, status);
    }

    /**
     * Gets the vms migrating in.
     *
     * @return the vms migrating in
     */
    public List<ContainerPod> getVmsMigratingIn() {
        return vmsMigratingIn;
    }

    /**
     * Gets the data center.
     *
     * @return the data center where the host runs
     */
    public WFCDatacenter getDatacenter() {
        return datacenter;
    }

    /**
     * Sets the data center.
     *
     * @param datacenter the data center from this host
     */
    public void setDatacenter(WFCDatacenter datacenter) {
        this.datacenter = datacenter;
    }


}


