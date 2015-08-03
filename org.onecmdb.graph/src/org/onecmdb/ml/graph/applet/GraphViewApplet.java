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
 * paper mail: Lokomo Systems AB, Sv�rdv�gen 27, SE-182 33
 * Danderyd, Sweden.
 *
 */
package org.onecmdb.ml.graph.applet;

import java.applet.AppletContext;

import javax.swing.JApplet;

import org.onecmdb.ml.graph.main.OneCMDBConnection;
import org.onecmdb.ml.graph.main.graphquery.MainGraphQueryView;

import prefuse.activity.ActivityManager;



public class GraphViewApplet extends JApplet {

	private MainGraphQueryView mainPanel;

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}

	@Override
	public AppletContext getAppletContext() {
		// TODO Auto-generated method stub
		return super.getAppletContext();
	}

	@Override
	public String getParameter(String name) {
		// TODO Auto-generated method stub
		return super.getParameter(name);
	}

	@Override
	public String[][] getParameterInfo() {
		return(new String[][] {
				{"token","string","An authenticated token to OneCMDB"},
				{"url","string","The URL to oneCMDB WSDL"},
				{"contentRoot","string","URL to content root for OneCMDB"},
				{"graphDefinition","string","PAth to graph definitions"},
							
		});
	}

	@Override
	public void init() {
		super.init();
		AppletLogger.showMessage("OneCMDB Graph View init...");
		OneCMDBConnection con = new OneCMDBConnection();
		con.setToken(getParameter("token"));
		con.setUrl(getParameter("url"));
		
		try {
			con.setup();
		} catch (Throwable t) {
			IllegalArgumentException e = new IllegalArgumentException("Setup CMDB Connection: " + t.getMessage());
			e.initCause(t);
			throw e;
		}
		
		OneCMDBConnection.setInstance(con);
		AppletLogger.showMessage("OneCMDB Graph View set url " + getParameter("url") + ", token=" + getParameter("token"));
		
		mainPanel = new MainGraphQueryView(getParameter("contentRoot"), getParameter("graphDefinition"));
	}
	
	public void start() {
		getContentPane().add(mainPanel);
	}
	
	public void stop() {
		// TODO: Pause prefuse thread.
		ActivityManager.stopThread();
	}

}
