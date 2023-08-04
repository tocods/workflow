package org.cloudbus.cloudsim.container.podSelectionPolicies;


import org.cloudbus.cloudsim.container.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sareh on 28/07/15.
 */
public abstract class PowerContainerPodSelectionPolicy {

        /**
         * Gets the vms to migrate.
         *
         * @param host the host
         * @return the vms to migrate
         */
        public abstract ContainerPod getVmToMigrate(PowerContainerHost host);

        /**
         * Gets the migratable vms.
         *
         * @param host the host
         * @return the migratable vms
         */
        protected List<PowerContainerPod> getMigratableVms(PowerContainerHost host) {
            List<PowerContainerPod> migratableVms = new ArrayList<>();
            for (PowerContainerPod vm : host.<PowerContainerPod> getVmList()) {
                if (!vm.isInMigration()) {
                    migratableVms.add(vm);
                }
            }
            return migratableVms;
        }

}

