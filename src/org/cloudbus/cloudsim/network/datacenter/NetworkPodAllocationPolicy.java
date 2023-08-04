/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pod;
import org.cloudbus.cloudsim.PodAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * NetworkPodAllocationPolicy is an {@link PodAllocationPolicy} that chooses,
 * as the host for a VM, the host with less PEs in use.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 1.0
 */
public class NetworkPodAllocationPolicy extends PodAllocationPolicy {

	/** The vm map where each key is a VM id and
         * each value is the host where the VM is placed. */
	private Map<String, Host> vmTable;

	/** The used PEs map, where each key is a VM id
         * and each value is the number of required PEs the VM is using. */
	private Map<String, Integer> usedPes;

	/** The free pes. */
	private List<Integer> freePes;

	/**
	 * Creates a new PodAllocationPolicySimple object.
	 * 
	 * @param list list Machines available in a {@link Datacenter}
	 * 
	 * @pre $none
	 * @post $none
	 */
	public NetworkPodAllocationPolicy(List<? extends Host> list) {
		super(list);

		setFreePes(new ArrayList<Integer>());
		for (Host host : getHostList()) {
			getFreePes().add(host.getNumberOfPes());

		}

		setVmTable(new HashMap<String, Host>());
		setUsedPes(new HashMap<String, Integer>());
	}

	/**
	 * Allocates the host with less PEs in use for a given VM.
	 * 
	 * @param pod {@inheritDoc}
	 * 
	 * @return {@inheritDoc}
	 * 
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
				int moreFree = Integer.MIN_VALUE;
				int idx = -1;

				// we want the host with less pes in use
				for (int i = 0; i < freePesTmp.size(); i++) {
					if (freePesTmp.get(i) > moreFree) {
						moreFree = freePesTmp.get(i);
						idx = i;
					}
				}

				NetworkHost host = this.<NetworkHost> getHostList().get(idx);
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

        /**
         * Gets the max utilization among the PEs of a given VM placed at a given host.
         * @param host The host where the VM is placed
         * @param pod The VM to get the max PEs utilization
         * @return The max utilization among the PEs of the VM
         */
	protected double getMaxUtilizationAfterAllocation(NetworkHost host, Pod pod) {
		List<Double> allocatedMipsForVm = null;
		NetworkHost allocatedHost = (NetworkHost) pod.getHost();

		if (allocatedHost != null) {
			allocatedMipsForVm = pod.getHost().getAllocatedMipsForVm(pod);
		}

		if (!host.allocatePesForVm(pod, pod.getCurrentRequestedMips())) {
			return -1;
		}

		double maxUtilization = host.getMaxUtilizationAmongVmsPes(pod);

		host.deallocatePesForVm(pod);

		if (allocatedHost != null && allocatedMipsForVm != null) {
			pod.getHost().allocatePesForVm(pod, allocatedMipsForVm);
		}

		return maxUtilization;
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
		/*@todo Auto-generated method stub.
                The method is doing nothing.*/
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
