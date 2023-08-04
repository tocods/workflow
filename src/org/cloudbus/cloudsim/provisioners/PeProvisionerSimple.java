/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.provisioners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Pod;

/**
 * PeProvisionerSimple is an extension of {@link PeProvisioner} which uses a best-effort policy to
 * allocate virtual PEs to VMs: 
 * if there is available mips on the physical PE, it allocates to a virtual PE; otherwise, it fails. 
 * Each host's PE has to have its own instance of a PeProvisioner.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PeProvisionerSimple extends PeProvisioner {

	/** The PE map, where each key is a VM id and each value
         * is the list of PEs (in terms of their amount of MIPS) 
         * allocated to that VM. */
	private Map<String, List<Double>> peTable;

	/**
	 * Instantiates a new pe provisioner simple.
	 * 
	 * @param availableMips The total mips capacity of the PE that the provisioner can allocate to VMs. 
	 * 
	 * @pre $none
	 * @post $none
	 */
	public PeProvisionerSimple(double availableMips) {
		super(availableMips);
		setPeTable(new HashMap<String, ArrayList<Double>>());
	}

	@Override
	public boolean allocateMipsForVm(Pod pod, double mips) {
		return allocateMipsForVm(pod.getUid(), mips);
	}

	@Override
	public boolean allocateMipsForVm(String vmUid, double mips) {
		if (getAvailableMips() < mips) {
			return false;
		}

		List<Double> allocatedMips;

		if (getPeTable().containsKey(vmUid)) {
			allocatedMips = getPeTable().get(vmUid);
		} else {
			allocatedMips = new ArrayList<Double>();
		}

		allocatedMips.add(mips);

		setAvailableMips(getAvailableMips() - mips);
		getPeTable().put(vmUid, allocatedMips);

		return true;
	}

	@Override
	public boolean allocateMipsForVm(Pod pod, List<Double> mips) {
		int totalMipsToAllocate = 0;
		for (double _mips : mips) {
			totalMipsToAllocate += _mips;
		}

		if (getAvailableMips() + getTotalAllocatedMipsForVm(pod) < totalMipsToAllocate) {
			return false;
		}

		setAvailableMips(getAvailableMips() + getTotalAllocatedMipsForVm(pod) - totalMipsToAllocate);

		getPeTable().put(pod.getUid(), mips);

		return true;
	}

	@Override
	public void deallocateMipsForAllVms() {
		super.deallocateMipsForAllVms();
		getPeTable().clear();
	}

	@Override
	public double getAllocatedMipsForVmByVirtualPeId(Pod pod, int peId) {
		if (getPeTable().containsKey(pod.getUid())) {
			try {
				return getPeTable().get(pod.getUid()).get(peId);
			} catch (Exception e) {
			}
		}
		return 0;
	}

	@Override
	public List<Double> getAllocatedMipsForVm(Pod pod) {
		if (getPeTable().containsKey(pod.getUid())) {
			return getPeTable().get(pod.getUid());
		}
		return null;
	}

	@Override
	public double getTotalAllocatedMipsForVm(Pod pod) {
		if (getPeTable().containsKey(pod.getUid())) {
			double totalAllocatedMips = 0.0;
			for (double mips : getPeTable().get(pod.getUid())) {
				totalAllocatedMips += mips;
			}
			return totalAllocatedMips;
		}
		return 0;
	}

	@Override
	public void deallocateMipsForVm(Pod pod) {
		if (getPeTable().containsKey(pod.getUid())) {
			for (double mips : getPeTable().get(pod.getUid())) {
				setAvailableMips(getAvailableMips() + mips);
			}
			getPeTable().remove(pod.getUid());
		}
	}

	/**
	 * Gets the pe map.
	 * 
	 * @return the pe map
	 */
	protected Map<String, List<Double>> getPeTable() {
		return peTable;
	}

	/**
	 * Sets the pe map.
	 * 
	 * @param peTable the peTable to set
	 */
	@SuppressWarnings("unchecked")
	protected void setPeTable(Map<String, ? extends List<Double>> peTable) {
		this.peTable = (Map<String, List<Double>>) peTable;
	}

}
