/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import java.util.List;
import java.util.Map;

/**
 * PodAllocationPolicy is an abstract class that represents the provisioning policy of hosts to
 * virtual machines in a Datacenter. It allocates hosts for placing VMs. 
 * It supports two-stage commit of reservation of hosts: first, we
 * reserve the host and, once committed by the user, it is effectively allocated to he/she.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public abstract class PodAllocationPolicy {

	/** The host list. */
	private List<? extends Host> hostList;

	/**
	 * Creates a new PodAllocationPolicy object.
	 * 
	 * @param list Machines available in a {@link Datacenter}
	 * @pre $none
	 * @post $none
	 */
	public PodAllocationPolicy(List<? extends Host> list) {
		setHostList(list);
	}

	/**
	 * Allocates a host for a given VM.
	 * 
	 * @param pod the VM to allocate a host to
	 * @return $true if the host could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateHostForVm(Pod pod);

	/**
	 * Allocates a specified host for a given VM.
	 * 
	 * @param pod virtual machine which the host is reserved to
         * @param host host to allocate the the given VM
	 * @return $true if the host could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateHostForVm(Pod pod, Host host);

	/**
	 * Optimize allocation of the VMs according to current utilization.
	 * 
	 * @param vmList the vm list
	 * @return the array list< hash map< string, object>>
         * 
         * @todo It returns a list of maps, where each map key is a string 
         * and stores an object. What in fact are the keys and values of this
         * Map? Neither this class or its subclasses implement the method
         * or have clear documentation. The only sublcass is the {@link PodAllocationPolicySimple}.
         * 
	 */
	public abstract List<Map<String, Object>> optimizeAllocation(List<? extends Pod> vmList);

	/**
	 * Releases the host used by a VM.
	 * 
	 * @param pod the pod to get its host released
	 * @pre $none
	 * @post $none
	 */
	public abstract void deallocateHostForVm(Pod pod);

	/**
	 * Get the host that is executing the given VM.
	 * 
	 * @param pod the pod
	 * @return the Host with the given vmID; $null if not found
         * 
	 * @pre $none
	 * @post $none
	 */
	public abstract Host getHost(Pod pod);

	/**
	 * Get the host that is executing the given VM belonging to the given user.
	 * 
	 * @param vmId the vm id
	 * @param userId the user id
	 * @return the Host with the given vmID and userID; $null if not found
	 * @pre $none
	 * @post $none
	 */
	public abstract Host getHost(int vmId, int userId);

	/**
	 * Sets the host list.
	 * 
	 * @param hostList the new host list
	 */
	protected void setHostList(List<? extends Host> hostList) {
		this.hostList = hostList;
	}

	/**
	 * Gets the host list.
	 * 
	 * @return the host list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Host> List<T> getHostList() {
		return (List<T>) hostList;
	}

}
