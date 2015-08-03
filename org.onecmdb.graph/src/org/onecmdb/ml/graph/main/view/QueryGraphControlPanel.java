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
package org.onecmdb.ml.graph.main.view;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.onecmdb.ml.graph.main.CEvents;
import org.onecmdb.ml.graph.main.Event;
import org.onecmdb.ml.graph.main.EventDispatcher;
import org.onecmdb.ml.graph.main.GraphControl;
import org.onecmdb.ml.graph.main.LocalOneCMDBConnection;
import org.onecmdb.ml.graph.main.model.CIAttributeModel;
import org.onecmdb.ml.graph.main.model.CIModel;
import org.onecmdb.swing.treetable.JTreeTable;

public class QueryGraphControlPanel extends JPanel {
	
	
	private GraphControl ctrl;
	private CIAttributeModel propertyControl;
	private JTreeTable pTree;
	private GraphView gView;
	
	public QueryGraphControlPanel(final GraphControl ctrl) {
		setLayout(new BorderLayout());
		JSplitPane split = new JSplitPane();
		
		this.ctrl = ctrl;
		
		propertyControl = new CIAttributeModel();
		pTree = new JTreeTable(propertyControl);
		gView = new GraphView(this.ctrl.getQueryGraph(), "name", null);	
	
		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		
		final JTextField group = new JTextField("SecurityGroup", 15);
		group.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					ctrl.setSecurityGroup(group.getText());
					EventDispatcher.fireEvent(this, new Event(CEvents.UPDATE_GRAPH, null));
				}
			}
		});
		JPanel p1 = new JPanel();
		p1.add(new JLabel("Security Group"));
		p1.add(group);
		top.add(p1, BorderLayout.NORTH);
		
		Box queryProperty = new Box(BoxLayout.Y_AXIS);
		queryProperty.setBorder(BorderFactory.createTitledBorder("Query Attribute"));
		queryProperty.add(new JScrollPane(pTree));

		top.add(queryProperty, BorderLayout.CENTER);
		
		Box queryView = new Box(BoxLayout.Y_AXIS);
		queryView.setBorder(BorderFactory.createTitledBorder("Query Graph"));
		queryView.add(gView);

		split.setTopComponent(top);
		split.setBottomComponent(queryView);
		split.setOrientation(JSplitPane.VERTICAL_SPLIT);
		
		add(split, BorderLayout.CENTER);
	}
	
	public void update() {	
		LocalOneCMDBConnection graphModel = new LocalOneCMDBConnection();
		graphModel.updateBeans(this.ctrl.graphBeanTemplate());
		graphModel.updateBeans(this.ctrl.graphToBean());
		
		CIModel model = new CIModel(graphModel, graphModel.getBeanFromAlias("GraphQuery"), graphModel.getBeanFromAlias("GraphQuery-1"));
		propertyControl.setRoot(model);
		
		TableModel tModel = pTree.getModel();
		if (tModel instanceof AbstractTableModel) {
			((AbstractTableModel)tModel).fireTableDataChanged();
		}
		
		gView.getVisualization().run("draw");
	}

	
	
	
	

}
