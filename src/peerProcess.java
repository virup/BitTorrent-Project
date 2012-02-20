import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;


/**
 * @author shantonu
 *
 */
public class peerProcess implements MessageConstants
{
	public ServerSocket listeningSocket = null;

	public int LISTENING_PORT;

	public String PEER_IP = null;

	public static String peerID;

	public int myPeerIndex;

	public Thread listeningThread; // Thread for listening to remote clients

	public static boolean isFinished = false;

	public static BitField ownBitField = null;

	public static volatile Timer timerPref;

	public static volatile Timer timerUnChok;

	public static volatile Hashtable<String, RemotePeerInfo> remotePeerInfoHash = new Hashtable<String, RemotePeerInfo>();

	public static volatile Hashtable<String, RemotePeerInfo> preferedNeighbors = new Hashtable<String, RemotePeerInfo>();

	public static volatile Hashtable<String, RemotePeerInfo> unchokedNeighbors = new Hashtable<String, RemotePeerInfo>();

	public static volatile Queue<DataMessageWrapper> messageQ = new LinkedList<DataMessageWrapper>();

	public static Hashtable<String, Socket> peerIDToSocketMap = new Hashtable<String, Socket>();

	public static Vector<Thread> receivingThread = new Vector<Thread>();

	public static Vector<Thread> sendingThread = new Vector<Thread>();

	public static Thread messageProcessor;

	
	public static synchronized void addToMsgQueue(DataMessageWrapper msg)
	{
		messageQ.add(msg);
	}
	
	public static synchronized DataMessageWrapper removeFromMsgQueue()
	{
		DataMessageWrapper msg = null;
		
		if(!messageQ.isEmpty())
		{
			msg = messageQ.remove();
		}
		
		return msg;
	}

	public static void readPeerInfoAgain()
	{
		try 
		{
			String st;
			BufferedReader in = new BufferedReader(new FileReader(
					"PeerInfo.cfg"));
			while ((st = in.readLine()) != null)
			{
				String[]args = st.trim().split("\\s+");
				String peerID = args[0];
				int isCompleted = Integer.parseInt(args[3]);
				
				if(isCompleted == 1)
				{
					remotePeerInfoHash.get(peerID).isCompleted = 1;
					remotePeerInfoHash.get(peerID).isInterested = 0;
					remotePeerInfoHash.get(peerID).isChoked = 0;
				}
			}
			in.close();
		}
		catch (Exception e) {
			showLog(peerID + e.toString());
		}
	}
	/**
	 * Class that handles the preferred neighbors information
	 * 1. Adding the preferred neighbors with highest data rate to
	 * the corresponding list
	 * 
	 * @author Clint
	 */
	public static class PreferedNeighbors extends TimerTask {

