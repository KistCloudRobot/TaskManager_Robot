package taskManager;

import java.io.BufferedWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import kr.ac.uos.ai.agentCommunicationFramework.agent.AgentExecutor;
import kr.ac.uos.ai.agentCommunicationFramework.channelServer.ChannelType;
import kr.ac.uos.ai.arbi.BrokerType;
import kr.ac.uos.ai.arbi.agent.ArbiAgent;
import kr.ac.uos.ai.arbi.agent.ArbiAgentExecutor;
import kr.ac.uos.ai.arbi.ltm.DataSource;
import kr.ac.uos.ai.arbi.model.GLFactory;
import kr.ac.uos.ai.arbi.model.GeneralizedList;
import kr.ac.uos.ai.arbi.model.Value;
import kr.ac.uos.ai.arbi.model.parser.ParseException;
import taskManager.aplview.APLViewer;
import taskManager.logger.TaskManagerLogger;
import taskManager.utility.CommunicationUtility;
import taskManager.utility.GLMessageManager;
import taskManager.utility.JAMUtilityManager;
import taskManager.utility.RecievedMessage;
import uos.ai.jam.Interpreter;
import uos.ai.jam.JAM;
import uos.ai.jam.parser.JAMParser;

public class TaskManager_Robot extends ArbiAgent {
	private Interpreter interpreter;
	private GLMessageManager msgManager;
	private BlockingQueue<RecievedMessage> messageQueue;
	private TaskManagerLogger logger;
	private McARBIAgentCommunicator mcARBIAgentCommunicator;
	private boolean isTriggered = false;
//	private APLViewer aplViewer;
	
	public static String ENV_JMS_BROKER;
	public static String ENV_ROBOT_NAME;
	public int ENV_WAIT_VERTEX;
	public int ENV_CHARGE_VERTEX;
	public static final String ARBI_PREFIX = "www.arbi.com/";
	public static final String TASKMANAGER_NAME = "www.arbi.com/TaskManager";
	public static String MY_mcARBI_AGENT_ID;
	public String RobotPlanPath;
	
	public static  String CONTEXTMANAGER_ADDRESS = "agent://www.arbi.com/ContextManager";
	public static  String KNOWLEDGEMANAGER_ADDRESS = "agent://www.arbi.com/KnowledgeManager";
	public static  String BEHAVIOUR_INTERFACE_ADDRESS = "agent://www.arbi.com/BehaviorInterface";
	public static  String REASONER_ADRESS = "agent://www.arbi.com/TaskReasoner";
	public static final String ACTION_ADRESS = "agent://www.arbi.com/action";

	public static final String AGENT_PREFIX = "agent://";
	public static final String DATASOURCE_PREFIX = "ds://";
	private TaskManagerDataSource dc;

	
	public TaskManager_Robot() {
		
		System.out.println("start");
		initAddress();
		interpreter = JAM.parse(new String[] { "./TaskManagerLiftPlan/boot.jam" });
		messageQueue = new LinkedBlockingQueue<RecievedMessage>();
		
		msgManager = new GLMessageManager(interpreter);
		
		
		ArbiAgentExecutor.execute(ENV_JMS_BROKER, AGENT_PREFIX + TASKMANAGER_NAME, this,BrokerType.ACTIVEMQ);
		
//		aplViewer = new APLViewer(interpreter);
		//logger = new TaskManagerLogger(this,interpreter);
		init();
	}
	
	
	public TaskManager_Robot(String robotID, String ip) {
		
		System.out.println("start");
		System.out.println("robotID : " + robotID);
		initAddress(robotID,ip);
		interpreter = JAM.parse(new String[] { "./TaskManagerRobotPlan/boot.jam" });
		messageQueue = new LinkedBlockingQueue<RecievedMessage>();
		
		msgManager = new GLMessageManager(interpreter);
		
		
		ArbiAgentExecutor.execute(ENV_JMS_BROKER, AGENT_PREFIX + TASKMANAGER_NAME, this,BrokerType.ZEROMQ);
		
//		aplViewer = new APLViewer(interpreter);
		//logger = new TaskManagerLogger(this,interpreter);
		init();
	}
	
