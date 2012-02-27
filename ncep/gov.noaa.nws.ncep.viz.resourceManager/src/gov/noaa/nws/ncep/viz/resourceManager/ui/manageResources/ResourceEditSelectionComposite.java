package gov.noaa.nws.ncep.viz.resourceManager.ui.manageResources;

import gov.noaa.nws.ncep.viz.resources.manager.AttrSetGroup;
import gov.noaa.nws.ncep.viz.resources.manager.AttributeSet;
import gov.noaa.nws.ncep.viz.resources.manager.ResourceDefinition;
import gov.noaa.nws.ncep.viz.resources.manager.ResourceDefnsMngr;
import gov.noaa.nws.ncep.viz.resources.manager.ResourceName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.viz.core.exception.VizException;


/**
 * .
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date       	Ticket#		Engineer	Description
 * ------------	----------	-----------	--------------------------
 * 07/03/10      #273        Greg Hull   
 * 03/01/11      #408        Greg Hull   remove Forecast/Observed
 * 07/25/11      #450        Greg Hull   use NcPathManager for Localization
 *
 * </pre>
 * 
 * @author ghull
 * @version 1
 */
public class ResourceEditSelectionComposite extends Composite {
	
	private ResourceDefnsMngr rscDefnsMngr;
	
//	private Button fcstRscCatBtn;
//	private Button obsAnlRscCatBtn;
//	private Boolean fcstCatSelected = false;
	
	private ResourceName seldResourceName = null;
	
	private String prevSeldCat = "";
	
	// a map to store the previous selections for each category.
	private static HashMap<String,ResourceName> prevCatSeldRscNames;
	
    private Composite sel_rsc_comp = null;

    private Label rscTypeLbl = null;
    private Label rscTypeGroupLbl = null;

    private ListViewer rscCatLViewer = null;
    private ListViewer rscTypeLViewer = null;
    private ListViewer rscGroupLViewer = null;
    private ListViewer rscAttrSetLViewer = null;
    
    private Button copyRscTypeBtn = null;
    private Button editRscTypeBtn = null;
    private Button removeRscTypeBtn = null;    // TODO : not implemented
    
    private Button copyRscGroupBtn = null;
    private Button editRscGroupBtn = null;
    private Button removeRscGroupBtn = null;
    
    private Button copyRscAttrSetBtn = null;
    private Button editRscAttrSetBtn = null;
    private Button removeRscAttrSetBtn = null;

    
    private EditResourceAction activeAction = EditResourceAction.NULL_ACTION;
	
    private final static int rscListViewerHeight = 140;
	
    
    enum EditResourceAction {
    	NULL_ACTION,
    	COPY_RESOURCE_TYPE, EDIT_RESOURCE_TYPE,    REMOVE_RESOURCE_TYPE,
    	COPY_RESOURCE_GROUP,  EDIT_RESOURCE_GROUP,   REMOVE_RESOURCE_GROUP,
    	COPY_RESOURCE_ATTR_SET, EDIT_RESOURCE_ATTR_SET, REMOVE_RESOURCE_ATTR_SET
    }
        
    private HashMap<EditResourceAction, Button> editButtonMap = new HashMap<EditResourceAction,Button>();
    
    public interface IEditResourceListener {
    	public void editResourceAction( ResourceName rscName, EditResourceAction action );
    }
    
    private IEditResourceListener rscActionListener;

