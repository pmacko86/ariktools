package com.aific.ariktools.stego;

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


import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.Image;
import java.awt.image.ImageObserver;
import javax.swing.ImageIcon;
import javax.imageio.ImageIO;
import java.io.FileOutputStream;
import java.io.File;

public class Decoder {
	
	protected static void handlesinglepixelE1(int x, int y, int pixel, BufferedImage bufimg) {
		@SuppressWarnings("unused")
		int alpha = (pixel >> 24) & 0xff;
		int red   = (pixel >> 16) & 0xff;
		int green = (pixel >>  8) & 0xff;
		int blue  = (pixel      ) & 0xff;
		
		// Deal with the pixel as necessary...
		
		red = (red & 0x3) << 6;
		green = (green & 0x3) << 6;
		blue = (blue & 0x3) << 6;
		
		bufimg.setRGB(x, y, (red << 16) | (green << 8) | (blue));
	}
	
	protected static void handlesinglepixelE2(int x, int y, int pixel, byte[]  letter, int byteoffset) {
		@SuppressWarnings("unused")
		int alpha = (pixel >> 24) & 3;
		int red   = (pixel >> 16) & 3;
		int green = (pixel >>  8) & 3;
		int blue  = (pixel      ) & 3;
		// Deal with the pixel as necessary...
		letter[byteoffset/8] = (byte) (((byte) (red << (6 - (byteoffset % 8)))) |letter[byteoffset/8]);
		letter[(byteoffset+2)/8] = (byte) ((green << (6 - ((byteoffset + 2) % 8)))|letter[(byteoffset+2)/8]);
		letter[(byteoffset+4)/8] = (byte) ((blue << (6 - ((byteoffset + 4) % 8)))|letter[(byteoffset+4)/8]);
	}
	
	
	private static void handlepixels(Image img, int x, int y, int w, int h, BufferedImage bufimg) {
		int[] pixels = new int[w * h];
		PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			System.err.println("interrupted waiting for pixels!");
			return;
		}
		if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
			System.err.println("image fetch aborted or errored");
			return;
		}
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				handlesinglepixelE1(x+i, y+j, pixels[j * w + i], bufimg);
			}
		}
	}
	
	protected static int byte2int(byte b) {
		if (b >= 0) return (int)b;
		return ((int)(b & 0x7f)) | 0x80;
	}
	
	private static void handleletters(Image img, int x, int y, int w, int h, FileOutputStream textout) {
		int[] pixels = new int[w*h];
		byte[] letter = new byte[3];
		long size = 3;
		PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
		try{
			pg.grabPixels();
		}
		catch (InterruptedException e){
			System.err.println("interrupted while waiting for pixels!");
			return;
		}
		if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
			System.err.println("image fetch aborted or errored");
			return;
		}
		int byteoffset = 0;
		boolean readHeader = false;
		long written = 0;
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				
				handlesinglepixelE2(x+i, y+j, pixels[j * w + i], letter, byteoffset);
				if(byteoffset==18)//only go up to 3 bytes (18+6 used in hspE2)			
				{
					//Write the 3 bytes to disk
					try{
						if(readHeader) {
							if (written < size) {
								if ((size - written) < 3) {
									byte[] buf = new byte[(int)size - (int)written];
									buf[0] = letter[0];
									if (buf.length > 1) buf[1] = letter[1];
									textout.write(buf);
									return;
								}
								else {
									textout.write(letter);
									written += 3;
								}
							}
							else {
								return;
							}
						}
						else {
							size = byte2int(letter[0]) | (byte2int(letter[1]) << 8) | (byte2int(letter[2]) << 16);
							readHeader = true;
						}
					}
					catch(java.lang.Exception e) {
						System.err.println("Write failed: "+e);
					}
					byteoffset=0;
					for(int k=0;k<3;k++)
						letter[k] &= 0x0;
				}
				else
					byteoffset+=6;
				
			}
		}
		
	}
	
	private static long handleletterssize(Image img, int x, int y, int w, int h) {
		int[] pixels = new int[w*h];
		byte[] letter = new byte[3];
		PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
		try{
			pg.grabPixels();
		}
		catch (InterruptedException e){
			System.err.println("interrupted while waiting for pixels!");
			return -1;
		}
		if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
			System.err.println("image fetch aborted or errored");
			return -1;
		}
		int byteoffset = 0;
		for (int j = 0; j < h; j++) {
			for (int i = 0; i < w; i++) {
				
				handlesinglepixelE2(x+i, y+j, pixels[j * w + i], letter, byteoffset);
				if(byteoffset==18) //only go up to 3 bytes (18+6 used in hspE2)			
				{
					return byte2int(letter[0]) | (byte2int(letter[1]) << 8) | (byte2int(letter[2]) << 16);
				}
				else
					byteoffset+=6;
			}
		}

		return -1;
	}
	
	// Return size of encoded binary file
	public static long getDecodeSize(ImageIcon ic) {
		int width = ic.getIconWidth();
		int height = ic.getIconHeight();
		return handleletterssize(ic.getImage(), 0, 0, width, height);
	}
	
	// Decode an image
	public static BufferedImage decode(ImageIcon ic) {
		Image img = ic.getImage();
		int wid=ic.getIconWidth();
		int hei=ic.getIconHeight();
		BufferedImage newimage = new BufferedImage(wid,hei,BufferedImage.TYPE_INT_RGB);
		handlepixels(img, 0, 0, wid, hei, newimage);
		return newimage;
	}
	
	// Decode any binary file
	public static void decode(ImageIcon ic, File outFile) throws java.io.IOException {
		FileOutputStream f = new FileOutputStream(outFile);
		int width = ic.getIconWidth();
		int height = ic.getIconHeight();
		handleletters(ic.getImage(), 0, 0, width, height, f);
		f.close();
	}
	
	public static void DecodeImage(Image img,int x, int y, int width, int height, String[] args) {
		BufferedImage newimage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		handlepixels(img,x,y,width,height,newimage);
		
		int ld = args[1].lastIndexOf('.');
		String extension = (ld < 0) ? ("png") : (args[1].substring(ld + 1));
		File f = new File(args[1]);
		try{
			ImageIO.write(newimage,extension, f);
		}
		catch(java.io.IOException e) {
			System.err.println(e);
		}
	}
	
	public static void DecodeText(Image img, int x, int y, int width, int height, String[] args) {
		try{
			FileOutputStream f = new FileOutputStream(args[1]);
			handleletters(img, x, y, width, height, f);
			try{f.close();}
			catch(java.io.IOException e)
			{
				System.err.println("Cannot close file: "+e);
			}
		}
		catch(java.io.FileNotFoundException e) {
			System.err.println("Error opening file: "+e);
			return;
		}
	}
	
	
	public static void main(String[] args){
		if(args.length<2)
		{
			System.err.println("Insufficient number of arguments.");
			System.err.println("Format is: \"java Decoder <Filename> <New Filename> <optional decoding algorithm>.\"");
			return;
		}
		ImageIcon ic = new ImageIcon(args[0]);
		Image img = ic.getImage();
		int wid=ic.getIconWidth();
		int hei=ic.getIconHeight();
		
		try{
			if(args[2].equals("0"))
				DecodeImage(img,0,0,wid,hei,args);
			else if(args[2].equals("1"))
				DecodeText(img,0,0,wid,hei,args);
		}
		catch(java.lang.ArrayIndexOutOfBoundsException e)
		{
			DecodeImage(img,0,0,wid,hei,args);//default action: decode an image
		}
	}	
}
