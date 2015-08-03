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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;

import org.onecmdb.ml.graph.main.tree.CheckBoxTreeRenderer;
import org.onecmdb.ml.graph.main.tree.NodTableEditor;
import org.onecmdb.ml.graph.main.view.GraphView;
import org.onecmdb.ml.graph.main.view.TreeView;

import prefuse.data.Graph;
import prefuse.data.Tree;
import prefuse.util.ui.JPrefuseTree;

public class TemplateTree extends JPanel {

	private JPrefuseTree jtree;

	public TemplateTree() {
		setLayout(new BorderLayout());
	}
	
	public void update() {
		Graph templateGraph = TemplateModel.get("Root").getTemplateGraph();
		
		Tree tree = templateGraph.getSpanningTree();
		
		JPrefuseTree.showTreeWindow(tree, "name");
		
		jtree = new JPrefuseTree(tree, "name");
		CheckBoxTreeRenderer render = new CheckBoxTreeRenderer();
		jtree.setCellRenderer(render);
		jtree.setEditable(true);
		
		NodTableEditor editor = new NodTableEditor();
		editor.setListener(new IEventListener() {

			public void onEvent(Event e) {
				EventDispatcher.fireEvent(this, new Event(CEvents.ITEM_MODIFIED, e.getData()));
			}
			
		});

		jtree.setCellEditor(editor);
		JTabbedPane tp = new JTabbedPane();
		tp.add(new JScrollPane(jtree), "Tree");
		tp.add(new TreeView(tree, "name"), "Tree View");
		
		
		add(tp, BorderLayout.CENTER);
	}
	
	public JTree getTree() {
		return(jtree);
	}
}