    SelectionAdapter editActionBtnSelectionListener = new SelectionAdapter() {
		@Override
    	public void widgetSelected( SelectionEvent ev ) {
			EditResourceAction seldAction = (EditResourceAction)ev.widget.getData();
			
			// if the use clicks the same/active button then make sure the toggle is 
			// selected and leave is as is. 
			if( seldAction == activeAction ) {
				((Button)ev.widget).setSelection( true );
				//	return;
			}
			
			if( activeAction != EditResourceAction.NULL_ACTION ) {
				editButtonMap.get( activeAction ).setSelection( false );
			}

			activeAction = (EditResourceAction)ev.widget.getData();
			
			if( rscActionListener != null ) {
				rscActionListener.editResourceAction( seldResourceName, activeAction );
			}
		}    	
    };

	
    public ResourceEditSelectionComposite( Composite parent, IEditResourceListener editActionListener )   throws VizException {
        super(parent, SWT.SHADOW_NONE );
        
        rscDefnsMngr = ResourceDefnsMngr.getInstance();
        
        rscActionListener = editActionListener;
        
        seldResourceName = new ResourceName();
        
        if( prevCatSeldRscNames == null ) {
            prevCatSeldRscNames = new HashMap<String,ResourceName>();        	        
        }
        
    	sel_rsc_comp = this;
    	
		FormData fd = new FormData();
    	fd.top = new FormAttachment( 0, 0 );
    	fd.left = new FormAttachment( 0, 0 );
    	fd.right = new FormAttachment( 100, 0 );
    	fd.bottom = new FormAttachment( 100, 0 );
    	sel_rsc_comp.setLayoutData(fd);

    	sel_rsc_comp.setLayout( new FormLayout() );
                    	   		                
        createSelectResourceGroup();

        // set up the content providers for the ListViewers
        setContentProviders();
        addSelectionListeners();

        initWidgets();        
    }
    
