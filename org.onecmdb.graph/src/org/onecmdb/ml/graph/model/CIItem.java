/*
 * OneCMDB, an open source configuration management project.
 * Copyright 2007, Lokomo Systems AB, and individual contributors
 * as indicated by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.onecmdb.ml.graph.model;


import java.io.PrintWriter;
import java.util.HashMap;

public abstract class CIItem {
	private HashMap<String, String> properties = new HashMap<String, String>();
	private String id;
	
	
	public CIItem(String id) {
		this.id = id;
	}
	
	public void setProperty(String key, String value) {
		this.properties.put(key, value);
	}
	
	public void propertiesToGraphML(PrintWriter out) {
		for (String key : this.properties.keySet()) {
			out.println("<data key=\"" + key + "\">" + this.properties.get(key) + "</data>");
		}
	}

	public String getId() {
		return(this.id);
	}

}
