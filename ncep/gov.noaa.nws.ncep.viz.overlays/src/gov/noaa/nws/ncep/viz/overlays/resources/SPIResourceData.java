package gov.noaa.nws.ncep.viz.overlays.resources;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.swt.graphics.RGB;

import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.AbstractNameGenerator;
import com.raytheon.uf.viz.core.rsc.AbstractVizResource;

import gov.noaa.nws.ncep.viz.resources.AbstractNatlCntrsResourceData;
import gov.noaa.nws.ncep.viz.resources.INatlCntrsResourceData;
import gov.noaa.nws.ncep.viz.common.RGBColorAdapter;
import gov.noaa.nws.ncep.viz.overlays.IPointOverlayResourceData;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name="NC-SPIResourceData")
public class SPIResourceData extends AbstractNatlCntrsResourceData 
		implements INatlCntrsResourceData, IPointOverlayResourceData { 

	public final static String DEFAULT_NAME = "NC SPI Overlay";
	
    /** The filename */
    @XmlElement
    private String filename = null;

    /** The human readable name */
    @XmlElement
    private String mapName = null;
    
    /**
     * the following attributes are NOAA misc-resources 
     * customized attributes
     */
    @XmlElement
	@XmlJavaTypeAdapter(RGBColorAdapter.class)
    private RGB color = new RGB(255, 0, 0);
	
    @XmlElement
    private MarkerState markerState;

    @XmlElement
    private MarkerType markerType; 
    
    @XmlElement
    private Float markerSize; 

    @XmlElement
    private Integer markerWidth;

    @XmlElement
    private MarkerTextSize markerTextSize;
    

    public SPIResourceData() {
        super();
        this.nameGenerator = new AbstractNameGenerator() {

            @Override
            public String getName(AbstractVizResource<?, ?> resource) {
                if (mapName != null) {
                    return mapName;
                }

                return filename;
            }
        };
    }

    public SPIResourceData(final String filename) {
        this();
        this.filename = filename;
    }

    @Override
    public SPIResource constructResource(LoadProperties loadProperties,
            IDescriptor descriptor) throws VizException {
        return new SPIResource(this, loadProperties);
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

	/**
     * @param filename
     *            the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return the mapName
     */
    public String getMapName() {
        return mapName;
    }

    /**
     * @param mapName
     *            the mapName to set
     */
    public void setMapName(String mapName) {
        this.mapName = mapName;
    }
	
    public RGB getColor	() {
		return color;
	}

	public void setColor(RGB color) {
		this.color = color;
		this.legendColor = color;
	}

	public MarkerState getMarkerState() {
		return markerState;
	}

	public void setMarkerState(MarkerState markerState) {
		this.markerState = markerState;
	}

	public MarkerType getMarkerType() {
		return markerType;
	}

	public void setMarkerType(MarkerType markerType) {
		this.markerType = markerType;
	}

	public float getMarkerSize() {
		return markerSize;
	}

	public void setMarkerSize(float markerSize) {
		this.markerSize = markerSize;
	}

	public int getMarkerWidth() {
		return markerWidth;
	}

	public void setMarkerWidth(int markerWidth) {
		this.markerWidth = markerWidth;
	}

	public MarkerTextSize getMarkerTextSize() {
		return markerTextSize;
	}

	public void setMarkerTextSize(MarkerTextSize markerTextSize) {
		this.markerTextSize = markerTextSize;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
        SPIResourceData other = (SPIResourceData) obj;
		return filename.equals( other.filename );
	}
}
