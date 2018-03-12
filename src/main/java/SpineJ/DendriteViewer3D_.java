package SpineJ;

import customnode.CustomTriangleMesh;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.Overlay;
import ij.gui.WaitForUserDialog;
import ij.io.OpenDialog;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.RGBStackMerge;
import ij.plugin.ZProjector;
import ij.process.AutoThresholder;
import ij.process.ImageProcessor;
import ij.process.StackConverter;
import ij3d.Content;
import ij3d.DefaultUniverse;
import ij3d.Image3DUniverse;
import ij3d.UniverseListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.media.j3d.View;
import org.scijava.vecmath.Color3f;
import org.scijava.vecmath.Point3f;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DSurface;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.image3d.processing.BinaryMorpho;
import mcib3d.image3d.processing.FastFilters3D;
import mcib3d.utils.ThreadUtil;
import Tools.SWCFileReader;
import Tools.Swc;
import Tools.Voxel;
import java.awt.Adjustable;
import java.util.List;

/**
 *
 * @author Brahim AMAIRI
 * @created 15 03 2010
 * @ modified by Philippe Mailly
 * @ 05 03 2015
 */
public class DendriteViewer3D_ implements PlugIn, MouseWheelListener, AdjustmentListener, WindowListener, UniverseListener {

    float closeDendXY = 10;     // binary close object
    float closeDendZ = 15;      // binary close objects
    float dilDendXY = 2;        // binary dilate
    float dilDendZ = 1;         // binary dilate
    int spineMaskFactor = 8;    // for masking max local outside dendrite
    int maxlocalXY = 4;         // parameter for max local filter
    int maxlocalZ = 5;          // parameter for max local filter
    Calibration cal = new Calibration();
    public double minDist, maxDist, minDistMap = 0;   // Minimum distances
    public double imgMapMin, imgMapMax;         // min and max values for distance map
    public int Zposition;           // slice position in imgMaxMerge
    // 
    public ImagePlus imageOrg;
    private ImagePlus imgDendBin;
    private ImagePlus imgMaxBin;
    public ImagePlus imgMaxMerge;
    public ImagePlus imgMaxProj;
    private ImageFloat imgDendMap;
    private ImagePlus imgEmpty;
    public ImagePlus mergeOrg, projOrg;
    private HashMap<Voxel, Double> distVoxels = new HashMap<Voxel, Double>();
    public List<Voxel> trimListMaxLocal;
    private List<Voxel> listMaxOrg;
    public int max16 = (int) Math.pow(2, 16) - 1;

// segmentation dialog
    public int globalBack = 1, localBack = 0, maxRadius = 10, sdValue = 2;
    public int SegMethod = Segment3DSpots.SEG_BLOCK;
    public int LocalMethod = Segment3DSpots.LOCAL_GAUSS;
    public boolean watershed = true;
    public ImagePlus imgSeg;
    private ImagePlus currentImage;

    // 3D Viewer
    public Objects3DPopulation spine3D = new Objects3DPopulation();
    public int spineSelected = 0;
    static public Image3DUniverse universe;
    Overlay over = new Overlay();
    static DendriteViewer3D_ instance;

    /**
     * Open dialog for image calibration parameters
     */
    private void calibDialog() {
        GenericDialog gdCal = new GenericDialog("Image calibration");
        gdCal.addNumericField("X, Y (micron) = ", 1, 3);
        gdCal.addNumericField("Z (micron) = ", 1, 3);
        gdCal.showDialog();
        cal.pixelWidth = gdCal.getNextNumber();
        cal.pixelHeight = cal.pixelWidth;
        cal.pixelDepth = gdCal.getNextNumber();
        cal.setUnit("micron");
        imageOrg.setCalibration(cal);
        imageOrg.updateAndDraw();
    }

    /**
     * create mask from swc file
     */
    private ImagePlus createDendriteMask(ImagePlus img, List<Swc> swc, int factor) {

        ObjectCreator3D oc3D = new ObjectCreator3D(img.getWidth(), img.getHeight(), img.getNSlices());
        oc3D.setCalibration(img.getCalibration());
        for (int i = 0; i < swc.size() - 1; i++) {
            double x1 = swc.get(i).getX();
            double x2 = swc.get(i + 1).getX();
            double y1 = swc.get(i).getY();
            double y2 = swc.get(i + 1).getY();
            double z1 = swc.get(i).getZ();
            double z2 = swc.get(i + 1).getZ();
            double r1 = swc.get(i).getR() * factor;
            double r2 = swc.get(i + 1).getR() * factor;
            oc3D.createEllipsoidUnit(x1, y1, z1, r1, r1, r1, 255, false);
            double dist = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
            int n = 0;
            // if the distance between point N and points N+1 is greather than (r1+r2)/2 place a sphere in between with (r1+r2)/2
            while (dist > (r1 + r2) / 2) {
                n++;
                x2 = (x1 + x2) / 2;
                y2 = (y1 + y2) / 2;
                z2 = (z1 + z2) / 2;
                r2 = (r1 + r2) / 2;
                oc3D.createEllipsoidUnit(x2, y2, z2, r2, r2, r2, 255, false);
                // place a symetric sphere
                if (n > 1) {
                    oc3D.createEllipsoidUnit(x2 + dist, y2 + dist, z2 + dist, r2, r2, r2, 255, false);
                }
                dist = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
            }
        }
        ImagePlus mask = new ImagePlus("", oc3D.getStack());
        mask.setCalibration(img.getCalibration());
        mask.getProcessor().resetMinAndMax();
        return (mask);
    }

