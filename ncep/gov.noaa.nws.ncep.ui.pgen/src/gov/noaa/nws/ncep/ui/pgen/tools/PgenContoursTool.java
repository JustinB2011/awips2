/*
 * gov.noaa.nws.ncep.ui.pgen.tools.PgenContoursTool
 * 
 * October 2009
 *
 * This code has been developed by the NCEP/SIB for use in the AWIPS2 system.
 */

package gov.noaa.nws.ncep.ui.pgen.tools;

import java.util.ArrayList;

import com.raytheon.uf.viz.core.rsc.IInputHandler;
import com.vividsolutions.jts.geom.Coordinate;

import gov.noaa.nws.ncep.ui.pgen.PgenUtil;
import gov.noaa.nws.ncep.ui.pgen.contours.ContourCircle;
import gov.noaa.nws.ncep.ui.pgen.contours.ContourMinmax;
import gov.noaa.nws.ncep.ui.pgen.contours.Contours;
import gov.noaa.nws.ncep.ui.pgen.contours.ContourLine;

import gov.noaa.nws.ncep.ui.pgen.elements.DrawableElementFactory;
import gov.noaa.nws.ncep.ui.pgen.elements.DrawableType;
import gov.noaa.nws.ncep.ui.pgen.display.ILine;
import gov.noaa.nws.ncep.ui.pgen.display.IAttribute;
import gov.noaa.nws.ncep.ui.pgen.elements.Line;
import gov.noaa.nws.ncep.ui.pgen.elements.Symbol;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;

import gov.noaa.nws.ncep.ui.pgen.attrDialog.ContoursAttrDlg;

/**
 * Implements a modal map tool for PGEN Contours drawing.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date       	Ticket#		Engineer	Description
 * ------------	----------	-----------	--------------------------
 * 10/09        #167		J. Wu  		Initial creation
 * 12/09		?			B. Yin		check if the attrDlg is 
 * 										the contours dialog
 * 12/09        #167		J. Wu  		Allow editing line and label attributes.
 * 06/10		#215		J. Wu		Added support for Contours Min/Max
 * 11/10		#345		J. Wu		Added support for Contours Circle
 * 02/11					J. Wu		Preserve auto/hide flags for text
 * 04/11		#?			B. Yin		Re-factor IAttribute
 * 
 * </pre>
 * 
 * @author	J. Wu
 */

public class PgenContoursTool extends AbstractPgenDrawingTool {

	/**
	 * Points of the new element.
	 */
    private ArrayList<Coordinate> points = new ArrayList<Coordinate>();

	/**
	 * An instance of DrawableElementFactory, which is used to 
	 * create new elements.
	 */
	private DrawableElementFactory def = new DrawableElementFactory();
	
	/**
	 * Current Contours element.
	 */
	private boolean  addContourLine = false;
	private Contours elem = null;	
	
    public PgenContoursTool(){
    	
    	super();
    	
    }
   
    /*
     * (non-Javadoc)
     * 
     * @see com.raytheon.viz.ui.tools.AbstractTool#runTool()
     */
    @Override
    protected void activateTool( ) {
    	
    	super.activateTool();
    	
    	/*
    	 * if the ExecutionEvent's trigger has been set, it should be something from 
    	 * a Contours to start with.  Load it's attributes to the Contours attr Dialog.
    	 * If not. we will start with a new Contours.
    	 */   	
      	Object de = event.getTrigger();
	        	
        if ( de instanceof Contours ) {
		    elem = (Contours)de;
		    addContourLine = true;
     	}
        else {
            elem = null; 	
        }
		              
     	if (attrDlg instanceof ContoursAttrDlg ){
     		((ContoursAttrDlg)attrDlg).disableActionButtons();
    	}
	                
   	    return;
        
    }

    /**
     * Returns the current mouse handler.
     * @return
     */
    public IInputHandler getMouseHandler() {	
    
        if ( this.mouseHandler == null ) {
        	
        	this.mouseHandler = new PgenContoursHandler();
        	
        }
        
        return this.mouseHandler;
    }
    
    /**
     * Implements input handler for mouse events.
     */       
    public class PgenContoursHandler extends InputHandlerDefaultImpl {
    	
