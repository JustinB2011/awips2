C MEMBER FHYDEX
C  (from old member FCFHYDEX)
C
C                             LAST UPDATE: 11/15/94.15:14:55 BY $WC30KH
C
C  DESC -- HYDRAULICALLY EXTEND RATING CURVE
C
C.......................................................................
C
      SUBROUTINE FHYDEX(GIVEN,CONVRT,HGUESS,ICONV,NRANGE,IBUG)
C
C.......................................................................
C
C  SUBROUTINE FHYDEX USES THE HYDRAULIC PROPERTIES OF THE CHANNEL X-SECT
C  TO DETERMINE THE DISCHARGE(STAGE) ASSOCIATED WITH A GIVEX STAGE(Q)
C  THAT IS BEYOND THE RANGE OF THE RATING CURVE STORED IN /FRATNG/.
C
C   ARGUMENT LIST:
C      GIVEN - THE GIVEN STAGE OR Q THAT IS TO BE CONVERTED
C      CONVRT - THE CONVERTED VALUE RETURNED TO CALLING PGM
C      HGUESS - THE FIRST ESTIMATE USED IN THE NEWTON RAPHSON ITERATION
C               SCHEME TO DETERMINE STAGE GIVEN Q
C      ICONV  - CONVERSION INDICATOR
C               =1, STAGE TO DISCHARGE
C               =2, DISCHARGE TO STAGE
C      NRANGE - COUNTER FOR NO. OF TIMES EXTRAPOLATED MANNING N VALUES
C               WENT OUT OF RANGE:   0.010 < N < 0.30  :
C
C.......................................................................
C
C  SUBROUTINE ORIGINALLY WRITTEN BY --
C      JONATHAN WETMORE - HRL - 801031
C
C.......................................................................
      INCLUDE 'common/fxsctn'
      INCLUDE 'common/fratng'
      INCLUDE 'common/ionum'
      INCLUDE 'common/fdbug'
      INCLUDE 'common/where'
C
      DIMENSION SUBNAM(2),OLDSUB(2)
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/fcst_rc/RCS/fhydex.f,v $
     . $',                                                             '
     .$Id: fhydex.f,v 1.2 1998/07/02 17:12:28 page Exp $
     . $' /
C    ===================================================================
C
      DATA SUBNAM/4HFHYD,4HEX  /
      DATA UNTL,UNTQ,UNTST/4HM   ,4HCMS ,4HCMSD/
      DATA DIML,DIMQ,DIMST/4HL   ,4HL3/T,4HL3  /
C
C  USE LINEAR (INTER/EXTRA)POLATION
C  RESET EMPTY(4) TEMPORARILY
      EMPTYS=EMPTY(4)
      EMPTY(4)=1.01
C
C  SET /WHERE/ INFO
C
      DO 5 I=1,2
      OLDSUB(I)=OPNAME(I)
  5   OPNAME(I)=SUBNAM(I)
      IOLDOP=IOPNUM
      IOPNUM=0
C
      IF(ITRACE.GE.2) WRITE(IODBUG,601)
  601 FORMAT(1H0,21H *** ENTER FHYDEX ***)
C
C  SET ARRAY INDEX
      ITWO=2
      LH1=LOCH
      LHN=LH1+NRCPTS-1
      LQ1=LOCQ
      LQN=LQ1+NRCPTS-1
      LXE1=LXELEV
      LXEN=LXE1+NCROSS-1
      LXT1=LXTOPW
      LXTN=LXT1+NCROSS-1
      RCMXH=XRC(LHN)
      RCMXEL=RCMXH+GZERO
C
C  SET INDECES FOR WHICH END OF R.C. IS TO BE EXTENDED
C
      IF(IBUG.GE.1) WRITE(IODBUG,602) XRC(LHN),XRC(LQN),
     1XRC(LXEN),XRC(LXTN),NRCPTS,NCROSS
 602  FORMAT(1H0,5X,21HEXTREME R.C. STAGE = ,F10.2,5X,25HEXTREME R.C. DI
     1SCHARGE = ,F10.2,/5X,22HEXTREME X-SECT ELEV = ,F10.2,26HEXTREME X-
     1SECT TOPWIDTH = ,F10.2,
     /5X,'RATING CURVE PAIRS =',I3,2X,'ELEV-TOP WIDTH PAIRS =',I2)
C
      IF (ICONV.GT.1) GO TO 20
