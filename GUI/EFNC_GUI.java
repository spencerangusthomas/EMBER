/*
	Basic GUI for the Equation-free Numerical Contiunation (EFNC) code
	Written by Spencer Angus Thomas 2014-2015

	Todo:
		threading for run button
		add plot as part of GUI	
*/
import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;
import java.awt.*;
import java.util.concurrent.*;

public class EFNC_GUI extends javax.swing.JFrame {

	//
	//	GUI globals
	//
	boolean MeasuresDefined     = false;
	boolean ModelParamDefined   = false;
	boolean EFNCParamDefined    = false;
	boolean NetlogoPathDefined  = false;
	boolean InitialStateDefined = false;
	EFNCparameters EFNCp = new EFNCparameters();	// Continuation values
	//
	// DEFAULT SETTINGS
	//
	
	// =====================================================					
	// Model parameter settings 
	// =====================================================
	String[] parameterName;		// model parameter names
	String[] parameterValue;	// model parameter values
	String[] systemMeasures;	// measurements of the system
	String[] initialValues;	  // initial values of the system
	String[] LiftVariables;		// Lift Operation
	String modelPath = null;	// path to netlogo file
	boolean isSystemInitialised = false; // is the model initialised during setup?
	
	// =====================================================
	// continaution parameters
	// =====================================================
	int 		realisations 			= 10,								// number of microscopic simualtions to run
					numOfMoments 			= 1, 								// number of moments to use on continuation (limited to 1 atm)
					maxIter 					= 10,								// Max Newton Iterations
					stepNum 					= 1, 								// Steps in Euler ODE solver
					NumContSteps 			= 20,							// number of continaution steps
					normNum						=	2;								// type of norm (1=abs, 2=Euclidean)
			 		
	double timeHorizon 				= 1.0,							// time evolution (tau)
				 dt 								= 1.0, //timeHorizon / (double)stepNum, // step size in the Euler solver
				 secantTol 					= 1e-2,						// tolerance for Newton Corrector //timeHorizon / Math.sqrt( (double) realisations ),
				 newtonTol 					= secantTol, 				// tolerane for initial fixed points used for secant predicter
				 lambda 						= 1.0,							// initial value in damped Newton corrector	x += lambda*h
				 continuationStep 	= 0.1,							// Step size for continuation (ds)
				 hFD 								= continuationStep/2.0, // stepsize for finite differencing
				 contStepInitial		= -continuationStep; // Initial ds, sign determines direction of continuation																							
	boolean DAMP							 = false;					// Use damped Newton Method in the Predictor-Corrector? 					
	boolean BranchSwitching		 = false;					// search for other branhces at bifurcation points after continuation?			
	int		  RANGE							 = 3;							// multiple of ds or stdErr for corrector convergence window	
	int		  NUMOFRESTARTS			 = 5;							// number of restarts to use in the corrector method if outside of convergence window	
	int		 STATISTIC					 = 1;							// type of statistics 0=normal stderr, 1=bootstrap resampling; 

	// =====================================================					
	// test phase settings 
	// =====================================================
	boolean	testing						= false;					// run algorithm in testing phase (true)
	int 		MaxIter 					= 200;						// Number of iterations
	int 		Nmax 							= 100000;					// maximum number of realisations
	int 		EulerSteps 				= 1; 						// number of Euler steps - how to determine?
	int 		NumOfRuns 				= 10;							// number of runs to take averages
	int 		IntReals 					= 10;							// initial number of reailisations	
	int 		realsInc 					= 2; 							// multiplier of N 
	double 	tauIncrement 			= 2.0;						// multiple of tau in test phase
	double 	desiredTol 				= secantTol;			// desired tolerance of fixed point solution (F-x<=tol)
	double tauSpredThreshold = 0.0; 			// confidence interval threshold for determining tau




	public EFNC_GUI() {
		initComponents();
	}

	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated Code">						  
	private void initComponents() {

		testButton = new javax.swing.JButton();		
		testOffButton = new javax.swing.JButton();
		runButton = new javax.swing.JButton();
		modelButton = new javax.swing.JButton();
		compileButton = new javax.swing.JButton();
		measureButton = new javax.swing.JButton();
		graphButton = new javax.swing.JButton();
		netlogoButton = new javax.swing.JButton();
		continuationButton = new javax.swing.JButton();
		initialXButton = new javax.swing.JButton();
		LiftButton = new javax.swing.JButton();
		createParamFileButton = new javax.swing.JButton();
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);


