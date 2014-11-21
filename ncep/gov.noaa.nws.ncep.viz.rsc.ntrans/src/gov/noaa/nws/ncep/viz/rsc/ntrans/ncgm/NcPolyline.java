package gov.noaa.nws.ncep.viz.rsc.ntrans.ncgm;

import gov.noaa.nws.ncep.viz.rsc.ntrans.jcgm.Polyline;
import gov.noaa.nws.ncep.viz.rsc.ntrans.rsc.ImageBuilder;
import gov.noaa.nws.ncep.viz.rsc.ntrans.rsc.ImageBuilder.WireframeKey;

import java.awt.geom.PathIterator;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.viz.core.IGraphicsTarget;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.IWireframeShape;
import com.raytheon.uf.viz.core.drawables.PaintProperties;
import com.raytheon.uf.viz.core.exception.VizException;

public class NcPolyline extends Polyline implements INcCommand {

    // private final Log logger = LogFactory.getLog(this.getClass());

    static List<double[]> currentDraw = new ArrayList<double[]>();

    public NcPolyline(int ec, int eid, int l, DataInput in) throws IOException {
        super(ec, eid, l, in);
    }

    @Override
    public void paint(IGraphicsTarget target, PaintProperties paintProps,
            IDescriptor descriptor, ImageBuilder ib) throws VizException {

        WireframeKey key = ib.new WireframeKey(ib.currentLineColor,
                ib.currentLineWidth);

        IWireframeShape wireframeForThisKey = ib.wireframes.get(key);

        if (wireframeForThisKey == null) {
            wireframeForThisKey = target
                    .createWireframeShape(false, descriptor);
            ib.wireframes.put(key, wireframeForThisKey);
        }

        PathIterator pi = this.path.getPathIterator(null);

        while (pi.isDone() == false) {
            processCurrentSegment(pi, wireframeForThisKey, ib);
            pi.next();
        }

        // if no close command
        if (currentDraw.size() > 1) {
            wireframeForThisKey.addLineSegment(currentDraw
                    .toArray(new double[0][0]));
        }
        currentDraw.clear();

    }

    public static void processCurrentSegment(PathIterator pi,
            IWireframeShape wireframeForThisKey, ImageBuilder ib) {
        double[] coordinates = new double[6];

        int type = pi.currentSegment(coordinates);
        switch (type) {
        case PathIterator.SEG_MOVETO:
            // System.out.println("move to " + coordinates[0] + ", " +
            // coordinates[1]);
            if (currentDraw.size() > 1) {
                wireframeForThisKey.addLineSegment(currentDraw
                        .toArray(new double[0][0]));
            }
            currentDraw.clear();
            currentDraw.add(ib.scalePoint(coordinates));
            break;
        case PathIterator.SEG_LINETO:
            // System.out.println("line to " + coordinates[0] + ", " +
            // coordinates[1]);
            currentDraw.add(ib.scalePoint(coordinates));
            break;
        case PathIterator.SEG_QUADTO:
            // System.out.println("quadratic to " + coordinates[0] + ", " +
            // coordinates[1] + ", "
            // + coordinates[2] + ", " + coordinates[3]);
            // TODO -- error / not supported
            break;
        case PathIterator.SEG_CUBICTO:
            // System.out.println("cubic to " + coordinates[0] + ", " +
            // coordinates[1] + ", "
            // + coordinates[2] + ", " + coordinates[3] + ", " + coordinates[4]
            // + ", " + coordinates[5]);
            // TODO -- error / not supported
            break;
        case PathIterator.SEG_CLOSE:
            // System.out.println("close");
            if (currentDraw.size() > 1) {
                wireframeForThisKey.addLineSegment(currentDraw
                        .toArray(new double[0][0]));
            }
            currentDraw.clear();
            break;
        default:
            break;
        }
    }

}