        /*
         * (non-Javadoc)
         * 
         * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseDown(int,
         *      int, int)
         */
        @Override	
        public boolean handleMouseDown(int anX, int aY, int button) {           
        	
        	//  Check if mouse is in geographic extent
        	Coordinate loc = mapEditor.translateClick(anX, aY);
        	if ( loc == null ) return false;
        	
        	//Drawing Min/Max symbol
        	if ( attrDlg != null && ((ContoursAttrDlg)attrDlg).drawSymbol() ) {
            	
        		if ( button == 1 ) {        		
                    drawContourMinmax( loc );
        		}
                else if ( button == 3 ) {                    
               	
                	setDrawingMode();  
                    points.clear();
                    if ( attrDlg != null ) {
                        ((ContoursAttrDlg)attrDlg).setDrawingLine();
                    }
                    drawingLayer.removeGhostLine();
                    
                	return true;           	
                }
                else {            	
                   	return false;              	
                }                   
        	}
        	
        	//Drawing Circle
        	if ( attrDlg != null && ((ContoursAttrDlg)attrDlg).drawCircle() ) {
                          	
        		if ( button == 1 ) {        		
            		
        			if ( points.size() == 0 ) {
            			
                        points.add( 0, loc );   
                        
           		    }
            		else {
            			
            			if ( points.size() > 1 ) points.remove( 1 );
            			
            			points.add( 1, loc );           		       
            			drawContourCircle();
            		}
          		
                    return true;

        		}
                else if ( button == 3 ) {                    
               	
                	setDrawingMode();  
                    points.clear();
                    if ( attrDlg != null ) {
                        ((ContoursAttrDlg)attrDlg).setDrawingLine();
                    }
                    drawingLayer.removeGhostLine();
                    
                	return true;           	
                }
                else {            	
                   	return false;              	
                }  
        		
        	}

        	
        	// Drawing line
            if ( button == 1 ) {        		

            	if ( attrDlg != null && !((ContoursAttrDlg)attrDlg).drawSymbol() &&
            			!((ContoursAttrDlg)attrDlg).drawCircle() ) {

                    points.add( loc );  
            	}
            	
                return true;               
            }
            else if ( button == 3 ) {
             	
                setDrawingMode(); 
              	drawContours(); 
             	
            	return true;           	
            }
        	else if ( button == 2 ) {       		
        		return true;       		
        	}
            else {            	
               	return false;              	
            }
        	
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.raytheon.viz.ui.input.IInputHandler#handleMouseMove(int,
         *      int)
         */
        @Override
        public boolean handleMouseMove(int x, int y) {
        
        	//  Check if mouse is in geographic extent
        	Coordinate loc = mapEditor.translateClick(x, y);
        	if ( loc == null ) return false;
        	
        	// Draw a ghost contour min/max
        	if ( attrDlg != null && ((ContoursAttrDlg)attrDlg).drawSymbol() ) {

        		ContoursAttrDlg dlg = (ContoursAttrDlg)attrDlg;
        		ContourMinmax ghost = null;            	
                ghost = new ContourMinmax( loc, dlg.getActiveSymbolClass(),dlg.getActiveSymbolObjType(),
                		    new String[] {dlg.getLabel()} );
                
         	    IAttribute mmTemp = ((ContoursAttrDlg)attrDlg).getMinmaxTemplate();

        	    if ( mmTemp != null ) {    	        	
        	    	Symbol oneSymb =  (Symbol)(ghost.getSymbol());
 	        	    oneSymb.update( mmTemp );
    	        }
        	       	        
        	    IAttribute lblTemp = ((ContoursAttrDlg)attrDlg).getLabelTemplate();
        	    if ( lblTemp != null ) {
     	            Text lbl = ghost.getLabel();
     	            String[] oldText = lbl.getText();
     	            boolean hide = lbl.getHide();
     	            boolean auto = lbl.getAuto();
     	            lbl.update( lblTemp );
     	            lbl.setText( oldText );
     	            lbl.setHide( hide );
     	            lbl.setAuto( auto );
        	    }

                drawingLayer.setGhostLine( ghost );      
              	mapEditor.refresh();
			                            	
            	return false;

        	}
        	
        	// Draw a ghost contour circle
        	if ( attrDlg != null && ((ContoursAttrDlg)attrDlg).drawCircle() ) {
                
        		if ( points != null && points.size() >= 1 ) {

        			ContourCircle ghost = new ContourCircle( points.get( 0 ), loc,
                            new String[]{ ((ContoursAttrDlg)attrDlg).getLabel() } ,
                            ((ContoursAttrDlg)attrDlg).hideCircleLabel() );       	     
      
	                IAttribute lineTemp = ((ContoursAttrDlg)attrDlg).getLineTemplate();
                    if ( lineTemp != null ) {    	        	
                        ghost.getCircle().setColors( lineTemp.getColors() );
                    }
     	        
                    IAttribute lblTemp = ((ContoursAttrDlg)attrDlg).getLabelTemplate();
                    if ( lblTemp != null ) {
                        Text lbl = ghost.getLabel();  	                    
   	        	        String[] oldText = lbl.getText();
   	        	        boolean hide = lbl.getHide();
   	        	        boolean auto = lbl.getAuto();
    	        	    lbl.update( lblTemp );
   	        	        lbl.setText( oldText );
   	        	        lbl.setHide( hide );
   	        	        lbl.setAuto( auto );
                    }

                
                    drawingLayer.setGhostLine( ghost );      
              	    mapEditor.refresh();
        		}
			                            	
            	return false;

        	}
        	
        	// Draw a ghost ContourLine            
        	if ( points != null && points.size() >= 1) {
                
        		ArrayList<Coordinate> ghostPts = new ArrayList<Coordinate>(points);
                ghostPts.add(loc);
       	
        	    ContourLine cline = new ContourLine( ghostPts, ((ILine)attrDlg).isClosedLine(), 
    			        new String[]{ ((ContoursAttrDlg)attrDlg).getLabel() },
        	    	   ((ContoursAttrDlg)attrDlg).getNumOfLabels() ); 
       	                	    
        	    IAttribute lineTemp = ((ContoursAttrDlg)attrDlg).getLineTemplate();

        	    if ( lineTemp != null ) {    	        	
 	        	    Line oneLine =  cline.getLine();
 	                Boolean isClosed = oneLine.isClosedLine();
 	        	    oneLine.update( lineTemp );
 	        	    oneLine.setClosed( isClosed );	
    	        }
        	    
        	    String  lblstr = ((ContoursAttrDlg)attrDlg).getLabel();
        	    if ( lblstr!= null && lblstr.contains( "9999" ) ) {
        	    	cline.getLine().setSmoothFactor(0);
        	    }
   	        
        	    IAttribute lblTemp = ((ContoursAttrDlg)attrDlg).getLabelTemplate();
        	    if ( lblTemp != null ) {
        	        for ( Text lbl : cline.getLabels() ) {
   	        	        String[] oldText = lbl.getText();
   	        	        boolean hide = lbl.getHide();
   	        	        boolean auto = lbl.getAuto();
    	        	    lbl.update( lblTemp );
   	        	        lbl.setText( oldText );
   	        	        lbl.setHide( hide );
   	        	        lbl.setAuto( auto );
    	            }
        	    }
    	        
        	    Contours el = (Contours)(def.create( DrawableType.CONTOURS, null,            		
 				       "MET", "Contours", points, drawingLayer.getActiveLayer() ) );
			
   			    cline.setParent( el );
       		    cline.getLine().setPgenType ( ((ContoursAttrDlg)attrDlg).getContourLineType() );
   			
			    el.update( (ContoursAttrDlg)attrDlg );
			    el.add( cline );
            	
			    drawingLayer.setGhostLine( el );
            	mapEditor.refresh();
			
        	}

        	return false;
        	
        }
        
