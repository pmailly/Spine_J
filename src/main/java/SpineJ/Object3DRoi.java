/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ;

import ij.gui.PolygonRoi;
import ij.gui.Roi;
import java.util.ArrayList;
import mcib3d.geom.Object3D;
import mcib3d.geom.Voxel3D;

/**
 *
 * @author phm
 */
public class Object3DRoi {
    final private Object3D obj;
    
    /**
     * @param obj
     */
    public Object3DRoi(Object3D obj) {
            super();
            this.obj = obj;
    }
    /**
     * @return the roi of the object at z position 
     * @param z Z position
     */
    public PolygonRoi getRois(int z) {
        ArrayList<Voxel3D> contours3D = obj.getContours();
        int[] x = new int[contours3D.size()];
        int[] y = new int[contours3D.size()];
        int nbPoint = -1;
        for (int i = 0; i < contours3D.size(); i++ ) {    
            if (z == contours3D.get(i).z+1) {
                nbPoint++;
                x[nbPoint] = (int)contours3D.get(i).x;
                y[nbPoint] = (int)contours3D.get(i).y;
            }
        }
            PolygonRoi pRoi = new PolygonRoi(x, y, nbPoint, Roi.POLYGON); 
            return(new PolygonRoi(pRoi.getConvexHull(), PolygonRoi.FREELINE));
    }   
}