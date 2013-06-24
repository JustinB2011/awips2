##
# This software was developed and / or modified by Raytheon Company,
# pursuant to Contract DG133W-05-CQ-1067 with the US Government.
# 
# U.S. EXPORT CONTROLLED TECHNICAL DATA
# This software product contains export-restricted data whose
# export/transfer/disclosure is restricted by U.S. law. Dissemination
# to non-U.S. persons whether in the United States or abroad requires
# an export license or other authorization.
# 
# Contractor Name:        Raytheon Company
# Contractor Address:     6825 Pine Street, Suite 340
#                         Mail Stop B8
#                         Omaha, NE 68106
#                         402.291.0100
# 
# See the AWIPS II Master Rights File ("Master Rights File.pdf") for
# further licensing information.
##


#
# Implements IDataRequest and wraps around a Java IDataRequest
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    12/17/12                      njensen       Initial Creation.
#    Feb 14, 2013    1614          bsteffen       refactor data access framework
#                                                 to use single request.
#    
# 
#

from ufpy.dataaccess import IDataRequest
from com.raytheon.uf.common.dataplugin.level import Level
import JUtil
import jep

class JDataRequest(IDataRequest, JUtil.JavaWrapperClass):
    
    def __init__(self, wrappedObject):
        self.jobj = wrappedObject
        
    def setDatatype(self, datatype):
        self.jobj.setDatatype(datatype)
        
    def addIdentifier(self, key, value):
        self.jobj.addIdentifier(key, JUtil.pyValToJavaObj(value))
    
    def removeIdentifier(self, key):
        self.jobj.removeIdentifier(key)
    
    def setParameters(self, *args):
        from java.lang import String as JavaString
        params = jep.jarray(len(args), JavaString)
        for i in xrange(len(args)):
            params[i] = JavaString(str(args[i]))
        self.jobj.setParameters(params)
    
    def setLevels(self, *args):
        levels = jep.jarray(len(args), Level)
        for i in xrange(len(args)):
            levels[i] = Level(str(args[i]))
        self.jobj.setLevels(levels)
        
    def setEnvelope(self, env):
        from com.vividsolutions.jts.geom import Envelope        
        bounds = env.bounds        
        jenv = Envelope(bounds[0], bounds[2], bounds[1], bounds[3])
        self.jobj.setEnvelope(bounds)

    def setLocationNames(self, *args):
        from java.lang import String as JavaString
        locs = jep.jarray(len(args), JavaString)
        for i in xrange(len(args)):
            locs[i] = JavaString(str(args[i]))
        self.jobj.setLocationNames(locs)    
    
    def getDatatype(self):
        return self.jobj.getDatatype()
    
    def getIdentifiers(self):
        ids = {}
        jmap = self.jobj.getIdentifiers()
        itr = jmap.keySet().iterator()
        while itr.hasNext():
            key = itr.next()
            value = JUtil.javaObjToPyVal(jmap.get(key))
            ids[key] = value
        return ids
    
    def getParameters(self):
        return self.jobj.getParameters()
    
    def getLevels(self):
        levels = []
        jlevels = self.jobj.getLevels()
        for lev in jlevels:
            levels.append(str(lev))
        return levels
    
    def getEnvelope(self):
        env = None
        jenv = self.jobj.getEnvelope()        
        if jenv:
            from com.vividsolutions.jts.geom import GeometryFactory
            env = shapely.wkt.loads(GeometryFactory().toGeometry(jenv).toText())
        return env 
    
    def getLocationNames(self):        
        return self.jobj.getLocationNames()
    
    def toJavaObj(self):
        return self.jobj
    
