# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# CoastalThreat
#
# Author: Tom LeFebvre/Pablo Santos
# April 20, 2012 - To use gridded MSL TO NAVD and MSL to MLLW
# corrections and to get rid of Very Low.
# Last Modified: June 7, 2012 Shannon White - To fix the handling of time
# for A2 so it works for both real time and displaced real time 
# Migrated TC Coastal Flood for AWIPS2. Updated 6/22/2012.  S.O.
# March 11, 2014 to adapt to new PSURGE 2.0/PHISH and VDATUM Datasets in A1. PS
# May 21, 2014: for new PHISH but in AWIPS 2: PS/SW
# Aug 13, 2014: To rename SurgeHtPlustTide to InundationMax and incorporate InundationTiming. PS
# Sept 17, 2014: To finalize changes and clean up for 2015initial Baseline Check in.
# 
# Last Modified: Sept 18, 2014: Added code to pull grids from NHC via ISC if PHISH not
# Available on time. Left inactive (commented out) for the moment until that can be fully tested later
# in 2014 or in 2015.
# 
# ----------------------------------------------------------------------------
# The MenuItems list defines the GFE menu item(s) under which the
# Procedure is to appear.
# Possible items are: Populate, Edit, Consistency, Verify, Hazards

MenuItems = ["Populate"]

import SmartScript
import numpy as np
import TimeRange
import AbsTime
import time
import sys

VariableList = [("DEFAULT: Typical. Should only be changed in coordination with NHC SS Unit", "", "label"),
                ("Forecast Confidence?", "Typical (10% Exceedance; for most systems anytime within 48 hours)",
##                 "radio", ["Low (Prob-only; 10% Exceedance; for ill behaved systems)",
                 "radio", ["Typical (10% Exceedance; for most systems anytime within 48 hours)",
                           "Medium (20% Exceedance; for well-behaved systems within 12 hours of event)",
                           "High (30% Exceedance; for well-behaved systems within 6-12 hours of event)",
                           "Higher (40% Exceedance; for well-behaved systems within 6 hours of the event)",
                           "Highest (50% Exceedance; for well-behaved systems at time of the event)"]),
                ("Grid Smoothing?", "Yes", "radio", ["Yes","No"]),
                ("Make grids from PHISH\n or ISC?\n", "PHISH", "radio", ["PHISH", "ISC"]),
                ]

class Procedure (SmartScript.SmartScript):
    def __init__(self, dbss):
        SmartScript.SmartScript.__init__(self, dbss)
       
    def getWEInventory(self, modelName, WEName, level):
        allTimes = TimeRange.allTimes().toJavaObj()
        gridInfo = self.getGridInfo(modelName, WEName, level, allTimes)
        trList = []
        for g in gridInfo:
            start = g.gridTime().startTime().unixTime()
            end = g.gridTime().endTime().unixTime()
            tr = TimeRange.TimeRange(AbsTime.AbsTime(start),
                                AbsTime.AbsTime(end))
            trList.append(tr)

        return trList

    def getAvgTopoGrid(self, topodb):

        siteID = self.getSiteID()
#        print "********************\n TOPO IS: ", topodb
        dbName = siteID + "_D2D_" + topodb

        weName = "avgTopo"
#        timeRange = TimeRange.allTimes().toJavaObj()
        trList = self.getWEInventory(dbName, weName, "SFC")
        
        #print "NED Topo list is", trList
        
        if len(trList)== 0:
            #print "CRAP!!!"
            return
        for tr in trList:
#            print "My time is", tr
            topoGrid = self.getGrids(dbName, weName, "SFC", tr, mode="First")
           

        # convert to feet
        topoGrid = topoGrid * 3.281
        #topoVal = topoGrid.copy()
        min = -16000
        max = 16000.0
        mask1 = topoGrid < min
        mask2 = topoGrid > max
        topoGrid[mask1] = -80
        topoGrid[mask2] = self.getTopo()[mask2]