        /*
         * create a Contours and add to the Pgen Resource.
         */
        private void drawContours() {
        	
        	if ( points.size() > 1 ) {
        	     
        		 ContourLine cline = new ContourLine( points, ((ILine)attrDlg).isClosedLine(), 
		                                 new String[]{ ((ContoursAttrDlg)attrDlg).getLabel() }, 
		                                 ((ContoursAttrDlg)attrDlg).getNumOfLabels() ); 
        		 cline.getLine().setPgenType ( ((ContoursAttrDlg)attrDlg).getContourLineType() );
        		 
        	     IAttribute lineTemp = ((ContoursAttrDlg)attrDlg).getLineTemplate();
        	     if ( lineTemp != null ) {    	        	
     	        	 Line oneLine =  cline.getLine();
     	             Boolean isClosed = oneLine.isClosedLine();
     	        	 oneLine.update( lineTemp );
     	        	 oneLine.setClosed( isClosed );	
        	     }
        	     
         	     String  lblstr = ((ContoursAttrDlg)attrDlg).getLabel();
        	     if ( lblstr!= null && lblstr.contains( "9999" ) ) {
        	    	 cline.getLine().setSmoothFactor(0);
        	     }
       		     
        	     IAttribute lblTemp = ((ContoursAttrDlg)attrDlg).getLabelTemplate();
        	     if ( lblTemp != null ) {
        	         for ( Text lbl : cline.getLabels() ) {
        	        	 String[] oldText = lbl.getText();
        	        	 boolean hide = lbl.getHide();
        	        	 boolean auto = lbl.getAuto();
        	        	 lbl.update( lblTemp );
        	        	 lbl.setText( oldText );
        	        	 lbl.setHide( hide );
        	        	 lbl.setAuto( auto );
    	             }
        	     }
        		 
        	     
         		 if ( elem == null ) {
       	     
			    	/*
			    	 * create a new element with attributes from the Attr dialog, and add
			     	* it to the PGEN Resource
			     	*/
   			    	elem = (Contours)(def.create( DrawableType.CONTOURS, null,            		
 				           "MET", "Contours", points, drawingLayer.getActiveLayer() ) );
			
   			    	cline.setParent( elem );
   			    	elem.update( (ContoursAttrDlg)attrDlg );
   			    	elem.add( cline );
        	              			
   			    	drawingLayer.addElement( elem );
   			    	   			
   				}
		    	else {
			
		        	/*
			     	* Make a copy of the existing element; update its attributes from
			     	* those in the Attr Dialog;  replace the existing element with
			     	*  the new one in the pgen resource - (This allows Undo/Redo)
			     	*/            			  	     
		        	Contours newElem = (Contours)elem.copy();   
           	          			
		        	cline.setParent( newElem );
			
		        	newElem.update( (ContoursAttrDlg)attrDlg );
			
		        	newElem.add( cline );
	        	    
		        	drawingLayer.replaceElement( elem, newElem );
		            elem = newElem;

		    	}
        		 
         		( (ContoursAttrDlg)attrDlg ).setCurrentContours( elem );
        				
		    }
        	
        	// Always clear the points for the next drawing.
        	points.clear();
           
        	// Update the display.       	
    		drawingLayer.removeGhostLine();                   
	        mapEditor.refresh();

        }
        
