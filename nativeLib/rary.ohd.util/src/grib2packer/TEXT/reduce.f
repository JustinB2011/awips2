      SUBROUTINE REDUCE(KFILDO,JMIN,JMAX,LBIT,NOV,LX,NDG,IBIT,JBIT,KBIT,
     1                  NOVREF,IBXX2,newbox,newboxp,IER)
C
C        NOVEMBER 2001   GLAHN   TDL   GRIB2
C        MARCH    2002   GLAHN   COMMENT IER = 715
C        MARCH    2002   GLAHN   MODIFIED TO ACCOMMODATE LX=1 ON ENTRY
C
C        PURPOSE
C            DETERMINES WHETHER THE NUMBER OF GROUPS SHOULD BE
C            INCREASED IN ORDER TO REDUCE THE SIZE OF THE LARGE
C            GROUPS, AND TO MAKE THAT ADJUSTMENT.  BY REDUCING THE
C            SIZE OF THE LARGE GROUPS, LESS BITS MAY BE NECESSARY
C            FOR PACKING THE GROUP SIZES AND ALL THE INFORMATION
C            ABOUT THE GROUPS.
C
C            THE REFERENCE FOR NOV( ) WAS REMOVED IN THE CALLING
C            ROUTINE SO THAT KBIT COULD BE DETERMINED.  THIS
C            FURNISHES A STARTING POINT FOR THE ITERATIONS IN REDUCE.
C            HOWEVER, THE REFERENCE MUST BE CONSIDERED.
C
C        DATA SET USE 
C           KFILDO - UNIT NUMBER FOR OUTPUT (PRINT) FILE. (OUTPUT) 
C
C        VARIABLES IN CALL SEQUENCE 
C              KFILDO = UNIT NUMBER FOR OUTPUT (PRINT) FILE.  (INPUT)
C             JMIN(J) = THE MINIMUM OF EACH GROUP (J=1,LX).  IT IS
C                       POSSIBLE AFTER SPLITTING THE GROUPS, JMIN( )
C                       WILL NOT BE THE MINIMUM OF THE NEW GROUP.
C                       THIS DOESN'T MATTER; JMIN( ) IS REALLY THE
C                       GROUP REFERENCE AND DOESN'T HAVE TO BE THE
C                       SMALLEST VALUE.  (INPUT/OUTPUT)
C             JMAX(J) = THE MAXIMUM OF EACH GROUP (J=1,LX). 
C                       (INPUT/OUTPUT)
C             LBIT(J) = THE NUMBER OF BITS NECESSARY TO PACK EACH GROUP
C                       (J=1,LX).  (INPUT/OUTPUT)
C              NOV(J) = THE NUMBER OF VALUES IN EACH GROUP (J=1,LX).
C                       (INPUT/OUTPUT)
C                  LX = THE NUMBER OF GROUPS.  THIS WILL BE INCREASED
C                       IF GROUPS ARE SPLIT.  (INPUT/OUTPUT)
C                 NDG = THE DIMENSION OF JMIN( ), JMAX( ), LBIT( ), AND
C                       NOV( ).  (INPUT)
C                IBIT = THE NUMBER OF BITS NECESSARY TO PACK THE JMIN(J)
C                       VALUES, J=1,LX.  (INPUT)
C                JBIT = THE NUMBER OF BITS NECESSARY TO PACK THE LBIT(J)
C                       VALUES, J=1,LX.  (INPUT)
C                KBIT = THE NUMBER OF BITS NECESSARY TO PACK THE NOV(J)
C                       VALUES, J=1,LX.  IF THE GROUPS ARE SPLIT, KBIT
C                       IS REDUCED.  (INPUT/OUTPUT)
C              NOVREF = REFERENCE VALUE FOR NOV( ).  (INPUT)
C            IBXX2(J) = 2**J (J=0,30).  (INPUT)
C                 IER = ERROR RETURN.  (OUTPUT)
C                         0 = GOOD RETURN.
C                       714 = PROBLEM IN ALGORITHM.  REDUCE ABORTED.
C                       715 = NGP NOT LARGE ENOUGH.  REDUCE ABORTED.
C           NTOTBT(J) = THE TOTAL BITS USED FOR THE PACKING BITS J
C                       (J=1,30).  (INTERNAL)
C            NBOXJ(J) = NEW BOXES NEEDED FOR THE PACKING BITS J
C                       (J=1,30).  (INTERNAL)
C           NEWBOX(L) = NUMBER OF NEW BOXES (GROUPS) FOR EACH ORIGINAL
C                       GROUP (L=1,LX) FOR THE CURRENT J.  (AUTOMATIC)
C                       (INTERNAL)
C          NEWBOXP(L) = SAME AS NEWBOX( ) BUT FOR THE PREVIOUS J.
C                       THIS ELIMINATES RECOMPUTATION.  (AUTOMATIC)
C                       (INTERNAL)
C               CFEED = CONTAINS THE CHARACTER REPRESENTATION
C                       OF A PRINTER FORM FEED.  (CHARACTER) (INTERNAL)
C               IFEED = CONTAINS THE INTEGER VALUE OF A PRINTER
C                       FORM FEED.  (INTERNAL)
C              IORIGB = THE ORIGINAL NUMBER OF BITS NECESSARY
C                       FOR THE GROUP VALUES.  (INTERNAL)
C        1         2         3         4         5         6         7 X
C
C        NON SYSTEM SUBROUTINES CALLED 
C           NONE
c
      CHARACTER*1 CFEED
