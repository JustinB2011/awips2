C MODULE SWRRS
C-----------------------------------------------------------------------
C
C  ROUTINE TO WRITE STATION RRS PARAMETERS.
C
      SUBROUTINE SWRRS (IVRRS,STAID,NBRSTA,DESCRP,STATE,NRRSTP,
     *   NMISS,NDIST,RRSTYP,URMISS,IRTIME,NVLPOB,MNODAY,NUMOBS,
     *   ITSREC,INTERP,EXTRAP,FLOMIN,FRACT,UNSD,LARRAY,ARRAY,IPTR,
     *   DISP,
     *   IWRITE,NPOS,ISTAT)
C
      CHARACTER*4 DISP
C      
      DIMENSION ARRAY(LARRAY)
C
      INCLUDE 'scommon/dimsta'
      INCLUDE 'scommon/dimrrs'
C
      INCLUDE 'uio'
      INCLUDE 'scommon/sudbgx'
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/ppinit_write/RCS/swrrs.f,v $
     . $',                                                             '
     .$Id: swrrs.f,v 1.2 1998/04/10 16:20:58 dws Exp $
     . $' /
C    ===================================================================
C
C
C
      IF (ISTRCE.GT.0) THEN
         WRITE (IOSDBG,190)
         CALL SULINE (IOSDBG,1)
         ENDIF
C
C  SET DEBUG LEVEL
      LDEBUG=ISBUG('RRS ')
C
      IF (LDEBUG.GT.0) THEN
         WRITE (IOSDBG,*)
     *      ' IVRRS=',IVRRS,
     *      ' UNSD=',UNSD,
     *      ' LARRAY=',LARRAY,
     *      ' '
         CALL SULINE (IOSDBG,1)
         ENDIF
C
      ISTAT=0
C
      IF (IWRITE.EQ.-1) GO TO 170
C
C  CHECK FOR SUFFICIENT SPACE IN PARAMETER ARRAY
      NUM=7
      IF (NDIST.GT.0) NUM=NUM+1
      MINLEN=14+(NRRSTP*NUM)+(NMISS*2)+(NDIST*24)
      IF (LDEBUG.GT.0) THEN
         WRITE (IOSDBG,*) ' MINLEN=',MINLEN
         CALL SULINE (IOSDBG,1)
         ENDIF
      IF (MINLEN.GT.LARRAY) THEN
         WRITE (LP,210) LARRAY,MINLEN
         CALL SUERRS (LP,2,-1)
         ISTAT=1
         GO TO 180
         ENDIF
C
      NPOS=0
C
C  STORE PARAMETER ARRAY VERSION NUMBER
      NPOS=NPOS+1
      ARRAY(NPOS)=IVRRS+.01
C
C  STORE STATION IDENTIFIER
      NCHAR=4
      NWORDS=LEN(STAID)/NCHAR
      NCHK=2
      IF (NWORDS.NE.NCHK) THEN
         WRITE (LP,200) 'STAID',NWORDS,NCHK,STAID
         CALL SUERRS (LP,2,-1)
         ISTAT=1
         GO TO 180
         ENDIF
      DO 10 I=1,NWORDS
         NPOS=NPOS+1
         N=(I-1)*NCHAR+1
         CALL SUBSTR (STAID(N:N),1,4,ARRAY(NPOS),1)
10       CONTINUE
C
C  STORE USER SPECIFIED STATION NUMBER
      NPOS=NPOS+1
      ARRAY(NPOS)=NBRSTA+.01
C
C  STORE DESCRIPTIVE INFORMATION
      NCHAR=4
      NWORDS=LEN(DESCRP)/NCHAR
      NCHK=5
      IF (NWORDS.NE.NCHK) THEN
         WRITE (LP,200) 'DESCRP',NWORDS,NCHK,STAID
         CALL SUERRS (LP,2,-1)
         ISTAT=1
         GO TO 180
         ENDIF
      DO 20 I=1,NWORDS
         NPOS=NPOS+1
         N=(I-1)*NCHAR+1
         CALL SUBSTR (DESCRP(N:N),1,NCHAR,ARRAY(NPOS),1)
