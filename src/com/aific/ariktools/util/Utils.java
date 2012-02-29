package com.aific.ariktools.util;

/*
 * ArikTools
 * Copyright (C) Arik Z.Lakritz, Peter Macko, and David K. Wittenberg
 * 
 * This file is part of ArikTools.
 *
 * ArikTools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ArikTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ArikTools.  If not, see <http://www.gnu.org/licenses/>.
 */


import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

import com.aific.ariktools.gui.FileExtensionFilter;
import com.aific.ariktools.stream.BitStream;

public class Utils {

    private static File lastChosenImageFile = null;
	
	private static boolean useEscapeCharacter_initialized = false;
	private static boolean useEscapeCharacter = false;
	
	private static final String[] SUPPORTED_IMAGE_FORMATS = { "png", "jpg", "jpeg", "gif" };
	private static final String[] SUPPORTED_IMAGE_LOSSLESS_FORMATS = { "png" };
	
	private static FileExtensionFilter filterImage = new FileExtensionFilter("Supported Image Formats", SUPPORTED_IMAGE_FORMATS);
	private static FileExtensionFilter filterImageLossless = new FileExtensionFilter("Lossless Image Formats", SUPPORTED_IMAGE_LOSSLESS_FORMATS);
	
	public static int STDOUT_COLOR_NORMAL = 7;
	public static int STDOUT_COLOR_BIG_HEADER = 15;
	public static int STDOUT_COLOR_HEADER = 15;		// light blue: 12
	public static int STDOUT_COLOR_WARNING = 1;
	public static int STDOUT_COLOR_ERROR = 9;
	public static int STDOUT_COLOR_DONE_MESSAGE = 7;
	
	
	/**
	 * Chooses an image to open or save
	 */
	public static File chooseImage(Component parent, String title, boolean open, boolean losslessOnly) {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle(title);
		fc.setDialogType(open ? JFileChooser.OPEN_DIALOG : JFileChooser.SAVE_DIALOG);
		if (lastChosenImageFile != null) fc.setSelectedFile(lastChosenImageFile);
		
		fc.addChoosableFileFilter(losslessOnly ? filterImageLossless : filterImage);
		// fc.setAccessory(new ImagePreview(fc));
		
		int r = 0;
		if (open) {
			r = fc.showOpenDialog(parent);
		}
		else {
			r = fc.showSaveDialog(parent);
		}
		if (r != JFileChooser.APPROVE_OPTION) return null;
        
        lastChosenImageFile = fc.getSelectedFile();
        
        return lastChosenImageFile;
    }
	
	
	/**
	 * Chooses an image to open or save
	 */
	public static File chooseImage(Component parent, String title, boolean open) {
        return chooseImage(parent, title, open, false);
    }
	
	
	/**
	 * Chooses a file to open or save
	 */
	public static File chooseFile(Component parent, String title, boolean open) {
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle(title);
		fc.setDialogType(open ? JFileChooser.OPEN_DIALOG : JFileChooser.SAVE_DIALOG);
		int r = 0;
		if (open) {
			r = fc.showOpenDialog(parent);
		}
		else {
			r = fc.showSaveDialog(parent);
		}
		if (r != JFileChooser.APPROVE_OPTION) return null;
		return fc.getSelectedFile();
	}
	
	
	/**
	 * Save an image
	 */
	public static boolean saveImage(String fileName, BufferedImage image) {
		String extension = getExtension(new File(fileName));
		if (extension == null) extension = "png";
		File f = new File(fileName);
		try{
			ImageIO.write(image, extension.toLowerCase(), f);
		}
		catch(java.io.IOException e){
			System.err.println(e);
			return false;
		}
		return true;
	}
	
	
	/**
	 * Save a file
	 */
	public static boolean saveStringFile(String fileName, String str) {
		File f = new File(fileName);
		try {
			PrintWriter w = new PrintWriter(new FileWriter(f));
			w.print(str);
			w.close();
		}
		catch (java.io.IOException e) {
			System.err.println(e);
			return false;
		}
		return true;
	}
	
	
	/**
	 * Perform a task for all files in the list in a headless environment
	 */
	public static void performHeadless(String taskName, String[] files, HeadlessTask task) {
		
		// Basic assertions
		
		if (files.length <= 0) {
			System.err.println(Utils.escapeColor(Utils.STDOUT_COLOR_WARNING) + "No input files." + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL));
			return;
		}
		
		
		// Print the task name
		
