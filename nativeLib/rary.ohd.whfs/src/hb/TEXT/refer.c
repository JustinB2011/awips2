/*
** Generated by X-Designer
*/
/*
**LIBS: -lXm -lXt -lX11
*/

#include <stdlib.h>
#include <X11/Xatom.h>
#include <X11/Intrinsic.h>
#include <X11/Shell.h>

#include <Xm/Xm.h>
#include <Xm/DialogS.h>
#include <Xm/Form.h>
#include <Xm/Label.h>
#include <Xm/List.h>
#include <Xm/PushB.h>
#include <Xm/ScrollBar.h>
#include <Xm/Separator.h>
#include <Xm/Text.h>


Widget referDS = (Widget) NULL;
Widget referFM = (Widget) NULL;
Widget referSLBLbl = (Widget) NULL;
Widget referSLB = (Widget) NULL;
Widget refer_horizSB = (Widget) NULL;
Widget refer_vertSB = (Widget) NULL;
Widget referLB = (Widget) NULL;
Widget rfokPB = (Widget) NULL;
Widget rfapplyPB = (Widget) NULL;
Widget rfclosePB = (Widget) NULL;
Widget rfnewPB = (Widget) NULL;
Widget rfdelPB = (Widget) NULL;
Widget rfrefTxt = (Widget) NULL;
Widget referSep = (Widget) NULL;