    /**
     * Find max local variable in interface ? for kernel x=y=3,z=4 if 60x60 (see
     * calibration)
     */
    public List<Voxel> findMaxLocal(ImagePlus imgSpine, ImagePlus imgOrg) {
        ImageInt imgInt = ImageInt.wrap(imgSpine);
        ImageInt maxLocalStack = FastFilters3D.filterIntImage(imgInt, FastFilters3D.MAXLOCAL, maxlocalXY, maxlocalXY, maxlocalZ, ThreadUtil.getNbCpus(), true);
        ImagePlus imgLocalMax = maxLocalStack.getImagePlus();
        imgLocalMax.setCalibration(imgOrg.getCalibration());
        // get max local
        List<Voxel> listMaxLocal = new ArrayList();
        double pixelGL;
        for (int z = 1; z <= imgLocalMax.getNSlices(); z++) {
            for (int y = 0; y < imgLocalMax.getHeight(); y++) {
                for (int x = 0; x < imgLocalMax.getWidth(); x++) {
                    imgLocalMax.setZ(z);
                    pixelGL = imgLocalMax.getProcessor().getPixel(x, y);
                    if (pixelGL != 0) {                    
                        listMaxLocal.add(new Voxel(x, y, z, (int) pixelGL));
                    }
                }
            }
        }
        return (listMaxLocal);
    }

    /**
     * Close dendrite mask
     */
    public ImagePlus binaryClose(ImagePlus imgDend) {
        ImageInt imgInt = ImageInt.wrap(imgDend);
        ImageHandler imgRes = BinaryMorpho.binaryCloseMultilabel(imgInt, closeDendXY, closeDendZ, ThreadUtil.getNbCpus());
        // ajouter un dilate pour bien remplir mais problème de décalage de l'image ??
        //ImageInt imgInt2 = ImageInt.wrap(dendMask);
        //ImageHandler imgRes2 = BinaryMorpho.binaryDilateMultilabel(imgInt2, dilDendXY, dilDendZ, ThreadUtil.getNbCpus());
        ImagePlus dendMaskClose = imgRes.getImagePlus();
        dendMaskClose.setTitle("dendrite Mask Close labels");
        dendMaskClose.setCalibration(imgDend.getCalibration());
        dendMaskClose.getProcessor().resetMinAndMax();
        //dendMaskClose.show();
        return (dendMaskClose);
    }

    /**
     * merge channels in composite image
     */
    private ImagePlus makeRGB(ImageStack red, ImageStack green, ImageStack blue, String title) {
        ImageStack imgRGB = RGBStackMerge.mergeStacks(red, green, blue, true);
        ImagePlus imgOut = new ImagePlus(title, imgRGB);
        imgOut.setCalibration(cal);
        imgOut.setTitle(title);
        return (imgOut);
    }

    /**
     * merge channels in composite image
     */
    public ImagePlus makeComposite(ImagePlus[] imgs, String title) {
        ImagePlus img = RGBStackMerge.mergeChannels(imgs, true);
        img.setTitle(title);
        img.setCalibration(cal);
        return (img);
    }

    /**
     * Fill imgOrg dendrite in black with dendrite mask
     */
    public ImagePlus fillDendrite(ImagePlus imgOrg, ImagePlus imgDend) {
        ImageCalculator dendCal = new ImageCalculator();
        ImageStack dendFillStack = new ImageStack(imgDend.getWidth(), imgDend.getHeight());
        for (int i = 1; i <= imgDend.getNSlices(); i++) {
            imgOrg.setSlice(i);
            imgDend.setSlice(i);
            imgDend.getProcessor().invert();
            dendFillStack.addSlice("", dendCal.run("Multiply create", imgDend, imgOrg).getProcessor());
        }
        ImagePlus imgFill = new ImagePlus("Filled dendrite", dendFillStack);
        //dendFill.show();
        return (imgFill);
    }

    /**
     * create new dendrite mask with radius * factor (in interface?) to fill in
     * black outside the spines space
     */
    public ImagePlus fillOutsideSine(ImagePlus imgOrg, ImagePlus imgDend, List<Swc> swc, int factor) {
        ImagePlus spineMask = createDendriteMask(imgOrg, swc, spineMaskFactor);
        //spineMask.setTitle("Spine mask");
        //spineMask.show();
        // divide mask by 255
        for (int i = 1; i <= spineMask.getNSlices(); i++) {
            spineMask.setSlice(i);
            spineMask.getProcessor().multiply(0.003921569);
        }
        // multiply mask with image filled dendrite
        ImageStack spineFillStack = new ImageStack(spineMask.getWidth(), spineMask.getHeight());
        ImageCalculator dendCal = new ImageCalculator();
        for (int i = 1; i <= spineMask.getNSlices(); i++) {
            spineMask.setSlice(i);
            imgDend.setSlice(i);
            spineFillStack.addSlice("", dendCal.run("Multiply create", spineMask, imgDend).getProcessor());
        }
        ImagePlus imgSpineDendFill = new ImagePlus("Filled spine and dendrite", spineFillStack);
        imgSpineDendFill.setCalibration(imgOrg.getCalibration());
        return (imgSpineDendFill);
    }

