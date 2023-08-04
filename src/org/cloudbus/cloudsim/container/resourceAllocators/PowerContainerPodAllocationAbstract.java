package org.cloudbus.cloudsim.container.resourceAllocators;


import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.container.core.ContainerHost;
import org.cloudbus.cloudsim.container.core.ContainerPod;
import org.cloudbus.cloudsim.core.CloudSim;
import java.util.*;
/**
 * Created by sareh on 14/07/15.
 */
public abstract  class PowerContainerPodAllocationAbstract extends ContainerPodAllocationPolicy {

        /** The vm table. */
        private final Map<String, ContainerHost> vmTable = new HashMap<String, ContainerHost>();

        /**
         * Instantiates a new power vm allocation policy abstract.
         *
         * @param list the list
         */
        public PowerContainerPodAllocationAbstract(List<? extends ContainerHost> list) {
            super(list);
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.PodAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Pod)
         */
        @Override
        public boolean allocateHostForVm(ContainerPod containerPod) {
            return allocateHostForVm(containerPod, findHostForVm(containerPod));
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.PodAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Pod,
         * org.cloudbus.cloudsim.Host)
         */
        @Override
        public boolean allocateHostForVm(ContainerPod containerPod, ContainerHost host) {
            if (host == null) {
                Log.formatLine("%.2f: No suitable host found for VM #" + containerPod.getId() + "\n", CloudSim.clock());
                return false;
            }
            if (host.containerVmCreate(containerPod)) { // if vm has been succesfully created in the host
                getVmTable().put(containerPod.getUid(), host);
                Log.formatLine(
                        "%.2f: VM #" + containerPod.getId() + " has been allocated to the host #" + host.getId(),
                        CloudSim.clock());
                return true;
            }
            Log.formatLine(
                    "%.2f: Creation of VM #" + containerPod.getId() + " on the host #" + host.getId() + " failed\n",
                    CloudSim.clock());
            return false;
        }

        /**
         * Find host for vm.
         *
         * @param containerPod the vm
         * @return the power host
         */
        public ContainerHost findHostForVm(ContainerPod containerPod) {
            for (ContainerHost host : this.<ContainerHost> getContainerHostList()) {
                if (host.isSuitableForContainerVm(containerPod)) {
                    return host;
                }
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.PodAllocationPolicy#deallocateHostForVm(org.cloudbus.cloudsim.Pod)
         */
        @Override
        public void deallocateHostForVm(ContainerPod containerPod) {
            ContainerHost host = getVmTable().remove(containerPod.getUid());
            if (host != null) {
                host.containerVmDestroy(containerPod);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.PodAllocationPolicy#getHost(org.cloudbus.cloudsim.Pod)
         */
        @Override
        public ContainerHost getHost(ContainerPod vm) {
            return getVmTable().get(vm.getUid());
        }

        /*
         * (non-Javadoc)
         * @see org.cloudbus.cloudsim.PodAllocationPolicy#getHost(int, int)
         */
        @Override
        public ContainerHost getHost(int vmId, int userId) {
            return getVmTable().get(ContainerPod.getUid(userId, vmId));
        }

        /**
         * Gets the vm table.
         *
         * @return the vm table
         */
        public Map<String, ContainerHost> getVmTable() {
            return vmTable;
        }

    public List<ContainerPod> getOverUtilizedVms() {
        List<ContainerPod> vmList = new ArrayList<ContainerPod>();
        for (ContainerHost host : getContainerHostList()) {
            for (ContainerPod vm : host.getVmList()) {
                if (vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) > vm.getTotalMips()) {
                    vmList.add(vm);

                }

            }

        }
        return vmList;
    }


}