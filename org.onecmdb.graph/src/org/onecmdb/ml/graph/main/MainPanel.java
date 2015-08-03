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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.codec.net.QCodec;
import org.onecmdb.core.utils.bean.CiBean;
import org.onecmdb.ml.graph.main.activty.UpdateModelActivity;
import org.onecmdb.ml.graph.main.event.ModifyRelationEvent;
import org.onecmdb.ml.graph.main.model.CIAttributeModel;
import org.onecmdb.ml.graph.main.model.CIModel;
import org.onecmdb.ml.graph.main.view.QueryGraphControlPanel;
import org.onecmdb.ml.graph.main.view.GraphView;
import org.onecmdb.swing.treetable.JTreeTable;

import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ui.JPrefuseTable;
import prefuse.util.ui.JPrefuseTree;
import prefuse.visual.VisualItem;

public class MainPanel extends JFrame implements IEventListener {
	
	private JPanel graph;
	private JPanel property;
	private GraphControl gControl;
	private Visualization vis;
	private CIAttributeModel propertyControl;
	private JTreeTable pTree;
	private QueryGraphControlPanel gControlPanel;

	public MainPanel() {
		initUI();
	}
	
	protected void initUI() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
	
		
		EventDispatcher.addEventListener(this);
		
		setSize(800, 600);
		
		gControl = new GraphControl();
		propertyControl = new CIAttributeModel();
		
		GraphView gView = new GraphView(gControl.getGraph(), "name", null);
		vis = gView.getVisualization();
		vis.putAction("updateModel", new UpdateModelActivity(gControl));
		