		public void run() 
		{
			
			//updates remotePeerInfoHash
			readPeerInfoAgain();
			
			int countInterested = 0;
			
			Enumeration<String> keys = remotePeerInfoHash.keys();
			while(keys.hasMoreElements())
			{
				String key = (String)keys.nextElement();
				RemotePeerInfo pref = remotePeerInfoHash.get(key);
				//showLog("peer" + key + " isCompleted =" + pref.isCompleted + " isInterested =" + pref.isInterested + " isChoked =" + pref.isChoked);
				if(key.equals(peerID))continue;
				
				if (pref.isCompleted == 0 && pref.isHandShaked == 1)
				{
					countInterested++;
				} 
				else if(pref.isCompleted == 1)
				{
					try
					{
						preferedNeighbors.remove(key);
					}
					catch (Exception e) {
					}
				}
			}
			String strPref = "";
			if(countInterested > CommonProperties.numOfPreferredNeighbr)
			{
				if(!preferedNeighbors.isEmpty())
					preferedNeighbors.clear();
						
				List <RemotePeerInfo> pv = new ArrayList <RemotePeerInfo>(remotePeerInfoHash.values());
				Collections.sort(pv, new PeerDataRateComparator(false));
				int count = 0;
				for (int i = 0; i < pv.size(); i++) 
				{
					if (count > CommonProperties.numOfPreferredNeighbr - 1)
						break;
					if(pv.get(i).isHandShaked == 1 && !pv.get(i).peerId.equals(peerID) 
							&& remotePeerInfoHash.get(pv.get(i).peerId).isCompleted == 0)
					{
						remotePeerInfoHash.get(pv.get(i).peerId).isPreferredNeighbor = 1;
						preferedNeighbors.put(pv.get(i).peerId, remotePeerInfoHash.get(pv.get(i).peerId));
						
						count++;
						
						strPref = strPref + pv.get(i).peerId + ", ";
						//peerProcess.showLog(peerProcess.peerID + " Selected preferred neighbor is " + pv.get(i).peerId + " data rate - " + pv.get(i).dataRate);
						
						if (remotePeerInfoHash.get(pv.get(i).peerId).isChoked == 1)
						{
							sendUnChoke(peerProcess.peerIDToSocketMap.get(pv.get(i).peerId), pv.get(i).peerId);
							peerProcess.remotePeerInfoHash.get(pv.get(i).peerId).isChoked = 0;
							sendHave(peerProcess.peerIDToSocketMap.get(pv.get(i).peerId), pv.get(i).peerId);
							peerProcess.remotePeerInfoHash.get(pv.get(i).peerId).state = 3;
						}
						
						
					}
				}
			}
			else
			{
				keys = remotePeerInfoHash.keys();
				while(keys.hasMoreElements())
				{
					String key = (String)keys.nextElement();
					RemotePeerInfo pref = remotePeerInfoHash.get(key);
					if(key.equals(peerID)) continue;
					
					if (pref.isCompleted == 0 && pref.isHandShaked == 1)
					{
						if(!preferedNeighbors.containsKey(key))
						{
							//showLog(peerID + " Adding new prefered neighbour " + key);
							strPref = strPref + key + ", ";
							preferedNeighbors.put(key, remotePeerInfoHash.get(key));
							remotePeerInfoHash.get(key).isPreferredNeighbor = 1;
						}
						if (pref.isChoked == 1)
						{
							sendUnChoke(peerProcess.peerIDToSocketMap.get(key), key);
							peerProcess.remotePeerInfoHash.get(key).isChoked = 0;
							sendHave(peerProcess.peerIDToSocketMap.get(key), key);
							peerProcess.remotePeerInfoHash.get(key).state = 3;
						}
						
					} 
					
				}
			}
			// LOG 3: Preferred Neighbors 
			if (strPref != "")
				peerProcess.showLog(peerProcess.peerID + " has selected the preferred neighbors - " + strPref);
		}
	}
	
	private static void sendUnChoke(Socket socket, String remotePeerID) {

		showLog(peerID + " is sending UNCHOKE message to remote Peer " + remotePeerID);
		DataMessage d = new DataMessage(DATA_MSG_UNCHOKE);
		byte[] msgByte = DataMessage.encodeMessage(d);
		SendData(socket, msgByte);

	}
	private static void sendHave(Socket socket, String remotePeerID) {
		
		showLog(peerID + " sending HAVE message to Peer " + remotePeerID);
		byte[] encodedBitField = peerProcess.ownBitField.encode();
		DataMessage d = new DataMessage(DATA_MSG_HAVE, encodedBitField);
		SendData(socket,DataMessage.encodeMessage(d));
		
		encodedBitField = null;
	}
	private static int SendData(Socket socket, byte[] encodedBitField) {
		try {
		OutputStream out = socket.getOutputStream();
		out.write(encodedBitField);
		} catch (IOException e) {
			
			e.printStackTrace();
			return 0;
		}
		return 1;
	}

	/**
	 * Class that handles the Optimistically unchoked neigbhbors information
	 * 1. Adding the Optimistically unchoked neighors to the corresponding
	 * list; here it is taken as the first neighbor which is in choked state
	 * 
	 * @author Clint
	 */
	public static class UnChokedNeighbors extends TimerTask {