#        mask1 = topoVal< min
#        mask2 = topoVal> max
#        topoGrid = np.where(mask1,-80.0,topoVal)
#        topoGrid = np.where(mask2,self.getTopo(),topoVal)
       
        return topoGrid

    def makeNewTimeRange(self, hours):

        cTime = int(self._gmtime().unixTime()/ 3600) * 3600
        startTime = AbsTime.AbsTime(cTime)
        endTime = startTime + (hours * 3600)
        threatTR = TimeRange.TimeRange(startTime, endTime)      
        timeRange = TimeRange.TimeRange(startTime, endTime)

        return timeRange

    def getModelIDList(self, matchStr):

        availParms = self.availableParms()

        modelList = []
        for pName, level, dbID in availParms:
            modelId = dbID.modelIdentifier()
            if modelId.find(matchStr) > -1:
                if modelId not in modelList:
                    modelList.append(modelId)
    
        return modelList

    def getExceedanceHeight(self, pctStr, level):
        
        ap = self.availableParms()
        dbName = self.getSiteID() + "_D2D_TPCSurgeProb"

        modelIDList = self.getModelIDList("TPCSurgeProb")
        modelIDList.sort()

        if len(modelIDList) == 0:
            self.statusBarMsg("No pSurge data found in your inventory.", "S")
            return None, None, None

        # the last one is the latest
#        modelIDList[-1]
        surgeModel = modelIDList[-1]
        
        weName = "Surge" + pctStr + "Pct"        
        trList = self.getWEInventory(dbName, weName, level)

        #print "Retreiving ", weName, " at ", level
        for tr in trList:
            grid = self.getGrids(dbName, weName, level, tr, mode="Max")
            #maxGrid = maximum(grid, -100.0)   # calculate the max as we go
        
        surgeVal = grid.copy()
        mask = surgeVal>-100
        grid = np.where(mask,surgeVal*3.28, -80.0)
#        print dir(grid)          
        return grid  # convert meters to feet 
    
    def makePhishGrid(self, pctStr, level, smoothThreatGrid):
        
        siteID = self.getSiteID()
        dbName = siteID + "_D2D_TPCSurgeProb"
        
        weName = "Surge" + pctStr + "Pctincr"
        #print "Attempting to retrieve: ", weName, level
        trList = self.getWEInventory(dbName, weName, level)
        
        if len(trList) == 0:
            self.statusBarMsg("No grids available for model:" + dbName, "S")
            return None

        n = 1
        for tr in trList:
            start = tr.startTime().unixTime() - 6*3600
            if n == 1:
                starttimeghls = tr.startTime().unixTime() - 3*3600
                trdelete = TimeRange.TimeRange(AbsTime.AbsTime(starttimeghls - 100*3600),
                                               AbsTime.AbsTime(starttimeghls + 100*3600))
                self.deleteCmd(['InundationTiming'], trdelete)
                n = n + 1   
            end = tr.startTime().unixTime()
            tr6 = TimeRange.TimeRange(AbsTime.AbsTime(start),
                                      AbsTime.AbsTime(end))           
            phishGrid = self.getGrids(dbName, weName, level, tr)
#
# For consistency we need to add smoothing here too as we do in execute.
#
            if phishGrid is None:
                self.statusBarMsg("No PHISH grid available for:" + repr(tr), "S")
                continue       
            
            if smoothThreatGrid is "Yes":
                phishGrid = np.where(np.greater(phishGrid, 0.0), self.smoothGrid(phishGrid,3), phishGrid)        
            
            grid = np.where(phishGrid>-100,phishGrid*3.28, -80.0)
            self.createGrid("Fcst", "InundationTiming", "SCALAR", grid, tr6, precision=1)
  
        return