		System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + taskName + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL));
		
		
		// Start the timer
		
		long gtime1 = System.currentTimeMillis();
		
		
		// Initialize the "reports" directory
		
		File currentDirectory = new File(".");
		File reports = null;
		
		try {
			reports = new File(currentDirectory, "reports");
			if (reports.exists()) {
				if (!reports.isDirectory()) {
					throw new Exception("File \"reports\" already exists");
				}
			}
			else {
				reports.mkdir();
			}
		}
		catch (Throwable e) {
			System.err.println(Utils.escapeColor(Utils.STDOUT_COLOR_ERROR) + "Cannot create the directory for reports: " + e.getMessage()
							   + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL));
			return;
		}
		
		
		// Run the task for all files
		
		int count = 0;
		for (int i = 0; i < files.length; i++) {
			File baseFile = new File(files[i]);
			
			
			// Build the list of the input files
			
			File workFiles[] = null;
			if (baseFile.exists()) {
				if (baseFile.isDirectory()) {
					System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_WARNING) + "  Ignoring directory " + baseFile + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL));
				}
				else {
					workFiles = new File[1];
					workFiles[0] = baseFile;
				}
			}
			else {
				final String filter = files[i];
				
				try {
					"".matches(filter);
				}
				catch (java.util.regex.PatternSyntaxException e) {
					System.err.println(Utils.escapeColor(Utils.STDOUT_COLOR_WARNING) + "  Invalid regular expression: " + e.getMessage()
									   + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL));
					continue;
				}
				
				workFiles = currentDirectory.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.matches(filter);
					}
				});
			}
			
			if (workFiles == null) continue;
			
			if (workFiles.length == 0) {
				System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_WARNING) + "  No matches found for the regular expression " + files[i]
								   + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL));
				continue;
			}
			
			
			// Run the task for all input files
			
			for (int j = 0; j < workFiles.length; j++) {
				File file = workFiles[j];
				System.out.print("  " + file.getName());
				count++;
				
				try {
					long time1 = System.currentTimeMillis();
					System.gc();
					task.performTask(file, reports);
					long time2 = System.currentTimeMillis();
					System.out.println(": Done in " + (Math.round(((time2 - time1) / 10.0)) / 100.0) + " seconds");
				}
				catch (Throwable e) {
					System.out.println(": " + Utils.escapeColor(Utils.STDOUT_COLOR_ERROR) + e.getMessage() + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL));
					continue;
				}
			}
		}
		
		long gtime2 = System.currentTimeMillis();
		System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_DONE_MESSAGE) + "Done (" + count + " file" + (count == 1 ? "" : "s") + " in "
						   + (Math.round(((gtime2 - gtime1) / 10.0)) / 100.0) + " seconds)" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL));
	}
	
	
	/**
	 * Determines whether the escape characters can be used in the standard output
	 */
	public static boolean useEscapeCharacter() {
		if (useEscapeCharacter_initialized) return useEscapeCharacter;
		String osName = System.getProperty("os.name");
		useEscapeCharacter = (osName.startsWith("Linux") || osName.startsWith("Mac"));
		useEscapeCharacter_initialized = true;
		return useEscapeCharacter;
	}
	
	
	/**
	 * Returns a stdout escape sequence for foreground color, or an empty string if the escape character
	 * should not be used
	 */
	public static String escapeColor(int color) {
		if (!useEscapeCharacter()) return "";
		if ((color < 0) || (color >= 16)) return "";
		
		return "" + (char)27 + ((color < 8) ? ("[0;3" + color) : ("[1;3" + (color - 8))) + "m";
	}
	
	
	/**
	* Overwrite confirmation
	 */
	public static boolean shouldOverwrite(Component parent, String fileName) {
		return JOptionPane.showConfirmDialog(parent, "Are you sure you want to overwrite " + fileName, "Warning",
											 JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
	}
	
	
	/**
	 * Center the given window
	 */
	public static void centerFrame(JFrame frame) {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((d.width - frame.getWidth()) / 2, (d.height - frame.getHeight()) / 2);
	}
	

	/**
	 * Converts a byte to an integer
	 */
	public static int byte2int(byte b) {
		if (b >= 0) return (int)b;
		return ((int)(b & 0x7f)) | 0x80;
	}
	
	
	/**
	 * Converts given bit stream to an array of bytes
	 */
	public static byte[] captureStream(BitStream stream) {
		int bytes = stream.size() / 8;
		stream.reset();
		byte[] data = new byte[bytes];
		for (int i = 0; i < bytes; i++) {
			int d = 0;
			for (int j = 0; j < 8; j++) {
				d |= ((int)stream.next()) << j;
			}
			data[i] = (byte)d;
		}
		return data;
	}
	
	
	/**
	 * Grabs a RGB values of the given image icon
	 *
	 * @param icon the image icon
	 * @return an array of RGB values
	 */
	public static int[] grabRGB(ImageIcon icon) {
	
		int width = icon.getIconWidth();
		int height = icon.getIconHeight();
		
		int[] rgb = new int[width * height];
		PixelGrabber pg = new PixelGrabber(icon.getImage(), 0, 0, width, height, rgb, 0, width);
		
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted waiting for pixels!");
		}
		
		if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
			throw new RuntimeException("Image fetch aborted or errored");
		}
		
		for (int i = 0; i < rgb.length; i++) rgb[i] &= 0xffffff;
		
		return rgb;
	}
	
	
	/**
	 * Create a buffered image from an RGB array
	 *
	 * @param rgb the RGB array
	 * @param width the image width
	 * @param height the image height
	 * @return the buffered image
	 */
	public static BufferedImage plotRGB(int rgb[], int width, int height) {
		
		BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		for (int y = 0; y < height; y++) {
			int base = y * width;
			for (int x = 0; x < width; x++) {
				b.setRGB(x, y, rgb[base + x]);
			}
		}
		
		return b;
	}

	
	/**
	 * Calculate the mean square error of the two RGB data
	 *
	 * @param rgb1 first RGB data
	 * @param rgb2 second RGB data
	 * @return the mean square error
	 */
	public static double RGB_MSE(int[] rgb1, int[] rgb2) {
		
		// Assertion
		
		if (rgb1.length != rgb2.length) throw new RuntimeException("The images need to have the same number of pixels");
		
		// Calculate the MSE
		
		double mse = 0;
		
		for (int i = 0; i < rgb1.length; i++) {
			int a = rgb1[i];
			int b = rgb2[i];
			mse += sqr(( a        & 0xff) - ( b        & 0xff));
			mse += sqr(((a >>  8) & 0xff) - ((b >>  8) & 0xff));
			mse += sqr(((a >> 16) & 0xff) - ((b >> 16) & 0xff));
		}
		
		return mse / (rgb1.length * 3);
	}
	
	
	/**
	 * Calculate the peak signal-to-noise ratio
	 *
	 * @param mse the mean square error
	 * @param max the maximum signal intensity
	 * @return the peak signal to noise ratio (dB)
	 */
	public static double PNSR(double mse, int max) {
		
		return 10.0 * Math.log(sqr(max) / mse) / Math.log(10);
	}
	
	
	/**
	 * Calculate the peak signal-to-noise ratio
	 *
	 * @param rgb1 first RGB data
	 * @param rgb2 second RGB data
	 * @return the peak signal to noise ratio (dB)
	 */
	public static double RGB_PNSR(int[] rgb1, int[] rgb2) {
		
		return PNSR(RGB_MSE(rgb1, rgb2), 255);
	}
	
	
	/**
	 * Determine whether the given image is grayscale
	 *
	 * @param rgb the RGB data
	 * @return true if the image is grayscale
	 */
	public static boolean isGrayscale(int[] rgb) {
		
		for (int i = 0; i < rgb.length; i++) {
			int pixel = rgb[i];
			int red   = (pixel >> 16) & 0xff;
			int green = (pixel >>  8) & 0xff;
			int blue  = (pixel      ) & 0xff;
			if ((red != blue) || (red != green) || (blue != green)) return false;
		}
		
		return true;
	}

	
	/**
     * Get the extension of a file.
	 * The code is based on http://java.sun.com/docs/books/tutorial/uiswing/components/examples/Utils.java
	 *
	 * @param f the file
	 * @return the file extension
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
		
        if ((i > 0) && (i < s.length() - 1)) {
            ext = s.substring(i + 1);
        }
		
        return ext;
    }
	
	
	/**
	 * Determine whether the given image icon is grayscale
	 *
	 * @param icon the image icon
	 * @return true if the image is grayscale
	 */
	public static boolean isGrayscale(ImageIcon icon) {
		return isGrayscale(grabRGB(icon));
	}
	
	
	/**
	 * Generate the absolute difference image between two images
	 *
	 * @param rgb1 the first RGB array
	 * @param rgb2 the second RGB array
	 * @param scale how much to scale the result
	 * @return new rgb array
	 */
	public static int[] absoluteDifferenceImage(int[] rgb1, int[] rgb2, double scale) {
		
		if (rgb1.length != rgb2.length) throw new RuntimeException("The two images need to have the same number of pixels");
		int[] out = new int[rgb1.length];
		
		for (int i = 0; i < rgb1.length; i++) {
			
			int c1 = rgb1[i];
			int r1 = (c1 >> 16) & 0xff;
			int g1 = (c1 >>  8) & 0xff;
			int b1 = (c1      ) & 0xff;
			
			int c2 = rgb2[i];
			int r2 = (c2 >> 16) & 0xff;
			int g2 = (c2 >>  8) & 0xff;
			int b2 = (c2      ) & 0xff;
			
			int r = Math.round(Math.max(Math.min(Math.round(Math.abs(r1 - r2) * scale), 255), 0));
			int g = Math.round(Math.max(Math.min(Math.round(Math.abs(g1 - g2) * scale), 255), 0));
			int b = Math.round(Math.max(Math.min(Math.round(Math.abs(b1 - b2) * scale), 255), 0));
				
			out[i] = (r << 16) | (g << 8) | b;
		}
		
		return out;
	}
	
	
	/**
	 * Generate the absolute difference image between two images
	 *
	 * @param icon1 the first image icon
	 * @param icon2 the second image icon
	 * @param scale how much to scale the result
	 * @return the difference buffered image
	 */
	public static BufferedImage absoluteDifferenceImage(ImageIcon icon1, ImageIcon icon2, double scale) {	
		return plotRGB(absoluteDifferenceImage(grabRGB(icon1), grabRGB(icon2), scale), icon1.getIconWidth(), icon1.getIconHeight());
	}
	
	
	/**
	 * Optionally validate the file name to have an extension of a lossless image format.
	 * If the initial file name does not have such extension, ask the user.
	 *
	 * @param parent the parent component
	 * @param f the file
	 * @return the optionally changed file reference
	 */
	public static File checkLosslessImageExt(Component parent, File f) {
		
		if (f == null) return null;
		
		String ext = getExtension(f);
		
		
		// No extension
		
		if (ext == null) {
			
			Object[] options = { "Use .png", "No Extension" };
			String msg = "<html><body><b>Are you sure to not use a file extension?</b><br><br>"
				+ "An image file without a proper extension<br>may not be opened correctly by other programs.</body></html>";
			
			int n = JOptionPane.showOptionDialog(parent, msg, "Save Image", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
												 options, options[0]);
			
			if (n == 0) f = new File(f.getAbsolutePath() + ".png");
			
			return f;
		}
		
		
		// Unsupported extension
		
		ext = ext.toLowerCase();
		boolean unsupported = true;
		
		for (int i = 0; i < SUPPORTED_IMAGE_FORMATS.length; i++) {
			if (ext.equals(SUPPORTED_IMAGE_FORMATS[i])) {
				unsupported = false; break;
			}
		}
		
		if (unsupported) {
			
			Object[] options = { "Use .png", "Cancel" };
			String msg = "<html><body><b>The image format \"" + ext + "\" is not supported.</b><br><br>"
				+ "Would you like to save the image as \"png\" instead?</body></html>";
			
			int n = JOptionPane.showOptionDialog(parent, msg, "Save Image", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null,
												 options, options[0]);
			
			if (n == 0) {
				String str = f.getAbsolutePath();
				return new File(str.substring(0, str.lastIndexOf('.')) + ".png");
			}
			else {
				return  null;
			}
		}
		
		
		// Not recommended extension
		
		ext = ext.toLowerCase();
		boolean notRecommended = true;
		
		for (int i = 0; i < SUPPORTED_IMAGE_LOSSLESS_FORMATS.length; i++) {
			if (ext.equals(SUPPORTED_IMAGE_LOSSLESS_FORMATS[i])) {
				notRecommended = false; break;
			}
		}
		
		if (notRecommended) {
			
			Object[] options = { "Use .png", "Keep ." + ext };
			String msg = "<html><body><b>Are you sure to save the file as \"" + ext + "\"?</b><br><br>"
				+ "The file format you have selected is not recommended,<br>"
				+ "because it does not employ lossless compression on<br>"
				+ "RGB images with 24-bit color depth.</body></html>";
			
			int n = JOptionPane.showOptionDialog(parent, msg, "Save Image", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
												 options, options[0]);
			
			if (n == 0) {
				String str = f.getAbsolutePath();
				return new File(str.substring(0, str.lastIndexOf('.')) + ".png");
			}

			return f;
		}		

		
		// It's okay
		
		return f;
	}
	
	
	/**
	 * Find the minimum element in an array
	 * 
	 * @param array the array
	 * @return the minimum element
	 */
	public static float minimum(float[] array) {
		if (array.length <= 0) throw new IllegalArgumentException("The array is empty");
		float min = array[0];
		for (int i = 1; i < array.length; i++) if (array[i] < min) min = array[i];
		return min;
	}
	
	
	/**
	 * Find the maximum element in an array
	 * 
	 * @param array the array
	 * @return the maximum element
	 */
	public static float maximum(float[] array) {
		if (array.length <= 0) throw new IllegalArgumentException("The array is empty");
		float max = array[0];
		for (int i = 1; i < array.length; i++) if (array[i] > max) max = array[i];
		return max;
	}
	
	
	/**
	 * Collapse a two-dimensional array to a one-dimensional one
	 * 
	 * @param array the two-dimensional array
	 * @return the corresponding one-dimensional array
	 */
	public static float[] collapse(float[][] array) {
		float[] a = new float[array[0].length * array.length];
		int i = 0;
		for (int x = 0; x < array.length; x++) {
			for (int y = 0; y < array[x].length; y++) {
				a[i++] = array[x][y];
			}
		}
		return a;
	}
	

	/**
	 * Calculate a square of a number
	 *
	 * @param x the number
	 * @return x^2
	 */
	public static int sqr(int x) {
		return x * x;
	}
	

	/**
	 * Calculate a square of a number
	 *
	 * @param x the number
	 * @return x^2
	 */
	public static long sqr(long x) {
		return x * x;
	}
	

	/**
	 * Calculate a square of a number
	 *
	 * @param x the number
	 * @return x^2
	 */
	public static float sqr(float x) {
		return x * x;
	}
	

	/**
	 * Calculate a square of a number
	 *
	 * @param x the number
	 * @return x^2
	 */
	public static double sqr(double x) {
		return x * x;
	}
	
	
	/**
	 * Double error function complement
	 *
	 * This code was implemented by the National Institute of Standards and Technology, and can be downloaded
	 * as a part of STS 1.5 from http://csrc.nist.gov/rng/. By 17 Section 105 of the United States Code, the
	 * source code of this method is considered public domain.
	 */
	public static double erfc(double x) {
		double t, u, y;
		
		t = 3.97886080735226 / (Math.abs(x) + 3.97886080735226);
		u = t - 0.5;
		y = (((((((((0.00127109764952614092 * u + 1.19314022838340944e-4) * u - 
					0.003963850973605135) * u - 8.70779635317295828e-4) * u + 
				  0.00773672528313526668) * u + 0.00383335126264887303) * u - 
				0.0127223813782122755) * u - 0.0133823644533460069) * u + 
			  0.0161315329733252248) * u + 0.0390976845588484035) * u + 
			0.00249367200053503304;
		y = ((((((((((((y * u - 0.0838864557023001992) * u - 
					   0.119463959964325415) * u + 0.0166207924969367356) * u + 
					 0.357524274449531043) * u + 0.805276408752910567) * u + 
				   1.18902982909273333) * u + 1.37040217682338167) * u + 
				 1.31314653831023098) * u + 1.07925515155856677) * u + 
			   0.774368199119538609) * u + 0.490165080585318424) * u + 
			 0.275374741597376782) * t * Math.exp(-x * x);
		return x < 0 ? 2 - y : y;
	}
	

	public static double rreimanndelta(double accuracy) {
		double cum_sum = 0;
		double upto = Double.MAX_VALUE - accuracy;
		upto = 10000000;
		for(double i = accuracy; i < upto; i+=accuracy)
		{
			cum_sum += accuracy * (Math.exp(-i) * Math.pow(i,((7.0/2.0)-1.0)));
		}
		return cum_sum;
	}

	public static double lreimanndelta(double accuracy) {
		double cum_sum = 0;
		double upto = Double.MAX_VALUE - accuracy;
		upto = 10000000;
		for(double i = 0; i < upto; i+=accuracy)
		{
			cum_sum += accuracy * (Math.exp(-i) * Math.pow(i,((7.0/2.0)-1.0)));
		}
		return cum_sum;
	}
	
	public static double rrchi2p(double chisq, double accuracy)
	{
		double cum_sum = 0;
		double upto = chisq;
		for(double i = accuracy; i <= upto; i+=accuracy)
		{
			cum_sum += accuracy * Math.pow(i,5)*Math.exp(-i/2.0);
		}
		return (cum_sum/(Math.pow(2,7.0/2.0)*rreimanndelta(accuracy)));
	}

	
}


