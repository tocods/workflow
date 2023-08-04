/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pod;
import org.cloudbus.cloudsim.PodAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * An abstract power-aware VM allocation policy.
 * 
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 * 
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public abstract class PowerPodAllocationPolicyAbstract extends PodAllocationPolicy {

	/** The map map where each key is a VM id and
         * each value is the host where the VM is placed. */
	private final Map<String, Host> vmTable = new HashMap<String, Host>();

	/**
	 * Instantiates a new PowerPodAllocationPolicyAbstract.
	 * 
	 * @param list the list
	 */
	public PowerPodAllocationPolicyAbstract(List<? extends Host> list) {
		super(list);
	}

	@Override
	public boolean allocateHostForVm(Pod pod) {
		return allocateHostForVm(pod, findHostForVm(pod));
	}

	@Override
	public boolean allocateHostForVm(Pod pod, Host host) {
		if (host == null) {
			Log.formatLine("%.2f: No suitable host found for VM #" + pod.getId() + "\n", CloudSim.clock());
			return false;
		}
		if (host.vmCreate(pod)) { // if pod has been succesfully created in the host
			getVmTable().put(pod.getUid(), host);
			Log.formatLine(
					"%.2f: VM #" + pod.getId() + " has been allocated to the host #" + host.getId(),
					CloudSim.clock());
			return true;
		}
		Log.formatLine(
				"%.2f: Creation of VM #" + pod.getId() + " on the host #" + host.getId() + " failed\n",
				CloudSim.clock());
		return false;
	}

	/**
	 * Finds the first host that has enough resources to host a given VM.
	 * 
	 * @param pod the pod to find a host for it
	 * @return the first host found that can host the VM
	 */
	public PowerHost findHostForVm(Pod pod) {
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (host.isSuitableForVm(pod)) {
				return host;
			}
		}
		return null;
	}

	@Override
	public void deallocateHostForVm(Pod pod) {
		Host host = getVmTable().remove(pod.getUid());
		if (host != null) {
			host.vmDestroy(pod);
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

}
