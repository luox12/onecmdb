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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.onecmdb.ml.graph.expression.EdgeTypeGroupPredicate;
import org.onecmdb.ml.graph.expression.FieldMatchGroupPredicate;
import org.onecmdb.ml.graph.utils.JValueSlider;
import org.onecmdb.ml.graph.view.AggregateDemo;
import org.onecmdb.ml.graph.view.GraphView;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.assignment.ColorAction;
import prefuse.activity.Activity;
import prefuse.controls.ControlAdapter;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.util.ui.JFastLabel;
import prefuse.util.ui.JPrefuseTable;
import prefuse.util.ui.JPrefuseTree;

import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;

public class TestAggregate {
	
	public static Graph getTestHw() {
		Graph g = new Graph();
		g.addColumn("name", String.class);
		g.addColumn("type", String.class);
		g.addColumn("mark", Boolean.class);
		
		// On Edge
		g.addColumn("springCoefficient", float.class);
		g.addColumn("springLength", float.class);
		
		// On Node
		g.addColumn("massValue", float.class);
		g.addColumn("image", String.class);
		
	
		
		// Create 1 router
		Node router = g.addNode();
		router.set("name", "Router");
		router.set("type", "Router");
		router.set("image", "images/router.jpg");
		
		Node rif1 = g.addNode();
		rif1.set("name", "Router-IF-1");
		rif1.set("type", "NIC");
		rif1.set("image", "images/nic.gif");
		
		Node rif2 = g.addNode();
		rif2.set("name", "Router-IF-2");
		rif2.set("type", "NIC");
		rif2.set("image", "images/nic.gif");
		
		Node rif3 = g.addNode();
		rif3.set("name", "Router-IF-3");
		rif3.set("type", "NIC");
		rif3.set("image", "images/nic.gif");
		
		Node rif4 = g.addNode();
		rif4.set("name", "Router-IF-4");
		rif4.set("type", "NIC");
		rif4.set("image", "images/nic.gif");
		
		Node wifi = g.addNode();
		wifi.set("name", "Router-WIFI");
		wifi.set("type", "NIC");
		wifi.set("image", "images/nic-wifi.png");
		
		
		addEdge(g, rif1, router, "ComposedOf");
		addEdge(g, rif2, router, "ComposedOf");
		addEdge(g, rif3, router, "ComposedOf");
		addEdge(g, rif4, router, "ComposedOf");
		addEdge(g, wifi, router, "ComposedOf");
			
		
		addServer(g, "Server-1", "UTP", rif1);
		addServer(g, "Server-2", "UTP", rif2);
		addServer(g, "Server-3", "UTP", rif3);
		addServer(g, "Server-4", "UTP", rif4);
			
		for (int i = 0; i < 10; i++) {
			addServer(g, "Server-wifi-" + i, "WIFI", wifi);
		}
		
		return(g);
	}

	private static Node addServer(Graph g, String name, String connectionType, Node rif) {
		Node n = g.addNode();
		n.set("name", name);
		n.set("type", "Server");
		n.set("image", "images/computer.png");
		
	
		// add a nic
		Node nic = g.addNode();
		nic.set("name", "NIC-" + name);
		nic.set("type", "NIC");
		if (connectionType.equals("WIFI")) {
			nic.set("image", "images/nic-wifi.png");
		} else {
			nic.set("image", "images/nic.gif");
		}
		
		addEdge(g, nic, n, "ComposedOf");
		
		// Connect nic to router IF.
		addEdge(g, nic, rif, connectionType);
		
		// Add OS + Programs..
		Node os = g.addNode();
		os.set("name", "OS");
		os.set("type", "OS");
		os.set("image", "images/vista.jpg");
		
		addEdge(g, os, n, "RunsOn");
		
		
		for (int i = 0; i < 10; i++) {
			Node p = g.addNode();
			p.set("name", "Application");
			p.set("type", "Application");
			p.set("image", "images/ssh.png");
			addEdge(g, p, os, "InstalledOn");
		}
		
		return(n);
		
	}
	
	
	private static Edge addEdge(Graph g, Node nic, Node n, String type) {
		Edge e = g.addEdge(nic, n);
		e.set("type", type);
		return(e);
	}

