package id.com.flare.common.utilities.barcode;

/** Supported GS1 Global Trade Item Number (GTIN) lengths. */
public enum GtinType {

	GTIN_8(8), GTIN_12(12), GTIN_13(13), GTIN_14(14);

	private final int length;

	GtinType(int length) {
		this.length = length;
	}

	/** Returns the exact number of digits in this GTIN representation. */
	public int getLength() {
		return length;
	}

}
