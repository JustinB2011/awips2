package com.raytheon.uf.common.datadelivery.harvester;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.datadelivery.registry.Provider;
import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * HarveterConfig
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 12 Sept, 2012   1038      dhladky     Initial creation
 * 1  May   2013   1959      dhladky     remove backup registry references
 * 
 * </pre>
 * 
 * @author dhladky
 * @version 1.0
 */

@XmlRootElement(name = "harvester")
@XmlAccessorType(XmlAccessType.NONE)
public class HarvesterConfig implements ISerializableObject {

    @XmlElement(name = "provider", type = Provider.class)
    @DynamicSerializeElement
    private Provider provider;

    @XmlElement(name = "agent")
    @DynamicSerializeElement
    private Agent agent;

    public HarvesterConfig() {

    }

    public Agent getAgent() {
        return agent;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

}
