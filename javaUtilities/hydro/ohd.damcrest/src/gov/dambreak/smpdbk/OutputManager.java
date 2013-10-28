package gov.dambreak.smpdbk;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import gov.dambreak.util.*;
import gov.dambreak.menu.*;
import gov.damcat.data.*;

/**
 * This class should NOT be regenerated from the VAJ Visual Composition Editor (VCE)
 * since a number of manual changes have been made to the code that bypassed the VCE
 *  
 * Creation date: (7/31/2003 8:35:05 AM)
 * @author: 
 */
public class OutputManager extends JDialog implements ActionListener {
	private AnalysisData currentData;
	private JPanel ivjJDialogContentPane = null;
	private JLabel ivjJLabel1 = null;
	private JPanel ivjpanelButtons = null;
	private JTabbedPane ivjtabbedOutput = null;
	private String strLastOpenPath;
	private String strOpenedFilename;
	private JPanel ivjJPanel = null;
	private JPanel ivjJPanel2 = null;
	private JLabel ivjJLabel2 = null;
	private JPanel ivjtabNoOutput = null;
	private JButton ivjButtonPrint = null;
	private JComboBox ivjcomboScenario = null;
	private JPanel ivjcloseOutput = null;
	private JButton ivjButtonClose = null;
	private JButton ivjButtonViewSave = null;
	private JTable jt = null;
	private JTextPane fullText = null;
	private JTextPane prerunText = null;
	private JTextPane warningText = null;
	private boolean prerunFlag = false;
	private boolean bNoArgs = false;
	private DBAccess dbAccess = null;

	public static boolean bEmptyDataset = false;
	
	IvjEventHandler ivjEventHandler = new IvjEventHandler();
	
	class IvjEventHandler implements java.awt.event.ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if (e.getSource() == OutputManager.this.getButtonPrint())
			connEtoC1(e);
			if (e.getSource() == OutputManager.this.getButtonClose())
			connOutput(e);
			if (e.getSource() == OutputManager.this.getButtonViewSave())
			connViewSave(e);
		};
	};
/**
 * OutputManager constructor comment.
 */
public OutputManager() {
	super();
	initialize();
}
/**
 * Insert the method's description here.
 * Creation date: (4/1/2004 11:13:19 AM)
 * @param _currentData gov.dambreak.util.AnalysisData
 */
public OutputManager(AnalysisData _currentData) 
{
	prerunFlag = true;

	currentData = _currentData;
	
	initialize();

//	show();
    this.setVisible( true );
}
/**
 * OutputManager constructor comment.
 * @param owner java.awt.Dialog
 */
public OutputManager(Frame owner, AnalysisData _currentData) {
	super(owner,true);

	String outOwner = owner.toString();
	if(outOwner.startsWith("gov.damcat.data.Search"))
	{
		prerunFlag = true;
		bNoArgs = true;
	}
	currentData = _currentData;
	
	initialize();

//	show();
    this.setVisible( true );
}
/**
 * This method is called when the user selects a scenario.
 * Creation date: (7/31/2003 11:45:30 AM)
 * @param e java.awt.event.ActionEvent
 */
public void actionPerformed(ActionEvent e) {
	handleScenarioSelection();
}
/**
 * Insert the method's description here.
 * Creation date: (7/31/2003 3:41:10 PM)
 */
