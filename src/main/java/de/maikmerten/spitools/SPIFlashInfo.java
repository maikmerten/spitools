package de.maikmerten.spitools;

/**
 *
 * @author maik
 */
public class SPIFlashInfo {
	
	private final int manufacturer;
	private final int memoryType;
	private final int capacityId;
	private final int capacity;

	public SPIFlashInfo(int manufacturer, int memoryType, int capacityId, int capacity) {
		this.manufacturer = manufacturer;
		this.memoryType = memoryType;
		this.capacityId = capacityId;
		this.capacity = capacity;
	}
	
	
	/**
	 * @return the manufacturer
	 */
	public int getManufacturer() {
		return manufacturer;
	}


	/**
	 * @return the memoryType
	 */
	public int getMemoryType() {
		return memoryType;
	}

	/**
	 * @return the capacity
	 */
	public int getCapacityId() {
		return capacityId;
	}
	
	/**
	 * @return the capacity (in bits)
	 */
	public int getCapacity() {
		return capacity;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("Manufacturer: 0x").append(Integer.toHexString(manufacturer)).append("\n");
		sb.append("Memory Type: 0x").append(Integer.toHexString(memoryType)).append("\n");
		sb.append("Capacity ID: 0x").append(Integer.toHexString(capacityId)).append("\n");
		sb.append("Capacity: ").append(capacity).append(" bits, ").append(capacity / (8.0 * 1024 * 1024)).append(" Megabytes");
		
		
		return sb.toString();
	}
	
	
}