		///////////////////////////////////
		//	Button setup 
		///////////////////////////////////
		modelButton.setText("Model Parameters");
		modelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				modelButtonActionPerformed(evt);
			}
		});

		continuationButton.setText("EFNC Parameters");
		continuationButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				continuationButtonActionPerformed(evt);
			}
		});

		netlogoButton.setText("NetLogo Path");
		netlogoButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				netlogoButtonActionPerformed(evt);
			}
		});
		
		measureButton.setText("Define Measure (Restrict Operator)");
		measureButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				measureButtonActionPerformed(evt);
			}
		});
		
		
		initialXButton.setText("Initial State");
		initialXButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				initialXButtonActionPerformed(evt);
			}
		});
		
		createParamFileButton.setText("Genereate File");
		createParamFileButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				createParamFileButtonActionPerformed(evt);
			}
		});

		testButton.setText("Testing");
		testButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				testButtonActionPerformed(evt);
			}
		});

		testOffButton.setText("Test Off");
		testOffButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				testOffButtonActionPerformed(evt);
			}
		});

		LiftButton.setText("Lift");
		LiftButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				LiftButtonActionPerformed(evt);
			}
		});
		
		compileButton.setText("Compile");
		compileButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				compileButtonActionPerformed(evt);
			}
		});
		
		runButton.setText("Run");
		runButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				runButtonActionPerformed(evt);
			}
		});

		graphButton.setText("Graph");
		graphButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				graphButtonActionPerformed(evt);
			}
		});


		///////////////////////////////////
		//	Button Location 
		///////////////////////////////////
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(
							layout.createSequentialGroup()
						.addGroup(
							layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
							.addComponent(modelButton)
							.addComponent(continuationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 
									140, javax.swing.GroupLayout.PREFERRED_SIZE)
							.addComponent(netlogoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 
									140, javax.swing.GroupLayout.PREFERRED_SIZE)
							.addComponent(measureButton, javax.swing.GroupLayout.PREFERRED_SIZE, 
									140, javax.swing.GroupLayout.PREFERRED_SIZE)
							.addComponent(initialXButton, javax.swing.GroupLayout.PREFERRED_SIZE, 
									140, javax.swing.GroupLayout.PREFERRED_SIZE)
							.addComponent(createParamFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 
									140, javax.swing.GroupLayout.PREFERRED_SIZE)
						)
						.addGap(28, 28, 28)
						.addGroup(
							layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
							.addComponent(runButton, javax.swing.GroupLayout.PREFERRED_SIZE, 
									80, javax.swing.GroupLayout.PREFERRED_SIZE)
							.addComponent(compileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 
									80, javax.swing.GroupLayout.PREFERRED_SIZE)
							.addComponent(testButton, javax.swing.GroupLayout.PREFERRED_SIZE, 
									80, javax.swing.GroupLayout.PREFERRED_SIZE)
							.addComponent(testOffButton, javax.swing.GroupLayout.PREFERRED_SIZE, 
									80, javax.swing.GroupLayout.PREFERRED_SIZE)
							.addComponent(LiftButton, javax.swing.GroupLayout.PREFERRED_SIZE, 
									80, javax.swing.GroupLayout.PREFERRED_SIZE)
							.addComponent(graphButton, javax.swing.GroupLayout.PREFERRED_SIZE, 
									80, javax.swing.GroupLayout.PREFERRED_SIZE)
						)
					)
				)
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			)
		);
		
		layout.setVerticalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(
				layout.createSequentialGroup()
				.addContainerGap()
				.addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(
						layout.createSequentialGroup()
						.addComponent(testButton)
						.addGap(18, 18, 18)
						.addComponent(testOffButton)
						.addGap(18, 18, 18)
						.addComponent(LiftButton)
						.addGap(18, 18, 18)
						.addComponent(compileButton)
						.addGap(18, 18, 18)
						.addComponent(runButton)
						.addGap(18, 18, 18)
						.addComponent(graphButton)
					)
					.addGroup(
						layout.createSequentialGroup()
						.addComponent(modelButton)
						.addGap(18, 18, 18)
						.addComponent(continuationButton)
						.addGap(18, 18, 18)
						.addComponent(netlogoButton)
						.addGap(18, 18, 18)
						.addComponent(measureButton)
						.addGap(18, 18, 18)
						.addComponent(initialXButton)
						.addGap(18, 18, 18)
						.addComponent(createParamFileButton)
					)
				)
				.addContainerGap(20, Short.MAX_VALUE)
			)
		);
		pack();
	}				 

	//===========================================================================================
	//
	//	BUTTON METHODS
	//
	//===========================================================================================
  

	////////////////////////////////////////////////////////////////////////////////////////////
	// Model Parameters Button - define all parameters in the model
	////////////////////////////////////////////////////////////////////////////////////////////
	private void modelButtonActionPerformed(java.awt.event.ActionEvent evt) {										 
	
		String inputValue = JOptionPane.showInputDialog
			("Please input the number of model parameters"); 
		
		int n = 0; // number of parameters in the models
		
		if (inputValue != null){
			JOptionPane.showMessageDialog(null, "Continuation parameter must go first!", 
					"Information", JOptionPane.INFORMATION_MESSAGE); 
			
			// variable number of inputs in the JPannel
			n = Integer.parseInt(inputValue);	
			JTextField[] pName  = new JTextField[n];
			JTextField[] pValue = new JTextField[n];
			parameterValue = new String[n];
			parameterName  = new String[n];
			JPanel myPanel = new JPanel();
			myPanel.setLayout(new GridLayout(n,1,1,1)); //3,3 are gaps
			ModelParamDefined = true; 
			
			for (int i=0; i<n; i++){
				pName[i]  = new JTextField("",15);
				pValue[i] = new JTextField("",15);
				myPanel.add(pName[i]);
			//	myPanel.add(Box.createHorizontalStrut(10));
				myPanel.add(pValue[i]);
			}
			
			JOptionPane.showMessageDialog(null, myPanel, "Parameter Name And Values",
			 		JOptionPane.DEFAULT_OPTION);
		   		
			// Check with user - format data as a table 
			Object rowData[][]  = new Object[n][2];
			for (int i=0; i<n; i++){
				rowData[i][0] = pName[i].getText();
				rowData[i][1] = pValue[i].getText();
				parameterValue[i] = pValue[i].getText();
				parameterName[i]  = pName[i].getText();
			}
			
			Object columeName[] = {"Parameter Name", "Parameter Value"};
			JTable parameterTable = new JTable(rowData, columeName);
			
			// output the table
			JFrame frame = new JFrame("Model parameter summary");
			JScrollPane scrollPane = new JScrollPane(parameterTable);
			frame.add(scrollPane, BorderLayout.CENTER);
			frame.setSize(300, 150);
			frame.setVisible(true);
		}
		
		// Error message
		if (inputValue == null)
		JOptionPane.showMessageDialog(null, "Need to enter model parameters!", 
				"Model Parameters", JOptionPane.ERROR_MESSAGE);	
				
		// model initialisation
		String IC = null;
		String message = "Is the model initialised (default no)";
		String title = "Model Initialisation";	 
		
		int answered = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
		
		if (answered == JOptionPane.YES_OPTION) {
		//	JOptionPane.showMessageDialog(null, "F(x) = x after Lifting step");
			isSystemInitialised = true;
		} else {
		//	JOptionPane.showMessageDialog(null, "F(x) = 0 after Lifting step");
			isSystemInitialised = false;
		}
	}											   

	 
  private static void printLines(String name, InputStream ins) throws Exception {
  	String line = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(ins));
		while ((line = in.readLine()) != null) { 
			System.out.println(line);
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////
	// EFNC Continuation Parameter Button - define the continuation parameter values (tau, N and delta s). 
	// These can be obtained through the test phase
	////////////////////////////////////////////////////////////////////////////////////////////
	private void continuationButtonActionPerformed(java.awt.event.ActionEvent evt) {										 
		
		// Basic or Advanced settings? 
		String message = "Configure basic parameters (default yes)";
		String title = "Continuation Parameters";	 
		
		int answered = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);

		JTextField N_t = new JTextField();
		JTextField tau_t = new JTextField();
		JTextField ds_t = new JTextField();
		JTextField steps_t = new JTextField();
		JTextField tol_t = new JTextField();
		JTextField initialDS_t = new JTextField();

		Object[] Parameters = {
			"Number of Realisations :", N_t,
			"Time Horizon (tau) :", tau_t,
			"Parameter Step Size (ds) :", ds_t,
			"Number of Steps :", steps_t,
			"Tolerance of Solution (default 0.01) :", tol_t,
			"Initial Step (+1 or -1) :", initialDS_t,
		};
			
		message = "Enter Continuation Parameters";
		title 	= "Basic Settings";	 	
		int option = JOptionPane.showConfirmDialog(null, Parameters, 
			title, JOptionPane.OK_CANCEL_OPTION);
						
		if (option == JOptionPane.OK_OPTION){
			int 		N 				= Integer.parseInt(N_t.getText());	
			double 	tau				= Double.parseDouble(tau_t.getText());
    	double 	ds 				= Double.parseDouble(ds_t.getText());
    	double 	tol 			= Double.parseDouble(tol_t.getText());
    	int		 	initialDS = Integer.parseInt(initialDS_t.getText());
    	int 		steps 		= Integer.parseInt(steps_t.getText());
    	EFNCp.setRealisations(N);
    	EFNCp.setTimeHorizon(tau);
    	EFNCp.setContinuationStep(ds);
    	EFNCp.setSecantTol(tol);
    	EFNCp.setContStepInitial(initialDS, ds);
			EFNCp.setNumContSteps(steps);
    	EFNCParamDefined = true;
   	}

		if (answered == JOptionPane.NO_OPTION) {

			JTextField maxIter_t = new JTextField();
			JTextField eulerStep_t = new JTextField();
			JTextField finiteDiffStep_t = new JTextField();
			JTextField ConvergenceRange_t = new JTextField();
			JTextField NoOfRestarts_t = new JTextField();

			Object[] Parameters2 = {
				"Number of Newton iterations :", maxIter_t,
				"Number of Euler steps :", eulerStep_t,
				"Step Size for finite differencing (default ds/2):", finiteDiffStep_t,
				"Convergence range (coefficient of ds) :", ConvergenceRange_t,
				"Number of Restarts in Netwon iterator :", NoOfRestarts_t,
			};
			
			message = "Enter Continuation Parameters";
			title 	= "Advanced Settings";	 	
			option = JOptionPane.showConfirmDialog(null, Parameters2, 
				title, JOptionPane.OK_CANCEL_OPTION);
				
			if (option == JOptionPane.OK_OPTION){
				int 		maxIter 				= Integer.parseInt(maxIter_t.getText());
    		int 		eulerStep 			= Integer.parseInt(eulerStep_t.getText());
				double 	finiteDiffStep	= Double.parseDouble(finiteDiffStep_t.getText());
    		int 		ConvergenceRange= Integer.parseInt(ConvergenceRange_t.getText());
    		int 		NoOfRestarts 		= Integer.parseInt(NoOfRestarts_t.getText());
    		EFNCp.setMaxIter(maxIter);
    		EFNCp.setEuler(eulerStep);
      	//EFNCp.setStepNum(eulerStep);
    		EFNCp.setFD(finiteDiffStep);
    		EFNCp.setRANGE(ConvergenceRange);
    		EFNCp.setNUMOFRESTARTS(NoOfRestarts);
   		}
   		
   		// Branch Switching 
			message = "Inlcude branch switching? (default no)";
			title   = "Continuation Option";	 
			answered = JOptionPane.showConfirmDialog(null, message, 
					title, JOptionPane.YES_NO_OPTION);
			if (answered == JOptionPane.YES_OPTION)
				EFNCp.setBranchSwitching(true);
   		
   		// Damped Newton Iterator
			message = "Use a damped Newton method? (default no)";
			title   = "Continuation Option";	 
			answered = JOptionPane.showConfirmDialog(null, message, 
					title, JOptionPane.YES_NO_OPTION);
			if (answered == JOptionPane.YES_OPTION)
				EFNCp.setDAMP(true);
		}
		
	}	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// NetLogo Button - define the path to the NetLogo code
	////////////////////////////////////////////////////////////////////////////////////////////
	private void netlogoButtonActionPerformed(java.awt.event.ActionEvent evt) {			
							
  	String inputValue = null;
  	while (inputValue==null)
		{
		inputValue = JOptionPane.showInputDialog("Please input the path to the model"); 
		
		if (inputValue != null){
		 modelPath = inputValue;
			JOptionPane.showMessageDialog(null, modelPath, 
				"Model location", JOptionPane.INFORMATION_MESSAGE); 
			NetlogoPathDefined = true;
		} else {
		
			JOptionPane.showMessageDialog(null, "Must define path to model!", 
				"Error", JOptionPane.ERROR_MESSAGE); 
			}
		}
		
	}	

				
	////////////////////////////////////////////////////////////////////////////////////////////					  
	// Measure Button - output parameter curves and realisation distibutions
	////////////////////////////////////////////////////////////////////////////////////////////
	private void measureButtonActionPerformed(java.awt.event.ActionEvent evt) {		
	
		int n = 0; // number of measurements in the models
		
		String inputValue = null; 
		while (inputValue == null){
		
		inputValue = JOptionPane.showInputDialog
			("Please input the number of system measurements (e.g. age and weight)"); 
		
		if (inputValue != null){
		
			// variable number of inputs in the JPannel
			n = Integer.parseInt(inputValue);	
			JTextField[] name  = new JTextField[n];
			systemMeasures = new String[n];
			JPanel myPanel = new JPanel();
			myPanel.setLayout(new GridLayout(n,2,1,1)); //3,3 are gaps
			MeasuresDefined = true;
			
			// add inputs to values
			// name is lift operation and value is Xinitial
			for (int i=0; i<n; i++){
				name[i]  = new JTextField("",40);
				myPanel.add(name[i]);
				//myPanel.add(Box.createHorizontalStrut(15));
			}
			
			// open the pop-up box with input areas
			JOptionPane.showMessageDialog(null, myPanel, "Measurements of the system",
			 		JOptionPane.DEFAULT_OPTION);
	  

			// Check with user - format data as a table 
			Object rowData[][]  = new Object[n][1];
			for (int i=0; i<n; i++){
				rowData[i][0] = name [i].getText();
				systemMeasures[i] = name[i].getText();
			}
			Object columeName[] = {"Macroscopic measures of the system " };
			JTable measureTable = new JTable(rowData, columeName);
			// output the table
			JFrame frame = new JFrame("Summary");
			JScrollPane scrollPane = new JScrollPane(measureTable);
			frame.add(scrollPane, BorderLayout.CENTER);
			frame.setSize(300, 150);
			frame.setVisible(true);
			
		}
		
		if (inputValue == null ) {
				JOptionPane.showMessageDialog(null, "You must define a macroscopic measure!", 
						"Error", JOptionPane.ERROR_MESSAGE); 
		} 
		}
		
	}

	
	////////////////////////////////////////////////////////////////////////////////////////////
	// Initial X Button - output parameter curves and realisation distibutions
	////////////////////////////////////////////////////////////////////////////////////////////
	private void initialXButtonActionPerformed(java.awt.event.ActionEvent evt) {		
		
		String inputValue = null;
		int n = 0;
		
		while (inputValue ==null){
		inputValue = JOptionPane.showInputDialog("Please input the number of initial values in the system");
		if ( inputValue != null ) {
			n = Integer.parseInt(inputValue);
			JTextField[] name  = new JTextField[n];
			JTextField[] value = new JTextField[n];
			initialValues = new String[n];
			LiftVariables  = new String[n]; 
			JPanel myPanel = new JPanel();
			myPanel.setLayout(new GridLayout(n,2,1,1)); //3,3 are gaps
			InitialStateDefined = true;
 			
			// name is lift operation and value is Xinitial
			for (int i=0; i<n; i++){
				name[i]  = new JTextField("",40);
				value[i] = new JTextField("",40);
				myPanel.add(name[i]);
				myPanel.add(value[i]);
				//myPanel.add(Box.createHorizontalStrut(15));
 			}
 			
 			// open the pop-up box with input areas
			JOptionPane.showMessageDialog(null, myPanel, "Initial state of the system",
			 		JOptionPane.DEFAULT_OPTION);
	  

			// Check with user - format data as a table 
			Object rowData[][]  = new Object[n][2];
			for (int i=0; i<n; i++){
				rowData[i][0] = name [i].getText();
				rowData[i][1] = value[i].getText();
				LiftVariables[i] = name [i].getText();
				initialValues[i] = value[i].getText();
			}
			Object columeName[] = {"Variables ", "Initial value" };
			JTable measureTable = new JTable(rowData, columeName);
			// output the table
			JFrame frame = new JFrame("Summary");
			JScrollPane scrollPane = new JScrollPane(measureTable);
			frame.add(scrollPane, BorderLayout.CENTER);
			frame.setSize(300, 150);
			frame.setVisible(true);
 			
 				
		} else {
			
			JOptionPane.showMessageDialog(null, "Must have at least one input value!", "ERROR! ",
				JOptionPane.ERROR_MESSAGE);
		
		}
		}
		
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	// Generate File Button - create the input file continutationParameters.Java 
	////////////////////////////////////////////////////////////////////////////////////////////
	private void createParamFileButtonActionPerformed(java.awt.event.ActionEvent evt) {	
		Writer test = null;
		
		if (
			MeasuresDefined     == false ||
			ModelParamDefined   == false ||
			EFNCParamDefined    == false ||
			NetlogoPathDefined  == false ||
			InitialStateDefined == false
		){
		
		JOptionPane.showMessageDialog(null, "Must press buttons above first", "Error",
					JOptionPane.ERROR_MESSAGE);
		} else {
		
	

  	try{
			test = new BufferedWriter(new OutputStreamWriter(
			new FileOutputStream("../codes/ContinuationParameters.java"),"utf-8"));
  			
  		test.write("// parameters used in continuation\n");
  		test.write("class ContinuationParameters {\n");
  		test.write("\t// ================================\n" + "\t//continuation parameters \n" + "\t// ================================\n");
  		test.write("\tstatic int realisations 			= "+ EFNCp.realisations + ", \t// number of microscopic simualtions to run\n");
  		test.write("\t\tmaxIter = " + EFNCp.maxIter +",	\t// Max Newton Iterations \n");
  		test.write("\t\tstepNum = " + EFNCp.stepNum + ", \t// Steps in Euler ODE solver \n");
  		test.write("\t\tNumContSteps = " + EFNCp.NumContSteps + ",\t// number of continaution steps \n");
  		test.write("\t\tnormNum = " + EFNCp.normNum + ";\t// type of norm (1=abs, 2=Euclidean) \n");
  		test.write("\n");
  		test.write("\tstatic double timeHorizon = " + EFNCp.timeHorizon + ",\t// time evolution (tau)\n");
  		test.write("\t\tdt = " + EFNCp.dt + " , \t// step size in the Euler solver\n");
  		test.write("\t\tsecantTol = " + EFNCp.secantTol + " , \t// tolerance for Newton Corrector \n");
  		test.write("\t\tnewtonTol = " + EFNCp.secantTol + " , \t// tolerance for initial fixed points \n");
  		test.write("\t\tlambda = " + EFNCp.lambda + " , \t// initial value in damped Newton corrector	x += lambda*h \n");
  		test.write("\t\tcontinuationStep = " + EFNCp.continuationStep + " , \t// Step size for continuation (ds) \n");
  		test.write("\t\thFD = " + EFNCp.hFD + " , \t// stepsize for finite differencing \n");
  		test.write("\t\tcontStepInitial = " + EFNCp.contStepInitial + " ; \t// Initial ds, sign determines direction of continuation \n");
  		test.write("\t \n");
  		test.write("\t\tstatic boolean DAMP = " + EFNCp.DAMP + " ; \t// Use damped Newton Method in the Predictor-Corrector?\n");
  		test.write("\t\tstatic boolean BranchSwitching = " + EFNCp.BranchSwitching + 
  						" ; \t// search for other branhces at bifurcation points after continuation? \n");
  		test.write("\t\tstatic int RANGE = " + EFNCp.RANGE + " ; \t// multiple of ds or stdErr for corrector convergence window	\n");
  		test.write("\t\tstatic int NUMOFRESTARTS = " + EFNCp.NUMOFRESTARTS + " ; \t// number of restarts to use in the corrector method if outside of convergence window \n");
  		test.write("\t\tstatic int STATISTIC = " + EFNCp.STATISTIC + " ; \t// 0=Gaussian Approximation, 1=bootstrap estimate\n");
  		test.write("\n \n");
  		
  		test.write("\t// ================================\n" + "\t//system exploration phase parameters \n" + "\t// ================================\n");
  		test.write("\t\tstatic boolean testing = " + testing + " ; \t// run algorithm in testing phase (true) \n");
  		test.write("\t\tstatic int MaxIter = " + MaxIter + " ; \t// Number of iterations \n");
  		test.write("\t\tstatic int Nmax = " + Nmax + " ; \t// maximum number of realisations \n");
  		test.write("\t\tstatic int EulerSteps = " + EulerSteps + " ; \t// number of Euler steps \n");
  		test.write("\t\tstatic int NumOfRuns = " + NumOfRuns + " ; \t// number of runs to take averages \n");
  		test.write("\t\tstatic int IntReals = " + IntReals + " ; \t// initial number of reailisations	 \n");
  		test.write("\t\tstatic int realsInc = " + realsInc + " ; \t// multiplier of N  \n");
  		test.write("\t\tstatic double tauIncrement = " + tauIncrement + " ; \t// multiple of tau \n");
  		test.write("\t\tstatic double desiredTol = " + desiredTol + " ; \t// desired tolerance of fixed point solution (F-x<=tol) \n");
  		test.write("\t\tstatic double tauSpredThreshold = " + tauSpredThreshold + " ; \t// confInterval on variance	 \n");
  		test.write("\n \n");
  		
  		test.write("\t// ================================\n" + "\t//Problem Sepcific parameters \n" + "\t// ================================\n");
  		test.write("\t\tstatic String[] systemParameters = {");
  		test.write( "\"" + parameterName[0] + "\"");
  		for (int i=1; i<parameterName.length; i++){
  			test.write( ", \"" + parameterName[i] + "\"" );
  		}
  		test.write("};\n");
  		test.write("\t\tstatic double[] param = {");
  		test.write( parameterValue[0] );
  		for (int i=1; i<parameterValue.length; i++){
  			test.write( ", " + parameterValue[i] );
  		}
  		test.write("};\n");
  		test.write("\t\tstatic String[] RestrictOperator = {");
  		test.write( "\"" + systemMeasures[0] + "\"");
  		for (int i=1; i<systemMeasures.length; i++){
  			test.write( ", \"" + systemMeasures[i] + "\"");
  		}
  		test.write("};\n");
  		
  		test.write("\t\tstatic String[] LiftOperator = {");
  		test.write(  "\"" + LiftVariables[0]+ "\"");
  		for (int i=1; i<LiftVariables.length; i++){
  			test.write( ", \"" + LiftVariables[i] + "\"" );
  		}
  		test.write("};\n");
  		test.write("\t\tstatic double[] xInitial = {");
  		test.write( initialValues[0] );
  		for (int i=1; i<initialValues.length; i++){
  			test.write( ", " + initialValues[i] );
  		}
  		test.write("};\n");
  	
  		test.write("\t\tstatic String NetlogoFile = \"" + modelPath + "\" ; \t// path to  file	 \n");
  		test.write("\t\tstatic boolean isSystemInitialised = " + isSystemInitialised + " ; \t\n");
  		
  		test.write("\t \n");
  		test.write("}\n");
  		
			test.close();  		
				
			JOptionPane.showMessageDialog(null, "File generated successfully! ", 
					" ", JOptionPane.INFORMATION_MESSAGE);
				
		} catch(IOException e) {
			JOptionPane.showMessageDialog(null, "No in file created: " + e, 
					"Error", JOptionPane.ERROR_MESSAGE); 
		} 
	}
	}				
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// Testing Button - run the model in system exploration phase 
	// to determine continuation parameters
	////////////////////////////////////////////////////////////////////////////////////////////
	private void testButtonActionPerformed(java.awt.event.ActionEvent evt) {		
		String title = "Test phase on";
		String message = "If gamma unknown run with a value of 10,000";
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE); 
		// tauSpredThreshold = gamma
		String gammaInput = null;
		boolean warning = false;	// dont warn the user twice
		
		while (gammaInput == null){
		
			gammaInput = JOptionPane.showInputDialog("Please input a value for gamma"); 
			tauSpredThreshold = 0.0;
			if (gammaInput != null){
				warning = false;
			
				try {
					tauSpredThreshold = Double.parseDouble(gammaInput);	
					testing = true;
				} catch (Exception e){
					JOptionPane.showMessageDialog(null, "Gamma must be a number!", 
							"Error", JOptionPane.ERROR_MESSAGE); 
					
					warning = true;
					gammaInput = null;
				}
				
				if (tauSpredThreshold < 0 ) {
					gammaInput = null;
					testing = false;
				}
			}
			if (gammaInput == null && warning == false) {
				JOptionPane.showMessageDialog(null, "Gamma must be positive and non-zero!", 
						"Error", JOptionPane.ERROR_MESSAGE); 
			}  
			
		}
	
	}										

	////////////////////////////////////////////////////////////////////////////////////////////
	// Test Off Button - turing the test phase off
	////////////////////////////////////////////////////////////////////////////////////////////
	private void testOffButtonActionPerformed(java.awt.event.ActionEvent evt) {							
		testing = false;
		JOptionPane.showMessageDialog(null, "System exploration phase turned off", 
					"Information", JOptionPane.INFORMATION_MESSAGE); 
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// LIFT Button - define the path to the NetLogo code
	////////////////////////////////////////////////////////////////////////////////////////////
	private void LiftButtonActionPerformed(java.awt.event.ActionEvent evt) {			
					
					// button not needed !!! 
					/*		
  	modelPath = JOptionPane.showInputDialog("Please define the Lift operation for the model"); 
		if (modelPath == null){
			JOptionPane.showMessageDialog(null, "Must define Lift to perform EFNC!", 
				"Error", JOptionPane.ERROR_MESSAGE); 
		} else {
			JOptionPane.showMessageDialog(null, modelPath, 
				"Lift Operation", JOptionPane.INFORMATION_MESSAGE); 
		}
		
		*/
		
	}	
	
	
	////////////////////////////////////////////////////////////////////////////////////////////
	// Compilation Button - compile the EFNC code
	////////////////////////////////////////////////////////////////////////////////////////////
	private void compileButtonActionPerformed(java.awt.event.ActionEvent evt) {										 
		try {
			System.out.println("Compiling ... ");
			
			runProcess("javac -cp " +
								 "../lib/NetLogo.jar:../lib/scala-library.jar:../lib/Jama-1.0.3.jar " +
								 "../codes/MacroOperator.java " +
								 "../codes/ContinuationParameters.java " +
								 "../codes/Stats.java " +
								 "../codes/GaussianElimination.java " +
								 "../codes/MacroDescription.java " +
								 "../codes/NewtonRaphson.java " +
								 "../codes/FunctionType.java " +
								 "../codes/FixedPoint.java " +
								 "../codes/Bootstrap.java " +
								 "../codes/ContinuationSecant.java"
			);
			
			System.out.println("Compiliation Complete!");
		} catch (Exception e) {
			Logger.getLogger(EFNC_GUI.class.getName()).log(Level.SEVERE, null, e);
			e.printStackTrace();
			System.out.println("Compiliation Failed");
		}
	}

	// Methods to run a console process from a Java code						  
  private static void runProcess(String command) throws Exception {
		Process pro = Runtime.getRuntime().exec(command);
   	printLines(command, pro.getInputStream());
		printLines(command, pro.getErrorStream());
		pro.waitFor();
	 }


	////////////////////////////////////////////////////////////////////////////////////////////
	// Run Button	- run the EFNC code
	////////////////////////////////////////////////////////////////////////////////////////////
	private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {									 
		try {
			JFrame frame = new JFrame("Running EFNC...");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(300, 150);
			frame.setVisible(true);
			
			runProcess("java -Xmx2G -cp :../lib/NetLogo.jar:../lib/scala-library.jar:../lib/picocontainer-2.13.6.jar:../lib/asm-all-3.3.1.jar:../lib/Jama-1.0.3.jar:../codes/ -Djava.library.path=lib ContinuationSecant");			
		} catch (Exception e) { 
			e.printStackTrace();
			Logger.getLogger(EFNC_GUI.class.getName()).log(Level.SEVERE, null, e);
			System.out.println("Running Programme Failed"); 
		} 
	}	

/*	
	// Run Button	- run the EFNC code
	private Future<?> taskFuture = null; // class variable to store the future of your task
	public void actionStart(String[] command) { // start the thread
		if (taskFuture == null || taskFuture.isDone()){	// only run once
			MyShellExecutor ex = new MyShellExecutor(command); 
			ExecutorService newThreadExecutor = Executors.newSingleThreadExecutor();
			taskFuture = newThreadExecutor.submit(ex);
		}
	}
	public void actionStop(){	// stop the thread
		// if it is not finished or calready cancelled, then cancel it
		if(taskFuture!=null && taskFuture.isDone()) {
			taskFuture.cancel(true);
		}
	}
	private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {				
			// Running EFNC message
			JFrame frame = new JFrame("Running EFNC code...");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(300, 150);
			frame.setVisible(true);
	
			String[] runCommand = {
				"java -Xmx2G -cp :../lib/NetLogo.jar:../lib/scala-library.jar:../lib/picocontainer-2.13.6.jar:../lib/asm-all-3.3.1.jar:../lib/Jama-1.0.3.jar:../codes/ -Djava.library.path=lib ContinuationSecant"
			};
			actionStart(runCommand); 
	}
	*/


	////////////////////////////////////////////////////////////////////////////////////////////
	// Graph Button - output parameter curves and realisation distibutions
	////////////////////////////////////////////////////////////////////////////////////////////
	private void graphButtonActionPerformed(java.awt.event.ActionEvent evt) {										 
  	
  	Writer test = null;
  	//System.out.println(systemMeasures.length);
  	if ( MeasuresDefined == false ){ 

  		JOptionPane.showMessageDialog(null, "Measurements have not been defined! ", 
						"Error", JOptionPane.ERROR_MESSAGE);
						
  	} else {
  	
  		int n = systemMeasures.length;
			try{
				test = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("GNUplotScript.plot"),"utf-8"));
  			
  			test.write("# Equation-free numerical continuation curve\n");
  			test.write("# Produced by EFNC 2015 written by Spencer Angus Thomas\n");
				test.write("reset\n");
				test.write("set term postscript enhanced color solid lw 3\n");
				test.write("set output \"continuation.eps\"\n");
				test.write("set key left\n");
				test.write("set xl \"Bifurcation Parameter\" \n");
				test.write("set yl \"Solution\"\n");
				test.write("set style fill  solid 0.25\n\n");

				for (int i=0; i<n; i++){
					test.write("plot \"outputfiles/output.txt\" u 1:($" + (2+i*9) + 
										"-$" + (3+i*9) + "*2):($" + (2+i*9) + "+$" + (3+i*9) + 
										"*2) ti \"+/- 2stderr\" w filledcurves lt 1, \"\" u 1:" + 
										(2+i*9) + " noti w lp lt 1\n\n");
				}
				test.close();  		
				
			} catch(IOException e) {
				JOptionPane.showMessageDialog(null, "No plot file created " + e, 
						"Error", JOptionPane.ERROR_MESSAGE); 
			}
			try {
			
			runProcess("gnuplot GNUplotScript.plot" );
			
		} catch (Exception e) {
			Logger.getLogger(EFNC_GUI.class.getName()).log(Level.SEVERE, null, e);
			e.printStackTrace();
			System.out.println("Compiliation Failed");
		}
  	}  
	}						



	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
		/* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		*/
		
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : 
				javax.swing.UIManager.getInstalledLookAndFeels()) {
				
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(EFNC_GUI.class.getName())
				.log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(EFNC_GUI.class.getName())
				.log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(EFNC_GUI.class.getName())
				.log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(EFNC_GUI.class.getName())
				.log(java.util.logging.Level.SEVERE, null, ex);
		}

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new EFNC_GUI().setVisible(true);
			}
		});
	}

	// Variables declaration				  
	private javax.swing.JButton testButton;
	private javax.swing.JButton testOffButton;
	private javax.swing.JButton runButton;
	private javax.swing.JButton modelButton;
	private javax.swing.JButton compileButton;
	private javax.swing.JButton measureButton;
	private javax.swing.JButton createParamFileButton;
	private javax.swing.JButton graphButton;
	private javax.swing.JButton netlogoButton;
	private javax.swing.JButton continuationButton;
	private javax.swing.JButton initialXButton;
	private javax.swing.JButton LiftButton;
	// End of variables declaration				   
}

