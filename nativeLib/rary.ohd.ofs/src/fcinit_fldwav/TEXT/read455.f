      SUBROUTINE READ455(PO,IUSEP,LEFTP,NBT,NJFMT,NJTOT,NIFMT,NITOT,
     * KRCHT,LROUT,XT,MIXF,JN,NP,DTFMN,NU,NODESC,K1,K3,K13,K16,K18,K19,
     * K20,K21,K23,W,IUSEW,NRC,IERR)
C
C      THIS SUBROUTINE READS THE LEVEE/DAM DATA
C
      CHARACTER*80 DESC
      COMMON/LEV55/NLEV,DHLV,NPOND,DTHLV,IDTHLV
      COMMON/GT55/KCG,NCG
      COMMON/M3055/EPSY,EPSQ,EPSQJ,THETA,XFACT
      COMMON/MXVAL55/MXNB,MXNGAG,MXNCM1,MXNCML,MXNQL,MXINBD,MXRCH,
     .               MXMGAT,MXNXLV,MXROUT,MXNBT,MXNSTR,MXSLC
      COMMON/IDOS55/IDOS,IFCST
      COMMON/FDBUG/IODBUG,ITRACE,IDBALL,NDEBUG,IDEBUG(20)
      COMMON/IONUM/IN,IPR,IPU

      INCLUDE 'common/ofs55'

      DIMENSION PO(*),W(1)
      DIMENSION NBT(K1),KRCHT(K13,K1),LROUT(K1),MIXF(K1)
      DIMENSION NJFMT(K18),NJTOT(K18),NIFMT(K18),NITOT(K18),XT(K23,K1)
      CHARACTER*8 SNAME
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/fcinit_fldwav/RCS/read455.f,v $
     . $',                                                             '
     .$Id: read455.f,v 1.9 2004/08/25 18:48:38 jgofus Exp $
     . $' /
