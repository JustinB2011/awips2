C MEMBER EVAC06
C  (from old member EEVAC06)
C
      SUBROUTINE EVAC06(IDACC,IHACC,LDACC,LHACC,D,A,NDAYS,NHRS,TSID,
     * TYPE,IDT,NPDT,ITSCAL,IVALUE,CO,KODE,VALUE,DAVG,MDAVG)
C
C ......................................................................
C
C      ACCUMULATOR 06 (MIN INSTANTANEOUS VALUE) SUBROUTINE
C
C THIS IS THE ACCUMULATOR SUBROUTINE FOR FINDING THE MINIMUM INST
C VALUE OF THE D ARRAY FOR THIS WINDOW AND THE NUMBER OF DAYS TO
C THAT MIN.
C ......................................................................
C
C     SUBROUTINE INITIALLY WRITTEN BY
C       ED VANBLARGAN - HRL - SEPT, 1981
C
C ......................................................................
C
C
C VARIABLES USED ARE:
C
C IDACC = 1ST JULIAN DAY TO BE ACCUMULATED
C IHACC = 1ST HOUR TO BE ACCM.
C LDACC = LAST JULIAN DAY TO BE ACCM.
C LHACC = LAST HOUR TO BE ACCM.
C     D = ARRAY CONTAINING TIME SERIES DATA
C     A = ACCUMULATOR ARRAY. A(1) IS MIN INST VAL, A(2)IS DAYS TO
C         MIN.
C NDAYS = NUMBER OF DAYS ALREADY ACCM.
C  NHRS = NUMBER OF HOURS ALREADY ACCM.
C  TSID = TIME SERIES I.D.
C  TYPE = DATA TYPE
C   IDT = TIME INTERVAL OF TIME SERIES
C    CO = CARRYOVER (VALID ONLY IF NDAYS OR NHRS IS GT 0)
C  KODE = OPTION CODE.       NOT NECESSARY FOR THIS SUBROUTINE
C VALUE = OPTION VALUE.      NOT NECESSARY FOR THIS SUBROU
C ITSCAL= TIME SCALE OF TIME SERIES
C LOCIN = 1ST (OR CURRENT) LOCATION IN D ARRAY
C LASTIN= LAST LOCATION WE WANT IN D ARRAY
C KDAYS = COUNTER OF THE NUMBER OF DAYS WE GO THRU
C  KHRS = COUNTER OF THE HOUR FOR EACH D VALUE EACH DAY.
C  DAVG = WORK SPACE ARRAY.
C MDAVG = MAXIMUM DAVG ARRAY LENGTH
C .........................................................
C
      DIMENSION SBNAME(2),OLDOPN(2),D(1),A(1),TSID(2),DAVG(1),ISUBN(2)
C
      INCLUDE 'common/ionum'
      INCLUDE 'common/where'
      INCLUDE 'common/fdbug'
      INCLUDE 'common/fctime'
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/shared_esp/RCS/evac06.f,v $
     . $',                                                             '
     .$Id: evac06.f,v 1.1 1995/09/17 19:19:23 dws Exp $
     . $' /
C    ===================================================================
C
C
      DATA NAME,SBNAME/4HEACC,4HEVAC,4H06  /
      DATA INTL/4HINTL/
C
C
C PUT ERROR TRACES FOR THIS ROUTINE IN COMMON BLK/WHERE
C
      IOLDOP=IOPNUM
      IOPNUM=0
      DO 10 I=1,2
        OLDOPN(I)=OPNAME(I)
        OPNAME(I)=SBNAME(I)
10    CONTINUE
C
C GET TRACE LEVEL AND DEBUG CODE.
C
      IF (ITRACE.GE.1) WRITE (IODBUG,115)
115   FORMAT(1H0,14HEVAC06 ENTERED)
      IBUG=IFBUG(NAME)
C
C CHECK A(1)
C
C IF EITHER NHRS OR NDAYS IS GT 0 THEN A(1) HAS A VALID ACCM VALUE
C AND CHECK THAT A(1) IS NOT MISSING.
C OTHERWISE, A(1) IS SET = TO A VERY LARGE NUMBER(SO THAT IT LOSES OUT
C IN 1ST COMPARISON) .
C
      IF (NDAYS.EQ.0 .AND. NHRS.EQ.0) GO TO 100
      IF (IFMSNG(A(1)).NE.0) GO TO 3000
      GO TO 110
