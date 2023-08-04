package org.cloudbus.cloudsim.container.core;

import org.cloudbus.cloudsim.container.containerPodProvisioners.ContainerPodBwProvisioner;
import org.cloudbus.cloudsim.container.containerPodProvisioners.ContainerPodPe;
import org.cloudbus.cloudsim.container.containerPodProvisioners.ContainerPodRamProvisioner;
import org.cloudbus.cloudsim.container.lists.ContainerPodPeList;
import org.cloudbus.cloudsim.container.schedulers.ContainerPodScheduler;
import org.cloudbus.cloudsim.HostStateHistoryEntry;
import org.cloudbus.cloudsim.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sareh on 14/07/15.
 */
public class ContainerHostDynamicWorkload extends ContainerHost{


        /** The utilization mips. */
        private double utilizationMips;

        /** The previous utilization mips. */
        private double previousUtilizationMips;

        /** The state history. */
        private final List<HostStateHistoryEntry> stateHistory = new LinkedList<HostStateHistoryEntry>();

        /**
         * Instantiates a new host.
         *
         * @param id the id
         * @param ramProvisioner the ram provisioner
         * @param bwProvisioner the bw provisioner
         * @param storage the storage
         * @param peList the pe list
         * @param vmScheduler the VM scheduler
         */
        public ContainerHostDynamicWorkload(
                int id,
                ContainerPodRamProvisioner ramProvisioner,
                ContainerPodBwProvisioner bwProvisioner,
                long storage,
                List<? extends ContainerPodPe> peList,
                ContainerPodScheduler vmScheduler) {
            super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
            setUtilizationMips(0);
            setPreviousUtilizationMips(0);
        }

        /*
         * (non-Javadoc)
         * @see cloudsim.Host#updateVmsProcessing(double)
         */
        @Override
        public double updateContainerVmsProcessing(double currentTime) {
            double smallerTime = super.updateContainerVmsProcessing(currentTime);
            setPreviousUtilizationMips(getUtilizationMips());
            setUtilizationMips(0);
            double hostTotalRequestedMips = 0;

            for (ContainerPod containerPod : getVmList()) {
                getContainerVmScheduler().deallocatePesForVm(containerPod);
            }

            for (ContainerPod containerPod : getVmList()) {
                getContainerVmScheduler().allocatePesForVm(containerPod, containerPod.getCurrentRequestedMips());
            }

            for (ContainerPod containerPod : getVmList()) {
                double totalRequestedMips = containerPod.getCurrentRequestedTotalMips();
                double totalAllocatedMips = getContainerVmScheduler().getTotalAllocatedMipsForContainerVm(containerPod);

                if (!Log.isDisabled()) {
                /*by arman I commented log  Log.formatLine(
                            "%.2f: [Host #" + getId() + "] Total allocated MIPS for VM #" + containerPod.getId()
                                    + " (Host #" + containerPod.getHost().getId()
                                    + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
                            CloudSim.clock(),
                            totalAllocatedMips,
                            totalRequestedMips,
                            containerPod.getMips(),
                            totalRequestedMips / containerPod.getMips() * 100);
                        */
                    List<ContainerPodPe> pes = getContainerVmScheduler().getPesAllocatedForContainerVM(containerPod);
                    StringBuilder pesString = new StringBuilder();
                    for (ContainerPodPe pe : pes) {
                        pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.", pe.getContainerVmPeProvisioner()
                                .getTotalAllocatedMipsForContainerVm(containerPod)));
                    }
                    /*by arman I commented logLog.formatLine(
                            "%.2f: [Host #" + getId() + "] MIPS for VM #" + containerPod.getId() + " by PEs ("
                                    + getNumberOfPes() + " * " + getContainerVmScheduler().getPeCapacity() + ")."
                                    + pesString,
                            CloudSim.clock());
                    */
                }

                if (getVmsMigratingIn().contains(containerPod)) {
                 /*by arman I commented log   Log.formatLine("%.2f: [Host #" + getId() + "] VM #" + containerPod.getId()
                            + " is being migrated to Host #" + getId(), CloudSim.clock());
                    */
                } else {
                    if (totalAllocatedMips + 0.1 < totalRequestedMips) {
                        /*by arman I commented logLog.formatLine("%.2f: [Host #" + getId() + "] Under allocated MIPS for VM #" + containerPod.getId()
                                + ": %.2f", CloudSim.clock(), totalRequestedMips - totalAllocatedMips);
                        */
                    }

                    containerPod.addStateHistoryEntry(
                            currentTime,
                            totalAllocatedMips,
                            totalRequestedMips,
                            (containerPod.isInMigration() && !getVmsMigratingIn().contains(containerPod)));

                    if (containerPod.isInMigration()) {
                       /*by arman I commented log Log.formatLine(
                                "%.2f: [Host #" + getId() + "] VM #" + containerPod.getId() + " is in migration",
                                CloudSim.clock());
                        */
                        totalAllocatedMips /= 0.9; // performance degradation due to migration - 10%
                    }
                }