    /**
     * select max local up to the mean
     *
     * @param arg0
     */
    private List<Voxel> selectIntMaxLocal(List<Voxel> max, ImagePlus imgMaxLocal) {

        List<Voxel> selectedMaxLocal = new ArrayList<Voxel>();

        // find mean of the max local
        IJ.showStatus("trim max local on voxel intensity ...");
        double sumMax = 0;
        for (Voxel voxelMax : max) {
            sumMax += voxelMax.getVal();
        }
        double maxLocalValue;
        for (Voxel max1 : max) {
            maxLocalValue = max1.getVal();
            // if max > mean keep max
            if (maxLocalValue > sumMax / max.size()) {
                selectedMaxLocal.add(max1);
            } // set the voxel to 0 in image maxLocal
            else {
                imgMaxLocal.getStack().setVoxel(max1.getX(), max1.getY(), max1.getZ(), 0);
            }
        }
        return (selectedMaxLocal);
    }

    private Objects3DPopulation getPopFromImage(ImagePlus img) {
        // label binary images first
        ImageLabeller labeller = new ImageLabeller();
        ImageInt labels = labeller.getLabels(ImageHandler.wrap(img));
        labels.setCalibration(img.getCalibration());
        Objects3DPopulation pop = new Objects3DPopulation(labels);
        return pop;
    }

    private HashMap<Voxel, Double> computeDistances(List<Voxel> voxels) {
        HashMap<Voxel, Double> tmp = new HashMap<Voxel, Double>();

        Objects3DPopulation popDend = getPopFromImage(imgDendBin);
        for (Voxel V : voxels) {
            double minDist0 = Double.POSITIVE_INFINITY;
            for (int j = 0; j < popDend.getNbObjects(); j++) {
                double distCenterBorder = popDend.getObject(j).distPixelBorderUnit(V.getX(), V.getY(), V.getZ());
                if (distCenterBorder <= minDist0) {
                    minDist0 = distCenterBorder;
                }
            }
            tmp.put(V, minDist0);
        }

        return tmp;
    }

    /**
     * binarize image
     *
     * @param img
     * @param invert
     * @return
     */
    private ImagePlus binarize(ImagePlus img, boolean invert) {

        if (invert) { // invert dendrite mask
            for (int s = 1; s <= img.getNSlices(); s++) {
                img.setSlice(s);
                img.getProcessor().invert();
            }
            img.updateAndDraw();
        }

        //  Threshold and binarize
        AutoThresholder at = new AutoThresholder();
        int th = at.getThreshold(AutoThresholder.Method.Yen, img.getStatistics().histogram);
        ImageHandler ha = ImageHandler.wrap(img);
        ImageByte bin = ha.thresholdAboveExclusive(th);
        ImagePlus imgBin = bin.getImagePlus();
        imgBin.setCalibration(img.getCalibration());
        imgBin.updateAndDraw();
        return (imgBin);
    }

    // Compute image map of dendrite
    private ImageFloat createImgMap(ImagePlus img) {
        ImageInt imgInt = ImageInt.wrap(img);
        ImageFloat edtDendrite = EDT.run(imgInt, 128, (float) cal.pixelWidth, (float) cal.pixelDepth, false, 0);
        // find min and max distMap
        imgMapMin = edtDendrite.getMin();
        imgMapMax = edtDendrite.getMax();
        return (edtDendrite);
    }

    /**
     * select max local if distance from the dendrite > dist write corresponding
     * pixel in merged images
     *
     * @return max
     */
    public List<Voxel> selectDistMaxLocal() { // afficher dist min et max ?

        List<Voxel> max = new ArrayList<Voxel>();
        int chOrg = imgMaxMerge.getC();
        int zOrg = imgMaxMerge.getZ();

        for (Voxel V : listMaxOrg) {
            double distdd = distVoxels.get(V);
            double distMd = imgDendMap.getPixel(V.getX(), V.getY(), V.getZ());

            // put the pixel at max 16bit in green channel if distance to dendrite > dist and distance Map > distMap
            // else zero in red channel
            if ((Math.abs(distdd) >= minDist) && (Math.abs(distMd) >= minDistMap)) {
               // IJ.log("dist = " + minDist + " distMap = " + minDistMap + " min dist " + distdd + " min distMap " + distMd +" add point:  " + V.getX() + "," + V.getY() + "," + V.getZ());

                // put max 16bit in green channel in projection
                imgMaxProj.setPositionWithoutUpdate(2, 1, 1);
                ImageProcessor ipProj = imgMaxProj.getProcessor();
                ipProj.putPixel(V.getX(), V.getY(), max16);

                // put 0 in red channel in projection
                imgMaxProj.setPositionWithoutUpdate(1, 1, 1);
                ipProj.putPixel(V.getX(), V.getY(), 0);

                // put max 16bit  in green in stack merge
                imgMaxMerge.setPositionWithoutUpdate(2, V.getZ() + 1, 1);
                ImageProcessor ipMerge = imgMaxMerge.getProcessor();
                ipMerge.putPixel(V.getX(), V.getY(), max16);

                // put 0 in red channel stack merge
                imgMaxMerge.setPositionWithoutUpdate(1, V.getZ() + 1, 1);
                ipMerge.putPixel(V.getX(), V.getY(), 0);
                max.add(V);
            } else {
                // put the pixel in red and remove in green
                //IJ.log("dist = " + minDist + " distMap = " + minDistMap + " min dist " + distdd + " min distMap " + distMd + " remove point:  " + V.getX() + "," + V.getY() + "," + V.getZ());
                // put max 16bit in red channel in projection
                imgMaxProj.setPositionWithoutUpdate(1, 1, 1);
                ImageProcessor ipProj = imgMaxProj.getProcessor();
                ipProj.putPixel(V.getX(), V.getY(), max16);

                // put 0 in green channel in projection
                imgMaxProj.setPositionWithoutUpdate(2, 1, 1);
                ipProj.putPixel(V.getX(), V.getY(), 0);

                // put max 16 bit in red channel in stack merge
                imgMaxMerge.setPositionWithoutUpdate(1, V.getZ() + 1, 1);
                ImageProcessor ipMerge = imgMaxMerge.getProcessor();
                ipMerge.putPixel(V.getX(), V.getY(), max16);

//                // put 0 in green channel stack merge
                imgMaxMerge.setPositionWithoutUpdate(2, V.getZ() + 1, 1);
                ipMerge.putPixel(V.getX(), V.getY(), 0);
            }
        }
        imgMaxMerge.setC(chOrg);
        imgMaxMerge.setZ(Zposition);
        imgMaxMerge.updateAndDraw();
        imgMaxProj.updateAndDraw();
        //IJ.log("new liste size = " + max.size());
        return (max);
    }

