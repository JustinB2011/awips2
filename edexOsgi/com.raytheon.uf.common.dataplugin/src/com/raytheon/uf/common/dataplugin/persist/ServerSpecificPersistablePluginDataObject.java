/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.common.dataplugin.persist;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Provides an abstract implementation of plugindataobject with clustered file
 * support.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 16, 2008            chammack     Initial creation
 * - AWIPS2 Baseline Repository --------
 * 08/22/2012          798 jkorman      Corrected hdfFileId persistence. 
 * </pre>
 * 
 * @author chammack
 * @version 1.0
 */
@Entity(name = "SS")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public abstract class ServerSpecificPersistablePluginDataObject extends
        PersistablePluginDataObject implements IPersistable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute
    @DynamicSerializeElement
    @Column
    private Integer hdfFileId;

    /**
     * Constructor
     */
    public ServerSpecificPersistablePluginDataObject(String uri) {
        super(uri);
    }

    /**
     * Constructor
     */
    public ServerSpecificPersistablePluginDataObject() {
        super();
    }

    // TODO Setting hdf file id on ingest has been disabled in preparation
    // of removing it altogether since it is OBE. After deployed sites have
    // had this version for long enough that all data with hdfFileId has null
    // hdfFileIds, then the getters/setters should be removed everywhere
    // and this class can be deleted from the inheritance hierarchy. An
    // update script will be needed at that time to drop the hdfFileId column.

    @Override
    public Integer getHdfFileId() {
        return hdfFileId;
    }

    @Override
    public void setHdfFileId(Integer hdfFileId) {
        this.hdfFileId = hdfFileId;
    }

}
