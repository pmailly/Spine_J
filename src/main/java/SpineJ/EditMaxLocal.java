/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SpineJ;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.process.ImageProcessor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import Tools.Voxel;

/**
 *
 * @author thomasb
 */
public class EditMaxLocal implements MouseListener, MouseMotionListener {

    // DendriteViewer3D_ dendrite = DendriteViewer3D_.instance;
    public EditMaxLocal() {
    }

    public void init() {
        DendriteViewer3D_ dendrite = DendriteViewer3D_.instance;
        ImageCanvas mergeCanvas = dendrite.imgMaxMerge.getCanvas();
        mergeCanvas.addMouseListener(this);
        mergeCanvas.addMouseMotionListener(this);
        ImageCanvas projCanvas = dendrite.imgMaxProj.getCanvas();
        projCanvas.addMouseListener(this);
        projCanvas.addMouseMotionListener(this);
        // CONSTRUCTOR
        EditJDialog dia = new EditJDialog(null, true);
        dia.setDendrite(dendrite);
        if (dia.main()) {
            mergeCanvas.removeMouseListener(this);
            mergeCanvas.removeMouseMotionListener(this);
            projCanvas.removeMouseListener(this);
            projCanvas.removeMouseMotionListener(this);
        }
    }

    public void editMaxLocalMerge(int x, int y, int z, boolean left, boolean leftShift, boolean leftCtrl) {
        DendriteViewer3D_ dendrite = DendriteViewer3D_.instance;
        // Add max local
        if ((left) && (leftCtrl)) {
            Voxel V = dendrite.findMaxInt(x, y, dendrite.imgMaxMerge, 3);
            // Add maxlocal in list
            dendrite.trimListMaxLocal.add(V);
            // set pixel in image stack and proj to max 16bit
            dendrite.imgMaxMerge.setPosition(1, V.getZ() , 1);
            ImageProcessor ipMerge = dendrite.imgMaxMerge.getProcessor();
            ipMerge.putPixel(V.getX(), V.getY(), dendrite.max16);
            dendrite.imgMaxMerge.updateAndDraw();
            dendrite.imgMaxProj.setPositionWithoutUpdate(1, 1, 1);
            ImageProcessor ipProj = dendrite.imgMaxProj.getProcessor();
            ipProj.putPixel(V.getX(), V.getY(), dendrite.max16);
            dendrite.imgMaxProj.updateAndDraw();

        } // Remove max local 
        else if ((left) && (leftShift)) {
            int index = findNearnestPointMerge(x, y, z);
            //IJ.log("index found:" + index + " "+trimListMaxLocal.get(index).getX()+ " "+trimListMaxLocal.get(index).getY());
            Voxel V = dendrite.trimListMaxLocal.remove(index);
            // set pixel in image stack and proj to 0 in green channel
            dendrite.imgMaxMerge.setPosition(1, V.getZ() + 1, 1);
            ImageProcessor ipMerge = dendrite.imgMaxMerge.getProcessor();
            ipMerge.putPixel(V.getX(), V.getY(), 0);
            ipMerge.resetMinAndMax();
            dendrite.imgMaxMerge.updateAndDraw();
            dendrite.imgMaxProj.setPositionWithoutUpdate(1, 1, 1);
            ImageProcessor ipProj = dendrite.imgMaxProj.getProcessor();
            ipProj.putPixel(V.getX(), V.getY(), 0);
            ipProj.resetMinAndMax();
            dendrite.imgMaxProj.updateAndDraw();
            //IJ.log("Remove pixel" + V.getX() +" "+ V.getY()+" "+V.getZ());
        }
        //IJ.log("list length " + trimListMaxLocal.size());
    }

    public int findNearnestPointMerge(int x, int y, int z) {
        DendriteViewer3D_ dendrite = DendriteViewer3D_.instance;
        double d, dMin;
        int index = -1;
        dMin = Integer.MAX_VALUE;
        // clic in stack image test only point at z position
        for (int i = 0; i < dendrite.trimListMaxLocal.size(); i++) {
            // find nearest point cliked in maxmerge image
            //z-1 shift Z between listmax and getZ position ??????
            if (dendrite.trimListMaxLocal.get(i).getZ() == z - 1) {
                d = Math.sqrt(Math.pow(dendrite.trimListMaxLocal.get(i).getX() - x, 2) + Math.pow(dendrite.trimListMaxLocal.get(i).getY() - y, 2));
                if (d < dMin) {
                    dMin = d;
                    index = i;
                    //IJ.log("index "+index+" distance "+dMin);
                }
            }
        }
        return (index);
    }

