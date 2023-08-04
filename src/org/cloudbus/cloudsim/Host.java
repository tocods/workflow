/*
 * Title: CloudSim Toolkit Description: CloudSim (Cloud Simulation) Toolkit for Modeling and
 * Simulation of Clouds Licence: GPL - http://www.gnu.org/copyleft/gpl.html
 * 
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * A Host is a Physical Machine (PM) inside a Datacenter. It is also called as a Server.
 * It executes actions related to management of virtual machines (e.g., creation and destruction).
 * A host has a defined policy for provisioning memory and bw, as well as an allocation policy for
 * Pe's to virtual machines. A host is associated to a datacenter. It can host virtual machines.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class Host {

	/** The id of the host. */
	private int id;

	/** The storage capacity. */
	private long storage;

	/** The ram provisioner. */
	private RamProvisioner ramProvisioner;

	/** The bw provisioner. */
	private BwProvisioner bwProvisioner;

	/** The allocation policy for scheduling VM execution. */
	private PodScheduler podScheduler;

	/** The list of VMs assigned to the host. */
	private final List<? extends Pod> vmList = new ArrayList<Pod>();

	/** The Processing Elements (PEs) of the host, that
         * represent the CPU cores of it, and thus, its processing capacity. */
	private List<? extends Pe> peList;

	/** Tells whether this host is working properly or has failed. */
	private boolean failed;

	/** The VMs migrating in. */
	private final List<Pod> vmsMigratingIn = new ArrayList<Pod>();

	/** The datacenter where the host is placed. */
	private Datacenter datacenter;

	/**
	 * Instantiates a new host.
	 * 
	 * @param id the host id
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage capacity
	 * @param peList the host's PEs list
	 * @param podScheduler the vm scheduler
	 */
	public Host(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			PodScheduler podScheduler) {
		setId(id);
		setRamProvisioner(ramProvisioner);
		setBwProvisioner(bwProvisioner);
		setStorage(storage);
		setVmScheduler(podScheduler);

		setPeList(peList);
		setFailed(false);
	}

	/**
	 * Requests updating of cloudlets' processing in VMs running in this host.
	 * 
	 * @param currentTime the current time
	 * @return expected time of completion of the next cloudlet in all VMs in this host or
	 *         {@link Double#MAX_VALUE} if there is no future events expected in this host
	 * @pre currentTime >= 0.0
	 * @post $none
         * @todo there is an inconsistency between the return value of this method
         * and the individual call of {@link Pod#updateVmProcessing(double, java.util.List),
         * and consequently the {@link CloudletScheduler#updateVmProcessing(double, java.util.List)}.
         * The current method returns {@link Double#MAX_VALUE}  while the other ones
         * return 0. It has to be checked if there is a reason for this
         * difference.}
	 */
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;

		for (Pod pod : getVmList()) {
			double time = pod.updateVmProcessing(
                                currentTime, getVmScheduler().getAllocatedMipsForVm(pod));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
		}

		return smallerTime;
	}

	/**
	 * Adds a VM migrating into the current host.
	 * 
	 * @param pod the pod
	 */
	public void addMigratingInVm(Pod pod) {
		pod.setInMigration(true);

		if (!getVmsMigratingIn().contains(pod)) {
			if (getStorage() < pod.getSize()) {
				Log.printConcatLine("[PodScheduler.addMigratingInVm] Allocation of VM #", pod.getId(), " to Host #",
						getId(), " failed by storage");
				System.exit(0);
			}

			if (!getRamProvisioner().allocateRamForVm(pod, pod.getCurrentRequestedRam())) {
				Log.printConcatLine("[PodScheduler.addMigratingInVm] Allocation of VM #", pod.getId(), " to Host #",
						getId(), " failed by RAM");
				System.exit(0);
			}

			if (!getBwProvisioner().allocateBwForVm(pod, pod.getCurrentRequestedBw())) {
				Log.printLine("[PodScheduler.addMigratingInVm] Allocation of VM #" + pod.getId() + " to Host #"
						+ getId() + " failed by BW");
				System.exit(0);
			}

			getVmScheduler().getVmsMigratingIn().add(pod.getUid());
			if (!getVmScheduler().allocatePesForVm(pod, pod.getCurrentRequestedMips())) {
				Log.printLine("[PodScheduler.addMigratingInVm] Allocation of VM #" + pod.getId() + " to Host #"
						+ getId() + " failed by MIPS");
				System.exit(0);
			}

			setStorage(getStorage() - pod.getSize());

			getVmsMigratingIn().add(pod);
			getVmList().add(pod);
			updateVmsProcessing(CloudSim.clock());
			pod.getHost().updateVmsProcessing(CloudSim.clock());
		}
	}

	/**
	 * Removes a migrating in pod.
	 * 
	 * @param pod the pod
	 */
	public void removeMigratingInVm(Pod pod) {
		vmDeallocate(pod);
		getVmsMigratingIn().remove(pod);
		getVmList().remove(pod);
		getVmScheduler().getVmsMigratingIn().remove(pod.getUid());
		pod.setInMigration(false);
	}

	/**
	 * Reallocate migrating in vms. Gets the VM in the migrating in queue
         * and allocate them on the host.
	 */
	public void reallocateMigratingInVms() {
		for (Pod pod : getVmsMigratingIn()) {
			if (!getVmList().contains(pod)) {
				getVmList().add(pod);
			}
			if (!getVmScheduler().getVmsMigratingIn().contains(pod.getUid())) {
				getVmScheduler().getVmsMigratingIn().add(pod.getUid());
			}
			getRamProvisioner().allocateRamForVm(pod, pod.getCurrentRequestedRam());
			getBwProvisioner().allocateBwForVm(pod, pod.getCurrentRequestedBw());
			getVmScheduler().allocatePesForVm(pod, pod.getCurrentRequestedMips());
			setStorage(getStorage() - pod.getSize());
		}
	}

	/**
	 * Checks if the host is suitable for pod. If it has enough resources
         * to attend the VM.
	 * 
	 * @param pod the pod
	 * @return true, if is suitable for pod
	 */
	public boolean isSuitableForVm(Pod pod) {
		return (getVmScheduler().getPeCapacity() >= pod.getCurrentRequestedMaxMips()
				&& getVmScheduler().getAvailableMips() >= pod.getCurrentRequestedTotalMips()
				&& getRamProvisioner().isSuitableForVm(pod, pod.getCurrentRequestedRam()) && getBwProvisioner()
				.isSuitableForVm(pod, pod.getCurrentRequestedBw()));
	}

	/**
	 * Try to allocate resources to a new VM in the Host.
	 * 
	 * @param pod Pod being started
	 * @return $true if the VM could be started in the host; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean vmCreate(Pod pod) {
		if (getStorage() < pod.getSize()) {
			Log.printConcatLine("[PodScheduler.vmCreate] Allocation of VM #", pod.getId(), " to Host #", getId(),
					" failed by storage");
			return false;
		}

		if (!getRamProvisioner().allocateRamForVm(pod, pod.getCurrentRequestedRam())) {
			Log.printConcatLine("[PodScheduler.vmCreate] Allocation of VM #", pod.getId(), " to Host #", getId(),
					" failed by RAM");
			return false;
		}

		if (!getBwProvisioner().allocateBwForVm(pod, pod.getCurrentRequestedBw())) {
			Log.printConcatLine("[PodScheduler.vmCreate] Allocation of VM #", pod.getId(), " to Host #", getId(),
					" failed by BW");
			getRamProvisioner().deallocateRamForVm(pod);
			return false;
		}

		if (!getVmScheduler().allocatePesForVm(pod, pod.getCurrentRequestedMips())) {
			Log.printConcatLine("[PodScheduler.vmCreate] Allocation of VM #", pod.getId(), " to Host #", getId(),
					" failed by MIPS");
			getRamProvisioner().deallocateRamForVm(pod);
			getBwProvisioner().deallocateBwForVm(pod);
			return false;
		}

		setStorage(getStorage() - pod.getSize());
		getVmList().add(pod);
		pod.setHost(this);
		return true;
	}

	/**
	 * Destroys a VM running in the host.
	 * 
	 * @param pod the VM
	 * @pre $none
	 * @post $none
	 */
	public void vmDestroy(Pod pod) {
		if (pod != null) {
			vmDeallocate(pod);
			getVmList().remove(pod);
			pod.setHost(null);
		}
	}

	/**
	 * Destroys all VMs running in the host.
	 * 
	 * @pre $none
	 * @post $none
	 */
	public void vmDestroyAll() {
		vmDeallocateAll();
		for (Pod pod : getVmList()) {
			pod.setHost(null);
			setStorage(getStorage() + pod.getSize());
		}
		getVmList().clear();
	}

	/**
	 * Deallocate all resources of a VM.
	 * 
	 * @param pod the VM
	 */
	protected void vmDeallocate(Pod pod) {
		getRamProvisioner().deallocateRamForVm(pod);
		getBwProvisioner().deallocateBwForVm(pod);
		getVmScheduler().deallocatePesForVm(pod);
		setStorage(getStorage() + pod.getSize());
	}

	/**
	 * Deallocate all resources of all VMs.
	 */
	protected void vmDeallocateAll() {
		getRamProvisioner().deallocateRamForAllVms();
		getBwProvisioner().deallocateBwForAllVms();
		getVmScheduler().deallocatePesForAllVms();
	}

	/**
	 * Gets a VM by its id and user.
	 * 
	 * @param vmId the vm id
	 * @param userId ID of VM's owner
	 * @return the virtual machine object, $null if not found
	 * @pre $none
	 * @post $none
	 */
	public Pod getVm(int vmId, int userId) {
		for (Pod pod : getVmList()) {
			if (pod.getId() == vmId && pod.getUserId() == userId) {
				return pod;
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
		return getPeList().size();
	}

	/**
	 * Gets the free pes number.
	 * 
	 * @return the free pes number
	 */
	public int getNumberOfFreePes() {
		return PeList.getNumberOfFreePes(getPeList());
	}

	/**
	 * Gets the total mips.
	 * 
	 * @return the total mips
	 */
	public int getTotalMips() {
		return PeList.getTotalMips(getPeList());
	}

	/**
	 * Allocates PEs for a VM.
	 * 
	 * @param pod the pod
	 * @param mipsShare the list of MIPS share to be allocated to the VM
	 * @return $true if this policy allows a new VM in the host, $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public boolean allocatePesForVm(Pod pod, List<Double> mipsShare) {
		return getVmScheduler().allocatePesForVm(pod, mipsShare);
	}

	/**
	 * Releases PEs allocated to a VM.
	 * 
	 * @param pod the pod
	 * @pre $none
	 * @post $none
	 */
	public void deallocatePesForVm(Pod pod) {
		getVmScheduler().deallocatePesForVm(pod);
	}

	/**
	 * Gets the MIPS share of each Pe that is allocated to a given VM.
	 * 
	 * @param pod the pod
	 * @return an array containing the amount of MIPS of each pe that is available to the VM
	 * @pre $none
	 * @post $none
	 */
	public List<Double> getAllocatedMipsForVm(Pod pod) {
		return getVmScheduler().getAllocatedMipsForVm(pod);
	}

	/**
	 * Gets the total allocated MIPS for a VM along all its PEs.
	 * 
	 * @param pod the pod
	 * @return the allocated mips for pod
	 */
	public double getTotalAllocatedMipsForVm(Pod pod) {
		return getVmScheduler().getTotalAllocatedMipsForVm(pod);
	}

	/**
	 * Returns the maximum available MIPS among all the PEs of the host.
	 * 
	 * @return max mips
	 */
	public double getMaxAvailableMips() {
		return getVmScheduler().getMaxAvailableMips();
	}

	/**
	 * Gets the total free MIPS available at the host.
	 * 
	 * @return the free mips
	 */
	public double getAvailableMips() {
		return getVmScheduler().getAvailableMips();
	}

	/**
	 * Gets the host bw.
	 * 
	 * @return the host bw
	 * @pre $none
	 * @post $result > 0
	 */
	public long getBw() {
		return getBwProvisioner().getBw();
	}

	/**
	 * Gets the host memory.
	 * 
	 * @return the host memory
	 * @pre $none
	 * @post $result > 0
	 */
	public int getRam() {
		return getRamProvisioner().getRam();
	}

	/**
	 * Gets the host storage.
	 * 
	 * @return the host storage
	 * @pre $none
	 * @post $result >= 0
	 */
	public long getStorage() {
		return storage;
	}

	/**
	 * Gets the host id.
	 * 
	 * @return the host id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the host id.
	 * 
	 * @param id the new host id
	 */
	protected void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the ram provisioner.
	 * 
	 * @return the ram provisioner
	 */
	public RamProvisioner getRamProvisioner() {
		return ramProvisioner;
	}

	/**
	 * Sets the ram provisioner.
	 * 
	 * @param ramProvisioner the new ram provisioner
	 */
	protected void setRamProvisioner(RamProvisioner ramProvisioner) {
		this.ramProvisioner = ramProvisioner;
	}

	/**
	 * Gets the bw provisioner.
	 * 
	 * @return the bw provisioner
	 */
	public BwProvisioner getBwProvisioner() {
		return bwProvisioner;
	}

	/**
	 * Sets the bw provisioner.
	 * 
	 * @param bwProvisioner the new bw provisioner
	 */
	protected void setBwProvisioner(BwProvisioner bwProvisioner) {
		this.bwProvisioner = bwProvisioner;
	}

	/**
	 * Gets the VM scheduler.
	 * 
	 * @return the VM scheduler
	 */
	public PodScheduler getVmScheduler() {
		return podScheduler;
	}

	/**
	 * Sets the VM scheduler.
	 * 
	 * @param podScheduler the vm scheduler
	 */
	protected void setVmScheduler(PodScheduler podScheduler) {
		this.podScheduler = podScheduler;
	}

	/**
	 * Gets the pe list.
	 * 
	 * @param <T> the generic type
	 * @return the pe list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Pe> List<T> getPeList() {
		return (List<T>) peList;
	}

	/**
	 * Sets the pe list.
	 * 
	 * @param <T> the generic type
	 * @param peList the new pe list
	 */
	protected <T extends Pe> void setPeList(List<T> peList) {
		this.peList = peList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Pod> List<T> getVmList() {
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
	 * Checks if the host PEs have failed.
	 * 
	 * @return true, if the host PEs have failed; false otherwise
	 */
	public boolean isFailed() {
		return failed;
	}

	/**
	 * Sets the PEs of the host to a FAILED status. NOTE: <tt>resName</tt> is used for debugging
	 * purposes, which is <b>ON</b> by default. Use {@link #setFailed(boolean)} if you do not want
	 * this information.
	 * 
	 * @param resName the name of the resource
	 * @param failed the failed
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setFailed(String resName, boolean failed) {
		// all the PEs are failed (or recovered, depending on fail)
		this.failed = failed;
		PeList.setStatusFailed(getPeList(), resName, getId(), failed);
		return true;
	}

	/**
	 * Sets the PEs of the host to a FAILED status.
	 * 
	 * @param failed the failed
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	public boolean setFailed(boolean failed) {
		// all the PEs are failed (or recovered, depending on fail)
		this.failed = failed;
		PeList.setStatusFailed(getPeList(), failed);
		return true;
	}

	/**
	 * Sets the particular Pe status on the host.
	 * 
	 * @param peId the pe id
	 * @param status Pe status, either <tt>Pe.FREE</tt> or <tt>Pe.BUSY</tt>
	 * @return <tt>true</tt> if the Pe status has changed, <tt>false</tt> otherwise (Pe id might not
	 *         be exist)
	 * @pre peID >= 0
	 * @post $none
	 */
	public boolean setPeStatus(int peId, int status) {
		return PeList.setPeStatus(getPeList(), peId, status);
	}

	/**
	 * Gets the vms migrating in.
	 * 
	 * @return the vms migrating in
	 */
	public List<Pod> getVmsMigratingIn() {
		return vmsMigratingIn;
	}

	/**
	 * Gets the data center of the host.
	 * 
	 * @return the data center where the host runs
	 */
	public Datacenter getDatacenter() {
		return datacenter;
	}

	/**
	 * Sets the data center of the host.
	 * 
	 * @param datacenter the data center from this host
	 */
	public void setDatacenter(Datacenter datacenter) {
		this.datacenter = datacenter;
	}

}
