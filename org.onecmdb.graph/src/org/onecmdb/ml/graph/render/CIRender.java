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
package org.onecmdb.ml.graph.render;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
 
public class CIRender extends JPanel {
    
    BufferedImage bi;
    int w = 400;
    int h = 300;
    
    public CIRender() {
        
    GradientPaint gradient =
    		new GradientPaint(0, 0, Color.red, 175, 175, Color.yellow,
    		true); // true means to repeat pattern
    	
        setPreferredSize (new Dimension (w, h));
        
        bi = new BufferedImage (w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        
        g.setPaint (Color.WHITE);
        g.fillRect (0, 0, w, h);
        
        g.setPaint (gradient);
        g.fillOval (5, 5, w - 10, h - 10);
        
        g.setColor (Color.BLUE);
        g.fillRect (60, 60, w - 120, h - 120);
        
        g.setColor (Color.BLACK);
        g.drawLine (0, 0, w, h);
        g.drawLine (w, 0, 0, h);
    }
    
    @Override
    public void paintComponent (Graphics g) {
        super.paintComponent (g);
        g.drawImage (bi, 0, 0, w, h, null);
    }
    
    public static void main (String[] args) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                JFrame frame = new JFrame ("Buffered Image Panel Test");
                frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
                
                frame.add (new CIRender ());
                frame.pack ();
                frame.setResizable (true);
                frame.setVisible (true);
            }
        });
    }
}
