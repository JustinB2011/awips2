C MEMBER LKRT51
C DESC PERFORM LAKE/RESERVOIR ROUTING ONE PERIOD AT A TIME
C
C@PROCESS LVL(77)
C
      SUBROUTINE LKRT51 (IP,XH2,FLIS,EFLI,WK,NPA,IS,FA,FB,CF,ELDS) 
C
CC  KSH CHANGE START
CC  NOTE: FROM SSARR ROUTINE LAKRT (IP,XH2,FLIS,EFLI,IS, FA, FB, CF)
CC  THE NAME OF ROUTINE LAKRT HAS BEEN CHANGED TO LKRT51
CC  THE NAME OF ROUTINE FEXTR HAS BEEN CHANGED TO EXTR51
CC  THE NAME OF ROUTINE STLU2 HAS BEEN CHANGED TO TLU251
CC  THE NAME OF ROUTINE TLU3 HAS BEEN CHANGED TO TLU351
C***********************************************************************
C
C WORD IN IS( ) ARRAY, FORMAT(I=INTEGER,R=REAL,A-ALPHA), DESCRIPTION -
C  1 I, TOTAL NUMBER OF WORDS USED IN THE CURRENT ARRAY
C       = NO. OF POINTS OF Q-VS-E TABLE * 4 +
C         NO. OF POINTS OF BACKWATER TABLE * 3 + 99
C  2 I, STATION TYPE, = 3, TO INDICATE LAKE/RESERVOIR/BACKWATER,
C  3-24 NOT USED
C 25 I, POINTER TO REGULATION OPTION INFORMATION IN THE NPA( ) ARRAY
C 26-27 NOT USED
C 28 I, POINTER TO DISCHARGE-ELEVATION-STORAGE TABLE IN THE IS( ) ARRAY
C 29    NOT USED
C 30 I, BACKWATER CONTROL PARAMETER CODE, 
C       = 1, DOWNSTREAM RESERVOIR OUTFLOW CONTROL;
C       = 2, DOWNSTREAM RESERVOIR ELEVATION CONTROL
C 31 R  MINIMUM RESERVOIR RELEASE TO MEET DOWNSTREAM REQUIREMENT
C 32 I, POINTER TO BACKWATER TABLE IN THE IS( ) ARRAY
C 33 R, LAKE ELEVATION UPPER BOUND
C 34 R, LAKE ELEVATION LOWER BOUND
C 35 I, SCAN INDEX TO BACKWATER TABLE (USED BY SUBR.TLU3)
C 36 R, MAXIMUM TRIBUTARY FLOW TO SHUT DOWN RESERVOIR RELEASE
C 37    NOT USED
C 38 I, RESERVOIR STORAGE (NOTE- THIS IS INTEGER VALUE)
C 39-49 NOT USED
C 50 I  = 0, FOR A SIGLE NON-BACKWATER RESERVOIR;
C       = 2, FOR A TWO-RESERVOIR SYSTEM NOT USED
C 52 I, PTR TO THE COMPUTED FLOW-ELEV ARRAY AT PERIOD START (FA ARRAY)
C       AND END (FB ARRAY) FOR THE RESERVOIR CURRENTLY IN COMPUTATION
C 53-68 , NOT USED
C 69 R, RESERVOIR ELEVATION
C 70 NOT USED
C 71 R, RESERVOIR DISCHARGE
C WORD 72 THROUGH THE END OF THE RECORD ARE THE FOLLOWING VARIABLE
C        LENGTH ARRAYS-
C
C DISCHARGE-ELEVATION-STORAGE TABLE (LOCATED BY WORD 28, 4 WORDS/POINT)
C  DISCHARGE, AND ELEVATION ARE REAL, TS IS NOT USED, STORAGE IS INTEGER
C BACKWATER TABLE, LOCATED BY WORD 32. THREE WORDS PER POINT - OUTFLOW,
C    ELEVATION, AND CONTROLLING STATION PARAMETER (EIHER FLOW OR ELEV,
C    AS DESCRIBED BY WORD 30)
C
C***********************************************************************
CC  KSH CHANGE END
C
C 11/20/89 TRACE FOR BACKWATER ERROR ADDED. 
C        ROUTE A PERIOD THROUGH LAKE OR RESERVOIR. 
C        IP-INTERNAL STATION NUMBER 
C        XH2- HALF-PERIOD HOURS 
C       FLIS IS INFLOW AT START OF PERIOD 
C       EFLI- INFLOW TO LAKE AT END OF PERIOD 
C        IS(1) - CHARACTERISTIC RECORD. 
C        FA(1)-TIME VS. FLOW RECORD (FILE IOTA/IOTB) AT START OF 
C         COMPUTE PERIOD. 
C        FB(1)-TIME VS FLOW RECORD AT END OF COMPUTE PERIOD. 
C        CF - FLOW RATE TO STORAGE VOLUME CONVERSION FACTOR.(S = Q*CF) 
CC
CC  KSH CHANGE START
      INCLUDE 'common/fdbug'
      INCLUDE 'common/fctime'
      INCLUDE 'common/sarr51'
      INCLUDE 'common/mod151'
      COMMON/NQEL51/NQEL(2)
