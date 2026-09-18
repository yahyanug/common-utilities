package id.com.flare.common.utilities.file;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

/**
 * Reads image dimensions with Java Image I/O without decoding the full image raster.
 * Inspection establishes neither MIME trust nor that an image is safe to store or serve.
 */
public final class ImageDimensionInspector {

	private ImageDimensionInspector() {
	}

	/**
	 * Returns first-frame header dimensions, or empty for null/empty/unsupported content
	 * or an IOException while reading headers. Does not validate pixel data or later
	 * frames.
	 */
	public static Optional<ImageDimensions> inspect(byte[] content) {
		if (content == null || content.length == 0)
			return Optional.empty();
		try {
			return inspect(new ByteArrayInputStream(content));
		}
		catch (IOException exception) {
			return Optional.empty();
		}
	}

	/**
	 * Reads first-frame header dimensions without closing the caller stream or creating
	 * temporary files. Null or unsupported images return empty. Header bytes are cached
	 * in memory; callers should bound input size. Provider support depends on installed
	 * Image I/O readers. A readable header does not prove a complete/valid image.
	 * @throws IOException on I/O failures or malformed/truncated headers reported by a
	 * reader
	 */
	public static Optional<ImageDimensions> inspect(InputStream content) throws IOException {
		if (content == null)
			return Optional.empty();
		try (ImageInputStream imageInput = new MemoryCacheImageInputStream(new NonClosingInputStream(content))) {
			Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
			if (!readers.hasNext())
				return Optional.empty();
			ImageReader reader = readers.next();
			try {
				reader.setInput(imageInput, true, true);
				return Optional.of(new ImageDimensions(reader.getWidth(0), reader.getHeight(0)));
			}
			finally {
				reader.dispose();
			}
		}
	}

	/**
	 * Returns whether dimensions fit inclusive caller-supplied width and height limits.
	 */
	public static boolean isWithinMaximumDimensions(ImageDimensions dimensions, int maximumWidth, int maximumHeight) {
		return dimensions != null && maximumWidth >= 0 && maximumHeight >= 0 && dimensions.width() <= maximumWidth
				&& dimensions.height() <= maximumHeight;
	}

	/** Returns whether dimensions fit an inclusive caller-supplied pixel-count limit. */
	public static boolean isWithinMaximumPixelCount(ImageDimensions dimensions, long maximumPixelCount) {
		return dimensions != null && maximumPixelCount >= 0 && dimensions.pixelCount() <= maximumPixelCount;
	}

	/**
	 * Immutable non-negative dimensions; negative values throw IllegalArgumentException.
	 */
	public record ImageDimensions(int width, int height) {

		public ImageDimensions {
			if (width < 0 || height < 0)
				throw new IllegalArgumentException("Image dimensions must not be negative");
		}

		/** Returns width multiplied by height without integer overflow. */
		public long pixelCount() {
			return (long) this.width * this.height;
		}

	}

	private static final class NonClosingInputStream extends FilterInputStream {

		private NonClosingInputStream(InputStream input) {
			super(input);
		}

		@Override
		public void close() {
			// The caller owns the wrapped stream.
		}

	}

}
