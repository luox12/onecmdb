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
import java.awt.ComponentOrientation;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import prefuse.data.Node;

public class OneCMDBGraphControl extends JPanel {

	public OneCMDBGraphControl() {
		setLayout(new BorderLayout());
		update();
	}
	
	public void update() {
		JSplitPane split = new JSplitPane();
		final ReferenceTree refTree = new ReferenceTree();
		TemplateTree tempTree = new TemplateTree();
		tempTree.update();
		tempTree.getTree().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				Node n = (Node)e.getPath().getLastPathComponent();
				refTree.update(n);
				EventDispatcher.fireEvent(this, new Event(CEvents.ITEM_SELECTED, n.getString("alias")));
				
			}
		});
		
		Box templates = new Box(BoxLayout.Y_AXIS);
		templates.setBorder(BorderFactory.createTitledBorder("Templates"));
		templates.add(tempTree);
		
		Box references = new Box(BoxLayout.Y_AXIS);
		references.setBorder(BorderFactory.createTitledBorder("References"));
		references.add(refTree);
	
		split.setTopComponent(templates);
		split.setBottomComponent(references);
		split.setOrientation(JSplitPane.VERTICAL_SPLIT);
		
		add(split, BorderLayout.CENTER);
		
	}
}
