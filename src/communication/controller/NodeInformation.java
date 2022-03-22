package communication.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.jgroups.Address;

import communication.exception.InitializationConditionNotSatisfiedException;

public class NodeInformation {

	public String ip;

	public int port;

	public String localName;
	
	public String clusterName;
	
	public Address address;

	public int nodeIndex = 0;
	
	public boolean isMaster = false;

	public int numberOfNodeInCluster = 1;

	public NodeInformation() throws InitializationConditionNotSatisfiedException {
		super();
	}

	

	public int getNodeIndex() {
		return nodeIndex;
	}

	public void setNodeIndex(int nodeIndex) {
		this.nodeIndex = nodeIndex;
	}


}
