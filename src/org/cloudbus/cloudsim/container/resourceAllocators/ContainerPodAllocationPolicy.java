package org.cloudbus.cloudsim.container.resourceAllocators;


import org.cloudbus.cloudsim.container.core.ContainerPod;
import org.wfc.core.WFCDatacenter;
import org.cloudbus.cloudsim.container.core.ContainerHost;

import java.util.List;
import java.util.Map;

/**
 * Created by sareh on 10/07/15.
 */
public abstract class ContainerPodAllocationPolicy {


    /**
     * The host list.
     */
    private List<? extends ContainerHost> containerHostList;

    /**
     * Allocates a new PodAllocationPolicy object.
     *
     * @param containerHostList Machines available in this Datacentre
     * @pre $none
     * @post $none
     */
    public ContainerPodAllocationPolicy(List<? extends ContainerHost> containerHostList) {
        setContainerHostList(containerHostList);
    }

    /**
     * Allocates a host for a given VM. The host to be allocated is the one that was already
     * reserved.
     *
     * @param vm virtual machine which the host is reserved to
     * @return $true if the host could be allocated; $false otherwise
     * @pre $none
     * @post $none
     */
    public abstract boolean allocateHostForVm(ContainerPod vm);

    /**
     * Allocates a specified host for a given VM.
     *
     * @param vm virtual machine which the host is reserved to
     * @return $true if the host could be allocated; $false otherwise
     * @pre $none
     * @post $none
     */
    public abstract boolean allocateHostForVm(ContainerPod vm, ContainerHost host);

    /**
     * Optimize allocation of the VMs according to current utilization.
     * <p>
     * //     * @param podList           the vm list
     * //     * @param utilizationBound the utilization bound
     * //     * @param time             the time
     *
     * @return the array list< hash map< string, object>>
     */
    public abstract List<Map<String, Object>> optimizeAllocation(List<? extends ContainerPod> vmList);

    /**
     * Releases the host used by a VM.
     *
     * @param containerPod the vm
     * @pre $none
     * @post $none
     */
    public abstract void deallocateHostForVm(ContainerPod containerPod);

    /**
     * Get the host that is executing the given VM belonging to the given user.
     *
     * @param containerPod the vm
     * @return the Host with the given vmID and userID; $null if not found
     * @pre $none
     * @post $none
     */
    public abstract ContainerHost getHost(ContainerPod containerPod);

    /**
     * Get the host that is executing the given VM belonging to the given user.
     *
     * @param vmId   the vm id
     * @param userId the user id
     * @return the Host with the given vmID and userID; $null if not found
     * @pre $none
     * @post $none
     */
    public abstract ContainerHost getHost(int vmId, int userId);

    /**
     * Sets the host list.
     *
     * @param containerHostList the new host list
     */
    protected void setContainerHostList(List<? extends ContainerHost> containerHostList) {
        this.containerHostList = containerHostList;
    }

    /**
     * Gets the host list.
     *
     * @return the host list
     */
    @SuppressWarnings("unchecked")
    public <T extends ContainerHost> List<T> getContainerHostList() {
        return (List<T>) containerHostList;
    }

    public abstract void setDatacenter(WFCDatacenter datacenter);


}