        /*
         * create a Contours and add to the Pgen Resource.
         */
        private void drawContourMinmax( Coordinate loc ) {
        	
        	if ( loc != null ) {
        	     
        		 String cls = ((ContoursAttrDlg)attrDlg).getActiveSymbolClass();
           		 String type = ((ContoursAttrDlg)attrDlg).getActiveSymbolObjType();      	        		 
        		 ContourMinmax cmm = new ContourMinmax( loc, cls, type,
		                                 new String[]{ ((ContoursAttrDlg)attrDlg).getLabel() } ); 
         	             	     
         	    IAttribute mmTemp = ((ContoursAttrDlg)attrDlg).getMinmaxTemplate();

        	    if ( mmTemp != null ) {    	        	
                    Symbol oneSymb =  (Symbol)(cmm.getSymbol());
 	        	    oneSymb.update( mmTemp );
    	        }
        	       	        
        	    IAttribute lblTemp = ((ContoursAttrDlg)attrDlg).getLabelTemplate();
        	    if ( lblTemp != null ) {
     	            Text lbl = cmm.getLabel();
     	            String[] oldText = lbl.getText();
     	            boolean hide = lbl.getHide();
     	            boolean auto = lbl.getAuto();
     	            lbl.update( lblTemp );
     	            lbl.setText( oldText );
     	            lbl.setHide( hide );
     	            lbl.setAuto( auto );
        	    }
   
        	             	     
         		 if ( elem == null ) {
			    	/*
			    	 * create a new element with attributes from the Attr dialog, and add
			     	* it to the PGEN Resource
			     	*/
   			    	elem = (Contours)(def.create( DrawableType.CONTOURS, null,            		
 				           "MET", "Contours", points, drawingLayer.getActiveLayer() ) );
			
   			    	cmm.setParent( elem );
   			    	elem.update( (ContoursAttrDlg)attrDlg );
   			    	elem.add( cmm );
        	              			
   			    	drawingLayer.addElement( elem );
   			    	   			
   				}
		    	else {
			
		        	/*
			     	* Make a copy of the existing element; update its attributes from
			     	* those in the Attr Dialog;  replace the existing element with
			     	*  the new one in the pgen resource - (This allows Undo/Redo)
			     	*/            			
		        	Contours newElem = (Contours)elem.copy();
           	          			
		        	cmm.setParent( newElem );
			
		        	newElem.update( (ContoursAttrDlg)attrDlg );
			
		        	newElem.add( cmm );
    		                   	
		        	drawingLayer.replaceElement( elem, newElem );
    	                			
		            elem = newElem;
		        
		    	}
         		 
         		( (ContoursAttrDlg)attrDlg ).setCurrentContours( elem );
        				
		    }
        	
        	// Always clear the points for the next drawing.
        	points.clear();
           
        	// Update the display.       	
    		drawingLayer.removeGhostLine();                   
	        mapEditor.refresh();

        }
        
