package gov.noaa.nws.ncep.viz.resources.manager;


import gov.noaa.nws.ncep.viz.resources.AbstractNatlCntrsRequestableResourceData;
import gov.noaa.nws.ncep.viz.resources.INatlCntrsResourceData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.raytheon.uf.common.serialization.SerializationUtil;
import com.raytheon.uf.viz.core.VariableSubstitutionUtil;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.drawables.ResourcePair;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.procedures.Bundle;
import com.raytheon.uf.viz.core.rsc.ResourceGroup;
import com.raytheon.uf.viz.core.rsc.ResourceList;

/**
 * Class used by the content providers for Resources and Overlays. This stores the 
 * instantiated Resource Bundle Templates first with the default attributes and 
 * additionally stores later changes to the attributes. 
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date       	Ticket#		Engineer	Description
 * ------------	----------	-----------	--------------------------
 * 12/1/08		#24			Greg Hull		Initial creation
 * 12/15/08     #43     	Greg Hull    	Call NmapCommon methods
 * 06/18/09     #115        Greg Hull       Integrate with ResourceAttrSet   
 * 09/01/09     #148        Greg Hull       set qualifiedRscName in resourceData
 * 10/20/09     #145        Greg Hull       setIsDominant & changes to labels
 * 01/07/10     #217        Greg Hull       simplified. Moved .prm parsing here. 
 * 01/26/10     #226        Greg Hull       instantiate from the resource name 
 * 03/04/10     #226        Greg Hull       special case for PGEN resource 
 * 03/17/10     #259        Greg Hull       add '@' parameter processing 
 * 08/23/10     #273        Greg Hull       isVisible()
 *   
 * </pre>
 * 
 * @author 
 * @version 1
 */

//class to store the instanced Bundle for a resource and its attributes
//
public class ResourceFactory {
		
	public static class ResourceSelection {
		private INatlCntrsResourceData rscData = null; 
		private ResourcePair rscPair = null;

//		// 
//		public ResourceSelectionUnused( String fullRscName ) throws VizException {
//		}
//
		// called when loading an existing RBD into the dialog and we need to get the attribute values
		// from the edited RBD instead of the original attrSet file.
		protected ResourceSelection( ResourcePair rp ) throws VizException {
			rscPair = rp;
			rscData = (INatlCntrsResourceData) rp.getResourceData();
		}

		public void markAsEdited() {		
			if( rscData != null ) {
				rscData.setIsEdited( true );
			}
		}

		public boolean isRequestable( ) {
			if( rscData != null &&
					rscData instanceof AbstractNatlCntrsRequestableResourceData ) {
				return true;
			}
			return false;
		}

		public ResourceName getResourceName() {
			return rscData.getResourceName();
		}
		
		public boolean isVisible() {
			if( rscPair == null || 
				rscPair.getProperties() == null ) { 
				return false;
			}
			
			return rscPair.getProperties().isVisible();
		}
		
		public void setIsVisible( boolean visible ) {
			if( rscPair == null || 
				rscPair.getProperties() == null ) { 
				return;
			}
				
			rscPair.getProperties().setVisible( visible );
		}
		

		// this is called by the LabelProvider for ListViewer that use this class as the ContentProvider.
		public String getRscLabel() {
			if( rscData == null  ) {
				return "???";
			}

			String rsc_label = rscData.getResourceName().toString();

			// TODO : Would it be nice to give an indication that this is the dominant resource???
			//		if( rscData instanceof AbstractNatlCntrsRequestableResourceData &&
			//			((AbstractNatlCntrsRequestableResourceData)rscData).getIsDominant() ) {
			//			rsc_label = rscData.getFullResourceName() + " (D)";
			//		}
			 
			if( rscData.getIsEdited() ) {
				rsc_label = rsc_label + " (E)";
			}
			
			if( !isVisible() ) {
				rsc_label = "(Off) "+rsc_label;
			}

			return rsc_label;
		}

		public ResourcePair getResourcePair() {
			return rscPair;
		}

		public INatlCntrsResourceData getResourceData() {
			return rscData;
		}		
	}

	public static ResourceSelection createResource( ResourcePair rscPair ) throws VizException {		
		return new ResourceSelection( rscPair );
	}

	public static ResourceSelection createResource( ResourceName rscName ) throws VizException {
		
		File bndlFile = ResourceDefnsMngr.getInstance().getRscBundleTemplateFile( rscName.getRscType() );
		HashMap< String, String > rscParams = ResourceDefnsMngr.getInstance().getAllResourceParameters( rscName );		
		
		String bundleStr = null;
		try {
			FileReader fr = new FileReader(bndlFile);
			char[] b = new char[(int) bndlFile.length()];
			fr.read(b);
			fr.close();
			bundleStr = new String(b);

		} catch (Exception e) {
			throw new VizException("Error opening  Resource Template file " + bndlFile, e);
		}
		
		try {
			String substStr = VariableSubstitutionUtil.processVariables(
					                        bundleStr, rscParams );

			ResourceList bndl_rscs = null;

			Object rg =  SerializationUtil.unmarshalFromXml(substStr);

			if( !(rg instanceof ResourceGroup) ) {
				throw new VizException("Resource Bundle template has unexpected class. (not ResourceGroup)");
			}

			ResourceGroup rscGroup = (ResourceGroup)rg;

			bndl_rscs = rscGroup.getResourceList();
				
			if( bndl_rscs.size() != 1 ) {
				System.out.println("Sanity check: ResourceSelectionUnused: should only be one rsc in Bundle file!");
			}
			
			if( bndl_rscs.size() >= 1 ) {
				if( !(bndl_rscs.get(0).getResourceData() instanceof INatlCntrsResourceData) ) {
					System.out.println("Sanity check: Bundle file contains non-NatlCntrs Resource?");
					return null;
				}
				
				ResourcePair rscPair = bndl_rscs.get(0);
				INatlCntrsResourceData rscData = 
					   (INatlCntrsResourceData) rscPair.getResourceData();	
				rscData.setResourceName( rscName );

				ResourceSelection rscSelection = new ResourceSelection( rscPair );
				
				return rscSelection;				
			}
		} catch (Exception e) {
			throw new VizException("Error unmarshalling Resource: "+e.getMessage(), e);
		}
		
		return null;
	}	
}
