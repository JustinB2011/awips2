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

# File auto-generated against equivalent DynamicSerialize Java class

class SiteActivationNotification(object):

    def __init__(self):
        self.active = None
        self.modifiedSite = None
        self.runMode = None
        self.serverName = None
        self.siteID = None

    def getActive(self):
        return self.active

    def setActive(self, active):
        self.active = active

    def getModifiedSite(self):
        return self.modifiedSite

    def setModifiedSite(self, modifiedSite):
        self.modifiedSite = modifiedSite

    def getRunMode(self):
        return self.runMode

    def setRunMode(self, runMode):
        self.runMode = runMode

    def getServerName(self):
        return self.serverName

    def setServerName(self, serverName):
        self.serverName = serverName

    def getSiteID(self):
        return self.siteID

    def setSiteID(self, siteID):
        self.siteID = siteID

