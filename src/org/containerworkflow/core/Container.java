package org.containerworkflow.core;

import org.workflowsim.Task;

public class Container extends Task {
    /**
     * Allocates a new Task object. The task length should be greater than or
     * equal to 1.
     *
     * @param taskId     the unique ID of this Task
     * @param taskLength the length or size (in MI) of this task to be executed
     *                   in a PowerDatacenter
     * @pre taskId >= 0
     * @pre taskLength >= 0.0
     * @post $none
     */
    public Container(int taskId, long taskLength) {
        super(taskId, taskLength);
    }
}
