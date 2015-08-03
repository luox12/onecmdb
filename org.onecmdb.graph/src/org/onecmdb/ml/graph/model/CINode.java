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

import java.io.PrintStream;
import java.io.PrintWriter;

public class CINode extends CIItem{
	
	public CINode(String id) {
		super(id);
	}
	
	public CINode(String id, String name) {
		super(id);
		setProperty("alias", id);
		setProperty("name", name);
	}
	
	public CINode(String id, String image, String name) {
		this(id, name);
		setProperty("image", image);
	}
	

	
	public void toGraphML(PrintWriter out) {
		out.println("<node id=\"" + getId() +"\">");
		propertiesToGraphML(out);
		out.println("</node>");
	}

}