		 TupleSet focusGroup = vis.getGroup(Visualization.FOCUS_ITEMS); 
	     focusGroup.addTupleSetListener(new TupleSetListener() {
	            public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem)
	            {
	            	if (add.length > 0) {
	            		VisualItem item = (VisualItem)add[0];
	            		String alias = item.getString("alias");
	            		if (alias != null) {
	            			EventDispatcher.fireEvent(this, new Event(CEvents.ITEM_SELECTED, alias));
	            		}
	            	}
	            }

	        });
		//JPrefuseTable.showTableWindow(gControl.getGraph().getNodeTable());
		//JPrefuseTable.showTableWindow(gControl.getGraph().getEdgeTable());
		
		graph = new JPanel();
		graph.setLayout(new BorderLayout());
		graph.add(gView, BorderLayout.CENTER);
		
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new BorderLayout());
		
		pTree = new JTreeTable(propertyControl);
		pTree.setDefaultEditor(CiBean.class, propertyControl.getTableCellEditor());
		//pTree.setColumnModel(propertyControl.getColumnModel());
		
		final JCheckBox box = new JCheckBox("Advanced");
		box.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				propertyControl.setAdvanced(box.isSelected());
				pTree.tableChanged(new TableModelEvent(pTree.getModel(), TableModelEvent.HEADER_ROW));
				/*
				TableModel tModel = pTree.getModel();
				if (tModel instanceof AbstractTableModel) {
					((AbstractTableModel)tModel).fireTableDataChanged();
				}
				*/
			}
			
		});
		final JCheckBox force = new JCheckBox("Show all without references");
		force.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gControl.setExclude(force.isSelected());
			}
		});
		force.setSelected(true);
		JPanel checkPanel = new JPanel();
		checkPanel.add(box);
		checkPanel.add(force);
		pPanel.add(checkPanel, BorderLayout.NORTH);
		pPanel.add(new JScrollPane(pTree), BorderLayout.CENTER);
		
		
		gControlPanel = new QueryGraphControlPanel(gControl);
		
		property = new JPanel();
		property.setLayout(new BorderLayout());
		JTabbedPane tp = new JTabbedPane();
		property.add(tp, BorderLayout.CENTER);
		tp.add(pPanel, "Properties");
		//tp.add(gControlPanel, "Graph Control");
		tp.add(new JScrollPane(new JPrefuseTable(gControl.getGraph().getNodeTable())), "Node Table");
		tp.add(new JScrollPane(new JPrefuseTable(gControl.getGraph().getEdgeTable())), "Edge Table");
		//tp.add(new JScrollPane(new JPrefuseTable(gView.getVisaulGraph().getNodeTable())), "VNode Table");
		//tp.add(new JScrollPane(new JPrefuseTable(gView.getVisaulGraph().getEdgeTable())), "VEdge Table");
		
		JSplitPane centerSplit = new JSplitPane();
		centerSplit.setTopComponent(graph);
		centerSplit.setBottomComponent(property);
		centerSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		centerSplit.setOneTouchExpandable(true);
		
		
		JSplitPane splitLeft = new JSplitPane();
		splitLeft.setTopComponent(new OneCMDBGraphControl());
		splitLeft.setBottomComponent(centerSplit);
		splitLeft.setOneTouchExpandable(true);
		
		JSplitPane splitRight = new JSplitPane();
		splitRight.setTopComponent(splitLeft);
		splitRight.setBottomComponent(gControlPanel);
		splitRight.setOneTouchExpandable(true);
		
		panel.add(splitRight, BorderLayout.CENTER);
		//panel.add(property, BorderLayout.SOUTH);
		getContentPane().add(panel);
		//pack();
	}

	public static void main(String argv[]) {
		new MainPanel().setVisible(true);
	}
	
	private void showProperty(String alias) {
		
		CiBean bean = OneCMDBConnection.instance().getBeanFromAlias(alias);
		CiBean template = null;
		
		if (bean == null) {
			return;
		}
		if (bean.isTemplate()) {
			template = bean;
		} else {
			template = OneCMDBConnection.instance().getBeanFromAlias(bean.getDerivedFrom());
		}
		
		CIModel model = new CIModel(template, bean);
		
		propertyControl.setRoot(model);
		TableModel tModel = pTree.getModel();
		if (tModel instanceof AbstractTableModel) {
			((AbstractTableModel)tModel).fireTableDataChanged();
		}
		
	}

	
	public void onEvent(Event e) {
		
		switch(e.getType()) {
			case CEvents.UPDATE_GRAPH:
				gControl.update();
				gControlPanel.update();
				vis.run("updateModel");
				vis.run("draw");
				break;
			case CEvents.ITEM_SELECTED:
				showProperty((String)e.getData());
				break;
			case CEvents.ITEM_MODIFIED:
				Node n = (Node) e.getData();
				
				String a = n.getString("alias");
				if (n.getBoolean("checked")) {
					gControl.addItem(a, a, true);
				} else {
					gControl.removeItem(a);
				}
				EventDispatcher.fireEvent(this, new Event(CEvents.UPDATE_GRAPH, null));
				break;
			
			case CEvents.RELATION_MODIFIED:
				if (e instanceof ModifyRelationEvent) {
					ModifyRelationEvent mod = (ModifyRelationEvent)e;
					if (mod.isChecked()) {
						if (mod.getEdge() != null) {
						gControl.addRelation(mod.getSource().getString("alias"), 
								mod.getEdge().getString("type"), 
								mod.getTarget().getString("alias"), 
								mod.getPrimary().getString("alias"));
						}
					} else {
						if (mod.getEdge() != null) {
							gControl.removeRelation(mod.getSource().getString("alias"), 
								mod.getEdge().getString("type"), 
								mod.getTarget().getString("alias"), mod.getClicked().getString("alias"));
						} else {
							gControl.removeRelation(mod.getSource().getString("alias"), 
									"Reference", 
									mod.getTarget().getString("alias"), mod.getClicked().getString("alias"));
						}
					}
					EventDispatcher.fireEvent(this, new Event(CEvents.UPDATE_GRAPH, null));
					
				}
				
				break;
		}
	}	
}
