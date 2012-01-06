package gov.noaa.nws.ncep.ui.nsharp.palette;

import gov.noaa.nws.ncep.edex.common.sounding.NcSoundingLayer;
import gov.noaa.nws.ncep.ui.nsharp.NsharpStationInfo;
import gov.noaa.nws.ncep.ui.nsharp.menu.NsharpLoadDialog;
import gov.noaa.nws.ncep.ui.nsharp.skewt.NsharpSkewTEditor;
import gov.noaa.nws.ncep.ui.nsharp.skewt.rsc.NsharpSkewTResource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class NsharpSaveHandle {
	public static void saveFile(Shell shell) {
		FileDialog dlg = new FileDialog(shell, SWT.SAVE);
		String fileName = null;

		// The user has finished when one of the
		// following happens:
		// 1) The user dismisses the dialog by pressing Cancel
		// 2) The selected file name does not exist
		// 3) The user agrees to overwrite existing file
		boolean done = false;
		boolean saveFile = false;
		NsharpSkewTResource rsc = NsharpSkewTEditor.getActiveNsharpEditor().getNsharpSkewTDescriptor().getSkewtResource();
		
		if(rsc != null ){
			while (!done ) {
				// Open the File Dialog
				dlg.setText("Save Text Data");
				String[] filterExt = { "*.nsp", "*","*.txt","*.doc", ".rtf", "*.*"};
				dlg.setFilterExtensions(filterExt);
				if( rsc.getPickedStnInfoStr()!= null && rsc.getPickedStnInfoStr().length() >0){
					StringTokenizer st = new StringTokenizer(rsc.getPickedStnInfoStr());
					int i =0;

					if(st.hasMoreTokens()== true){
						fileName = "";
						while (st.hasMoreTokens()) {
							i++;
							if(i > 3){   					
								break;
							}
							String tok = st.nextToken();
							if(i ==3) {
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
					dlg.setFileName(fileName);
				}
				else
					dlg.setFileName("nsharp.nsp");
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
					String textToSave = new String("");

					if(rsc!=null && rsc.getSoundingLys()!= null){
						List<NcSoundingLayer> soundLyList = rsc.getSoundingLys();
						String latlonstr;
						NsharpStationInfo stnInfo=rsc.getPickedStnInfo();
						if( stnInfo!= null){
							latlonstr = "  LAT=" + stnInfo.getLatitude() + " LON="+ stnInfo.getLongitude();
						} 
						else {
							latlonstr = "  LAT=  LON=  ";
						}
						int loadsoundingType= NsharpLoadDialog.OBSER_SND;
						String loadsoundingTypeStr= "OBS";;
						if(NsharpLoadDialog.getAccess()!= null ){
							loadsoundingType = NsharpLoadDialog.getAccess().getActiveLoadSoundingType();
							switch(loadsoundingType ){
							case NsharpLoadDialog.PFC_SND:
								loadsoundingTypeStr = "PFC";
								break;
							case NsharpLoadDialog.MODEL_SND:
								loadsoundingTypeStr = "MDL";
								break;
							case NsharpLoadDialog.OBSER_SND:
							default:
								loadsoundingTypeStr = "OBS";
								break;
							}
						}
						textToSave = loadsoundingTypeStr+ " "+rsc.getPickedStnInfo().getSndType() +"  "+rsc.getPickedStnInfoStr() + latlonstr 
						+ "\n" + "PRESSURE  HGHT\t   TEMP\t  DWPT    WDIR     WSPD    OMEG\n";
						String tempText="";
						for (NcSoundingLayer layer: soundLyList){
							tempText = String.format("%f  %f  %f  %f  %f  %f  %f\n", layer.getPressure(),
									layer.getGeoHeight(),layer.getTemperature(),layer.getDewpoint(), layer.getWindDirection(),
									layer.getWindSpeed(), layer.getOmega());
							textToSave = textToSave + tempText;
						}
					}
					out.write(textToSave);
					//Close the output stream
					out.close();
				}catch (Exception e){//Catch exception if any
					System.err.println("Error: " + e.getMessage());
				}
			}
		}
	}
}