#**************************************************************************************
# THis procedure was written to extract MSL to NAVD corrections from the VDATUMS D2D
# Database. It is not yet implemented because the VDATUMS database has not been
# finalized.

    def getMSLtoNAVD(self):
        siteID = self.getSiteID()
        dbName = siteID + "_D2D_VDATUMS"

        weName = "MSLtoNAVD88"
        trList = self.getWEInventory(dbName, weName, "SFC")

        if len(trList) == 0:
            msgStr = weName + " does not exist in the VDATUMS model. "
            self.statusBarMsg(msgStr, "S")

        #maxGrid = zeros(self.getTopo().shape)

        for tr in trList:
            grid = self.getGrids(dbName, weName, "SFC", tr, mode="First")
            #maxGrid = maximum(grid, -100.0)   # calculate the max as we go

        #maxGrid = where(greater(maxGrid,-100.0), maxGrid*3.28, maxGrid)
        conversionGrid = grid.copy()
        mask = conversionGrid>-0.40
        grid = np.where(mask, conversionGrid*3.28, -80.0)        

        #return maxGrid  # convert meters to feet
        return grid


# THis procedure was written to extract MSL to MLLW corrections from the VDATUMS D2D
# Database. It is not yet implemented because the VDATUMS database has not been
# finalized.

    def getMSLtoMLLW(self):
        siteID = self.getSiteID()
        dbName = siteID + "_D2D_VDATUMS"

        weName = "MSLtoMLLW"
        trList = self.getWEInventory(dbName, weName, "SFC")

#         if len(trList) == 0:
#             msgStr = weName + " does not exist in the VDATUMS model. "
#             self.statusBarMsg(msgStr, "S")

        #maxGrid = zeros(self.getTopo().shape)

        for tr in trList:
            grid = self.getGrids(dbName, weName, "SFC", tr, mode="First")
            #maxGrid = maximum(grid, -100.0)   # calculate the max as we go

        #maxGrid = where(greater(maxGrid,-100.0),maxGrid*3.28, maxGrid)
        conversionGrid = grid.copy()
        mask = conversionGrid>0.0
        grid = np.where(mask,conversionGrid *3.28, -80.0) 

        #return maxGrid  # convert meters to feet
        return grid

    
# THis procedure was written to extract MSL to MHHW corrections from the VDATUMS D2D
# Database. It is not yet implemented because the VDATUMS database has not been
# finalized.

    def getMSLtoMHHW(self):
        siteID = self.getSiteID()
        dbName = siteID + "_D2D_VDATUMS"

        weName = "MSLtoMHHW"
        trList = self.getWEInventory(dbName, weName, "SFC")

        if len(trList) == 0:
            msgStr = weName + " does not exist in the VDATUMS model. "
            self.statusBarMsg(msgStr, "S")

        #maxGrid = zeros(self.getTopo().shape)

        for tr in trList:
            grid = self.getGrids(dbName, weName, "SFC", tr, mode="First")
            #maxGrid = maximum(grid, -100.0)   # calculate the max as we go

        #maxGrid = where(greater(maxGrid,-100.0),maxGrid*3.28, maxGrid)
        conversionGrid = grid.copy()
        mask = conversionGrid>-3.09
        grid = np.where(mask, conversionGrid*3.28, -80.0) 

        #return maxGrid  # convert meters to feet
        return grid

# THis procedure was written to extract NAVD88 to MLLW corrections from the VDATUMS D2D
# Database. It is not yet implemented because the VDATUMS database has not been
# finalized.

    def getNAVDtoMLLW(self):
        siteID = self.getSiteID()
        dbName = siteID + "_D2D_VDATUMS"

        weName = "NAVD88toMLLW"
        trList = self.getWEInventory(dbName, weName, "SFC")

#         if len(trList) == 0:
#             msgStr = weName + " does not exist in the VDATUMS model. "
#             self.statusBarMsg(msgStr, "S")

        #maxGrid = zeros(self.getTopo().shape)

        for tr in trList:
            grid = self.getGrids(dbName, weName, "SFC", tr, mode="First")
            #maxGrid = maximum(grid, -100.0)   # calculate the max as we go

        #maxGrid = where(greater(maxGrid,-100.0),maxGrid*3.28, maxGrid)
        conversionGrid=grid.copy()
        mask = conversionGrid>-2.20
        grid = np.where(mask, conversionGrid*3.28, -80.0) 

        #return maxGrid  # convert meters to feet
        return grid


