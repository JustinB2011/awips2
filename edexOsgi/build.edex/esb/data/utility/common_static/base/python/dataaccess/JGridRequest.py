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
# Implements IGridRequest and wraps around a Java IGridRequest.
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    12/18/12                      njensen       Initial Creation.
#    
# 
#

from ufpy.dataaccess import IGridRequest
import JUtil, JDataRequest
import jep

class JGridRequest(IGridRequest, JDataRequest.JDataRequest):
    
    def __init__(self, wrappedObject):
        JDataRequest.JDataRequest.__init__(self, wrappedObject)
            
    
    

