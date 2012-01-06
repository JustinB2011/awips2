C MEMBER SRODLY
C-----------------------------------------------------------------------
C
C                             LAST UPDATE: 03/09/94.14:21:02 BY $WC20SV
C
C @PROCESS LVL(77)
C
C DESC READ DAILY DATA STATION ALPHABETICAL ORDER
C
      SUBROUTINE SRODLY (TYPE,IPRERR,IVTYPE,UNUSED,IORDER,MAXSTA,
     *   IPNTRS,NUMSTA,LARRAY,ARRAY,ISTAT)
C
      CHARACTER*8 BLNK8/' '/
      REAL OP24/4HOP24/,OPVR/4HOPVR/,OT24/4HOT24/,OE24/4HOE24/
      INTEGER*2 IPNTRS(*)
C
      DIMENSION ARRAY(LARRAY)
      DIMENSION UNUSED(*)
C
      INCLUDE 'uio'
      INCLUDE 'scommon/sudbgx'
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/ppinit_read/RCS/srodly.f,v $
     . $',                                                             '
     .$Id: srodly.f,v 1.1 1995/09/17 19:14:56 dws Exp $
     . $' /
C    ===================================================================
C
C
C
      IF (ISTRCE.GT.0) WRITE (IOSDBG,50)
      IF (ISTRCE.GT.0) CALL SULINE (IOSDBG,1)
C
C  SET DEBUG LEVEL
      LDEBUG=ISBUG(TYPE)
C
      ISTAT=0
C
C  CHECK FOR VALID TYPE
      IF (TYPE.EQ.OP24.OR.TYPE.EQ.OPVR.OR.TYPE.EQ.OT24.OR.
     *    TYPE.EQ.OE24) GO TO 10
         WRITE (LP,60) TYPE
         CALL SUERRS (LP,2,-1)
         ISTAT=1
         GO TO 40
C
C  READ PARAMETER RECORD
10    CALL SUDOPN (1,'PPP ',IERR)
      IPTR=0
      CALL RPPREC (BLNK8,TYPE,IPTR,LARRAY,ARRAY,NFILL,IPTRNX,
     *     IERR)
      IF (IERR.EQ.0) GO TO 20
         ISTAT=IERR
         IF (IPRERR.EQ.0) GO TO 40
            CALL SRPPST (BLNK8,TYPE,IPTR,LARRAY,NFILL,IPTRNX,IERR)
            WRITE (LP,90)
            CALL SUERRS (LP,2,-1)
            ISTAT=1
            GO TO 40
C
C  SET PARAMETER ARRAY VERSION NUMBER
20    IVTYPE=ARRAY(1)
C
C  SET INDICATOR HOW LIST WAS ORDERED
      IORDER=ARRAY(2)
C
C  POSITIONS 3 AND 4 ARE UNUSED
      UNUSED(1)=ARRAY(3)
      UNUSED(2)=ARRAY(4)
C
C  SET NUMBER STATIONS IN LIST
      NUMSTA=ARRAY(5)
C
      NPOS=5
C
      IF (NUMSTA.EQ.0) GO TO 40
C
C  CHECK FOR SUFFICIENT SPACE TO STORE POINTERS
      IF (MAXSTA.GE.NUMSTA) GO TO 30
         WRITE (LP,70) MAXSTA,NUMSTA
         CALL SUERRS (LP,2,-1)
         ISTAT=1
         GO TO 40
C
C  SET RECORD LOCATION OF PARAMETERS IN PARAMETRIC DATA BASE
30    IPOS=NPOS*4+1
      CALL SUBSTR (ARRAY,IPOS,NUMSTA*2,IPNTRS,1)
      NPOS=NPOS+(NUMSTA+1)/2
C
C
      IF (LDEBUG.EQ.0) GO TO 40
         WRITE (IOSDBG,100) NPOS,NFILL,IPTRNX,IVTYPE
         CALL SULINE (IOSDBG,1)
         CALL SUPDMP (TYPE,'BOTH',0,NPOS,ARRAY,ARRAY)
C
      IF (ISTAT.EQ.0) WRITE (LP,110) TYPE
      IF (ISTAT.EQ.0) CALL SULINE (LP,2)
      IF (ISTAT.GT.0) WRITE (LP,120) TYPE
      IF (ISTAT.GT.0) CALL SULINE (LP,2)
C
40    IF (ISTRCE.GT.0) WRITE (IOSDBG,130)
      IF (ISTRCE.GT.0) CALL SULINE (IOSDBG,1)
C
      RETURN
C
C- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C
50    FORMAT (' *** ENTER SRODLY')
60    FORMAT ('0*** ERROR - IN SRODLY - INVALID TYPE CODE : ',A4)
70    FORMAT ('0*** ERROR - IN SRODLY - POINTER ARRAY CAN HOLD ',I4,
     *   'VALUES HOWEVER ',I4,' POINTERS ARE STORED IN PARAMETER ',
     *   'ARRAY.')
90    FORMAT ('0*** ERROR - IN SRODLY - UNSUCCESSFUL CALL TO RPPREC.')
100   FORMAT (' NPOS=',I3,3X,'NFILL=',I3,3X,'IPTRNX=',I3,3X,
     *   'IVTYPE=',I3)
110   FORMAT ('0*** NOTE - ',A4,' PARAMETERS SUCCESSFULLY READ.')
120   FORMAT ('0*** NOTE - ',A4,' PARAMETERS NOT SUCCESSFULLY READ.')
130   FORMAT (' *** EXIT SRODLY')
C
      END