        /*
         * Set drawing mode for adding a contour line or a new Contours.
         */
        private void setDrawingMode() {
        	
        	if ( points.size() == 0 ) {
        	    if ( elem == null ) {

       	           //quit Contours drawing
        	        if ( attrDlg != null ) {
        	        	attrDlg.close();         	        	
        	        }

        	        attrDlg = null; 
        	        
       	            addContourLine = false;
    		    
        	        PgenUtil.setSelectingMode();
        	    }
        	    else {
                    
                    // start a new Contours element - new points will be drawn
        	        // as new ContourLine in a new Contours element.
       	            if ( !addContourLine ) {
      	        	    elem = null;
        	        }
        	        else { //back to selecting
        	            PgenUtil.setSelectingMode();
       	            }
        	    } 
        	    
        	}

        }
        
        /*
         * Add a circle to Contours.
         */
        private void drawContourCircle() {
        	
        	if ( points != null && points.size() > 1 ) {
        	     
       		    ContourCircle cmm = new ContourCircle( points.get( 0 ), points.get( 1 ),
		                                 new String[]{ ((ContoursAttrDlg)attrDlg).getLabel() } ,
		                                 ((ContoursAttrDlg)attrDlg).hideCircleLabel() );       	     
       	        
       		    IAttribute lineTemp = ((ContoursAttrDlg)attrDlg).getLineTemplate();
    	        if ( lineTemp != null ) {    	        	
                    cmm.getCircle().setColors( lineTemp.getColors() );
    	        }
       	       	        
        	    IAttribute lblTemp = ((ContoursAttrDlg)attrDlg).getLabelTemplate();
        	    if ( lblTemp != null ) {
        	        Text lbl = cmm.getLabel();
	        	        String[] oldText = lbl.getText();
   	        	        boolean hide = lbl.getHide();
   	        	        boolean auto = lbl.getAuto();
    	        	    lbl.update( lblTemp );
   	        	        lbl.setText( oldText );
   	        	        lbl.setHide( hide );
   	        	        lbl.setAuto( auto );
        	    }

        	             	     
         		 if ( elem == null ) {
			    	/*
			    	 * create a new element with attributes from the Attr dialog, and add
			     	* it to the PGEN Resource
			     	*/
   			    	elem = (Contours)(def.create( DrawableType.CONTOURS, null,            		
 				           "MET", "Contours", points, drawingLayer.getActiveLayer() ) );
			
   			    	cmm.setParent( elem );
   			    	elem.update( (ContoursAttrDlg)attrDlg );
   			    	elem.add( cmm );
        	              			
   			    	drawingLayer.addElement( elem );
   			    	   			
   				}
		    	else {
			
		        	/*
			     	* Make a copy of the existing element; update its attributes from
			     	* those in the Attr Dialog;  replace the existing element with
			     	*  the new one in the pgen resource - (This allows Undo/Redo)
			     	*/            			
		        	Contours newElem = (Contours)elem.copy();
           	          			
		        	cmm.setParent( newElem );
			
		        	newElem.update( (ContoursAttrDlg)attrDlg );
			
		        	newElem.add( cmm );
    		                   	
		        	drawingLayer.replaceElement( elem, newElem );
    	                			
		            elem = newElem;
		        
		    	}
         		 
         		( (ContoursAttrDlg)attrDlg ).setCurrentContours( elem );
        				
		    }
        	
        	// Always clear the points for the next drawing.
        	points.clear();
           
        	// Update the display.       	
    		drawingLayer.removeGhostLine();                   
	        mapEditor.refresh();

        }


    }

}
