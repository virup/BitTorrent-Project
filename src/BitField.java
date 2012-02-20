import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;

/**
 * @author shantonu
 * 
 */
public class BitField implements MessageConstants 
{
	public Piece[] pieces;
	public int size;

	public BitField() 
	{
		size = (int) Math
				.ceil(((double) CommonProperties.fileSize / (double) CommonProperties.pieceSize));
		this.pieces = new Piece[size];

		for (int i = 0; i < this.size; i++)
			this.pieces[i] = new Piece();

	}
	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the pieces
	 */
	public Piece[] getPieces() {
		return pieces;
	}

	/**
	 * @param pieces
	 *            the pieces to set
	 */
	public void setPieces(Piece[] pieces) {
		this.pieces = pieces;
	}
	
	public byte[] encode()
	{
		return this.getBytes();
	}
	
	public static BitField decode(byte[] b)
	{
		BitField returnBitField = new BitField();
		for(int i = 0 ; i < b.length; i ++)
		{
			int count = 7;
			while(count >=0)
			{
				int test = 1 << count;
				if(i * 8 + (8-count-1) < returnBitField.size)
				{
					if((b[i] & (test)) != 0)
						returnBitField.pieces[i * 8 + (8-count-1)].isPresent = 1;
					else
						returnBitField.pieces[i * 8 + (8-count-1)].isPresent = 0;
				}
				count--;
			}
		}
		
		return returnBitField;
	}
	
	// This function should return true if there is some bits which are not
	// there in the invoking bitfield.
	public synchronized boolean compare(BitField yourBitField) {
		int yourSize = yourBitField.getSize();
		/*
		 * String str = ""; for (int i = 0; i < yourSize; i++) { str = str +
		 * yourBitField.pieces[i].isPresent + " "; }
		 * 
		 * peerProcess.showLog(peerProcess.peerID + " " + str + "\n");
		 * peerProcess.showLog(peerProcess.peerID + " My bitfield"); str = "";
		 * for (int i = 0; i < this.size; i++) { str = str +
		 * this.pieces[i].isPresent + " ";
		 *  } peerProcess.showLog(peerProcess.peerID + " " + str + "\n");
		 */

		for (int i = 0; i < yourSize; i++) {
			if (yourBitField.getPieces()[i].getIsPresent() == 1
					&& this.getPieces()[i].getIsPresent() == 0) {
				return true;
			} else
				continue;
		}

		return false;
	}

	// This function should return the first bit number that is not there in
	// myBitField, // but is there in yourBitField.
	public synchronized int returnFirstDiff(BitField yourBitField) 
	{
		int mySize = this.getSize();
		int yourSize = yourBitField.getSize();

		if (mySize >= yourSize) {
			for (int i = 0; i < yourSize; i++) {
				if (yourBitField.getPieces()[i].getIsPresent() == 1
						&& this.getPieces()[i].getIsPresent() == 0) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < mySize; i++) {
				if (yourBitField.getPieces()[i].getIsPresent() == 1
						&& this.getPieces()[i].getIsPresent() == 0) {
					return i;
				}
			}
		}
		// no match found
		return -1;
	}

	public byte[] getBytes() 
	{
		int s = this.size / 8;
		if (size % 8 != 0)
			s = s + 1;
		byte[] iP = new byte[s];
		int tempInt = 0;
		int count = 0;
		int Cnt;
		for (Cnt = 1; Cnt <= this.size; Cnt++)
		{
			int tempP = this.pieces[Cnt-1].isPresent;
			tempInt = tempInt << 1;
			if (tempP == 1) 
			{
				tempInt = tempInt + 1;
			} else
				tempInt = tempInt + 0;

			if (Cnt % 8 == 0 && Cnt!=0) {
				iP[count] = (byte) tempInt;
				count++;
				tempInt = 0;
			}
			
		}
		if ((Cnt-1) % 8 != 0) 
		{
			int tempShift = ((size) - (size / 8) * 8);
			tempInt = tempInt << (8 - tempShift);
			iP[count] = (byte) tempInt;
		}
		return iP;
	}

	
	 static String byteArrayToHexString(byte in[]) 
	 {
	    byte ch = 0x00;

	    int i = 0; 

	    if (in == null || in.length <= 0)
	        return null;
	    String pseudo[] = {"0", "1", "2","3", "4", "5", "6", "7", "8","9", "A", "B", "C", "D", "E","F"};

	    StringBuffer out = new StringBuffer(in.length * 2);

	    while (i < in.length) 
	    {
	        ch = (byte) (in[i] & 0xF0); 
	        ch = (byte) (ch >>> 4);
	        // shift the bits down

	        ch = (byte) (ch & 0x0F);    
	        // must do this is high order bit is on!

	        out.append(pseudo[ (int) ch]);

	        ch = (byte) (in[i] & 0x0F); 

	        out.append(pseudo[ (int) ch]); 
	        i++;
	    }

	    String rslt = new String(out);

	    return rslt;
	}
    