                setUtilizationMips(getUtilizationMips() + totalAllocatedMips);
                hostTotalRequestedMips += totalRequestedMips;
            }

            addStateHistoryEntry(
                    currentTime,
                    getUtilizationMips(),
                    hostTotalRequestedMips,
                    (getUtilizationMips() > 0));

            return smallerTime;
        }

        /**
         * Gets the completed vms.
         *
         * @return the completed vms
         */
        public List<ContainerPod> getCompletedVms() {
            List<ContainerPod> vmsToRemove = new ArrayList<>();
            for (ContainerPod containerPod : getVmList()) {
                if (containerPod.isInMigration()) {
                    continue;
                }
//                if the  vm is in waiting state then dont kill it just waite !!!!!!!!!
                 if(containerPod.isInWaiting()){
                     continue;
                 }
//              if (containerPod.getCurrentRequestedTotalMips() == 0) {
//                    vmsToRemove.add(containerPod);
//                }
                if(containerPod.getNumberOfContainers()==0 ){
                    vmsToRemove.add(containerPod);
                }
            }
            return vmsToRemove;
        }



    /**
     * Gets the completed vms.
     *
     * @return the completed vms
     */
    public int getNumberofContainers() {
        int numberofContainers = 0;
        for (ContainerPod containerPod : getVmList()) {
            numberofContainers += containerPod.getNumberOfContainers();
            Log.print("The number of containers in VM# " + containerPod.getId()+"is: "+ containerPod.getNumberOfContainers());
            Log.printLine();
        }
        return numberofContainers;
    }




        /**
         * Gets the max utilization among by all PEs.
         *
         * @return the utilization
         */
        public double getMaxUtilization() {
            return ContainerPodPeList.getMaxUtilization(getPeList());
        }

        /**
         * Gets the max utilization among by all PEs allocated to the VM.
         *
         * @param vm the vm
         * @return the utilization
         */
        public double getMaxUtilizationAmongVmsPes(ContainerPod vm) {
            return ContainerPodPeList.getMaxUtilizationAmongVmsPes(getPeList(), vm);
        }

        /**
         * Gets the utilization of memory.
         *
         * @return the utilization of memory
         */
        public double getUtilizationOfRam() {
            return getContainerVmRamProvisioner().getUsedVmRam();
        }

        /**
         * Gets the utilization of bw.
         *
         * @return the utilization of bw
         */
        public double getUtilizationOfBw() {
            return getContainerVmBwProvisioner().getUsedBw();
        }

        /**
         * Get current utilization of CPU in percentage.
         *
         * @return current utilization of CPU in percents
         */
        public double getUtilizationOfCpu() {
            double utilization = getUtilizationMips() / getTotalMips();
            if (utilization > 1 && utilization < 1.01) {
                utilization = 1;
            }
            return utilization;
        }

        /**
         * Gets the previous utilization of CPU in percentage.
         *
         * @return the previous utilization of cpu
         */
        public double getPreviousUtilizationOfCpu() {
            double utilization = getPreviousUtilizationMips() / getTotalMips();
            if (utilization > 1 && utilization < 1.01) {
                utilization = 1;
            }
            return utilization;
        }

        /**
         * Get current utilization of CPU in MIPS.
         *
         * @return current utilization of CPU in MIPS
         */
        public double getUtilizationOfCpuMips() {
            return getUtilizationMips();
        }

        /**
         * Gets the utilization mips.
         *
         * @return the utilization mips
         */
        public double getUtilizationMips() {
            return utilizationMips;
        }

        /**
         * Sets the utilization mips.
         *
         * @param utilizationMips the new utilization mips
         */
        protected void setUtilizationMips(double utilizationMips) {
            this.utilizationMips = utilizationMips;
        }

        /**
         * Gets the previous utilization mips.
         *
         * @return the previous utilization mips
         */
        public double getPreviousUtilizationMips() {
            return previousUtilizationMips;
        }

        /**
         * Sets the previous utilization mips.
         *
         * @param previousUtilizationMips the new previous utilization mips
         */
        protected void setPreviousUtilizationMips(double previousUtilizationMips) {
            this.previousUtilizationMips = previousUtilizationMips;
        }

        /**
         * Gets the state history.
         *
         * @return the state history
         */
        public List<HostStateHistoryEntry> getStateHistory() {
            return stateHistory;
        }

        /**
         * Adds the state history entry.
         *
         * @param time the time
         * @param allocatedMips the allocated mips
         * @param requestedMips the requested mips
         * @param isActive the is active
         */
        public
        void
        addStateHistoryEntry(double time, double allocatedMips, double requestedMips, boolean isActive) {

            HostStateHistoryEntry newState = new HostStateHistoryEntry(
                    time,
                    allocatedMips,
                    requestedMips,
                    isActive);
            if (!getStateHistory().isEmpty()) {
                HostStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
                if (previousState.getTime() == time) {
                    getStateHistory().set(getStateHistory().size() - 1, newState);
                    return;
                }
            }
            getStateHistory().add(newState);
        }

    }


