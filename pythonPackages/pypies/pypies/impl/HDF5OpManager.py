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
# Semi-port from Java HDF5OpManager, handles read requests
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    07/20/10                      njensen       Initial Creation.
#    
# 
#

import numpy, pypies, logging
import h5py.selections
from pypies import StorageException, NotImplementedException
logger = pypies.logger

def read(ds, request):
    rt = request.getType()
    if logger.isEnabledFor(logging.DEBUG):
        logger.debug('requestType=' + rt)
    result = None
    indices = request.getIndices()
    if rt == 'ALL':
        if ds.len():
            result = ds.value
        else:
            result = numpy.zeros((0,), ds.dtype.type)
    elif rt == 'POINT':
        points = request.getPoints()
        ndims = len(ds.shape)
        if ndims == 1:
            indices = []
            for pt in points:
                indices.append(pt.getX())
            result = __do1DPointRequest(ds, indices)
        elif ndims == 2:
            result = __do2DPointRequest(ds, points)        
    elif rt == 'XLINE':
        # if a line query was used, but it's only 1d, this is really
        # a point query. We could use hyperslabs to do this, but
        # it would be a lot slower than a regular point query.
        if len(ds.shape) == 1:
            result = __do1DPointRequest(ds, indices)
        else:
            sel = h5py.selections.HyperSelection(ds.shape)
            sel[()] = False
            for n in indices:
                sel[:,n] = True
            result = ds[sel]
            nLines = len(indices)
            if len(result) > nLines:
                result.resize(len(result) / nLines, nLines)
    elif rt == 'YLINE':
        # if a line query was used, but it's only 1d, this is really
        # a point query. We could use hyperslabs to do this, but
        # it would be a lot slower than a regular point query.
        if len(ds.shape) == 1:
            result = __do1DPointRequest(ds, indices)
        else:
            sel = h5py.selections.HyperSelection(ds.shape)
            sel[()] = False
            for n in indices:
                sel[n] = True
            result = ds[sel]        
            nLines = len(indices)
            if len(result) > nLines:
                result.resize(nLines, len(result) / nLines)
    elif rt == 'SLAB':
        minIndex = request.getMinIndexForSlab()
        maxIndex = request.getMaxIndexForSlab()
        sel = h5py.selections.HyperSelection(ds.shape)
        sel[()] = False
        # reverse x and y for hdf5
        sel[minIndex[1]:maxIndex[1], minIndex[0]:maxIndex[0]] = True        
        result = ds[sel]
        result.resize(maxIndex[1]-minIndex[1], maxIndex[0]-minIndex[0])        
    else:
        raise NotImplementedException('Only read requests supported are ' +
                                      'ALL, POINT, XLINE, YLINE, and SLAB')
    
    return result


def __do1DPointRequest(ds, indices):
    points = numpy.asarray(indices)
    points.resize(len(indices), 1)
    sel = h5py.selections.PointSelection(ds.shape)
    sel.set(points)
    return ds[sel]

def __do2DPointRequest(ds, points):
    indices = []
    for pt in points:        
        indices.append((pt.getY(), pt.getX()))
    arr = numpy.asarray(indices)
    sel = h5py.selections.PointSelection(ds.shape)
    sel.set(arr)
    return ds[sel]
    
