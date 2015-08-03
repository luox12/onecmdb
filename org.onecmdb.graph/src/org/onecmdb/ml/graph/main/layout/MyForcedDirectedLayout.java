package org.onecmdb.ml.graph.main.layout;


import org.onecmdb.ml.graph.layout.ForceDirectedLayout;

import prefuse.util.force.DragForce;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

public class MyForcedDirectedLayout extends ForceDirectedLayout {

	public MyForcedDirectedLayout(String graph) {
		super(graph, false);
	}
	
	public MyForcedDirectedLayout(String graph, ForceSimulator fsim) {
		super(graph, fsim, false);
	}
	
	public void setEnforceBounds(boolean value) {
		m_enforceBounds = value;
	}

	@Override
	protected float getMassValue(VisualItem n) {
		if (n.canGetFloat("massValue")) {
			float f = n.getFloat("massValue");
			if (f > 0) {
				return(f);
			}
		}
		
		return super.getMassValue(n);
	}

	@Override
	protected float getSpringCoefficient(EdgeItem e) {
		if (e.canGetFloat("springCoefficient")) {
			float f = e.getFloat("springCoefficient");
			if (f > 0) {
				return(f);
			}
		}
		return super.getSpringCoefficient(e);
	}

	@Override
	protected float getSpringLength(EdgeItem e) {
		if (e.canGetFloat("springLength")) {
			float f = e.getFloat("springLength");
			if (f > 0) {
				return(f);
			}
		}
		return super.getSpringLength(e);
	}
}