C
100   A(1)=999000000.
C
C SET VARIABLES
C
110   LOCIN = (IDACC-IDADAT)*24*NPDT/IDT + (IHACC-1)*NPDT/IDT +IVALUE
      LASTIN= (LDACC-IDADAT)*24*NPDT/IDT + (LHACC-1)*NPDT/IDT +IVALUE
C
      KDAYS=NDAYS+1
      KHRS=IHACC-IDT
C
C
C ......................................................................
C
C TIME FOR DEBUG
C
      IF (IBUG.EQ.0) GO TO 400
C
      WRITE(IODBUG,210) A(1),A(2),NDAYS,NHRS
210   FORMAT(/ 1H0,30HA(1) = MIN INSTANTANEOUS VALUE /
     *1H0,33HA(2) = NUMBER OF DAYS TO THAT MIN // 1H0,10X,11H** INPUT **
     * / 1H0,6X,22HA(1)  A(2)  NDAYS NHRS / 1H0,F10.2,F6.0,2X,2I4)
C
      WRITE(IODBUG,220) IDACC,IHACC,LDACC,LHACC,TSID,TYPE,
     *IDT,ITSCAL,LOCIN,LASTIN,CO
220   FORMAT(// 1H0,23HIDACC IHACC LDACC LHACC,6X,4HTSID,1X,
     *28HTYPE IDT ITSCAL LOCIN LASTIN / 1H0,
     *I5,3I6,2X,2A4,1X,A4,I4,3X,A4,I6,I7 //
     *1H0,11HINPUT CO = ,F10.2 // 1X,16HD ARRAY FOLLOWS:)
C
      WRITE(IODBUG,230) (D(I),I=LOCIN,LASTIN,NPDT)
230   FORMAT(/ 1X,12F10.2)
C
C
C ......................................................................
C
C
C TIME TO CALCULATE A(1) AND A(2).
C
C LOOP THRU D ARRAY FROM LOCIN TO LASTIN. IF ANY D VALUE IS
C LT A(1) THEN:
C  1. A(1)=THAT D VALUE AND
C  2. A(2)=TO THE KDAYS.
C
C KHRS GETS INCREMENTED BY IDT ON EACH PASS THRU LOOP. AT EACH 24
C HOUR MARK KDAYS GETS INCREMENTED BY 1 AND KHRS GETS RESET TO IDT.
C
400   DO 1500 I=LOCIN,LASTIN,NPDT
        IF (IFMSNG(D(I)).EQ.1) GO TO 1999
        KHRS=KHRS+IDT
        IF (KHRS.LE.24) GO TO 1100
        KDAYS=KDAYS+1
        KHRS=IDT
C
1100    IF (D(I).GE.A(1)) GO TO 1500
        A(1)=D(I)
        A(2)=KDAYS+0.01
C
1500  CONTINUE
      GO TO 2000
C
C MISSING DATA ENCOUNTERED
C
1999  A(1)=-999.0
      A(2)=-999.0
      CALL MDYH1(IDACC,IHACC,MO,MDAY,MYR,MHR,100,0,INTL)
      MYR=MOD(MYR,100)
      WRITE(IPR,899) TSID,TYPE,IDT,MO,MYR
899   FORMAT(1H0,45H**NOTE** EVAC06 ENCOUNTERED MISSING DATA FOR:,
     * 6H TSID=,2A4,6H TYPE=,A4,5H IDT=,I2,3X,14HINITIAL MONTH=,
     * I2,1H/,I2)
C
C ................DEBUG TIME............................................
C
2000  IF (IBUG.EQ.0) GO TO 3000
C
      WRITE (IODBUG,2100) A(1),A(2),CO,KDAYS,KHRS
2100  FORMAT(// 1H0,10X,12H** OUTPUT ** / 1H0,14HMIN INST VALUE,
     * 8H A(1) = ,F10.2 / 1H0,24HDAYS TO MINIMUM, A(2) = ,5X,F10.2 //
     *1H0,9HEND CO = ,F10.2,7H KDAYS=,I5,6H KHRS=,I5)
C
C ......................................................................
C
C
C PUT ERROR TRACES BACK IN
C
3000  IOPNUM=IOLDOP
      OPNAME(1)=OLDOPN(1)
      OPNAME(2)=OLDOPN(2)
C
      RETURN
      END