    // create all the widgets in the Resource Selection (top) section of the sashForm.  
    // 
    private void createSelectResourceGroup() {
    	
    	rscCatLViewer = new ListViewer( sel_rsc_comp, 
    			             SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
    	FormData fd = new FormData();//100, rscListViewerHeight);
    	fd.height = rscListViewerHeight;
    	fd.top = new FormAttachment( 0, 35 );
    	fd.left = new FormAttachment( 0, 10 );
    	fd.bottom = new FormAttachment( 100, -110 );
    	fd.right = new FormAttachment( 25, -3 );
    	rscCatLViewer.getList().setLayoutData( fd );

    	Label rscCatLbl = new Label(sel_rsc_comp, SWT.NONE);
    	rscCatLbl.setText("Category");
    	fd = new FormData();
    	fd.left = new FormAttachment( rscCatLViewer.getList(), 0, SWT.LEFT );
    	fd.bottom = new FormAttachment( rscCatLViewer.getList(), -5, SWT.TOP );
    	rscCatLbl.setLayoutData( fd );

        
    	// first create the lists and then attach the label to the top of them
        rscTypeLViewer = new ListViewer( sel_rsc_comp, 
        		                SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
    	fd = new FormData();//150, rscListViewerHeight);
    	fd.top = new FormAttachment( rscCatLViewer.getList(), 0, SWT.TOP );
    	fd.left = new FormAttachment( 25, 3 );//rscCatLViewer.getList(), 10, SWT.RIGHT );
    	fd.bottom = new FormAttachment( rscCatLViewer.getList(), 0, SWT.BOTTOM );
    	fd.right = new FormAttachment( 50, -3 );
    	rscTypeLViewer.getList().setLayoutData( fd );

        rscTypeLbl = new Label(sel_rsc_comp, SWT.NONE);
    	rscTypeLbl.setText("Resource Type");
    	fd = new FormData();
    	fd.left = new FormAttachment( rscTypeLViewer.getList(), 0, SWT.LEFT );
    	fd.bottom = new FormAttachment( rscTypeLViewer.getList(), -5, SWT.TOP );
    	
    	rscTypeLbl.setLayoutData( fd );
    	
        copyRscTypeBtn = new Button( sel_rsc_comp, SWT.TOGGLE );
        copyRscTypeBtn.setText("Copy ...");
        fd = new FormData(100,25);
    	fd.top = new FormAttachment( rscTypeLViewer.getList(), 10, SWT.BOTTOM );
    	fd.left = new FormAttachment( rscTypeLViewer.getList(), -50, SWT.CENTER );
    	copyRscTypeBtn.setLayoutData( fd );
        
    	editRscTypeBtn = new Button( sel_rsc_comp, SWT.TOGGLE );
        editRscTypeBtn.setText("Edit ...");
        fd = new FormData(100,25);
        fd.top = new FormAttachment( copyRscTypeBtn, 7, SWT.BOTTOM );
    	fd.left = new FormAttachment( copyRscTypeBtn, -50, SWT.CENTER );
    	editRscTypeBtn.setLayoutData( fd );

    	removeRscTypeBtn = new Button( sel_rsc_comp, SWT.PUSH );
    	removeRscTypeBtn.setText("Remove");
        fd = new FormData(100,25);
        fd.top = new FormAttachment( editRscTypeBtn, 7, SWT.BOTTOM );
    	fd.left = new FormAttachment( editRscTypeBtn, -50, SWT.CENTER );
    	removeRscTypeBtn.setLayoutData( fd );
    	//removeRscTypeBtn.setEnabled( false ); // TODO : not implemented
            	
    	
    	// first create the lists and then attach the label to the top of them
        rscGroupLViewer = new ListViewer( sel_rsc_comp, 
        		                SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
    	fd = new FormData(); //150, rscListViewerHeight);
    	fd.top = new FormAttachment( rscTypeLViewer.getList(), 0, SWT.TOP );
    	fd.left = new FormAttachment( 50, 3 );//rscTypeLViewer.getList(), 10, SWT.RIGHT );
    	fd.bottom = new FormAttachment( rscTypeLViewer.getList(), 0, SWT.BOTTOM );
    	fd.right = new FormAttachment( 75, -3);
    	rscGroupLViewer.getList().setLayoutData( fd );

        rscTypeGroupLbl = new Label(sel_rsc_comp, SWT.NONE);
        rscTypeGroupLbl.setText("Group"); // changed later depending on type
    	fd = new FormData();
    	fd.left = new FormAttachment( rscGroupLViewer.getList(), 0, SWT.LEFT );
    	fd.bottom = new FormAttachment( rscGroupLViewer.getList(), -5, SWT.TOP );
    	fd.right = new FormAttachment( rscGroupLViewer.getList(), 0, SWT.RIGHT );
    	rscTypeGroupLbl.setLayoutData( fd );
       	
    	
        copyRscGroupBtn = new Button( sel_rsc_comp, SWT.TOGGLE );
        copyRscGroupBtn.setText("Copy ...");
        fd = new FormData(100,25);
    	fd.top = new FormAttachment( rscGroupLViewer.getList(), 7, SWT.BOTTOM );
    	fd.left = new FormAttachment( rscGroupLViewer.getList(), -50, SWT.CENTER );
    	copyRscGroupBtn.setLayoutData( fd );
        
    	editRscGroupBtn = new Button( sel_rsc_comp, SWT.TOGGLE );
    	editRscGroupBtn.setText("Edit ...");
        fd = new FormData(100,25);
//    	fd.top = new FormAttachment( rscGroupLViewer.getList(), 7, SWT.BOTTOM );
//    	fd.left = new FormAttachment( rscGroupLViewer.getList(), -50, SWT.CENTER );
        fd.top = new FormAttachment( copyRscGroupBtn, 7, SWT.BOTTOM );
    	fd.left = new FormAttachment( copyRscGroupBtn, -50, SWT.CENTER );
    	editRscGroupBtn.setLayoutData( fd );

    	removeRscGroupBtn = new Button( sel_rsc_comp, SWT.PUSH );
    	removeRscGroupBtn.setText(" Remove ");
        fd = new FormData(100,25);
        fd.top = new FormAttachment( editRscGroupBtn, 7, SWT.BOTTOM );
    	fd.left = new FormAttachment( editRscGroupBtn, -50, SWT.CENTER );
    	removeRscGroupBtn.setLayoutData( fd );
        

    	
        rscAttrSetLViewer = new ListViewer( sel_rsc_comp, 
                SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL );
    	fd = new FormData();//150, rscListViewerHeight);
        fd.top = new FormAttachment( rscGroupLViewer.getList(), 0, SWT.TOP );
        fd.left = new FormAttachment( 75, 3 ); //rscGroupLViewer.getList(), 7, SWT.RIGHT );
        fd.right = new FormAttachment( 100, -10 );
        fd.bottom = new FormAttachment( rscGroupLViewer.getList(), 0, SWT.BOTTOM );
        rscAttrSetLViewer.getList().setLayoutData( fd );

        Label rscAttrsLbl = new Label(sel_rsc_comp, SWT.NONE);
        rscAttrsLbl.setText("Attributes");
        fd = new FormData();
        fd.left = new FormAttachment( rscAttrSetLViewer.getList(), 0, SWT.LEFT );
        fd.bottom = new FormAttachment( rscAttrSetLViewer.getList(), -5, SWT.TOP );
        rscAttrsLbl.setLayoutData( fd );
        
        copyRscAttrSetBtn = new Button( sel_rsc_comp, SWT.TOGGLE );
        copyRscAttrSetBtn.setText("Copy ...");
        fd = new FormData(100,25);
    	fd.top = new FormAttachment( rscAttrSetLViewer.getList(), 7, SWT.BOTTOM );
    	fd.left = new FormAttachment( rscAttrSetLViewer.getList(), -50, SWT.CENTER );
    	copyRscAttrSetBtn.setLayoutData( fd );
        
    	editRscAttrSetBtn = new Button( sel_rsc_comp, SWT.TOGGLE );
    	editRscAttrSetBtn.setText("Edit ...");
        fd = new FormData(100,25);
//    	fd.top = new FormAttachment( rscAttrSetLViewer.getList(), 7, SWT.BOTTOM );
//    	fd.left = new FormAttachment( rscAttrSetLViewer.getList(), -50, SWT.CENTER );
        fd.top = new FormAttachment( copyRscAttrSetBtn, 7, SWT.BOTTOM );
    	fd.left = new FormAttachment( copyRscAttrSetBtn, -50, SWT.CENTER );
    	editRscAttrSetBtn.setLayoutData( fd );

    	removeRscAttrSetBtn = new Button( sel_rsc_comp, SWT.PUSH );
    	removeRscAttrSetBtn.setText(" Remove ");
        fd = new FormData(100,25);
        fd.top = new FormAttachment( editRscAttrSetBtn, 7, SWT.BOTTOM );
    	fd.left = new FormAttachment( editRscAttrSetBtn, -50, SWT.CENTER );
    	removeRscAttrSetBtn.setLayoutData( fd );      
    }

    private void setContentProviders() {
    	
    	// input is the rscDefnsMngr and output is a list of categories based
    	// on the forecast flag
   		rscCatLViewer.setContentProvider( new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				
				return rscDefnsMngr.getResourceCategories( );
			}

			@Override
			public void dispose() { }

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }    			
   		});
   		
   		//rscCatLViewer.setLabelProvider( NmapCommon.createFileLabelProvider() );

    	rscTypeLViewer.setContentProvider( new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				
				if( seldResourceName.getRscCategory().isEmpty() ) {
					rscTypeLbl.setText("");
					return new String[]{};
				}
				else if( seldResourceName.getRscCategory().equals("PGEN" ) ) {
					rscTypeLbl.setText("PGEN");
				}
				else {
					rscTypeLbl.setText(seldResourceName.getRscCategory()+" Resources");
				}
				List<String> rscTypes = rscDefnsMngr.getResourceTypesForCategory( 
							seldResourceName.getRscCategory(), null, false );
					return rscTypes.toArray();				
			}

			@Override
			public void dispose() { }

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }    			
   		});
    	
    	rscGroupLViewer.setContentProvider(  new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				//String rscType = (String)inputElement;
				String rscType = seldResourceName.getRscType();
				
				if( !rscType.isEmpty() ) {
					
					// if this resource uses attrSetGroups then get get the list of 
					// groups. (PGEN uses groups but we will list the subTypes (products) and not the group)
					if( rscDefnsMngr.doesResourceUseAttrSetGroups( rscType ) ) {

						// for PGEN there is only 1 'default' attrSetGroup 
						if( seldResourceName.isPgenResource() ) {
							rscTypeGroupLbl.setText("PGEN Attribute Group");
							return new String[]{"PGEN"};
						}

						List<String> rscAttrSetsList = rscDefnsMngr.getAttrSetGroupNamesForResource( rscType );

						if( rscAttrSetsList != null &&
								!rscAttrSetsList.isEmpty() ) {
							if( rscType.length() < 8 ) {
								rscTypeGroupLbl.setText( rscType+" Attribute Groups " );
							}
							else { 
								rscTypeGroupLbl.setText( rscType+" Attr Groups " );
							}

							return rscAttrSetsList.toArray();
						}
					}
					else {						
						String[] rscGroups = rscDefnsMngr.getResourceSubTypes( rscType );
						
						rscTypeGroupLbl.setText( rscType+" Sub-Types");
	
						if( rscGroups != null && rscGroups.length != 0 ) {
							return rscGroups;//.toArray();
						}
					}
				}
				else {
					rscTypeGroupLbl.setText("");
				}				

				return new String[]{};
			}

			@Override
			public void dispose() { }

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }    			
   		});    

