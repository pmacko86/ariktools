package com.aific.ariktools.gui;

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


import java.io.File;
import java.util.HashSet;
import javax.swing.filechooser.*;

import com.aific.ariktools.util.Utils;


/**
 * A file extension filter.
 *
 * @author Peter Macko
 * @version 1.00
 */
public class FileExtensionFilter extends FileFilter {
	
	private String name;
	private HashSet<String> accept;
	
	
    /**
	 * Create an empty file filter
	 *
	 * @param name the name of the filter
	 */
	public FileExtensionFilter(String name) {
		this.name = name;
		this.accept = new HashSet<String>();
	}
	
	
    /**
	 * Create a pre-initialized file filter
	 *
	 * @param name the name of the filter
	 * @param exts the accepted file name extensions
	 */
	public FileExtensionFilter(String name, String[] exts) {
		this(name);
		for (int i = 0; i < exts.length; i++) add(exts[i]);
	}
	
	
	/**
	 * Add a supported file extension
	 *
	 * @param ext the extension to add
	 */
	public void add(String ext) {
		accept.add(ext.toLowerCase());
	}
	
	
	/**
	 * Determine whether the given file should be accepted by the filter
	 *
	 * @param f the file
	 * @return true if the file was accepted by the filter
	 */
    public boolean accept(File f) {
		
        if (f.isDirectory()) return true;
		
        String extension = Utils.getExtension(f);
        return extension != null ? accept.contains(extension.toLowerCase()) : false;
    }
	
	
    /**
	 * Return the name (description) of the filter
	 *
	 * @return the name of the filter
	 */
    public String getDescription() {
        return name;
    }
}
