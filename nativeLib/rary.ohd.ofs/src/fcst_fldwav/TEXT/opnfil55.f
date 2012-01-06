C$PRAGMA C (GET_APPS_DEFAULTS)
C$PRAGMA C (CHECK_EXIST)
CC AV-- 6/29/01 this is not c routine C$PRAGMA C (DIRNAME)
C MODULE OPNFIL55
C-----------------------------------------------------------------------
C
      SUBROUTINE OPNFIL55 (IOPNERR)

C  THIS ROUTINE OPENS THE FILES NEEDED FOR PROGRAM FLDGRF.
C
      CHARACTER*20 FILNAM
      CHARACTER*20 FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,FILEFORM_U
      CHARACTER*100 ENVVAR1,ENVVAR2
      CHARACTER*150 FILNM
      CHARACTER*150 DIRNAME,PATHNAME,UNIXCMD
C
      INCLUDE 'common/ionum'
      INCLUDE 'common/fcsegn'
      INCLUDE 'common/opfil55'
C
C    ================================= RCS keyword statements ==========
      CHARACTER*68     RCSKW1,RCSKW2
      DATA             RCSKW1,RCSKW2 /                                 '
     .$Source: /fs/hseb/ob72/rfc/ofs/src/fcst_fldwav/RCS/opnfil55.f,v $
     . $',                                                             '
     .$Id: opnfil55.f,v 1.5 2004/02/02 21:50:08 jgofus Exp $
     . $' /
C    ===================================================================
C
C
C  GET DIRECTORY NAME
      DIRNAME=' '
      IF(NFGRF.EQ.2) THEN
        ENVVAR1='fldat_dir'
      ELSE
        ENVVAR1='fldgrf_iface'
      endif
      LENVVAR1=LENSTR(ENVVAR1)
      CALL GET_APPS_DEFAULTS (ENVVAR1,LENVVAR1,DIRNAME,LDIRNAME)
      IF (LDIRNAME.EQ.0) THEN
         DIRNAME=' '
         ENVVAR2='HOME'
         LENVVAR2=LENSTR(ENVVAR2)
         CALL GET_APPS_DEFAULTS (ENVVAR2,LENVVAR2,DIRNAME,LDIRNAME)
         DIRNAME(LDIRNAME+1:LDIRNAME+1)=' '
         IF(NFGRF.EQ.2) THEN
           CALL UCNCAT (DIRNAME,'/fldat',IERR)
         ELSE
           CALL UCNCAT (DIRNAME,'/fldgrf',IERR)
         ENDIF
         CALL ULENTH (DIRNAME,LEN(DIRNAME),LDIRNAME)
         WRITE (IPR,10) ENVVAR1(1:LENVVAR1),DIRNAME(1:LDIRNAME)
10    FORMAT ('0**WARNING** FLDGRF ENVIRONMENT VARIABLE (',A,') NOT ',
     *    'SPECIFIED. FLDGRF FILES WILL BE WRITTEN TO ',A,'.')
         CALL WARN
         ENDIF
C
C  CREATE DIRECTORY
      UNIXCMD='mkdir -p '//DIRNAME
      CALL SYSTEM (UNIXCMD)
C
C  CHECK IF DIRECTORY EXISTS
      IPRINT=1
      DIRNAME(LDIRNAME+1:LDIRNAME+1)=CHAR(0)
      CALL CHECK_EXIST (DIRNAME,'directory',IEXIST,IPRINT)
      IF (IEXIST.NE.1) THEN
         CALL UEROR (LP,1,-1)
         WRITE (LP,20) DIRNAME(1:LDIRNAME)
20    FORMAT ('0**ERROR** DIRECTORY ',A,' NOT FOUND.')
         CALL ERROR
         IOPNERR=1
         ENDIF
C
      IF (IOPNERR.EQ.1) GO TO 40
C
      FILNAM=' '
      CALL UMEMOV (IDSEGN,FILNAM,2)
      IF (FILNAM.EQ.' ') FILNAM='dummy'
      FILNM=' '
      FILNM=DIRNAME(1:LDIRNAME)//'/'//FILNAM(1:LENSTR(FILNAM))
      LFILNM=LENSTR(FILNM)
C
C  OPEN FILES
C
      IF(NFGRF.EQ.2) THEN
        FILETYPE='FLDWAV-FLDAT  '
      ELSE
        FILETYPE='FLDWAV-FLDGRF '
      ENDIF
      FILEACCS='SEQUENTIAL'
      FILESTAT='UNKNOWN'
      FILEFORM_F='FORMATTED'
      FILEFORM_U='UNFORMATTED'
      LRECL=0
C
c ----------------
c open fldgrf files
c ----------------
      if(nfgrf.eq.0) then      
      PATHNAME=FILNM(1:LFILNM)//'.tim'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFTIM,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.h'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFH,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.q'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFQ,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.us'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFUS,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.ds'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFDS,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.ttl'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFTTL,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.loc'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFLOC,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.pk'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFPK,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.fld'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFFLD,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.bs'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFBS,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.hs'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFHS,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.bsl'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFBSL,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.bsr'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFBSR,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.bss'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFBSS,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.xs'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFXS,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.obs'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFOBS,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.gz'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_U,
     *   LRECL,JFGZ,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      endif
c ----------------
c open fldat files
c ----------------
      if(nfgrf.eq.2) then
      PATHNAME=FILNM(1:LFILNM)//'.ver'
      OPEN(JFVER,FILE=PATHNAME,IOSTAT=ISTAT)
      IF(ISTAT.EQ.0) CLOSE(JFTTL,STATUS='DELETE')
      PATHNAME=FILNM(1:LFILNM)//'.ttl'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFTTL,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.trib'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFTRIB,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.dist'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFDIST,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.flood'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFFLOOD,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.gage'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFGAGE,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.stat'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFSTAT,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.dsbd'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFDSBD,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.usdy'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFUSBD,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.crs'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFCRS,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.qfcst'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFQFCST,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.hfcst'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFHFCST,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.vel'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFVEL,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.time'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFTIME,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      PATHNAME=FILNM(1:LFILNM)//'.intb'
      CALL OPFILE (PATHNAME,FILETYPE,FILEACCS,FILESTAT,FILEFORM_F,
     *   LRECL,JFINTB,IERR)
      IF (IERR.NE.0) CALL OPNERR55 (PATHNAME,IOPNERR)
      endif
C
40    RETURN
C
      END
C
C-----------------------------------------------------------------------
C
      SUBROUTINE OPNERR55 (PATHNAME,IOPNERR)
C
      CHARACTER*(*) PATHNAME
      INCLUDE 'common/ionum'
C
      WRITE (IPR,10) PATHNAME(1:LENSTR(PATHNAME))
10    FORMAT ('0**ERROR** IN OPNFIL55 - CANNOT OPEN FILE ',A,'.')
      CALL ERROR
      IOPNERR=1
C
      RETURN
C
      END
