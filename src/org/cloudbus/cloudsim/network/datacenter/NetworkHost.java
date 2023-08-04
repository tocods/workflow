/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Pod;
import org.cloudbus.cloudsim.PodScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.lists.PodList;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * NetworkHost class extends {@link Host} to support simulation of networked datacenters. It executes
 * actions related to management of packets (sent and received) other than that of virtual machines
 * (e.g., creation and destruction). A host has a defined policy for provisioning memory and bw, as
 * well as an allocation policy for PE's to virtual machines.
 * 
 * <br/>Please refer to following publication for more details:<br/>
 * <ul>
 * <li><a href="http://dx.doi.org/10.1109/UCC.2011.24">Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.</a>
 * </ul>
 * 
 * @author Saurabh Kumar Garg
 * @since CloudSim Toolkit 3.0
 */
public class NetworkHost extends Host {
	public List<NetworkPacket> packetTosendLocal;

	public List<NetworkPacket> packetTosendGlobal;

        /**
         * List of received packets.
         */
	public List<NetworkPacket> packetrecieved;

        /**
         * @todo the attribute is not being used 
         * and is redundant with the ram capacity defined in {@link Host#ramProvisioner}
         */
	public double memory;

        /** 
         * Edge switch in which the Host is connected. 
         */
	public Switch sw; 

        /**
         * @todo What exactly is this bandwidth?
         * Because it is redundant with the bw capacity defined in {@link Host#bwProvisioner}
         */
	public double bandwidth; 

	/** Time when last job will finish on CPU1. 
         * @todo it is not being used.
         **/
	public List<Double> CPUfinTimeCPU = new ArrayList<Double>();

	/** 
         * @todo it is not being used.
         **/
	public double fintime = 0;

	public NetworkHost(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			PodScheduler podScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, podScheduler);

		packetrecieved = new ArrayList<NetworkPacket>();
		packetTosendGlobal = new ArrayList<NetworkPacket>();
		packetTosendLocal = new ArrayList<NetworkPacket>();

	}

	@Override
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = Double.MAX_VALUE;
		// insert in each vm packet recieved
		recvpackets();
		for (Pod pod : super.getVmList()) {
			double time = ((NetworkPod) pod).updateVmProcessing(currentTime, getVmScheduler()
					.getAllocatedMipsForVm(pod));
			if (time > 0.0 && time < smallerTime) {
				smallerTime = time;
			}
		}
		// send the packets to other hosts/VMs
		sendpackets();

		return smallerTime;

	}

	/**
	 * Receives packets and forward them to the corresponding VM.
	 */
	private void recvpackets() {
		for (NetworkPacket hs : packetrecieved) {
			hs.pkt.recievetime = CloudSim.clock();

			// insert the packet in recievedlist of VM
			Pod pod = PodList.getById(getVmList(), hs.pkt.reciever);
			List<HostPacket> pktlist = ((NetworkCloudletSpaceSharedScheduler) pod.getCloudletScheduler()).pktrecv
					.get(hs.pkt.sender);

			if (pktlist == null) {
				pktlist = new ArrayList<HostPacket>();
				((NetworkCloudletSpaceSharedScheduler) pod.getCloudletScheduler()).pktrecv.put(
						hs.pkt.sender,
						pktlist);

			}
			pktlist.add(hs.pkt);

		}
		packetrecieved.clear();
	}

	/**
	 * Sends packets checks whether a packet belongs to a local VM or to a 
         * VM hosted on other machine.
	 */
	private void sendpackets() {
		for (Pod pod : super.getVmList()) {
                    for (Entry<Integer, List<HostPacket>> es : ((NetworkCloudletSpaceSharedScheduler) pod
                                    .getCloudletScheduler()).pkttosend.entrySet()) {
                        List<HostPacket> pktlist = es.getValue();
                        for (HostPacket pkt : pktlist) {
                                NetworkPacket hpkt = new NetworkPacket(getId(), pkt, pod.getId(), pkt.sender);
                                Pod pod2 = PodList.getById(this.getVmList(), hpkt.recievervmid);
                                if (pod2 != null) {
                                        packetTosendLocal.add(hpkt);
                                } else {
                                        packetTosendGlobal.add(hpkt);
                                }
                        }
                        pktlist.clear();
                    }
		}

		boolean flag = false;

		for (NetworkPacket hs : packetTosendLocal) {
                    flag = true;
                    hs.stime = hs.rtime;
                    hs.pkt.recievetime = CloudSim.clock();
                    // insertthe packet in recievedlist
                    Pod pod = PodList.getById(getVmList(), hs.pkt.reciever);

                    List<HostPacket> pktlist = ((NetworkCloudletSpaceSharedScheduler) pod.getCloudletScheduler()).pktrecv
                                    .get(hs.pkt.sender);
                    if (pktlist == null) {
                            pktlist = new ArrayList<HostPacket>();
                            ((NetworkCloudletSpaceSharedScheduler) pod.getCloudletScheduler()).pktrecv.put(
                                            hs.pkt.sender,
                                            pktlist);
                    }
                    pktlist.add(hs.pkt);
		}
		if (flag) {
                    for (Pod pod : super.getVmList()) {
                        pod.updateVmProcessing(CloudSim.clock(), getVmScheduler().getAllocatedMipsForVm(pod));
                    }
		}

		// Sending packet to other VMs therefore packet is forwarded to a Edge switch
		packetTosendLocal.clear();
		double avband = bandwidth / packetTosendGlobal.size();
		for (NetworkPacket hs : packetTosendGlobal) {
                    double delay = (1000 * hs.pkt.data) / avband;
                    NetworkConstants.totaldatatransfer += hs.pkt.data;

                    CloudSim.send(getDatacenter().getId(), sw.getId(), delay, CloudSimTags.Network_Event_UP, hs);
                    // send to switch with delay
		}
		packetTosendGlobal.clear();
	}

        /**
         * Gets the maximum utilization among the PEs of a given VM.
         * @param pod The VM to get its PEs maximum utilization
         * @return The maximum utilization among the PEs of the VM.
         */
	public double getMaxUtilizationAmongVmsPes(Pod pod) {
		return PeList.getMaxUtilizationAmongVmsPes(getPeList(), pod);
	}

}
