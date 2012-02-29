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
import java.io.FileInputStream;
import java.io.File;

public class Encoder {
	
	@SuppressWarnings("unused")
	protected static void handlesinglepixelE1(int x, int y, int pixel,int pixel2, BufferedImage bufimg) {
		int alpha = (pixel >> 24) & 0xff;
		int red   = (pixel >> 16) & 0xff;
		int green = (pixel >>  8) & 0xff;
		int blue  = (pixel      ) & 0xff;
		
		int alpha2 = (pixel2 >> 24) & 0xff;
		int red2   = (pixel2 >> 16) & 0xff;
		int green2 = (pixel2 >>  8) & 0xff;
		int blue2  = (pixel2      ) & 0xff;
		
		// Deal with the pixel as necessary...
		
		// Zero the left two bits
		red &= 0xfc;
		green &= 0xfc;
		blue &= 0xfc;
		
		// Shift out the right 6 bits (leaving the two highest order)
		red2 = red2 >> 6;
		green2 = green2 >> 6;
		blue2 = blue2 >> 6;
		
		// Add them
		red+=red2;
		green+=green2;
		blue+=blue2;
		bufimg.setRGB(x, y, (red << 16) | (green << 8) | (blue));
	}
	
	protected static void handlesinglepixelE2(int x, int y, int pixel, byte[] letter, int byteoffset, boolean donereading, BufferedImage bufimg) {
		@SuppressWarnings("unused")
		int alpha = (pixel >> 24) & 0xff;
		int red   = (pixel >> 16) & 0xff;
		int green = (pixel >>  8) & 0xff;
		int blue  = (pixel      ) & 0xff;
		
		if(!donereading){
			int red2   = letter[byteoffset/8] >> (6 - (byteoffset % 8)); 
			int green2 = letter[(byteoffset+2)/8] >> (6 - ((byteoffset + 2) % 8));
			int blue2  = letter[(byteoffset+4)/8] >> (6 - ((byteoffset + 4) % 8));
			
			// Deal with the pixel as necessary...
			
			// Zero the left two bits
			red &= 0xfc;
			green &= 0xfc;
			blue &= 0xfc;
			
			// Make sure the character->pixel is only two bits/color
			red2 = red2 & 3;
			green2 = green2 & 3;
			blue2 = blue2 & 3;
			// Add them
			red+=red2;
			green+=green2;
			blue+=blue2;
		}
		bufimg.setRGB(x, y, (red << 16) | (green << 8) | (blue));
	}
	
	
	public static void handlepixels(Image img, int x, int y, int w, int h, Image img2,  int w2, int h2, BufferedImage bufimg) {
		int[] pixels = new int[w * h];
		int[] pixels2= new int[w2 * h2];
		PixelGrabber pg = new PixelGrabber(img, x, y, w, h, pixels, 0, w);
		PixelGrabber pg2 = new PixelGrabber(img2, 0, 0, w2, h2, pixels2, 0, w2);
		try {
			pg.grabPixels();
			pg2.grabPixels();
		} catch (InterruptedException e) {
			System.err.println("interrupted waiting for pixels!");
			return;
		}
		if ((pg.getStatus() & pg2.getStatus() & ImageObserver.ABORT) != 0) {
			System.err.println("image fetch aborted or errored");
			return;
		}
		if ((h2 != h) || (w2 != w)) {
			for (int j = 0; j < h; j++) {
				for (int i = 0; i < w; i++) {
					handlesinglepixelE1(x+i, y+j, pixels[j * w + i], pixels[j * w2 + i],  bufimg);
				}
			}
		}
		for (int j = 0; j < h && j < h2; j++) {
			for (int i = 0; i < w && i< w2; i++) {
				handlesinglepixelE1(x+i, y+j, pixels[j * w + i], pixels2[j * w2 + i],  bufimg);
			}
		}
		//return bufimg;
	}
	
