package gov.noaa.nws.ncep.viz.rsc.ncgrid.rsc;

/**
 * The grid contour attribute editing dialog help.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer    Description
 * ------------ ----------  ----------- --------------------------
 * Nov,22 2010  			 X. Guo     Initial creation for both Ensemble/Grid
 *
 * 
 * @author xguo
 * @version 1
 */
public class NcgridAttributesHelp {
	
    public static String TitleToolTipText() {
    	String toolTipText="Title string to be displayed as data resource legend.";
		
    	return toolTipText;	
	}
    
    public static String HlsymToolTipText() {
		String toolTipText="HLSYM defines the characteristics for the HILO symbols specified\n"+
		"in HILO.  The text sizes, value position, fonts, text widths and\n"+
		"hardware/software flags are specified for the symbols (s) and\n"+
		"plotted values (v) as:\n\n"+

		"   sizes;sizev/position/fonts;fontv/widths;widthv/hwflgs;hwflgv\n\n"+

		"The size, font, width, and hw flag are the same as the TEXT\n"+
		"variable.  If only one value is given, it is used for both the\n"+
		"symbol and value.\n"+
		"The value plotting position may be 1, 2, or 3 where 2 is the\n"+
		"default.  The number selects the position of the value string\n"+
		"beneath the symbol string.  The three positions are shown below:\n\n"+
		"                        H\n"+
		"                     1  2  3\n\n"+
		"It is common for HILO symbols near the edge of the display to be\n"+
		"hidden when hardware text font is used. Therefore, when using\n"+
		"hardware text font, the number of HILO symbols displayed may be\n"+
		"slightly less than what the user specifies.\n\n"+
		"Examples:\n\n"+
		"           HLSYM = 2;1/3/2//HW  -- symbol text size = 2\n"+
		"                                   value text size = 1\n"+
		"                                   plot value in position 3\n"+
		"                                   hardware text font 2 applies to both\n";

		return toolTipText;	
	}

    public static String HiloToolTipText() {
		String toolTipText="HILO contains the information for plotting relative highs and lows\n"+
		"in the following format:\n\n"+

		"   colorh;colorl/symbolh;symboll/rangeh;rangel/radius/counth;countl/interp\n\n"+

		"Colorh and colorl are the colors for the high and low symbols to be\n"+
		"plotted.  If only a single number is entered, it will be used for\n"+
		" both highs and lows.  The default for this entry is 0.\n\n"+
		"Symbolh and symboll specify the symbols to be plotted.  The format\n"+
		"for the symbol input is:\n\n"+
		"                character # precision\n\n"+
		"where the character is the character to be plotted.  If the character\n"+
		"is an integer, markers corresponding to that number will be plotted.\n"+
		"If the character is an integer preceeded by the character 'S', special\n"+
		"symbols corresponding to that number will be plotted.\n"+
		"Information about markers can be found in the help for MARKER.\n"+
		"The # is a flag to plot values beneath the marker.  The integer followin\n"+
		"the # is the number of decimal places to display in the value.  If a\n"+
		"# is present without the following number, integer values are printed.\n"+
		"The default for the symbols is H;L.\n\n"+
		"Rangeh and rangel are ranges for highs and lows specified as:\n"+
		"                minval - maxval \n\n"+
		"where minval and maxval are integers which specify the range of values\n"+
		"to be considered for designation as a high or low.  The default is to\n"+
		"consider all data.\n\n"+
		"The search radius is a sqaure region of grid points.The default is 3.\n"+
		"A large radius, such as 10 or higher, is not very effective.\n\n"+
		"Counth and countl are integer values for the maximum number of\n"+
		"high and low values to be displayed.  The default is 20;20.\n\n"+
		"Interp is an interpolation flag which specifies whether the values and\n"+
		"locations of the highs and lows will be at grid points, or will be\n"+
		"interpolated between grid points.  The default is NO.\n";

		return toolTipText;	
	}
    
    public static String WindToolTipText() {
		String toolTipText="WIND specifies wind color, size and width \n" +
		"separated by slashes\n\n"+ 

		"		color / size / width\n";

		return toolTipText;	
	}

