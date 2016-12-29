package de.maikmerten.spitools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * @author maik
 */
public class SPIFlash {

	private final SPIConnection spi;
	private final SPIFlashInfo info;

	public final int OP_ID = 0x90;
	public final int OP_JEDECID = 0x9F;
	public final int OP_READSFDP = 0x5A;
	public final int OP_READ = 0x03;
	public final int OP_CHIPERASE = 0x60;
	public final int OP_READSTATUS1 = 0x05;
	public final int OP_WRITEENABLE = 0x06;
	public final int OP_PAGEPROGRAM = 0x02;

	public SPIFlash() {
		//spi = new SPIConnectionPi4j();
		spi = new SPIConnectionWiringPi();
		info = buildFlashInfo();
	}

	public void sendAddress(int a) {
		spi.sendByte((a >> 16) & 0xFF);
		spi.sendByte((a >> 8) & 0xFF);
		spi.sendByte(a & 0xFF);
	}

	private SPIFlashInfo buildFlashInfo() {

		spi.select();
		spi.sendByte(OP_JEDECID);
		int manufacturer = spi.receiveByte();
		int memoryType = spi.receiveByte();
		int capacityId = spi.receiveByte();
		spi.deselect();

		int capacity = 0;
		spi.select();
		spi.sendByte(OP_READSFDP);
		sendAddress(0x84);
		spi.receiveByte(); // consume dummy byte
		capacity |= spi.receiveByte();
		capacity |= (spi.receiveByte() << 8) & 0xFF00;
		capacity |= (spi.receiveByte() << 16) & 0xFF0000;
		capacity |= (spi.receiveByte() << 24) & 0xFF000000;
		spi.deselect();

		// the capacity seems to denote the last bit *address*, add one to get
		// actual capacity
		capacity += 1;

		// Adestos's AT25SF041 does not provide capacity information
		if (manufacturer == 0x1f && memoryType == 0x84) {
			capacity = 4194304;
		}

		return new SPIFlashInfo(manufacturer, memoryType, capacityId, capacity);
	}

	private boolean isBusy() {
		boolean result = true;

		spi.select();
		spi.sendByte(OP_READSTATUS1);
		result = (spi.receiveByte() & 0x01) != 0;
		spi.deselect();

		return result;
	}

	private void waitWhileBusy() {
		while (isBusy()) {
		}
	}

	private void chipErase() {
		spi.select();
		spi.sendByte(OP_CHIPERASE);
		spi.deselect();

		waitWhileBusy();
	}

	public SPIFlashInfo getFlashInfo() {
		return info;
	}

	public void readFlash(int address, byte[] buf) {

		spi.select();
		spi.sendByte(OP_READ);
		sendAddress(address);
		spi.receiveBytes(buf);

		spi.deselect();
	}

	public void writeEnable() {
		spi.select();
		spi.sendByte(OP_WRITEENABLE);
		spi.deselect();
	}

	public void writeFlash(int address, byte[] buf) {
		writeEnable();
		chipErase();

		boolean firstpage = true;

		for (int i = 0; i < buf.length; ++i) {
			if (firstpage || (address & 0xFF) == 0) {
				firstpage = false;
				spi.deselect();
				waitWhileBusy();
				writeEnable();

				spi.select();
				spi.sendByte(OP_PAGEPROGRAM);
				sendAddress(address);
			}

			spi.sendByte(buf[i]);

			address += 1;
		}

		spi.deselect();

		waitWhileBusy();
	}

	public static void main(String[] args) throws Exception {

		SPIFlash sf = new SPIFlash();
		SPIFlashInfo info = sf.getFlashInfo();
		System.out.println(info);

		byte[] buf = null;
		File f = null;
		if (args.length >= 1) {
			f = new File(args[0]);
			if (!(f.exists() && f.isFile())) {
				System.err.println("Input file must exist!");
				System.exit(1);
			}

			// read input data from file
			FileInputStream fis = new FileInputStream(f);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] fbuf = new byte[512];
			int read = fis.read(fbuf);
			while (read != -1) {
				baos.write(fbuf, 0, read);
				read = fis.read(fbuf);
			}
			fis.close();
			buf = baos.toByteArray();

			if (info.getCapacity() / 8 < buf.length) {
				System.err.println("Input file is larger than flash capacity!");
				System.exit(1);
			}
		}

		byte[] buf2 = null;
		if (buf != null) {
			buf2 = new byte[buf.length];
		} else {
			buf2 = new byte[info.getCapacity() / 8];
		}

		long start, millis;
		if (buf != null) {
			start = System.currentTimeMillis();
			System.out.println("Writing " + buf.length + " bytes to flash... ");
			sf.writeFlash(0, buf);
			millis = System.currentTimeMillis() - start;
			System.out.println("Writing took " + millis / 1000 + " seconds.");
		}

		start = System.currentTimeMillis();
		System.out.println("Reading flash...");
		sf.readFlash(0, buf2);
		millis = System.currentTimeMillis() - start;
		System.out.println("Reading took " + millis / 1000 + " seconds.");

		if (buf != null) {
			boolean match = true;
			for (int i = 0; i < Math.min(buf.length, buf2.length); ++i) {
				if (buf[i] != buf2[i]) {
					match = false;
				}
			}

			if (match) {
				System.out.println("All okay!");
			} else {
				System.out.println("!!! READ DATA DOES NOT MATCH WRITTEN DATA !!!");
			}
		}

		FileOutputStream fos = new FileOutputStream(new File("flashdump.bin"));
		fos.write(buf2);
		fos.close();
		System.out.println("Flash dump written to 'flashdump.bin'.");

	}

}
