package gov.noaa.nws.ncep.ui.nsharp.palette;
/**
 * 
 * gov.noaa.nws.ncep.ui.nsharp.palette.NsharpShowTextDialog
 * 
 * 
 * This code has been developed by the NCEP-SIB for use in the AWIPS2 system.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    	Engineer    Description
 * -------		------- 	-------- 	-----------
 * 03/23/2010	229			Chin Chen	Initial coding
 *
 * </pre>
 * 
 * @author Chin Chen
 * @version 1.0
 */


import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingLayer;

import gov.noaa.nws.ncep.ui.nsharp.NsharpStationInfo;
import gov.noaa.nws.ncep.ui.nsharp.skewt.NsharpSkewTEditor;
import gov.noaa.nws.ncep.ui.nsharp.skewt.rsc.NsharpSkewTResource;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import com.raytheon.uf.viz.core.exception.VizException;

public class NsharpShowTextDialog extends Dialog {
	private static NsharpShowTextDialog INSTANCE = null;
	protected Composite top;
	private Shell shell;
	private Text text=null;
	private Group textGp;
	private Font newFont ;
	private static boolean iAmClosed;
	private static String textToSave="";
	public Text getText() {
		return text;
	}

	protected NsharpShowTextDialog(Shell parentShell) throws VizException {
		super(parentShell);
		this.setShellStyle(SWT.TITLE | SWT.MODELESS | SWT.CLOSE );
		shell = parentShell;
		// TODO Auto-generated constructor stub
	}
	
	private void createShowtextDialogContents(Composite parent){
		textGp = new Group(parent,SWT.SHADOW_OUT);
		textGp.setLayout( new GridLayout() );
        textGp.setText("Sounding Text");
        GridData data = new GridData (SWT.FILL, SWT.FILL, true, true);
        textGp.setLayoutData (data);
        text = new Text(textGp, SWT.V_SCROLL| SWT.H_SCROLL);
        
        GridData data1 = new GridData (SWT.FILL,SWT.FILL, true, true);
        text.setLayoutData (data1);
        text.setEditable(false);
        Font font = text.getFont();
		FontData[] fontData = font.getFontData();
		for (int i = 0; i < fontData.length; i++) {
			fontData[i].setHeight(12);				
			fontData[i].setName("courier");
		}
		newFont = new Font(font.getDevice(), fontData);
		text.setFont(newFont);
		
	}
	
	/*private String createDefaultSaveFileName() {
		StringTokenizer st = new StringTokenizer(text.getText());
		int i =0;
		String fileName;
		if(st.hasMoreTokens()== true){
			fileName = "";
			//text header are the first 2,3,4 tokens. use them as default file name
			while (st.hasMoreTokens()) {
				i++;
				String tok = st.nextToken();
				//System.out.println("tok "+ i + " ="+ tok);
				if(i==1)
					continue;
				if(i > 4){
					break;
				}
				
				if(i ==4) {
					if(tok.length() >= 5)
						tok = " " + tok.substring(0, 5);
					else 
						tok = " " + tok;
				}
				fileName = fileName + tok;
				
			}
			fileName = fileName + ".nsp";
		}
		else
			fileName= "nsharp.nsp";
		
		return fileName;
	}*/
	@Override
	public void createButtonsForButtonBar(Composite parent) {
		// create buttons with "CLOSE" label but with cancel function
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CLOSE_LABEL, false);
		