	public static String FlineToolTipText() {
		String toolTipText="FLINE is the color and fill type to be used for contour fill:\n\n"+

		"   colr1;..;colrn/type1;..;typen\n\n"+

		"The number of fill colors and types needed is one greater than\n"+
		"the number of fill levels in FINT.  The number of fill colors \n"+
		"may be entered as a list of color numbers separated by \n"+
		"semicolons or a range of colors.  The number of fill types may \n"+
		"be entered as a list of numbers separated by semicolons.  \n"+
		" More information on color selection can be found in the help \n"+
		"for COLORS.\n\n"+

		"The fill type may be set any of the following values:\n\n"+

		"           1               Solid\n"+
		"           2               Slanted Dash\n"+
		"           3               Wide-spaced Slanted Line\n"+
		"           4               Medium-spaced Slanted Line\n"+
		"           5               Zig-Zag Line\n"+
		"           6               Dots\n"+
		"           7               Thin-spaced Slanted Line\n\n"+

		"If fill type is set to 0, soild fill is used. If the fill type is\n"+
		"set to a single negative number, negative values will use the\n"+
		"absolute value of the fill type, and positive values will be solid.\n";

		return toolTipText;	
	}

	public static String FintToolTipText() {
		String toolTipText="FINT is the contour fill interval, minimum and maximum values\n"+
		"separated by slashes:\n\n"+

		"           fill interval / minimum / maximum\n\n"+

		"The contour fill interval may be any real number.\n\n"+

		"The minimum and maximum values specify the range of data\n"+
		"to use in selecting the fill levels.  If either value \n"+
		"is not specified, thevalue will be obtained from the \n"+
		"range of values in the dataset.  If the minimum and \n"+
		"maximum are equal, that value will be used and only one \n"+
		"contour fill level will be selected; however, since the \n"+
		"number of colors is one greater than the number of fill \n"+
		"levels, two colors will be needed--the first for filling \n"+
		"regions with values less than the input value and the \n"+
		"second for filling regions of greater value.\n\n"+

		"A list of two or more fill levels may be entered using semicolons\n"+
		"to separate the individual values.  In this case, the minimum \n"+
		"and maximum are ignored.";

		return toolTipText;		
	}

	public static String CintToolTipText() {
    	String toolTipText="TWO FORMATS OF CINT\n\n" +
    			"FORMAT1: INTERVAL1/MIN1/MAX1>INTERVAL2/MIN2/MAX2>...\n\n"+
    			"FORMAT2: VALUE11;VALUE12;...;VALUE1n>VALUE21;VALUE22;...;VALUE2n>...";
    			
    	return toolTipText;		
    }
    
    public static String GlevelToolTipText() {
    	String text = "GLEVEL is the vertical level for the grid.\n" +

     "Grids may contain two levels separated by a colon.  If the grid\n" +
     "to be selected contains only one level, the colon and second \n" +
     "level may be omitted.  In this case, the second level is stored \n" +
     "in the grid file as -1.\n" +
      "\n" +
     "Note that the vertical coordinate system for GLEVEL is \n" +
     "specified by GVCORD.\n\n" +
     "The value in GLEVEL may be overridden by specifying @GLEVEL \n" +
     "with the grids to be found.  For example, the following two \n" +
     "computations are identical:\n\n" +

     "           GFUNC = SUB (TMPF@850,TMPF@500)\n\n" +

     "           GFUNC = LDF (TMPF)  and  GLEVEL = 850:500.\n\n" +

     "See the help information on gparm for information on how \n" +
     "to specify a layer in GLEVEL for a LYR_ function."; 
     		
     
     	return text;
    }
    
    public static String GvcordToolTipText() {
    	String text = "GVCORD is the vertical coordinate of the grid to\n be selected.\n" +

     "The standard values are:\n\n" +

     "   NONE    for surface data\n" +
     "   PRES    data in pressure coordinates (millibars)\n" +
     "   THTA    data in isentropic coordinates (Kelvin)\n" +
     "   HGHT    data in height coordinates (meters)\n" +
     "   SGMA    data in sigma coordinates\n\n" +

     "The value in GVCORD may be overridden by specifying %GVCORD \n" +
     "with the grids to be found.  For example:\n\n" +

     "  GFUNC = SUB ( TMPC @850 %PRES, TMPC @1500 %HGHT )\n\n" +

     "will compute the difference between temperatures on the\n" +
     "850-mb level and the 1500-meter level.\n";
     		
     
     	return text;
    }
    