    /**
     * Remove the red channel
     */
    public void removeRedChannel() {

        int Z = imgMaxMerge.getZ();
        ImagePlus[] imgChannel = ChannelSplitter.split(imgMaxProj);
        // remove red channel
        imgChannel[0] = null;
        imgMaxProj.hide();
        imgMaxProj = makeComposite(imgChannel, imgMaxProj.getTitle());
        imgMaxProj.updateAndDraw();
        imgMaxProj.show();

        imgMaxMerge.hide();
        imgChannel = ChannelSplitter.split(imgMaxMerge);
        imgChannel[0] = null;
        imgMaxMerge = makeComposite(imgChannel, imgMaxMerge.getTitle());
        imgMaxMerge.updateAndDraw();
        imgMaxMerge.setZ(Z);
        imgMaxMerge.show();
    }

    public void spineSegmentation(ImagePlus imgOrg) {
        IJ.showStatus("Segmenting Spines... ");
        ImagePlus[] imgSeed = ChannelSplitter.split(imgMaxMerge);
        ImageHandler imgSeeds, imhSpots;
        localBack = 0;// means automatic
        imgSeeds = ImageHandler.wrap(imgSeed[0]);
        imhSpots = ImageHandler.wrap(imgSeed[1]);
        Segment3DSpots seg = new Segment3DSpots(imhSpots, imgSeeds);
        seg.setSeedsThreshold(globalBack);
        seg.setLocalThreshold(localBack);
        seg.setWatershed(watershed);
        seg.setVolumeMin(0);
        seg.setVolumeMax(Integer.MAX_VALUE);
        seg.setMethodLocal(Segment3DSpots.LOCAL_GAUSS);
        seg.setGaussPc(sdValue);
        seg.setGaussMaxr(maxRadius);
        seg.setMethodSeg(SegMethod);
        seg.segmentAll();
        ImageHandler imgSpine = seg.getLabelImage();
        ImagePlus imgSegmented = new ImagePlus("Segmented spines", imgSpine.getImageStack());
        imgSegmented.setCalibration(cal);

        spine3D = getPopFromImage(imgSegmented);
        // enhance contrast
        for (int z = 1; z <= imgSegmented.getNSlices(); z++) {
            imgSegmented.setZ(z);
            IJ.run(imgSegmented, "Enhance Contrast", "saturated=0.35");
            imgSegmented.updateAndDraw();
        }
        IJ.run(imgSegmented, "3-3-2 RGB", "");
        ImagePlus[] segArray = {imgSegmented, imageOrg};
        imgSeg = makeComposite(segArray, "Segmented spines");
        imgSeg.setZ(Math.round(imgSeg.getNSlices() / 2));
        imgSeg.show();
        show3DViewer(imgSegmented);
        WindowManager.getWindow("Log").hide();
    }
    
    /**
     * Add object or select/deselect in 3D Viewer
     * @param obj
     * @param col color
     * @param select select object
     * @param add add object
     */
    public void add3DViewer(Object3D obj, Color3f col, boolean select, boolean add) {

        // add new object in universe
        if (!select) {
            List<Point3f> l = null;
            Object3DSurface surf = obj.getObject3DSurface();
            l = surf.getSurfaceTrianglesUnit(false);
            CustomTriangleMesh tm = new CustomTriangleMesh(l);
            Content surface = universe.addCustomMesh(tm, obj.getName());
            surface.setColor(col);
            surface.setLocked(true);
            universe.setInteractiveBehavior(new CustomBehavior(surface));
        } 
        // already exists selection
        else {
            if (add) {
                //System.out.println(obj.getName() + " selected");
                Content surface = universe.getContent(obj.getName());
                surface.setSelected(true);
                surface.showBoundingBox(true);
            } 
            else {
                //System.out.println(obj.getName() + " unselected");
                Content surface = universe.getContent(obj.getName());
                surface.setSelected(false);
                surface.showBoundingBox(false);
            }
        }

    }

