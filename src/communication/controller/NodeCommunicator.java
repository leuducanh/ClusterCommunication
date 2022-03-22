package communication.controller;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.jgroups.*;

import communication.controller.ClustersManager.NodeChangeListener;
import communication.exception.InitializationConditionNotSatisfiedException;



public class NodeCommunicator extends ReceiverAdapter {

	public JChannel jChannel;
	public Address addressOfThisNode;
	
	public NodeInformation localNodeInformation;
	
	public String jgroupConfigurationProtocolFilePath;
	public NodeChangeListener nodeChangeListener;
//	public List<NodeOrder> 
	
	public NodeCommunicator(String jgroupConfigurationProtocolFilePath, NodeInformation localNodeInformation) {
		super();
		
		this.jgroupConfigurationProtocolFilePath = jgroupConfigurationProtocolFilePath;
		this.localNodeInformation = localNodeInformation;
	}	
	
	public void startConnectingToTheCluster() throws InitializationConditionNotSatisfiedException {
		try {
			jChannel = new JChannel(jgroupConfigurationProtocolFilePath);
			jChannel.setReceiver(this);
			jChannel.setName(localNodeInformation.localName);
			jChannel.connect(localNodeInformation.clusterName);
			addressOfThisNode = jChannel.getAddress();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InitializationConditionNotSatisfiedException("Having problem when connect node in a cluster");
		}
	}
	
	public void setNodeChangeListner(NodeChangeListener nodeChangeListener) {
		this.nodeChangeListener = nodeChangeListener;
	}

	public void send(Message message) throws Exception {
		try {
			jChannel.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receive(Message msg) {
		Object msgObject = msg.getObject();		
	}

	@Override
	public void viewAccepted(View newView) {
//		Receiver.super.viewAccepted(newView);
		if(newView instanceof MergeView) {
			
		} else {
			nodeChangeListener.listen(newView);
		}
	}
}