//    	rscGroupLViewer.setLabelProvider( NmapCommon.createFileLabelProvider() );
    	
    	rscAttrSetLViewer.setContentProvider( new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				
				// if an attrSetGroup is selected, return the attrSets in the group
				if( !seldResourceName.getRscType().isEmpty() ) {
					return rscDefnsMngr.getAttrSetsForResource( seldResourceName, false );

//					if( attrSets != null && !attrSets.isEmpty() ) {
//						return attrSets.toArray();
//					}
				}
				return new String[]{};
			}
			@Override
			public void dispose() { }

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { }    			
    	});
    			
        rscAttrSetLViewer.setLabelProvider( new LabelProvider() {
	    	public String getText( Object element ) {
	    		String attrSetName = (String)element;
	    		if( attrSetName.endsWith(".attr") ) {
	    			return attrSetName.substring(0, attrSetName.length()-5);
	    		}
	    		else {
	    			return attrSetName;
	    		}
	    	}
        });
   	}
   	

    // add all of the listeners for widgets on this dialog
    private void addSelectionListeners() {

        copyRscTypeBtn.setData( EditResourceAction.COPY_RESOURCE_TYPE );
    	copyRscTypeBtn.addSelectionListener( editActionBtnSelectionListener );
    	editButtonMap.put( EditResourceAction.COPY_RESOURCE_TYPE, copyRscTypeBtn);
    	
    	editRscTypeBtn.setData( EditResourceAction.EDIT_RESOURCE_TYPE );
    	editRscTypeBtn.addSelectionListener( editActionBtnSelectionListener );
    	editButtonMap.put( EditResourceAction.EDIT_RESOURCE_TYPE, editRscTypeBtn);
    	
    	removeRscTypeBtn.setData( EditResourceAction.REMOVE_RESOURCE_TYPE );
    	removeRscTypeBtn.addSelectionListener( editActionBtnSelectionListener );
    	editButtonMap.put( EditResourceAction.REMOVE_RESOURCE_TYPE, removeRscTypeBtn);

        copyRscGroupBtn.setData( EditResourceAction.COPY_RESOURCE_GROUP );
        copyRscGroupBtn.addSelectionListener( editActionBtnSelectionListener );
    	editButtonMap.put( EditResourceAction.COPY_RESOURCE_GROUP, copyRscGroupBtn);

    	editRscGroupBtn.setData( EditResourceAction.EDIT_RESOURCE_GROUP );
    	editRscGroupBtn.addSelectionListener( editActionBtnSelectionListener );
    	editButtonMap.put( EditResourceAction.EDIT_RESOURCE_GROUP, editRscGroupBtn);
    	
    	removeRscGroupBtn.setData( EditResourceAction.REMOVE_RESOURCE_GROUP );
    	removeRscGroupBtn.addSelectionListener( editActionBtnSelectionListener );
    	editButtonMap.put( EditResourceAction.REMOVE_RESOURCE_GROUP, removeRscGroupBtn);

        copyRscAttrSetBtn.setData( EditResourceAction.COPY_RESOURCE_ATTR_SET );
        copyRscAttrSetBtn.addSelectionListener( editActionBtnSelectionListener );
    	editButtonMap.put( EditResourceAction.COPY_RESOURCE_ATTR_SET, copyRscAttrSetBtn);

    	editRscAttrSetBtn.setData( EditResourceAction.EDIT_RESOURCE_ATTR_SET );
    	editRscAttrSetBtn.addSelectionListener( editActionBtnSelectionListener );
    	editButtonMap.put( EditResourceAction.EDIT_RESOURCE_ATTR_SET, editRscAttrSetBtn);
    	
    	removeRscAttrSetBtn.setData( EditResourceAction.REMOVE_RESOURCE_ATTR_SET );
    	removeRscAttrSetBtn.addSelectionListener( editActionBtnSelectionListener );
    	editButtonMap.put( EditResourceAction.REMOVE_RESOURCE_ATTR_SET, removeRscAttrSetBtn);


    	rscCatLViewer.addSelectionChangedListener( new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
            	StructuredSelection seld_elem = (StructuredSelection) event.getSelection();            	
            	String seldCat = (String)seld_elem.getFirstElement();            	
            	
            	// get the previously selected resource for this category

        		seldResourceName = new ResourceName( );
        		seldResourceName.setRscCategory( seldCat );
            	
        		// if a resource was previously selected for this category, select it
        		if( prevCatSeldRscNames.containsKey( seldCat ) ) {
        			seldResourceName = prevCatSeldRscNames.get( seldCat );
        		}

            	updateResourceTypes();
			}
    	});

       	rscTypeLViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
            	StructuredSelection seld_elem = (StructuredSelection) event.getSelection();               
            	
            	seldResourceName.setRscType( (String)seld_elem.getFirstElement() );
            	seldResourceName.setRscGroup( "" );
            	seldResourceName.setRscAttrSetName( "" );

            	updateResourceGroups();
            }
        } );       	      

       	rscGroupLViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
            	StructuredSelection seld_elem = (StructuredSelection) event.getSelection();               
            	seldResourceName.setRscGroup( (String)seld_elem.getFirstElement() );
            	seldResourceName.setRscAttrSetName( "" );

            	updateResourceAttrSets();
            }
        });       	      

       	rscAttrSetLViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
            	StructuredSelection seld_elem = (StructuredSelection) event.getSelection(); 
            	
            	seldResourceName.setRscAttrSetName( (String)seld_elem.getFirstElement() );

            	updateSelectedResource();
            }
        });       	      

       	
