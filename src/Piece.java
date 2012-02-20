public class Piece 
{
	public int isPresent;
	public String fromPeerID;
	public byte [] filePiece; 			
	public int pieceIndex;
	
	public Piece()
	{
		filePiece = new byte[CommonProperties.pieceSize];
		pieceIndex = -1;
		isPresent = 0;
		fromPeerID = null;
	}

	/**
	 * @return the isPresent
	 */
	public int getIsPresent() {
		return isPresent;
	}

	/**
	 * @param isPresent the isPresent to set
	 */
	public void setIsPresent(int isPresent) {
		this.isPresent = isPresent;
	}

	/**
	 * @return the fromPeerID
	 */
	public String getFromPeerID() {
		return fromPeerID;
	}

	/**
	 * @param fromPeerID the fromPeerID to set
	 */
	public void setFromPeerID(String fromPeerID) {
		this.fromPeerID = fromPeerID;
	}
	
	
	/**
	 * Decodes the payload and returns a Piece with pieceIndex
	 * @param payload
	 * @return
	 */
	public static Piece decodePiece(byte []payload)
	{
		Piece piece = new Piece();
		byte[] byteIndex = new byte[MessageConstants.PIECE_INDEX_LEN];
		System.arraycopy(payload, 0, byteIndex, 0, MessageConstants.PIECE_INDEX_LEN);
		piece.pieceIndex = ConversionUtil.byteArrayToInt(byteIndex);
		//System.out.println("Piece index = " + piece.pieceIndex);
		//System.out.println("Payload length = " + payload.length);

		piece.filePiece = new byte[payload.length-MessageConstants.PIECE_INDEX_LEN];
		System.arraycopy(payload, MessageConstants.PIECE_INDEX_LEN, piece.filePiece, 0, payload.length-MessageConstants.PIECE_INDEX_LEN);
		//System.out.println("File Piece length = " + piece.filePiece.length);
		
		return piece;
	}
}