C
      DIMENSION JMIN(NDG),JMAX(NDG),LBIT(NDG),NOV(NDG)
      DIMENSION NEWBOX(NDG),NEWBOXP(NDG)
C        NEWBOX( ) AND NEWBOXP( ) ARE AUTOMATIC ARRAYS.
      DIMENSION NTOTBT(31),NBOXJ(31)
      DIMENSION IBXX2(0:30)
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/util/src/grib2packer/RCS/reduce.f,v $
     . $',                                                             '
     .$Id: reduce.f,v 1.1 2004/09/16 16:52:29 dsa Exp $
     . $' /
C    ===================================================================
C
C
      DATA IFEED/12/
C
      IER=0
      IF(LX.EQ.1)GO TO 410
C        IF THERE IS ONLY ONE GROUP, RETURN.
C
      CFEED=CHAR(IFEED)
C
C        INITIALIZE NUMBER OF NEW BOXES PER GROUP TO ZERO.
C
      DO 110 L=1,LX
         NEWBOX(L)=0
 110  CONTINUE
C
C        INITIALIZE NUMBER OF TOTAL NEW BOXES PER J TO ZERO.
C
      DO 112 J=1,31
         NTOTBT(J)=999999999
         NBOXJ(J)=0
 112  CONTINUE
C
      IORIGB=(IBIT+JBIT+KBIT)*LX
C        IBIT = BITS TO PACK THE JMIN( ).
C        JBIT = BITS TO PACK THE LBIT( ).
C        KBIT = BITS TO PACK THE NOV( ).
C        LX = NUMBER OF GROUPS.
         NTOTBT(KBIT)=IORIGB
C           THIS IS THE VALUE OF TOTAL BITS FOR THE ORIGINAL LX
C           GROUPS, WHICH REQUIRES KBITS TO PACK THE GROUP
C           LENGHTS.  SETTING THIS HERE MAKES ONE LESS LOOPS
C           NECESSARY BELOW.
C
C        COMPUTE BITS NOW USED FOR THE PARAMETERS DEFINED.
C
C        DETERMINE OTHER POSSIBILITES BY INCREASING LX AND DECREASING
C        NOV( ) WITH VALUES GREATER THAN THRESHOLDS.  ASSUME A GROUP IS
C        SPLIT INTO 2 OR MORE GROUPS SO THAT KBIT IS REDUCED WITHOUT
C        CHANGING IBIT OR JBIT.
C
      JJ=0
C
      DO 200 J=MIN(30,KBIT-1),2,-1