	public void initAddress(String robotID, String ip) {

		String brokerURL = "";
		if(ip.equals("env")) {
			brokerURL = "tcp://" + System.getenv("JMS_BROKER");
		} else {
			brokerURL = ip;
		}
		ENV_ROBOT_NAME = robotID;
		
		
		if (ENV_ROBOT_NAME.equals("AMR_LIFT1")) {
			MY_mcARBI_AGENT_ID = "agent://www.mcarbi.com/AMR_LIFT1";
			ENV_WAIT_VERTEX = 143;
			ENV_CHARGE_VERTEX = 500;
			RobotPlanPath = "./TaskManagerRobotPlan/LiftPlanList.jam";
			
		} else if (ENV_ROBOT_NAME.equals("AMR_LIFT2")) {
			MY_mcARBI_AGENT_ID = "agent://www.mcarbi.com/AMR_LIFT2";
			ENV_WAIT_VERTEX = 157;
			ENV_CHARGE_VERTEX = 500;
			RobotPlanPath = "./TaskManagerRobotPlan/LiftPlanList.jam";
		}else if (ENV_ROBOT_NAME.equals("AMR_LIFT3")) {
			MY_mcARBI_AGENT_ID = "agent://www.mcarbi.com/AMR_LIFT3";
			ENV_WAIT_VERTEX = 146;
			ENV_CHARGE_VERTEX = 501;
			RobotPlanPath = "./TaskManagerRobotPlan/LiftPlanList.jam";
		}else if (ENV_ROBOT_NAME.equals("AMR_LIFT4")) {
			MY_mcARBI_AGENT_ID = "agent://www.mcarbi.com/AMR_LIFT4";
			ENV_WAIT_VERTEX = 158;
			ENV_CHARGE_VERTEX = 501;
			RobotPlanPath = "./TaskManagerRobotPlan/LiftPlanList.jam";
		} else if (ENV_ROBOT_NAME.equals("Palletizer")) {
			MY_mcARBI_AGENT_ID = "agent://www.mcarbi.com/Palletizer";
			ENV_WAIT_VERTEX = 0;
			ENV_CHARGE_VERTEX = 0;
			RobotPlanPath = "./TaskManagerRobotPlan/PalletizerPlanList.jam";
		}
		ENV_JMS_BROKER = brokerURL;
		
	}

	public void initAddress() {
		//ENV_JMS_BROKER = "tcp://" + System.getenv("JMS_BROKER");
		//ENV_AGENT_NAME = System.getenv("AGENT");
		//ENV_ROBOT_NAME = System.getenv("ROBOT");
		//ENV_WAIT_VERTEX = Integer.parseInt(System.getenv("WAIT"));
		//ENV_CHARGE_VERTEX = Integer.parseInt(System.getenv("CHARGE"));
		
	}
	
	public void test(){
		
		if(isTriggered == false){
			System.out.println("test");
			
			messageQueue.add(new RecievedMessage("test","(WakeupService)"));
			isTriggered = true;
		}
		
	}
	
	private void init() {
		msgManager.assertFact("GLUtility", msgManager);
		msgManager.assertFact("Communication", new CommunicationUtility(this, dc));
		msgManager.assertFact("ExtraUtility", new JAMUtilityManager(interpreter));
		msgManager.assertFact("TaskManager", this);
		
		msgManager.assertFact("isro:robot", ENV_ROBOT_NAME);
		msgManager.assertFact("isro:agent", MY_mcARBI_AGENT_ID);
		
		msgManager.assertFact("RobotPlanPath", RobotPlanPath);
		msgManager.assertFact("WaitVertex", ENV_WAIT_VERTEX);
		msgManager.assertFact("ChargeStation", ENV_CHARGE_VERTEX);
		
		msgManager.assertFact("OnAgentTaskStatus", MY_mcARBI_AGENT_ID, "wait", "wait");

		msgManager.assertFact("robotPosition", ENV_ROBOT_NAME, 0, 0);
		msgManager.assertFact("robotAt", ENV_ROBOT_NAME, ENV_WAIT_VERTEX, ENV_WAIT_VERTEX);

		msgManager.assertFact("robotLoading", ENV_ROBOT_NAME, "Unloading");
		msgManager.assertFact("robotVelocity", ENV_ROBOT_NAME, 0);
		msgManager.assertFact("batteryRemain", ENV_ROBOT_NAME, 50);
		msgManager.assertFact("onRobotTaskStatus", ENV_ROBOT_NAME, "wait");
		
		mcARBIAgentCommunicator = new McARBIAgentCommunicator(messageQueue);
		
		AgentExecutor.execute(MY_mcARBI_AGENT_ID, mcARBIAgentCommunicator, ChannelType.ZeroMQ);
		msgManager.assertFact("McARBIAgentCommunicator", mcARBIAgentCommunicator);
		
		//aplViewer.init();
		
		Thread t = new Thread() {
			public void run() {
				interpreter.run();
			}
		};
		
		t.start();
	}
	
