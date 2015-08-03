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
package org.onecmdb.ml.graph.main.transform;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

public class ProgressTask extends Thread {
	
	private Runnable run;
	JProgressBar bar = new JProgressBar(0,100);
	private JDialog dialog;
	
	public ProgressTask(Runnable run) {
		this.run = run;
	}
	
	public void initUI() {
		dialog = new JDialog();
		dialog.setTitle("Title of Dialog");
		
		bar.setStringPainted(true);
		dialog.add(bar);
		dialog.setSize(300, 200);
		dialog.setVisible(true);
	}
	


	@Override
	public void run() {
		initUI();
		
		Thread t = new Thread(new Runnable() {

			public void run() {
				run.run();
			}
			
		});
		t.start();
		long start = System.currentTimeMillis();
		boolean threadDied = false;
		int count = 0;
		while(!threadDied) {
			try {
				t.join(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			count++;
			if (t.isAlive()) {
				bar.setValue(count);
				bar.setString("Elapsed: " + (System.currentTimeMillis()-start) + "ms");
			} else {
				threadDied = true;
			}
		}
		
		dialog.setVisible(false);
	}
	
	

}
