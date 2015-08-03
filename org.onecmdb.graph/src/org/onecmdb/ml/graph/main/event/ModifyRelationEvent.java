/*
 * Lokomo OneCMDB - An Open Source Software for Configuration
 * Management of Datacenter Resources
 *
 * Copyright (C) 2006 Lokomo Systems AB
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 * 
 * Lokomo Systems AB can be contacted via e-mail: info@lokomo.com or via
 * paper mail: Lokomo Systems AB, Svärdvägen 27, SE-182 33
 * Danderyd, Sweden.
 *
 */
package org.onecmdb.ml.graph.main.event;

import java.util.List;

import org.onecmdb.ml.graph.main.CEvents;
import org.onecmdb.ml.graph.main.Event;

import prefuse.data.Edge;
import prefuse.data.Node;

public class ModifyRelationEvent extends Event {
	Node target;
	Node source;
	Edge edge;
	boolean checked;
	Node primary;
	Node clicked;
	private List<Node> path;
	
	
	public ModifyRelationEvent() {
		super(CEvents.RELATION_MODIFIED, null);
	}
	
	public Node getClicked() {
		return clicked;
	}

	public void setClicked(Node clicked) {
		this.clicked = clicked;
	}

	public Node getTarget() {
		return target;
	}
	public void setTarget(Node target) {
		this.target = target;
	}
	public Node getSource() {
		return source;
	}
	public void setSource(Node source) {
		this.source = source;
	}
	public Edge getEdge() {
		return edge;
	}
	public void setEdge(Edge edge) {
		this.edge = edge;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public void setPrimary(Node root) {
		this.primary = root;
	}

	public Node getPrimary() {
		return primary;
	}

	public void setPath(List<Node> path) {
		this.path = path;
		
	}

	public List<Node> getPath() {
		return path;
	}
	
	
	
	
	
}
