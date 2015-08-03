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
package org.onecmdb.ml.graph.main.graphquery;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;

import org.onecmdb.ml.graph.main.CEvents;
import org.onecmdb.ml.graph.main.Event;
import org.onecmdb.ml.graph.main.EventDispatcher;
import org.onecmdb.ml.graph.main.IEventListener;

public class GraphQueryInputPanel extends JPanel implements IEventListener {

	
	private JTextArea area;

	public GraphQueryInputPanel() {
		EventDispatcher.addEventListener(this);
		initUI();
	}
	
	protected void initUI() {
		setLayout(new BorderLayout());
		area = new JTextArea();
		area.append("<?xml version=\"1.0\"?>\n");
		area.append("<GraphQuery>\n");
		area.append("\t...\n");
		area.append("</GraphQuery>\n");
		JButton b = new JButton("ReDraw");
		b.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				EventDispatcher.fireEvent(this, new Event(CEvents.REDRAW_GRAPH, area.getText()));
			}
		});
		JToolBar bar = new JToolBar();
		bar.add(b);
		add(bar, BorderLayout.NORTH);
		add(new JScrollPane(area), BorderLayout.CENTER);
	}

	public void onEvent(Event e) {
		switch(e.getType()) {
		case(CEvents.REDRAW_GRAPH):
			area.setText((String)e.getData());
			break;
		}
	}
}