    /**
     * Display 3D viewer to edit spines
     *
     * @param imgSpine objects image
     */
    public void show3DViewer(ImagePlus imgSpine) {
        boolean[] channels = {true, true, true};
        ImagePlus imgOrg8bit = imageOrg.duplicate();
        Color3f color3DGreen = new Color3f(Color.green);

        // resample 8bit imageOrg duplicate for 3D Viewer and enhance contrast
        new StackConverter(imgOrg8bit).convertToGray8();
        for (int z = 1; z <= imgOrg8bit.getNSlices(); z++) {
            imgOrg8bit.setZ(z);
            IJ.run(imgOrg8bit, "Enhance Contrast", "saturated=0.35");
            imgOrg8bit.updateAndDraw();
        }

        // add img8bit in 3DViewer
        IJ.setTool("hand");
        universe = new Image3DUniverse();
        Content imageOrgVol = universe.addVoltex(imgOrg8bit, color3DGreen, "Tree", 0, channels, 1);
        imageOrgVol.setLocked(true);
        imageOrgVol.setSaturatedVolumeRendering(true);
        // discard imgOrg8bit
        imgOrg8bit.flush();

        //resample to RGB imgSeg
        ImagePlus imgRGB = imgSpine.duplicate();
        new StackConverter(imgRGB).convertToRGB();

        // Add spine objects in 3DViewer
        for (int i = 0; i < spine3D.getNbObjects(); i++) {
            Object3D obj = spine3D.getObject(i);
            obj.setComment(null);
            obj.setName("Obj-"+i);
            // find objets color
            imgRGB.setZ((int) obj.getCenterZ() + 1);
            int[] pixelValue = imgRGB.getPixel((int) obj.getCenterX(), (int) obj.getCenterY());
            Color3f col = new Color3f(pixelValue[0] / 255f, pixelValue[1] / 255f, pixelValue[2] / 255f);
            add3DViewer(obj, col, false, true);
        }
        // Discarde imgRGB
        imgRGB.flush();
        universe.show();
        imgSeg.setOverlay(over);
        IJ.showStatus("3D universe done");

        // Display Dialog for Adding, Deleting, Merging spines in 3D viewer
        Edit3dSpineFrame dialogSpine3DEdit = new Edit3dSpineFrame();
        dialogSpine3DEdit.setDendrite(this);
        dialogSpine3DEdit.setVisible(true);
    }

    /**
     * Draw rois on 2D image
     *
     * @param name object3D name
     * @param draw true draw roi, false erase roi
     * @param clear clear inside roi if erase
     *        fill with object color if draw
     */
    public void drawRois(String name, boolean draw) {
        imgSeg.setC(1);
        this.registerActiveImage();
        // get zmin and zmax of selected object
        Object3D obj = spine3D.getObjectByName(name);
        int zmin = obj.getZmin();
        int zmax = obj.getZmax();
        int middle = (int) (0.5 * zmin + 0.5 * zmax);
        imgSeg.setZ(middle+1);
        if (draw) {
            // draw roi for selected objet
            System.out.println("Draw roi z= " + (middle) + "spine " + name);
            over.add(obj.getConvexPolygonRoi(middle));
            imgSeg.updateAndDraw();
        } 
        else {
            System.out.println("Delete roi z= " + (middle) + "spine " + name);
            over.remove(obj.getConvexPolygonRoi(middle));
            imgSeg.updateAndDraw();
        }
    }

    /**
     * Update rois on 2D image
     */
    public void updateRois() {
        int sl = imgSeg.getZ()-1;
        imgSeg.setC(1);
        over.clear();
        
        for (int i = 0; i < spine3D.getNbObjects(); i++) {
            Object3D obj = spine3D.getObject(i);
            if (obj.getComment() != null) {
                //System.out.println("spine selected "+i);
                int zmin = obj.getZmin();
                int zmax = obj.getZmax();
                if ((sl >= zmin) && (sl <= zmax)) {
                    over.add(obj.getConvexPolygonRoi(sl));
                    imgSeg.updateAndDraw();
                    //System.out.println("obj "+i+" add roi zmin "+ zmin+ " zmax "+zmax+ " sl = "+sl);  
                }
            }
        }
    }

    
    /**
     * Deselect all spines in 3D viewer Undraw all rois
     */
    public void deSelectAllSpines() {
        for (int i = 0; i < spine3D.getNbObjects(); i++) {
            spine3D.getObject(i).setComment(null);
            add3DViewer(spine3D.getObject(i), null, true, false);
        }
        imgSeg.setC(1);
        over.clear();
        imgSeg.updateAndDraw();
    }

