/**
 * 
 */
package com.raytheon.uf.common.dataplugin.warning.config;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.raytheon.uf.common.dataquery.requests.RequestConstraint;
import com.raytheon.uf.common.dataquery.requests.RequestableMetadataMarshaller;

/**
 * 
 * General point source configuration object
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 17, 2011            mschenke     Initial creation
 * 
 * </pre>
 * 
 * @author mschenke
 * @version 1.0
 */
@XmlAccessorType(XmlAccessType.NONE)
public class PointSourceConfiguration {

    @XmlAccessorType(XmlAccessType.NONE)
    public static enum SearchMethod {
        TRACK, POINTS;
    }

    @XmlAttribute
    private String variable;

    @XmlElement
    private String pointSource;

    @XmlElement
    private String pointField;

    @XmlJavaTypeAdapter(value = RequestableMetadataMarshaller.class)
    private HashMap<String, RequestConstraint> filter;

    /**
     * TRACK means that the points returned will be distance searched by total
     * distance from track along time. POINTS means that the points returned
     * will be from the storm center locations
     */
    @XmlElement
    private SearchMethod searchMethod = SearchMethod.TRACK;

    @XmlElement
    private boolean withinPolygon = false;

    @XmlElement
    private int maxResults = 30;

    @XmlElement
    private double distanceThreshold = 10;

    @XmlElementWrapper(name = "sortBy")
    @XmlElement(name = "sort")
    private String[] sortBy;

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public String getPointSource() {
        return pointSource;
    }

    public void setPointSource(String pointSource) {
        this.pointSource = pointSource;
    }

    public String getPointField() {
        return pointField;
    }

    public void setPointField(String pointField) {
        this.pointField = pointField;
    }

    public HashMap<String, RequestConstraint> getFilter() {
        return filter;
    }

    public void setFilter(HashMap<String, RequestConstraint> filter) {
        this.filter = filter;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public double getDistanceThreshold() {
        return distanceThreshold;
    }

    public void setDistanceThreshold(double distanceThreshold) {
        this.distanceThreshold = distanceThreshold;
    }

    public String[] getSortBy() {
        return sortBy;
    }

    public void setSortBy(String[] sortBy) {
        this.sortBy = sortBy;
    }

    public SearchMethod getSearchMethod() {
        return searchMethod;
    }

    public void setSearchMethod(SearchMethod searchMethod) {
        this.searchMethod = searchMethod;
    }

    public boolean isWithinPolygon() {
        return withinPolygon;
    }

    public void setWithinPolygon(boolean withinPolygon) {
        this.withinPolygon = withinPolygon;
    }

}
