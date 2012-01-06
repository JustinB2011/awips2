#!/bin/bash
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
export INIT_MEM=256 # in Meg

if [ $HIGH_MEM_FLAG == "on" ]; then
    export MAX_MEM=1280 # in Meg
else
    export MAX_MEM=768 # in Meg
fi

export JMS_POOL_MIN=16
export JMS_POOL_MAX=32
export METADATA_POOL_MIN=15
export METADATA_POOL_MAX=30
export EDEX_DEBUG_PORT=5008
export EDEX_JMX_PORT=1619
export MGMT_PORT=9604

