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
# Port of requestAT code from AWIPS1
#
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    02/06/13        1447          dgilling       Initial Creation.
# 
#

import cPickle
import errno
import logging
import os
import sys
import tempfile
import time
import xml.etree.ElementTree as ET

import IrtAccess
import siteConfig
import VTECPartners

from com.raytheon.uf.common.activetable import ActiveTableMode
from com.raytheon.uf.edex.activetable import ActiveTable



log = None

def init_logging():
    logPath = os.path.join(siteConfig.GFESUITE_LOGDIR, 
                           time.strftime("%Y%m%d", time.gmtime()), 'requestAT.log')
    try:
        os.makedirs(os.path.dirname(logPath))
    except OSError as e:
        if e.errno != errno.EEXIST:
            sys.stderr.write("Could not create log directory " + os.path.dirname(logPath))
            sys.exit(-1)
    
    logging.basicConfig(filename=logPath, 
                        format="%(levelname)s  %(asctime)s [%(process)d:%(thread)d] %(filename)s: %(message)s", 
                        datefmt="%H:%M:%S", 
                        level=logging.INFO)
    global log
    log = logging.getLogger("requestAT") 

def execute_request_at(serverHost, serverPort, serverProtocol, mhsid, siteID, ancf, 
                         bncf, xmtScript):
    #--------------------------------------------------------------------
    # Create a message - pickled
    # (MHSsiteID, mySiteID, listOfVTECMergeSites, countDict, issueTime)
    # Note that VTEC_MERGE_SITES does not contain our site or SPC, TPC.
    #--------------------------------------------------------------------
    
    # determine my 4 letter siteid
    if siteID in ['SJU']:
        mysite4 = "TJSJ"
    elif siteID in ['AFG','AJK','HFO','GUM']:
        mysite4 = "P" + siteID
    elif siteID in ['AER','ALU']:
        mysite4 = "PAFC"
    else:
        mysite4 = "K" + siteID
    otherSites = [mysite4, VTECPartners.VTEC_SPC_SITE,
      VTECPartners.VTEC_TPC_SITE]

    # determine the MHS WMO id for this message
    wmoid = "TTAA00 " + mysite4
    wmoid += " " + time.strftime("%d%H%M", time.gmtime(time.time()))

    # connect to ifpServer and retrieve active table
    actTab = ActiveTable.getActiveTable(mysite4, ActiveTableMode.OPERATIONAL)

    # analyze active table to get counts
    countDict = {}
    issueTime = 0
    for i in xrange(actTab.size()):
        rec = actTab.get(i)
        # only care about our own sites that we merge
        if rec.getOfficeid() not in VTECPartners.VTEC_MERGE_SITES and \
          rec.getOfficeid() not in otherSites:
            continue

        recIssueTime = rec.getIssueTime().getTimeInMillis() / 1000
        #track latest
        issueTime = max(recIssueTime, issueTime)

        cnt = countDict.get(rec.getOfficeid(), 0)  #get old count
        countDict[rec.getOfficeid()] = cnt + 1

    data = (mhsid, siteID, VTECPartners.VTEC_MERGE_SITES, countDict, issueTime)
    log.info("Data: " + repr(data))

    tempdir = os.path.join(siteConfig.GFESUITE_HOME, "products", "ATBL")
    with tempfile.NamedTemporaryFile(suffix='.reqat', dir=tempdir, delete=False) as fp:
        fname = fp.name
        buf = cPickle.dumps(data)
        fp.write(buf)
    
    #--------------------------------------------------------------------
    # Assemble XML source/destination document
    #--------------------------------------------------------------------
    msgSendDest = []   #list of mhs sites to send request

    irt = IrtAccess.IrtAccess(ancf, bncf)
    iscE = ET.Element('isc')
    # this is the requestor of the data
    sourceServer = {'mhsid': mhsid, 'host': serverHost, 'port': serverPort,
      'protocol': serverProtocol, 'site': siteID}
    irt.addSourceXML(iscE, sourceServer)
    log.info("Requesting Server: " + irt.printServerInfo(sourceServer))

    # who is running the domains requested?
    sites = VTECPartners.VTEC_TABLE_REQUEST_SITES
    if not sites:
        log.error('No sites defined for VTEC_TABLE_REQUEST_SITES')
        sys.exit(1)

    status, xml = irt.getServers(sites)
    if not status:
        log.error('Failure to getServers from IRT')
        sys.exit(1)

    # decode the XML
    try:
        serverTree = ET.ElementTree(ET.XML(xml))
        serversE = serverTree.getroot()
    except:
        log.exception("Malformed XML on getServers()")
        sys.exit(1)

    if serversE.tag != "servers":
        log.error("Servers packet missing from web server")
        sys.exit(1)

    # process each requested domain returned to us
    chosenServers = []
    matchingServers = []
    for domainE in serversE:
        if domainE.tag != "domain":
            continue
        servers = []  #list of servers for this domain

        # decode each server in the domain
        for addressE in domainE.getchildren():
            info = irt.decodeXMLAddress(addressE)
            if info is None:
                continue   #not address tag
            servers.append(info)
            matchingServers.append(info)

        # server search list in priority.  The px3 entries are used for
        # dual domain for AFC.
        hp = [('dx4','98000000'),('px3', '98000000'), ('dx4','98000001'),
          ('px3', '98000001')]

        # choose one server from this domain, find first dx4, 98000000
        # try to use one with the same mhsidDest as the site, which
        # would be the primary operational GFE. Note that the px3 entries
        # are for AFC.
        found = False
        for matchServer, matchPort in hp:
            for server in servers:
                if server['host'][0:3] == matchServer and \
                  server['port'] == matchPort and server['mhsid'] == siteID:
                    chosenServers.append(server)
                    if server['mhsid'] not in msgSendDest:
                        msgSendDest.append(server['mhsid'])
                    found = True
                    break

        # find first dx4, 98000000, but perhaps a different mhsid
        # this is probably not the primary operational GFE
        if not found:
            for matchServer, matchPort in hp:
                for server in servers:
                    if server['host'][0:3] == matchServer and \
                      server['port'] == matchPort:
                        chosenServers.append(server)
                        if server['mhsid'] not in msgSendDest:
                            msgSendDest.append(server['mhsid'])
                        found = True
                        break

        # if didn't find standard one, then take the first one, but don't
        # take ourselves unless we are the only one.
        if not found and servers:
            for server in servers:
                if server['mhsid'] != mhsid and server['host'] != serverHost \
                  and server['port'] != serverPort and \
                  server['mhsid'] != siteID:
                    chosenServers.append(server)
                    if server['mhsid'] not in msgSendDest:
                        msgSendDest.append(server['mhsid'])
                    found = True
            if not found:
                chosenServers.append(servers[0])
                if servers[0]['mhsid'] not in msgSendDest:
                    msgSendDest.append(servers[0]['mhsid'])

    # Display the set of matching servers
    s = "Matching Servers:"
    for x in matchingServers:
        s += "\n" + irt.printServerInfo(x)
    log.info(s)

    # Display the chosen set of servers
    s = "Chosen Servers:"
    for x in chosenServers:
        s += "\n" + irt.printServerInfo(x)
    log.info(s)

    irt.addDestinationXML(iscE, chosenServers)

    # create the XML file
    with tempfile.NamedTemporaryFile(suffix='.xml', dir=tempdir, delete=False) as fd:
        fnameXML = fd.name
        fd.write(ET.tostring(iscE))

    #--------------------------------------------------------------------
    # Now send the message
    #--------------------------------------------------------------------
    irt.transmitFiles("GET_ACTIVE_TABLE2", msgSendDest, mhsid,
      [fname, fnameXML], xmtScript)


def runFromJava(serverHost, serverPort, serverProtocol, mhsid, siteID, ancf, 
                bncf, xmtScript):
    init_logging()
    
    log.info('*********** requestAT ******************')
    startT = time.time()
    
    try:
        execute_request_at(serverHost, serverPort, serverProtocol, mhsid, 
                             siteID, ancf, bncf, xmtScript)
    except:
        log.exception('Error requesting active table')
    
    #--------------------------------------------------------------------
    # Finish
    #--------------------------------------------------------------------
    endT = time.time()
    log.info("Final: wctime: {0:-6.2f}, cputime: {1:-6.2f}".format(endT - startT, time.clock()))