C
C  CONVERT FROM STAGE TO DISCHARGE
C  COMPUTE DISCHARGE QFIND
  10  HGIVEN=GIVEN+GZERO
      IF(IBUG.GE.1) WRITE(IODBUG,603) GIVEN
  603 FORMAT(1H0,5X,25HSTAGE VALUE TO CONVERT = ,F10.2)
      IF(HGIVEN.EQ.XRC(LXE1)) GO TO 101
      IF(HGIVEN.GT.XRC(LXE1)) GO TO 11
C  GIVEN STAGE IS BELOW CHNL INVERT SET Q TO ZERO
      WRITE(IPR,6040) GIVEN,HGIVEN,XRC(LXE1)
 6040 FORMAT(10X, 27H**WARNING** GIVEN STAGE OF ,F10.2,1H(,F10.2,
     17H M MSL),/22X,46HFOR CONVERSION BY HYDRAULIC EXTENSION IS BELOW,
     1/20X,16HCHANNEL INVERT (,F10.2,29H M MSL).  WILL SET ASSOCIATED,
     1/20X,18HDISCHARGE TO ZERO.)
      CALL WARN
 101  CONVRT=0.
      GO TO 999
  11   CONTINUE
       IF(HGIVEN.LE.XRC(LXEN)) GO TO 110
       CALL FXTRPL(HGIVEN,XRC(LXEN-1),ITWO,XRC(LXTN-1),BGIVEN,0)
       ELDIF=HGIVEN-XRC(LXEN)
       ADIF=(XRC(LXTN)+BGIVEN)/2.
       AGIVEN=XAREA(NCROSS)+ADIF*ELDIF
       GO TO 111
 110  CALL FTERPL(HGIVEN,XRC(LXE1),NCROSS,XRC(LXT1),BGIVEN,0)
      DO 1105 I=2,NCROSS
      KEEPI=I
      IF(HGIVEN.LT.XRC(LXE1-1+I)) GO TO 1106
 1105 CONTINUE
 1106 KEEPIM=KEEPI-1
      ADIF=(XRC(LXT1-1+KEEPIM)+BGIVEN)/2.
      ELDIF=HGIVEN-XRC(LXE1-1+KEEPIM)
      AGIVEN=XAREA(KEEPIM)+ADIF*ELDIF
 111  STGIVN=GIVEN
      CK=CKARAY(LHN)
      CMN=(1.000/CK)*SQRT(SLOPE)
      IF(CMN.LT.0.010.OR.CMN.GT.0.30) NRANGE=NRANGE+1
       IF(CMN.LT.0.010) CK=(1.000/0.010)*SQRT(SLOPE)
       IF(CMN.GT.0.30) CK=(1.000/0.30)*SQRT(SLOPE)
C  CHECK TO SEE IF COMPOSIT ROUGHNESS IS TO BE USED BASED ON INPUT
C  F.P. "N" AND COMPUTED X-SECT TOP "N"
       IF(FLOODN.LT.1.0) GO TO 129
       LXMN=LXT1+NCROSS
       IF(RCMXEL.LE.XRC(LXEN)) THEN
         CALL FTERPL(RCMXEL,XRC(LXE1),NCROSS,XRC(LXMN),CMN,0)
       ELSE
         CMN=XRC(LXMN+NCROSS-1)
       ENDIF
        CK=(1.000/CMN)*SQRT(SLOPE)
       GO TO 112
 129   CONTINUE
       IF(HGIVEN.LE.XRC(LHN)) GO TO 112
       IF(FLOODN.LE.0.0) GO TO 112
       CKTOP=CKARAY(LHN)
       CMNTOP=(1.000/CKTOP)*SQRT(SLOPE)
       IF(CMNTOP.LT.0.010) CKTOP=(1.000/0.010)*SQRT(SLOPE)
       IF(CMNTOP.GT.0.30) CKTOP=(1.000/0.30)*SQRT(SLOPE)
       CMNTOP=(1.000/CKTOP)*SQRT(SLOPE)
       CALL FTERPL(RCMXEL,XRC(LXE1),NCROSS,XRC(LXT1),RCMXW,0)
        SQUARN=RCMXW*CMNTOP*CMNTOP+(BGIVEN-RCMXW)
     1  *FLOODN*FLOODN
        CMPSTN=SQRT(SQUARN/BGIVEN)
        CK=(1.000/CMPSTN)*SQRT(SLOPE)
 112  QFIND=CK*AGIVEN**(5./3.)/BGIVEN**(2./3.)
      CMN=(1.000/CK)*SQRT(SLOPE)
      IF(IBUG.GE.1) WRITE(IODBUG,6044) STGIVN,CMN,QFIND,AGIVEN,BGIVEN
 6044 FORMAT(5X,'STGIVN,CMN,QFIND,AGIVEN,BGIVEN= ',F10.2,F8.6,3F10.0)
      CONVRT=QFIND
      GO TO 999
