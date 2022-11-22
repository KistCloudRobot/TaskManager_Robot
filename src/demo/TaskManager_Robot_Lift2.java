package demo;

import taskManager.TaskManager_Robot;

public class TaskManager_Robot_Lift2 {
	public static void main(String[] args) {
		String brokerAddress;
		String robotID;
		if(args.length == 0) {
//			brokerAddress = "tcp://172.16.165.141:61115";
			brokerAddress = "tcp://192.168.100.10:65115";
//			brokerAddress = "tcp://127.0.0.1:65115";
			robotID = "AMR_LIFT2";	
		} else {
			robotID = args[0];
			brokerAddress = args[1];
		}
		TaskManager_Robot robot = new TaskManager_Robot(robotID, brokerAddress);
		
	}
}
