C MEMBER SWGBOX
C-----------------------------------------------------------------------
C
C                             LAST UPDATE: 07/27/94.13:06:51 BY $WC20SV
C
C @PROCESS LVL(77)
C
C DESC WRITE GRID BOX PARAMETERS
C
      SUBROUTINE SWGBOX (IVGBOX,GBOXID,NUGBOX,BOXLOC,IBXADJ,IBXMDR,
     *   UNUSED,UNSD,LARRAY,ARRAY,IPTR,DISP,
     *   ISTAT)
C
      REAL XNEW/4HNEW /,XOLD/4HOLD /
C
      DIMENSION ARRAY(LARRAY)
      DIMENSION UNUSED(1)
      INCLUDE 'scommon/dimgbox'
C
      INCLUDE 'uio'
      INCLUDE 'scommon/sudbgx'
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/ppinit_write/RCS/swgbox.f,v $
     . $',                                                             '
     .$Id: swgbox.f,v 1.1 1995/09/17 19:16:07 dws Exp $
     . $' /
C    ===================================================================
C
C
C
      IF (ISTRCE.GT.0) WRITE (IOSDBG,80)
      IF (ISTRCE.GT.0) CALL SULINE (IOSDBG,1)
C
C  SET DEBUG LEVEL
      LDEBUG=ISBUG('GBOX')
C
      IF (LDEBUG.GT.0) THEN
         WRITE (IOSDBG,90) IVGBOX,UNSD,LARRAY
         CALL SULINE (IOSDBG,1)
         ENDIF
C
      ISTAT=0
      NUMERR=0
C
C  CHECK FOR SUFFICIENT SPACE IN PARAMETER ARRAY
      MINLEN=17
      IF (LDEBUG.GT.0) THEN
         WRITE (IOSDBG,110) MINLEN
         CALL SULINE (IOSDBG,1)
         ENDIF
      IF (MINLEN.LE.LARRAY) GO TO 10
         WRITE (LP,120) LARRAY,MINLEN
         CALL SUERRS (LP,2,NUMERR)
         ISTAT=1
         GO TO 70
C
C  STORE PARAMETER ARRAY VERSION NUMBER
10    ARRAY(1)=IVGBOX+.01
C
C  STORE GRID BOX IDENTIFIER
      ARRAY(2)=GBOXID(1)
      ARRAY(3)=GBOXID(2)
C
C  STORE DEGREE BOX NUMBER
      ARRAY(4)=NUGBOX+.01
C
      NPOS=4
C
C  UNUSED POSITIONS
      DO 20 I=1,2
         NPOS=NPOS+1
         ARRAY(NPOS)=UNSD
20       CONTINUE
C
C  STORE LATITUDE AND LONGITUDE
      DO 30 I=1,2
         NPOS=NPOS+1
         ARRAY(NPOS)=BOXLOC(I)
30       CONTINUE
C
C  STORE ADJACENT GRID BOX NUMBERS
      DO 40 I=1,8
         NPOS=NPOS+1
         ARRAY(NPOS)=IBXADJ(I)+.01
40       CONTINUE
C
C  STORE MDR USAGE INDICATOR
      NPOS=NPOS+1
      ARRAY(NPOS)=IBXMDR+.01
C
C  WRITE PARAMETER RECORD TO FILE
      IF (LDEBUG.GT.0) THEN
         WRITE (IOSDBG,100) NPOS
         CALL SULINE (IOSDBG,1)
         ENDIF
      CALL SUDOPN (1,'PPP ',IERR)
      IPTR=0
      CALL WPPREC (GBOXID,'GBOX',NPOS,ARRAY,IPTR,IERR)
      IF (IERR.EQ.0) GO TO 50
         CALL SWPPST (GBOXID,'GBOX',NPOS,IPTR,IERR)
         WRITE (LP,130) IERR
         CALL SUERRS (LP,2,NUMERR)
         ISTAT=3
C
50    IF (LDEBUG.GT.0) CALL SUPDMP ('GBOX','BOTH',0,NPOS,ARRAY,ARRAY)
C
      IF (ISTAT.EQ.0.AND.DISP.EQ.XNEW) WRITE (LP,140) GBOXID
      IF (ISTAT.EQ.0.AND.DISP.EQ.XOLD) WRITE (LP,150) GBOXID
      IF (ISTAT.EQ.0) CALL SULINE (LP,2)
      IF (ISTAT.EQ.0) CALL SUDWRT (1,'PPP ',IERR)
      IF (ISTAT.GT.0) WRITE (LP,160) GBOXID
      IF (ISTAT.GT.0) CALL SULINE (LP,2)
C
70    IF (ISTRCE.GT.0) WRITE (IOSDBG,170)
      IF (ISTRCE.GT.0) CALL SULINE (IOSDBG,1)
C
      RETURN
C
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C
80    FORMAT (' *** ENTER SWGBOX')
90    FORMAT (' IVGBOX=',I2,3X,'UNSD=',F7.2,3X,'LARRAY=',I5)
100   FORMAT (' NPOS=',I5)
110   FORMAT (' MINLEN=',I5)
120   FORMAT ('0*** ERROR - IN SWGBOX - NOT ENOUGH SPACE IN PARAMETER ',
     *   'ARRAY: NUMBER OF WORDS IN PARAMETER ARRAY=',I5,3X,
     *   'NUMBER OF WORDS NEEDED=',I5)
130   FORMAT ('0*** ERROR - IN SWGBOX - UNSUCCESSFUL CALL TO WPPREC : ',
     *   'STATUS CODE=',I3)
140   FORMAT ('0*** NOTE - GBOX PARAMETERS SUCCESSFULLY WRITTEN ',
     *   'FOR IDENTIFIER ',2A4,'.')
150   FORMAT ('0*** NOTE - GBOX PARAMETERS SUCCESSFULLY UPDATED ',
     *   'FOR IDENTIFIER ',2A4,'.')
160   FORMAT ('0*** NOTE - GBOX PARAMETERS NOT ',
     *   'SUCCESSFULLY WRITTEN FOR IDENTIFIER ',2A4,'.')
170   FORMAT (' *** EXIT SWGBOX')
C
      END
