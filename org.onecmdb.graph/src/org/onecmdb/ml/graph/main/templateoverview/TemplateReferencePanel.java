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
package org.onecmdb.ml.graph.main.templateoverview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.onecmdb.core.utils.bean.CiBean;
import org.onecmdb.ml.graph.main.CEvents;
import org.onecmdb.ml.graph.main.Event;
import org.onecmdb.ml.graph.main.EventDispatcher;
import org.onecmdb.ml.graph.main.IEventListener;
import org.onecmdb.ml.graph.main.TemplateModel;
import org.onecmdb.ml.graph.main.model.CIAttributeModel;
import org.onecmdb.ml.graph.main.model.CIModel;
import org.onecmdb.ml.graph.main.view.GraphView;
import org.onecmdb.swing.treetable.JTreeTable;

import prefuse.Visualization;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ui.JValueSlider;

public class TemplateReferencePanel extends JPanel implements IEventListener {
	
	private TemplateRelationGraphControl ctrl;
	private GraphView gView;
	private int defaultDistance = 1;
	private TemplateModel tModel;
	private CIAttributeModel referencePropertyControl;
	private JTreeTable pTree;
	private JTabbedPane tp;
	
	public TemplateReferencePanel(TemplateModel model) {
		this.tModel = model;
		
		initUI();
		EventDispatcher.addEventListener(this);
		
	}
	
	protected void initUI() {
		referencePropertyControl = new CIAttributeModel();
		referencePropertyControl.setAdvanced(true);
		
		pTree = new JTreeTable(referencePropertyControl);
		//pTree.setDefaultEditor(CiBean.class, referencePropertyControl.getTableCellEditor());
		
		ctrl = new TemplateRelationGraphControl(tModel);
		gView = new GraphView(ctrl.getGraph(), "alias", "type");
		gView.setEnableDistanceFilter(true);
		gView.setDistance(defaultDistance);
		
		
		gView.getVisualization().getGroup(Visualization.FOCUS_ITEMS).addTupleSetListener(
	            new TupleSetListener() {
	                public void tupleSetChanged(TupleSet t, Tuple[] add, Tuple[] rem) {
	                	if (add.length == 1) {
	                		// Fire selection change...
	                		if (add[0] instanceof Node) {
	                			Node n = (Node)add[0];
	                			EventDispatcher.fireEvent(this, new Event(CEvents.RELATION_ITEM_SELECTED, n.getString("alias")));
	                		}
	                	}
	                }
	            }
	        );
		
		JPanel center = new JPanel();
		center.setLayout(new BorderLayout());
		//center.add(getToolBar(), BorderLayout.NORTH);
		center.add(gView, BorderLayout.CENTER);
		
		tp = new JTabbedPane();
		tp.setTabPlacement(JTabbedPane.TOP);
		tp.add(new JScrollPane(pTree), "Attributes");
		tp.add(getControlPanel(), "Graph Control");
		//tp.add(new TemplateReferencePanel(), "Reference(s)");
		
		final JSplitPane split = new JSplitPane();
		split.setTopComponent(center);
		split.setBottomComponent(tp);
		split.setOrientation(JSplitPane.VERTICAL_SPLIT);
		split.setOneTouchExpandable(true);
	
		setLayout(new BorderLayout());
		add(split, BorderLayout.CENTER);
		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				Dimension newDimension = e.getComponent().getSize();
				split.setDividerLocation(0.8D);
		}
		});
	}
	
	public JComponent getControlPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Color.WHITE);
	   
		final JValueSlider slider = new JValueSlider("Max distance from selected", 0, 10, defaultDistance);
	    slider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
               gView.setDistance(slider.getValue().intValue());
            }
        });
	    final JCheckBox forceBounds = new JCheckBox("Keep objects within window");
	    forceBounds.setBackground(Color.white);
	    forceBounds.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				gView.setEnforceBounds(forceBounds.isSelected());
			}
	    	
	    });
	    final JCheckBox animation = new JCheckBox("Animation", true);
	    animation.setBackground(Color.WHITE);
	    animation.addActionListener(new ActionListener() {
		
			public void actionPerformed(ActionEvent e) {
				gView.animate(animation.isSelected());
			}
	    	
	    });
	    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    forceBounds.setAlignmentX(Component.LEFT_ALIGNMENT);
	    slider.setAlignmentX(Component.LEFT_ALIGNMENT);
	    animation.setAlignmentX(Component.LEFT_ALIGNMENT);
	    
	    Box checks = new Box(BoxLayout.Y_AXIS);
	    checks.add(Box.createHorizontalStrut(5));
	    checks.add(slider);
	    checks.add(forceBounds);
        checks.add(animation);
	    checks.setAlignmentY(Component.TOP_ALIGNMENT);
        checks.setBorder(BorderFactory.createTitledBorder(""));
	    checks.setMaximumSize(new Dimension(400, 80));
	    
        panel.add(Box.createVerticalStrut(5));
        panel.add(checks);
        
	    return(panel);
	}
	/*
	private JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		
		
		final JLabel label = new JLabel("" + defaultDistance);
		gView.setDistance(defaultDistance);
		
		final JSlider slider = new JSlider(0, 30, defaultDistance);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				gView.setDistance(slider.getValue());
				label.setText("" + slider.getValue());
			}
		});
		slider.setPreferredSize(new Dimension(150,30));
		slider.setMaximumSize(new Dimension(150,30));
		slider.setEnabled(true);
		label.setPreferredSize(new Dimension(48, 30));
		
		JPanel component = new JPanel();
		component.setLayout(new BoxLayout(component, BoxLayout.X_AXIS));
	    component.add(label);
	    component.add(slider);
		final JCheckBox enableDistance = new JCheckBox("Enable Distance");
        enableDistance.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				gView.setEnableDistanceFilter(enableDistance.isSelected());
				slider.setEnabled(enableDistance.isSelected());
			}
        });
		
        enableDistance.setSelected(true);
        gView.setEnableDistanceFilter(true);
        
		bar.add(enableDistance);
        bar.add(component);
		return(bar);
		
	}
	*/

	
	public void onEvent(Event e) {
		switch(e.getType()) {
			case(CEvents.ITEM_SELECTED):
			{
				String alias = (String) e.getData();
				
				gView.animate(false);
				
				Node n = ctrl.reloadGraph(alias);
				/*
				VisualItem f = (VisualItem)vg.getNode(0);
			    if (f != null) {
			    	
			    	m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(n);
			    }
				*/
				gView.setFocus(n);
				gView.redrawAndZoomToFit();
				gView.animate(true);
			}
				break;
			case(CEvents.REDRAW_GRAPH):
				break;
			case (CEvents.RELATION_ITEM_SELECTED):
			{
				String alias = (String) e.getData();
				CiBean bean = tModel.getBean(alias);
				if (bean != null) {
					tp.setTitleAt(0, "Attributes for " + bean.getAlias());
					referencePropertyControl.setRoot(new CIModel(bean, bean));
					TableModel tModel = pTree.getModel();
					if (tModel instanceof AbstractTableModel) {
						((AbstractTableModel)tModel).fireTableDataChanged();
					}
				}
			}
				break;
		}
		
	}

}
