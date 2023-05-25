	program main
c---------------------------------------------------------------------------
c	character*30 filename
c	character*4 sta
c	character*3 chan
c	character*1 net
c	character*1 loc
c	character*3 units
c	character*2 rtype
c	character*1 verbose
c	character*30 datime

	IMPLICIT NONE

	character*80 filename
	character*10 sta
	character*10 chan
	character*10 net
	character*10 loc
	character*10 units
	character*10 rtype
	character*10 verbose
	character*80 datime
	
        integer*4 nfreqs
	real*4 freqs(100)
	real*4 resp(200)
        integer*4 start_stage
        integer*4 stp_stage
        integer*4 stdio_flag

	integer*4 stalen
	integer*4 chalen
	integer*4 netlen
	integer*4 loclen
	integer*4 unitlen
	integer*4 filelen
	integer*4 rtypelen
	integer*4 datimelen
	integer*4 verboselen

	integer*4 i
	integer*4 length
	character*10 newchan
	real*4 val
	

	stalen = 4
	chalen = 3
	netlen = 1
	loclen = 1
	datimelen = 15
	rtypelen = 2
	filelen = 28
	unitlen = 3
	verboselen = 1


	val = 0.001
	nfreqs = 100
	do 100 i = 1, 	nfreqs
	   freqs(i) = val
	   val = val * 1.123324
 100	continue

	datime = '1990,1,00:00:00'
	filename = './RESP.IU.ANMO..BHZ'
	chan = 'BHZ'
	sta = 'ANMO'
	net = '*'
	loc = '*'
	units = 'VEL'
	rtype = 'AP'
	verbose = 'Y'

	start_stage = -1
	stp_stage = 0
	stdio_flag = 0

	CALL EVRESP_(sta,chan,net,loc,datime,units,filename, freqs, nfreqs, resp, rtype, verbose, start_stage, stp_stage, stdio_flag, stalen, chalen, netlen, loclen, datimelen, unitlen, filelen, rtypelen, verboselen)


	write (*,*) '------------------------------------'
	do 200 i = 1, nfreqs

	   write (*,10) freqs(i),resp(i*2), resp(i*2 +1)

 200	continue 

	write (*,*) '------------------------------------'

 10	format (E14.6, E14.6, E14.6)
	return
	
	end
