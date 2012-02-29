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


public class XORBitStream extends BitStream {
	
	private BitStream stream1;
	private BitStream stream2;
	
	public XORBitStream(BitStream stream1, BitStream stream2) {
		super();
		this.stream1 = stream1;
		this.stream2 = stream2;
		this.size = Math.min(stream1.size(), stream2.size());
	}
	
	protected byte get(int index) {
		return (byte)(stream1.get(index) ^ stream2.get(index));
	}
}