private void addRiverProfileCharts(ModelOutput outputFile) {

	try {
		//Output Data Table
		String[] columnName = {new String("River Mile"), new String("Max Flow (cfs)"),new String("Max Elevation (ft MSL)"),
			new String("Max Depth (ft)"), new String("Max Depth Time (hrs)"),new String("Flood Time (hrs)"),
			new String("De-flood Time (hrs)"), new String("Flood Depth (ft)")};

		int nSections = outputFile.inputDownstream.size();

		String[][] tableValue = new String[nSections][];
		for (int i=0; i< nSections; i++)
		{
			tableValue[i] = new String[] {NumFormat.format(((DownstreamPoint)outputFile.inputDownstream.get(i)).distanceToSection,4),NumFormat.format(outputFile.maxFlow[i],4), NumFormat.format(outputFile.maxElevation[i],4),
				NumFormat.format(outputFile.maxDepth[i],4), NumFormat.format(outputFile.timeMaxDepth[i],4), NumFormat.format(outputFile.timeFlood[i],4), NumFormat.format(outputFile.timeDeflood[i],4), NumFormat.format(((DownstreamPoint)outputFile.inputDownstream.get(i)).floodDepth,4)};
		}
		//javax.swing.JTable 
		jt = new javax.swing.JTable();
		jt.setModel(new UneditableJTableModel(tableValue,columnName));
		setColumnAlignment(jt.getColumnModel());
		gettabbedOutput().addTab("Output Data Table", new javax.swing.JScrollPane(jt));

		//Water Surface Profile Chart
		double[] xValue = new double[nSections];
		for (int j=0; j<nSections; j++)
		{
			xValue[j] = ((DownstreamPoint)outputFile.inputDownstream.get(j)).distanceToSection;
		}

		double[] yValueWater = new double[nSections];
		for (int j=0; j<nSections; j++)
		{
			yValueWater[j] = outputFile.maxElevation[j];
		}
		double[] yValueInvert = new double[nSections];
		for (int j=0; j<nSections; j++)
		{
			yValueInvert[j] = outputFile.maxElevation[j] - outputFile.maxDepth[j];
		}
	
		com.klg.jclass.chart.JCChart jc = new com.klg.jclass.chart.JCChart();
		com.klg.jclass.chart.data.JCEditableDataSource dm = 
			new com.klg.jclass.chart.data.JCEditableDataSource(new double[][] { xValue, xValue },
				new double[][]{ yValueWater, yValueInvert },null,new String[]{"  Water Surface","  Invert"},"");
		jc.getDataView(0).setDataSource(dm);

		jc.getHeader().setVisible(true);
		((javax.swing.JLabel)jc.getHeader()).setText("Water Surface Profile Chart");
		((javax.swing.JLabel)jc.getHeader()).setFont(new Font("default",Font.BOLD,17));
		((javax.swing.JLabel)jc.getHeader()).setForeground(new Color(0,0,0));
		jc.getLegend().setVisible(true);

		jc.getChartArea().setAxisBoundingBox(true);
	
		jc.getDataView(0).getSeries(0).getStyle().setLineColor(new Color(0,0,255));
		jc.getDataView(0).getSeries(0).getStyle().setSymbolColor(new Color(0,0,255));
		jc.getDataView(0).getSeries(1).getStyle().setLineColor(new Color(110,70,70));
		jc.getDataView(0).getSeries(1).getStyle().setSymbolColor(new Color(110,70,70));

		com.klg.jclass.chart.JCAxisTitle titleX = jc.getChartArea().getXAxis(0).getTitle();
		titleX.setText("DISTANCE - miles");
		titleX.setVisible(true);
		com.klg.jclass.chart.JCAxisTitle titleY = jc.getChartArea().getYAxis(0).getTitle();
		titleY.setPlacement(com.klg.jclass.util.legend.JCLegend.WEST);
		titleY.setRotation(com.klg.jclass.chart.ChartText.DEG_270);
		jc.getChartArea().getYAxis(0).setGridVisible(true);
		titleY.setText("ELEVATION - feet MSL");
		titleY.setVisible(true);
		gettabbedOutput().addTab("Water Surface Profile Chart", jc);


		//Peak Discharge Profile Chart
		double[] yValueFlow = new double[nSections];
		for (int j=0; j<nSections; j++)
		{
			yValueFlow[j] = outputFile.maxFlow[j] / 1000;
		}
		com.klg.jclass.chart.JCChart jc1 = new com.klg.jclass.chart.JCChart();
		com.klg.jclass.chart.data.JCEditableDataSource dm1 = 
			new com.klg.jclass.chart.data.JCEditableDataSource(new double[][] { xValue },
				new double[][]{ yValueFlow},null,null,"");
		jc1.getDataView(0).setDataSource(dm1);

		jc1.getHeader().setVisible(true);
		((javax.swing.JLabel)jc1.getHeader()).setText("Peak Discharge Profile Chart");
		((javax.swing.JLabel)jc1.getHeader()).setFont(new Font("default",Font.BOLD,17));
		((javax.swing.JLabel)jc1.getHeader()).setForeground(new Color(0,0,0));

		jc1.getChartArea().setAxisBoundingBox(true);

		com.klg.jclass.chart.JCAxisTitle titleX1 = jc1.getChartArea().getXAxis(0).getTitle();
		titleX1.setText("DISTANCE - miles");
		titleX1.setVisible(true);
		com.klg.jclass.chart.JCAxisTitle titleY1 = jc1.getChartArea().getYAxis(0).getTitle();
		titleY1.setPlacement(com.klg.jclass.util.legend.JCLegend.WEST);
		titleY1.setRotation(com.klg.jclass.chart.ChartText.DEG_270);
		jc1.getChartArea().getYAxis(0).setGridVisible(true);
		titleY1.setText("DISCHARGE - cfs in thousands");
		titleY1.setVisible(true);
		gettabbedOutput().addTab("Peak Discharge Profile Chart", jc1);
	} catch (Exception e) {
		System.out.println("caught in addRiverProfileCharts");
		e.printStackTrace();
	}
}
/**
 * connEtoC1:  (ButtonPrint.action.actionPerformed(java.awt.event.ActionEvent) --> OutputManager.handlePrintResults()V)
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connEtoC1(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.handlePrintResults();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
/*connOutput
*/
private void connOutput(java.awt.event.ActionEvent arg1) {
	try {
		this.handleWindowClosing();
	} catch (java.lang.Throwable ivjExc){
		handleException(ivjExc);
	}
}
/**
 * connOutput:  (closeOutput.action.actionPerformed(java.awt.event.ActionEvent) --> OutputManager.handleCloseOutput())
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connOutput(java.awt.event.WindowEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.handleWindowClosing();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * connViewSave:  (ViewSaveOutput.action.actionPerformed(java.awt.event.ActionEvent) --> OutputManager.handleViewSaveOutput())
 * @param arg1 java.awt.event.ActionEvent
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void connViewSave(java.awt.event.ActionEvent arg1) {
	try {
		// user code begin {1}
		// user code end
		this.handleViewSaveOutput();
		// user code begin {2}
		// user code end
	} catch (java.lang.Throwable ivjExc) {
		// user code begin {3}
		// user code end
		handleException(ivjExc);
	}
}
/**
 * 
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private static void getBuilderData() {
/*V1.1
**start of data**
	D0CB838494G88G88GB1DCBDAFGGGGGGGGGGGG8CGGGE2F5E9ECE4E5F2A0E4E1F4E13DBC8BD4D4D712CE96E7CC629AB2B19BA7A1B3A61A8C9B5D8C33E3B21CDDF246E41C44CC4CD93347C94C2E49BA891398E3A2C6B498C9FCB42008080D48C7C0E824792840C0641CE588AABF7936CA14A049A27A201F505AF4375D8F4144C9366AFEDEBF3ADF731BB81CD3673D2A7B2BDB372AEE553D2F9176654C0D0D526D930428D8017D7DEF14A024378AC2FF5DC33BB82D726D8B71026AAF97A0C9F836
	A9861E4DD0B6D256932F97DA0E46815E8DF8C4E9CFFC9A3CA78B270F7AB7410B0FF186204CF540785D5863742770F1F6907C412A84F8A682DC821A9F40F2G3D70C4D5BA2387821E485E9122F4B0569A2CF3BEA7DDC55F3706562B06E7BBC04DBBD8E7F9D596AB8F825AD200C8EC7B8E2DDB844F1D043B69D5152CDE775D3305705F776AF663D82A7EB94CE0ED973227C1D8CB8491AD7C72AF99B8FE7B557E632EC1073977EAD55519A159B1A41F92EDE23F6494042C354853DFF362C8FDC18805BE8289EDD38E4A
	61923C0FG782F65FC3D343B1A0DBB7B6AECE1753CAFFC7235EEAD65C77F2E1B2F180F6669963F0B7DEFA1F246F66D004FG20BCD71A8AB94F7D766B25E6F13057A909D7CB35A7C0792F7D0C739F330E731FF6G7B2F863C83402A50DFA7F4BF4083G962D6373BAD6486755754DBCA122F73AEE0F0FF9ADDBC767933E4EBD2F65EB676976937D59180ABAD0769E0C8BB0GB093E0A140B200CC22370F279C06E7ED2FDCEF9F9C345BEA9D0EBA2B59F94AEE162C70DED585A50CF6D66A33BB4D02C0F573361CE28D
	FEB8043EB7DAAF91685C4E8BDC17961E3DDF1065EFF4319AF3DB748733AE027511703EFBEEB56F235CAC8614DBB13CE3BBECCF69B2217B2968F4FD37927DC1FC8E14C53E4F655C1548F9B1454C927465BF526D5060E54EB3B7F51756D39EC6563BF921B2FEB19DE5BC9F680B81D68204816843AE441705F199C7249C9DCF46ADB6D9F25AC46B3921DE0768146B06ACD6B368941575362A31E42D85345D6E4FA65B0E4AA57028FA0DD67D6A071AF2D995F8D5E78A2372B0073965C247EE74B936DF1837EEA4AAEBF8
	A73109B9E1F48D23957A3D049E28225335BDC0680887C3D9568670ED247E59EC24CF5B6059CFB7217BEF706CA7D469E7AE1445FCF0A11E2E6F758C741183007B8184GAC82D88190869033112FF1FD6A1FA7E8C76D2A7525EDA3D3D361A90D48CE31456612C5DB1F54A9BAADE22FD5F2892CCEEC9A4A35F7D43B78B4543DBE9823C932C9CED13658ED503BB8A801B239E81D6434A42C83FE8BCBBBAC03928CB06890F83F19E94970340AAE798B07D914A5F78DDA1E1706BAF1DD6C049E2183F86F5808727A897153
	C6F82F5948E5B5AF93E9F3G8FD2647253343D70EC8365162CD5D58D16BEE4C3F45E60BAEAB7E01BA558E7B8374B773B387EB56C7E09D07C07691A7AC7761BF02ACBF343BD756FEB526FD2202F89677BE914214B07CE6278635906BEFC6CBAF40C8C1FE3C4CF407EE247A927F2CEA23A9A3769C23C9120E19317752BE9862A03F522CB62DAF746898AA3195D1227F5DFCE47BDC53AAE59648649A1594C7062E2EB7EDA7ACE37BEDE36AB3A72A62123FF2A8705F1A974F75213A89D7AD0EBB7AF5FC04AC5D9F6DAFA
	07E4691CA44B96DB3F3B7D96420BEB146BF33139A693FB6D06B94AGFE4ADCEF5B1F2C52AB135DFEF54851019643747382590767C07D20CDFC7D6FBE0071558A85FF00606E723C833E56EEBC40BEA766132B27BC46C474070E51150D730AD270DE0207A973DCCBF85032276F8368985159DD9364F9AAC0F5E0DA06EF9D7ED106368E7FE306368E5F13B1360EBE10212523346C618C34DB7B30CD2573BF4FC03F57E73759A4E2F8CA930146166FF760BCACB60BFC5EE26E1764F3162FA55823B6236E86915F1A8E6F
	451B39CF9C4E63BE5178753D42620B17B46326B90FFC27735BCCF5F14EE637CF24BC85E622ADC9D6E99084D5EF9F3249AC86A4FE1B51DD5104A69FBD01ED0B6A06E499F6952745A6970BE4A6250612126306D131EF7100E8B3DBA5D26B2C649A324A2E22C31D074A845A57734295DFFD51C73B643CECF78ECA6622AB62F531D49C16CB492A165612621378FEE89243B25F51C8623EE01043B200504D5CF79C4E42F26EBB18032C37DB2DF429202C7BC3DC070506FDAA59C6EE26B9D8B314517212DC2D72799F72F1
	528E68D1E724EBC392EC69681C5A249BECDF4EBE3057232CD3348E39777B02831814DAD5FE0D5277E7EB2FE18E31712BCA7FF9595B855C37E91FD46702C46C5361348C42B6000E1A650A72E76ADCB19878473CF2B5BC43BF6439508BA318DBF924ECC2E084363FA71B671189009B819AA2E89FFE91ACBFA1751ED3F23BBFAB345789ED7D8E1AB1D3DDBF25D7671BE65708262EA3D4EE217D1B36F846655F107D5F8A741BDB78FABC182D5EB3EADDAEFB1F0504C5CC36269C359DD05A67B91828DF36B8DD90231B25
	913EBE67F310D70C1B981949F84111E302F79CF852EC116DCE3EA5578F930D6E3057C368568CCBDC2F3809CE0BB4AC70DC78CD8317177F966E877E5A284EF9C3768C6AF679104DD2A613B5AA594C065C1076ED4E65FD97FD4457786CB5149D49F7518EC7CADD4360832B2ADA9BAC22555E2F7075BA59DB111FCB9F71DC766E63A4EE87BC88E0A9G0D2BC332F96E359AE869DB396D1C2FD55BCEBB50C7G82G284F484B79947D71243D84DF0797772540D353C188DC4F9ECE2EE163AFFBFC16D07D5DE94DBCFB46
	1EAEDD50D6AA4315DB5D32ECFEE0DDCCB7G4BEBBFD56B6C6255D61FBAEB0C24FDD4C6FA6A6C4503A8578E208FC4F21D7D4F1CA4154EC28E8E69C1BFF883E58FB4C4D33BE7381FA1165B3D407762B506496401B7A3F9BC9F1007EB648F78C200A5G2BGA2955EF6650E1B571E9C99A32F3564CE062F2C0FF97C72A649AD4BBE560E399A983D0BBD2D9F731CB076844989BF764C8977903A7F471EB961FD47151C904AE2BE6163BF136D497740A723F9717340E93B65DE6DCABD70C00FF6D417771C73E117F4C395
	78195FA6075A0FAF063EC2816236C1DE0CC06CF4D6ED9C6327E29BDF677379683F2D004FG188B9082309AA09240A07430757EAAD74B370DDEEFDF0E4D1D9350B16FABC0BEEA206FF6257FDF676ED669FA3B9D2CD651F373C15C4F6CCECB3F4546E996C26B33BBC960C5637EED44E7DE02BEEFAA7D4E4F739DA7AE48530E93FF19279DA77EBAEF6CDC67B7F93E62C84A5F34DC4E1FBFG25FDC47C69FC4017AA344F2B11368A70F005367C98098B23FA620B23B8ED7FF1A2C74481281E6DBA013140A585CFA178B4
	85FF7078D4F390DF00E30D5675A73F77B15439448102BF1F3A072C054BA5390862B50832484A5E16D3840D787B34B8A8313EA06F4C5E496A1C146D75650A3DEDBC142EEDEF6DA2E8A63535E7DBBDED8D65BB9FA098005ADA4935B85667F22051FD72619A123B6BFA628DG15BA3EA67754FA7A06F6A8B36B28DCC7F4BDDE3E0E6A1B186FDB37877235F55B160FF666328FB97BA4CF7D9D4ACFD74A5C565BA2CB03A8BADA67DB52DE961D1037F8361FCE5A53B2DF6DE7650F1DEB8594209F9ED17039843F212C0FD4
	1B280CF152E6E60E02461329443EDE050AAFC6F15AFD0436GE8BF0B62EB1FD40D7AE116AE0B909554DB6DAE690C039D2181FD098E6FF90A2B797A4D841C5DE38CE33CF559A98E824B679CD21F90900DF5872A39BE44811E85D08D508DF093000E7976B16CE350EE962D4A981F9CD36EBB14FAE547311EEC1149BE9F8EF4D84592C1515BDA91F38796150110E5620BEC925F879ACFF86AA61EF1D44674441BE238EEFE524A7781F38C574D0F1BD1B783GDF8430AC066B662757BCF5B3B4462D0F91B15E3AC97755
	54DA6CAFA13647E33FB590FA160ACE775B409A3E5F96C319FFAC77F10F11BDAB9C70386D1C36115032GAFD6E8DDF55CE7D6EE67322C2FC65ED5E6DCFA5EE2B35B07C1ECF852AA3BCA9BDA4E351FB9F772621953976BCD70C2633939436AF82EB25E69B31E6B5ECE676FE04FA5CFE530B8F996519FEC9388E52B3763FAE79CD3473D48E3F48C0D83E839E9D4B95A8904342A72EFC715A32F195B5565C1234AE3D56D693CDE486779C3648B8102758959671DD9518EBA9F93FBF64D4C90D6DD1E2F23F2CDB90EFB0A
	6C9C12E83F7F71FFEAF9696F6F56C57968570F75674F1E987FD2F9D554F0FBDB32036B641ED65409C840774541DA47F11D0CEE63BA599047780BF36B21B54E17CEDEEBA1B1E09C553D1D8A7DDBC28FD05129CEC693BA6241D01635136BDA8B716D53G1F2D506EF2F27D8B5E49794C2AC5B94545F33C006018A3D37C711A496E135857C46B52B11E1B749813B937237A70B43147C11B840121250F7A36150476C7DC63F8EE0F71F5C4EFE0D322FE6D9BBC8AE0BE54DE477DD69954EFGB08378814C8720F1E3D199
	39AFGFCF5BC0F6F9EABB9824FCE710AF7368D44F6279DBC3BFC03D249A98B134B066A295D0FBD3D035B5022FF7BA7A1780795BEF3F0E19753F1768C3BA3035069DCFEDC0E73ED07B2AB00BFC0A0C088400ADDFCEE11A548A3C65640FE2DAC0BFD8364F402875CF8E060A66306C47BFE8253E736FF5615667B128F74508A3E71CC6F955828BB44FEC5E77ED263A91BB53B5CFA97394B17FD7D8331D7FDC20F475D6CACC2B72A68543ED2DA7979C0A51485A4F0DB4AE843F596F6034D6D66344AA62415815E20507E
	35195B5C40EEEEE343ADF86EB69B70C000A5GAB15B2BFD2368E7004C470A7GF86EC04BF636201DE07D7174A010751948461938AD9EA4E36C18C07DD826EF1331C7EAE3172EE1DEE495EF5807A0AEDA140836B5FC0D5BD6BB6083091467GB67F06BDD867D1E783DF5FC46FB971BB38E886FAE8E384C8E1F8A53CCB2ABA490269AB348B5529AF232F2F45E72123106D13775671FDF2669E3ECF7E364EF31F9C3D8FEE2A735CA723C7159756FB6E135BC615FFD96F39CFFE414A294D1414A1DD09D1295BE0E595E5
	58964F6EA18233FCCD0246F3D05156CFED6F1923D83E84678290236F09CF87A856F31B8EAEBBA8901B5630671366FE0947A64A5DC3396F987F6172716F0E97146399E62F9D56E1907BD7F25CF2DCD3596EA0C4CAFB329CE585E97600CC2BB2FAC8B9B9CF15AE93B2353D769AEE0F4DFAEE0F2F35F0FB346A394D5DCEE2CDFF4083955AECC2437309458AAD33156FD3D8F7B2FEB9F082756998954A98133D1B9A6FEE56EDFF65ED98D3B91C52E54969144C74EA07457B2D442F05823FE1FA3EA77657F85B2B58446D
	B5AB29A73E82209B408100B28BC842BA4FB79B4660E5C69215899D639719B87FD61358ABD94B996D51B264334FBE58EBBF57A759C0ED6C824F31D27B10A71A7172BE7CF76CD0E81F32BB126096359D05B27E62767630F35E3BC759E956DEDA5EGCF1A2B4C4A57BA336A02F2ABG5DA3FEF60DFF1F658734103DBC976BBF087B9A4953E19225165E4152216B0325E22FDCAA4196D7D5F5FA48EAB5B1318AB41EDCC57A8A07BE921241FF8150BDFD24006F698B9F1DA518267DD0B3AEDE5DB1282BCE26F23D146C0E
	DFC315F375D71CBC068D00729CA2B70859847E97F6C20BC6E32107CC08D1FE7E18407949DAF0175070CA05AECF039F04950C8F63BEB7BF151031D2FD3F3FEE32B18E35775F9F756DF3DEBABA3E4F79AF720D24043E620603657D7FF3141C61DD3E6C12945F96FAD42F50DADC6DF617853DA935019FB4F35D68063949G81GC1GF85632F49F5689EFC99FE3AE6CAEF3872E39835516EEC66C0EB34908F4D96C6D154C74E60B6A64670D6ABE75A9F43CEAF8368350355A57A2A87D17B4611913CD9AB6A897938D96
	706A7841CCD1D93161E8996EDDD417EFEF623ABCGFD4DCD613ADCFEA9CBD01DB9E1A7D5D5CDCE0B39CE6C27AACC756B25A63ED72ECC61B952B65227BE35A73E8820A6157B15371A316F53F627BC2068553024562B2C1CB2DD128F3D6ACA257A64176A56AB6E8723050D17FD1C11918FC3F97D220D5BC2F02AE23356CC6DF67568066A6CA3ACA6D8DA474FC542D279194533793C3F22B4BE77BA8779B6947091G7FB43E17BC62F5E6B1317D61D713EEC7635BF6A779BEAD0D4637D18A7DCBC2DF2422D3BDF8C371
	E90B214C184E731BEAF22EB8837079GA1GABG4281747BF97CF02322CC98679C3E436EB00171DAD5C754F4FE2B1C6A7999214F4A7DE36F1D142F075AB0B6B4C15D810019G7381028116830482C4GA4E440DCG2A815A814CGA3GB3G82G02B2789C9EECC21B701533B145A70110D32477GD4EE7F5C4C6536847A0A4C60FA75D669A83D2276D3E7473BFD3683D09D5A504C6D27A2136BD08E49712D007B8184E472BD614AD5352D207E7BBAABD914C9E538B2D34BD60E449D5056798E62FA68519D636F76
	C62E7769D91C3F30C6642F9D708100E9D9DC476FEA1A1A0EBFB845F6AFCF3A9D358197D933B9D91E36F1135067E7F95AC62C3246C1D0366FG370DBBCE9960B98278EC00E00065G21G11G7AEC70FF59DC3F3ADA0A4650AF2A98EE7522737B2ACDEBFE8D596CFCA9DBBBA7CA92DE9CF30F357AEC77C23EE73BE92A3A33B55AD139BDC9FCDEE03627BCFFC3680B32BD65596CD0FC8D1499F3387EF46231BD6365226AFD4D356F7B6E46FB6ED14EDBB9865501132D3EE398D32B765D4859D66DFB3F2E56316FACAE
	357A7EBE2C3FD57DFD98BB3FA6F1620C9C3AD61496C1E8F3990D36FD224D779CC25A460F43FE57261D7B3D50661D7BADEF534E7DDEEED367FE14765FEDB827FE7053DDCA3D558A6DCB0576A752A7524EDB4CA48E2174F789DFC8EF16F0C065DBD14257B021F5B15AE6956DCBC65B5A0631A4CCF9406E64ED85931EADDE36D82D940FB1E19D0B8DC2CBC59EBBCD482345A12A78CED4E8DFAA7A790B66297948272658EE73245BD11B7FA9593BC2F2BCFD414F89FDE90E27AF48EF613EE08514D59A38EF7D60B4469D
	810087832C828887084B6D09B764F21F7A10893F777065D34F5A6D64968AAF2440272A5DAB1B632C463166D8116B4B5F19C9FC5B1C6BF90FE4A3742EDC4FFB244D0D7C9EE9G4A1665F2FFF723D9D064FD1B6AFDD7732D77F746DBBA86DD1FFBCF79769581274677950F1C5276958BCEF97B0A20D35A3E6249D35E3E625FD93FCE65821E521FBE057194BD77F0FFD3C8FD61B562F34A8EC2BEF3101FADD1BF04E7CBA605962A5C7985A834B7C81E308EF0A10F530A8AF9BDE39E57079007DB87FE27FABF6D38757A
	50FFCB4720F127D800F6404EA9097B2574AC72ADA723F3774B781A5F6216433F285E0F345CFA191C3925E330FD6334EF9B511D995FC6F61D51361124B35EB612FAC65BC6324EF81F25BE524875D3D674738A795D69DC8517899E2C60110D5F173CF08C6DB5C9E97A3E4650BAEF256794B35AC65F895E590EFAC76FA45EEB63F7922BB4F844766ABB09B02803104268B1C6777EC77B5BDA477B539B3D7B0B75684FE8F477C76E28D47D31EF08723D4FEE75857C7C2841683EEB69B6527BA64A4709C24E072C41C714
	879FB320CE0051FD5792E4F45F3550336010C64398328FB12A4F0237DB71DEC696FBCF5B6931977BEE6411A64F33D8BC1BDF8EEDC30D7C8CA65B444FE0A20DDA671B7CFB1547BF47F75A26381E37294857BE9325ED5EBA0B985D43AB8BF81CE205B67373F9AEBCC4682BG8F87C8A860FCB98A79984602491E3B6E6C9094593C5A086BD9AF616FB1B97F2FB571EFE53A8BB89F3B483D65EC409782ACA960B619E7D247B82B009ED6B076798AEDD7DE2FEE97D358931FDEB811F6478B2696EF1AA65D0E46DC6F9079
	9797FA461B9F90FAE521E73C790D320E8DD096D84865B4D4289EBF2188624C22094C778D2FB3088AD53BE60FBE3A81277CFDC7564BD16499C35ECE68530ABCE34824D69EC37AC3D9F8910FA32ACC5EF95B8913BAEF233466FA6FFA276A557568FC5697884AFB046A7D878537FE3F3D7F160EC167BC3350DB8EF797FA4BEB0BF2375FF5084B5A255CA55ED4E877905ADC40979D623AB43D95770F458AFE9741D7A8F8DA6B5FBACF6CFBECD9167D4DE30CF59FC97715034AFDC878A1FAA718F0986C86GE3024A43D8
	670E895C07BCC6E4887E3F83BFB124BE70B93C7EE66547EC6E3BC2FE4F373855731E8F471CF6185F5135AAE7F63301366CB0774BBFD64EDBE30EF05F3918542D863C8340FA041F37AE3D207EB64DF06404669DDC4405ABBA3FA3F4BD820F384FDBCD42CCF68777E1AB5F8BD69D61BA50BE453C3CE70A6D6E1BE2BEDFC17273FD451E7E7598219B0ABD7D6B330A5C0B214C3F186773EFF5229E04832EAF81DD81B081DC8298A961797C2E4E314EC86931396799E9E42B563CE61650F518DB62AB0E2CD26592752A77
	E5FF075C7BCFFF075CFBFD276F3CE2F36778F94536CE6D3CA2763CF7DE91FFDEBB2F501F774EAB1215BCFAD18977213212C7AFAE61771961A8A296A35F29445C7A5271E3D34F185BD86A56D1EA0FA14ABDE0D9A99FAF6DAB378EBCBD6A5CFDG6A5C5498D78DF49EAB5B3C7716575A54FB8B7D5FBCC50DAEC0963EA7444AC2A20DF88D6C3CC196F613AF138C6C67E1AE2730C73944EDA1A7072B2F9F2D0AFEF3DE34CE08E991365395EA91E2D04CADC2ACD90596A19AD6E8840F90E254G3D6D753E922E1D756AAB
	8BAFB66C5389BA68F0C022BD448EE302CE7A48C6514842CE7ED1C6156B1A702D20736AFD92D4D9C8BEEDE73F1EB554DE97ADD654BED9E82A378FD94D861BDDB6B807EC86F9C0B28C62BDE2294150E2433AE6D1968D9617C132C01953B0E8F139AC36FE035DE930587A40B7D8DC7875F92976286C4E7950F76AEF03A3261F7379ADF5C899DD7765E5EE3FCE7D59D3E49FC9A86F09AFAA677EEBF7B3FE946F6B3B46F3032255AA0B562B35B6B3FE63D8F7C3165C67754FB4E37FAEDE07955232671B116FAB2AB67F8F
	D0CB8788641039235698GGC0CAGGD0CB818294G94G88G88GB1DCBDAF641039235698GGC0CAGG8CGGGGGGGGGGGGGGGGGE2F5E9ECE4E5F2A0E4E1F4E1D0CB8586GGGG81G81GBAGGG9099GGGG
**end of data**/
}
/**
 * Return the JButton1 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButtonClose() {
	if (ivjButtonClose == null) {
		try {
			ivjButtonClose = new javax.swing.JButton();
			ivjButtonClose.setName("ButtonClose");
			ivjButtonClose.setMnemonic('c');
			ivjButtonClose.setText("  Close  ");

			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButtonClose;
}
/**
 * Return the ButtonPrint property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButtonPrint() {
	if (ivjButtonPrint == null) {
		try {
			ivjButtonPrint = new javax.swing.JButton();
			ivjButtonPrint.setName("ButtonPrint");
			ivjButtonPrint.setMnemonic('p');
			ivjButtonPrint.setText("Print Screen");
		//	ivjButtonPrint.setBounds(42, 1, 127, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButtonPrint;
}
/**
 * Return the ButtonViewSave property value.
 * @return javax.swing.JButton
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JButton getButtonViewSave() {
	if (ivjButtonViewSave == null) {
		try {
			ivjButtonViewSave = new javax.swing.JButton();
			ivjButtonViewSave.setName("ButtonViewSave");
			ivjButtonViewSave.setMnemonic('s');
			ivjButtonViewSave.setText("View/Save/Print Output File");
			ivjButtonViewSave.setEnabled(false);
		//	ivjButtonPrint.setBounds(42, 1, 127, 20);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjButtonViewSave;
}
/**
 * Return the closeOutput property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getcloseOutput() {
	if (ivjcloseOutput == null) {
		try {
			ivjcloseOutput = new javax.swing.JPanel();
			ivjcloseOutput.setName("closeOutput");
			ivjcloseOutput.setPreferredSize(new java.awt.Dimension(0, 30));
			ivjcloseOutput.setLayout(new java.awt.GridBagLayout());

			java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
		//	constraints.gridx = 1; constraints.gridy = 0;
		//	constraints.insets = new java.awt.Insets(4, 4, 4, 4);
		
			constraints.insets = new java.awt.Insets(0, 100, 0, 100);
			getcloseOutput().add(getButtonViewSave(),constraints);
			
			constraints.insets = new java.awt.Insets(0, 100, 0, 100);
			getcloseOutput().add(getButtonPrint(), constraints);
			
			constraints.insets = new java.awt.Insets(0, 100, 0, 100);
			getcloseOutput().add(getButtonClose(), constraints);
			
	    	// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjcloseOutput;
}
/**
 * Return the JComboBox1 property value.
 * @return javax.swing.JComboBox
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JComboBox getcomboScenario() {
	if (ivjcomboScenario == null) {
		try {
			ivjcomboScenario = new javax.swing.JComboBox();
			ivjcomboScenario.setName("comboScenario");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjcomboScenario;
}
/**
 * Return the JDialogContentPane property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJDialogContentPane() {
	if (ivjJDialogContentPane == null) {
		try {
			ivjJDialogContentPane = new javax.swing.JPanel();
			ivjJDialogContentPane.setName("JDialogContentPane");
			ivjJDialogContentPane.setLayout(new java.awt.BorderLayout());
			getJDialogContentPane().add(getpanelButtons(), "North");
			// user code begin {1}
			getJDialogContentPane().add(gettabbedOutput(), "Center");
			getJDialogContentPane().add(getcloseOutput(), "South");
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJDialogContentPane;
}
/**
 * Return the JLabel1 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel1() {
	if (ivjJLabel1 == null) {
		try {
			ivjJLabel1 = new javax.swing.JLabel();
			ivjJLabel1.setName("JLabel1");
			ivjJLabel1.setText("Scenario:");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel1;
}
/**
 * Return the JLabel2 property value.
 * @return javax.swing.JLabel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JLabel getJLabel2() {
	if (ivjJLabel2 == null) {
		try {
			ivjJLabel2 = new javax.swing.JLabel();
			ivjJLabel2.setName("JLabel2");
			ivjJLabel2.setText("Could not run the model.  Input data is either missing or inconsistent.");
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJLabel2;
}
/**
 * Return the JPanel property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel() {
	if (ivjJPanel == null) {
		try {
			ivjJPanel = new javax.swing.JPanel();
			ivjJPanel.setName("JPanel");
			ivjJPanel.setLayout(null);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel;
}
/**
 * Return the JPanel2 property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getJPanel2() {
	if (ivjJPanel2 == null) {
		try {
			ivjJPanel2 = new javax.swing.JPanel();
			ivjJPanel2.setName("JPanel2");
			ivjJPanel2.setLayout(null);
		//	getJPanel2().add(getButtonPrint(), getButtonPrint().getName());
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjJPanel2;
}
/**
 * Return the panelButtons property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel getpanelButtons() {
	if (ivjpanelButtons == null) {
		try {
			ivjpanelButtons = new javax.swing.JPanel();
			ivjpanelButtons.setName("panelButtons");
			ivjpanelButtons.setPreferredSize(new java.awt.Dimension(0, 30));
			ivjpanelButtons.setLayout(new java.awt.GridBagLayout());

			java.awt.GridBagConstraints constraintsJLabel1 = new java.awt.GridBagConstraints();
			constraintsJLabel1.gridx = 1; constraintsJLabel1.gridy = 0;
			constraintsJLabel1.insets = new java.awt.Insets(4, 4, 4, 4);
			getpanelButtons().add(getJLabel1(), constraintsJLabel1);

			java.awt.GridBagConstraints constraintscomboScenario = new java.awt.GridBagConstraints();
			constraintscomboScenario.gridx = 2; constraintscomboScenario.gridy = 0;
			constraintscomboScenario.fill = java.awt.GridBagConstraints.HORIZONTAL;
			constraintscomboScenario.weightx = 1.0;
			constraintscomboScenario.insets = new java.awt.Insets(4, 4, 4, 4);
			getpanelButtons().add(getcomboScenario(), constraintscomboScenario);

			java.awt.GridBagConstraints constraintsJPanel = new java.awt.GridBagConstraints();
			constraintsJPanel.gridx = 0; constraintsJPanel.gridy = 0;
			constraintsJPanel.fill = java.awt.GridBagConstraints.BOTH;
			constraintsJPanel.weightx = 1.0;
			constraintsJPanel.weighty = 1.0;
			constraintsJPanel.ipadx = 50;
			constraintsJPanel.insets = new java.awt.Insets(4, 4, 4, 4);
			getpanelButtons().add(getJPanel(), constraintsJPanel);

			java.awt.GridBagConstraints constraintsJPanel2 = new java.awt.GridBagConstraints();
			constraintsJPanel2.gridx = 3; constraintsJPanel2.gridy = 0;
			constraintsJPanel2.fill = java.awt.GridBagConstraints.BOTH;
			constraintsJPanel2.weightx = 1.0;
			constraintsJPanel2.weighty = 1.0;
			constraintsJPanel2.ipadx = 50;
			constraintsJPanel2.insets = new java.awt.Insets(4, 4, 4, 4);
			getpanelButtons().add(getJPanel2(), constraintsJPanel2);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjpanelButtons;
}
/**
 * Return the tabbedOutput property value.
 * @return javax.swing.JTabbedPane
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JTabbedPane gettabbedOutput() {
	if (ivjtabbedOutput == null) {
		try {
			ivjtabbedOutput = new javax.swing.JTabbedPane();
			ivjtabbedOutput.setName("tabbedOutput");
			ivjtabbedOutput.insertTab("No Output Available", null, gettabNoOutput(), null, 0);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjtabbedOutput;
}
/**
 * Return the Page property value.
 * @return javax.swing.JPanel
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private javax.swing.JPanel gettabNoOutput() {
	if (ivjtabNoOutput == null) {
		try {
			ivjtabNoOutput = new javax.swing.JPanel();
			ivjtabNoOutput.setName("tabNoOutput");
			ivjtabNoOutput.setLayout(new java.awt.GridBagLayout());

			java.awt.GridBagConstraints constraintsJLabel2 = new java.awt.GridBagConstraints();
			constraintsJLabel2.gridx = 0; constraintsJLabel2.gridy = 0;
			constraintsJLabel2.insets = new java.awt.Insets(4, 4, 4, 4);
			gettabNoOutput().add(getJLabel2(), constraintsJLabel2);
			// user code begin {1}
			// user code end
		} catch (java.lang.Throwable ivjExc) {
			// user code begin {2}
			// user code end
			handleException(ivjExc);
		}
	}
	return ivjtabNoOutput;
}
/**
 * Called whenever the part throws an exception.
 * @param exception java.lang.Throwable
 */