# THis procedure was written to extract NAVD88 to MLLW corrections from the VDATUMS D2D
# Database. It is not yet implemented because the VDATUMS database has not been
# finalized.

    def getNAVDtoMHHW(self):
        siteID = self.getSiteID()
        dbName = siteID + "_D2D_VDATUMS"

        weName = "NAVD88toMHHW"
        trList = self.getWEInventory(dbName, weName, "SFC")

#         if len(trList) == 0:
#             msgStr = weName + " does not exist in the VDATUMS model. "
#             self.statusBarMsg(msgStr, "S")

        #maxGrid = zeros(self.getTopo().shape)

        for tr in trList:
            grid = self.getGrids(dbName, weName, "SFC", tr, mode="First")
            #maxGrid = maximum(grid, -100.0)   # calculate the max as we go

        #maxGrid = where(greater(maxGrid,-100.0),maxGrid*3.28, maxGrid)
        conversionGrid = grid.copy()
        mask = conversionGrid>-3.40
        grid = np.where(mask, conversionGrid*3.28, -80.0) 

        #return maxGrid  # convert meters to feet
        return grid

    def smoothGrid(self, grid, factor):
        # factors of less than 3 are useless or dangerous
        if factor < 3:
            return grid
        st = time.time()
        half = int(factor)/ 2
        sg = np.zeros(grid.shape,"f8")
        count = np.zeros(grid.shape,"f8")
        gridOfOnes = np.ones(grid.shape,"f8")
        for y in range(-half, half + 1):
            for x in range(-half, half + 1):
                if y < 0:
                    yTargetSlice = slice(-y, None, None)
                    ySrcSlice = slice(0, y, None)
                if y == 0:
                    yTargetSlice = slice(0, None, None)
                    ySrcSlice = slice(0, None, None)
                if y > 0:
                    yTargetSlice = slice(0, -y, None)
                    ySrcSlice = slice(y, None, None)
                if x < 0:
                    xTargetSlice = slice(-x, None, None)
                    xSrcSlice = slice(0, x, None)
                if x == 0:
                    xTargetSlice = slice(0, None, None)
                    xSrcSlice = slice(0, None, None)
                if x > 0:
                    xTargetSlice = slice(0, -x, None)
                    xSrcSlice = slice(x, None, None)

                target = [yTargetSlice, xTargetSlice]
                src = [ySrcSlice, xSrcSlice]
                sg[target] = np.where(np.greater(grid[src],-80.0),sg[target] + grid[src],sg[target])
                count[target] = np.where(np.greater(grid[src],-80.0),count[target] + gridOfOnes[src],count[target])

        return np.where(np.greater(count,0.0), sg / count, -80.0)
    
    
    # Copies the specified weather elements in elementList into the Fcst database.
    def copyISCGridstoFcst(self, elementList):
        
        # First delete the existing grids so there's no confusion
        cTime = int(self._gmtime().unixTime()/ 3600) * 3600
        startTime = AbsTime.AbsTime(cTime - 24*3600)
        endTime = startTime + 240*3600
        timeRange = TimeRange.TimeRange(startTime, endTime)      

        
        for elem in elementList:
             if elem == "InundationTiming":
                 #print "Deleting: ", elem
                 self.deleteCmd([elem], timeRange)         

        for weName in elementList:
            iscWeName = weName + "nc"
            # get the inventory for the ISC grids
            try:
                trList = self.getWEInventory("ISC", iscWeName, "SFC")
            except:
                self.statusBarMsg("No grids found in ISC database for " + iscWeName, "S")
                continue
            
            if len(trList) == 0:
                self.statusBarMsg("No grids found in ISC database for " + iscWeName, "S")
                continue
            
            # Fetch the ISC grid and create the same grid in the Fcst database
            for tr in trList:
                grid = self.getGrids("ISC", iscWeName, "SFC", tr)
                if iscWeName == "InundationTimingnc":
                    self.createGrid("Fcst", weName, "SCALAR", grid, tr, precision=2) 
                elif iscWeName == "InundationMaxnc":
                    surgePctGrid = grid
                elif iscWeName == "SurgeHtPlusTideMSLnc":
                    surgePctGridMSL = grid
                elif iscWeName == "SurgeHtPlusTideMLLWnc":
                    surgePctGridMLLW = grid
                elif iscWeName == "SurgeHtPlusTideMHHWnc":
                    surgePctGridMHHW = grid     
                elif iscWeName == "SurgeHtPlusTideNAVDnc":
                    surgePctGridNAVD = grid 

        return surgePctGrid,surgePctGridMSL,surgePctGridMLLW,surgePctGridMHHW,surgePctGridNAVD
    
    def execute(self, varDict):
        
        # List of elements       
        # See if we should copy from ISC. If so, do the copy and exit
        smoothThreatGrid = varDict["Grid Smoothing?"]
        PHISHorISC = varDict["Make grids from PHISH\n or ISC?\n"] 
        #PHISHorISC = "PHISH"
        topodb = "NED"
        #topodb = varDict["Topographic Database?"]

        editArea = self.getEditArea("StormSurgeWW_EditArea")
        ssea = self.encodeEditArea(editArea)

        Topo = self.getAvgTopoGrid(topodb)

        confidenceStr = varDict["Forecast Confidence?"]
             
        # extract the percent value from this string
        pctPos = confidenceStr.find("%")
        pctStr = confidenceStr[pctPos - 2:pctPos]

        #print "pctStr is: ", pctStr

        if PHISHorISC == "PHISH":
           
            #initialize grids to zero
            surgePctGrid = self._empty
            surgePctGridNAVD = self._empty

            # Now get the psurge
            surgePctGrid = self.getExceedanceHeight(pctStr, "FHAG0")
            surgePctGridNAVD = self.getExceedanceHeight(pctStr, "SFC")
            #print "retrieved my grids"   
