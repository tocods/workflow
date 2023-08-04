package org.workflowsim.k8s;

import org.cloudbus.cloudsim.Log;

import java.util.List;

public class Container {
    private class volumeMount {
        public String name;
        public String mountPath;
        public Boolean readOnly;

        public volumeMount(){}

        public void setMountPath(String mountPath) {
            this.mountPath = mountPath;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setReadOnly(Boolean readOnly) {
            this.readOnly = readOnly;
        }

        public String getName() {
            return name;
        }

        public Boolean getReadOnly() {
            return readOnly;
        }

        public String getMountPath() {
            return mountPath;
        }
    }


    private class port {
        public String name;
        public Integer containerPort;
        public Integer hostPort;
        public String protocol;

        public port(){}

        public void setName(String name) {
            this.name = name;
        }

        public void setContainerPort(Integer containerPort) {
            this.containerPort = containerPort;
        }

        public void setHostPort(Integer hostPort) {
            this.hostPort = hostPort;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getName() {
            return name;
        }

        public Integer getContainerPort() {
            return containerPort;
        }

        public Integer getHostPort() {
            return hostPort;
        }

        public String getProtocol() {
            return protocol;
        }
    }

    private class env {
        public String name;
        public String value;

        public env() {}

        public void setName(String name) {
            this.name = name;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    private class resources {
        public class resource {
            public String cpu;
            public String memory;

            public resource(){}

            public void setCpu(String cpu) {
                this.cpu = cpu;
            }

            public void setMemory(String memory) {
                this.memory = memory;
            }

            public String getCpu() {
                return cpu;
            }

            public String getMemory() {
                return memory;
            }
        }

        public resource limits;
        public resource requests;

        public resources(){}

        public void setLimits(resource limits) {
            this.limits = limits;
        }

        public void setRequests(resource requests) {
            this.requests = requests;
        }

        public resource getLimits() {
            return limits;
        }

        public resource getRequests() {
            return requests;
        }
    }

    private String name;
    private String image;
    private String imagePullPolicy;
    private List<String> command;
    private List<String> args;
    private String workingDir;
    private List<volumeMount> volumeMounts;
    private List<port> ports;
    private List<env> env;
    private resources resources;

    public Container(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Container.env> getEnv() {
        return env;
    }

    public List<port> getPorts() {
        return ports;
    }

    public List<String> getArgs() {
        return args;
    }

    public List<String> getCommand() {
        return command;
    }

    public List<volumeMount> getVolumeMounts() {
        return volumeMounts;
    }

    public Container.resources getResources() {
        return resources;
    }

    public String getImage() {
        return image;
    }

    public String getImagePullPolicy() {
        return imagePullPolicy;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public void setEnv(List<Container.env> env) {
        this.env = env;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setPorts(List<port> ports) {
        this.ports = ports;
    }

    public void setImagePullPolicy(String imagePullPolicy) {
        this.imagePullPolicy = imagePullPolicy;
    }

    public void setResources(Container.resources resources) {
        this.resources = resources;
    }

    public void setVolumeMounts(List<volumeMount> volumeMounts) {
        this.volumeMounts = volumeMounts;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public Double getCpus() {
        String cpuRequest = this.resources.requests.cpu;
        Boolean ifTranToM = false;
        if (cpuRequest.substring(cpuRequest.length() - 1).equals("m")) {
            cpuRequest = cpuRequest.substring(0, cpuRequest.length() - 1);
            ifTranToM = true;
        }
        Double ret = 0.0;
        try {
            ret = Double.parseDouble(cpuRequest);
            if (ifTranToM) {
                ret = ret / 1000;
            }
        } catch(Exception e) {
            Log.printLine("error when parse cpu request to Double");
            Log.printLine(e.getMessage());
            return 0.0;
        }
        return ret;
    }

    public Integer getMemory() {
        String ramRequest = this.resources.requests.memory;
        try {
            Integer ret = Integer.parseInt(ramRequest.substring(0, ramRequest.length() - 2));
            return ret;
        } catch (Exception e) {
            Log.printLine("error when parse memory request to Integer");
            Log.printLine(e.getMessage());
            return 0;
        }
    }


}