C           VALUES GE KBIT WILL NOT REQUIRE SPLITS.  ONCE THE TOTAL
C           BITS START INCREASING WITH DECREASING J, STOP.  ALSO, THE
C           NUMBER OF BITS REQUIRED IS KNOWN FOR KBITS = NTOTBT(KBIT).
C
         NEWBOXT=0
C
         DO 190 L=1,LX
C
            IF(NOV(L).LT.IBXX2(J))THEN
               NEWBOX(L)=0
C                 NO SPLITS OR NEW BOXES.
               GO TO 190
            ELSE
               NOVL=NOV(L)
C
               M=(NOV(L)-1)/(IBXX2(J)-1)+1
C                 M IS FOUND BY SOLVING THE EQUATION BELOW FOR M:
C                 (NOV(L)+M-1)/M LT IBXX2(J)
C                 M GT (NOV(L)-1)/(IBXX2(J)-1)
C                 SET M = (NOV(L)-1)/(IBXX2(J)-1)+1
 130           NOVL=(NOV(L)+M-1)/M
C                 THE +M-1 IS NECESSARY.  FOR INSTANCE, 15 WILL FIT
C                 INTO A BOX 4 BITS WIDE, BUT WON'T DIVIDE INTO
C                 TWO BOXES 3 BITS WIDE EACH.
C      
               IF(NOVL.LT.IBXX2(J))THEN
                  GO TO 185
               ELSE
                  M=M+1
C***                  WRITE(KFILDO,135)L,NOV(L),NOVL,M,J,IBXX2(J)
C*** 135              FORMAT(/' AT 135--L,NOV(L),NOVL,M,J,IBXX2(J)',6I10)               
                  GO TO 130
               ENDIF
C
C                 THE ABOVE DO LOOP WILL NEVER COMPLETE.
            ENDIF
C
 185        NEWBOX(L)=M-1
            NEWBOXT=NEWBOXT+M-1
 190     CONTINUE
C
         NBOXJ(J)=NEWBOXT
         NTOTPR=NTOTBT(J+1)
         NTOTBT(J)=(IBIT+JBIT)*(LX+NEWBOXT)+J*(LX+NEWBOXT)
C
         IF(NTOTBT(J).GE.NTOTPR)THEN
            JJ=J+1
C              THE PLUS IS USED BECAUSE J DECREASES PER ITERATION.
            GO TO 250
         ELSE
C
C              SAVE THE TOTAL NEW BOXES AND NEWBOX( ) IN CASE THIS
C              IS THE J TO USE.
C
            NEWBOXTP=NEWBOXT
C
            DO 195 L=1,LX
               NEWBOXP(L)=NEWBOX(L)
 195        CONTINUE
C
D           WRITE(KFILDO,197)NEWBOXT,IBXX2(J)
D197        FORMAT(/' *****************************************'
D    1             /' THE NUMBER OF NEWBOXES PER GROUP OF THE TOTAL',
D    2              I10,' FOR GROUP MAXSIZE PLUS 1 ='I10
D    3             /' *****************************************')
D           WRITE(KFILDO,198) (NEWBOX(L),L=1,LX)
D198        FORMAT(/' '20I6/(' '20I6))
    
         ENDIF
C        
D205     WRITE(KFILDO,209)KBIT,IORIGB
D209     FORMAT(/' ORIGINAL BITS WITH KBIT OF',I5,' =',I10)
D        WRITE(KFILDO,210)(N,N=2,10),(IBXX2(N),N=2,10),
D    1                    (NTOTBT(N),N=2,10),(NBOXJ(N),N=2,10),
D    2                    (N,N=11,20),(IBXX2(N),N=11,20),
D    3                    (NTOTBT(N),N=11,20),(NBOXJ(N),N=11,20),
D    4                    (N,N=21,30),(IBXX2(N),N=11,20),
D    5                    (NTOTBT(N),N=21,30),(NBOXJ(N),N=21,30)
D210     FORMAT(/' THE TOTAL BYTES FOR MAXIMUM GROUP LENGTHS BY ROW'//
D    1      '   J         = THE NUMBER OF BITS PER GROUP LENGTH'/
D    2      '   IBXX2(J)  = THE MAXIMUM GROUP LENGTH PLUS 1 FOR THIS J'/
D    3      '   NTOTBT(J) = THE TOTAL BITS FOR THIS J'/
D    4      '   NBOXJ(J)  = THE NEW GROUPS FOR THIS J'/
D    5      4(/10X,9I10)/4(/10I10)/4(/10I10))
C
 200  CONTINUE