C
C  CONVERT FROM DISCHARGE TO STAGE
C  COMPUTE STAGE USING NEWTON-RAPHSON
C
  20  PONE=0.003048
      IBISEC=0
      HE=HGUESS
      IF(IBUG.GE.1) WRITE(IODBUG,604) GIVEN
  604 FORMAT(1H0,5X,29HDISCHARGE VALUE TO CONVERT = ,F10.2)
      IF(HE.LT.XRC(LHN)) HE=XRC(LHN)
      HE=HE+GZERO
C  IF MANNINGS N FOR FLOODPLAIN ABOVE TOPMOST GIVEN X-SECT ELEV WAS
C     DEFINED IN DEF-RC, MUST DETERMINE N ASSOCIATED WITH TOPMOST ELEV
C     FOR USE IN DEFINING COMPOSIT ROUGHNESS FOR STAGES ABOVE TOPMOST
C     X-SECT ELEV. (SEE STMT N0. 114)
C
      TOPX=XRC(LXEN)-GZERO
       CKTOP=CKARAY(LHN)
       CMNTOP=(1.000/CKTOP)*SQRT(SLOPE)
       IF(CMNTOP.LT.0.010) CKTOP=(1.000/0.010)*SQRT(SLOPE)
       IF(CMNTOP.GT.0.30) CKTOP=(1.000/0.30)*SQRT(SLOPE)
       CMNTOP=(1.000/CKTOP)*SQRT(SLOPE)
C
 24   NITER=0
C
 25   CONTINUE
      NITER=NITER+1
      STEST=HE-GZERO
      CK=CKARAY(LHN)
      CMN=(1.000/CK)*SQRT(SLOPE)
       IF(CMN.LT.0.010) CK=(1.000/0.010)*SQRT(SLOPE)
       IF(CMN.GT.0.30) CK=(1.000/0.30)*SQRT(SLOPE)
       IF(HE.LE.XRC(LXEN)) GO TO 113
       CALL FXTRPL(HE,XRC(LXEN-1),ITWO,XRC(LXTN-1),B,0)
       ELDIF=HE-XRC(LXEN)
       ADIF=(XRC(LXTN)+B)/2.
       A=XAREA(NCROSS)+ADIF*ELDIF
        GO TO 114
 113  CALL FTERPL(HE,XRC(LXE1),NCROSS,XRC(LXT1),B,0)
      DO 1135 I=2,NCROSS
      KEEPI=I
      IF(HE.LT.XRC(LXE1-1+I)) GO TO 1136
 1135 CONTINUE
 1136 KEEPIM=KEEPI-1
      ELDIF=HE-XRC(LXE1-1+KEEPIM)
      ADIF=(XRC(LXT1-1+KEEPIM)+B)/2.
      A=XAREA(KEEPIM)+ADIF*ELDIF
C  CHECK TO SEE IF COMPOSIT ROUGHNESS IS TO BE USED BASED ON INPUT
C  F.P. "N" AND COMPUTED X-SECT TOP "N"
       IF(HE.LE.RCMXEL) GO TO 251
 114   IF(FLOODN.LE.0.0 .OR. FLOODN.GT.1.0) GO TO 251
       CKTOP=CKARAY(LHN)
       CMNTOP=(1.000/CKTOP)*SQRT(SLOPE)
       IF(CMNTOP.LT.0.010) CKTOP=(1.000/0.010)*SQRT(SLOPE)
       IF(CMNTOP.GT.0.30) CKTOP=(1.000/0.30)*SQRT(SLOPE)
       CMNTOP=(1.000/CKTOP)*SQRT(SLOPE)
       CALL FTERPL(RCMXEL,XRC(LXE1),NCROSS,XRC(LXT1),RCMXW,0)
        SQUARN=RCMXW*CMNTOP*CMNTOP+(B-RCMXW)*FLOODN*FLOODN
        CMPSTN=SQRT(SQUARN/B)
        CK=(1.000/CMPSTN)*SQRT(SLOPE)
 251  CONTINUE
       IF(FLOODN.LT.1.0) GO TO 149
       LXMN=LXT1+NCROSS
       IF(HE.LE.XRC(LXEN)) THEN
         CALL FTERPL(HE,XRC(LXE1),NCROSS,XRC(LXMN),CMN,0)
       ELSE
         CMN=XRC(LXMN+NCROSS-1)
       ENDIF
        CK=(1.000/CMN)*SQRT(SLOPE)
 149   CONTINUE
      DO 30 I=2,NCROSS
      IKEEP=I
      IF (HE.GT.XRC(LXE1-1+I-1).AND.HE.LE.XRC(LXE1-1+I)) GO TO 35
  30  CONTINUE
  35  DB=XRC(LXT1-1+IKEEP)-XRC(LXT1-1+IKEEP-1)
      DH=XRC(LXE1-1+IKEEP)-XRC(LXE1-1+IKEEP-1)
 46   B23=B**(2./3.)
      A53=A**(5./3.)
      FOLD=F
      F=GIVEN*B23-CK*A53