    /**
     * @param name name of the selected 3D objet Show objects in 2D image
     * @param x point clicked
     * @param y point clicked
     */
    public void spineSelected(String name, int x, int y) {
        if ("Tree".equals(name)) {
            addSpine(x, y);   
        }
        
        int spineIndex = spine3D.getIndexFromName(name);
        Object3D obj = spine3D.getObject(spineIndex);
        // spine allready selected
        if (obj.getComment() != null) {
            obj.setComment(null);
            add3DViewer(obj, null, true, false);
            drawRois(name, false);
            updateRois();
            spineSelected--;
            System.out.println("Deselected spine " + name);
        } else {
            obj.setComment("selected");
            add3DViewer(obj, null, true, true);
            drawRois(name, true);
            updateRois();
            spineSelected++;
            System.out.println("Selected spine " + name);
        }
    }
    /** Remove spine in spine3D population and 
     *  in 3D Viewer
     */
    public void removeSpine(String name) {
        int index = spine3D.getIndexFromName(name);
        // remove roi
        over.clear();
        // Remove spine from spine pop
        spine3D.removeObject(index);
        spineSelected--;
        //remove spine in 3D Viewer
        universe.removeContent(name);
    }
     public Voxel findMaxInt(int xp, int yp, ImagePlus img, int channel) {
        double maxInt = img.getProcessor().getMin();
        Voxel V = new Voxel(0, 0, 0, 0);
        // scan all z in image for pixel max intensity arround 3x3

        for (int y = yp - 1; y <= yp + 1; y++) {
            for (int x = xp - 1; x <= xp + 1; x++) {
                for (int z = 1; z <= img.getNSlices(); z++) {
                   img.setPositionWithoutUpdate(channel, z, 1);
                    if (img.getProcessor().getPixel(x, y) > maxInt) {
                        maxInt = img.getProcessor().getPixel(x, y);
                        V.setX(x);
                        V.setY(y);
                        V.setZ(z);
                        V.setVal(max16);
                        System.out.println("z = "+z+" int="+maxInt);
                    }
                }
            }
        }
        return (V);
    }
    /**
     *  Add spine in 3D viewer remove max local in 2D projection and
     * 3D image
     * @param x point coordinates in 3D View
     * @param y point coordinates in 3D View
     * 
     */
    public void addSpine(int x, int y) {
        
        // find max intensity in Z stack image
        System.out.println("x, y "+x+", "+y);
        Voxel maxVol = findMaxInt(x, y, imgMaxMerge,2);
        trimListMaxLocal.add(maxVol);
        ImagePlus[] imgs = new ChannelSplitter().split(imgMaxMerge);
        ImageHandler imhSeeds, imhSpots;
        // Fill image with zero and add maxlocal
        imgs[0].getProcessor().setColor(Color.black);
        for (int z = 1; z <= imgs[0].getNSlices(); z++) {
            imgs[0].setZ(z);
            imgs[0].getProcessor().fill();
            if (z == maxVol.getZ())
                imgs[0].getProcessor().putPixelValue(maxVol.getX(), maxVol.getY(), maxVol.getVal());
        }
        
        // 3D segmentation
        imgs[0].setZ(maxVol.getZ());
        imgs[0].updateAndDraw();
        imgs[0].show();
        new WaitForUserDialog("test").show();
        imhSeeds = ImageHandler.wrap(imgs[0]);
        imhSpots = ImageHandler.wrap(imgs[1]);
        Segment3DSpots seg = new Segment3DSpots(imhSpots, imhSeeds);
        seg.setSeedsThreshold(globalBack);
        seg.setLocalThreshold(localBack);
        seg.setWatershed(watershed);
        seg.setVolumeMin(0);
        seg.setVolumeMax(Integer.MAX_VALUE);
        seg.setMethodLocal(Segment3DSpots.LOCAL_GAUSS);
        seg.setGaussPc(sdValue);
        seg.setGaussMaxr(maxRadius);
        seg.setMethodSeg(SegMethod);
        seg.segmentAll();
        ImageHandler imgSpine = seg.getLabelImage();
        ImagePlus imgSegmented = new ImagePlus("Segmented spine", imgSpine.getImageStack());
        imgSegmented.setCalibration(cal);
        Objects3DPopulation newSpine3D = getPopFromImage(imgSegmented);
        //add spine to sine3D population
        newSpine3D.getObject(0).setComment(null);
        newSpine3D.getObject(0).setName("Obj-" +spine3D.getNbObjects());
        Color3f col = new Color3f(Color.cyan);
        spine3D.addObject(newSpine3D.getObject(0));
        // Add new spine in 3D Viewer
        add3DViewer(newSpine3D.getObject(0), col, false, false);
        // Select new spine in 3D Viewer
        add3DViewer(newSpine3D.getObject(0), col, true, true);
        spineSelected++;
        // Add new spine in imgSeg
        // fill voxel with color object for object in 3D segmentation image
        ArrayList<Voxel3D> voxelObj = newSpine3D.getObject(0).getVoxels();
        imgSeg.setC(1);
        int currentZ = imgSeg.getZ();
        for (int i = 0; i < voxelObj.size(); i++) {
            imgSeg.setZ((int)voxelObj.get(i).z + 1);
            imgSeg.getProcessor().putPixel((int)voxelObj.get(i).x, (int)voxelObj.get(i).y, (int)voxelObj.get(i).getValue());
            imgSeg.updateAndDraw();
        }       
        imgSeg.setZ(currentZ);
        // Draw ROI in segmented spines image
        drawRois(newSpine3D.getObject(0).getName(), true);
        imgSegmented.flush();
    }
    
    
    /**
     * Delete selected spine in 3D viewer, max local in 2D projection and
     * 3D image
     *
     * @param name of spineSelected
     */
    public void deleteSpine(String name) {
        // keep zoom and view
        DefaultUniverse.GlobalTransform zoom = new DefaultUniverse.GlobalTransform();
        universe.getGlobalTransform(zoom);
        int spineIndex = spine3D.getIndexFromName(name);
        
        // fill voxel with zero for object in 3D segmentation image
        ArrayList<Voxel3D> voxelObj = spine3D.getObject(spineIndex).getVoxels();
        imgSeg.setC(1);
        int currentZ = imgSeg.getZ();
        for (int i = 0; i < voxelObj.size(); i++) {
            imgSeg.setZ((int)voxelObj.get(i).z + 1);
            imgSeg.getProcessor().putPixel((int)voxelObj.get(i).x, (int)voxelObj.get(i).y, 0);
            imgSeg.updateAndDraw();
        }       
        imgSeg.setZ(currentZ);
        
        // Find coord of max intensity point in object for imgMaxMerge
        ImagePlus[] img = ChannelSplitter.split(imgMaxMerge);
        Voxel3D pixelMax = spine3D.getObject(spineIndex).getPixelMax(ImageHandler.wrap(img[0]));
        int x = (int) pixelMax.x;
        int y = (int) pixelMax.y;
        int z = (int) pixelMax.z;

        // Find max local in trimList and remove
        for (int i = 0; i < trimListMaxLocal.size(); i++) {
            int xx = trimListMaxLocal.get(i).getX();
            int yy = trimListMaxLocal.get(i).getY();
            int zz = trimListMaxLocal.get(i).getZ();
            if (xx == x && yy == y && zz == z) {
                //System.out.println("max local found at " + xx + "," + yy + "," + zz);
                trimListMaxLocal.remove(i);
                // set pixel in image stack and proj to 0 in green channel
                imgMaxMerge.setPosition(1, z+1, 1);
                ImageProcessor ipMerge = imgMaxMerge.getProcessor();
                ipMerge.putPixel(x, y, 0);
                imgMaxMerge.updateAndDraw();
                imgMaxProj.setPosition(1, 1, 1);
                ImageProcessor ipProj = imgMaxProj.getProcessor();
                ipProj.putPixel(x, y, 0);
                imgMaxProj.updateAndDraw();
                break;
            }
        }
        
        // remove object in 3d Viewer
        removeSpine(name);
        universe.setGlobalTransform(zoom);
    }

