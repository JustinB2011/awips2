C - jgg modified argument list to address HSD bug 23-48 
C jgg      SUBROUTINE LVIN55(NN,X,NJFM,NJTO,NXLV,NIFM,NITO,IFLV,TFLV0,HWLV,
C jgg     * WCLV,TFLV,BLVMX,HFLV,HLVMN,SLV,HLV,BLV,HPLV,DPLV,K2,K23)
      SUBROUTINE LVIN55(NN,X,NJFM,NJTO,NXLV,NIFM,NITO,IFLV,TFLV0,HWLV,
     * WCLV,TFLV,BLVMX,HFLV,HLVMN,SLV,HLV,BLV,HPLV,DPLV,
     * K1,K2,K18,K22,K23)
C jgg end change
     
C
C       THIS SUBROUTINE INTERPOLATES THE LEVEE PROPERTIES WHERE
C         ADDITIONAL X-SECTIONS HAVE BEEN INTERPOLATED.
C       NJFM=MAIN RIVER, NJTO=TRIBUTARY RIVER
C
C      COMPUTE THE FOLLOWING INTERPOLATED LEVEE PROPERTIES:
C            NIFM--NO. OF REACH ALONG RIVER WITH LEVEE FLOW TO NITO
C            NITO--NO. OF REACH RECEIVING FLOW FROM LEVEE AT NIFM
C            IF NITO=0, LEVEE FROM MAIN RIVER NJFM INTO STORAGE POND NJTO
C            WCLV----WEIR COEFF. OF DISCHARGE
C            HWLV---WEIR CREST ELEV. OF LEVEE
C            TFLV---TIME OF FAILURE OF LEVEE
C            BLVMX---MAXIMUM WIDTH(FT) OF LEVEE FAILURE
C            HFLV---ELEV OF WATER WHEN LEVEE FAILS
C            HLVMN---MINIMUM ELEV OF BOTTOM OF LEVEE BREACH
C
C
      COMMON/METR55/METRIC
      COMMON/LEV55/NLEV,DHLV,NPOND,DTHLV,IDTHLV
      COMMON/M3055/EPSY,EPSQ,EPSQJ,THETA,XFACT
      COMMON/FDBUG/IODBUG,ITRACE,IDBALL,NDEBUG,IDEBUG(20)
      COMMON/IONUM/IN,IPR,IPU
C - jgg made the following changes for bug r23-48 
C jgg      DIMENSION NN(K23,1),X(K2,1),NJFM(1),NJTO(1),NXLV(1)
C jgg      DIMENSION NIFM(1),NITO(1),IFLV(1),TFLV0(1),HLVMN(1),SLV(1)
C jgg      DIMENSION HWLV(1),WCLV(1),TFLV(1),BLVMX(1),HFLV(1)
C jgg      DIMENSION HLV(1),BLV(1),HPLV(1),DPLV(1)

      DIMENSION NN(K23,K1),X(K2,K1),NJFM(K22),NJTO(K22),NXLV(K18)
      DIMENSION NIFM(K22),NITO(K22),IFLV(K22),TFLV0(K22),HLVMN(K22)
      DIMENSION HWLV(K22),WCLV(K22),TFLV(K22),BLVMX(K22),HFLV(K22)
      DIMENSION HLV(K22),BLV(K22),HPLV(K22),DPLV(K22),SLV(K22)
C jgg end changes      
      CHARACTER*8 SNAME
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/fcinit_fldwav/RCS/lvin55.f,v $
     . $',                                                             '
     .$Id: lvin55.f,v 1.4 2004/08/25 18:39:58 jgofus Exp $
     . $' /
C    ===================================================================
C
      DATA SNAME/'LVIN55  '/

      CALL FPRBUG(SNAME, 1, 55, IBUG)

C
C       CREATE TEMPORARY ARRAYS.
      NLVO=NLEV
