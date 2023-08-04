package org.cloudbus.cloudsim.container.lists;

import org.cloudbus.cloudsim.container.core.ContainerPod;

import java.util.List;

/**
 * Created by sareh on 15/07/15.
 */
public class ContainerPodList {

    public static <T extends ContainerPod> T getById(List<T> vmList, int id) {
        for (T vm : vmList) {
            if (vm.getId() == id) {
                return vm;
            }
        }
        return null;
    }

    /**
     * Return a reference to a Pod object from its ID and user ID.
     *
     * @param id     ID of required VM
     * @param userId the user ID
     * @param vmList the vm list
     * @return Pod with the given ID, $null if not found
     * @pre $none
     * @post $none
     */
    public static <T extends ContainerPod> T getByIdAndUserId(List<T> vmList, int id, int userId) {
        for (T vm : vmList) {
            if (vm.getId() == id && vm.getUserId() == userId) {
                return vm;
            }
        }
        return null;
    }

}

