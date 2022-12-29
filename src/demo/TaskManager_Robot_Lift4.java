package demo;

import kr.ac.uos.ai.arbi.BrokerType;
import taskManager.TaskManager_Robot;

public class TaskManager_Robot_Lift4 {
	public static void main(String[] args) {
		String brokerAddress;
		String robotID;
		int port;
		if(args.length == 0) {
			brokerAddress = "172.16.165.141";
//			brokerAddress = "192.168.100.10";
//			brokerAddress = "127.0.0.1";
			robotID = "AMR_LIFT4";
			port = 61113;
		} else {
			robotID = args[0];
			brokerAddress = args[1];
			port = Integer.parseInt(args[2]);
		}
		TaskManager_Robot robot = new TaskManager_Robot(robotID, brokerAddress, port, BrokerType.ACTIVEMQ);
	}
}
