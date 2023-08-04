package org.wfc.core;


import org.cloudbus.cloudsim.Log;
import org.workflowsim.k8s.Pod;
//import org.yaml.snakeyaml.*;
import sun.misc.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


public class YamlUtils {
   /* public static org.workflowsim.k8s.Pod ParsePodFromPath(String path) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        InputStream input = new FileInputStream(new File(path));
        /*Scanner s = new Scanner(input).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        Log.printLine(result);
        Pod pod;
        pod = yaml.loadAs(input, Pod.class);
        return pod;
    }

    public static void main(String[] args) throws FileNotFoundException {
        Pod pod = YamlUtil.ParsePodFromPath("config/pod/pod.yml");

    }*/
}