    public void editMaxLocalProj(int x, int y, boolean left, boolean leftShift, boolean leftCtrl) {
        DendriteViewer3D_ dendrite = DendriteViewer3D_.instance;
        // Add max local, find in the image stack max intensity near clicked pixel (3x3) in each Z
        if ((left) && (leftCtrl)) {
            Voxel V = dendrite.findMaxInt(x, y, dendrite.imgMaxMerge, 3);

            // check if max local intensity => min trimListMaxLocal
            // Add maxlocal in list
            dendrite.trimListMaxLocal.add(V);
            // set pixel in image stack and proj to max 16bit
            dendrite.imgMaxMerge.setPosition(1, V.getZ(), 1);
            ImageProcessor ipMerge = dendrite.imgMaxMerge.getProcessor();
            ipMerge.putPixel(V.getX(), V.getY(), dendrite.max16);
            dendrite.imgMaxMerge.updateAndDraw();
            dendrite.imgMaxProj.setPositionWithoutUpdate(1, 1, 1);
            ImageProcessor ipProj = dendrite.imgMaxProj.getProcessor();
            ipProj.putPixel(V.getX(), V.getY(), dendrite.max16);
            dendrite.imgMaxProj.updateAndDraw();
        } // Remove max local 
        else if ((left) && (leftShift)) {
            int index = findNearnestPointProj(x, y);
            //IJ.log("index "+index);
            Voxel V = dendrite.trimListMaxLocal.remove(index);
            // set pixel in image stack and proj to 0 in green channel
            dendrite.imgMaxMerge.setPositionWithoutUpdate(1, V.getZ() + 1, 1);
            ImageProcessor ipMerge = dendrite.imgMaxMerge.getProcessor();
            ipMerge.putPixel(V.getX(), V.getY(), 0);
            ipMerge.resetMinAndMax();
            dendrite.imgMaxMerge.updateAndDraw();
            dendrite.imgMaxProj.setPosition(1, 1, 1);
            ImageProcessor ipProj = dendrite.imgMaxProj.getProcessor();
            ipProj.putPixel(V.getX(), V.getY(), 0);
            ipProj.resetMinAndMax();
            dendrite.imgMaxProj.updateAndDraw();
        }
    }


    public int findNearnestPointProj(int x, int y) {
        DendriteViewer3D_ dendrite = DendriteViewer3D_.instance;
        double d, dMin;
        int index = -1;
        dMin = Integer.MAX_VALUE;

        for (int z = 1; z <= dendrite.imgMaxMerge.getNSlices(); z++) {
            for (int i = 0; i < dendrite.trimListMaxLocal.size(); i++) {
                if (dendrite.trimListMaxLocal.get(i).getZ() == z - 1) {
                    d = Math.sqrt(Math.pow(dendrite.trimListMaxLocal.get(i).getX() - x, 2) + Math.pow(dendrite.trimListMaxLocal.get(i).getY() - y, 2));
                    if (d <= dMin) {
                        dMin = d;
                        index = i;
                    }
                }
            }
        }
        return (index);
    }

    private DendriteViewer3D_ getDendrite() {
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mousePressed(MouseEvent e) {
        DendriteViewer3D_ dendrite = DendriteViewer3D_.instance;
        // if ctrl left or shift left button get cursor coordinates
        if (e.getButton() == MouseEvent.BUTTON1) {
            int z;
            Point pt;
            boolean left, leftShift, leftCtrl;
            ImagePlus img = WindowManager.getCurrentImage();
            // image stack merge selected 
            if (img == dendrite.imgMaxMerge) {
                pt = dendrite.imgMaxMerge.getCanvas().getCursorLoc();
                z = dendrite.imgMaxMerge.getZ();
                left = e.getButton() == MouseEvent.BUTTON1; // always true
                leftShift = e.isShiftDown();
                leftCtrl = e.isControlDown();
                // Edit max local list on merge image
                editMaxLocalMerge(pt.x, pt.y, z, left, leftShift, leftCtrl);
            } else if (img == dendrite.imgMaxProj) {
                pt = dendrite.imgMaxProj.getCanvas().getCursorLoc();
                left = e.getButton() == MouseEvent.BUTTON1; // always true
                leftShift = e.isShiftDown();
                leftCtrl = e.isControlDown();
                // Edit max local list on proj image
                editMaxLocalProj(pt.x, pt.y, left, leftShift, leftCtrl);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        ;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        ;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        ;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        ;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        ;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
