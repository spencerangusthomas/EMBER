/*
Note: this runs each realisation sequentially, to the full extent of the time simulation, as running them simultaneously requires FAR too much memory (running 1000 netlogo models in parallel). Therefore I decided to go with the time efficient approach (here), which comes at the cost of knowing the statistics on the fly. Previously we ran 1000 simulations for a time interval, calculated the statistics, then restarted 1000 simulations with a larger time interval (starting from zero). This was very timer inefficient, but provided feedback on the fly. 
*/

//package org.apache.commons.lang.ArrayUtils;
//import org.apache.commons.lang3.ArrayUtils;
import java.io.*;
import Jama.*;
// NetLogo libraries
import org.nlogo.api.LogoList;
import org.nlogo.api.Turtle;
import org.nlogo.headless.HeadlessWorkspace;


public class ConfigurationPhase extends ContinuationParameters
{
	
	// Class to configure the equation-free parameters
	// to a specific model given in ContinuationParameters
	public ConfigurationPhase(){}
	
	
	public void InitialParameters(){
		
		// dummy objects for methods, not required here 
		String[] files = {"null", "null", "null"};
		
		System.out.println(
				"\n################################################################" 	+
				"\n# Testing Phase - Potentially long runtimes!" 											+ 
				"\n################################################################" 
		);
			
		double[] tauVsVariance = timeHorzionPredictor();	
		
		// maximum variance of all order parameters 
		double maxVaraince= 0.0;
		System.out.println(" ");
		for (int i=1; i<tauVsVariance.length; i++){
			if (tauVsVariance[i] > maxVaraince)
				maxVaraince = tauVsVariance[i];
			System.out.print(	RestrictOperator[i] + " variance = " + tauVsVariance[i] + "\n" );
		}
		System.out.println(" ");
		

		double M = (maxVaraince * maxVaraince) / (realisationTolerance * realisationTolerance); 
		System.out.println("Minium realisations is: " + M + " (based on tolerance = " + realisationTolerance + ")" );

	
		double DS = 5.0 * Math.sqrt(maxVaraince);
		System.out.println("Step in parameter space is: " + DS + " (based on variance of = " + maxVaraince + ")");


	}
	
	// time horizon (based on the spread of variance over multiple runs)
	// need to run each realisation sequentially for the time interval 
	// as a vector of simulations is too memory intensive
	// can either have statistics on the fly as time increases (but restart simialtion at each time interval)
	// or continuous time simualtions for ecah simulation (time efficieint) but can calculate stats until all realisations are complete. 
	// for now use the later
	public double[] timeHorzionPredictor(){
	
		int	orderNumber	= xInitial.length;
		double[][][] Phi = new double[MaxIter][orderNumber][IntitialRealisations];
		
		// class containing bootstrap statistic estimator method
		Bootstrap boot 		= new Bootstrap();
		double[] mean     = new double[orderNumber];
		double[] variance = new double[orderNumber];
		
		// output files 
		Writer test = null;	
		Writer dist = null;	
		try {
			test = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("outputfiles/ConfigurationPhase-tau.txt"), "utf-8"));
				
			test.write( "#Test phase: time horizon prediction " + "\n");
			