void create_referDS (Widget parent)
{
	Widget children[9];      /* Children to manage */
	Arg al[64];                    /* Arg List */
	register int ac = 0;           /* Arg Count */
	XmString xmstrings[16];    /* temporary storage for XmStrings */

	XtSetArg(al[ac], XmNallowShellResize, TRUE); ac++;
	referDS = XmCreateDialogShell ( parent, "referDS", al, ac );
	ac = 0;
	XtSetArg(al[ac], XmNwidth, 808); ac++;
	XtSetArg(al[ac], XmNautoUnmanage, FALSE); ac++;
	referFM = XmCreateForm ( referDS, "referFM", al, ac );
	ac = 0;
	xmstrings[0] = XmStringCreateLtoR ( "Reference", (XmStringCharSet)XmFONTLIST_DEFAULT_TAG );
	XtSetArg(al[ac], XmNlabelString, xmstrings[0]); ac++;
	referSLBLbl = XmCreateLabel ( referFM, "referSLBLbl", al, ac );
	ac = 0;
	XmStringFree ( xmstrings [ 0 ] );
	referLB = XmCreateScrolledList ( referFM, "referLB", al, ac );
	referSLB = XtParent ( referLB );

	XtSetArg(al[ac], XmNhorizontalScrollBar, &refer_horizSB); ac++;
	XtSetArg(al[ac], XmNverticalScrollBar, &refer_vertSB); ac++;
	XtGetValues(referSLB, al, ac );
	ac = 0;
	rfokPB = XmCreatePushButton ( referFM, "rfokPB", al, ac );
	rfapplyPB = XmCreatePushButton ( referFM, "rfapplyPB", al, ac );
	rfclosePB = XmCreatePushButton ( referFM, "rfclosePB", al, ac );
	rfnewPB = XmCreatePushButton ( referFM, "rfnewPB", al, ac );
	rfdelPB = XmCreatePushButton ( referFM, "rfdelPB", al, ac );
	rfrefTxt = XmCreateText ( referFM, "rfrefTxt", al, ac );
	referSep = XmCreateSeparator ( referFM, "referSep", al, ac );


	XtSetArg(al[ac], XmNtopAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNtopOffset, 10); ac++;
	XtSetArg(al[ac], XmNbottomAttachment, XmATTACH_NONE); ac++;
	XtSetArg(al[ac], XmNleftAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNleftOffset, 10); ac++;
	XtSetArg(al[ac], XmNrightAttachment, XmATTACH_NONE); ac++;
	XtSetValues ( referSLBLbl,al, ac );
	ac = 0;

	XtSetArg(al[ac], XmNtopAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNtopOffset, 35); ac++;
	XtSetArg(al[ac], XmNbottomAttachment, XmATTACH_OPPOSITE_FORM); ac++;
	XtSetArg(al[ac], XmNbottomOffset, -180); ac++;
	XtSetArg(al[ac], XmNleftAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNleftOffset, 10); ac++;
	XtSetArg(al[ac], XmNrightAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNrightOffset, 10); ac++;
	XtSetValues ( referSLB,al, ac );
	ac = 0;

	XtSetArg(al[ac], XmNtopAttachment, XmATTACH_WIDGET); ac++;
	XtSetArg(al[ac], XmNtopOffset, 15); ac++;
	XtSetArg(al[ac], XmNtopWidget, referSep); ac++;
	XtSetArg(al[ac], XmNbottomAttachment, XmATTACH_NONE); ac++;
	XtSetArg(al[ac], XmNleftAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNleftOffset, 11); ac++;
	XtSetArg(al[ac], XmNrightAttachment, XmATTACH_NONE); ac++;
	XtSetValues ( rfokPB,al, ac );
	ac = 0;

	XtSetArg(al[ac], XmNtopAttachment, XmATTACH_WIDGET); ac++;
	XtSetArg(al[ac], XmNtopOffset, 15); ac++;
	XtSetArg(al[ac], XmNtopWidget, referSep); ac++;
	XtSetArg(al[ac], XmNbottomAttachment, XmATTACH_NONE); ac++;
	XtSetArg(al[ac], XmNleftAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNleftOffset, 180); ac++;
	XtSetArg(al[ac], XmNrightAttachment, XmATTACH_NONE); ac++;
	XtSetValues ( rfapplyPB,al, ac );
	ac = 0;

	XtSetArg(al[ac], XmNtopAttachment, XmATTACH_WIDGET); ac++;
	XtSetArg(al[ac], XmNtopOffset, 15); ac++;
	XtSetArg(al[ac], XmNtopWidget, referSep); ac++;
	XtSetArg(al[ac], XmNbottomAttachment, XmATTACH_NONE); ac++;
	XtSetArg(al[ac], XmNleftAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNleftOffset, 362); ac++;
	XtSetArg(al[ac], XmNrightAttachment, XmATTACH_NONE); ac++;
	XtSetValues ( rfclosePB,al, ac );
	ac = 0;

	XtSetArg(al[ac], XmNtopAttachment, XmATTACH_WIDGET); ac++;
	XtSetArg(al[ac], XmNtopOffset, 15); ac++;
	XtSetArg(al[ac], XmNtopWidget, referSep); ac++;
	XtSetArg(al[ac], XmNbottomAttachment, XmATTACH_NONE); ac++;
	XtSetArg(al[ac], XmNleftAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNleftOffset, 554); ac++;
	XtSetArg(al[ac], XmNrightAttachment, XmATTACH_NONE); ac++;
	XtSetValues ( rfnewPB,al, ac );
	ac = 0;

	XtSetArg(al[ac], XmNtopAttachment, XmATTACH_WIDGET); ac++;
	XtSetArg(al[ac], XmNtopOffset, 15); ac++;
	XtSetArg(al[ac], XmNtopWidget, referSep); ac++;
	XtSetArg(al[ac], XmNbottomAttachment, XmATTACH_NONE); ac++;
	XtSetArg(al[ac], XmNleftAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNleftOffset, 714); ac++;
	XtSetArg(al[ac], XmNrightAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNrightOffset, 10); ac++;
	XtSetValues ( rfdelPB,al, ac );
	ac = 0;

	XtSetArg(al[ac], XmNtopAttachment, XmATTACH_WIDGET); ac++;
	XtSetArg(al[ac], XmNtopOffset, 10); ac++;
	XtSetArg(al[ac], XmNtopWidget, referSLB); ac++;
	XtSetArg(al[ac], XmNbottomAttachment, XmATTACH_NONE); ac++;
	XtSetArg(al[ac], XmNleftAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNleftOffset, 10); ac++;
	XtSetArg(al[ac], XmNrightAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNrightOffset, 10); ac++;
	XtSetValues ( rfrefTxt,al, ac );
	ac = 0;

	XtSetArg(al[ac], XmNtopAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNtopOffset, 243); ac++;
	XtSetArg(al[ac], XmNbottomAttachment, XmATTACH_NONE); ac++;
	XtSetArg(al[ac], XmNleftAttachment, XmATTACH_FORM); ac++;
	XtSetArg(al[ac], XmNleftOffset, 0); ac++;
	XtSetArg(al[ac], XmNrightAttachment, XmATTACH_NONE); ac++;
	XtSetValues ( referSep,al, ac );
	ac = 0;
	XtManageChild(referLB);
	children[ac++] = referSLBLbl;
	children[ac++] = rfokPB;
	children[ac++] = rfapplyPB;
	children[ac++] = rfclosePB;
	children[ac++] = rfnewPB;
	children[ac++] = rfdelPB;
	children[ac++] = rfrefTxt;
	children[ac++] = referSep;
	XtManageChildren(children, ac);
	ac = 0;
}