	public String getConversationID() {
		return this.getConversationID();
	}

	@Override
	public void onNotify(String sender, String notification) {
//		System.out.println("recieved Notify from " + sender + " : " + notification);
//		aplViewer.msgReceived(notification, sender);
		RecievedMessage msg = new RecievedMessage(sender, notification);
		messageQueue.add(msg);	
	}

	@Override
	public void onStart() {
		dc = new TaskManagerDataSource(this);

		dc.connect(ENV_JMS_BROKER, DATASOURCE_PREFIX + TASKMANAGER_NAME,BrokerType.ZEROMQ);

		System.out.println("======Start Test Agent======");
		System.out.println("??");
		// subscribe
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//String subscribeStatement = "(rule (fact (VoicePerceived $user $input)) --> (notify (VoicePerceived $user $input)))";
		//dc.subscribe(subscribeStatement);
		//System.out.println("??");
		String subscribeStatement = "(rule (fact (context $context)) --> (notify (context $context)))";
		System.out.println("??");
		System.out.println(dc.subscribe(subscribeStatement));
		subscribeStatement = "(rule (fact (robotPosition $a $b $c)) --> (notify (context (robotPosition $a $b $c))))";
		System.out.println(dc.subscribe(subscribeStatement));
		subscribeStatement = "(rule (fact (robotLoading $a $b)) --> (notify (context (robotLoading $a $b))))";
		System.out.println(dc.subscribe(subscribeStatement));
		subscribeStatement = "(rule (fact (robotDegree $a $b)) --> (notify (context (robotDegree $a $b))))";
		System.out.println(dc.subscribe(subscribeStatement));
		subscribeStatement = "(rule (fact (onRobotTaskStatus $a $b)) --> (notify (context (onRobotTaskStatus $a $b))))";
		System.out.println(dc.subscribe(subscribeStatement));
		subscribeStatement = "(rule (fact (batteryRemain $a $b)) --> (notify (context (batteryRemain $a $b))))";
		System.out.println(dc.subscribe(subscribeStatement));

		System.out.println("??");
		//subscribeStatement = "(rule (fact (UserIntention $person $intention)) --> (notify (UserIntention $person $intention)))";
		//dc.subscribe(subscribeStatement);
		
	}

	
	public boolean dequeueMessage() {
		if (messageQueue.isEmpty())
			return false;
		else {
			try {
				RecievedMessage message = messageQueue.take();
				GeneralizedList gl = null;
				String data = message.getMessage();
				String sender = message.getSender();
//				aplViewer.msgReceived(data, sender);

				gl = GLFactory.newGLFromGLString(data);

//				System.out.println("message dequeued : " + gl.toString());

				if (gl.getName().equals("PostGoal")) {
					//System.out.println("test");
					gl = gl.getExpression(0).asGeneralizedList();

					System.out.println("goal to post " + gl.toString());
					
					msgManager.assertGoal(gl.toString());
					
				} else if (gl.getName().equals("InitiateServicePackage")) {
					String packageName = gl.getExpression(0).toString();
					packageName = packageName.substring(1, packageName.length() - 1);
					initServicePackage(packageName);
					msgManager.assertGL(gl.toString());
				} else if(gl.getName().equals("relationChanged")) {
					String relationChanged = "(relationChanged " + gl.getExpression(0).toString() + ")";
					msgManager.assertGL(relationChanged);
				} else if (gl.getName().equals("GoalReport")) {
					System.out.println(gl.toString());
					msgManager.assertFact(gl.toString());
				} else if (gl.getName().equals("GoalRequest")) {
					//System.out.println("requested Goal : " + gl.toString());
					GeneralizedList goalGL = gl.getExpression(0).asGeneralizedList();
					msgManager.assertFact(goalGL.getName() + "RequestedFrom", sender, goalGL.getExpression(1), goalGL.getExpression(2));
				} else if (gl.getName().equals("context")) {
					String recievedContext = "(ContextRecieved \"" + gl.getExpression(0).asGeneralizedList().getName() + "\" " + gl.getExpression(0).toString() + ")";
					msgManager.assertGL(recievedContext);
				}
				
				else {
					//System.out.println("recieved message : " + data);
					msgManager.assertGL(data);
				}

			} catch (InterruptedException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return true;
		}
	}

	@Override
	public String onQuery(String sender, String query) {
		System.out.println("recieved query from " + sender + " : " + query);

//		aplViewer.msgReceived(query, sender);

		String result = handleQuery(query);

		return "(result \"" + result + "\")";
	}

	private String handleQuery(String query) {
		String result = "success";
		try {
			GeneralizedList gl = GLFactory.newGLFromGLString(query);
			String name = gl.getName();
			if (name.equals("retractFact")) {
				System.out.println("retractFact called");
				msgManager.retractFact(gl.getExpression(0).toString());
			} else if (name.equals("updateFact")) {
				System.out.println("updateFact called");
				msgManager.updateFact(gl.getExpression(0).toString(), gl.getExpression(1).toString());
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return result;
	}


	@Override
	public void onData(String sender, String data) {
		//System.out.println("recieved data from " + sender + " : " + data);
		RecievedMessage msg = new RecievedMessage(sender, data);
		messageQueue.add(msg);
		System.out.println("message add complete " + sender + " : " + data);
	}

	@Override
	public String onRequest(String sender, String request) {
		//System.out.println("received data from " + sender + " : " + request);
		RecievedMessage msg = new RecievedMessage(sender, request);
		messageQueue.add(msg);

		return "(ok)";
	}
	

	public void initServicePackage(String packageName) {
		String retrieve = "";
		String plan = "";
		System.out.println("retrieving service package start");

		GeneralizedList gl = null;
		int i = 0;
		while (true) {
			i++;
			String data = "(ServicePackage \"" + packageName + "\" \"plan\" \"" + i + "\" $a)";
			System.out.println(data);
			retrieve = dc.retrieveFact(data);
			System.out.println("plan " + i + " retrieval");
			System.out.println(retrieve);
			if (retrieve.equals("(fail)") || retrieve.equals("(error)")) {
				break;
			}

			try {
				gl = GLFactory.newGLFromGLString(retrieve);
				plan = GLFactory.unescape(gl.getExpression(3).toString());
				plan = plan.substring(1, plan.length() - 1);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			JAMParser.parseString(interpreter, plan);
		}
		i = 0;
		while (true) {
			i++;

			String data = "(Context \"" + i + "\" $a)";
			retrieve = dc.retrieveFact(data);
			System.out.println("context retrieval : " + retrieve);

			if (retrieve.equals("(fail)") || retrieve.equals("(error)")) {
				break;
			}

			try {
				gl = GLFactory.newGLFromGLString(retrieve);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String context = GLFactory.unescape(gl.getExpression(1).toString());
			context = context.substring(1, context.length() - 1);
			GeneralizedList contextGL = null;
			try {
				contextGL = GLFactory.newGLFromGLString(context);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String contextStatement = "(" + contextGL.getName();
			String tempText = "";
			
			for (int j = 0; j < contextGL.getExpressionsSize(); j++) {
				tempText = contextGL.getExpression(j).asGeneralizedList().getExpression(0).toString();
				System.out.println(tempText);
				tempText = tempText.substring(1, tempText.length() - 1);
				contextStatement += " " + tempText;
			}
			
			contextStatement += ")";
			String subscribeStatement = "(rule (fact (context " + contextStatement + ")) --> (notify " + contextStatement + "))";

			System.out.println("subscribe statement : " + subscribeStatement);

			String id = dc.subscribe(subscribeStatement);
			System.out.println("context : " + context);
		}
		
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//this.addMessage("dispatcher", "(PostGoal (initiation))");
		
		System.out.println("****ServicePackage Parse Completed****");
		System.out.println("****Initiated Service : " + packageName + " ****");
	}

	public void addMessage(String sender,String data){
		RecievedMessage msg = new RecievedMessage(sender,data);
		messageQueue.add(msg);
	}
	
	public GLMessageManager getMsgManager() {
		return msgManager;
	}
	
	public String toString() {
		return "TaskManager";
	}

	public static void main(String[] args) {

		new TaskManager_Robot();
	}
}
