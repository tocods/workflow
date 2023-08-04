package org.workflowsim.k8s;

import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class Pod {
    public class metadata {
        public String name;
        public String namespace;
        public Map<String, String> labels;
        public Map<String, String> annotations;

        public metadata() {}

        public void setAnnotations(Map<String, String> annotations) {
            this.annotations = annotations;
        }

        public void setLabels(Map<String, String> labels) {
            this.labels = labels;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public Map<String, String> getAnnotations() {
            return annotations;
        }

        public Map<String, String> getLabels() {
            return labels;
        }

        public String getName() {
            return name;
        }

        public String getNamespace() {
            return namespace;
        }
    }

    public class spec {
        public List<Container> containers;
        public String nodeName;

        public spec(){}

        public void setContainers(List<Container> containers) {
            this.containers = containers;
        }

        public void setNodeName(String nodeName) {
            this.nodeName = nodeName;
        }

        public List<Container> getContainers() {
            return containers;
        }

        public String getNodeName() {
            return nodeName;
        }
    }

    private String apiVersion;
    private String kind;
    private metadata metadata;
    private spec spec;


    public List<Pair<Double, Integer>> getContainers() {
        List<Pair<Double, Integer>> ret = new ArrayList<>();
        for (Container c: this.spec.containers) {
            ret.add(new Pair<Double, Integer>(c.getCpus(), c.getMemory()));
        }
        return ret;
    }

    public Pod() {
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiVersion() {
        return this.apiVersion;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getKind() {
        return kind;
    }

    public void setMetadata(Pod.metadata metadata) {
        this.metadata = metadata;
    }

    public Pod.metadata getMetadata() {
        return metadata;
    }

    public void setSpec(Pod.spec spec) {
        this.spec = spec;
    }

    public Pod.spec getSpec() {
        return spec;
    }
}