		// Push buttons for SAVE
		Button saveBtn = createButton(parent, IDialogConstants.CLIENT_ID,
				"SAVE", false);
		saveBtn.addListener( SWT.MouseUp, new Listener() {
			public void handleEvent(Event event) {  
				// Action to save text report 
				NsharpSaveHandle.saveFile(shell);
				/*FileDialog dlg = new FileDialog(shell, SWT.SAVE);
				String fileName = null;

				// The user has finished when one of the
				// following happens:
				// 1) The user dismisses the dialog by pressing Cancel
				// 2) The selected file name does not exist
				// 3) The user agrees to overwrite existing file
				boolean done = false;
				boolean saveFile = false;
				while (!done) {
					// Open the File Dialog
					dlg.setText("Save");
	                String[] filterExt = {"*.nsp"};
	                dlg.setFilterExtensions(filterExt);
	                dlg.setFileName(createDefaultSaveFileName());
					fileName = dlg.open();
					//System.out.println("file name = "+ fileName);
					if (fileName == null) {
						// User has cancelled, so quit and return
						done = true;
					} else {
						// User has selected a file; see if it already exists
						File file = new File(fileName);
						if (file.exists()) {
							// The file already exists; asks for confirmation
							MessageBox mb = new MessageBox(dlg.getParent(), SWT.ICON_WARNING
									| SWT.YES | SWT.NO);

							// We really should read this string from a
							// resource bundle
							mb.setMessage(fileName + " already exists. Do you want to replace it?");

							// If they click Yes, we're done and we drop out. If
							// they click No, we redisplay the File Dialog
							done = mb.open() == SWT.YES;
							if(done == true)
								saveFile = true;
						} else {
							// File does not exist, so drop out
							done = true;
							saveFile = true;
						}
					}
				}
				if(saveFile == true) {
					try{
						// Create file 
						FileWriter fstream = new FileWriter(fileName);
						BufferedWriter out = new BufferedWriter(fstream);
						out.write(textToSave);
						//Close the output stream
						out.close();
					}catch (Exception e){//Catch exception if any
						System.err.println("Error: " + e.getMessage());
					}
				}*/
			}         	            	 	
		} );
	}
	/**
	 * Creates the dialog area
	 */	
	@Override
	public Control createDialogArea(Composite parent) {
		
	        top = (Composite) super.createDialogArea(parent);

	        // Create the main layout for the shell.
	        GridLayout mainLayout = new GridLayout(1, false);
	        mainLayout.marginHeight = 13;
	        mainLayout.marginWidth = 13;
	        top.setLayout(mainLayout);

	        // Initialize all of the menus, controls, and layouts
	        createShowtextDialogContents(top);
	             
	        refreshTextData();
	        return top;
	}   
	
	//@Override
    public int open() {
        //System.out.println("ShowText Dialog opened");
        
        if ( this.getShell() == null ){
			this.create();
		}
   	    this.getShell().setLocation(this.getShell().getParent().getLocation().x+700,
   	    		this.getShell().getParent().getLocation().y+200);
   	    
   	    iAmClosed = false;
   	    return super.open();
    	
    }
	@Override
	public boolean close() {
		
		//System.out.println("ShowText close called");
		iAmClosed = true;
		if(newFont!= null){
			newFont.dispose();
			newFont=null;
		}
		return (super.close());
	}
	public static NsharpShowTextDialog getInstance( Shell parShell){
		
		if ( INSTANCE == null ){
			try {
				INSTANCE = new NsharpShowTextDialog( parShell );
				//System.out.println("new showtext dialog INSTANCE created");
			} catch (VizException e) {
				e.printStackTrace();
			}
			
		}
		else {
			//System.out.println("current showtext dialog INSTANCE returned!");
		}
		
		return INSTANCE;
		
	}
	public static NsharpShowTextDialog getAccess() {
		if(iAmClosed == true)
			return null;
		
		return INSTANCE;
	}
	public static NsharpShowTextDialog getAccess(boolean force) {
		if(force == true)
			return INSTANCE;
		else
			return getAccess();
	}
	
	public void refreshTextData() {
		NsharpSkewTResource rsc = NsharpSkewTEditor.getActiveNsharpEditor().getNsharpSkewTDescriptor().getSkewtResource();
		
		
		if(rsc!=null && rsc.getSoundingLys()!= null && !text.isDisposed() && text!=null){
			String hdr;
			List<NcSoundingLayer> soundLyList = rsc.getSoundingLys();
			hdr = "PRESSURE  HGHT\t   TEMP\t  DWPT    WDIR     WSPD    OMEG\n";
			String latlonstr;
			NsharpStationInfo stnInfo=rsc.getPickedStnInfo();
			if( stnInfo!= null){
				latlonstr = "  LAT=" + stnInfo.getLatitude() + " LON="+ stnInfo.getLongitude();
			} 
			else {
				latlonstr = "  LAT=  LON=  ";
			}
			String textToShow = rsc.getPickedStnInfo().getSndType() +"  "+rsc.getPickedStnInfoStr() + latlonstr+ "\n" + hdr;
			textToSave = rsc.getPickedStnInfo().getSndType() +"  "+rsc.getPickedStnInfoStr() + latlonstr + "\n" + hdr;
			String tempText="", tempSaveText="";
			for (NcSoundingLayer layer: soundLyList){
				tempText = String.format("%7.2f\t%8.2f %7.2f %7.2f   %6.2f  %6.2f  %9.6f\n", layer.getPressure(),
						layer.getGeoHeight(),layer.getTemperature(),layer.getDewpoint(), layer.getWindDirection(),
						layer.getWindSpeed(), layer.getOmega());
				tempSaveText = String.format("%f %f %f  %f  %f  %f  %f\n", layer.getPressure(),
						layer.getGeoHeight(),layer.getTemperature(),layer.getDewpoint(), layer.getWindDirection(),
						layer.getWindSpeed(), layer.getOmega());
				textToShow = textToShow + tempText;
				textToSave = textToSave + tempSaveText;
			}
			
			text.setText(textToShow);
			
			//System.out.println(textToShow);
			textGp.layout();
		}
	}

	
	//Need use asyncExec to handle update text request from other thread (worker thread)
	public void updateTextFromWorkerThread(){  
		try{  
		Display.getDefault().asyncExec(new Runnable(){  
			public void run(){  
				refreshTextData(); 
			}  
		});  
		}catch(SWTException e){  
			System.out.println("updateTextFromWorkerThread: can not run asyncExec()");
		}  
	}
}
