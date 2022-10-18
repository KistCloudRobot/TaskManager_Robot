package demo;

import taskManager.TaskManager_Robot;

public class TaskManager_Robot_Lift3 {
	public static void main(String[] args) {	String brokerAddress;
	String robotID;
	if(args.length == 0) {
		brokerAddress = "tcp://172.16.165.141:61114";
//		brokerAddress = "tcp://127.0.0.1:64114";
		robotID = "AMR_LIFT3";	
	} else {
		robotID = args[0];
		brokerAddress = args[1];
	}

	
	TaskManager_Robot tm = new TaskManager_Robot(robotID, brokerAddress);
		
	}
}