CC        COMMON/C1/ICLGT,NPALA,ICS(50000)
CC         COMMON/TIME/ITME(10),ICOP(4),ICOPAL,NTI,ITEND,IRIVER,
CC     C   ICLOCK,ICLCAL(5)
CC        COMMON/TIMEC/ NAMMTH(3,12),NAMDAY(3,7)
CC        CHARACTER*4 NAMMTH,NAMDAY
CC      COMMON/JOB/JOB(3),JDATE(3),JID(10),IRTRC,IHEAD,ICPRT,IRJOB,MEAS,
CC     C  IPRINT,IPUNIT,IPOVFL,IGRAPH,IPUNCH,IFCST,ICPAR,
CC    C  ICL36,ICL37,ICL38,ICL39,ICL40,ISTEP,IERROR,IPLIST(2),IPLPRV(2),
CC     C  ITITLE(8)
      DIMENSION WK(*),NPA(*) 
CC  KSH CHANGE END
CC
      DIMENSION IS(*), FA(*), FB(*), R(5), NR(5) 
CC
CC  KSH CHANGE START
      DIMENSION IRRC(2,7)
CC      EQUIVALENCE (ICS(1),NPA(1)) 
CC  KSH CHANGE END
CC
      EQUIVALENCE (R(1), NR(1)) 
      EQUIVALENCE (IQO,QO), (IQ1,Q1), (IEO, EO), (IE1, E1), (IW, W) 
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/fcst_ssarresv/RCS/lkrt51.f,v $
     . $',                                                             '
     .$Id: lkrt51.f,v 1.8 2000/07/28 12:50:49 page Exp $
     . $' /
C    ===================================================================
C
CC
CC  KSH CHANGE START
      DATA IRRC/4HFREE,4HFLOW,
     &          4HSETQ,4H    ,4HSETH,4H    ,4HSETS,4H    ,
     &          4HSETD,4HQ   ,4HSETD,4HH   ,4HSETD,4HS   /
CC  KSH CHANGE END
CC
C
C        RESET MANUAL CONTROL SWITCH 
      SWF = 0. 
C        EXTRACT INITIAL CONDITIONS 
      IQO = IS(71) 
      QON = QO 
      IEO=IS(69) 
      ISTO = IS(38) 
C        SETUP FOR Q VS E TABLE REFERENCE 
      IXQ = IS(28) 
C 
CC
CC  KSH CHANGE START
      IQREL = 0
      IW = IS(31) 
      QRELMN = W 
      ISHUT=0
      IW = IS(36) 
      QSHUT = W 
      IF(IS(50).EQ.0) THEN
        LOCQ = (NQEL(IRES)-1)*4
        QX = WK(IXQ+7+LOCQ)
        IF(QX.LE.0.0) QC=1.0+E10
      ENDIF
