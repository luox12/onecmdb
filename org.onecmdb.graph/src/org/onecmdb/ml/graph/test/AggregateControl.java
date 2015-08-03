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
package org.onecmdb.ml.graph.test;

import java.util.Iterator;

import org.onecmdb.ml.graph.expression.FieldMatchGroupPredicate;

import prefuse.Visualization;
import prefuse.util.GraphLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class AggregateControl {

	public static void collapseAggregate(Visualization vis, String nodeType, boolean enable) {
		Iterator iter = vis.items(new FieldMatchGroupPredicate("aggregates", "type", nodeType));
		
	
		while(iter.hasNext()) {
			AggregateItem aitem = (AggregateItem)iter.next();
			
			double x = aitem.getBounds().getCenterX();
			double y = aitem.getBounds().getCenterY();

			// Make all items invisable...
			Iterator aiter = aitem.items();
			while(aiter.hasNext()) { 
				NodeItem item = (NodeItem)aiter.next();
				item.setVisible(enable);
				
				if (!enable) {
					item.setFixed(true);
					PrefuseLib.setX(item, null, x);
					PrefuseLib.setY(item, null, y);
				}
				
				Iterator edges = item.edges();
				while(edges.hasNext()) {
					EdgeItem e = (EdgeItem)edges.next();
					VisualItem target = e.getTargetItem();
					VisualItem source = e.getSourceItem();
					if (aitem.containsItem(target) && aitem.containsItem(source)) {
						e.setVisible(enable);
					}
				}
			}
		}
	}
	
	public static void handleAggregate(Visualization vis, int id, String nodeType, String edgeType, boolean enable) {
		
		AggregateTable at = (AggregateTable) vis.getGroup("aggregates");
		
		System.out.println("Aggregate: " + nodeType + ":" + enable);
		
		Iterator iter = vis.items(new FieldMatchGroupPredicate("graph.nodes", "type", nodeType));
		while(iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			Iterator aggregates = at.getAggregates(item);
			if (!aggregates.hasNext()) {
				addAggregate(id, (NodeItem)item, (AggregateItem) at.addItem(), enable);
			} else {
				while(aggregates.hasNext()) {
					addAggregate(id, (NodeItem)item, (AggregateItem) aggregates.next(), enable);
				}
			}
		}
	}
	
	private static void addAggregate(int id, NodeItem node, AggregateItem aitem, boolean enable) {
		System.out.println("\t" + node.toString());
		
		aitem.set("id", id);
		aitem.set("type", node.getString("type"));
		Iterator edges = node.edges();
		if (enable) {
			aitem.addItem(node);
		} else {
			aitem.removeItem(node);
		}
		
		while(edges.hasNext()) {
			EdgeItem e = (EdgeItem)edges.next();
			VisualItem target = e.getTargetItem();
			VisualItem source = e.getSourceItem();
			
			System.out.println("\t\t-->" + target.toString());
			System.out.println("\t\t-->" + source.toString());
			
			aitem.setInt("id", 1);
	    	if (!target.equals(node)) {
	    		if (enable) {
	    			aitem.addItem(target);
	    		} else {
	    			aitem.removeItem(target);
	    		}
	    	}
	    	if (!source.equals(node)) {
	    		if (enable) {
	    			aitem.addItem(source);
	    		} else {
	    			aitem.removeItem(source);
	    		}
	    	}
	   }

	}
}
