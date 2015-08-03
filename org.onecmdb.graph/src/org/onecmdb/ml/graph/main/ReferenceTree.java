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
package org.onecmdb.ml.graph.main;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.onecmdb.ml.graph.main.event.ModifyRelationEvent;
import org.onecmdb.ml.graph.main.tree.CheckBoxTreeRenderer;
import org.onecmdb.ml.graph.main.tree.NodTableEditor;
import org.onecmdb.ml.graph.main.view.GraphView;
import org.onecmdb.ml.graph.main.view.TreeView;


import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tree;
import prefuse.util.ui.JPrefuseTree;

public class ReferenceTree extends JPanel {

	public ReferenceTree() {
		setLayout(new BorderLayout());
	}
	
	public void update(Node n) {
		System.out.println(n);
		Node refNode = null;
		
		final Graph refGraph = TemplateModel.get("Root").getReferenceGraphAll();
		final Graph ref2Graph = TemplateModel.get("Root").getReferenceGraph();
	
		for (Iterator iter = refGraph.nodes(); iter.hasNext();) {
			Node rn = (Node)iter.next();
			System.out.println("\t" + rn.toString());
			if (rn.get("alias").equals(n.get("alias"))) {
				refNode = rn;
				break;
			}
		}
		
		removeAll();
		
		if (refNode != null) {
			
			refGraph.clearSpanningTree();
			
			final Tree tree = refGraph.getSpanningTree(refNode);
			
			JPrefuseTree jtree = new JPrefuseTree(tree, "name");
			CheckBoxTreeRenderer render = new CheckBoxTreeRenderer();
			
			jtree.setCellRenderer(render);
			jtree.setEditable(true);
			
			NodTableEditor editor = new NodTableEditor();
			editor.setListener(new IEventListener() {

				public void onEvent(Event e) {
					if (e.getData() instanceof Node) {
						Node n = (Node)e.getData();
						
						Edge parentEdge = tree.getParentEdge(n);
						if (parentEdge != null) {
							System.out.println(parentEdge.getSourceNode().getString("alias")
									+ "-->[" + 
									parentEdge.getString("type") 
									+ "]-->" + 
									parentEdge.getTargetNode().getString("alias"));

							ModifyRelationEvent modEvent = new ModifyRelationEvent();
							modEvent.setSource(parentEdge.getSourceNode());
							modEvent.setTarget(parentEdge.getTargetNode());
							modEvent.setEdge(parentEdge);
							modEvent.setChecked(n.getBoolean("checked"));
							modEvent.setPrimary(getRoot(tree, n));
							modEvent.setClicked(n);
							
							
							EventDispatcher.fireEvent(this, modEvent);
								
						} else {
							
							Edge edge = refGraph.getEdge(n,n);
							//if (edge != null) {
								ModifyRelationEvent modEvent = new ModifyRelationEvent();
								modEvent.setClicked(n);
								modEvent.setSource(n);
								modEvent.setTarget(n);
								modEvent.setEdge(edge);
								modEvent.setChecked(n.getBoolean("checked"));
								modEvent.setPrimary(n);
								EventDispatcher.fireEvent(this, modEvent);
							//}
					
						}
						
					}

				}

				private Node getRoot(Tree tree, Node n) {
					if (tree.getParent(n) == null) {
						n.setBoolean("checked", true);
						return(n);
					}
					return(getRoot(tree, tree.getParent(n)));
				}

			});
			jtree.setCellEditor(editor);
			JTabbedPane tp = new JTabbedPane();
			tp.add(new JScrollPane(jtree), "Tree");
			GraphView view = new GraphView(ref2Graph, "name", null);
			
		
			tp.add(new JScrollPane(view), "Graph");
			/*
			if (refNode != null) {
				Tree t = refGraph.getSpanningTree(refNode);
				tp.add(new JScrollPane(new TreeView(t, "name")), "Tree View");
			}
			*/
					
			
			add(tp, BorderLayout.CENTER);
		} else {
			add(new JPanel(), BorderLayout.CENTER);
		}
		
		revalidate();
	}
}
