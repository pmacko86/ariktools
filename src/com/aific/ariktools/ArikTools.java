package com.aific.ariktools;

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


import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import com.aific.ariktools.analysis.ColorAnalysis;
import com.aific.ariktools.analysis.FridrichGroupAnalysis;
import com.aific.ariktools.analysis.NoiseTest;
import com.aific.ariktools.analysis.RandomnessTest;
import com.aific.ariktools.analysis.SamplePairAnalysis;
import com.aific.ariktools.gui.MainFrame;
import com.aific.ariktools.stego.AdvDecoder;
import com.aific.ariktools.stego.AdvEncoder;
import com.aific.ariktools.util.HeadlessTask;
import com.aific.ariktools.util.Utils;

public class ArikTools {
	
	
    public static void main (String args[]) {
		
		
		// Set-up platform-specific properties
		
		try {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
		}
		catch (Exception e) {
			// do a silent failover
		}
		
		
		// Start in a GUI mode if no command line arguments are passed
		
		if (args.length == 0) {
			MainFrame app = new MainFrame();
			app.run();
			return;
		}
		
		
		// Start processing a command
		
		System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_BIG_HEADER) + "ArikTools by (c) Arik Z. Lakritz and Peter Macko"
						   + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL));

		int args_start = 1;
		
		String command = args[0];
		
		
		// Process commands - help
		
		if (command.equals("help")) {
			if (args.length <= args_start) {
				System.out.println();

				System.out.println(Utils.rreimanndelta(0.05));
				System.out.println(Utils.lreimanndelta(0.05));
				System.out.println(Utils.rrchi2p(2,0.5));
				System.out.println("moo");
	
				
				System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
								   + " java -jar ArikTools.jar [command] [arguments]");
				System.out.println();
				System.out.println("If no commands or arguments are specified, the program starts in a GUI mode.");
				System.out.println("Use \"java -jar ArikTools.jar help [command]\" to get help about a particular command.");
				System.out.println();
				System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Available commands:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL));
				System.out.println("  analysis                      - perform complete stego-analysis");
				System.out.println("  color-analysis                - analyze the color structure");
				System.out.println("  decode                        - decode XLSB-2 encoded secrets");
				System.out.println("  encode                        - encode a secret using XLSB-2");
				System.out.println("  fridrich-group-analysis       - analyze Fridrich\'s groups of pixels");
				System.out.println("  help                          - this help page");
				System.out.println("  intensity-bound-stats         - count pixels near the intensity bounds");
				System.out.println("  list-files                    - just list the files");
				System.out.println("  noise-analysis                - analyze the Gaussian noise");
				System.out.println("  psnr                          - calculate the peak signal-to-noise ratio");
				System.out.println("  random-alsb1-transform        - perform a random ALSB-1 transform");
				System.out.println("  random-alsb2-transform        - perform a random ALSB-2 transform");
				System.out.println("  random-arithmetic-transform   - perform a random arithmetic transform");
				System.out.println("  random-xlsb1-transform        - perform a random XLSB-1 transform");
				System.out.println("  random-xlsb2-transform        - perform a random XLSB-2 transform");
				System.out.println("  randomness-analysis           - analyze the randomness of low order bits");
				System.out.println("  sample-pair-analysis          - analyze adjacent pairs of pixels");
				System.out.println("  to-grayscale                  - convert the image to grayscale");
			}
			else {
				for (int i = args_start; i < args.length; i++) {
					
					if (args[i].equals("analysis")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar analysis [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Runs the stego-analysis on the given images. The analysis consists of Lee\'s Color");
						System.out.println("Cube Analysis, Dumitrescu\'s Sample Pair Analysis, and Fridrich\'s Group Analysis.");
						System.out.println();
						System.out.println("The summary of the results will be saved as an HTML file in the current directory. The");
						System.out.println("sub-results will be saved as HTML files in the subdirectory \"reports\". Existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("color-analysis")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar color-analysis [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Analyze the color structure of the given images.");
						System.out.println();
						System.out.println("The results will be saved as HTML files in the subdirectory \"reports\"; existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("decode")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar decode [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Decode XLSB-2 encoded secrets from the given images.");
						System.out.println();
						System.out.println("The results will be saved as binary files in the subdirectory \"reports\"; existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("encode")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar encode [secret] [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Use XLSB-2 to encode the secret to the given image files.");
						System.out.println();
						System.out.println("The results will be saved as PNG files in the subdirectory \"reports\"; existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("fridrich-group-analysis")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar fridrich-group-analysis [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Run the sample pair analysis on Fridrich\'s adjacent groups of pixels of the given images.");
						System.out.println();
						System.out.println("The results will be saved as HTML files in the subdirectory \"reports\"; existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("help")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar ArikTools.jar help [command]");
						System.out.println();
						System.out.println("Displays a help about the given command.");
						continue;
					}
					
					if (args[i].equals("intensity-bound-stats")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar intensity-bound-stats [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Calculate the percentage of sufficiently random pixels with intensities close to");
						System.out.println("their respective bounds.");
						System.out.println();
						System.out.println("The results will be saved as an HTML file in the current directory; existing file");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("list-files")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar list-files [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Just lists the input files.");
						continue;
					}					
					
					if (args[i].equals("noise-analysis")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar noise-analysis [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Analyze the Gaussian noise of the given images.");
						System.out.println();
						System.out.println("The results will be saved as PNG files in the subdirectory \"reports\"; existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("psnr")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar psnr [original] [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Calculate the peak signal-to-noise ratio of the original image and the derived images.");
						System.out.println();
						System.out.println("The image files can be specified either directly or using regular expressions;");
						System.out.println("shell patters (e.g. \"*.png\") are not supported. The results are outputted to");
						System.out.println("the terminal.");
						continue;
					}

					if (args[i].equals("random-alsb1-transform")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar random-alsb1-transform [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Uses the ALSB-1 algorithm to embed 0 or 1 to all pixels in sufficiently noisy areas.");
						System.out.println();
						System.out.println("The results will be saved as PNG files in the subdirectory \"reports\"; existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("random-alsb2-transform")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar random-alsb2-transform [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Uses the ALSB-2 algorithm to embed 0, 1, 2, or 3 to all pixels in sufficiently noisy areas.");
						System.out.println();
						System.out.println("The results will be saved as PNG files in the subdirectory \"reports\"; existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("random-arithmetic-transform")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar random-arithmetic-transform [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Add a random number between -2 and 2 to all pixels in sufficiently noisy areas.");
						System.out.println();
						System.out.println("The results will be saved as PNG files in the subdirectory \"reports\"; existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("random-xlsb1-transform")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar random-xlsb1-transform [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Uses the XLSB-1 algorithm to embed 0 or 1 to all pixels in sufficiently noisy areas.");
						System.out.println();
						System.out.println("The results will be saved as PNG files in the subdirectory \"reports\"; existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("random-xlsb2-transform")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar random-xlsb2-transform [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Uses the XLSB-2 algorithm to embed 0, 1, 2, or 3 to all pixels in sufficiently noisy areas.");
						System.out.println();
						System.out.println("The results will be saved as PNG files in the subdirectory \"reports\"; existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("randomness-analysis")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar randomness-analysis [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Analyze the randomness of low order bits of the given images.");
						System.out.println();
						System.out.println("The results will be saved as PNG files in the subdirectory \"reports\"; existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("sample-pair-analysis")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar sample-pair-analysis [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Run the sample pair analysis on the adjacent pixels of the given images.");
						System.out.println();
						System.out.println("The results will be saved as HTML files in the subdirectory \"reports\"; existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					if (args[i].equals("to-grayscale")) {
						System.out.println();
						System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_HEADER) + "Usage:" + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL)
										   + " java -jar ArikTools.jar to-grayscale [image 1] [image 2] ... [image N]");
						System.out.println();
						System.out.println("Convert the images to grayscale.");
						System.out.println();
						System.out.println("The results will be saved as PNG files in the subdirectory \"reports\"; existing files");
						System.out.println("will be overwritten without confirmation. The image files can be specified either directly");
						System.out.println("or using regular expressions; shell patters (e.g. \"*.png\") are not supported.");
						continue;
					}
					
					System.out.println();
					System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_ERROR) + "Unrecognized command: " + args[i] + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL));
				}
			}
			return;
		}

		System.out.println();
		
		
		// Process commands - color-analysis
		
		if (command.equals("color-analysis")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools Color Structure Analysis", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					
					ColorAnalysis ca = new ColorAnalysis(icon);
					ca.analyze();
					String report = ca.generateReport(file.getName());
					
					Utils.saveStringFile((new File(reports, file.getName() + "_color_report.html")).getAbsolutePath(), report);
				}
				
			});
			return;
		}
		
		
		// Process commands - randomness-analysis
		
		if (command.equals("randomness-analysis")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools LSB Randomness Analysis", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					
					float[][] r = RandomnessTest.test(icon);
					BufferedImage rimg = RandomnessTest.plotScores(r);
					
					Utils.saveImage((new File(reports, file.getName() + "_randomness_map.png")).getAbsolutePath(), rimg);
				}
				
			});
			return;
		}
		
		
		// Process commands - noise-analysis
		
		if (command.equals("noise-analysis")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools Gaussian Noise Analysis", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					
					float[][] r = (new NoiseTest()).test(icon);
					BufferedImage rimg = NoiseTest.plotScores(r);
					
					Utils.saveImage((new File(reports, file.getName() + "_noise_map.png")).getAbsolutePath(), rimg);
				}
				
			});
			return;
		}
		
		
		// Process commands - sample-pair-analysis
		
		if (command.equals("sample-pair-analysis")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools Sample Pair Analysis", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					
					SamplePairAnalysis spa = new SamplePairAnalysis(icon);
					spa.analyze();
					String report = spa.generateReport(file.getName());
					
					Utils.saveStringFile((new File(reports, file.getName() + "_sample_pair_report.html")).getAbsolutePath(), report);
				}
				
			});
			return;
		}
		
		
		// Process commands - fridrich-group-analysis
		
		if (command.equals("fridrich-group-analysis")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools Fridrich\'s Group Analysis", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					
					FridrichGroupAnalysis fga = new FridrichGroupAnalysis(icon);
					fga.analyze();
					String report = fga.generateReport(file.getName());
					
					Utils.saveStringFile((new File(reports, file.getName() + "_fridrich_group_report.html")).getAbsolutePath(), report);
				}
				
			});
			return;
		}
		
		
		// Process commands - analysis
		
		if (command.equals("analysis")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			
			final LinkedList<String> results = new LinkedList<String>();
			
			Utils.performHeadless("ArikTools Analysis", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					
					String lineReport = "<!-- " + file.getName() + " -->\n";
					lineReport += "<tr>\n  <td><b><a href=\"" + file.getName() + "\">" + file.getName() + "</a></b>&nbsp;</td>\n";
					lineReport += "  <td>&nbsp;" + icon.getIconWidth() + "&nbsp;x&nbsp;" + icon.getIconHeight() + "&nbsp;</td>\n";
					
					String report;
					
					// Color Cube Analysis
					
					ColorAnalysis ca = new ColorAnalysis(icon);
					ca.analyze();
					report = ca.generateReport(file.getName());
					
					Utils.saveStringFile((new File(reports, file.getName() + "_color_report.html")).getAbsolutePath(), report);
					
					lineReport += ca.generateLineReport();
					
					// Sample Pair Analysis
					
					SamplePairAnalysis spa = new SamplePairAnalysis(icon);
					spa.analyze();
					report = spa.generateReport(file.getName());
					
					Utils.saveStringFile((new File(reports, file.getName() + "_sample_pair_report.html")).getAbsolutePath(), report);
					
					lineReport += spa.generateLineReport();
					
					// Fridrich's Group Analysis
					
					FridrichGroupAnalysis fga = new FridrichGroupAnalysis(icon);
					fga.analyze();
					report = fga.generateReport(file.getName());
					
					Utils.saveStringFile((new File(reports, file.getName() + "_fridrich_group_report.html")).getAbsolutePath(), report);
					
					lineReport += fga.generateLineReport();
					
					// Save the line report
					
					results.add(lineReport + "</tr>\n");
				}
				
			});
			
			if (results.isEmpty()) return;
			
			try {
				Collections.sort(results);
			}
			catch (Throwable e) {
			}
			
			// Generate the report
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("<html>\n");
			sb.append("<head><title>ArikTools Steganalysis Report</title></head>\n");
			sb.append("<body><center>\n");
			
			sb.append("<h1>ArikTools Steganalysis Report</h1>\n");
			sb.append("<p></p>\n\n");
			
			sb.append("<table>\n\n");
			
			sb.append("<tr>\n");
			sb.append("  <td colspan=\"2\"></td>");
			sb.append("  <td align=\"center\" colspan=\"" + ColorAnalysis.getLineReportColumns() + "\"><b>Color Cube Analysis</b></td>\n");
			sb.append("  <td align=\"center\" colspan=\"" + SamplePairAnalysis.getLineReportColumns() + "\"><b>Sample Pair Analysis</b></td>\n");
			sb.append("  <td align=\"center\" colspan=\"" + FridrichGroupAnalysis.getLineReportColumns() + "\"><b>Fridrich\'s Group Analysis</b></td>\n");
			sb.append("</tr>\n\n");
			
			sb.append("<tr>\n");
			sb.append("  <td><b>File Name</b></td>\n");
			sb.append("  <td align=\"center\"><b>Size</b></td>\n");
			sb.append(ColorAnalysis.generateLineReportHeader());
			sb.append(SamplePairAnalysis.generateLineReportHeader());
			sb.append(FridrichGroupAnalysis.generateLineReportHeader());
			sb.append("</tr>\n\n");
			
			for (Iterator<String> i = results.iterator(); i.hasNext(); ) {
				Object o = i.next();
				
				sb.append(o.toString());
				sb.append("\n");
			}
			
			sb.append("</table>\n\n");
			sb.append("</center></body></html>\n");
			
			Utils.saveStringFile((new File("analysis_report.html")).getAbsolutePath(), sb.toString());
			return;
		}
		
		
		// Process commands - random-arithmetic-transform
		
		if (command.equals("random-arithmetic-transform")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools Random Arithmetic Transform", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					
					float[][] rnd = RandomnessTest.test(icon);
					int width = icon.getIconWidth();
					int height = icon.getIconHeight();
					BufferedImage rimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					int[] rgb = Utils.grabRGB(icon);
					int cnt = 0, gcnt = 0;
					for (int y = 0, p = 0; y < height; y++) {
						for (int x = 0; x < width; x++, p++) {
							int c = rgb[p];
							int r = (c >> 16) & 0xff;
							int g = (c >>  8) & 0xff;
							int b =  c        & 0xff;
							if (rnd[x][y] < 0.04) {
								r += Math.round(Math.random() * 5) - 2;
								g += Math.round(Math.random() * 5) - 2;
								b += Math.round(Math.random() * 5) - 2;
								cnt++;
							}
							gcnt++;
							if (r < 0) r = 0; if (g < 0) g = 0; if (b < 0) b = 0;
							if (r > 0xff) r = 0xff; if (g > 0xff) g = 0xff; if (b > 0xff) b = 0xff;
							rimg.setRGB(x, y, (r << 16) | (g << 8) | b);
						}
					}
					
					System.out.print(" (" + cnt + "/" + gcnt + ")");
					
					Utils.saveImage((new File(reports, file.getName() + "_arithmetic_transform.png")).getAbsolutePath(), rimg);
				}
				
			});
			return;
		}
		
		
		// Process commands - random-xlsb1-transform
		
		if (command.equals("random-xlsb1-transform")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools Random XLSB-1 Transform", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					
					float[][] rnd = RandomnessTest.test(icon);
					int width = icon.getIconWidth();
					int height = icon.getIconHeight();
					BufferedImage rimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					int[] rgb = Utils.grabRGB(icon);
					int cnt = 0, gcnt = 0;
					for (int y = 0, p = 0; y < height; y++) {
						for (int x = 0; x < width; x++, p++) {
							int c = rgb[p];
							int r = (c >> 16) & 0xff;
							int g = (c >>  8) & 0xff;
							int b =  c        & 0xff;
							if (rnd[x][y] < 0.04) {
								
								if (Math.random() > 0.5) {		// 1
									if ((r & 1) == 0) {
										r++;
									}
								}
								else {
									if ((r & 1) == 1) {
										if (r == 0xff) {
											r--;
										}
										else {
											r++;
										}
									}
								}
								
								if (Math.random() > 0.5) {		// 1
									if ((g & 1) == 0) {
										g++;
									}
								}
								else {
									if ((g & 1) == 1) {
										if (g == 0xff) {
											g--;
										}
										else {
											g++;
										}
									}
								}
								
								if (Math.random() > 0.5) {		// 1
									if ((b & 1) == 0) {
										b++;
									}
								}
								else {
									if ((b & 1) == 1) {
										if (b == 0xff) {
											b--;
										}
										else {
											b++;
										}
									}
								}
								
								cnt++;
							}
							gcnt++;
							if (r < 0) r = 0; if (g < 0) g = 0; if (b < 0) b = 0;
							if (r > 0xff) r = 0xff; if (g > 0xff) g = 0xff; if (b > 0xff) b = 0xff;
							rimg.setRGB(x, y, (r << 16) | (g << 8) | b);
						}
					}
					
					System.out.print(" (" + cnt + "/" + gcnt + ")");
					
					Utils.saveImage((new File(reports, file.getName() + "_xlsb1_transform.png")).getAbsolutePath(), rimg);
				}
				
			});
			return;
		}
		
		
		// Process commands - random-xlsb2-transform
		
		if (command.equals("random-xlsb2-transform")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools Random XLSB-2 Transform", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					
					float[][] rnd = RandomnessTest.test(icon);
					int width = icon.getIconWidth();
					int height = icon.getIconHeight();
					BufferedImage rimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					int[] rgb = Utils.grabRGB(icon);
					int cnt = 0, gcnt = 0;
					for (int y = 0, p = 0; y < height; y++) {
						for (int x = 0; x < width; x++, p++) {
							int c = rgb[p];
							int r = (c >> 16) & 0xff;
							int g = (c >>  8) & 0xff;
							int b =  c        & 0xff;
							if (rnd[x][y] < 0.04) {
								
								int z;
								
								z = (int)(Math.random() * 4);
								while ((g & 3) != z) g++;
								if (g > 0xff) g -= 4;
								
								z = (int)(Math.random() * 4);
								while ((r & 3) != z) r++;
								if (r > 0xff) r -= 4;
								
								z = (int)(Math.random() * 4);
								while ((b & 3) != z) b++;
								if (b > 0xff) b -= 4;
								
								cnt++;
							}
							gcnt++;
							if (r < 0) r = 0; if (g < 0) g = 0; if (b < 0) b = 0;
							if (r > 0xff) r = 0xff; if (g > 0xff) g = 0xff; if (b > 0xff) b = 0xff;
							rimg.setRGB(x, y, (r << 16) | (g << 8) | b);
						}
					}
					
					System.out.print(" (" + cnt + "/" + gcnt + ")");
					
					Utils.saveImage((new File(reports, file.getName() + "_xlsb2_transform.png")).getAbsolutePath(), rimg);
				}
				
			});
			return;
		}
		
		
		// Process commands - random-alsb1-transform
		
		if (command.equals("random-alsb1-transform")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools Random ALSB-1 Transform", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					
					float[][] rnd = RandomnessTest.test(icon);
					int width = icon.getIconWidth();
					int height = icon.getIconHeight();
					BufferedImage rimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					int[] rgb = Utils.grabRGB(icon);
					int cnt = 0, gcnt = 0;
					for (int y = 0, p = 0; y < height; y++) {
						for (int x = 0; x < width; x++, p++) {
							int c = rgb[p];
							int r = (c >> 16) & 0xff;
							int g = (c >>  8) & 0xff;
							int b =  c        & 0xff;
							if (rnd[x][y] < 0.04) {
								
								if (Math.random() > 0.5) r ^= 1;
								if (Math.random() > 0.5) g ^= 1;
								if (Math.random() > 0.5) b ^= 1;
								
								cnt++;
							}
							gcnt++;
							if (r < 0) r = 0; if (g < 0) g = 0; if (b < 0) b = 0;
							if (r > 0xff) r = 0xff; if (g > 0xff) g = 0xff; if (b > 0xff) b = 0xff;
							rimg.setRGB(x, y, (r << 16) | (g << 8) | b);
						}
					}
					
					System.out.print(" (" + cnt + "/" + gcnt + ")");
					
					Utils.saveImage((new File(reports, file.getName() + "_alsb1_transform.png")).getAbsolutePath(), rimg);
				}
				
			});
			return;
		}
		
		
		// Process commands - random-alsb2-transform
		
		if (command.equals("random-alsb2-transform")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools Random ALSB-2 Transform", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					
					float[][] rnd = RandomnessTest.test(icon);
					int width = icon.getIconWidth();
					int height = icon.getIconHeight();
					BufferedImage rimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					int[] rgb = Utils.grabRGB(icon);
					int cnt = 0, gcnt = 0;
					for (int y = 0, p = 0; y < height; y++) {
						for (int x = 0; x < width; x++, p++) {
							int c = rgb[p];
							int r = (c >> 16) & 0xff;
							int g = (c >>  8) & 0xff;
							int b =  c        & 0xff;
							if (rnd[x][y] < 0.04) {
								
								int z;
								
								z = (int)(Math.random() * 4);
								r = (r & 0xfc0) | z;
								
								z = (int)(Math.random() * 4);
								g = (g & 0xfc0) | z;
								
								z = (int)(Math.random() * 4);
								b = (b & 0xfc0) | z;
								
								cnt++;
							}
							gcnt++;
							rimg.setRGB(x, y, (r << 16) | (g << 8) | b);
						}
					}
					
					System.out.print(" (" + cnt + "/" + gcnt + ")");
					
					Utils.saveImage((new File(reports, file.getName() + "_alsb2_transform.png")).getAbsolutePath(), rimg);
				}
				
			});
			return;
		}		
		
		
		// Process commands - intensity-bound-stats
		
		if (command.equals("intensity-bound-stats")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			
			final LinkedList<String> results = new LinkedList<String>();

			Utils.performHeadless("ArikTools Pixel Boundary Intensity Statistics", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					
					String lineReport = "<!-- " + file.getName() + " -->\n";
					lineReport += "<tr>\n  <td><b><a href=\"" + file.getName() + "\">" + file.getName() + "</a></b>&nbsp;</td>\n";
					lineReport += "  <td>&nbsp;" + icon.getIconWidth() + "&nbsp;x&nbsp;" + icon.getIconHeight() + "&nbsp;</td>\n";
					
					float[][] rnd = RandomnessTest.test(icon);
					int width = icon.getIconWidth();
					int height = icon.getIconHeight();
					int[] rgb = Utils.grabRGB(icon);
					
					int[] count_r = new int[16];
					int[] count_g = new int[16];
					int[] count_b = new int[16];
					int[] count_y = new int[16];
					
					for (int i = 0; i < 16; i++) {
						count_r[i] = 0;
						count_g[i] = 0;
						count_b[i] = 0;
						count_y[i] = 0;
					}
					
					int total = 0;

					for (int y = 0, p = 0; y < height; y++) {
						for (int x = 0; x < width; x++, p++) {

							if (rnd[x][y] < 0.04) {
								
								int c = rgb[p];
								int r = (c >> 16) & 0xff;
								int g = (c >>  8) & 0xff;
								int b =  c        & 0xff;
								int Y = Math.min(255, (int) Math.round(0.2989 * r + 0.5866 * g + 0.1145 * b));
								
								if (r <       8) count_r[          r]++;
								if (r > 255 - 8) count_r[8 + 255 - r]++;
								if (g <       8) count_g[          g]++;
								if (g > 255 - 8) count_g[8 + 255 - g]++;
								if (b <       8) count_b[          b]++;
								if (b > 255 - 8) count_b[8 + 255 - b]++;
								if (Y <       8) count_y[          Y]++;
								if (Y > 255 - 8) count_y[8 + 255 - Y]++;
								
								total++;
							}
						}
					}
					
					if (total == 0) return;

					lineReport += "  <td>&nbsp;" + (Math.round(10000.0 * total / (double) (width * height)) / 100.0) + "&nbsp;%</td>\n";
					
					lineReport += "  ";
					for (int i = 0; i < 16; i++) {
						lineReport += "<td>";
						if ((i < 4) || ((i >= 8) && (i < 12))) lineReport += "<b>";
						lineReport += "&nbsp;" + (Math.round(10000.0 * count_r[i] / (double) total) / 100.0) + "&nbsp;%";
						if ((i < 4) || ((i >= 8) && (i < 12))) lineReport += "</b>";
						lineReport += "</td>";
					}
					lineReport += "\n";
					
					lineReport += "  ";
					for (int i = 0; i < 16; i++) {
						lineReport += "<td>";
						if ((i < 4) || ((i >= 8) && (i < 12))) lineReport += "<b>";
						lineReport += "&nbsp;" + (Math.round(10000.0 * count_g[i] / (double) total) / 100.0) + "&nbsp;%";
						if ((i < 4) || ((i >= 8) && (i < 12))) lineReport += "</b>";
						lineReport += "</td>";
					}
					lineReport += "\n";
					
					lineReport += "  ";
					for (int i = 0; i < 16; i++) {
						lineReport += "<td>";
						if ((i < 4) || ((i >= 8) && (i < 12))) lineReport += "<b>";
						lineReport += "&nbsp;" + (Math.round(10000.0 * count_b[i] / (double) total) / 100.0) + "&nbsp;%";
						if ((i < 4) || ((i >= 8) && (i < 12))) lineReport += "</b>";
						lineReport += "</td>";
					}
					lineReport += "\n";
					
					lineReport += "  ";
					for (int i = 0; i < 16; i++) {
						lineReport += "<td>";
						if ((i < 4) || ((i >= 8) && (i < 12))) lineReport += "<b>";
						lineReport += "&nbsp;" + (Math.round(10000.0 * count_y[i] / (double) total) / 100.0)+ "&nbsp;%";
						if ((i < 4) || ((i >= 8) && (i < 12))) lineReport += "</b>";
						lineReport += "</td>";
					}
					lineReport += "\n";

					lineReport += "</tr>\n";
					
					results.add(lineReport);
				}
				
			});
			
			if (results.isEmpty()) return;
			
			try {
				Collections.sort(results);
			}
			catch (Throwable e) {
			}
			
			// Generate the report
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("<html>\n");
			sb.append("<head><title>ArikTools Pixel Intensity Boundary Statistics</title></head>\n");
			sb.append("<body><center>\n");
			
			sb.append("<h1>ArikTools Pixel Intensity Boundary Statistics</h1>\n");
			sb.append("<p></p>\n\n");
			sb.append("<p>(considering pixels with regularity 0.04 or smaller)</p>\n\n");
			sb.append("<p></p>\n\n");
			
			sb.append("<table>\n\n");
			
			sb.append("<tr>\n");
			sb.append("  <td colspan=\"3\"></td>");
			sb.append("  <td align=\"center\" colspan=\"16\"><b>Red</b></td>\n");
			sb.append("  <td align=\"center\" colspan=\"16\"><b>Green</b></td>\n");
			sb.append("  <td align=\"center\" colspan=\"16\"><b>Blue</b></td>\n");
			sb.append("  <td align=\"center\" colspan=\"16\"><b>Monochrome</b></td>\n");
			sb.append("</tr>\n\n");
			
			sb.append("<tr>\n");
			sb.append("  <td><b>File Name</b></td>\n");
			sb.append("  <td align=\"center\"><b>Size</b></td>\n");
			sb.append("  <td align=\"center\"><b>Coverage</b></td>\n");
			for (int j = 0; j < 4; j++) {
				for (int i =   0; i <       8; i++) sb.append("  <td align=\"center\"><b>" + i + "</b></td>\n");
				for (int i = 255; i > 255 - 8; i--) sb.append("  <td align=\"center\"><b>" + i + "</b></td>\n");
			}
			sb.append("</tr>\n\n");
			
			for (Iterator<String> i = results.iterator(); i.hasNext(); ) {
				Object o = i.next();
				
				sb.append(o.toString());
				sb.append("\n");
			}
			
			sb.append("</table>\n\n");
			sb.append("</center></body></html>\n");
			
			Utils.saveStringFile((new File("bound_intensity_report.html")).getAbsolutePath(), sb.toString());
			return;
		}
		
		
		// Process commands - list-files
		
		if (command.equals("list-files")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools File Listing", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
				}
				
			});
			return;
		}
		
		
		// Process commands - encode
		
		if (command.equals("encode")) {
			String secretStr = args[Math.min(args.length - 1, args_start++)];
			final File secret = new File(secretStr);
			String[] files = new String[Math.max(args.length - args_start, 0)];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools XLSB-2 Encoding", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					AdvEncoder encoder = new AdvEncoder(icon, secret);
					BufferedImage rimg = encoder.encode();
					Utils.saveImage((new File(reports, file.getName() + "_" + secret.getName() + "_xlsb2.png")).getAbsolutePath(), rimg);
				}
				
			});
			return;
		}
		
		
		// Process commands - decode
		
		if (command.equals("decode")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools XLSB-2 Decoding", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					AdvDecoder decoder = new AdvDecoder(icon, new File(reports, file.getName() + ".decoded"));
					decoder.decode();
				}
				
			});
			return;
		}
		
		
		// Process commands - psnr
		
		if (command.equals("psnr")) {

			try {
				String originalStr = args[Math.min(args.length - 1, args_start++)];
				File originalFile = new File(originalStr);
				ImageIcon originalIcon = new ImageIcon(originalFile.getAbsolutePath());
				final int[] original = Utils.grabRGB(originalIcon);
				
				String[] files = new String[Math.max(args.length - args_start, 0)];
				for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
				
				Utils.performHeadless("ArikTools Peak Signal-to-Noise Ratio", files, new HeadlessTask() {
					
					public void performTask(File file, File reports) throws Exception {
						ImageIcon icon = new ImageIcon(file.getAbsolutePath());
						int[] rgb = Utils.grabRGB(icon);
						
						boolean same = original.length == rgb.length;
						if (same) {
							for (int i = 0; i < rgb.length; i++) {
								if (rgb[i] != original[i]) {
									same = false;
									break;
								}
							}
						}
						
						if (same) {
							System.out.print(" (identical)");
						}
						else {
							double psnr = Utils.RGB_PNSR(original, rgb);
							double psnr_rounded = Math.round(10.0 * psnr) / 10.0;
							System.out.print(" (" + psnr_rounded + " dB)");
						}
					}
					
				});
				return;
			}
			catch (Exception e) {
				System.err.println(Utils.escapeColor(Utils.STDOUT_COLOR_WARNING) + "  Error: " + e.getMessage()
								   + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL));
			}
		}		
		
		
		// Process commands - to-grayscale
		
		if (command.equals("to-grayscale")) {
			String[] files = new String[args.length - args_start];
			for (int i = args_start; i < args.length; i++) files[i - args_start] = args[i];
			Utils.performHeadless("ArikTools Conversion To Grayscale", files, new HeadlessTask() {
				
				public void performTask(File file, File reports) throws Exception {
					ImageIcon icon = new ImageIcon(file.getAbsolutePath());
					int[] rgb = Utils.grabRGB(icon);
					
					int width = icon.getIconWidth();
					int height = icon.getIconHeight();
					BufferedImage rimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
					
					for (int y = 0, p = 0; y < height; y++) {
						for (int x = 0; x < width; x++, p++) {
							int c = rgb[p];
							int r = (c >> 16) & 0xff;
							int g = (c >>  8) & 0xff;
							int b =  c        & 0xff;
							int Y = Math.min((int) Math.round(0.2989 * r + 0.5866 * g + 0.1145 * b), 255);
							rimg.setRGB(x, y, (Y << 16) | (Y << 8) | Y);
						}
					}
					
					Utils.saveImage((new File(reports, file.getName() + "_grayscale.png")).getAbsolutePath(), rimg);
				}
				
			});
			return;
		}
		
		// Invalid command
		
		System.out.println(Utils.escapeColor(Utils.STDOUT_COLOR_ERROR) + "Invalid ArikTools command: " + command + Utils.escapeColor(Utils.STDOUT_COLOR_NORMAL));
		System.out.println("Use \"java -jar ArikTools.jar help\" for the list of available commands");
    }
}