			dist = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("outputfiles/ConfigurationPhase-distribution.txt"), "utf-8"));
				
			dist.write( "#Test phase: distribution of realisations at each time horizon value " + "\n");
			
			// vector to retain the time at each step
			// and to avoid if statement in deep nested for loops
			double[] timeMonitor = new double[MaxIter]; 
 			timeMonitor[0] = tauStart;
 			for (int i=1; i<MaxIter; i++){
 				if (tauIterator==0){
					timeMonitor[i] = tauIncrement + timeMonitor[i-1];
				} else {
					timeMonitor[i] = tauIncrement * timeMonitor[i-1];
				}	
			}
 	
 			// so only evaluate once rather than for each realisation and time step
      double[] intitialValue = xInitial;
      if (isSystemInitialised==true){
      	for (int i=0; i<orderNumber; i++)
      		intitialValue[i] = 0.0; 	
      }
			
			// NetLogo simualtor		
			HeadlessWorkspace simulator = null;
			try{
		
		
			  // create simulator
				String scenarioFile = NetlogoFile;
      	simulator = HeadlessWorkspace.newInstance();
      	simulator.open(scenarioFile);
     
    		
     	 		// set order parameters
      		for (int i=0; i<orderNumber; i++){
      			simulator.command( LiftOperator[i] + xInitial[i] );  
      		}
 	
      		// set system parameters
      		for (int i=0; i<systemParameters.length; i++){		
      			simulator.command( systemParameters[i] + param[i] );  
     			}		
 
  		// Run the simulation and get the output and model details
  			for (int k=0; k<IntitialRealisations; k++){  	
			    
			    System.out.println( "Realisation " + (k+1) + " of " + IntitialRealisations);			
			    
      		// set up initial states for all continuation orders	
  				for (int i=0; i<orderNumber; i++)
	  				for (int j=0; j<MaxIter; j++)
			     		Phi[j][i][k] = intitialValue[i]; 
		     	
					simulator.command("setup");  		
							
					for (int j=0; j<MaxIter; j++){			
						simulator.command("repeat " + timeMonitor[j] + "[ go ]");
							
						//System.out.print( timeMonitor[j] + "\n");
						
						for (int i=0; i<orderNumber; i++){
	  	  	 		Phi[j][i][k] = dt * (double) (Double) simulator.report( RestrictOperator[i] ) ;
	  	  	 	}				
  				}	
  	  	}
  /*
  Works but gives an empty list error at some point during the computation
  most likely a result of the resitrict operator so check and out it a condition 
  for an empty list 
  */	  	
  	  	
  	
  	  	// now can output the statistics for each time step
		  	test.write("#timeHorizon\t");
  	  	for (int i=0; i<orderNumber; i++)
			  	test.write(RestrictOperator[i] + "\tstdErr\tstdDev\tvariance\t");
  	  	test.write ( "\n" );
  	  	
  	  	for (int j=0; j<MaxIter; j++){	
					test.write( timeMonitor[j] + "\t");
					
					// out put for the tau  predictor
  	  		for (int i=0; i<orderNumber; i++){
  					// Bootstrap the sample
  	  		  boot.getBootstrap( 200, Phi[j][i] );
						mean[i] = boot.mean;
						variance[i] = boot.variance;
			
						//	System.out.print( boot.mean + "\t" + boot.standardError + "\t" +
						//										boot.standardDeviation + "\t" + boot.variance + "\t");	
						test.write( boot.mean + "\t" + boot.standardError + "\t" + 
												boot.standardDeviation + "\t" + boot.variance + "\t");	
		  		}
		  		
		  		// output for the distributions
		  		dist.write("# time horizon = "+ timeMonitor[j] + "\n#");
		  		for (int i=0; i<orderNumber; i++)
			  		dist.write(RestrictOperator[i] + "\t");
  	  		dist.write ( "\n" );
		  		for (int k=0; k<IntitialRealisations; k++){  
  	  			for (int i=0; i<orderNumber; i++){
  	  				dist.write ( Phi[j][i][k] + "\t" );
  	  			}
  	  			dist.write ( "\n" );
  				}
  	  		dist.write ( "\n\n" );	// for plotting in gnuplot
		  			
					//System.out.print("\n");
					test.write("\n");
					test.flush(); // flush so do not lose any information if crashes. 
					dist.flush(); // flush so do not lose any information if crashes. 	
				}	
		
			// reporting per simulation ends here
			} catch (Exception e) {
    		System.out.println("Error running simulation: " + e);
    	} finally {
    		try {
      		if(simulator != null) simulator.dispose();
      	} catch (Exception e) {
      		System.out.println("Error stopping simulation: " + e);
      	}
    	}	
		} catch (IOException e) {
			System.out.println(e);
			System.out.println("\nCould not open file. Problem in Test Phase of Code\n");
		} finally {
			try {
				test.flush();
				test.close();
			} catch (Exception ex) {}
		}
		
		return variance;
	}	
	/*
	// working ersion but does each realistion 0-maxiter intern so stats on only one run not all
	public double[] timeHorzionPredictor(){
	
		int	orderNumber	= xInitial.length;
		double[][] Phi = new double[orderNumber][IntitialRealisations];
		
		// class containing bootstrap statistic estimator method
		Bootstrap boot 		= new Bootstrap();
		double[] mean     = new double[orderNumber];
		double[] variance = new double[orderNumber];
		// output files 
		Writer test = null;	
		try {
			test = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("outputfiles/ConfigurationPhase.txt"), "utf-8"));
				
			test.write( "#Test phase: time horizon prediction " + "\n");
			int iteration=0;
			
			// NetLogo simualtor		
			HeadlessWorkspace simulator = null;
			try{
				// create simulator
				String scenarioFile = NetlogoFile;
      	simulator = HeadlessWorkspace.newInstance();
      	simulator.open(scenarioFile);
      	
				double meanPhi=0.0;
 
      	// Run the simulation and get the output and model details
  			for (int k=0; k<IntitialRealisations; k++){  

     	 		// set order parameters
      		for (int i=0; i<orderNumber; i++){
      			simulator.command( LiftOperator[i] + xInitial[i] );  
      		}
 	
      		// set system parameters
      		for (int i=0; i<systemParameters.length; i++){		
      			simulator.command( systemParameters[i] + param[i] );  
     			}		
     	 			
      		// set up initial states for all continuation orders
  				for (int i=0; i<orderNumber; i++){
		     		if (isSystemInitialised==true) Phi[i][k] = 0.0; 	
		     		else 													 Phi[i][k] = xInitial[i]; 
		     	}
				
					double time = tauStart;
					simulator.command("setup");  		
						
					// run each simualtion time batches
					//do {	
				//	for (int zz=0; zz<; j++){			
						for (int j=0; j<MaxIter; j++){			
							simulator.command("repeat " + time + "[ go ]");
							
							System.out.print( time + "\t");
							test.write( time + "\t");
				
							for (int i=0; i<orderNumber; i++){
	  	  	 			Phi[i][k] += dt * (double) (Double) simulator.report( RestrictOperator[i] ) ;
	  	  	 				
	  	  	 			boot.getBootstrap(200, Phi[i]);
								mean[i] = boot.mean;
								variance[i] = boot.variance;
			
							//	System.out.print( boot.mean + "\t" + boot.standardError + "\t" +
							//										boot.standardDeviation + "\t" + boot.variance + "\t");	
								test.write      ( boot.mean + "\t" + boot.standardError + "\t" + 
																	boot.standardDeviation + "\t" + boot.variance + "\t");	
		  				}
		  					
							//System.out.print("\n");
							test.write("\n");
							test.flush(); // flush so do not lose any information if crashes. 
										
							if (tauIterator==0){
								time += tauIncrement;
							} else {
								time *= tauIncrement;
							}		
  					}	
  					
  					//iteration++;
					//} //while (iteration < MaxIter);
  	  	}
			// reporting per simulation ends here
			} catch (Exception e) {
    		System.out.println("Error running simulation: " + e);
    	} finally {
    		try {
      		if(simulator != null) simulator.dispose();
      	} catch (Exception e) {
      		System.out.println("Error stopping simulation: " + e);
      	}
    	}	
		} catch (IOException e) {
			System.out.println(e);
			System.out.println("\nCould not open file. Problem in Test Phase of Code\n");
		} finally {
			try {
				test.flush();
				test.close();
			} catch (Exception ex) {}
		}
		
		return variance;
	}
*/    
    /*
  // The following methods are no longer used and have replaced with equations at the end of InitialiseParameters method 
    
    
  // number of realisations 
	public int RealisationPredictor(double[] PredictedTau, double[] x, double[] p, String files[])
	{
	
		MacroState 	b 				= new MacroState();
		int 		N 						= IntReals; 
		int 		suggestedN 		= IntReals;
		int			orderNum			= x.length;
		boolean	exit					= false;
		// from timeHorzionPredictor method
		double 	tau 					= PredictedTau[0]; 
		double 	threshold 		= PredictedTau[1]; 
		
		// list of lists -> orderNum of lists each with expandable list of realisations
		List<List<Double>> ensemble = new ArrayList<List<Double>>();
		for (int i=0; i<orderNum; i++)
			ensemble.add(new ArrayList<Double>());
		
		//List of Double[] where each element in Double[] is an order of the system.
		//List<Double[]> ensemble = new ArrayList<Double[]>;
		
		Bootstrap boot = new Bootstrap();
		double[] mean     = new double[orderNum];
		double[] stdError = new double[orderNum];
		int size = N;
		
		Writer test = null;
		try{
			test = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("outputfiles/SystemTest_RealisationPrediction.txt"),"utf-8"));
		
			test.write("Test phase: realisation prediction " + "\n");
			test.write("Time horizon: " + PredictedTau[0] + " Threshold: " + PredictedTau[1] + "\n");
			System.out.println("Time horizon: " + PredictedTau[0] + " Threshold: " + PredictedTau[1] );
		
			// Realisation Prediction Loop	
			do {
				MacroDescription l = new MacroOperator(N);
				l.Lift(x, p, EulerSteps, tau);
				double[][] LiftAverage = l.getPhi();
				
				test.write(N + "\t");
				System.out.print(N + "\t");
				
				for (int i=0; i<orderNum; i++){
					
					// asList() does not work with primatives 
					// convert double[] to Double[] then  Double[] to List<Double>
					Double[] DoubleLiftAverage = new Double[ LiftAverage[i].length ];
					for (int j=0; j<LiftAverage[i].length; j++)
						DoubleLiftAverage[j] = (Double) LiftAverage[i][j] ;
						
					// covert array to fixed list using asList()
					List<Double> temp = Arrays.asList(DoubleLiftAverage);
					// copy fixed size list to variable size list 
					ensemble.get(i).addAll(temp); 
					
					// copy all elements of List to Double[] for bootstrap calculation
					Double[] currentDist = new Double[ensemble.get(i).size()];
					currentDist = ensemble.get(i).toArray(currentDist);
					size = currentDist.length; // maintain the size of current sample 
					
					boot.getBootstrap(200, currentDist);	// bootstrap the data
					mean[i]     = boot.mean;
					stdError[i] = boot.standardError;
					
					test.write(mean[i] + "\t" + stdError[i]  + "\t");
					
					System.out.print(DoubleLiftAverage.length + "\t" + currentDist.length + "\t" + mean[i] + "\t" + stdError[i]  + "\t");
					if (stdError[i] < threshold)		//if (varianceCLT[k] < threshold)
						exit=true;
				}
				test.write("\n");
				System.out.print("\n");
	
				if (N < Nmax && exit==false){
					N *= realsInc;
					 
				}
		
			} while ( N < Nmax && exit==false );
			
			suggestedN = (int) roundToSignificantFigures(N, 2); 	// round to the nearest 2 sigfigs
			System.out.println("The recommended number of realisations is " + 
				N + "\t" + (int) roundToSignificantFigures(N, 2) +"\n"); 
			test.write("The recommended number of realisations is " + 
				N + "\t" + (int) roundToSignificantFigures(N, 2) +"\n");
		
			
	
			if (suggestedN >= Nmax){
				System.out.println("\n************************************");
				System.out.println("WARNING: high number of realisations");
				System.out.println("N="+suggestedN+" may lead to large run times");
				System.out.println("Perhaps consider a higher tolerance?");
				System.out.println("Switching to defualt number of realisations 1000");
				System.out.println("************************************");	
				suggestedN = 1000;
				test.write("\n************************************\n");
				test.write("WARNING: high number of realisations\n");
				test.write("N="+suggestedN+" may lead to large run times\n");
				test.write("Perhaps consider a higher tolerance?\n");
				test.write("Switching to defualt number of realisations 1000\n");
				test.write("************************************\n\n");	
			}
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			try{
				test.flush();
				test.close();
			} catch (Exception ex) {}
		}	
	
		return suggestedN;
	}
  
 	public double dsPredictor(int N, double[] tau, double[] x, double[] p, String files[])
	{
		double ds = 0.1;	// default value
		Writer test = null;	
		try {
			test = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("outputfiles/SystemTest_dsPrediction.txt"), "utf-8"));	
	
			// initial parameters
			int NumOfOrders = x.length;
			double max = 0.0;
			double[] func  = new double[NumOfOrders];	// Phi-x
			double[] del  = new double[NumOfOrders];	// stderr on x

			MacroDescription l = new MacroOperator(N);
		
			// calculate the stderr on X
			l.Lift(x, p, stepNum, tau[0]);		
			for (int i=0; i<NumOfOrders; i++) {
				func[i] = l.Restrict(i) - x[i];
				del [i] = l.getStandardErr(i);
				test.write( del[i] + "\t");
				System.out.print( del[i] + "\t");
				if(del[i]>max)
					max = del[i]; 		// record largest stderr on order parameters
			}	
			ds = max * 3.0; // 3 stderrs for h in Finite differencing (so not same dist) and ds is 2*h
			System.out.println(" "); 
			System.out.println("Predicted step size for continuation is: " + ds); 
			test.write("\nPredicted step size for continuation is: " + ds + "\n");
			test.flush();
			
		} catch(IOException e) {
			System.out.println(e);
		} finally {
			try{
				test.close();
			} catch (Exception ex) {}
		}	
	
		return ds;
	}
	
	// I dont think these are used anymore	
	public static int roundUp(int x, int f) {
  	return (int) ( (double) f * Math.ceil( (double) x / (double) f) );
	}	

	public static double roundToSignificantFigures(double num, int n) {
    if(num == 0) {
        return 0;
    }

    final double d = Math.ceil(Math.log10(num < 0 ? -num: num));
    final int power = n - (int) d;

    final double magnitude = Math.pow(10, power);
    final long shifted = Math.round(num*magnitude);
    return shifted/magnitude;
	}
	
	public static double mean(double[] xx){
		double sum = 0.0;
		for (int k=0; k<xx.length; k++){
			sum += xx[k];
		}
		return sum/(double)xx.length; 
	}

	public static double variance(double[] xx){
		return Math.pow( getStandardDev(xx), 2 );
	}
		
	public static double standardDev(double[] xx){
		double mean = mean(xx);
		double temp = 0;
		for (double a : xx)
			temp += Math.pow((mean-a),2);
		return Math.sqrt(temp/(double)xx.length);
	}
	
  public static double standardErr(double[] xx){
		return ( getStandardDev(xx) / Math.sqrt((double)xx.length) );
	}	
*/
}