    public static String ScaleToolTipText() {
    	String text = "SCALE is the scaling factor for the data. \n" + 
    	"All data will be multiplied by 10 ** SCALE.";
     		
        return text;
    }
    
    public static String GdpfunToolTipText() {
    	String text = "GDPFUN specifies a grid diagnostic function\n" +
    	"which yields either a scalar or vector quantity.    \n" +
    	"For more information, see the GPARM documentation.";
     	
        return text;
    }
    
    public static String TypeToolTipText() {
    	String text = "TYPE specifies the processing type for the \n" +
    	"GDPFUN parameter. The TYPE list does not need separators, \n" +
    	"however slashes could be     used for clarity:\n\n" +

        "        type 1 / type 2 / ... / type n  \n\n" +

     "Valid inputs for type are:\n\n" +

     "   SCALAR TYPEs:\n" +
     "   C       the original GEMPAK contouring algorithm\n" +
     "   F       contour fill algorithm\n" +
     "   P       plot grid point values\n" +
     "   D       plot scaler as a directional arrow\n\n" +

     "   VECTOR TYPEs:\n" +
     "   A       wind arrows\n" +
     "   B       wind barbs\n" +
     "   S       streamlines\n\n" +

     "   OTHER TYPEs:\n" +
     "   M       plot grid point markers\n" +
     "   G       plot grid indices (row/column numbers)";
     	
        return text;
    }
    
    public static String LineToolTipText() {
    	String text = "LINE is the color, line type, line width\n" +
     "separated by slashes. The individual values in each \n" +
     "group are separated by semicolons:\n\n" +

     "colr1;..;colrn/type1;..;typen/width1;..;widthn\n\n" +

    "There are ten distinct line types:\n\n" +

    "  1       -       solid\n" +
    "  2       -       short dashed\n" +
    "  3       -       medium dashed\n" +
    "  4       -       long dash short dash\n" +
    "  5       -       long dash\n" +
    "  6       -       long dash three short dashes\n" +
    "  7       -       long dash dot\n" +
    "  8       -       long dash three dots\n" +
    "  9       -       medium dash dot\n" +
    "  10      -       dotted";
     	
        return text;
    }
    
    public static String MarkerToolTipText() {
    	String text = "MARKER specifies the marker color, type, size, \n" +
    	"line width separated by slashes:\n\n" +

         "    marker color / marker type / size / width\n\n" +

     "If the marker color is 0, no markers will be drawn.\n" +
     "If the marker color is not specified, a default of 1\n " +
     "will be used.\n\n" +

    "The marker type specifies the shape of the marker.\n" +
     "The software marker types are:\n\n" +

      "   1      plus sign			12      asterisk\n" +
      "   2      octagon			13      hourglass X\n" +
      "   3      triangle			14      star\n" +
      "   4      box				15      dot\n" +
      "   5      small X			16      large X\n" +
      "   6      diamond			17      filled octagon\n" +
      "   7      up arrow			18      filled triangle\n" +
      "   8      X with top bar		19      filled box\n" +
      "   9      Z with bar			20      filled diamond\n" +
      "  10      Y					21      filled star\n" +
      "  11      box with diagonals	22      minus sign\n";
     	
        return text;
    }
    
    public static String ColorsToolTipText() {
    	String toolTipText="COLORS is the color number to be used\n" +
    	"in plotting the grid point values.  If COLORS = 0 \n" +
    	"or blank, grid point values are not plotted.";
    			
    	return toolTipText;		
    }
    
    public static String GrdlblToolTipText() {
    	String text = "GRDLBL is the color number to be used\n" +
    	"in plotting the grid index numbers.  If GRDLBL = 0 \n" +
    	"or blank, grid index numbers are not plotted.";
    	
        return text;
    }
}
