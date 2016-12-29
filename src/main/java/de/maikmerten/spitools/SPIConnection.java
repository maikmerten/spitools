package de.maikmerten.spitools;

/**
 *
 * @author maik
 */
public interface SPIConnection {
	
	public void select();
	public void deselect();
	
	public void sendByte(int b);
	public int receiveByte();
	public int sendReceiveByte(int b);

	public void receiveBytes(byte[] buf);
	
}