	/**
	 * Initialize the Bit field class's piece information 
	 * at the beginning of Peer Process
	 * @author Clint
	 * 
	 * @param OwnPeerId  Peer Id of the peerProcess
	 * @param hasFile Peer has the full file  
	 */
	public void initOwnBitfield(String OwnPeerId, int hasFile) {

		if (hasFile != 1) {

			// If the file not exists
			for (int i = 0; i < this.size; i++) {
				this.pieces[i].setIsPresent(0);
				this.pieces[i].setFromPeerID(OwnPeerId);
			}

		} else {

			// If the file exists
			for (int i = 0; i < this.size; i++) {
				this.pieces[i].setIsPresent(1);
				this.pieces[i].setFromPeerID(OwnPeerId);
			}

		}

	}

	/**
	 * Update the bit field class and piece information  
	 * Note : It should not be called before calling the 
	 * function initOwnBitfield
	 * 
	 * @author Clint
	 * 
	 * @param pieceIndex piece index to be updated 
	 * @param peerId 
	 * @param piece Piece object  
	 */
	public synchronized void updateBitField(String peerId, Piece piece) {
		try 
		{
			if (peerProcess.ownBitField.pieces[piece.pieceIndex].isPresent == 1) {
				//peerProcess.showLog(peerId + " Piece already received!!");
			} 
			else 
			{
				String fileName = CommonProperties.fileName;
				File file = new File(peerProcess.peerID, fileName);
				int off = piece.pieceIndex * CommonProperties.pieceSize;
				RandomAccessFile raf = new RandomAccessFile(file, "rw");
				byte[] byteWrite;

				byteWrite = piece.filePiece;
				
				//peerProcess.showLog(peerProcess.peerID
				//		+ " has downloaded the PIECE  " + piece.pieceIndex
				//		+ " from Peer = " + peerId);
				raf.seek(off);
				raf.write(byteWrite);

				// Updates the piece data structure...
				this.pieces[piece.pieceIndex].setIsPresent(1);
				this.pieces[piece.pieceIndex].setFromPeerID(peerId);
				raf.close();
				
//				 LOG 10:
				peerProcess.showLog(peerProcess.peerID
						+ " has downloaded the PIECE " + piece.pieceIndex
						+ " from Peer " + peerId
						+ ". Now the number of pieces it has is "
						+ peerProcess.ownBitField.ownPieces());

				if (peerProcess.ownBitField.isCompleted()) {
					peerProcess.remotePeerInfoHash.get(peerProcess.peerID).isInterested = 0;
					peerProcess.remotePeerInfoHash.get(peerProcess.peerID).isCompleted = 1;
					peerProcess.remotePeerInfoHash.get(peerProcess.peerID).isChoked = 0;
					updatePeerInfo(peerProcess.peerID, 1);
					
					// LOG 11:
					peerProcess.showLog(peerProcess.peerID + " has DOWNLOADED the complete file.");
				}
			}

		} catch (Exception e) {
			peerProcess.showLog(peerProcess.peerID
					+ " EROR in updating bitfield " + e.getMessage());
		}

	}
	
	// Updates PeerInfo.cfg file
	public void updatePeerInfo(String clientID, int hasFile)
	{
		BufferedWriter out = null;
		BufferedReader in = null;
		//System.out.println("client ID " + clientID + "has " + hasFile);
		try 
		{
			in= new BufferedReader(new FileReader("PeerInfo.cfg"));
		
			String line;
			StringBuffer buffer = new StringBuffer();
		
			while((line = in.readLine()) != null) 
			{
				if(line.trim().split("\\s+")[0].equals(clientID))
				{
					buffer.append(line.trim().split("\\s+")[0] + " " + line.trim().split("\\s+")[1] + " " + line.trim().split("\\s+")[2] + " " + hasFile);
				}
				else
				{
					buffer.append(line);
					//System.out.println("buf is"+buffer.toString());
				}
				buffer.append("\n");
			}
			
			in.close();
		
			out= new BufferedWriter(new FileWriter("PeerInfo.cfg"));
			//peerProcess.showLog(clientID + " is updating PeerInfo.cfg " );
			out.write(buffer.toString());	
			
			out.close();
		} 
		catch (Exception e) 
		{
			peerProcess.showLog(clientID + " Error in updating the PeerInfo.cfg " +  e.getMessage());
		}
	}
	
	
	public boolean isCompleted() {

		for (int i = 0; i < this.size; i++) {
			if (this.pieces[i].isPresent == 0) {
				return false;
			}
		}
		return true;
	}
	
	public int ownPieces()
	{
		int count = 0;
		for (int i = 0; i < this.size; i++)
			if (this.pieces[i].isPresent == 1) 
				count++;

		return count; 
	}
}