		public void run() 
		{
			//updates remotePeerInfoHash
			readPeerInfoAgain();
			
			if(!unchokedNeighbors.isEmpty())
				unchokedNeighbors.clear();
			
			Enumeration<String> keys = remotePeerInfoHash.keys();
			Vector<RemotePeerInfo> peers = new Vector<RemotePeerInfo>();
			while(keys.hasMoreElements())
			{
				String key = (String)keys.nextElement();
				RemotePeerInfo pref = remotePeerInfoHash.get(key);
				
				if (pref.isChoked == 1 
						&& !key.equals(peerID) 
						&& pref.isCompleted == 0 
						&& pref.isHandShaked == 1)
					peers.add(pref);
			}
			
			// Randomize the vector elements 	
			if (peers.size() > 0)
			{
				Collections.shuffle(peers);
				RemotePeerInfo p = peers.firstElement();
				
				remotePeerInfoHash.get(p.peerId).isOptUnchokedNeighbor = 1;
				unchokedNeighbors.put(p.peerId, remotePeerInfoHash.get(p.peerId));
				// LOG 4:
				peerProcess.showLog(peerProcess.peerID + " has the optimistically unchoked neighbor " + p.peerId);
				
				if (remotePeerInfoHash.get(p.peerId).isChoked == 1)
				{
					peerProcess.remotePeerInfoHash.get(p.peerId).isChoked = 0;
					sendUnChoke(peerProcess.peerIDToSocketMap.get(p.peerId), p.peerId);
					sendHave(peerProcess.peerIDToSocketMap.get(p.peerId), p.peerId);
					peerProcess.remotePeerInfoHash.get(p.peerId).state = 3;
				}
			}
			
		}

	}

	/**
	 * Methods to start and stop the Prefered Neighbors and Optimistically
	 * unchoked neigbhbors update threads
	 * @author Clint 
	 */
	public static void startUnChokedNeighbors() 
	{
		timerPref = new Timer();
		timerPref.schedule(new UnChokedNeighbors(),
				CommonProperties.optUnchokingInterval * 1000 * 0,
				CommonProperties.optUnchokingInterval * 1000);
	}

	public static void stopUnChokedNeighbors() {
		timerPref.cancel();
	}

	public static void startPreferredNeighbors() {
		timerPref = new Timer();
		timerPref.schedule(new PreferedNeighbors(),
				CommonProperties.unchokingInterval * 1000 * 0,
				CommonProperties.unchokingInterval * 1000);
	}

	public static void stopPreferredNeighbors() {
		timerPref.cancel();
	}
	


	/**
	 * Generates log message in following format
	 * [Time]: Peer [peer_ID] [message]
	 * @param message
	 */
	public static void showLog(String message)
	{
		LogGenerator.writeLog(DateUtil.getTime() + ": Peer " + message);
		System.out.println(DateUtil.getTime() + ": Peer " + message);
	}
	
	/**
	 * Reads the system details from the Common.cfg file 
	 * and populates to CommonProperties class static variables 
	 */
	public static void readCommonProperties() {
		String line;
		try {
			BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
			while ((line = in.readLine()) != null) {
				String[] tokens = line.split("\\s+");
				if (tokens[0].equalsIgnoreCase("NumberOfPreferredNeighbors")) {
					CommonProperties.numOfPreferredNeighbr = Integer
							.parseInt(tokens[1]);
				} else if (tokens[0].equalsIgnoreCase("UnchokingInterval")) {
					CommonProperties.unchokingInterval = Integer
							.parseInt(tokens[1]);
				} else if (tokens[0]
						.equalsIgnoreCase("OptimisticUnchokingInterval")) {
					CommonProperties.optUnchokingInterval = Integer
							.parseInt(tokens[1]);
				} else if (tokens[0].equalsIgnoreCase("FileName")) {
					CommonProperties.fileName = tokens[1];
				} else if (tokens[0].equalsIgnoreCase("FileSize")) {
					CommonProperties.fileSize = Integer.parseInt(tokens[1]);
				} else if (tokens[0].equalsIgnoreCase("PieceSize")) {
					CommonProperties.pieceSize = Integer.parseInt(tokens[1]);
				}
			}

			in.close();
		} catch (Exception ex) {
			showLog(peerID + ex.toString());
		}
	}