C      F=GIVEN*B**(2./3.)-CK*A**(5./3.)
      FSGN=F*FOLD
      FP=(2.*GIVEN*DB/(3.*DH*B**(1./3.)))-(5./3.)*CK*B*A**(2./3.)
      HEE=HE-F/FP
      IF(FSGN.LT.-1.0) HEE=0.5*(HE+HEOLD)
      RR=A/B
      IF(IBUG.GE.1) WRITE(IODBUG,605) HE,HEE,RR,F,FP,A,B,CMN
 605  FORMAT(5X,'HE,HEE,RR,F,FP,A,B,CMN=',3F10.2,4F10.0,F7.5)
C
C  IF FIRST ESTIMATE OF HE IS WAY OFF, THE N-R SCHEME MAY BLOW UP AND
C     GIVE A NEW ESTIMATE (HEE) THAT CAN DROP BELOW THE CHANNEL INVERT.
C     THIS LEADS TO BIG PROBLEMS SO THE FOLLOWING CHECK IS IN TO AVOID
C     THAT AND GIVE A NEW ESTIMATE THAT IS MORE REASONABLE THAN THE
C     FIRST ESTIMATE OR THE ESTIMATE PROVIDED BY THE N-R SCHEME.
C
      IF(HEE.LT.XRC(LXE1-1+1)) HEE=HE+0.5
      IF(ABS(HEE-HE).LT.PONE) GO TO 40
C  IF NO CONVERGENCE AFTER 50 ITERATIONS USE LOG EXTENSION
      IF(NITER.LT.50) THEN
         HEOLD=HE
         HE=HEE
         GO TO 25
      ENDIF
      EMPTY(4)=0.0
       WRITE(IPR,9893) RTCVID,GIVEN
 9893 FORMAT(1H0,10X,45H*WARNING* NONCONVERGENCE OCCURED IN HYDRAULIC,
     110H EXTENSION,/20X,16HOF RATING CURVE ,2A4,18H TO GIVEN FLOW OF ,
     1F10.2,' CMS,'
     1/20X,51HLOGARITHMIC EXTRAPOLATION USED TO FIND ASSOC STAGE.)
      CALL WARN
      CALL FXTRPL(GIVEN,XRC(LQN-1),ITWO,XRC(LHN-1),HEE,0)
      HFIND=HEE
      CONVRT=HEE
      EMPTY(4)=1.01
      GO TO 998
C
  40  CONTINUE
      CMN=(1.000/CK)*SQRT(SLOPE)
      IF(CMN.LT.0.010.OR.CMN.GT.0.30) NRANGE=NRANGE+1
      IF(CMN.EQ.0.010.OR.CMN.EQ.0.30) NRANGE=NRANGE+1
      HFIND=HEE-GZERO
      CONVRT=(HEE-GZERO)
  998 CONTINUE
      IF(IBUG.GE.1) WRITE(IODBUG,6045) GIVEN,HFIND,CMN
 6045 FORMAT(5X,'GIVEN DISCHARGE= ',F10.0,
     & 5X,'COMPUTED STAGE= ',F10.2,5X,4HCMN=,F8.6)
      HGUESS=CONVRT
  999 CONTINUE
      IF(IBUG.GE.1) WRITE(IODBUG,606) CONVRT
 606  FORMAT(1H0,5X,27H COMPUTED VALUE RETURNED = ,F10.2)
      IF(ITRACE.GE.2) WRITE(IODBUG,7895)
 7895 FORMAT(1H0,2X,19H*** EXIT FHYDEX ***)
C
      OPNAME(1) = OLDSUB(1)
      OPNAME(2) = OLDSUB(2)
      IOPNUM = IOLDOP
C
C  RESET EMPTY(4)
      EMPTY(4)=EMPTYS
C
      RETURN
      END