    private void removeScrollListener(ImagePlus img, AdjustmentListener al, MouseWheelListener ml) {
        //from Fiji code
        // TODO Find author...
        if ((img.getWindow() != null) && (img.getWindow().getComponents() != null)) {
            for (Component c : img.getWindow().getComponents()) {
                if (c instanceof Scrollbar) {
                    ((Adjustable) c).removeAdjustmentListener(al);
                } else if (c instanceof Container) {
                    for (Component c2 : ((Container) c).getComponents()) {
                        if (c2 instanceof Scrollbar) {
                            ((Adjustable) c2).removeAdjustmentListener(al);
                        }
                    }
                }
            }
            img.getWindow().removeMouseWheelListener(ml);
        }
    }

    private void addScrollListener(ImagePlus img, AdjustmentListener al, MouseWheelListener ml) {
        //from Fiji code
        // TODO Find author...
        for (Component c : img.getWindow().getComponents()) {
            if (c instanceof Scrollbar) {
                ((Adjustable) c).addAdjustmentListener(al);
            } else if (c instanceof Container) {
                for (Component c2 : ((Container) c).getComponents()) {
                    if (c2 instanceof Scrollbar) {
                        ((Adjustable) c2).addAdjustmentListener(al);
                    }
                }
            }
        }
        img.getWindow().addMouseWheelListener(ml);
    }

    private void registerActiveImage() {
        ImagePlus activeImage = imgSeg;
        //System.out.println("Register " + activeImage + " " + currentImage);
        if (activeImage != null && activeImage.getProcessor() != null && activeImage.getImageStackSize() > 1) {
            if (currentImage != null && currentImage.getWindow() != null && currentImage != activeImage) {
                //System.out.println("remove listener:"+currentImage.getTitle());
                removeScrollListener(currentImage, this, this);
                currentImage.killRoi();
                currentImage.updateAndDraw();
                currentImage = null;
            }
            if (currentImage != activeImage) {
                //System.out.println("add listener:"+activeImage.getTitle());
                addScrollListener(activeImage, this, this);
                this.currentImage = activeImage;
            }
        }
    }

