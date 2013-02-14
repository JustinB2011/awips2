# #
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
# #


#
# Routes requests to the Data Access Framework through JEP to the Java classes.
# Returns Python objects that wrap Java objects.
#
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    12/10/12                      njensen       Initial Creation.
#    
# 
#

from ufpy.dataaccess import IGeometryRequest, IGridRequest

from com.raytheon.uf.common.dataaccess import DataAccessLayer as JavaDataAccessLayer
from com.raytheon.uf.common.dataaccess.impl import DefaultGridRequest, DefaultGeometryRequest
from com.raytheon.uf.common.time import DataTime as JavaDataTime
from com.raytheon.uf.common.geospatial import LatLonReprojection
from com.raytheon.uf.common.python import PythonNumpyFloatArray

import jep
import DataTime
import JGeometryData, JGridData, JGridRequest, JGeometryRequest


def getAvailableTimes(request):
    javaTimes = JavaDataAccessLayer.getAvailableTimes(request.toJavaObj())
    times = []
    for jt in javaTimes:
        times.append(DataTime.DataTime(jt))
    return times


def getData(request, times):
    if type(times) is list:
        # presuming list of DataTimes
        jtimes = jep.jarray(len(times), JavaDataTime)
        for i in xrange(len(times)):
            jtimes[i] = times[i].toJavaObj()    
        javaData = JavaDataAccessLayer.getData(request.toJavaObj(), jtimes)
    else:
        # presuming TimeRange
        javaData = JavaDataAccessLayer.getData(request.toJavaObj(), times.toJavaObj())        
    wrapper = None
    if isinstance(request, IGeometryRequest):
        wrapper = JGeometryData.JGeometryData
    elif isinstance(request, IGridRequest):
        wrapper = JGridData.JGridData
    data = []
    for jd in javaData:
        data.append(wrapper(jd))
    return data

def getLatLonCoords(gridRequest):
    '''
        @return: a tuple where the first element is a numpy array of lons, and the second element is a numpy array of lats
    '''
    gridGeometry = JavaDataAccessLayer.getGridGeometry(gridRequest.toJavaObj())
    if gridGeometry is None :
        return None
    latlons = LatLonReprojection.getLatLons(gridGeometry)
    nx = gridGeometry.getGridRange().getSpan(0)
    ny = gridGeometry.getGridRange().getSpan(1)
    latndarray = PythonNumpyFloatArray(latlons.getLats(), nx, ny).__numpy__[0]
    lonndarray = PythonNumpyFloatArray(latlons.getLons(), nx, ny).__numpy__[0]
    return (lonndarray, latndarray)

def getAvailableLocationNames(request):
    return JavaDataAccessLayer.getAvailableLocationNames(request.toJavaObj())

def newGeometryRequest():
    return JGeometryRequest.JGeometryRequest(DefaultGeometryRequest())
    
def newGridRequest():        
    return JGridRequest.JGridRequest(DefaultGridRequest())

