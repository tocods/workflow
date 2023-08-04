/**
 * 
 */
package org.cloudbus.cloudsim.container.resourceAllocators;


import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerPod;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sareh
 *
 */
public class ContainerAllocationPolicySimple extends ContainerAllocationPolicy {
	/** The vm table. */
	private Map<String, ContainerPod> containerVmTable;

	/** The used pes. */
	private Map<String, Integer> usedPes;

	/** The free pes. */
	private List<Integer> freePes;
	/**
	 * Creates the new PodAllocationPolicySimple object.
	 *
	 * @pre $none
	 * @post $none
	 */
	public ContainerAllocationPolicySimple() {
		super();
		setFreePes(new ArrayList<Integer>());
		setContainerVmTable(new HashMap<String, ContainerPod>());
		setUsedPes(new HashMap<String, Integer>());
	}


	@Override
	public boolean allocateVmForContainer(Container container, List<ContainerPod> containerPodList) {
//		the available container list is updated. It gets is from the data center.
		setContainerVmList(containerPodList);
		for (ContainerPod containerPod : getContainerVmList()) {
			getFreePes().add(containerPod.getNumberOfPes());

		}
		int requiredPes = container.getNumberOfPes();
		boolean result = false;
		int tries = 0;
		List<Integer> freePesTmp = new ArrayList<>();
		for (Integer freePes : getFreePes()) {
			freePesTmp.add(freePes);
		}

		if (!getContainerVmTable().containsKey(container.getUid())) { // if this vm was not created
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

				ContainerPod containerPod = getContainerVmList().get(idx);
				result = containerPod.containerCreate(container);

				if (result) { // if vm were succesfully created in the host
					getContainerVmTable().put(container.getUid(), containerPod);
					getUsedPes().put(container.getUid(), requiredPes);
					getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
					result = true;
					break;
				} else {
					freePesTmp.set(idx, Integer.MIN_VALUE);
				}
				tries++;
			} while (!result && tries < getFreePes().size());

		}

		freePesTmp.clear();

		return result;
	}

	@Override
	public boolean allocateVmForContainer(Container container, ContainerPod containerPod) {
		if (containerPod.containerCreate(container)) { // if vm has been succesfully created in the host
			getContainerVmTable().put(container.getUid(), containerPod);

			int requiredPes = container.getNumberOfPes();
			int idx = getContainerVmList().indexOf(container);
			getUsedPes().put(container.getUid(), requiredPes);
			getFreePes().set(idx, getFreePes().get(idx) - requiredPes);

			Log.formatLine(
					"%.2f: Container #" + container.getId() + " has been allocated to the Pod #" + containerPod.getId(),
					CloudSim.clock());
			return true;
		}

		return false;
	}


	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Container> containerList) {
		return null;
	}

	@Override
	public void deallocateVmForContainer(Container container) {

		ContainerPod containerPod = getContainerVmTable().remove(container.getUid());
		int idx = getContainerVmList().indexOf(containerPod);
		int pes = getUsedPes().remove(container.getUid());
		if (containerPod != null) {
			containerPod.containerDestroy(container);
			getFreePes().set(idx, getFreePes().get(idx) + pes);
		}

	}

	@Override
	public ContainerPod getContainerVm(Container container) {
		return getContainerVmTable().get(container.getUid());
	}

	@Override
	public ContainerPod getContainerVm(int containerId, int userId) {
		return getContainerVmTable().get(Container.getUid(userId, containerId));
	}

	protected Map<String, ContainerPod> getContainerVmTable() {
		return containerVmTable;
	}

	protected void setContainerVmTable(Map<String, ContainerPod> containerVmTable) {
		this.containerVmTable = containerVmTable;
	}

	protected Map<String, Integer> getUsedPes() {
		return usedPes;
	}

	protected void setUsedPes(Map<String, Integer> usedPes) {
		this.usedPes = usedPes;
	}

	protected List<Integer> getFreePes() {
		return freePes;
	}

	protected void setFreePes(List<Integer> freePes) {
		this.freePes = freePes;
	}
}
