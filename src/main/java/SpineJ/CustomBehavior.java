/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ;

import ij3d.Content;
import ij3d.behaviors.InteractiveBehavior;
import ij3d.behaviors.Picker;
import java.awt.event.MouseEvent;

/**
 *
 * @author phm
 */
public class CustomBehavior extends InteractiveBehavior {
        DendriteViewer3D_ viewer = null;
        
	private Content c;
	
	CustomBehavior(Content c) {
		super(DendriteViewer3D_.universe);
		this.c = c;
	}

	public void doProcess(MouseEvent e) {
		viewer = DendriteViewer3D_.instance;
                if(!e.isControlDown() ||
			e.getID() != MouseEvent.MOUSE_PRESSED) {
			super.doProcess(e);
			return;
		}
		// Get the point on the geometry where the mouse
                // press occurred
                Picker p = new Picker(DendriteViewer3D_.universe);
                c = p.getPickedContent(e.getX(), e.getY());
                // click outside
                if(c == null) {
                    viewer.deSelectAllSpines();
                    return;
                }
		String name = c.getName();
		viewer.spineSelected(name, e.getX(), e.getY());
	}
}