20       CONTINUE
C
C  STORE STATE IDENTIFIER
      NPOS=NPOS+1
      ARRAY(NPOS)=STATE
C
C  THE NEXT POSITION IS UNUSED
      NPOS=NPOS+1
      ARRAY(NPOS)=UNSD
C
C  SET NUMBER OF DATA TYPES
      NPOS=NPOS+1
      ARRAY(NPOS)=NRRSTP+.01
C
C  SET NUMBER OF DATA TYPES FOR WHICH MISSING IS NOT ALLOWED
      NPOS=NPOS+1
      ARRAY(NPOS)=NMISS+.01
C
C  SET NUMBER OF DATA TYPES FOR WHICH DISTRIBUTION IS ALLOWED
      NPOS=NPOS+1
      ARRAY(NPOS)=NDIST+.01
C
C  SET DATA TYPE CODES
      DO 30 I=1,NRRSTP
         NPOS=NPOS+1
         CALL SUBSTR (RRSTYP(I),1,4,ARRAY(NPOS),1)
30       CONTINUE
C
C  SET INDICATOR WHETHER DATA IS ALLOWED
      DO 40 I=1,NRRSTP
         NPOS=NPOS+1
         CALL SUBSTR (URMISS(I),1,4,ARRAY(NPOS),1)
40       CONTINUE
C
C  SET DATA TIME INTERVAL (HOURS)
      DO 50 I=1,NRRSTP
         NPOS=NPOS+1
         IF (IRTIME(I).LT.0) ARRAY(NPOS)=IRTIME(I)-.01
         IF (IRTIME(I).GT.0) ARRAY(NPOS)=IRTIME(I)+.01
50       CONTINUE
C
C  SET NUMBER OF VALUES PER OBSERVATION
      DO 60 I=1,NRRSTP
         NPOS=NPOS+1
         ARRAY(NPOS)=NVLPOB(I)+.01
60       CONTINUE
C
C  SET MINIMUM NUMBER OF DAYS OF DATA TO BE RETAINED IN PPDB
      DO 70 I=1,NRRSTP
         NPOS=NPOS+1
         ARRAY(NPOS)=MNODAY(I)+.01
70       CONTINUE
C
C  SET TYPICAL NUMBER OF OBSERVATIONS HELD IN PPDB
      DO 80 I=1,NRRSTP
         NPOS=NPOS+1
         ARRAY(NPOS)=NUMOBS(I)+.01
80       CONTINUE
C
C  SET RECORD NUMBER OF TIME SERIES HEADER
      DO 90 I=1,NRRSTP
         NPOS=NPOS+1
         ARRAY(NPOS)=ITSREC(I)+.01
         IF (IWRITE.EQ.1) THEN
            IF (IRTIME(I).GT.0.AND.ITSREC(I).EQ.0) THEN
               WRITE (LP,220) RRSTYP(I),STAID
               CALL SUWRNS (LP,2,-1)
               ENDIF
            ENDIF
90       CONTINUE
C
      IF (NMISS.EQ.0) GO TO 120
C
C  SET INTERPOLATION OPTION
      DO 100 I=1,NMISS
         NPOS=NPOS+1
         ARRAY(NPOS)=INTERP(I)+.01
100      CONTINUE
C
C  SET EXTRAPOLATION RECESSION CONSTANT
      DO 110 I=1,NMISS
         NPOS=NPOS+1
         ARRAY(NPOS)=EXTRAP(I)
110      CONTINUE
C
120   IF (NDIST.EQ.0) GO TO 160
C
C  SET MINIMUM FLOW
      DO 130 I=1,NRRSTP
         NPOS=NPOS+1
         ARRAY(NPOS)=FLOMIN(I)