C  DETERMINE NEW LEVEE NO. AFTER INTERPOLATION
      LN1=1
      DO 100 LO =1,NLVO
      NXLV(LO)=LN1
      JFM=NJFM(LO)
      IFM1=NIFM(LO)
      N1=NN(IFM1,JFM)
      IFM2=IFM1+1
      N2=NN(IFM2,JFM)
      NXFM=N2-N1
      JTO=NJTO(LO)
      ITO1=NITO(LO)
      N1=0
      IF(ITO1.GT.0) N1=NN(ITO1,JTO)
      ITO2=ITO1+1
      N2=NN(ITO2,JTO)
      NXTO=N2-N1
      IF(ITO1.EQ.0) NXTO=0
      NIMX=MAX0(NXFM,NXTO)
      LN1=LN1+NIMX
  100 CONTINUE
      NLEV=LN1-1
C
C  STORE INPUT LEVEE PROPERTIES INTO RENUMBERED LOCATION
C
      DO 300 LOO=1,NLVO
      LO=NLVO-LOO+1
      LN1=NXLV(LO)
      JFM=NJFM(LO)
      NJFM(LN1)=NJFM(LO)
      NIFM(LN1)=NIFM(LO)
      WCLV(LN1)=WCLV(LO)
      HWLV(LN1)=HWLV(LO)
      TFLV(LN1)=TFLV(LO)
      BLVMX(LN1)=BLVMX(LO)
      HFLV(LN1)=HFLV(LO)
      HLVMN(LN1)=HLVMN(LO)
      SLV(LN1)=SLV(LO)
      HPLV(LN1)=HPLV(LO)
      DPLV(LN1)=DPLV(LO)
      JTO=NJTO(LO)
      NJTO(LN1)=NJTO(LO)
      NITO(LN1)=NITO(LO)
  300 CONTINUE
C
C  GENERATE INTERPOLATED LEVEE PROPERTIES
C
      DO 500 LOO=1,NLVO
      LO=NLVO-LOO+1
      LN1=NXLV(LO)
      LN=LN1-1
      HWLVS=HWLV(LN1)
      BLVMXS=BLVMX(LN1)
      HFLVS=HFLV(LN1)
      HLVMNS=HLVMN(LN1)
      HPLVS=HPLV(LN1)
      JFM=NJFM(LN1)
      IFM1=NIFM(LN1)
      N1=NN(IFM1,JFM)
      NIFMS=N1
      IFM2=IFM1+1
      N2=NN(IFM2,JFM)
      NXFM=N2-N1
      N11=N1+1
      DX=ABS(X(N1,JFM)-X(N11,JFM))*XFACT
      DXX=ABS(X(N1,JFM)-X(N2,JFM))*XFACT
      DXR=DX/DXX
      DH=DX*SLV(LN1)