C    ===================================================================
C
      DATA SNAME/ 'READ455 ' /

      CALL FPRBUG(SNAME, 1, 55, IBUG)

      IERR=0
      DTFMN=999.
      IF(NLEV.EQ.0) GO TO 330
      LOHWLV=IUSEP+1
      LOWCLV=LOHWLV+MXNXLV
      LOTFLV=LOWCLV+MXNXLV
      LOBLMX=LOTFLV+MXNXLV
      LOHFLV=LOBLMX+MXNXLV
      LOHPND=LOHFLV+MXNXLV
      LOSAP=LOHPND+MXNXLV
      LOHSAP=LOSAP+MXNXLV*8
      LOHLMN=LOHSAP+MXNXLV*8
      LOSLV=LOHLMN+MXNXLV
      LOHPLV=LOSLV+MXNXLV
      LODPLV=LOHPLV+MXNXLV
      IUSEP=LODPLV+MXNXLV-1

      CALL CHECKP(IUSEP,LEFTP,NERR)
      IF(NERR.EQ.1) THEN
        IUSEP=0
        GO TO 5000
      ENDIF

  220 LCTFL0=IUSEP+1
      IUSEP=LCTFL0+MXNXLV-1

      CALL CHECKP(IUSEP,LEFTP,NERR)
      IF(NERR.EQ.1) THEN
        IUSEP=0
        GO TO 5000
      ENDIF

  300 PO(135)=LOHWLV+0.01
      PO(136)=LOSLV+0.01
      PO(137)=LOWCLV+0.01
      PO(138)=LCTFL0+0.01
      PO(139)=LOTFLV+0.01
      PO(140)=LOBLMX+0.01
      PO(141)=LOHFLV+0.01
      PO(142)=LOHLMN+0.01
      PO(143)=LOSAP+0.01
      PO(144)=LOHSAP+0.01
      PO(145)=LOHPND+0.01
      PO(146)=LOHPLV+0.01
      PO(147)=LODPLV+0.01

      IF(NODESC.EQ.0)THEN
      IF(IBUG.EQ.1) WRITE(IODBUG,857)
  857 FORMAT(//
     .10X,'L    = LEVEE NUMBER'/
     .10X,'NJFM = RIVER NO. FROM WHICH LEVEE FLOW IS PASSED'/
     .10X,'NIFM = REACH NO. ON RIVER NJFM PASSING FLOW TO NITO'/
     .10X,'NJTO = RIVER NO. TO WHICH LEVEE FLOW IS PASSED'/
     .10X,'NJTO = REACH NO. ON RIVER NJFM RECEIVING FLOW FROM NIFM'/
     .10X,'HWLV = ELEVATION OF TOP LEVEE, RIDGE LINE, ETC.'/
     .10X,'WCLV = WEIR-FLOW DISCHARGE COEFFICIENT'/
     .10X,'TFLV = TIME OF LEVEE FAILURE (CREVASSE)'/
     .10X,'BLVMX= FINAL WIDTH OF LEVEE CREVASSE'/
     .10X,'HFLV = ELEVATION OF WATER SURFACE WHEN LEVEE STARTS TO FAIL'/
     .10X,'HLVMN= FINAL ELEVATION OF BOTTOM LEVEE CREVASSE'/
     .10X,'SLV  = SLOPE OF LEVEE REACH'/
     .10X,'HPLV = CENTERLINE ELEVATION OF FLOOD DRAINAGE PIPE'/
     .10X,'DPLV = DIAMETER OF FLOOD DRAINAGE PIPE'/)
      ENDIF
      IF(IBUG.EQ.1) WRITE(IODBUG,723)
  723 FORMAT(/
     .'    L   NJFM NIFM NJTO NITO     X       HWLV       WCLV      TFLV
     .    BLVMX    HFLV      HLVMN      SLV      HPLV         DPLV'/)

      DO 210 L=1,NLEV
      JFM=NJFMT(L)
      IFM=NIFMT(L)
      READ(IN,'(A)',END=1000) DESC
      READ(IN,*) PO(LOHWLV+L-1),PO(LOWCLV+L-1),PO(LOTFLV+L-1),
     1 PO(LOBLMX+L-1),PO(LOHFLV+L-1),PO(LOHLMN+L-1),PO(LOSLV+L-1)
      IF (PO(LOHFLV+L-1).LE.0.00001) PO(LOHFLV+L-1)=10000.
      PO(LOHPLV+L-1)=100000.
      PO(LODPLV+L-1)=0.0
      IF(PO(LOWCLV+L-1).LT.0.0) THEN
        READ(IN,'(A)',END=1000) DESC
        READ(IN,*) PO(LOHPLV+L-1),PO(LODPLV+L-1)
      END IF
      IF(NITOT(L).EQ.0 .AND. NJTOT(L).GT.NPOND) NPOND=NJTOT(L)
      IF(IBUG.EQ.1)
     *WRITE(IODBUG,853)L,NJFMT(L),NIFMT(L),NJTOT(L),NITOT(L),XT(IFM,JFM)
     *,PO(LOHWLV+L-1),PO(LOWCLV+L-1),PO(LOTFLV+L-1),PO(LOBLMX+L-1),
     * PO(LOHFLV+L-1),PO(LOHLMN+L-1),PO(LOSLV+L-1),PO(LOHPLV+L-1),
     * PO(LODPLV+L-1)
      IF(PO(LOWCLV+L-1).LT.0.0) PO(LOWCLV+L-1)=ABS(PO(LOWCLV+L-1))
  210 CONTINUE
  853 FORMAT(5I5,7F10.2,F10.5,2F10.2)
C
      PO(148)=NPOND+0.01
      IF(NPOND.EQ.0) GO TO 330
      IF(NODESC.EQ.0)THEN
      IF(IBUG.EQ.1) WRITE(IODBUG,2843)
 2843 FORMAT(//
     .10X,'HPND = INITIAL WSEL OF STORAGE POND LEVEE'/
     .10X,'SAP  = SURFACE AREA OF STORAGE POND CORRESPONDING TO HSAP'/
     .10X,'HSAP = ELEVATION CORRESPONDING TO SAPOND'//)
      ENDIF
      DO 320 L=1,NPOND
      READ(IN,'(A)',END=1000) DESC
      READ(IN,*) PO(LOHPND+L-1)
      IF(IBUG.EQ.1) WRITE(IODBUG,2844) L,PO(LOHPND+L-1)
      LSAP=LOSAP+(L-1)*8-1
      LHSAP=LOHSAP+(L-1)*8-1
      READ(IN,'(A)',END=1000) DESC
      READ(IN,*) (PO(LSAP+KK),KK=1,8)
      IF(IBUG.EQ.1) WRITE(IODBUG,2845) (PO(LSAP+KK),KK=1,8)
      READ(IN,'(A)',END=1000) DESC
      READ(IN,*) (PO(LHSAP+KK),KK=1,8)
      IF(IBUG.EQ.1) WRITE(IODBUG,2846) (PO(LHSAP+KK),KK=1,8)
  320 CONTINUE

  330 CONTINUE
 2844 FORMAT(/4X,'POND= ',I2,5X,'HPOND=',F10.2)
 2845 FORMAT(6X,'SAPOND:',8F10.0)
 2846 FORMAT(6X,'HSAP:  ',8F10.2)

      MXINBD=0
      MXMGAT=0
      MXROUT=0

      DO 450 J=1,JN
      NINBD=0
      NMGAT=0
      MIX=MIXF(J)
      LROUT(J)=1
      LRTYP=KRCHT(1,J)
      IF (LRTYP.GE.10 .AND. MIX.EQ.1) GOTO 410
      LR1=KRCHT(1,J)
      LRTYP=KRCHT(1,J)
      IF(MIX.EQ.5.AND.LR1.EQ.6) LRTYP=0
      LR2=KRCHT(2,J)
      IF (LR1.GE.10.AND.LR2.EQ.0) LRTYP=0
      IF (LR1.GE.10.AND.LR2.EQ.1) LRTYP=1
      IF (LR1.GE.10.AND.LR2.EQ.6) LRTYP=0
      IF (LRTYP.GE.10) LRTYP=0
  410 DO 440 I=2,NBT(J)-1
      KRA=KRCHT(I,J)
      IF(KRA.EQ.6) KRA=0
      IF(NP.LT.0) GO TO 430
      IF(KRA.GE.10) GOTO 430
      IF(KRA.NE.LRTYP) THEN
        LROUT(J)=LROUT(J)+1
        LRTYP=KRA
      ENDIF
  430 IF(KRA.LT.10.OR.KRA.GT.40) GO TO 440
      IF(KRA.EQ.14) NMGAT=NMGAT+1
      NINBD=NINBD+1
  440 CONTINUE
      IF(MXROUT.LT.LROUT(J)) MXROUT=LROUT(J)
      IF(MXINBD.LT.NINBD) MXINBD=NINBD
      IF(MXMGAT.LT.NMGAT) MXMGAT=NMGAT
  450 CONTINUE

      K16=MXINBD
         IF(K16.EQ.0) K16=1
      K20=MXMGAT
C jgg added the following to fix HSD bug r23-48
         IF(K20.EQ.0) K20=1
C jgg end of changes

      IF(MXINBD.EQ.0) GO TO 9000

      NLOCK=PO(321)
      IF (NLOCK.LE.0) GOTO 470
      LONTSP=IUSEP+1
      LONTST=LONTSP+JN*MXINBD
      IUSEP  =LONTST+JN*MXINBD-1
cc      IF(IDOS.LT.3) GOTO 470

cc      LOTS1=IUSEP+1
cc      IUSEP=LOTS1+10*NLOCK-1

470   LOSAR=IUSEP+1
      LOHSAR=LOSAR+JN*MXINBD*8
      LOLAD=LOHSAR+JN*MXINBD*8
      LOHDD=LOLAD+JN*MXINBD
      LOCLL=LOHDD+JN*MXINBD
      LOCDOD=LOCLL+JN*MXINBD
      LOQTD=LOCDOD+JN*MXINBD
      LOICG=LOQTD+JN*MXINBD
      LOHSPD=LOICG+JN*MXINBD
      LOSPL=LOHSPD+JN*MXINBD
      LOCSD=LOSPL+JN*MXINBD
      LOHGTD=LOCSD+JN*MXINBD
      LOCGD=LOHGTD+JN*MXINBD
      LOHCRL=LOCGD+JN*MXINBD
      LOCRL=LOHCRL+JN*MXINBD*8
      LORHI=LOCRL+JN*MXINBD*8
c  no. of points in r.c. changed from 8 to 112 to be compatible with ofs
      LORQI=LORHI+JN*MXINBD*112
      LOQGH=LORQI+JN*MXINBD*112
      LOCGCG=LOQGH+JN*MXINBD*KCG
      LOQHT= LOCGCG+JN*MXINBD*KCG
      LOTIQH=LOQHT+JN*MXINBD*8*8
      LOTFH=LOTIQH+JN*MXINBD
      LODTHF=LOTFH+JN*MXINBD
      LOHFDD=LODTHF+JN*MXINBD
      LOBBD=LOHFDD+JN*MXINBD
      LOZBCH=LOBBD+JN*MXINBD
      LOYMIN=LOZBCH+JN*MXINBD
      LOBEXP=LOYMIN+JN*MXINBD
      LOCPIP=LOBEXP+JN*MXINBD
      LOEBE1=LOCPIP+JN*MXINBD
      LOEBE2=LOEBE1+JN*MXINBD
      LOEBW1=LOEBE2+JN*MXINBD
      LOEBW2=LOEBW1+JN*MXINBD
      LOBRGW=LOEBW2+JN*MXINBD
      LOCDBR=LOBRGW+JN*MXINBD
      LOBRHS=LOCDBR+JN*MXINBD
      LOBRBS=LOBRHS+JN*MXINBD*8
      LOEMBE=LOBRBS+JN*MXINBD*8
      LOEMBW=LOEMBE+JN*MXINBD*8
      LONG=LOEMBW+JN*MXINBD*8
      LOGSIL=LONG+JN*MXMGAT
      LOGWID=LOGSIL+JN*MXMGAT*NCG
      LOTGHT=LOGWID+JN*MXMGAT*NCG
      LOGHT=LOTGHT+JN*MXMGAT*NCG*KCG
      LCNFLD=LOGHT+JN*MXMGAT*NCG*KCG
      LCTCG=LCNFLD+JN*MXINBD
      LCTOPN=LCTCG+JN*MXINBD*20
      LOICHN=LCTOPN+JN*MXINBD
      LOPTAR=LOICHN+JN*MXINBD
      LOCHTW=LOPTAR+JN*MXINBD
      LOGZPL=LOCHTW+JN*MXINBD
      LOLQTT=LOGZPL+JN*MXINBD
      LOLTQT=LOLQTT+JN*MXINBD
      IUSEP=LOLTQT+JN*MXINBD-1

      CALL CHECKP(IUSEP,LEFTP,NERR)
      IF(NERR.EQ.1) THEN
        IUSEP=0
        GO TO 5000
      ENDIF

C-----------------  READ IN RESERVOIR/DAM/BRIDGE  DATA  ----------------
      KLOCK=0
      ILOCK=1
      DO 1860 J=1,JN
      KRB=0
      K=0
      NDAM=0
      NBRG=0
      DO 1855 I=1,NBT(J)-1
      KRA=IABS(KRCHT(I,J))
      IF(KRA.LT.10.OR.KRA.GT.40) GO TO 1855
      IF(I.GT.1) KRB=IABS(KRCHT(I-1,J))
      K=K+1
      IF(KRA.EQ.35) THEN
        NBRG=NBRG+1
        GO TO 1850
      ELSE
        NDAM=NDAM+1
      ENDIF
      IF(IBUG.EQ.1) WRITE(IODBUG,2848) J,NDAM
 2848 FORMAT(//1X,'RIVER NO.',I3,',  DAM NO.',I3)
C DAM AND RESERVOIR
      IF(I.EQ.1.OR.KRB.EQ.4) THEN
        CALL REDRES55(K,J,PO(LOSAR),PO(LOHSAR),NODESC,IERR,K1,K16)
        IF(IERR.EQ.1) GO TO 5000
      ENDIF

      CALL REDDAM55(K,J,KRA,PO(LOHDD),PO(LOCLL),PO(LOHSPD),PO(LOSPL),
     1 PO(LOCRL),PO(LOHCRL),PO(LOCSD),PO(LOHGTD),PO(LOCGD),PO(LOCDOD),
     2 PO(LOQTD),PO(LOHFDD),PO(LOTFH),PO(LOBBD),PO(LOZBCH),PO(LOYMIN),
     3 PO(LORHI),PO(LORQI),PO(LCTCG),PO(LOQGH),PO(LOCGCG),PO(LOQHT),
     4 PO(LONG),PO(LOLAD),PO(LOICG),PO(LCNFLD),PO(LODTHF),PO(LOBEXP),
     5 PO(LOCPIP),PO(LOTIQH),PO(LONLAD),PO(LOGSIL),PO(LOGWID),
     6 PO(LOTGHT),PO(LOGHT),DTFMN,PO(LODTIN),NU,W(LOQTT),W(LOTQT),
     7 PO(LOICHN),PO(LOPTAR),PO(LOCHTW),PO(LOGZPL),W,W,PO(LOSAR),
     8 PO(LOHSAR),IRES,NODESC,PO(LOLQTT),
     9 PO(LOLTQT),IERR,K1,K3,K16,K19,K20,K21,IUSEW,NRC)
      IF(IERR.EQ.1) GO TO 5000
      GO TO 1855
C   BRIDGE
1850  IF(IBUG.EQ.1) WRITE(IODBUG,2849) J,NBRG
 2849 FORMAT(//1X,'RIVER NO.',I3,',  BRIDGE NO.',I3)
      CALL REDBRG55(K,J,PO(LOEBE1),PO(LOBRGW),PO(LOHFDD),PO(LOTFH),
     2  PO(LOBBD),PO(LOZBCH),PO(LOYMIN),PO(LOLAD),PO(LODTHF),PO(LOBEXP),
     3  PO(LOEBE2),PO(LOCDBR),PO(LOBRBS),PO(LOBRHS),PO(LCNFLD),
     4  PO(LOEBW2),PO(LOEBW1),PO(LOCPIP),PO(LCTOPN),DTFMN,NODESC,IERR,
     5  K1,K16)
      IF(IERR.EQ.1) GO TO 5000
 1855 CONTINUE
 1860 CONTINUE

      PO(151)=LOLAD+0.01
      PO(152)=LORHI+0.01
      PO(153)=LORQI+0.01
      PO(154)=LODTHF+0.01
      PO(155)=LOBEXP+0.01
      PO(156)=LOGSIL+0.01
      PO(157)=LOSAR+0.01
      PO(158)=LOHSAR+0.01
      PO(159)=LOQGH+0.01
      PO(160)=LOHFDD+0.01
      PO(161)=LOTFH+0.01
      PO(162)=LOBBD+0.01
      PO(163)=LOZBCH+0.01
      PO(164)=LOYMIN+0.01
      PO(165)=LOHDD+0.01
      PO(166)=LOHSPD+0.01
      PO(167)=LOHGTD+0.01
      PO(168)=LOCSD+0.01
      PO(169)=LOCGD+0.01
      PO(170)=LOCDOD+0.01
      PO(171)=LOQTD+0.01
      PO(172)=LCNFLD+0.01
      PO(173)=LOTIQH+0.01
      PO(174)=LOCPIP+0.01
      PO(175)=LOCGCG+0.01
      PO(176)=LOGWID+0.01
      PO(177)=LONG+0.01
      PO(178)=LOGHT+0.01
      PO(179)=LOQHT+0.01
      PO(180)=LCTOPN+0.01
      PO(181)=LOGZPL+0.01

      PO(183)=LONTSP+0.01
      PO(184)=LONTST+0.01
      PO(185)=LOQTT+0.01
      PO(186)=LOTQT+0.01
      PO(187)=LOICHN+0.01
      PO(188)=LOPTAR+0.01
      PO(189)=LOCHTW+0.01
cc      PO(322)=LOTS1+0.01

      PO(192)=LOCLL+0.01
      PO(193)=LOSPL+0.01
      PO(194)=LOCRL+0.01
      PO(195)=LOHCRL+0.01
      PO(196)=LCTCG+0.01
      PO(197)=LOICG+0.01
      PO(198)=LOTGHT+0.01
      PO(199)=LOBRBS+0.01
      PO(200)=LOBRHS+0.01
      PO(201)=LOEBE2+0.01
      PO(202)=LOEBE1+0.01
      PO(203)=LOBRGW+0.01
      PO(204)=LOCDBR+0.01
      PO(205)=LOEBW2+0.01
      PO(206)=LOEBW1+0.01
      PO(207)=LOEMBW+0.01
      PO(208)=LOEMBE+0.01
      PO(333)=LOLQTT+0.01
      PO(334)=LOLTQT+0.01
      GO TO 9000

 1000 WRITE(IPR,1010)
      CALL ERROR
      IERR=1
 1010 FORMAT(/5X,'**ERROR** END OF FILE ENCOUNTERED WHILE READING INPUT
     *LEVEE INFO.'/)
      GO TO 9000

 5000 IERR=1
cc      WRITE(IPR,5010)
 5010 FORMAT(//2X,'**ERROR** AMOUNT OF STORAGE EXCEEDED ... PROGRAM TERM
     *INATED.')
Cc      IF(IBUG.EQ.1) WRITE(IODBUG,11111)
11111 FORMAT(1X,'** EXIT READ4 **')
 9000 RETURN
      END