class EFNCparameters {

		// contructor to initialise all values
		
		public EFNCparameters(){}
			int realisations 			= 10,								// number of microscopic simualtions to run
					numOfMoments 			= 1, 								// number of moments to use on continuation (limited to 1 atm)
					maxIter 					= 10,								// Max Newton Iterations
					stepNum 					= 1, 								// Steps in Euler ODE solver
					NumContSteps 			= 100,							// number of continaution steps
					normNum						=	2;								// type of norm (1=abs, 2=Euclidean)
			 		
			double timeHorizon 				= 1.0,							// time evolution (tau)
						 dt 								= 1.0, 							// step size in the Euler solver
							secantTol 					= 1e-2,							// tolerance for Newton Corrector //timeHorizon / Math.sqrt( (double) realisations ),
				 newtonTol 					= secantTol, 				// tolerane for initial fixed points used for secant predicter
				 lambda 						= 1.0,							// initial value in damped Newton corrector	x += lambda*h
				 continuationStep 	= 0.1,							// Step size for continuation (ds)
				 hFD 								= continuationStep/2.0, // stepsize for finite differencing
				 contStepInitial		= -continuationStep; // Initial ds, sign determines direction of continuation																							
				boolean DAMP							 = false;					// Use damped Newton Method in the Predictor-Corrector? 					
				boolean BranchSwitching		 = false;					// search for other branhces at bifurcation points after continuation?			
					int		  RANGE							 = 5;							// multiple of ds or stdErr for corrector convergence window	
					int		  NUMOFRESTARTS			 = 5;							// number of restarts to use in the corrector method if outside of convergence window	
					int		 STATISTIC					 = 1;							// type of statistics 0=normal stderr, 1=bootstrap resampling; 
	
		
		// set values
		void setRealisations(int realisations){
			this.realisations = realisations;
		}
		