C
 250  PIMP=((IORIGB-NTOTBT(JJ))/FLOAT(IORIGB))*100.
D     WRITE(KFILDO,252)PIMP,KBIT,JJ
D252  FORMAT(/' PERCENT IMPROVEMENT =',F6.1,
D    1        ' BY DECREASING GROUP LENGTHS FROM',I4,' TO',I4,' BITS')
      IF(PIMP.GE.2.)THEN
C
D        WRITE(KFILDO,255)CFEED,NEWBOXTP,IBXX2(JJ)
D255     FORMAT(A1,/' *****************************************'
D    1             /' THE NUMBER OF NEWBOXES PER GROUP OF THE TOTAL',
D    2             I10,' FOR GROUP MAXSIZE PLUS 1 ='I10
D    2             /' *****************************************')
D        WRITE(KFILDO,256) (NEWBOXP(L),L=1,LX)
D256     FORMAT(/' '20I6)
C
C           ADJUST GROUP LENGTHS FOR MAXIMUM LENGTH OF JJ BITS.
C           THE MIN PER GROUP AND THE NUMBER OF BITS REQUIRED
C           PER GROUP ARE NOT CHANGED.  THIS MAY MEAN THAT A
C           GROUP HAS A MIN (OR REFERENCE) THAT IS NOT ZERO.
C           THIS SHOULD NOT MATTER TO THE UNPACKER.
C
         LXNKP=LX+NEWBOXTP
C           LXNKP = THE NEW NUMBER OF BOXES
C  
         IF(LXNKP.GT.NDG)THEN
C              DIMENSIONS NOT LARGE ENOUGH.  PROBABLY AN ERROR
C              OF SOME SORT.  ABORT.
D           WRITE(KFILDO,257)NDG,LXNPK
C        1         2         3         4         5         6         7 X
D257        FORMAT(/' DIMENSIONS OF JMIN, ETC. IN REDUCE =',I8,
D    1              ' NOT LARGE ENOUGH FOR THE EXPANDED NUMBER OF',
D    2              ' GROUPS =',I8,'.  ABORT REDUCE.')
            IER=715
            GO TO 410
C              AN ABORT CAUSES THE CALLING PROGRAM TO REEXECUTE 
C              WITHOUT CALLING REDUCE.
         ENDIF
C
         LXN=LXNKP
C           LXN IS THE NUMBER OF THE BOX IN THE NEW SERIES BEING
C           FILLED.  IT DECREASES PER ITERATION.
         IBXX2M1=IBXX2(JJ)-1
C           IBXX2M1 IS THE MAXIMUM NUMBER OF VALUES PER GROUP.
C
         DO 300 L=LX,1,-1
C
C              THE VALUES IS NOV( ) REPRESENT THOSE VALUES + NOVREF.
C              WHEN VALUES ARE MOVED TO ANOTHER BOX, EACH VALUE
C              MOVED TO A NEW BOX REPRESENTS THAT VALUE + NOVREF.
C              THIS HAS TO BE CONSIDERED IN MOVING VALUES.
C
            IF(NEWBOXP(L)*(IBXX2M1+NOVREF)+NOVREF.GT.NOV(L)+NOVREF)THEN
C                 IF THE ABOVE TEST IS MET, THEN MOVING IBXX2M1 VALUES
C                 FOR ALL NEW BOXES WILL LEAVE A NEGATIVE NUMBER FOR
C                 THE LAST BOX.  NOT A TOLERABLE SITUATION.
               MOVMIN=(NOV(L)-(NEWBOXP(L))*NOVREF)/NEWBOXP(L)
               LEFT=NOV(L)
