package id.com.flare.common.utilities.identity.indonesia;

import java.time.DateTimeException;
import java.time.MonthDay;
import java.util.Optional;
import id.com.flare.common.utilities.identity.Gender;

/**
 * Structural validator for an Indonesian Nomor Induk Kependudukan (NIK). A NIK has
 * sixteen digits: six regional-administration digits, a six-digit birth-date component,
 * and a four-digit sequence beginning at {@code 0001}. It does not verify issued region
 * codes, a NIK's existence in Dukcapil, or its ownership. Its two-digit birth year is
 * ambiguous, so no full birth date is exposed. Validation level: STRUCTURAL.
 *
 * @see <a href=
 * "https://ppid.kemendagri.go.id/storage/dokumen/MDthYfTypIBUJ3HsVJaj4biUch5T4C5SGbfIQCy0.pdf">Kemendagri
 * NIK reference</a>
 */
public final class NikValidator {

	private static final int NIK_LENGTH = 16;

	private NikValidator() {
	}

	/** Returns whether input conforms to the implemented NIK structural rules. */
	public static boolean isValid(String nik) {
		return nik != null && nik.length() == NIK_LENGTH && nik.chars().allMatch(NikValidator::isAsciiDigit)
				&& birthDay(nik).isPresent() && !nik.endsWith("0000");
	}

	/** Returns the documented sex marker encoded by a structurally valid NIK. */
	public static Optional<Gender> getGender(String nik) {
		if (!isValid(nik))
			return Optional.empty();
		return Optional.of(Integer.parseInt(nik.substring(6, 8)) > 40 ? Gender.FEMALE : Gender.MALE);
	}

	/** Returns the province-code digits of a structurally valid NIK. */
	public static Optional<String> getProvinceCode(String nik) {
		return segment(nik, 0, 2);
	}

	/** Returns the city/regency-code digits of a structurally valid NIK. */
	public static Optional<String> getCityCode(String nik) {
		return segment(nik, 2, 4);
	}

	/** Returns the district-code digits of a structurally valid NIK. */
	public static Optional<String> getDistrictCode(String nik) {
		return segment(nik, 4, 6);
	}

	private static Optional<String> segment(String nik, int start, int end) {
		return isValid(nik) ? Optional.of(nik.substring(start, end)) : Optional.empty();
	}

	private static Optional<MonthDay> birthDay(String nik) {
		int encodedDay = Integer.parseInt(nik.substring(6, 8));
		int day = encodedDay > 40 ? encodedDay - 40 : encodedDay;
		try {
			return Optional.of(MonthDay.of(Integer.parseInt(nik.substring(8, 10)), day));
		}
		catch (DateTimeException exception) {
			return Optional.empty();
		}
	}

	private static boolean isAsciiDigit(int character) {
		return character >= '0' && character <= '9';
	}

}
