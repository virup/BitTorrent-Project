import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author shantonu
 *
 */
public class ListeningThread implements Runnable 
{
	private ServerSocket listenSocket;
	private String peerID;
	Socket remoteSocket;
	Thread sendingThread;
	
	public ListeningThread(ServerSocket socket, String peerID) 
	{
		this.listenSocket = socket;
		this.peerID = peerID;
	}
	
	public void run() 
	{
		while(true)
		{
			try
			{
				remoteSocket = listenSocket.accept();
				// instantiates thread for handling individual remote peer
				// 1 denotes active connection type
				sendingThread = new Thread(new RemotePeerHandler(remoteSocket,0,peerID));
				peerProcess.showLog(peerID + " Connection is established");
				peerProcess.sendingThread.add(sendingThread);
				sendingThread.start(); 
			}
			catch(Exception ex)
			{
				peerProcess.showLog(this.peerID + " Exception in connection: " + ex.toString());
			}
		}
	}
	
	public void releaseSocket()
	{
		try 
		{
			if(!remoteSocket.isClosed())
			remoteSocket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}