C                 LEFT = THE NUMBER OF VALUES TO MOVE FROM THE ORIGINAL
C                 BOX TO EACH NEW BOX EXCEPT THE LAST.  LEFT IS THE
C                 NUMBER LEFT TO MOVE.
            ELSE
               MOVMIN=IBXX2M1
C                 MOVMIN VALUES CAN BE MOVED FOR EACH NEW BOX.
               LEFT=NOV(L)
C                 LEFT IS THE NUMBER OF VALUES LEFT TO MOVE.
            ENDIF
C
            IF(NEWBOXP(L).GT.0)THEN
               IF((MOVMIN+NOVREF)*NEWBOXP(L)+NOVREF.LE.NOV(L)+NOVREF.
     1          AND.(MOVMIN+NOVREF)*(NEWBOXP(L)+1).GE.NOV(L)+NOVREF)THEN
                  GO TO 288
               ELSE
C***D                 WRITE(KFILDO,287)L,MOVMIN,NOVREF,NEWBOXP(L),NOV(L)
C***D287              FORMAT(/' AT 287 IN REDUCE--L,MOVMIN,NOVREF,',
C***D    1                    'NEWBOXP(L),NOV(L)',5I12
C***D    2                    ' REDUCE ABORTED.')
D              WRITE(KFILDO,2870)
D2870          FORMAT(/' AN ERROR IN REDUCE ALGORITHM.  ABORT REDUCE.')
               IER=714
               GO TO 410
C                 AN ABORT CAUSES THE CALLING PROGRAM TO REEXECUTE 
C                 WITHOUT CALLING REDUCE.
               ENDIF
C
            ENDIF
C
 288        DO 290 J=1,NEWBOXP(L)+1
               MOVE=MIN(MOVMIN,LEFT)
               JMIN(LXN)=JMIN(L)
               JMAX(LXN)=JMAX(L)
               LBIT(LXN)=LBIT(L)
               NOV(LXN)=MOVE
               LXN=LXN-1
               LEFT=LEFT-(MOVE+NOVREF)
C                 THE MOVE OF MOVE VALUES REALLY REPRESENTS A MOVE OF
C                 MOVE + NOVREF VALUES.
 290        CONTINUE
C
            IF(LEFT.NE.-NOVREF)THEN
C***               WRITE(KFILDO,292)L,LXN,MOVE,LXNKP,IBXX2(JJ),LEFT,NOV(L),
C***     1                          MOVMIN
C*** 292           FORMAT(' AT 292 IN REDUCE--L,LXN,MOVE,LXNKP,',
C***     1                'IBXX2(JJ),LEFT,NOV(L),MOVMIN'/8I12)
            ENDIF
C     
 300     CONTINUE
C
         LX=LXNKP
C           LX IS NOW THE NEW NUMBER OF GROUPS.
         KBIT=JJ
C           KBIT IS NOW THE NEW NUMBER OF BITS REQUIRED FOR PACKING
C           GROUP LENGHTS.
      ENDIF
C
D     WRITE(KFILDO,406)CFEED,LX
D406  FORMAT(A1,/' *****************************************'
D    1          /' THE GROUP SIZES NOV( ) AFTER REDUCTION IN SIZE',
D    2           ' FOR'I10,' GROUPS',
D    3          /' *****************************************')
D     WRITE(KFILDO,407) (NOV(J),J=1,LX)
D407  FORMAT(/' '20I6)
D     WRITE(KFILDO,408)CFEED,LX
D408  FORMAT(A1,/' *****************************************'
D    1          /' THE GROUP MINIMA JMIN( ) AFTER REDUCTION IN SIZE',
D    2           ' FOR'I10,' GROUPS',
D    3          /' *****************************************')
D     WRITE(KFILDO,409) (JMIN(J),J=1,LX)
D409  FORMAT(/' '20I6)
C
 410  RETURN
      END
      
