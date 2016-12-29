package de.maikmerten.spitools;

import com.pi4j.wiringpi.Gpio;

/**
 *
 * @author maik
 */
public class GPIOSpeedTest {

	public static void main(String[] args) {

		if (Gpio.wiringPiSetup() == -1) {
			throw new RuntimeException("Failed to set up GPIO access");
		}

		Gpio.pinMode(0, Gpio.OUTPUT);

		while (true) {
			Gpio.digitalWrite(0, true);
			Gpio.digitalWrite(0, false);
		}

	}

}