	public static void handleletters(Image img, int x, int y, int w, int h, FileInputStream hide, int size, BufferedImage bufimg) {
		int[] pixels = new int[w * h];
		byte[] letter = new byte[3];//holds character data
			
			letter[0] = (byte)((size) & 0xff);
			letter[1] = (byte)((size >> 8) & 0xff);
			letter[2] = (byte)((size >> 16) & 0xff);
			
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
			boolean donereading = false;
			int byteoffset=0;
			for (int j = 0; j < h; j++) {
				for (int i = 0; i < w; i++) {
					if(byteoffset==0 && (!(i==0 && j==0)))
					{
						try{
							if(hide.read(letter)==-1)
								donereading=true;
							
						}
						catch(java.io.IOException e)
					{
							System.err.println(e);
							return;
					}
					}
					handlesinglepixelE2(x+i, y+j, pixels[j * w + i],letter,byteoffset, donereading,  bufimg);
					if(byteoffset==18)//only go up to 3 bytes (18+6 bits used in hspE2)
						byteoffset=0;
					else
						byteoffset+=6;
				}
			}
	}
	
	
	public static BufferedImage encode(ImageIcon ic, ImageIcon hide) {
		Image img = ic.getImage();
		int wid=ic.getIconWidth();
		int hei=ic.getIconHeight();
		Image img2 = hide.getImage();
		int wid2=hide.getIconWidth();
		int hei2=hide.getIconHeight();
		BufferedImage newimage = new BufferedImage(wid,hei,BufferedImage.TYPE_INT_RGB);
		handlepixels(img,0,0,wid,hei, img2, wid2, hei2, newimage);
		return newimage;
	}
	
	public static BufferedImage encode(ImageIcon ic, File hide) throws java.io.IOException { //txt or binary
		Image img = ic.getImage();
		int wid=ic.getIconWidth();
		int hei=ic.getIconHeight();
		FileInputStream hidestream = new FileInputStream(hide);
		int size = (int)hide.length();
		BufferedImage newimage = new BufferedImage(wid,hei,BufferedImage.TYPE_INT_RGB);
		handleletters(img, 0, 0, wid, hei, hidestream, size, newimage);
		return newimage;
	}
	
	public static void EncodeImage(String[] args) {
		ImageIcon ic = new ImageIcon(args[0]);
		ImageIcon hide = new ImageIcon(args[1]);
		Image img = ic.getImage();
		Image img2 = hide.getImage();
		int wid=ic.getIconWidth();
		int hei=ic.getIconHeight();
		int wid2=hide.getIconWidth();
		int hei2=hide.getIconHeight();
		BufferedImage newimage = new BufferedImage(wid,hei,BufferedImage.TYPE_INT_RGB);
		handlepixels(img,0,0,wid,hei, img2, wid2, hei2, newimage);
		
		int ld = args[2].lastIndexOf('.');
		String extension = (ld < 0) ? ("png") : (args[2].substring(ld + 1));
		File f = new File(args[2]);
		try{
			ImageIO.write(newimage,extension, f);;
		}
		catch(java.io.IOException e){
			System.err.println(e);
		}
	}
	
	public static void EncodeText(String[] args) {
		ImageIcon ic = new ImageIcon(args[0]);
		Image img = ic.getImage();
		int wid = ic.getIconWidth();
		int hei = ic.getIconHeight();
		BufferedImage newimage = new BufferedImage(wid, hei, BufferedImage.TYPE_INT_RGB);
		try{
			FileInputStream hide = new FileInputStream(args[1]);
			File file = new File(args[1]);
			handleletters(img,0,0,wid,hei,hide,(int)file.length(),newimage);
		} catch (java.io.FileNotFoundException e) {
			System.err.println(e); return;
		}
		
		int ld = args[2].lastIndexOf('.');
		String extension = (ld < 0) ? ("png") : (args[2].substring(ld + 1));
		File f = new File(args[2]);
		try{
			ImageIO.write(newimage,extension, f);;
		}
		catch(java.io.IOException e){
			System.err.println(e);
		}
		
	}
	
	public static void main(String[] args){
		if(args.length<3)
		{
			System.err.println("Insufficient number of arguments.");
			System.err.println("Format is: \"java Encoder <Observable Filename> <Encoded Filename> <New Filename> <optional encoding algorithm>.\"");
			return;
		}
		try{
			if(args[3].equals("1"))
			{
				EncodeText(args);//alternate encoding algorithm 1 called.
			}
			else if(args[3].equals("0"))//default encoding algorithm 0 called explicitly.
			{
				EncodeImage(args);
			}
		}
		catch(java.lang.ArrayIndexOutOfBoundsException e) {
			EncodeImage(args);//default action: encode an image
		}
	}	
}
