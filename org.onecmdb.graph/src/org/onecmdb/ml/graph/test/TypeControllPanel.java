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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.onecmdb.ml.graph.expression.FieldMatchGroupPredicate;
import org.onecmdb.ml.graph.utils.JValueSlider;

import prefuse.Visualization;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;

public class TypeControllPanel extends JPanel {
	
	public TypeControllPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	    setBackground(Color.WHITE);
	    
	}
	
	public void setVisualization(final Visualization vis) {
		
		
		TupleSet nodes = vis.getGroup("graph.nodes");
		Iterator iter = nodes.tuples();
	
		Set<String> nodeTypes = new HashSet<String>();
		Set<String> edgeTypes = new HashSet<String>();
		
		while(iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			if (item.canGet("type", String.class)) {
				String type = item.getString("type");
				nodeTypes.add(type);
			}
		}
		
		TupleSet edges = vis.getGroup("graph.edges");
		iter = edges.tuples();
		while(iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			if (item.canGet("type", String.class)) {
				String type = item.getString("type");
				edgeTypes.add(type);
			}
		}
		Box massBox = new Box(BoxLayout.Y_AXIS);
		massBox.setBorder(BorderFactory.createTitledBorder("Mass Filter"));
		for (String nType : nodeTypes) {
			massBox.add(getControl(vis, "Mass " + nType, 0, 100, 5, nType, "massValue"));
		}
		add(massBox);
		
		for (String eType : edgeTypes) {
			Box rf = new Box(BoxLayout.Y_AXIS);
			rf.add(getControl(vis, "Spring Coeff", 1E-8, 1E-3, 1E-5, eType, "springCoefficient"));
			rf.add(getControl(vis, "Spring Length", 0, 200, 5, eType, "springLength"));
			rf.setBorder(BorderFactory.createTitledBorder(eType));
			add(rf);
		}
		
		Box agg = new Box(BoxLayout.Y_AXIS);
		agg.setBorder(BorderFactory.createTitledBorder("Aggregates"));
		add(agg);
		int index = 0;
		for (final String nType : nodeTypes) {
			final int idx = index;
			final JCheckBox box = new JCheckBox("Aggregate " + nType);
			box.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					AggregateControl.handleAggregate(vis, idx, nType, "ComposedOf", box.isSelected());
				}
				
			});
			agg.add(box);
			index++;
		}
		for (final String nType : nodeTypes) {
			final int idx = index;
			final JCheckBox box = new JCheckBox("Collapse " + nType);
			box.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					AggregateControl.collapseAggregate(vis, nType, !box.isSelected());
				}
				
			});
			agg.add(box);
			index++;
		}
		
	}
	
	
	protected Component getControl(final Visualization vis, String name, Number min, Number max, Number def,
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
	

}
