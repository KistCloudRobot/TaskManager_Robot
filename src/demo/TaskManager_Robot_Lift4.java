package demo;

import taskManager.TaskManager_Robot;

public class TaskManager_Robot_Lift4 {
	public static void main(String[] args) {	String brokerAddress;
	String robotID;
	if(args.length == 0) {
//		brokerAddress = "tcp://172.16.165.141:61113";
		brokerAddress = "tcp://127.0.0.1:63113";
		robotID = "AMR_LIFT4";	
	} else {
		robotID = args[0];
		brokerAddress = args[1];
	}
	
	TaskManager_Robot tm = new TaskManager_Robot(robotID, brokerAddress);
		
	}
}
