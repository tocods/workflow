package org.cloudbus.cloudsim.container.schedulers;

import org.cloudbus.cloudsim.container.containerPodProvisioners.ContainerPodPe;
import org.cloudbus.cloudsim.container.lists.ContainerPodPeList;
import org.cloudbus.cloudsim.container.containerPodProvisioners.ContainerPodPeProvisioner;
import org.cloudbus.cloudsim.container.core.ContainerPod;
import org.cloudbus.cloudsim.Log;

import java.util.*;

/**
 * Created by sareh on 14/07/15.
 */
public class ContainerPodSchedulerTimeShared extends ContainerPodScheduler {
    /** The mips map requested. */
    private Map<String, List<Double>> mipsMapRequested;

    /** The pes in use. */
    private int pesInUse;

    /**
     * Instantiates a new vm scheduler time shared.
     *
     * @param pelist the pelist
     */
    public ContainerPodSchedulerTimeShared(List<? extends ContainerPodPe> pelist) {
        super(pelist);
        setMipsMapRequested(new HashMap<String, List<Double>>());
    }

    @Override
    public boolean allocatePesForVm(ContainerPod containerPod, List<Double> mipsShare) {
        //Log.printLine("PodSchedulerTimeShared: allocatePesForVm with mips share size......" + mipsShare.size());
        if (containerPod.isInMigration()) {
            if (!getVmsMigratingIn().contains(containerPod.getUid()) && !getVmsMigratingOut().contains(containerPod.getUid())) {
                getVmsMigratingOut().add(containerPod.getUid());
            }
        } else {
            if (getVmsMigratingOut().contains(containerPod.getUid())) {
                getVmsMigratingOut().remove(containerPod.getUid());
            }
        }
        boolean result = allocatePesForVm(containerPod.getUid(), mipsShare);
        updatePeProvisioning();
        return result;
    }

    /**
     * Allocate pes for vm.
     *
     * @param vmUid the vm uid
     * @param mipsShareRequested the mips share requested
     * @return true, if successful
     */
    protected boolean allocatePesForVm(String vmUid, List<Double> mipsShareRequested) {
        //Log.printLine("PodSchedulerTimeShared: allocatePesForVm for Vmuid......"+vmUid);
        double totalRequestedMips = 0;
        double peMips = getPeCapacity();
        for (Double mips : mipsShareRequested) {
            // each virtual PE of a VM must require not more than the capacity of a physical PE
            if (mips > peMips) {
                return false;
            }
            totalRequestedMips += mips;
        }

        // This scheduler does not allow over-subscription
        if (getAvailableMips() < totalRequestedMips) {
            return false;
        }

        getMipsMapRequested().put(vmUid, mipsShareRequested);
        setPesInUse(getPesInUse() + mipsShareRequested.size());

        if (getVmsMigratingIn().contains(vmUid)) {
            // the destination host only experience 10% of the migrating VM's MIPS
            totalRequestedMips *= 0.1;
        }

        List<Double> mipsShareAllocated = new ArrayList<Double>();
        for (Double mipsRequested : mipsShareRequested) {
            if (getVmsMigratingOut().contains(vmUid)) {
                // performance degradation due to migration = 10% MIPS
                mipsRequested *= 0.9;
            } else if (getVmsMigratingIn().contains(vmUid)) {
                // the destination host only experience 10% of the migrating VM's MIPS
                mipsRequested *= 0.1;
            }
            mipsShareAllocated.add(mipsRequested);
        }

        getMipsMap().put(vmUid, mipsShareAllocated);
        setAvailableMips(getAvailableMips() - totalRequestedMips);

        return true;
    }

    /**
     * Update allocation of VMs on PEs.
     */
    protected void updatePeProvisioning() {
        //Log.printLine("PodSchedulerTimeShared: update the pe provisioning......");
        getPeMap().clear();
//        Log.printConcatLine("The Pe Map is being cleared ");
        for (ContainerPodPe pe : getPeList()) {
            pe.getContainerVmPeProvisioner().deallocateMipsForAllContainerVms();
        }

        Iterator<ContainerPodPe> peIterator = getPeList().iterator();
        ContainerPodPe pe = peIterator.next();
        ContainerPodPeProvisioner containerPodPeProvisioner = pe.getContainerVmPeProvisioner();
        double availableMips = containerPodPeProvisioner.getAvailableMips();

        for (Map.Entry<String, List<Double>> entry : getMipsMap().entrySet()) {
            String vmUid = entry.getKey();
            getPeMap().put(vmUid, new LinkedList<ContainerPodPe>());

            for (double mips : entry.getValue()) {
//                Log.printConcatLine("The mips value is: ",mips);
                while (mips >= 0.1) {
                    if (availableMips >= mips) {
                        containerPodPeProvisioner.allocateMipsForContainerVm(vmUid, mips);
                        getPeMap().get(vmUid).add(pe);
//                        Log.formatLine("The allocated Mips is % f to Pe Id % d", mips, pe.getId());
                        availableMips -= mips;
//                        Log.print(getPeMap().get(vmUid));
                        break;
                    } else {
                        containerPodPeProvisioner.allocateMipsForContainerVm(vmUid, availableMips);
                        if(availableMips != 0){
                        getPeMap().get(vmUid).add(pe);}
                        mips -= availableMips;
//                        Log.print(getPeMap().get(vmUid));
                        if (mips <= 0.1) {
                            break;
                        }
                        if (!peIterator.hasNext()) {
                            Log.printConcatLine("There is no enough MIPS (", mips, ") to accommodate VM ", vmUid);
                            // System.exit(0);
                        }
                        pe = peIterator.next();
                        containerPodPeProvisioner = pe.getContainerVmPeProvisioner();
                        availableMips = containerPodPeProvisioner.getAvailableMips();
                    }
                }
            }
        }
//        Log.printConcatLine("These are the values",getPeMap().keySet());


    }




    @Override
    public void deallocatePesForVm(ContainerPod containerPod) {
        //Log.printLine("PodSchedulerTimeShared: deallocatePesForVm.....");
        getMipsMapRequested().remove(containerPod.getUid());
        setPesInUse(0);
        getMipsMap().clear();
        setAvailableMips(ContainerPodPeList.getTotalMips(getPeList()));

        for (ContainerPodPe pe : getPeList()) {
            pe.getContainerVmPeProvisioner().deallocateMipsForContainerVm(containerPod);
        }
        //Log.printLine("PodSchedulerTimeShared: deallocatePesForVm. allocates again!!!!!!!....");
        for (Map.Entry<String, List<Double>> entry : getMipsMapRequested().entrySet()) {
            allocatePesForVm(entry.getKey(), entry.getValue());
        }

        updatePeProvisioning();

    }

    /**
     * Releases PEs allocated to all the VMs.
     *
     * @pre $none
     * @post $none
     */
    @Override
    public void deallocatePesForAllContainerVms() {
        super.deallocatePesForAllContainerVms();
        getMipsMapRequested().clear();
        setPesInUse(0);
    }
    /**
     * Returns maximum available MIPS among all the PEs. For the time shared policy it is just all
     * the avaiable MIPS.
     *
     * @return max mips
     */
    @Override
    public double getMaxAvailableMips() {
        return getAvailableMips();
    }


    public Map<String, List<Double>> getMipsMapRequested() {
        return mipsMapRequested;
    }

    public void setMipsMapRequested(Map<String, List<Double>> mipsMapRequested) {
        this.mipsMapRequested = mipsMapRequested;
    }

    public int getPesInUse() {
        return pesInUse;
    }

    public void setPesInUse(int pesInUse) {
        this.pesInUse = pesInUse;
    }
}