CC  KSH CHANGE END
CC
      IW = IS(33) 
      EUB = W 
      IW = IS(34) 
      ELB = W 
C        COMPUTE AVERAGE INFLOW TO LAKE FOR PERIOD 
      FLI = (FLIS + EFLI)/2. 
C     SET TOLERANCE FACTOR 
      TEST=0.001*QO 
C 
C        TEST - BACKWATER STATION 
 4    IF(IS(50)) 5,90,5 
   5  ILB = IS(32) 
C     TEST FOR BKWTR PARAMETER CODE 
C       1 = OUTFLOW 
C       2 = ELEVATION 
      IF(IS(30)-1)10,20,10 
C        CONTROL IS ELEVATION. 
   10 J = 1
      SCALE = 1. 
      GO TO 30 
   20 J = 0 
C        CONTROL IS OUTFLOW. MATCH SCALING OF Q TO BACKWATER TABLE. 
C         BACKWATER TABLE CONTROLLING VALUE IS READ THROUGH FORMAT F9.3 
CC
CC  KSH CHANGE START
CC      SCALE = 100. 
      SCALE = 1.
CC  KSH CHANGE END
CC
 30   J=IS(50)*2+J 
 50   IF(IS(50)-IP) 70,70,60 
C        BACKWATER FROM  DOWNSTREAM RESERVOIR
   60 E2 = FA(J) /SCALE 
      GO TO 90 
C        BACKWATER FROM TRIBUTARY 
   70 E2 = FB(J) /SCALE
CC
CC  KSH CHANGE START
      QRELMN=QRELMN-E2
      IF(QRELMN.LE.0.0) QRELMN=0.0
      IF(E2.GE.QSHUT) THEN
         ISHUT=1
         R(1)=0.0
         GO TO 150
      ENDIF
CC  KSH CHANGE END
CC
C        TEST FOR MANUAL CONTROL 
 90   IF(IS(25).EQ.0) GO TO 400 
      I = IS(25) 
C        GET PERIOD START TIME AND LENGTH. 
      W = FA(1) 
      ITO = IW 
      W = FB(1) 
      ITE=IW 
      IH = ITE-ITO 
C        GO PROCESS REGULATION 
      CALL EXTR51 (ITO, IH, NPA(I), R(1), NC, ITR) 
      ICODE = NC
      VALUE = R(1)
      NC = NC+1 
      GO TO (400, 150, 180, 190, 210, 170, 220), NC 
C        GO FREEFLOW, NC=0 OR MORE THAN 6. 
C 
C        OUTFLOW SPECIFIED - ALSO USED BY STMT. 320 
 150  Q1 = R(1) 
      QON=QO 
C        COMPUTE DELTA STORAGE 
      DST  = CF*(FLI - ((QO + Q1)/2.)) 
      IDST = DST + SIGN(.5,DST) 
C        PERIOD-END STORAGE 
      IST1 = ISTO + IDST 
CC
CC  KSH CHANGE START
CC      IF(IST1.LE.0) IST1=0
      IF(IST1.GT.0) GO TO 250
      IST1 = 0
      Q1 = EFLI
CC  KSH CHANGE END
CC
      GO TO 250 
C        CHANGE IN ELEVATION SPECIFIED 
C        R(1) CONTAINS PER DAY RATE OF CHANGE 
 170  E1 = EO + (XH2/12.)*R(1) 
      GO TO 185 
C        ELEVATION SPECIFIED 
 180  IF(ITE-ITR) 183,182,182 
 182  E1 = R(1) 
      GO TO 185 
 183  XH = IH 
      X = ITR - ITO 
      E1 = EO + ((R(1) - EO)*XH)/X 
C        STMT ALSO ENTERED FROM STMT. 170 AND 290. 
 185  CALL TLU251(IS(IXQ),2,E1,R) 
      IST1 = NR(4) 
      IF(QX.GT.0.0) QC =  R(1) 
