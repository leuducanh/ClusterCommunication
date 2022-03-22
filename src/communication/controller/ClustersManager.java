package communication.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import communication.model.NodeOrderMessage;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.View;

import communication.exception.InitializationConditionNotSatisfiedException;
import communication.util.Contants;
import org.jgroups.stack.IpAddress;

public class ClustersManager {
    private static ClustersManager clustersManager;

    private NodeCommunicator nodeCommunicator;
//    private List<NodeInformation> listNodeInforInCluster;

    private NodeInformation nodeInforOfLocalMachine;

    private View viewOfTheCluster;

    private ClustersManager() throws InitializationConditionNotSatisfiedException {
        super();

        initNodeFromLocal();
        startConnectingToCluster();
    }

    public static ClustersManager getInstance() throws InitializationConditionNotSatisfiedException {
        if (clustersManager != null) {
            clustersManager = new ClustersManager();
        }
        return clustersManager;
    }

    private void initNodeFromLocal() throws InitializationConditionNotSatisfiedException {
        Map<String, String> propertyNameToValue = loadFromConfigurationFile(Contants.FilePath.CONFIGURATION_FILE_FOR_LOCAL_NODE);

        nodeInforOfLocalMachine = new NodeInformation();
        nodeInforOfLocalMachine.ip = propertyNameToValue.get("ip");
        nodeInforOfLocalMachine.port = Integer.parseInt(propertyNameToValue.get("port"));
        nodeInforOfLocalMachine.clusterName = propertyNameToValue.get("clusterName");
        nodeInforOfLocalMachine.localName = propertyNameToValue.get("localName");

        String jgroupConfigurationProtocolFilePath = propertyNameToValue.get("jgroupConfigurationProtocolFilePath");

        nodeCommunicator = new NodeCommunicator(jgroupConfigurationProtocolFilePath, nodeInforOfLocalMachine);
    }

    private void startConnectingToCluster() throws InitializationConditionNotSatisfiedException {
        nodeCommunicator.setNodeChangeListner(new NodeChangeListener());
        nodeCommunicator.startConnectingToTheCluster();
    }


    public Map<String, String> loadFromConfigurationFile(String configurationPathFile) throws InitializationConditionNotSatisfiedException {
        File file = new File(configurationPathFile);

        FileReader fileReader;
        Map<String, String> propertyNameToValue = new HashMap<String, String>();
        try {
            fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    String[] pair = line.split("=");
                    propertyNameToValue.put(pair[0], pair[1]);
                }
            } catch (NumberFormatException | IOException e) {
                e.printStackTrace();
                throw new InitializationConditionNotSatisfiedException("Data in configuration file not right.");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new InitializationConditionNotSatisfiedException("Can not find configuration file.");
        }

        return propertyNameToValue;
    }

    public int getNumberOfNodeInCluster() {
        return nodeInforOfLocalMachine.numberOfNodeInCluster;
    }

    public int getThisNodeIndex() {
        return nodeInforOfLocalMachine.nodeIndex;
    }

    public boolean isLocalNodeBeMaster() {
        return nodeInforOfLocalMachine.isMaster;
    }

    public class NodeChangeListener {
        public void listen(View newView) {
            List<Address> listNodeAddressInCluster = newView.getMembers();
            viewOfTheCluster = newView;
            if (listNodeAddressInCluster != null && !listNodeAddressInCluster.isEmpty()) {
                if (nodeInforOfLocalMachine.equals(listNodeAddressInCluster.get(0))) {
                    nodeInforOfLocalMachine.isMaster = true;
                } else {
                    nodeInforOfLocalMachine.isMaster = false;
                }

                int index = 0;
                for (Address address : listNodeAddressInCluster) {
                    if (address.equals(nodeInforOfLocalMachine.address)) {
                        nodeInforOfLocalMachine.nodeIndex = index;
                        nodeInforOfLocalMachine.numberOfNodeInCluster = listNodeAddressInCluster.size();
                        break;
                    }
                    index++;
                }

                if (nodeInforOfLocalMachine.isMaster = true) {
                    for (int i = 0; i < listNodeAddressInCluster.size(); i++) {
                        NodeOrderMessage nodeOrderMessage = new NodeOrderMessage();
                        nodeOrderMessage.size = listNodeAddressInCluster.size();
                        nodeOrderMessage.index = i;

                        Message message = new Message();
                        message.setObject(nodeOrderMessage);
                        try {
                            nodeCommunicator.send(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error master tell slave index: " + i);
                        }
                    }
                }
            }
        }
    }
}