//       	// a double click will close the dialog
//       	rscAttrSetLViewer.getList().addListener(SWT.MouseDoubleClick, new Listener() {
//			public void handleEvent(Event event) {
//				selectResource( true );
//			}
//       	});
   	}
   	
    public void updateResourceSelections( ResourceName newSeldRscName ) {
    	seldResourceName = newSeldRscName;
    	
//    	if( fcstCatSelected ) {
//    		prevFcstCatSeldRscNames.put( seldResourceName.getRscCategory(), seldResourceName );
//    	}
//    	else {
//    		prevObsvdCatSeldRscNames.put( seldResourceName.getRscCategory(), seldResourceName );
//    	}
    	
    	updateResourceCategories();
    }
    
   	// set the initial values of the widgets. 
   	//     
   	private void initWidgets() {
 
   		prevSeldCat = seldResourceName.getRscCategory();
			
   		seldResourceName = new ResourceName();

		updateResourceCategories( );
	}
	
	// set the cat list based on the fcst flag and then 
	// use seldResourceName to select the category 
	private void updateResourceCategories( ) {
		
		// update the cat list
		rscCatLViewer.setInput( rscDefnsMngr );
		rscCatLViewer.refresh();
		
		rscCatLViewer.getList().deselectAll();

		// 
		if( !seldResourceName.getRscCategory().isEmpty() ) {
			for( int itmIndx=0 ; 
					 itmIndx < rscCatLViewer.getList().getItemCount() ; itmIndx++ )  {
				
				if( rscCatLViewer.getList().getItem(itmIndx).equals( 
						seldResourceName.getRscCategory() ) ) {
					rscCatLViewer.getList().select( itmIndx );
					break;
				}
			}
			
			if( rscCatLViewer.getList().getSelectionCount() == 0 ) {
				seldResourceName = new ResourceName();
			}
		}

		// if no cat is selected or it is not found for some reason, select the first 
		if( seldResourceName.getRscCategory().isEmpty() && 
			rscCatLViewer.getList().getItemCount() > 0 ) {

			rscCatLViewer.getList().select(0);
			StructuredSelection seld_elem = (StructuredSelection)rscCatLViewer.getSelection();
			
			seldResourceName = new ResourceName( );
			seldResourceName.setRscCategory( (String)seld_elem.getFirstElement() );
		}

		updateResourceTypes();		
	}

	// refresh the types list based on the type in the seldResourceName 
	// use seldResourceName to select the type 
	private void updateResourceTypes() {
		
		rscTypeLViewer.setInput( rscDefnsMngr );
		rscTypeLViewer.refresh();
		
		rscTypeLViewer.getList().deselectAll();

		// 
		if( !seldResourceName.getRscType().isEmpty() ) {
			for( int itmIndx=0 ; 
					 itmIndx < rscTypeLViewer.getList().getItemCount() ; itmIndx++ )  {
				
				if( rscTypeLViewer.getList().getItem(itmIndx).equals( 
						                       seldResourceName.getRscType() ) ) {
					rscTypeLViewer.getList().select( itmIndx );
					break;
				}
			}
			
			if( rscTypeLViewer.getList().getSelectionCount() == 0 ) {
				seldResourceName.setRscType("");
				seldResourceName.setRscGroup("");
				seldResourceName.setRscAttrSetName(""); 
			}
		}

		// if no type is selected or it is not found for some reason, select the first 
		if( seldResourceName.getRscType().isEmpty() &&
					rscTypeLViewer.getList().getItemCount() > 0 ) {

			rscTypeLViewer.getList().select(0);
			StructuredSelection seld_elem = (StructuredSelection)rscTypeLViewer.getSelection();
			
			seldResourceName.setRscType( (String)seld_elem.getFirstElement() );
			seldResourceName.setRscGroup("");
			seldResourceName.setRscAttrSetName(""); 
		}
		
		// enable/disable the Edit/Delete buttons based on whether a type is selected.
		//
		if( seldResourceName.getRscType().isEmpty() ) {
			copyRscTypeBtn.setEnabled( false );
			editRscTypeBtn.setEnabled( false );
			removeRscTypeBtn.setEnabled( false );
		}
		else {
			copyRscTypeBtn.setEnabled( true );
			editRscTypeBtn.setEnabled( true );
			
			removeRscTypeBtn.setEnabled( false );			
		}

		updateResourceGroups();
	}

	private void updateResourceGroups() {
		rscGroupLViewer.setInput( rscDefnsMngr );
		rscGroupLViewer.refresh();
		
		// if there are no groups 
		if( rscGroupLViewer.getList().getItemCount() == 0 ) {
			if( !seldResourceName.getRscGroup().isEmpty() ) {
				// ????
				seldResourceName.setRscGroup("");
				seldResourceName.setRscAttrSetName("");				
			}
		}
		else { // there are items in the groups list
			  // if a group has been selected then select it in the list, otherwise
			 // select the first in the list and update the seldResourceName
			//
			rscGroupLViewer.getList().deselectAll();

			// 
			if( !seldResourceName.getRscGroup().isEmpty() ) {
				for( int itmIndx=0 ; 
						 itmIndx < rscGroupLViewer.getList().getItemCount() ; itmIndx++ )  {

					if( rscGroupLViewer.getList().getItem(itmIndx).equals( 
							seldResourceName.getRscGroup() ) ) {
						rscGroupLViewer.getList().select( itmIndx );
						break;
					}
				}

				if( rscGroupLViewer.getList().getSelectionCount() == 0 ) {
					seldResourceName.setRscGroup("");
					seldResourceName.setRscAttrSetName(""); 
				}
			}

			// if no type is selected or it is not found for some reason, select the first 
			if( seldResourceName.getRscGroup().isEmpty() &&
					rscGroupLViewer.getList().getItemCount() > 0 ) {

				rscGroupLViewer.getList().select(0);
				StructuredSelection seld_elem = (StructuredSelection)rscGroupLViewer.getSelection();

				seldResourceName.setRscGroup( (String)seld_elem.getFirstElement() );
				seldResourceName.setRscAttrSetName(""); 
			}
		}
		
		ResourceDefinition rscDefn = rscDefnsMngr.getResourceDefinition( seldResourceName );
		
		// enable/disable the Edit/Delete buttons based on whether a group is selected and whether
		// 
		// TODO : if there is a USER group superceding a BASE, SITE, or DESK group then
		// change the name of the Remove button to 'Revert'
		
		if( seldResourceName.getRscGroup().isEmpty() ||
			!rscDefn.applyAttrSetGroups() ) {
			
			copyRscGroupBtn.setEnabled( false );
			editRscGroupBtn.setEnabled( false );
			removeRscGroupBtn.setEnabled( false );			
		}
		// Also, PGEN groups are the PGEN files which can't be edited here.
		else if( seldResourceName.getRscCategory().equals("PGEN") ) {
			copyRscGroupBtn.setEnabled( false );
			editRscGroupBtn.setEnabled( false );
			removeRscGroupBtn.setEnabled( false );			
		}
		else {
			copyRscGroupBtn.setEnabled( true );
			editRscGroupBtn.setEnabled( true );
			removeRscTypeBtn.setEnabled( false );

			AttrSetGroup asg = rscDefnsMngr.getAttrSetGroupForResource( seldResourceName );
			if( asg != null ) {
				LocalizationLevel lLvl = asg.getLocalizationFile().getContext().getLocalizationLevel();

				removeRscTypeBtn.setEnabled( (lLvl == LocalizationLevel.USER) );	
			}
		}
		
//		// Can't remove the last group. Must be in the BASE 
//		if( rscGroupLViewer.getList().getItemCount() == 1 ) {
//			removeRscGroupBtn.setEnabled( false );
//		}
		
		rscTypeGroupLbl.setVisible( !seldResourceName.getRscGroup().isEmpty() );

		LocalizationLevel lLvl = rscDefn.getLocalizationFile().getContext().getLocalizationLevel();
		
		removeRscTypeBtn.setEnabled( (lLvl == LocalizationLevel.USER) );	

		updateResourceAttrSets();
	}

	private void updateResourceAttrSets() {
		
		
		// if there is a group set it is from the BASE or SITE level then don't let the user 
		// delete it.
		if( !seldResourceName.getRscGroup().isEmpty() ) {		
			AttrSetGroup asg = rscDefnsMngr.getAttrSetGroupForResource( seldResourceName );

			if( asg == null ) {
				removeRscGroupBtn.setEnabled( false );
			}
			else if( asg.getLocalizationFile().getContext().getLocalizationLevel() == 
												          LocalizationLevel.USER ) {
				removeRscGroupBtn.setEnabled( true );					
			}
			else {
				removeRscGroupBtn.setEnabled( false );
			}
		}
		
		rscAttrSetLViewer.setInput( rscDefnsMngr );
		rscAttrSetLViewer.refresh( true );
		
		rscAttrSetLViewer.getList().deselectAll();

		// 
		if( !seldResourceName.getRscAttrSetName().isEmpty() ) {
			for( int itmIndx=0 ; 
					 itmIndx < rscAttrSetLViewer.getList().getItemCount() ; itmIndx++ )  {
				
				if( rscAttrSetLViewer.getList().getItem(itmIndx).equals( 
						                          seldResourceName.getRscAttrSetName() ) ) {
					rscAttrSetLViewer.getList().select( itmIndx );
					break;
				}
			}
			
			if( rscAttrSetLViewer.getList().getSelectionCount() == 0 ) {
				seldResourceName.setRscAttrSetName(""); 
			}
		}

		// if no attr set is selected or it is not found for some reason, select the first 
		if( seldResourceName.getRscAttrSetName().isEmpty() &&
				rscAttrSetLViewer.getList().getItemCount() > 0 ) {

			rscAttrSetLViewer.getList().select(0);
			StructuredSelection seld_elem = (StructuredSelection)rscAttrSetLViewer.getSelection();
			
			seldResourceName.setRscAttrSetName( (String)seld_elem.getFirstElement() );
		}		
		
		updateSelectedResource( );
	}
   	
   	// when an attrSetName is selected and a valid resource name is ready for selection 
   	// 
	public void updateSelectedResource( ) {
		// enable/disable the Edit/Delete buttons based on whether an attr set is selected.
		//
		if( seldResourceName.getRscAttrSetName().isEmpty() ) {
			copyRscAttrSetBtn.setEnabled( false );
			editRscAttrSetBtn.setEnabled( false );
			removeRscAttrSetBtn.setEnabled( false );
		}
		else {
			copyRscAttrSetBtn.setEnabled( true );
			editRscAttrSetBtn.setEnabled( true );

			// The user can not edit or delete a BASE, SITE or DESK level file.
			AttributeSet selAttrSet = rscDefnsMngr.getAttrSet( seldResourceName );

			if( selAttrSet != null && 
				selAttrSet.getLocalizationLevel() == LocalizationLevel.USER ) {
				removeRscAttrSetBtn.setEnabled( true );
			}
			else {							
				removeRscAttrSetBtn.setEnabled( false );
			}
		}
	}


	public void cancelEditAction( ) {
		if( activeAction != EditResourceAction.NULL_ACTION ) {
			editButtonMap.get( activeAction ).setSelection( false );
			activeAction = EditResourceAction.NULL_ACTION;
		}
	}

    // code for the Listeners for the Add Resource button and the double Click on the list
	public void selectResource( boolean done ) {
	}
	
//	public void addResourceSelectionListener( IResourceSelectedListener lstnr ) {
//		rscSelListeners.add( lstnr );
//	}
	
}