/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import java.util.HashMap;
import java.util.Map;

import org.cloudbus.cloudsim.Pod;

/**
 * RamProvisionerSimple is an extension of {@link RamProvisioner} which uses a best-effort policy to
 * allocate memory to VMs: if there is available ram on the host, it allocates; otherwise, it fails. 
 * Each host has to have its own instance of a RamProvisioner.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class RamProvisionerSimple extends RamProvisioner {

	/** The RAM map, where each key is a VM id and each value
         * is the amount of RAM allocated to that VM. */
	private Map<String, Integer> ramTable;

	/**
	 * Instantiates a new ram provisioner simple.
	 * 
	 * @param availableRam The total ram capacity from the host that the provisioner can allocate to VMs. 
	 */
	public RamProvisionerSimple(int availableRam) {
		super(availableRam);
		setRamTable(new HashMap<String, Integer>());
	}

	@Override
	public boolean allocateRamForVm(Pod pod, int ram) {
		int maxRam = pod.getRam();
                /* If the requested amount of RAM to be allocated to the VM is greater than
                the amount of VM is in fact requiring, allocate only the
                amount defined in the Pod requirements.*/
		if (ram >= maxRam) {
			ram = maxRam;
		}

		deallocateRamForVm(pod);

		if (getAvailableRam() >= ram) {
			setAvailableRam(getAvailableRam() - ram);
			getRamTable().put(pod.getUid(), ram);
			pod.setCurrentAllocatedRam(getAllocatedRamForVm(pod));
			return true;
		}

		pod.setCurrentAllocatedRam(getAllocatedRamForVm(pod));

		return false;
	}

	@Override
	public int getAllocatedRamForVm(Pod pod) {
		if (getRamTable().containsKey(pod.getUid())) {
			return getRamTable().get(pod.getUid());
		}
		return 0;
	}

	@Override
	public void deallocateRamForVm(Pod pod) {
		if (getRamTable().containsKey(pod.getUid())) {
			int amountFreed = getRamTable().remove(pod.getUid());
			setAvailableRam(getAvailableRam() + amountFreed);
			pod.setCurrentAllocatedRam(0);
		}
	}

	@Override
	public void deallocateRamForAllVms() {
		super.deallocateRamForAllVms();
		getRamTable().clear();
	}

	@Override
	public boolean isSuitableForVm(Pod pod, int ram) {
		int allocatedRam = getAllocatedRamForVm(pod);
		boolean result = allocateRamForVm(pod, ram);
		deallocateRamForVm(pod);
		if (allocatedRam > 0) {
			allocateRamForVm(pod, allocatedRam);
		}
		return result;
	}

	/**
	 * Gets the map between VMs and allocated ram.
	 * 
	 * @return the ram map
	 */
	protected Map<String, Integer> getRamTable() {
		return ramTable;
	}

	/**
	 * Sets the map between VMs and allocated ram.
	 * 
	 * @param ramTable the ram map
	 */
	protected void setRamTable(Map<String, Integer> ramTable) {
		this.ramTable = ramTable;
	}

}