130      CONTINUE
C
C  SET FRACTION OF FLOW OCCURRING IN EACH HOUR
      DO 150 I=1,NRRSTP
         DO 140 J=1,24
            IF (FLOMIN(I).EQ.-997.) GO TO 150
               NPOS=NPOS+1
               ARRAY(NPOS)=FRACT(J,I)
140         CONTINUE
150      CONTINUE
C
160   IF (ISTAT.GT.0) GO TO 180
      IF (IWRITE.EQ.0) GO TO 180
C
C  OPEN DATA BASE
170   CALL SUDOPN (1,'PPP ',IERR)
      IF (IERR.NE.0) THEN
         ISTAT=1
         GO TO 180
         ENDIF
C
C  WRITE PARAMETER RECORD TO FILE
      IF (LDEBUG.GT.0) THEN
         WRITE (IOSDBG,*) 'NPOS=',NPOS
         CALL SULINE (IOSDBG,1)
         ENDIF
      CALL SUDOPN (1,'PPP ',IERR)
      IPTR=0
      CALL WPPREC (STAID,'RRS ',NPOS,ARRAY,IPTR,IERR)
      IF (IERR.NE.0) THEN
         CALL SWPPST (STAID,'RRS ',NPOS,IPTR,IERR)
         WRITE (LP,230) IERR
         CALL SUERRS (LP,2,-1)
         ISTAT=2
         ENDIF
C
      IF (LDEBUG.GT.0) CALL SUPDMP ('RRS ','BOTH',0,NPOS,ARRAY,ARRAY)
C
      IF (ISTAT.EQ.0) THEN
         IF (DISP.EQ.'NEW') THEN
            WRITE (LP,240) STAID
            CALL SULINE (LP,2)
            ENDIF
         IF (DISP.EQ.'OLD') THEN
            WRITE (LP,250) STAID
            CALL SULINE (LP,2)
            ENDIF
         CALL SUDWRT (1,'PPP ',IERR)
         ENDIF
      IF (ISTAT.GT.0) THEN
         WRITE (LP,260) STAID
         CALL SULINE (LP,2)
         ENDIF
C
180   IF (ISTRCE.GT.0) THEN
         WRITE (IOSDBG,270)
         CALL SULINE (IOSDBG,1)
         ENDIF
C
      RETURN
C
C- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C
190   FORMAT (' *** ENTER SWRRS')
200   FORMAT ('0*** ERROR - IN SWSTAN - NUMBER OF WORDS IN VARIABLE ',
     *   A,'(',I2,') IS NOT ',I2,' FOR STATION ',A,'.')
210   FORMAT ('0*** ERROR - IN SWRRS - NOT ENOUGH SPACE IN PARAMETER ',
     *   'ARRAY: NUMBER OF WORDS IN PARAMETER ARRAY=',I5,3X,
     *   'NUMBER OF WORDS NEEDED=',I5)
220   FORMAT ('0*** WARNING - IN SWRRS - TIME SERIES RECORD NUMBER ',
     *   'IS ZERO FOR DATA TYPE ',A4,' FOR STATION ',A,'.')
230   FORMAT ('0*** ERROR - IN SWRRS - UNSUCCESSFUL CALL TO WPPREC : ',
     *   'STATUS CODE=',I3)
240   FORMAT ('0*** NOTE - RRS  PARAMETERS SUCCESSFULLY ',
     *   'WRITTEN FOR STATION ',A,'.')
250   FORMAT ('0*** NOTE - RRS  PARAMETERS SUCCESSFULLY ',
     *   'UPDATED FOR STATION ',A,'.')
260   FORMAT ('0*** NOTE - RRS  PARAMETERS NOT SUCCESSFULLY ',
     *   'WRITTEN FOR STATION ',A,'.')
270   FORMAT (' *** EXIT SWRRS')
C
      END