CC
CC  KSH CHANGE START
CC  COMPUTE MAXIMUM DELTA STORAGE FOR A MINIMUM OUTFLOW OF QRELMN 
      DST  = CF*(FLI-QRELMN)
      IDST = DST + SIGN(.5,DST) 
C        PERIOD-END STORAGE 
      IST1M = ISTO + IDST 
      IF(IST1.GT.IST1M) THEN
        IST1=IST1M
        CALL TLU251(IS(IXQ),4,IST1,R) 
        IF(QX.GT.0.0) QC = R(1) 
        E1 = R(2) 
        DQ = -EFLI
        GO TO 240
      ENDIF
CC  KSH CHANGE END
CC
      GO TO 200 
C        STORAGE LEVEL SPECIFIED 
C         STORAGE VALUES ARE KEPT INTERNALLY IN INTEGER FORM, AS ACRE- 
C          FEET OR 1000CUBIC METERS. OUTPUT FROM FEXTR IN R(1) IS 
C          IN REAL FORM. 
 190  IF(ITE - ITR) 195, 192, 195 
 192  IST1 = R(1) 
      GO TO 197 
 195  XH = IH 
      X = ITR - ITO 
      R(2) = ISTO 
      IST1 = R(2) + ((R(1) - R(2))*XH)/X 
      IF(IST1.LE.0) IST1=0
 197  CALL TLU251(IS(IXQ),4,IST1,R) 
      IF(QX.GT.0.0) QC = R(1) 
      E1 = R(2) 
C        COMPUTE DELTA-Q, CHANGE IN FLOW DURING PERIOD 
 200  DQ = (ISTO - IST1) 
      DQ = DQ/CF 
      SWE = 0. 
      GO TO 240 
C        DELTA-Q SPECIFIED 
 210  DQ = R(1) 
      IDST = DQ * CF + SIGN(.5,DQ) 
      DQ = -DQ 
      GO TO 230 
C        DELTA-STORAGE SPECIFIED - VALUE IS PER DAY RATE OF CHANGE. 
 220  R(1) = R(1) * XH2/12. 
      IDST = R(1) 
      DQ = -R(1)/CF 
C        COMPUTE PERIOD END STORAGE 
 230  IST1 = ISTO + IDST 
      IF(IST1.LE.0) IST1=0
      SWE = 1. 
C        COMPUTE PERIOD-END FLOW FOR SPECIFIED STORAGE OR ELEVATION. 
 240  Q1 = EFLI + DQ 
      QON = FLIS + DQ 
CC
CC  KSH CHANGE START
      IF(QON.GE.QRELMN .AND. Q1.GE.QRELMN) GO TO 245
      R(1) = Q1
      IF(Q1.LE.QRELMN) R(1)=QRELMN
      IQREL = 1
      GO TO 150
CC  KSH CHANGE END
CC
C        TEST SWE - NEED TO TLU FOR E1 AND HYDRO-CAPACITY (QC). 
 245  IF(SWE) 250, 260, 250 
C 
 250  CALL TLU251(IS(IXQ),4,IST1,R) 
      E1 = R(2) 
      IF(QX.GT.0.0) QC = R(1) 
C 
C        TEST ELEVATION BOUNDS OF POOL 
 260  IF(ELB - E1) 270, 300, 278 
 270  IF(E1 - EUB) 300, 300, 279 
C        SET ELEVATN AT BOUND AND TRY AGAIN 
 278  E1 = ELB 
      GO TO 280 
 279  E1 = EUB 
C        TEST SWF-SHOULD WE TRY AGAIN. 
 280  IF(SWF) 400, 290, 400 
 290  SWF =-1. 
      GO TO 185 
C 
C        TEST - BACKWATER 
 300  IF(IS(50)) 310,320,310 
C        GET HYDRAULIC CAPACITY FROM BACKWATER TABLE 
 310  CALL TLU351 (E1, E2, IS(35), IS(ILB), QC, KE) 
