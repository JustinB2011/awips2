C MODULE PWCNTL
C-----------------------------------------------------------------------
C
C  MAIN ROUTINE FOR DUMPPP24 COMMAND.
C
      SUBROUTINE PWCNTL (UNRC,UNWC,UNWD,UNWTTY,IG,IUNFLG)
C
      INCLUDE 'uiox'
      INCLUDE 'udebug'
C
      CHARACTER*4 TYPS(64)
      CHARACTER*8 DBEGD,DENDD
      INTEGER UNRC,UNWC,UNWD,UNWTTY
      INTEGER STAID(2)
      DIMENSION LOCA(64)
      PARAMETER (MSSTA=100)
      DIMENSION LSSTA(MSSTA*2)
      DIMENSION LSMSF(MSSTA)
      PARAMETER (MMLM=4000)
      DIMENSION MMS1(MMLM),MMS2(MMLM),MMSA(MMLM),MMLD(MMLM)
      INTEGER   MMNO
      DIMENSION MMD1(MMLM),MMD2(MMLM),MMD3(MMLM),MMD4(MMLM),MMD5(MMLM)
      PARAMETER (LIARAY=500)
      DIMENSION IARAY(LIARAY)
      PARAMETER (LIDATA=1000)
      INTEGER*2 IDATA(LIDATA)
      CHARACTER*4  CHPP24
      INTEGER      IPP24
      EQUIVALENCE (IPP24,CHPP24)
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/ppdutil_dmp24/RCS/pwcntl.f,v $
     . $',                                                             '
     .$Id: pwcntl.f,v 1.9 2002/02/11 20:51:51 dws Exp $
     . $' /
C    ===================================================================
C
      DATA CHPP24/'PP24'/
C
C
      ICOND=0
C
      CALL PVSUBB ('PWCNTL  ',ICOND)
C
C  READ PREPROCESSOR PAREMETRIC DATA BASE CONTROLS
      CALL RPPPCO (ISTAT)
      IF (ISTAT.NE.0) THEN
         WRITE (LP,10)
10    FORMAT ('0**ERROR** RETURNING FROM RPPPCO IN ROUTINE PWCNTL')
         GO TO 120
         ENDIF
C
C  SAVE CURRENT UNIT NUMBERS AND TRACE INDICATOR 
      ICDSV=ICD
      LPSV=LP
      LPESV=LPE
      IPDTSV=IPDTR
      IOGDSV=IOGDB
C
C  SET CURRENT UNIT NUMBERS AND TRACE INDICATOR TO ARGUMENTS
      ICD=UNRC
      LP=UNWD
      LPE=UNWC
      IF (IG.EQ.1) THEN
         IPDTR=1
         IOGDB=LP
         ENDIF
C
C  GET AND CHECK DATES
      DBEGD='*-6'
      DENDD='*'
      CALL PWDDC (DBEGD,DENDD,ICOND)
      CALL PVGDC (UNWC,UNWTTY,JULDB,INTHRB,JULDE,JULHB,JULHE,ICOND)
      MXDAYS=7
      CALL PWCDC (JULDB,JULDE,CHPP24,MXDAYS,ICOND)
C
C  GET STATIONS AND OPTIONS
      CALL PWGSL (UNWC,UNWTTY,LSSTA,LSMSF,MSSTA,NOFS,KNTL,KPP,KZK,KZ,
     *   ICOND)
      IF (ICOND.NE.0) GO TO 110
C
      NXSTA=0
      MMNO=0
C
C  GET STATION INFORMATION
20    ISORT=0
      NINFSTA=0
      MINFSTA=0
      CALL PVGNS (ISORT,NINFSTA,MINFSTA,LINFSTA,INFSTA,
     *   NXSTA,STAID,NUMID,IPGENL,NTYPES,TYPS,LOCA,ICOND)
      IF (ICOND.EQ.1) GO TO 73
      IF (ICOND.EQ.2) GO TO 75
C
C  CHECK IF STATION TO BE PROCESSED
      CALL PVVNS (STAID,LSSTA,LSMSF,NOFS,IMF,ICOND)
      IF (ICOND.NE.0) GO TO 73
C
      DO 70 I=1,NTYPES
         IF (CHPP24.NE.TYPS(I)) GO TO 70
C     STORE INFORMATION FOR EACH STATION FOUND
         IF (MMNO.GT.MMLM) THEN
            WRITE (LP,40) MMLM
40    FORMAT ('0**WARNING** MAXIMUM NUMBER OF STATIONS THAT CAN ',
     *   'BE PROCESSED (',I5,') EXCEEDED.')
            GO TO 75
            ENDIF
          MMNO=MMNO+1
          MMS1(MMNO)=STAID(1)
          MMS2(MMNO)=STAID(2)
          MMSA(MMNO)=0