	/**
	 * Reads the Peer details from the PeerInfo.cfg file 
	 * and populates to peerInfoVector vector
	 */
	public static void readPeerInfo() {

		String st;

		try {
			BufferedReader in = new BufferedReader(new FileReader(
					"PeerInfo.cfg"));
			int i = 0;
			while ((st = in.readLine()) != null) {

				String[] tokens = st.split("\\s+");

				remotePeerInfoHash.put(tokens[0], new RemotePeerInfo(tokens[0],
						tokens[1], tokens[2], Integer.parseInt(tokens[3]), i));

				i++;

			}

			in.close();
		} catch (Exception ex) {
			showLog(peerID + ex.toString());
		}
	}
	
	

	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) 
	{
		//TODO:
		// 1. connect to bootstrap node
		// 2. read configuration file
		// 3. start listening thread
		// 4. start sending thread
		// 5. check for termination
		peerProcess pProcess = new peerProcess();
		peerID = args[0];

		try
		{
			// starts saving standard output to log file
			LogGenerator.start("log_peer_" + peerID +".log");
			showLog(peerID + " is started");

			// reads Common.cfg file and populates CommonProperties class
			readCommonProperties();

			// reads PeerInfo.cfg file and populates RemotePeerInfo class
			readPeerInfo();
			
			// for the initial calculation
			initializePrefferedNeighbours();
			
			boolean isFirstPeer = false;

			Enumeration<String> e = remotePeerInfoHash.keys();
			
			while(e.hasMoreElements())
			{
				RemotePeerInfo peerInfo = remotePeerInfoHash.get(e.nextElement());
				if(peerInfo.peerId.equals(peerID))
				{
					// checks if the peer is the first peer or not
					pProcess.LISTENING_PORT = Integer.parseInt(peerInfo.peerPort);
					pProcess.myPeerIndex = peerInfo.peerIndex;
					if(peerInfo.getIsFirstPeer() == 1)
					{
						isFirstPeer = true;
						break;
					}
				}
			}
			
			// Initialize the Bit field class 
			ownBitField = new BitField();
			ownBitField.initOwnBitfield(peerID, isFirstPeer?1:0);
			
			messageProcessor = new Thread(new MessageProcessor(peerID));
			messageProcessor.start();
			
			if(isFirstPeer)
			{
				try
				{
					pProcess.listeningSocket = new ServerSocket(pProcess.LISTENING_PORT);
					
					//instantiates and starts Listening Thread
					pProcess.listeningThread = new Thread(new ListeningThread(pProcess.listeningSocket, peerID));
					//showLog(peerID + " starts listening to port " + pProcess.LISTENING_PORT);
					pProcess.listeningThread.start();
				}
				catch(SocketTimeoutException tox)
				{
					showLog(peerID + " gets time out expetion: " + tox.toString());
					LogGenerator.stop();
					System.exit(0);
				}
				catch(IOException ex)
				{
					showLog(peerID + " gets exception in Starting Listening thread: " + pProcess.LISTENING_PORT + ex.toString());
					LogGenerator.stop();
					System.exit(0);
				}
			}
			// Not the first peer
			else
			{	
				createEmptyFile();
				
				e = remotePeerInfoHash.keys();
				while(e.hasMoreElements())
				{
					RemotePeerInfo peerInfo = remotePeerInfoHash.get(e.nextElement());
					if(pProcess.myPeerIndex > peerInfo.peerIndex)
					{
						// spawns a sending thread for each client
						// 0 denotes passive connection type
						//showLog(" " + peerID + " spawning a receiving thread.");
						Thread tempThread = new Thread(new RemotePeerHandler(
								peerInfo.getPeerAddress(), Integer
										.parseInt(peerInfo.getPeerPort()), 1,
								peerID));
						receivingThread.add(tempThread);
						tempThread.start();
					}
				}

				// Spawns a listening thread
				try
				{
					pProcess.listeningSocket = new ServerSocket(pProcess.LISTENING_PORT);
					pProcess.listeningThread = new Thread(new ListeningThread(pProcess.listeningSocket, peerID));
					pProcess.listeningThread.start();
					//showLog(peerID + " starts listening to port " + pProcess.LISTENING_PORT);
				}
				catch(SocketTimeoutException tox)
				{
					showLog(peerID + " gets time out exception in Starting the listening thread: " + tox.toString());
					LogGenerator.stop();
					System.exit(0);
				}
				catch(IOException ex)
				{
					showLog(peerID + " gets exception in Starting the listening thread: " + pProcess.LISTENING_PORT + " "+ ex.toString());
					LogGenerator.stop();
					System.exit(0);
				}
			}
			
			startPreferredNeighbors();
			startUnChokedNeighbors();
			
			while(true)
			{
				// checks for termination
				isFinished = isFinished();
				if (isFinished) {
					showLog("All peers have completed downloading the file.");

					stopPreferredNeighbors();
					stopUnChokedNeighbors();

					try {
						Thread.currentThread();
						Thread.sleep(2000);
					} catch (InterruptedException ex) {
					}

					if (pProcess.listeningThread.isAlive())
						pProcess.listeningThread.stop();

					if (messageProcessor.isAlive())
						messageProcessor.stop();

					for (int i = 0; i < receivingThread.size(); i++)
						if (receivingThread.get(i).isAlive())
							receivingThread.get(i).stop();

					for (int i = 0; i < sendingThread.size(); i++)
						if (sendingThread.get(i).isAlive())
							sendingThread.get(i).stop();

					break;
				} else {
					try {
						Thread.currentThread();
						Thread.sleep(5000);
					} catch (InterruptedException ex) {
					}
				}
			}
			/*
			 * if(!pProcess.listeningSocket.isClosed()) {
			 * //pProcess.listeningSocket.close(); }
			 */
		}
		catch(Exception ex)
		{
			showLog(peerID + " Exception in ending : " + ex.getMessage() );
		}
		finally
		{
			showLog(peerID + " Peer process is exiting..");
			LogGenerator.stop();
			System.exit(0);
		}
	}

	private static void initializePrefferedNeighbours() 
	{
		Enumeration<String> keys = remotePeerInfoHash.keys();
		while(keys.hasMoreElements())
		{
			String key = (String)keys.nextElement();
			if(!key.equals(peerID))
			{
				preferedNeighbors.put(key, remotePeerInfoHash.get(key));		
			}
		}
	}

	/**
	 * Checks if all peer has down loaded the file
	 */
	public static synchronized boolean isFinished() {

		String line;
		int hasFileCount = 1;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(
					"PeerInfo.cfg"));

			while ((line = in.readLine()) != null) {
				hasFileCount = hasFileCount
						* Integer.parseInt(line.trim().split("\\s+")[3]);
			}
			if (hasFileCount == 0) {
				in.close();
				return false;
			} else {
				in.close();
				return true;
			}

		} catch (Exception e) {
			showLog(e.toString());
			return false;
		}

	}
	
	public static void createEmptyFile() {
		
		try {
			File dir = new File(peerID);
			dir.mkdir();

			File newfile = new File(peerID, CommonProperties.fileName);
			OutputStream os = new FileOutputStream(newfile, true);
			byte b = 0;
			
			//showLog(peerID + " Size of file = " + CommonProperties.fileSize);
			
			for (int i = 0; i < CommonProperties.fileSize; i++)
				os.write(b);
			os.close();
		} 
		catch (Exception e) {
			showLog(peerID + " ERROR in creating the file : " + e.getMessage());
		}

	}
	

}