C
CC  KSH CHANGE START
CC      IF(IRTRC.LE.0) GO TO 320
CC      IF(KE.NE.0) THEN 
CC          IPC=IS(50) 
CC          WRITE(6,'('' LAKRT/TLU3 - ARGUMENT IS OUTSIDE OF SURFACE'', 
CC     1    '' DEFINED BY C2 TABLE.'' 
CC     2    /'' THIS STA: '',2A4,'', EL='',F12.3, 
CC     3    ''  CONTROL STA: '',2A4,'', E2='',F12.3)') 
CC     4    ICS(2*IP-1),ICS(2*IP),E1, 
CC     5    ICS(2*IPC-1),ICS(2*IPC),E2 
CC      ENDIF 
       IF(IBUG.LT.2 .OR. KE.LE.0) GO TO 320
       WRITE(IODBUG,1610) E1,E2
1610  FORMAT(5X,'LKRT51/TLU351 - ARGUMENT IS OUTSIDE OF SURFACE',
     1    ' DEFINED BY C2 TABLE.',
     & /10X,'BACKWATER ELEVATION, E1=',F10.2,
     & 5X,'DOWNSTREAM CONTROL VALUE, E2=',F10.2)
C        TEST HYDRAULIC CAPACITY 
CC  320  IF(QC) 330, 1000, 330 
CC  330  IF(QC-Q1) 400,1000,1000
 320  IF(IQREL.EQ.1) GO TO 1000
      IF(IS(50).NE.0) GO TO 330
      IF(QX.LE.0) GO TO 350
      IQC=QC
      IF(IQC) 330, 350, 330 
 330  IF(QC-Q1) 400,350,350
ccc 350  IF(IS(50).GT.0) GO TO 1000
 350  CONTINUE
      IF(SWF.LT.0.0) GO TO 1000
      IF(E1.LE.ELDS .AND. IS(50).GT.0) THEN
        R(1) = 0.0
        IQREL = 1
        GO TO 150
      ENDIF
      IF(Q1.GE.QRELMN) GO TO 1000
      R(1) = QRELMN
      IQREL = 1
      GO TO 150
CC  KSH CHANGE END
CC
C 
C        FREEFLOW LAKE 
C         TEST FOR BACKWATER 
cc 400  IF(IS(50)) 410,500,410 
 400  IF(IQREL.EQ.1) GO TO 1000
      IF(IS(50)) 410,500,410 
C 
C        BACKWATER 
C     START TRIAL ROUTINE 
C 
 410  N=20 
      QON = QO 
      Q1 = QO 
C
 420  DST = CF * (FLI - (QO + Q1)/2.) 
      IDST = DST + SIGN(.5, DST) 
      IST1 = ISTO + IDST 
      IF(IST1.LE.0) IST1=0
C 
C     GET CORRESPONDING ELEV E1 FROM STLU2 
      K=4 
      CALL TLU251(IS(IXQ),4,IST1,R) 
      E1 = R(2) 
C 
C     GIVEN E1 AND E2 GET TRIAL Q1T FROM TLU3 
      CALL TLU351(E1,E2, IS(35), IS(ILB), Q1T, KE) 
C
CC  KSH CHANGE START
C  STORE HYDRAULIC CAPACITY FOR OUTPUT
      IF(QX.GT.0.0) QC = Q1T