    public void run(String arg0) {
        instance = this;

        imageOrg = WindowManager.getCurrentImage();
        if (imageOrg == null) {
            IJ.error("No image found !!!\n Open an image first");
            return;
        }

        // check if image is calibrated
        cal = imageOrg.getCalibration();
        if (cal.getUnit().startsWith("pixel")) {
            calibDialog();
        }
        
        // find swc file
        OpenDialog dialog = new OpenDialog("Open Swc file", "");
        String path = dialog.getDirectory() + dialog.getFileName();
        if (path.equals("nullnull")) {
            return;
        }
        if (path.equals("nullnull") || !path.endsWith(".swc")) {
            IJ.error("The file is not a swc file !!!");
            return;
        }

        // read coordinates from swc file
        SWCFileReader dv = new SWCFileReader(path);
        List<Swc> listSwc = dv.getListSwc();
        if (listSwc == null) {
            return;
        }

        // create mask for dendrite
        IJ.showStatus("Compute dendrite mask .");
        ImagePlus dendMask = createDendriteMask(imageOrg, listSwc, 1);
        dendMask.setTitle("Dendrite Mask");

        // binary close labels x,y = 10, z = 15 put in dialog ?
        IJ.showStatus("Compute dendrite mask ..");
        ImagePlus dendMaskClose = binaryClose(dendMask);

        // create red channel for removed max local (may be not show) !!!!!
        imgEmpty = NewImage.createImage("", imageOrg.getWidth(), imageOrg.getHeight(), imageOrg.getNSlices(), 16, NewImage.FILL_BLACK);

        // Fill imageOrg dendrite in black with dendMaskClose
        ImagePlus dendFill = fillDendrite(imageOrg, dendMaskClose);

        //dendMask.close();
        dendMask.flush();
        dendMaskClose.close();

        // Fill outside spines space    
        ImagePlus spineDendFill = fillOutsideSine(imageOrg, dendFill, listSwc, spineMaskFactor);

        dendFill.flush();
        dendFill.close();

        // find maxlocal
        List<Voxel> listMaxLocal = findMaxLocal(spineDendFill, imageOrg);

        // create an empty stack filled with black
        ImagePlus maxLocalStack = NewImage.createImage("", imageOrg.getWidth(), imageOrg.getHeight(), imageOrg.getNSlices(), 16, NewImage.FILL_BLACK);
        maxLocalStack.setTitle("Max local stack");
        // add max local in stack
        for (Voxel max : listMaxLocal) {
            IJ.showStatus("Creating max local image ...");
            maxLocalStack.getStack().setVoxel(max.getX(), max.getY(), max.getZ(), max16);
        }

        // Trim max local on intensity and update maxLocalStack
        trimListMaxLocal = selectIntMaxLocal(listMaxLocal, maxLocalStack);

        // Keep max local original list
        listMaxOrg = new ArrayList<Voxel>(trimListMaxLocal);

        // merge green imgMaxLocal and spineDendFill blue -> imageMerge + red for removed max local
        // on green and red channels put brightness contrast set 0 - 1
        for (int s = 1; s <= imgEmpty.getNSlices(); s++) {
            imgEmpty.setZ(s);
            imgEmpty.getProcessor().setMinAndMax(0, 1);
            maxLocalStack.setZ(s);
            maxLocalStack.getProcessor().setMinAndMax(0, 1);
        }

        // create image stack
        ImagePlus[] imageArray = {imgEmpty, maxLocalStack, spineDendFill};
        imgMaxMerge = makeComposite(imageArray, imageOrg.getTitle() + "(Max local merge)");
        Zposition = Math.round(imgMaxMerge.getNSlices() / 2);
        // enhance contrast for blue channel 
        imgMaxMerge.setC(3);
        imgMaxMerge.setZ(Zposition);
        IJ.run(imgMaxMerge, "Enhance Contrast", "saturated=0.35 normalize");
        imgMaxMerge.updateAndDraw();
        imgMaxMerge.setC(1);
        imgMaxMerge.show();

        // create image max projection
        for (int c = 0; c < imageArray.length; c++) {
            ZProjector proj = new ZProjector(imageArray[c]);
            proj.setMethod(proj.MAX_METHOD);
            proj.doProjection();
            imageArray[c] = proj.getProjection();
            if ((c == 0) || (c == 1)) {
                imageArray[c].getProcessor().setMinAndMax(0, 1);
            }
        }
        imgMaxProj = makeComposite(imageArray, imageOrg.getTitle() + "(Max local projection)");

        // enhance contrast for blue channel
        imgMaxProj.setC(3);
        IJ.run(imgMaxProj, "Enhance Contrast", "saturated=0.35 normalize");
        imgMaxProj.setC(2);
        imgMaxProj.updateAndDraw();
        imgMaxProj.show();

        // compute image map from spineDendFill
        imgDendMap = createImgMap(spineDendFill);
        imgMapMin = imgDendMap.getMin();
        imgMapMax = imgDendMap.getMax();

        spineDendFill.flush();
        spineDendFill.close();
        imageOrg.hide();

        // binarize dendrite mask and  maxLocalStack
        imgDendBin = binarize(dendMaskClose, true);
        imgMaxBin = binarize(maxLocalStack, false);
        maxLocalStack.flush();
        dendMaskClose.flush();

        // compute distances from max local to dendrite
        distVoxels = computeDistances(trimListMaxLocal);

        // find min and max distance to show in dialog box
        minDist = Double.POSITIVE_INFINITY;
        maxDist = 0;
        for (Voxel V : distVoxels.keySet()) {
            if (distVoxels.get(V) <= minDist) {
                minDist = distVoxels.get(V);
            } else if (distVoxels.get(V) >= maxDist) {
                maxDist = distVoxels.get(V);
            }

        }

        // keep original images
        mergeOrg = imgMaxMerge.duplicate();
        mergeOrg.setTitle(imgMaxMerge.getTitle());
        projOrg = imgMaxProj.duplicate();
        projOrg.setTitle(imgMaxProj.getTitle());

        // trim max local inf to dist from dendrite mask
        distanceJDialog dialogdist = new distanceJDialog(this, new javax.swing.JFrame(), true);
        dialogdist.setDendrite(this);
        dialogdist.setVisible(true);
    }

    public void transformationStarted(View view) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void transformationUpdated(View view) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void transformationFinished(View view) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void contentAdded(Content cntnt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    public void contentRemoved(Content cntnt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void contentChanged(Content cntnt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    public void canvasResized() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    public void universeClosed() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void contentSelected(Content cntnt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void mouseWheelMoved(MouseWheelEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void adjustmentValueChanged(AdjustmentEvent ae) {
        this.updateRois();

    }

    
    public void windowOpened(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void windowClosing(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void windowClosed(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void windowIconified(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void windowDeiconified(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void windowActivated(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void windowDeactivated(WindowEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void transformationStarted(org.scijava.java3d.View arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void transformationUpdated(org.scijava.java3d.View arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    public void transformationFinished(org.scijava.java3d.View arg0) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
