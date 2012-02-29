package com.aific.ariktools.stream;

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


import java.io.FileInputStream;
import java.io.IOException;

public class FileBitStream extends BitStream {

	private int[] data;
	
	public FileBitStream(FileInputStream fin, int bytes) throws IOException {
		super();
		this.size = bytes * 8;
		data = new int[bytes];
		for (int i = 0; i < bytes; i++) {
			int b = fin.read();
			if (b < 0) {
				size = i * 8;
				break;
			}
			data[i] = b;
		}
	}
	
	protected byte get(int index) {
		return (byte)((data[index / 8] >> (index % 8)) & 1);
	}
}