	public static void main(String argv[]) {
		
		
		
		JFrame frame = new JFrame();
		JPanel center = new JPanel();
		center.setLayout(new BorderLayout());
		final String label = "name";
	
		//final GraphView gView = new GraphView(getTestHw(), "name");
	    
		Display display = new AggregateDemo(); 
		Graph g = getTestHw();
		
		display.getVisualization().addGraph("graph", g);
		
		AggregateTable at = display.getVisualization().addAggregates("aggregates");
		at.addColumn(VisualItem.POLYGON, float[].class);
	    at.addColumn("id", int.class);
	    at.addColumn("type", String.class);
	    
		
		final JFastLabel title = new JFastLabel("                 ");
	        title.setPreferredSize(new Dimension(350, 20));
	        title.setVerticalAlignment(SwingConstants.BOTTOM);
	        title.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
	        title.setFont(FontLib.getFont("Tahoma", Font.PLAIN, 16));
	        
	        display.getVisualization().getDisplay(0).addControlListener(new ControlAdapter() {
	            public void itemEntered(VisualItem item, MouseEvent e) {
	                if ( item.canGetString(label) )
	                    title.setText(item.getString(label));
	            }
	            public void itemExited(VisualItem item, MouseEvent e) {
	                title.setText(null);
	            }
	        });
	 
	        /*	
		JPanel control = new JPanel();
		control.setLayout(new BoxLayout(control, BoxLayout.Y_AXIS));
	    control.setBackground(Color.WHITE);
	    
		final JValueSlider massRouter = new JValueSlider("Mass Router", 0, 100, 5);
		massRouter.setPreferredSize(new Dimension(300,30));
		massRouter.setMaximumSize(new Dimension(300,30));
		
		final JValueSlider massServer = new JValueSlider("Mass Server", 0, 100, 5);
		massServer.setPreferredSize(new Dimension(300,30));
		massServer.setMaximumSize(new Dimension(300,30));

		final JValueSlider massNIC = new JValueSlider("Mass NIC", 0, 100, 5);
		massNIC.setPreferredSize(new Dimension(300,30));
		massNIC.setMaximumSize(new Dimension(300,30));

		Box cf = new Box(BoxLayout.Y_AXIS);
		cf.add(addControl(display.getVisualization(), "Mass Router", 0, 100, 5, "Router", "massValue"));
		cf.add(addControl(display.getVisualization(), "Mass Server", 0, 100, 5, "Server", "massValue"));
		cf.add(addControl(display.getVisualization(), "Mass NIC", 0, 100, 5, "NIC", "massValue"));
		cf.setBorder(BorderFactory.createTitledBorder("Mass Filter"));
		
		Box rf = new Box(BoxLayout.Y_AXIS);
		rf.add(addControl(display.getVisualization(), "Spring Coeff", 1E-8, 1E-3, 1E-5, "ComposedOf", "springCoefficient"));
		rf.add(addControl(display.getVisualization(), "Spring Length", 0, 200, 5, "ComposedOf", "springLength"));
		rf.setBorder(BorderFactory.createTitledBorder("ComposedOf Releations"));

		Box c2 = new Box(BoxLayout.Y_AXIS);
		c2.add(addControl(display.getVisualization(), "Spring Coeff", 1E-8, 1E-3, 1E-5, "ConnectedTo", "springCoefficient"));
		c2.add(addControl(display.getVisualization(), "Spring Length", 0, 200, 5, "ConnectedTo", "springLength"));
		c2.setBorder(BorderFactory.createTitledBorder("ConnectedTo Releations"));

		control.add(cf);
		control.add(rf);
		control.add(c2);
		*/
		TypeControllPanel control = new TypeControllPanel();
		control.setVisualization(display.getVisualization());
		
		ColorAction markFill = new ColorAction("graph", 
				new FieldMatchGroupPredicate("graph", "mark", Boolean.TRUE), 
				VisualItem.STROKECOLOR, Color.green.getRGB());
		
		markFill.setVisualization(display.getVisualization());
		ActionList layout = (ActionList) display.getVisualization().getAction("layout");
		layout.add(1, markFill);
		
		//display.getVisualization().putAction("mark", mark);
		//display.getVisualization().runAfter("mark", "layout");
		
		
		JPrefuseTree.showTreeWindow(g.getSpanningTree(), "name");
		JPrefuseTable.showTableWindow(g.getNodeTable());
		
		
		center.add(display, BorderLayout.CENTER);
		center.add(title, BorderLayout.NORTH);
		center.add(control, BorderLayout.WEST);
		frame.getContentPane().add(center);
		
		frame.setSize(600, 400);
		
		frame.setVisible(true);
		
		display.getVisualization().run("layout");
		//display.getVisualization().run("mark");
	}