private void handleException(java.lang.Throwable exception) {

	/* Uncomment the following lines to print uncaught exceptions to stdout */
	// System.out.println("--------- UNCAUGHT EXCEPTION ---------");
	// exception.printStackTrace(System.out);
}
/**
 * This method is called when "Print Results" is clicked.
 */
private void handlePrintResults() {
	TabbedPrinter tp = new TabbedPrinter(gettabbedOutput(),jt);
	return;
}
/**
 * Insert the method's description here.
 * Creation date: (7/31/2003 11:46:07 AM)
 */
private void handleScenarioSelection() {

	try {
	
		ModelScenario selectedScenario = (ModelScenario)currentData.scenarios.get(getcomboScenario().getSelectedIndex());
	
		if (selectedScenario == null)
		{
			selectedScenario = (ModelScenario)currentData.scenarios.get(0);
		}
		gettabbedOutput().removeAll();
		if (selectedScenario.bOutputAvailable) {

			/******************************************************************************
		 	* NOTE:	This is where a dambreak forecast generator could be inserted.
		 	*			You could create a new class extending JPanel (like FldViewPanel).
		 	******************************************************************************/
			gettabbedOutput().addTab("Graphic", new DisplayGraphicsPanel(selectedScenario.output));
			addRiverProfileCharts(selectedScenario.output);
			gettabbedOutput().addTab("Export to FLDVIEW", new FldViewPanel(selectedScenario.output));
			if (selectedScenario.output.bHasWarning) {
				//JTextPane 
				warningText = new JTextPane();
				warningText.setEditable(false);
				warningText.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
				warningText.setContentType("text/html");
				warningText.setText("<b>WARNING</b><br><br>"+selectedScenario.output.warning);
				gettabbedOutput().add("Warnings", new JScrollPane(warningText));
			}

			if (selectedScenario.output.bHasFullText) {
				//JTextPane
				fullText = new JTextPane();
				fullText.setEditable(false);
				fullText.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
				// fullText.setContentType("text/html");	
				fullText.setText(selectedScenario.output.fullText);
				fullText.setCaretPosition(0);
				//if title on the tab is changed here, it must be changed in handleViewSaveOutput() also
				gettabbedOutput().add("SMPDBK Output Text", new JScrollPane(fullText));
			}
			if (selectedScenario.output.bHasPrerunText) {
				//JTextPane
				prerunText = new JTextPane();	
				prerunText.setFont(new java.awt.Font("Courier", Font.PLAIN, 14));
				prerunText.setEditable(false);
				prerunText.setBorder(BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
				// fullText.setContentType("text/html");	
				prerunText.setText(selectedScenario.output.prerunText);
			
				int indPrerun = ivjcomboScenario.getSelectedIndex();
				//System.out.println("Scenario index prerun: " + indPrerun + prerunText.getText());
			
				prerunText.setCaretPosition(0);
				//if title on the tab is changed here, it must be changed in handleViewSaveOutput() also
				gettabbedOutput().addTab("Stored Forecast Text", new JScrollPane(prerunText));
			}
		
			// set the "Stored ForecastText" tab to be a default in the "View Forecast Info" button
			// is clicked in the "Search" window
			if(prerunFlag)
			{
				int indexDefaultPrerun = gettabbedOutput().indexOfTab("Stored Forecast Text");
				gettabbedOutput().setSelectedIndex(indexDefaultPrerun);
			}
	
		} else {
			ivjtabbedOutput.insertTab("No Output Available", null, gettabNoOutput(), null, 0);
			gettabNoOutput().setVisible(true);
		}
		gettabbedOutput().addChangeListener(new ChangeListener()
			{
				public void stateChanged(ChangeEvent ev)
				{
					if(gettabbedOutput().getSelectedIndex() == 5)
					{
						if(gettabbedOutput().getTitleAt(5).equalsIgnoreCase("SMPDBK Output Text"))
						{
							getButtonViewSave().setEnabled(true);
						}
						else if(gettabbedOutput().getTitleAt(5).equalsIgnoreCase("Warnings"))
						{
							getButtonViewSave().setEnabled(false);
						}
					}
					else if (gettabbedOutput().getSelectedIndex() == 6)
					{
						getButtonViewSave().setEnabled(true);
					}
					else if (gettabbedOutput().getSelectedIndex() == 7)
					{
						getButtonViewSave().setEnabled(true);
					}
					else if(gettabbedOutput().getSelectedIndex() == 0)
					{
						getButtonViewSave().setEnabled(false);
					}
					else if(gettabbedOutput().getSelectedIndex() == 1)
					{
						getButtonViewSave().setEnabled(false);
					}
					else if(gettabbedOutput().getSelectedIndex() == 2)
					{
						getButtonViewSave().setEnabled(false);
					}	
					else if(gettabbedOutput().getSelectedIndex() == 3)
					{
						getButtonViewSave().setEnabled(false);
					}
					else if(gettabbedOutput().getSelectedIndex() == 4)
					{
						getButtonViewSave().setEnabled(false);
					}
				}
			
			});		
	} catch (Exception e) {
		System.out.println("caught in handleScenarioSelection");
		e.printStackTrace();
	}
}
/**
 * This method is called when "ViewSave Output File" is clicked.
 */
private void handleViewSaveOutput(){
	String directory = "";
	String tmp = "";
    
	String osName = System.getProperty("os.name");
	String lowerCaseName = osName.toLowerCase();
	if(lowerCaseName.indexOf("windows") > -1)
	{
		System.out.println("OS Name: " + osName);
		// to view the content of the created output file when "View/Save/Print Output File" button
		// is chosen the wordPad editor should be used
		
		String dbaEditor = PropertyReader.getProperty("damcrest.editor");
		System.out.println("Editor: " + dbaEditor);
	
		if ((directory = System.getProperty("damcrest.home")) == null)
			directory = "";

		if (!directory.endsWith(System.getProperty("file.separator")))
			directory = directory.concat(System.getProperty("file.separator"));
		tmp = directory.concat("_tmp.txt");
			
		File out = new File(tmp);
		
		try
		{
			out.createNewFile();
			System.out.println("Out file is: " + out);
		} catch (IOException io) {
			System.out.println ("A new file cannot be created");
			io.printStackTrace();	
		}
		
		if (!out.exists()){
			System.out.println("In OutputManager.handleViewSaveOutput() - Output Does Not Exist" + out);	
	  		return;
		}
		
		ModelScenario selectedScenario = (ModelScenario)currentData.scenarios.get(getcomboScenario().getSelectedIndex());
		String runThis = "";
		BufferedWriter saveOutput = null;
		
		try {
			if(gettabbedOutput().getSelectedIndex() == 5)
			{
				if(gettabbedOutput().getTitleAt(5).equalsIgnoreCase("SMPDBK Output Text"))
				{
					//if title on the tab is changed in the handleScenarioSelected(), it must be changed here also
					System.out.println("SMPDBK Output Text");

					
					int sizeText = selectedScenario.output.fullText.length();
					runThis = out.getAbsolutePath();
					System.out.println("In OutputManager, runThis is: " + runThis);
					saveOutput = new BufferedWriter(new FileWriter(runThis), sizeText);
					saveOutput.write(selectedScenario.output.fullText);
				}
				else
				{
					//if title on the tab is changed in the handleScenarioSelected(), it must be changed here also
					// System.out.println("Stored Forecast Text");
					int sizeText = selectedScenario.output.prerunText.length();
					runThis = out.getAbsolutePath();
					System.out.println("In OutputManager, runThis is :" + runThis);
					saveOutput = new BufferedWriter(new FileWriter(runThis), sizeText);
					saveOutput.write(selectedScenario.output.prerunText);
				}
			}
			if(gettabbedOutput().getSelectedIndex() == 6)
			{
				int sizeText = selectedScenario.output.prerunText.length();
				runThis = out.getAbsolutePath();
				System.out.println("In OutputManager, runThis is :" + runThis);
				saveOutput = new BufferedWriter(new FileWriter(runThis), sizeText);
				saveOutput.write(selectedScenario.output.prerunText);
			}
		} catch (IOException io) {
			JOptionPane.showMessageDialog(this, "Error writing file.", 
				"Error", JOptionPane.ERROR_MESSAGE);
			io.printStackTrace();
			return ;
		
		}
		
		try {
			saveOutput.newLine();
		} catch (IOException io) {
			JOptionPane.showMessageDialog(this, "Error in newline() function.", 
				"Error", JOptionPane.ERROR_MESSAGE);
			io.printStackTrace();
			return;
		}	

		try {
    		saveOutput.flush();
		} catch (IOException io) {
			JOptionPane.showMessageDialog(this, "Error in flush() function.", 
				"Error", JOptionPane.ERROR_MESSAGE);
			io.printStackTrace();
			return;
		}

		try {	
    		saveOutput.close();
		} catch (IOException io) {
			JOptionPane.showMessageDialog(this, "Error in close() function.", 
				"Error", JOptionPane.ERROR_MESSAGE);
			io.printStackTrace();
			return;
		}

		try {		
			System.out.println("Editing a file with " + dbaEditor);
			
			Runtime rt = Runtime.getRuntime();
			Process ps = rt.exec(dbaEditor + " " + "\"" + runThis + "\"");
			ps.waitFor();
			// System.out.println("Process exit code is: " + ps.exitValue());
			if (ps.exitValue() != 0)
				throw new Exception("View/Save " + runThis + " Process exit code is: " + ps.exitValue() + " - Process Failed!");
			
		} catch (IOException io) {
			io.printStackTrace();
			JOptionPane.showMessageDialog(this, "Cannot view the file", "Cannot view the file", JOptionPane.ERROR_MESSAGE); 
		} catch(InterruptedException ie) {
			ie.printStackTrace();
			System.out.println("Interrupted waiting for process!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception in handleViewSaveOutput!");
		}
	
		// System.out.print("ViewSaving output file finished" );	

		out.deleteOnExit();
	}
	else if(lowerCaseName.indexOf("linux") > -1)
	{
		System.out.println("OS Name: " + osName);
		String dbaEditor = PropertyReader.getProperty("damcrest.editor");
		// String [] commandArray = new String[3];
		// commandArray[0] = dbaEditor.trim();
		// commandArray[1] = "+\"set popt=portrait:n\" ";
		
		if ((directory = System.getProperty("damcrest.home")) == null)
			directory = "";

		if (!directory.endsWith(System.getProperty("file.separator")))
			directory = directory.concat(System.getProperty("file.separator"));
			
		tmp = directory.concat("_tmp.txt");
			
		File out = new File(tmp);
	
		try
		{
			System.out.println("Out file is: " + out);
			out.createNewFile();
		} catch (IOException io) {
			System.out.println ("A new file cannot be created");
			io.printStackTrace();
		}
		
		if (!out.exists()){
			System.out.println("In OutputManager.handleViewSaveOutput() - Output Does Not Exist" + out);			
			return;
		}
		ModelScenario selectedScenario = (ModelScenario)currentData.scenarios.get(getcomboScenario().getSelectedIndex());
		String runThis = "";
		BufferedWriter saveOutput = null;
		try {
			if(gettabbedOutput().getSelectedIndex() == 5)
			{
				if(gettabbedOutput().getTitleAt(5).equalsIgnoreCase("SMPDBK Output Text"))
				{
					//if title on the tab is changed in the handleScenarioSelected(), it must be changed here also
					System.out.println("SMPDBK Output Text");
					int sizeText = selectedScenario.output.fullText.length();
					runThis = out.getAbsolutePath();
					System.out.println("In OutputManager, runThis is :" + runThis);
					saveOutput = new BufferedWriter(new FileWriter(runThis), sizeText);
					saveOutput.write(selectedScenario.output.fullText);
				}
				else
				{
					//if title on the tab is changed in the handleScenarioSelected(), it must be changed here also
					System.out.println("Stored Forecast Text");
					int sizeText = selectedScenario.output.prerunText.length();
					runThis = out.getAbsolutePath();
					System.out.println("In OutputManager, runThis is :" + runThis);
					saveOutput = new BufferedWriter(new FileWriter(runThis), sizeText);
					saveOutput.write(selectedScenario.output.prerunText);
				}
			}
			if(gettabbedOutput().getSelectedIndex() == 6)
			{
				int sizeText = selectedScenario.output.prerunText.length();
				runThis = out.getAbsolutePath();
				System.out.println("In OutputManager, runThis is: " + runThis);
				saveOutput = new BufferedWriter(new FileWriter(runThis), sizeText);
				saveOutput.write(selectedScenario.output.prerunText);
			}
		} catch (IOException io) {
			JOptionPane.showMessageDialog(this, "Error writing file.", 
				"Error", JOptionPane.ERROR_MESSAGE);
			io.printStackTrace();
			return ;
		
		}
	
		try {
		saveOutput.newLine();
		} catch (IOException io) {
			JOptionPane.showMessageDialog(this, "Error in newline() function.", 
				"Error", JOptionPane.ERROR_MESSAGE);
			io.printStackTrace();
			return;
		}	

		try {
    		saveOutput.flush();
		} catch (IOException io) {
			JOptionPane.showMessageDialog(this, "Error in flush() function.", 
				"Error", JOptionPane.ERROR_MESSAGE);
			io.printStackTrace();
			return;
		}
		try {	
    		saveOutput.close();
		} catch (IOException io) {
			JOptionPane.showMessageDialog(this, "Error in close() function.", 
				"Error", JOptionPane.ERROR_MESSAGE);
			io.printStackTrace();
			return;
		}

		try
		{		
			System.out.println("Editing a file with " + dbaEditor);
			int dbaEditorNameLength = dbaEditor.length() - 4;
			String dbaEditorName = dbaEditor.substring(dbaEditorNameLength);
			if (dbaEditorName.equalsIgnoreCase("gvim"))
			{
				// TEMPPATH variable is renamed on the Linux side to DAMCREST_RES_DIR
				// for better readability
				String tempPath = PropertyReader.getProperty("DAMCREST_RES_DIR");
		
				if (!tempPath.endsWith(System.getProperty("file.separator")))
					tempPath = tempPath.concat(System.getProperty("file.separator"));
					
				// commandArray[2] = runThis;
				// Process ps = rt.exec(commandArray);// + " " + runThis);
				// System.out.println("process: " + commandArray[0] + " " + commandArray[1] + " " + commandArray[2]);
			
				String vimRes = tempPath.concat(".vimrc");
				
				Runtime rt = Runtime.getRuntime();
				String commandLine = dbaEditor + " -u " + vimRes + " " + runThis; 
				Process ps = rt.exec(commandLine);
				ps.waitFor();
			
				if (ps.exitValue() != 0)
					throw new Exception("View/Save " + runThis + " Process Failed!");
			}
			else if (dbaEditorName.equalsIgnoreCase("edit")) //for nedit
			{
				Runtime rt = Runtime.getRuntime();
				Process ps = rt.exec(dbaEditor + " " + runThis); //" fs/hseb/ob5/wfo_rfc/HP/whfs/bin/_tmp.txt");
				ps.waitFor();
				// System.out.println("Process exit code is: " + ps.exitValue());
				if (ps.exitValue() != 0)
					throw new Exception("View/Save " + runThis + " Process exit code is: " + ps.exitValue() + " - Process Failed!");
			}
			else if (dbaEditorName.equalsIgnoreCase("rite"))  //for "KWrite" editor
			{
				Runtime rt = Runtime.getRuntime();
				Process ps = rt.exec(dbaEditor + " " + runThis);
				ps.waitFor();
				//System.out.println("Process exit code is: " + ps.exitValue());
				if (ps.exitValue() != 0)
					throw new Exception("View/Save " + runThis + " Process exit code is: " + ps.exitValue() + " - Process Failed!");
			}
		}
		catch (IOException io)
		{
			JOptionPane.showMessageDialog(this, "Cannot view the file", "Cannot view the file", JOptionPane.ERROR_MESSAGE); 
			io.printStackTrace();
		}
		catch(InterruptedException ie) 
		{
			System.out.println("Interrupted, waiting for process!");
			ie.printStackTrace();	
		}			
		catch (Exception e) 
		{
			System.out.println("Exception in handleViewSaveOutput!");
			e.printStackTrace();
		}
		
		// System.out.print("ViewSaving output file finished" );
		
		out.deleteOnExit();						
	}
}
/**
 * Comment
 */
private void handleWindowClosing() {
	
	dispose();
	//to exit application when the "Output Manager" window was closed
	//if command line argument was supplied
	if(Launcher.closeFlag)
	{
		System.exit(0);
	}
	return;
}
private void initCloseOutput() throws java.lang.Exception {

	ivjButtonClose.addActionListener(ivjEventHandler);
}
/**
 * Initializes connections
 * @exception java.lang.Exception The exception description.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initConnections() throws java.lang.Exception {
	// user code begin {1}
	// user code end
	/*getButtonPrint().addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			connEtoC1(e);
		};
	});*/
	ivjButtonPrint.addActionListener(ivjEventHandler);
	ivjButtonViewSave.addActionListener(ivjEventHandler);
}
/**
 * Initialize the class.
 */
