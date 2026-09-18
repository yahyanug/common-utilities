package id.com.flare.common.utilities.identity.brunei;

import java.util.Optional;

/**
 * Structural validator for Brunei Identity Card (IC) numbers.
 *
 * <p>
 * The published IC structure is a two-digit block number followed by a six-digit serial,
 * displayed as {@code NN-NNNNNN}. This class accepts either that display form or eight
 * digits for normalization. It validates the published serial ranges for each block, but
 * does not verify that an IC was issued or is current. Validation level: STRUCTURAL.
 * </p>
 *
 * @see <a href=
 * "https://www.oecd.org/tax/automatic-exchange/crs-implementation-and-assistance/tax-identification-numbers/Brunei-Darussalam-TIN.pdf">Brunei
 * IC structure reference</a>
 */
public final class BruneiIdentityCardValidator {

	private static final int CANONICAL_LENGTH = 8;

	private BruneiIdentityCardValidator() {
	}

	/** Returns whether the input has the documented Brunei IC structural format. */
	public static boolean isValid(String identityCardNumber) {
		return normalize(identityCardNumber).isPresent();
	}

	/** Returns a Brunei IC in its documented {@code NN-NNNNNN} display format. */
	public static Optional<String> normalize(String identityCardNumber) {
		if (identityCardNumber == null) {
			return Optional.empty();
		}
		String canonical = identityCardNumber.length() == CANONICAL_LENGTH ? identityCardNumber
				: identityCardNumber.length() == CANONICAL_LENGTH + 1 && identityCardNumber.charAt(2) == '-'
						? identityCardNumber.substring(0, 2) + identityCardNumber.substring(3) : "";
		if (canonical.length() != CANONICAL_LENGTH
				|| !canonical.chars().allMatch(BruneiIdentityCardValidator::isAsciiDigit)
				|| !hasPublishedBlockAndSerialRange(canonical)) {
			return Optional.empty();
		}
		return Optional.of(canonical.substring(0, 2) + "-" + canonical.substring(2));
	}

	/**
	 * Returns the documented resident category represented by a structurally valid IC.
	 */
	public static Optional<BruneiIdentityCardType> getType(String identityCardNumber) {
		return normalize(identityCardNumber).map(value -> {
			int block = Integer.parseInt(value.substring(0, 2));
			if (block < 30) {
				return BruneiIdentityCardType.CITIZEN;
			}
			if (block < 50) {
				return BruneiIdentityCardType.PERMANENT_RESIDENT;
			}
			return BruneiIdentityCardType.TEMPORARY_RESIDENT;
		});
	}

	private static boolean hasPublishedBlockAndSerialRange(String canonical) {
		int block = Integer.parseInt(canonical.substring(0, 2));
		int serial = Integer.parseInt(canonical.substring(2));
		if (block == 0) {
			return inRange(serial, 1, 80_000) || inRange(serial, 110_001, 130_000) || inRange(serial, 250_001, 650_000);
		}
		if (block < 30 || (block >= 31 && block < 50) || block > 50) {
			return inRange(serial, 1, 999_999);
		}
		if (block == 30) {
			return inRange(serial, 80_001, 110_000);
		}
		return inRange(serial, 130_001, 200_000) || inRange(serial, 650_001, 999_999);
	}

	private static boolean inRange(int value, int lowerInclusive, int upperInclusive) {
		return value >= lowerInclusive && value <= upperInclusive;
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