	private static Component addControl(final Visualization vis, String name, Number min, Number max, Number def,
			final String type, final String variable) {
		
		final JValueSlider slider = new JValueSlider(name, min, max, def);
		slider.getSlider().addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseEntered(MouseEvent e) {
				System.out.println("Mouse Enter");
				Iterator iter = vis.items(new FieldMatchGroupPredicate("graph", "type", type));
				while(iter.hasNext()) {
					VisualItem item = (VisualItem) iter.next();
					item.set("mark", Boolean.TRUE);
					//item.setFillColor(Color.green.getRGB());
				}
				
			}

			public void mouseExited(MouseEvent e) {
				System.out.println("Mouse Exit");
				
				Iterator iter = vis.items(new FieldMatchGroupPredicate("graph", "type", type));
				while(iter.hasNext()) {
					VisualItem item = (VisualItem) iter.next();
					item.set("mark", Boolean.FALSE);
					//item.setFillColor(Color.green.getRGB());
				}
			}

			public void mousePressed(MouseEvent e) {
				System.out.println("Mouse Pressed");
				
			}

			public void mouseReleased(MouseEvent e) {
				System.out.println("Mouse Relesed");
				
			}
			
		});
		 slider.addChangeListener(new ChangeListener() {
	            public void stateChanged(ChangeEvent e) {
	            	Iterator iter = vis.items(new FieldMatchGroupPredicate("graph", "type", type));
					while(iter.hasNext()) {
						VisualItem item = (VisualItem) iter.next();
						item.set(variable, slider.getValue());
						
						//vis.run("mark");
						//item.setFillColor(Color.green.getRGB());
					}
	            }
	        });
		 slider.setPreferredSize(new Dimension(300,30));
		 slider.setMaximumSize(new Dimension(300,30));

		return(slider);
		
	}

	
	
	private static void setupGraph(Visualization vis) {
		Graph g = new Graph();
		g.addColumn("name", String.class);
		
		for (int j = 0; j < 10; j++) {
			Node parent = g.addNode();
			parent.set("name", "Parent" + j);
			for (int i = 0; i < 4; i++) {
				Node child = g.addNode();
				child.set("name", "Child" + j + "." + i);
				
				if (((i % 3) == 0) && i != 0 && j > 0) {
					//Node s = g.getNode(i);
					int index = (j*5)-5;
					System.out.println(i + "-->" + index);
					Node t = g.getNode(index);
					g.addEdge(child, t);
				}
				
				g.addEdge(parent, child);
			}
		}
		
		vis.addGraph("graph", g);
		
		AggregateTable at = vis.addAggregates("aggregates");
		at.addColumn(VisualItem.POLYGON, float[].class);
	    at.addColumn("id", int.class);
	    at.addColumn("type", String.class);
		
	    
	    
	    for (int i = 0; i < 10; i++) {
	    	AggregateItem aitem = (AggregateItem)at.addItem();
	    	aitem.setInt("id", i);
	    	System.out.println("Group " + i);
	    	for (int j = 0; j < 5; j++) {
	    		int index = (i*5)+j;
	    		VisualItem item = vis.getVisualItem("graph.nodes", g.getNode(index));
	    		System.out.println("\t" + index + ":" +item.get("name"));
	    		aitem.addItem(item);
	    	}
	    }
	}

	
	
}