CC      IF(IRTRC.LE.0) GO TO 425
CC      WRITE(IODBUG,'('' LAKRT/TLU3 E1,E2,Q: '',2A4, 
CC     1 2F11.3,F10.1)') ICS(2*IP-1),ICS(2*IP),E1,E2,Q1T 
CC      IF(KE.NE.0) THEN 
CC          IPC=IS(50) 
CC          WRITE(6,'('' LAKRT/TLU3 - ARGUMENT IS OUTSIDE OF SURFACE'', 
CC     1    '' DEFINED BY C2 TABLE.'' 
CC     2    /'' THIS STA: '',2A4,'', EL='',F12.3, 
CC     3    ''  CONTROL STA: '',2A4,'', E2='',F12.3)') 
CC     4    ICS(2*IP-1),ICS(2*IP),E1, 
CC     5    ICS(2*IPC-1),ICS(2*IPC),E2 
CC      ENDIF 
      IF(IBUG.LT.2) GO TO 425
      ITER = 20-N+1
      WRITE(IODBUG,1611) E1,E2,Q1T,ITER
 1611 FORMAT(10X,'LKRT51/TLU351 E1,E2,Q,ITER: ',2F11.2,F10.0,I5)
      IF(KE.NE.0) WRITE(IODBUG,1610) E1,E2
CC  KSH CHANGE END
C
 425  IF(ABS(Q1T-Q1)-TEST)600, 600, 430 
C
C     SET NEW TRIAL Q1T USING BISECTION METHOD 
 430  IF(N) 600,600,440 
 440  N=N-1 
      IF(N.NE.19) GO TO 460 
      IF(Q1T.GT.Q1) GO TO 450 
C        SET LOWER LIMIT OF ESTIMATE 
      QL=Q1T 
      GO TO 462 
C        SET UPPER LIMIT OF ESTIMATE 
 450  QH=Q1T 
      GO TO 464 
 460  IF(Q1T.GT.Q1) GO TO 464 
C        TOO HIGH 
 462  QH=Q1 
      Q1=(QL+Q1)/2. 
      GO TO 420 
C        TOO LOW 
 464  QL=Q1 
      Q1=(QH+Q1)/2. 
      GO TO 420 
C 
C        LAKE 
cc 500  CALL TLU251(IS(IXQ),4,ISTO,R) 
 500  CALL TLU251(IS(IXQ),4,ISTO,R) 
      QON = QO 
C        TEST FOR RESERVOIR - ZERO OUTFLOW FROM Q-VS-E TABLE. 
      IF(QX.LE.0.0) GO TO 502
      IF(R(1)) 503, 502, 503 
C        RESERVOIR - CANNOT FREEFLOW 
 502  Q1 = EFLI 
      IST1 = ISTO 
      E1 = EO 
CC
CC  KSH CHANGE START
CC   DISCHARGE QRELMN IF DISCHARGE CURVE IS NOT ENTERED
      QON = FLIS
      IF(QON.GE.QRELMN .AND. Q1.GE.QRELMN) GO TO 600
      R(1) = Q1
      IF(Q1.LE.QRELMN) R(1)=QRELMN
      IQREL = 1
      GO TO 150
CC  KSH CHANGE END
CC
CCC   GO TO 600 ......... this statement cannot be reached
C 
C        FREEFLOW 
 503  TS = R(3) 
      N = 20 
      IF(TS-XH2) 510, 510, 505 
 505  TSR =(2.*XH2)/(XH2+TS) 
      DQ = QO - FLI 
      IF(.0001 - ABS(DQ)) 520, 510, 510 
 510  Q1 = QO 
      GO TO 530 
 520  Q1 = QO - (DQ*TSR) 
C 
C        COMPUTE RESULTING STORAGE LEVEL 
 530  DST  = CF*(FLI -(QO+Q1)/2.) 
      IDST = DST + SIGN(.5,DST) 
      IST1 = ISTO + IDST 
      CALL TLU251(IS(IXQ),4,IST1,R) 
      QC = R(1) 
      E1 = R(2) 
      IF( TEST - ABS(QC-Q1)) 540, 600, 600 
C        NOT CLOSE ENOUGH TO TABLE VALUE AT RESULTING STAGE. 
 540  IF(N) 550, 600, 550 
 550  N = N - 1 
      Q1 =(Q1+QC)/2. 
      GO TO 530 
C
C        TEST ELEVATION LOWER BOUND 
 600  IF(E1 - ELB) 610, 1000, 1000 