#
# The following lines are the gridded vdatum corrections.
#
            msltonavd = self.getMSLtoNAVD()
            msltomllw = self.getMSLtoMLLW()
            msltomhhw = self.getMSLtoMHHW()
            navdtomllw = self.getNAVDtoMLLW()
            navdtomhhw = self.getNAVDtoMHHW()  

# Apply 3x3 smooth within the surge zone
# for values greater than 1 as to not underplay areas adjacent to zero value pixels.       
# If you apply a smoother, for consistency among storm surge plus tide and derived
# grids, it must be done here.

            if smoothThreatGrid is "Yes":
                surgePctGrid = np.where(np.greater(surgePctGrid, 0.0), self.smoothGrid(surgePctGrid,3), surgePctGrid)
                surgePctGridNAVD = np.where(np.greater(surgePctGridNAVD, -10.0), self.smoothGrid(surgePctGridNAVD,3), surgePctGridNAVD)
        
            mask1 = np.logical_and(np.greater(msltonavd, -80.0),np.greater(surgePctGridNAVD,-80.0))
            surgePctGridMSL= np.where(mask1, surgePctGridNAVD - msltonavd, -80.0) # MSL Grid     
            surgePctGridMLLW = np.where(np.greater(navdtomllw,-80.0) & np.greater(surgePctGridNAVD,-80.0), \
                                        surgePctGridNAVD + navdtomllw, -80.0)# MLLW Grid
            surgePctGridMHHW = np.where(np.greater(navdtomhhw,-80.0) & np.greater(surgePctGridNAVD,-80.0), \
                                        surgePctGridNAVD + navdtomhhw, -80.0)# MHHW Grid
            surgeDiffMLLWMHHW = np.where(np.greater(surgePctGridMLLW,-80.0) & np.greater(surgePctGridMHHW, -80.0), \
                                         surgePctGridMLLW-surgePctGridMHHW, -80.0)# Diff Grid Between MLLW and MHHW   
           
            self.makePhishGrid(pctStr, "FHAG0", smoothThreatGrid) 
        
        else:
            
            elementList = ["InundationMax","InundationTiming", "SurgeHtPlusTideMSL","SurgeHtPlusTideMLLW",
                           "SurgeHtPlusTideNAVD","SurgeHtPlusTideMHHW"]
            surgePctGrid,surgePctGridMSL,surgePctGridMLLW,surgePctGridMHHW,surgePctGridNAVD = self.copyISCGridstoFcst(elementList)
        
        threatWEName = "StormSurgeThreat"
         
        threatKeys = self.getDiscreteKeys(threatWEName)
 
        # Define a mapping between UI names and key names
        # keyMap = {"Very Low" :"Very Low",
        keyMap = {"Elevated" : "Elevated", 
                  "Moderate" : "Mod",
                  "High" : "High",
                  "Extreme" : "Extreme",
                  }
         
        threshDict = {}  # a dict to store thresholds from the UI
 
        for key in keyMap.keys():
        #    if not key in varDict.keys(): # This should never happen
        #        print "Error in mapping UI keys to DISCRETE keys."
        #        print "Please fix the keyMap dictionary."
        #        return
 
            #threshDict[keyMap[key]] = varDict[key]
            if   keyMap[key] == "Extreme":
                threshDict[keyMap[key]] = 9
            elif keyMap[key] == "High":
                threshDict[keyMap[key]] = 6
            elif keyMap[key] == "Mod":
                threshDict[keyMap[key]] = 3
            elif keyMap[key] == "Elevated":
                threshDict[keyMap[key]] = 1
                             
            #print "threshDict[keyMap[key]]: ", keyMap[key], threshDict[keyMap[key]]
 
        # make a grid of zeros.  This will be the CoastalThreat grid
        coastalThreat = np.zeros(self.getTopo().shape)
 
        # Yet another list to define the order in which we set grid values
        # This order must be ranked lowest to highest
        #keyList = ["Very Low", "Elevated", "Mod", "High", "Extreme"]
        keyList = ["Elevated", "Mod", "High", "Extreme"]
 
        # Set the grid values based on the surgePctGrid grid and thresholds
        for key in keyList:
            #print "THRESHOLD FOR KEY IS: ", key, threshDict[key]
            thresh = threshDict[key]
            keyIndex = self.getIndex(key, threatKeys)
            coastalThreat = np.where(ssea & np.greater_equal(surgePctGrid, thresh), keyIndex,
                                                coastalThreat)
        
        # make a timeRange - 6 hours long
        elementList = ["StormSurgeThreat","InundationMax","SurgeHtPlusTideMSL","SurgeHtPlusTideMLLW","SurgeHtPlusTideNAVD","SurgeHtPlusTideMHHW"]

        cTime = int(self._gmtime().unixTime()/ 3600) * 3600
        startTime = AbsTime.AbsTime(cTime - 24*3600)
        endTime = startTime + 240*3600
        timeRange = TimeRange.TimeRange(startTime, endTime)      
        #print "time range to delete is: ", timeRange

        for elem in elementList:
             #print "Deleting: ", elem
             self.deleteCmd([elem], timeRange)         
                
        timeRange = self.makeNewTimeRange(6)
               
        # display the D2D grid for debugging purposes only
        self.createGrid("Fcst", "InundationMax", "SCALAR", surgePctGrid,
                        timeRange, precision=2)      
        self.createGrid("Fcst", "SurgeHtPlusTideMSL", "SCALAR", surgePctGridMSL,
                        timeRange, precision=2)
        self.createGrid("Fcst", "SurgeHtPlusTideMLLW", "SCALAR", surgePctGridMLLW,
                        timeRange, precision=2)
        self.createGrid("Fcst", "SurgeHtPlusTideNAVD", "SCALAR", surgePctGridNAVD,
                        timeRange, precision=2)
        self.createGrid("Fcst", "SurgeHtPlusTideMHHW", "SCALAR", surgePctGridMHHW,
                        timeRange, precision=2)

 
#       create the CoastalThreat Grid
        self.createGrid("Fcst", threatWEName, "DISCRETE",
                        (coastalThreat, threatKeys), timeRange,
                        discreteKeys=threatKeys,
                        discreteOverlap=0,
                        discreteAuxDataLength=2,
                        defaultColorTable="Hazards")

        return