		void setNumOfMoments(int numOfMoments){
			this.numOfMoments = numOfMoments;
		}
		
		void setMaxIter(int maxIter){
			this.maxIter = maxIter;
		}
		
		void setStepNum(int stepNum){
			this.stepNum = stepNum;
		}
		
		void setNumContSteps(int NumContSteps){
			this.NumContSteps = NumContSteps;
		}
		
		void setNormNum(int normNum){
			this.normNum = normNum;
		}
		
		void setTimeHorizon(double timeHorizon){
			this.timeHorizon = timeHorizon;
		}
		
		void setEuler(double dt){
			this.dt = dt;
		}
		
		void setSecantTol(double secantTol){
			this.secantTol = secantTol;
		}
		
		void setNewtonTol(double newtonTol){
			this.newtonTol = newtonTol;
		}
		
		void setLambda(double lambda){
			this.lambda = lambda;
		}
		
		void setContinuationStep(double continuationStep){
			this.continuationStep = continuationStep;
		}
		
		void setFD(double hFD){
			this.hFD = hFD;
		}
		
		void setContStepInitial(int coefficient, double step){
			this.contStepInitial = coefficient * step;
		}

		void setDAMP(boolean DAMP){
			this.DAMP = DAMP;
		}
		
		void setBranchSwitching(boolean BranchSwitching){
			this.BranchSwitching = BranchSwitching;
		}
		
		void setRANGE(int RANGE){
			this.RANGE = RANGE;
		}
		
		void setNUMOFRESTARTS(int NUMOFRESTARTS){
			this.NUMOFRESTARTS = NUMOFRESTARTS;
		}
		
		void setSTATISTIC(int STATISTIC){
			if (STATISTIC==0 || STATISTIC==1)
				this.STATISTIC = STATISTIC;
			else 
				this.STATISTIC = 1;
		}
}