/* WARNING: THIS METHOD WILL BE REGENERATED. */
private void initialize() {
	try {
		// user code begin {1}
		// user code end
		setName("OutputManager");
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		setSize(911, 513);
		setModal(false);
		setTitle("Output Manager");
		setContentPane(getJDialogContentPane());
		initConnections();
		initCloseOutput();
	} catch (java.lang.Throwable ivjExc) {
		handleException(ivjExc);
	}
	// user code begin {2}
	initializeScenarioChooser(bNoArgs);
	
	// user code end
}
/**
 * Insert the method's description here.
 * Creation date: (7/31/2003 11:27:54 AM)
 */
private void initializeScenarioChooser(boolean bNoArgs) {
	
	int count = 0;  //to count scenarios
	
	String nidid = currentData.damNidid;
	
	if (currentData.scenarios.size() == 0) //the dam has empty dataset or not existing in the database NIDID was inserted from the command line as an argument
	{
		if(bNoArgs == true) 
		{
			bEmptyDataset = true;	//if there is no command line arguments
		} 
		else
		{
			DBAccess dbAccess = new DBAccess();
			boolean bHasDam = dbAccess.doesDamExist(nidid);
			
			if(bHasDam)
			{
				JOptionPane.showMessageDialog(null,"The dam with NIDID = " + nidid + " has empty dataset." + 
				 	"\nMinimum one scenario must be added to create prerun results.", "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			else
			{
				JOptionPane.showMessageDialog(null,"The dam with NIDID = " + nidid + " \ndoesn't exist in the database.", "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
	}
	else
	{
		for (int i=0; i<currentData.scenarios.size(); i++) {
			ModelScenario scenario = (ModelScenario)currentData.scenarios.get(i);
			getcomboScenario().addItem(scenario.source + " - " + EnumeratedTypes.getEnglish(EnumeratedTypes.FIELD_SCENARIO, scenario.name));
			String changedSource = scenario.source;
			if(changedSource.startsWith("#"))
			{
				count++;
			}
		}

		getcomboScenario().addActionListener(this);
		handleScenarioSelection();
		getcomboScenario().setSelectedIndex(count);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (5/7/2004 3:11:53 PM)
 * @return boolean
 */
public static boolean isBEmptyDataset() {
	return bEmptyDataset;
}
/**
 * main entrypoint - starts the part when it is run as an application
 * @param args java.lang.String[]
 */
public static void main(java.lang.String[] args) {
	try {
		OutputManager aOutputManager;
		aOutputManager = new OutputManager();
		aOutputManager.setModal(true);
		aOutputManager.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				System.exit(0);
			};
		});
//		aOutputManager.show();
        aOutputManager.setVisible( true );
		java.awt.Insets insets = aOutputManager.getInsets();
		aOutputManager.setSize(aOutputManager.getWidth() + insets.left + insets.right, aOutputManager.getHeight() + insets.top + insets.bottom);
		aOutputManager.setVisible(true);
	} catch (Throwable exception) {
		System.err.println("Exception occurred in main() of javax.swing.JDialog");
		exception.printStackTrace(System.out);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (7/31/2003 3:50:20 PM)
 * @param tcm javax.swing.table.TableColumnModel
 */
private void setColumnAlignment(TableColumnModel tcm) {
	TableColumn column;
	DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
	for(int i=0; i<tcm.getColumnCount(); i++) {
		column = tcm.getColumn(i);
		renderer.setHorizontalAlignment(SwingConstants.RIGHT);
		column.setCellRenderer(renderer);
	}
}
}