C     SET SORT CONDITIONS: IGETC=NEED STATE CODE
C                          IGETD=NEED STATE DESC
C                          IGETP=NEED PCPN SUM
C     (IF NEEDED, GET STATE-CODE AND/OR DESC FROM RPPREC)
C     (IF NEEDED, PLACE STATE CODE IN LEFT 2 BYTES OF MMSA)
C     (IF NEEDED, PLACE DESCRIPTION IN MMD1 THRU MMD5)
C     (IF NEEDED, GET PCPN SUM FROM PWNU24, PLACE IN MMLD)
         IGETC=0
         IGETD=0
         IGETP=0
         ISTAT=0
         IF (KNTL.EQ.1) IGETC=1
         IF (KNTL.EQ.2) IGETC=1
         IF (KNTL.EQ.3) IGETC=1
         IF (KNTL.EQ.4) IGETC=1
         IF (KNTL.EQ.2) IGETD=1
         IF (KNTL.EQ.4) IGETD=1
         IF (KNTL.EQ.6) IGETD=1
         IF (KNTL.EQ.8) IGETD=1
         IF (KNTL.EQ.3) IGETP=1
         IF (KNTL.EQ.4) IGETP=1
         IF (KNTL.EQ.7) IGETP=1
         IF (KNTL.EQ.8) IGETP=1
         IF (IGETC.EQ.1.OR.IGETD.EQ.1) THEN
            IPTR=0
            CALL RPPREC (STAID,'GENL',IPTR,LIARAY,IARAY,NFILL,IPTRNX,
     *         ISTAT)
            IF (ISTAT.NE.0) THEN
               IGETC=0
               IGETD=0
               WRITE (LP,50) STAID
50    FORMAT ('0**WARNING** CANNOT GET DESCRIPTION OR STATE FOR ',
     *   'SORTING STATION ',2A4,'.')
               ENDIF
            ENDIF
         LDSUM=0
         IF (IGETP.EQ.1.OR.KPP.GT.0) THEN
            CALL PWNU24 (STAID,IPP24,JULDB,JULDE,IUNFLG,IDATA,LIDATA,
     *         LDSUM)
            ENDIF
C     DO NOT INCLUDE STATION IF PCPN IS LESS THAN KPP UNLESS MISSING
C     DATA IS ALLOWED (IMF=8 OR MORE)
         IF (IMF.LE.7) THEN
            IF (LDSUM.LT.KPP) THEN
               MMNO=MMNO-1
               GO TO 70
               ENDIF
            ENDIF
         IF (IGETC.EQ.1) CALL PWLW (IARAY(16),MMSA(MMNO))
         IF (IGETD.EQ.1) THEN
            MMD1(MMNO)=IARAY(05)
            MMD2(MMNO)=IARAY(06)
            MMD3(MMNO)=IARAY(07)
            MMD4(MMNO)=IARAY(08)
            MMD5(MMNO)=IARAY(09)
            ENDIF
         IF (IGETP.EQ.1) MMLD(MMNO)=LDSUM
C    PUT IMF INDICATOR (65+++) INTO LEFT WORD OF MMSA
C    (IMF IS INDICATOR FOR ALLOWING MISSING DATA)
         MMSA(MMNO)=MMSA(MMNO)+65536*IMF
         ICOND=0
70       CONTINUE
C
73    IF (NXSTA.GT.0) GO TO 20
C
C  SORT LIST
75    IF (MMNO.LE.0) KNTL=0
      IF (KNTL.EQ.1)
     *  CALL PWQS1 (MMSA,MMS1,MMS2,0,MMNO)
      IF (KNTL.EQ.2)
     *  CALL PWQS2 (MMSA,MMS1,MMS2,MMD1,MMD2,MMD3,MMD4,MMD5,0,MMNO)
      IF (KNTL.EQ.3)
     *  CALL PWQS3 (MMSA,MMS1,MMS2,MMLD,0,MMNO)
      IF (KNTL.EQ.4)
     *  CALL PWQS4 (MMSA,MMS1,MMS2,MMD1,MMD2,MMD3,MMD4,MMD5,MMLD,0,MMNO)
      IF (KNTL.EQ.5)
     *  CALL PWQS1 (MMSA,MMS1,MMS2,1,MMNO)
      IF (KNTL.EQ.6)
     *   CALL PWQS2 (MMSA,MMS1,MMS2,MMD1,MMD2,MMD3,MMD4,MMD5,1,MMNO)
      IF (KNTL.EQ.7)
     *   CALL PWQS3 (MMSA,MMS1,MMS2,MMLD,1,MMNO)
      IF (KNTL.EQ.8)
     *  CALL PWQS4 (MMSA,MMS1,MMS2,MMD1,MMD2,MMD3,MMD4,MMD5,MMLD,1,MMNO)
C
      ISKPLN=0
C
C  SET FLAG TO SKIP LINES BETWEEN STATES FOR STATE SORT
      IF (KNTL.EQ.1) ISKPLN=1
      IF (KNTL.EQ.2) ISKPLN=1
      IF (KNTL.EQ.3) ISKPLN=1
      IF (KNTL.EQ.4) ISKPLN=1
C
C  SET JSTCK TO FIRST OUTPUT STATE SO NO SKIP AT FIRST
      JSTCK=0
      IF (ISKPLN.EQ.1.AND.MMNO.GT.0) CALL PWRW (MMSA(1),JSTCK)
C        
C  PRINT STATION INFORMATION
      NXLIN=0
      DO 90 MM=1,MMNO
C     PRINT STATION INFORMATION
        STAID(1)=MMS1(MM)
        STAID(2)=MMS2(MM)
        IMF=MMSA(MM)/65536
        CALL PWPP24 (UNWD,UNWTTY,STAID,CHPP24,JULDB,JULDE,IUNFLG,IMF,
     *     KZK,KZ,IDATA,LIDATA,NXLIN,ISKPLN,JSTCK,IARAY,LIARAY,ICOND)
90      CONTINUE
C
      NXLIN=1
C
C   RESET UNIT NUMBERS AND TRACE INDICATOR TO ORIGINALS
110   LPE=LPESV
      LP=LPSV
      ICD=ICDSV
      IPDTR=IPDTSV
      IOGDB=IOGDSV
C
120   CALL PVSUBE ('PWCNTL  ',ICOND)
C
      RETURN
C
      END
