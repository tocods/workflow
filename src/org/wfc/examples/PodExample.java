package org.wfc.examples;

import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerHost;
import org.cloudbus.cloudsim.container.core.ContainerPod;
import org.wfc.core.WFCConstants;
import org.workflowsim.failure.FailureParameters;
import org.workflowsim.utils.*;

import java.io.File;
import java.util.List;

public class PodExample {

    private static String experimentName = "PodExample";
    private static int num_user = 1;
    private static boolean trace_flag = false;
    private static boolean failure_flag = false;
    private static List<Container> containerList;
    private static List<ContainerHost> hostList;
    public static List<? extends ContainerPod> podList;

    public static void main(String[] args) {
        try {
            WFCConstants.CAN_PRINT_SEQ_LOG = false;
            WFCConstants.CAN_PRINT_SEQ_LOG_Just_Step = false;
            WFCConstants.ENABLE_OUTPUT = false;
            WFCConstants.FAILURE_FLAG = false;
            WFCConstants.RUN_AS_STATIC_RESOURCE = true;

            FailureParameters.FTCMonitor ftc_monitor = null;
            FailureParameters.FTCFailure ftc_failure = null;
            FailureParameters.FTCluteringAlgorithm ftc_method = null;
            DistributionGenerator[][] failureGenerators = null;

            String daxPath = "./config/dax/Montage_" + (WFCConstants.WFC_NUMBER_CLOUDLETS - 1) + ".xml";
            File daxFile = new File(daxPath);

            Parameters.SchedulingAlgorithm sch_method = Parameters.SchedulingAlgorithm.MINMIN;//local
            Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.INVALID;//global-stage
            WFCReplicaCatalog.FileSystem file_system = WFCReplicaCatalog.FileSystem.LOCAL;

            OverheadParameters op = new OverheadParameters(0, null, null, null, null, 0);

            ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
            ClusteringParameters cp = new ClusteringParameters(0, 0, method, null);

            Parameters.init(WFCConstants.WFC_NUMBER_VMS, daxPath, null,
                    null, op, cp, sch_method, pln_method,
                    null, 0);
            WFCReplicaCatalog.init(file_system);

        } finally {

        }
    }
}
