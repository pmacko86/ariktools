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


import java.util.NoSuchElementException;

public abstract class BitStream {

	protected int start;
	protected int limit;
	protected int offset;
	protected int size;
	
	public BitStream() {
		start = limit = offset = size = 0;
	}
	
	public byte next() {
		if (!hasNext()) throw new NoSuchElementException();
		return get(start + offset++);
	}
	
	public boolean hasNext() {
		return offset < limit;
	}
	
	protected abstract byte get(int index);
	
	public int size() {
		return size;
	}
	
	public int getSize() {
		return size;
	}
	
	public int getLimit() {
		return limit;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public int getStart() {
		return start;
	}
	
	public void reset() {
		offset = 0;
	}
	
	public void setStart(int start) {
		if (start + limit > size) throw new IllegalArgumentException("The start offset is too big");
		this.start = start;
	}
	
	public void setLimit(int limit) {
		if (start + limit > size) throw new IllegalArgumentException("The limit is too big");
		this.limit = limit;
	}
	
	public void setOffset(int offset) {
		if (offset >= limit) throw new IllegalArgumentException("The offset is out of range");
		this.offset = offset;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer((int)Math.round(1.2 * size));
		for (int i = 0; i < limit; i++) {
			if ((i % 64 == 0) && (i != 0)) sb.append("\n");
			sb.append(get(start + i) == 0 ? "-" : "1");
		}
		return sb.toString();
	}
	
}