C  IF A POND, ONLY INTERPOLATE MAIN RIVER LEVEE PROPERTIES
      JTO=NJTO(LN1)
      NITOS=NITO(LN1)
      IF(NITOS.EQ.0) GO TO 520
      ITO1=NITO(LN1)
      N1=NN(ITO1,JTO)
      NITOS=N1
      ITO2=ITO1+1
      N2=NN(ITO2,JTO)
      NXTO=N2-N1
      IF(NXFM.EQ.NXTO) GO TO 520
      IF(NXFM.GT.NXTO) GO TO 530
      NTF2=NXTO/NXFM
      NTF1=NTF2+1
      LC=NXTO-NTF2*NXFM
      NXX=0
      DO 515 L1=1,NXFM
      NX=NTF1
      IF(L1.GT.LC) NX=NTF2
      DO 514 L2=1,NX
      LN=LN+1
      NJFM(LN)=NJFM(LN1)
      NIFM(LN)=NIFMS+L1-1
      WCLV(LN)=WCLV(LN1)
      HWLV(LN)=HWLVS-DH*(L1-1)
      TFLV(LN)=TFLV(LN1)
      BLVMX(LN)=BLVMXS*DXR
      HFLV(LN)=HFLVS-DH*(L1-1)
      IF(HFLVS.GT.9000.) HFLV(LN)=10000.
      HLVMN(LN)=HLVMNS-DH*(L1-1)
      IF(HLVMNS.LE.0.) HLVMN(LN)=0.
      SLV(LN)=SLV(LN1)
      HPLV(LN)=HPLVS-DH*(L1-1)
      DPLV(LN)=DPLV(LN1)
      NJTO(LN)=NJTO(LN1)
      NITO(LN)=NITOS+NXX+L2-1
  514 CONTINUE
      NXX=NXX+NX
  515 CONTINUE
      GO TO 540
  520 CONTINUE
      NX=NXFM
      DO 525 L=1,NX
      LN=LN+1
      NJFM(LN)=NJFM(LN1)
      NIFM(LN)=NIFMS+L-1
      WCLV(LN)=WCLV(LN1)
      HWLV(LN)=HWLVS-DH*(L-1)
      TFLV(LN)=TFLV(LN1)
      BLVMX(LN)=BLVMXS*DXR
      HFLV(LN)=HFLVS-DH*(L-1)
      IF(HFLVS.GT.9000.) HFLV(LN)=10000.
      HLVMN(LN)=HLVMNS-DH*(L-1)
      IF(HLVMNS.LE.0.) HLVMN(LN)=0.
      SLV(LN)=SLV(LN1)
      HPLV(LN)=HPLVS-DH*(L-1)
      DPLV(LN)=DPLV(LN1)
      NJTO(LN)=NJTO(LN1)
      NITO(LN)=NITOS+L-1
      IF(NITOS.EQ.0) NITO(LN)=0
  525 CONTINUE
      GO TO 540
  530 CONTINUE
      ICKVAL=0
      IF (NXTO.EQ.ICKVAL) THEN
         WRITE (IPR,998) 'NXTO',NXTO,ICKVAL
 998        FORMAT ('0**ERROR** IN LVIN55 - VALUE OF VARIABLE ',A4,' (',
     *   I2,') EQUAL TO ',I2,'.')
         CALL ERROR
         GO TO 999
      ENDIF
      NFT2=NXFM/NXTO
      NFT1=NFT2+1
      LC=NXFM-NFT2*NXTO
      NXX=0
      L=0
      DO 535 L1=1,NXTO
      NX=NFT1
      IF(L1.GT.LC) NX=NFT2
      DO 534 L2=1,NX
      L=L+1
      LN=LN+1
      NJFM(LN)=NJFM(LN1)
      NIFM(LN)=NIFMS+NXX+L2-1
      WCLV(LN)=WCLV(LN1)
      HWLV(LN)=HWLVS-DH*(L-1)
      TFLV(LN)=TFLV(LN1)
      BLVMX(LN)=BLVMXS*DXR
      HFLV(LN)=HFLVS-DH*(L-1)
      IF(HFLVS.GT.9000.) HFLV(LN)=10000.
      HLVMN(LN)=HLVMNS-DH*(L-1)
      IF(HLVMNS.LE.0.) HLVMN(LN)=0.
      SLV(LN)=SLV(LN1)
      HPLV(LN)=HPLVS-DH*(L-1)
      DPLV(LN)=DPLV(LN1)
      NJTO(LN)=NJTO(LN1)
      NITO(LN)=NITOS+L1-1
  534 CONTINUE
      NXX=NXX+NX
  535 CONTINUE
  540 CONTINUE
  500 CONTINUE   
      IF(IBUG.EQ.1) WRITE(IODBUG,857)
      DO 855 L=1,NLEV
      IFLV(L)=0
      TFLV0(L)=0.0
      HLV(L)=HWLV(L)
      BLV(L)=0.0
      JFM=NJFM(L)
      IFM=NIFM(L)
      HPLEV=HPLV(L)
      HP=1.0
      XP=1.0 
      WP=1.0
      IF (METRIC.EQ.1) THEN
      HP=0.3048
      XP=1.6093
      WP=0.552
      ENDIF
      IF(IBUG.EQ.1) WRITE(IODBUG,853) L,NJFM(L),NIFM(L),NJTO(L),
     . NITO(L),X(IFM,JFM)*XP,HWLV(L)*HP,WCLV(L)*WP,TFLV(L),BLVMX(L)*HP,
     . HFLV(L)*HP,HLVMN(L)*HP,SLV(L),HPLEV*HP,DPLV(L)*HP
  855 CONTINUE
CCC   856 CONTINUE
  853 FORMAT(5I5,7F10.2,F10.5,2F10.2)
  857 FORMAT(/'    L NJFM NIFM NJTO NITO         X      HWLV      WCLV
     .    TFLV     BLVMX      HFLV     HLVMN       SLV      HPLV      DP
     .LV')
  999 RETURN
      END
