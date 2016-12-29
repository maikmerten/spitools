package de.maikmerten.spitools;

import com.pi4j.wiringpi.Gpio;

/**
 *
 * @author maik
 */
public class SPIConnectionWiringPi implements SPIConnection {

	private final int CLK = 14;
	private final int SEL = 10;
	private final int MISO = 13;
	private final int MOSI = 12;
	
	private final long delayTime = 500;
	private final boolean skipdelay = true;
	

	public SPIConnectionWiringPi() {

		if (Gpio.wiringPiSetup() == -1) {
			throw new RuntimeException("Failed to set up GPIO access");
		}

		Gpio.pinMode(CLK, Gpio.OUTPUT);
		Gpio.pinMode(SEL, Gpio.OUTPUT);
		Gpio.pinMode(MISO, Gpio.INPUT);
		Gpio.pinMode(MOSI, Gpio.OUTPUT);

		deselect();
		clkLow();

	}
	
	private void delay() {
		if(skipdelay) {
			return;
		}
		
		long waitfor = System.nanoTime() + delayTime;
		while(System.nanoTime() < waitfor) {}
	}
	
	private void clkHigh() {
		delay();
		Gpio.digitalWrite(CLK, true);
		delay();
	}
	
	private void clkLow() {
		delay();
		Gpio.digitalWrite(CLK, false);
		delay();
	}
	

	@Override
	public final void select() {
		delay();
		// SPI select is inverted
		Gpio.digitalWrite(SEL, false);
		delay();
	}

	@Override
	public final void deselect() {
		delay();
		// SPI select is inverted
		Gpio.digitalWrite(SEL, true);
		delay();
	}
	
	@Override
	public int sendReceiveByte(int b) {
		int rxBuf = 0;
		int txBuf = (b & 0xFF);
		
		for(int i = 0; i < 8; ++i) {
			Gpio.digitalWrite(MOSI, (txBuf & 0x80) != 0);
			txBuf <<= 1;
			
			clkHigh();
			
			rxBuf <<= 1;
			if (Gpio.digitalRead(MISO) != 0) {
				rxBuf |= 1;
			}
			
			clkLow();
		}
		
		return rxBuf;
	}



	@Override
	public void receiveBytes(byte[] buf) {
		for (int idx = 0; idx < buf.length; ++idx) {
			buf[idx] = (byte) (sendReceiveByte(0) & 0xFF);
		}
	}

	@Override
	public void sendByte(int b) {
		sendReceiveByte(b);
	}

	@Override
	public int receiveByte() {
		return sendReceiveByte(0);
	}

}
