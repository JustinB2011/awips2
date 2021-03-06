C MODULE RDLIST
C-----------------------------------------------------------------------
C
C  ROUTINE TO READS A LIST OF CHARACTER STRINGS USING FREE FORMAT.
C
      SUBROUTINE RDLIST (ICARDS,NCARDS,ICARD,LIST,NLIST,NWORDS,NUMC,IER)
C
C   THIS ROUTINE READS A LIST OF CHARACTER STRING ITEMS.
C   THE ITEMS ARE READ IN FREE FORMAT. THE LIST CAN BE CONTINUED
C   ON ANY NUMBER OF CONTINUATION CARDS. CONTINUATION IS SPECIFIED
C   BY AN AMPERSAND (&) AS THE LAST ITEM ON A CARD.
C
C   ARGUMENT LIST:
C      INPUT:
C          ICARDS - NUMBER OF CURRENT CARD IN GROUP OF INPUT CARDS
C          NCARDS - TOTAL NUMBER OF CARDS IN INPUT CARD GROUP
C          ICARDS - ARRAY HOLDING CARD IMAGES IN 20A4 FORMAT
C          NWORDS - NUMBER OF WORDS IN LIST ARRAY
C      OUTPUT:
C            LIST - ARRAY HOLDING STRINGS IN 2A4 FORMAT
C           NLIST - NUMBER OF ITEMS IN LIST
C            NUMC - NUMBER OF CHARACTERS IN EACH ITEM IN LIST
C             IER - ERROR INDICATOR:
C                     0=NO ERROR
C                     1=ERROR
C
C  ORIGINALLY PROGRAMMED BY -- JOE OSTROWSKI -- HRL -- 801229
C
      CHARACTER*8 RTNNAM,OPNOLD
      CHARACTER*80 ICARD(NCARDS),STRNG,IBUF
C
      PARAMETER (MLIST=100)
      DIMENSION LIST(5,MLIST),NUMC(MLIST)
C
      INCLUDE 'common/ionum'
      INCLUDE 'common/fdbug'
      INCLUDE 'common/errdat'
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/fcinit_top/RCS/rdlist.f,v $
     . $',                                                             '
     .$Id: rdlist.f,v 1.3 2000/03/14 11:58:20 page Exp $
     . $' /
C    ===================================================================
C
C
      IER=0
C
      IF (ITRACE.GT.0) WRITE (IODBUG,*) 'ENTER RDLIST'
C
      IBUG=IFBUG('PSEG')
C
      RTNNAM='FGDEF'
      IOPNUM=0
      CALL FSTWHR (RTNNAM,IOPNUM,OPNOLD,IOLDOP)
C
      CALL UMEMOV (ICARD(ICARDS),IBUF,20)
C
      NLIST=0
      NSCAN=1
C
10    IBUF=ICARD(ICARDS)
      NSCAN=NSCAN+1
      IF (IBUG.GE.1) WRITE (IODBUG,*) 'ICARDS=',ICARDS,
     *   ' NCARDS=',NCARDS,' IBUF=',IBUF
C
C  GET NEXT FIELD
      CALL USCAN2 (IBUF(1:72),' ',NSCAN,STRNG,LSTRNG,IERR)
      IF (IBUG.GE.1) WRITE (IODBUG,*) 'STRNG=',STRNG
      IF (STRNG.EQ.' ') GO TO 60
C
C  CHECK FOR COMMENT INDICATOR
      IF (STRNG.EQ.'$') GO TO 60
C
C  CHECK FOR CONTINUATION INDICATOR
      IF (STRNG.EQ.'&') GO TO 50
C
      IF (NLIST+1.GT.MLIST) THEN
         WRITE (IPR,20) MLIST
20    FORMAT ('0**ERROR** MAXIMUM NUMBER OF ITEMS ALLOWED IN LIST (',I3,
     *   ') EXCEEDED.')
         CALL ERROR
         IER=1
         GO TO 60
         ENDIF
C
      NLIST=NLIST+1
C
C  STORE NUMBER OF CHARACTER IN ITEM
      NUMC(NLIST)=LSTRNG
      IF (IBUG.GE.1) WRITE (IODBUG,*) 'NLIST=',NLIST,
     *   ' NUMC(NLIST)=',NUMC(NLIST)
C
C  CHECK IF NUMBER OF CHARS EXCEEDS THAT EXPECTED
      IF (LSTRNG/4.GT.NWORDS) THEN
         NMCH=NWORDS*4
         WRITE (IPR,30) STRNG,NMCH
30    FORMAT ('0**ERROR** NUMBER OF CHARACTERS IN THE ITEM ',A,
     *   ' EXCEEDS THE MAXIMUM (',I2,').')
         CALL ERROR
         IER=1
         ENDIF
C
      CALL UMEMOV (STRNG,LIST(1,NLIST),NWORDS)
C
      IF (IBUG.GE.1) WRITE (IODBUG,40) NLIST,(LIST(I,NLIST),I=1,NWORDS)
40    FORMAT (' NLIST=',I3,' (LIST(I,NLIST),I=1,NWORDS=)',5A4)
C
      GO TO 10
C
50    ICARDS=ICARDS+1
      IF (ICARDS.LE.NCARDS) THEN
         CALL UMEMOV (ICARD(ICARDS),IBUF,20)
         NSCAN=0
         GO TO 10
         ENDIF
C
60    CALL FSTWHR (OPNOLD,IOLDOP,OPNOLD,IOLDOP)
C
      IF (ITRACE.GT.0) WRITE (IPR,*) 'EXIT RDLIST'
C
      RETURN
C
      END