C 
C        ADJUST Q1 TO BRING ELEVATION TO ELB. 
 610  E1 = ELB 
      CALL TLU251(IS(IXQ),2,E1,R) 
      DQ =ISTO- NR(4) 
      DQ = DQ/CF 
      Q1 = EFLI + DQ 
      QON = FLIS + DQ 
      IST1 = NR(4) 
C 
C        STORE RESULTS 
 1000 IS(71) = IQ1 
      IS(69)=IE1 
      IS(38) = IST1 
      I = IS(52) 
1020  FB(2*IP)   = Q1 
      FB(2*IP+1) = E1 
      FB(I) = Q1 
      IF(QO-QON) 1025, 1030, 1025 
1025  FA(I) = QON 
C 
1030  CONTINUE 
C
CC  KSH CHANGE START
      IF (IBUG.LT.2) GO TO 1060
      IF(ISHUT.NE.1) GO TO 891
      WRITE(IODBUG,892) IS2
 892  FORMAT(/5X,I5,' HOUR MAXIMUM TRIBUTARY FLOW EXCEEDED,',
     &  ' SHUT DOWN RESERVOIR RELEASE.')
      GO TO 1060
 891  IIR = IS(25)
      IS2=ITE/10
      IRRC3 = IFIX(RRC(3,1))
      JULHRX = IRRC3+ITE/10
      IF(NPA(IIR).LE.7 .OR. JULHRX.LT.IRRC3) THEN
        WRITE(IODBUG,893) IS2
 893    FORMAT(/5X,I5,' HOUR REGULATION CODE NOT SPECIIED,',
     &  ' FREEFLOW OPTION IS USED!')
        GO TO 1060
      ENDIF
      KDA = JULHRX/24+1
      KHR = JULHRX-(KDA-1)*24
      WRITE(IODBUG,895) IS2
 895  FORMAT(/5X,I5,' HOUR REGULATION CODE BRACKETED BY:',
     & '   TIME (HR)  DATA VALUE   DATA CODE')
      ISC = NPA(IIR+1)-3
      DO 750 IC=1,2
      IISC = IIR+ISC+(IC-1)*3-1
      ICN = NPA(IISC+2)+1
      JHR = NPA(IISC)/10
      CALL MDYH1(KDA,KHR,KMON,KDAY,KYEAR,KHOUR,NLSTZ,NOUTDS,TZCS)
      IF(IC.EQ.1)
     & WRITE(IODBUG,897) KMON,KDAY,KYEAR,KHOUR,JULHRX,
     &  JHR,WK(IISC+1),IRRC(1,ICN),IRRC(2,ICN)
 897   FORMAT(5X,6HDATE= ,I2.2,'/',I2.2,'/',I4.4,'/',I2.2,
     & 6X,8HJULHOUR=,I10,I11,F12.2,4X,2A4)
      IF(IC.EQ.2)
     & WRITE(IODBUG,898) JHR,WK(IISC+1),IRRC(1,ICN),IRRC(2,ICN)
 898   FORMAT(45X,I12,F12.2,4X,2A4)
 750  CONTINUE
      ICN = ICODE+1
C      IF(IS(50).EQ.0) THEN
C        LOCQ = IS(IXQ)-22
C        QX = WK(IXQ+LOCQ)
C        IF(QX.LE.0.0) QC=-999.0
C      ENDIF
      WRITE(IODBUG,1614) IRRC(1,ICN),IRRC(2,ICN),VALUE,QC
 1614 FORMAT(5X,'REGULATION CODE = ',2A4,
     & 2X,'DATA VALUE =',F10.2,2X,'HYDRAULIC CAPACITY =',F10.1)
      IF(E1.GE.EUB) WRITE(IODBUG,1615)
 1615 FORMAT(5X,'PASS-INFLOW / FREEFLOW USED BECAUSE ',
     & 'COMPUTED ELEVATION EXCEEDS MAXIMUM ELEVATION.')
CC  KSH CHANGE END
C
1060  RETURN 
CCC   IBUG=0 ......... this statement cannot be reached
      END
