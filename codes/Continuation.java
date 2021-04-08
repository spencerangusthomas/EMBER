import java.util.Random;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.lang.*;
//package org.apache.commons.lang.ArrayUtils;
//import org.apache.commons.lang3.ArrayUtils;
import java.io.*;
import Jama.*;
// NetLogo libraries
import org.nlogo.api.LogoList;
import org.nlogo.api.Turtle;
import org.nlogo.headless.HeadlessWorkspace;

public class Continuation extends ContinuationParameters
{

	public static void main (String[] args) {
		long startTime = System.currentTimeMillis();
		
		if (xInitial.length != LiftOperator.length){
			System.out.println("System not initialised properly!");
	
			System.out.println("Number of order parameters (xInitial) specifed, " + xInitial.length +
			 ", must be the same as number used in NetLogo Code (LiftOperator), " + LiftOperator.length );
			System.out.println("See continuationParamters.java and correct this");
			System.out.println("Exiting Continuation ...");
			return;
		}
		
		if (param.length != systemParameters.length){
			System.out.println("Not correct number of system parameters!");
	
			System.out.println("Number of system parameters (param) specifed, " + param.length +
			 ", must be the same as number used in NetLogo Code (systemParameters), " + systemParameters.length );
			System.out.println("See continuationParamters.java and correct this");
			System.out.println("Exiting Continuation ...");
			return;
		}
		
		System.out.println("System setup");
		for (int i=0; i<LiftOperator.length; i++)
			System.out.println (LiftOperator[i] + " " + xInitial[i] ) ;
		for (int i=0; i<systemParameters.length; i++)
			System.out.println (systemParameters[i] + " " + param[i] ) ;
		System.out.println(" ");
		
		double[] p = param;							// parameter settings for the NetLogo system
    double[] x0 = xInitial;					// initial guess of the fixed point
    double h = hFD; 								// step size for finite differencing
    int NumOfOrders = x0.length;		// orders (number of x's) in the continuation calculation
		MacroState [] b = new MacroState [NumContSteps+numOfPreviousPoints];

		for (int i=0; i<NumContSteps+numOfPreviousPoints; i++){
			b[i] = new MacroState();
			b[i].setSize(NumOfOrders);
		}
			
		
		if (testing==true){
    	
    	// determine initial continuation parameters
    	ConfigurationPhase test = new ConfigurationPhase();
    	test.InitialParameters();
			
			return;
		}	
			
		// open file
		String[] files = new String[5];
		files[0] = "outputfiles/distribution.txt";					// distribution of realisations
		files[1] = "outputfiles/output.txt"; 								// continuation along the bifurcation curve
		files[2] = "outputfiles/newton.txt";								// log file for the Newton iterations
		files[3] = "outputfiles/runtime.txt";								// runtime for the continuation
		files[4] = "outputfiles/JacobianInfo.txt";	
		// write lines to files
		openFile(files[0], "# Distribution of realisations in the lifting stage\n# Phi(u,mu)\tPhi(u,mu)-u\t");
		openFile(files[1], "# Continuation along the bifurcation curve");
		openFile(files[2], "# LOG file for the Newton Iterator");
		openFile(files[3], "# Runtime (seconds) file for continuation");
		openFile(files[4], "# Jacobian for continuation points\n#Jacobian\tEigenValueRe\tEigenValueIm\tEigenVector");
    
    try{ 	   
   		
   		// create colum headings for the output file
   		String fileEntry = "#";
    	for (int i=0; i<NumOfOrders; i++)	{								
				fileEntry += "b.parameter\tb.X[" + Integer.toString(i) + "]\t"
									 + "b.stdErr[" + Integer.toString(i) + "]\t"
									 + "b.stdDev[" + Integer.toString(i) + "]\t"
									 + "b.var[" + Integer.toString(i) + "]\t"
									 + "b.liftAverage[" + Integer.toString(i) + "]\t"
									 + "b.mean[" + Integer.toString(i) + "]\t"
									 + "b.median[" + Integer.toString(i) + "]\t"
									 + "b.solution[" + Integer.toString(i) + "]\t"
									 + "b.eigenvalue[" + Integer.toString(i) + "]\t"
									 + "b.predictedMu\t"
									 + "b.predictedX[" + Integer.toString(i) + "]\t";									
			}
			fileEntry += "iter\tb.size\tb.determinantOfJ\t\n";
			appendFile(files[1], fileEntry);
 		
			File timeFile = new File(files[3]);
			FileOutputStream timeS = new FileOutputStream(timeFile);
			BufferedWriter timeWriter = new BufferedWriter(new OutputStreamWriter(timeS));
			
			
			Predictor predictor = new Predictor();
			double[] xGuess = xInitial;
			double 	 pGuess = param[0];
			for (int i=0; i<numOfPreviousPoints; i++)
			{
				// set iniitial guess
				b[i].setPredictedState(xGuess);
				b[i].setPredictedParameter(pGuess);
				
				// correct initial guess with poormans continuation method
				predictor.poormans(b[i], files);
				
				// perturb parameter for next iteration
				pGuess += contStepInitial;
				// update next guess with current convereged solution
				xGuess = b[i].getPredictedState();
				
			}
			
			///////////////////////////////////////////////////
			// continuation along the branches
			//////////////////////////////////////////////////
  	  NewtonRaphson newtRaph = new NewtonRaphson();
   		FunctionType F  = new FixedPoint();
   		outerloop:
			for (int i=numOfPreviousPoints; i<NumContSteps+numOfPreviousPoints; i++)
			{
					if (predictorMethod=="LeastSquares" || predictorMethod=="LS" 
					 || predictorMethod=="ls" || predictorMethod=="leastsquares")
					{
						// set prediction for the next point based on numOfPreviousPoints 
						double[] predictedVector = predictor.LeastSquares(b, i, files, "S"); 
					} else {
						// set prediction for the next point based on numOfPreviousPoints 
						double[] predictedVector = predictor.Secant(b, i, files);
					}

					// correct this prediction 
					// make newton method void as it sets the converged values so do not eneed to return anything
					double[] pp = param;
					pp[0] = b[i].getPredictedParameter();
					//double[] v = 
					newtRaph.newtonPA(F, predictor, pp, h, maxIter, stepNum, newtonTol, b[i], files, timeHorizon, DAMP);
					
					// check if theres a massive jump, if so terminate
  	  		for (int k=0; k<NumOfOrders; k++)
  	  			if (b[i].getSolution(k)==null)
  	  			{
  	  				System.out.println("Continuation haulted as converged solution is far from previous point");
  	  				break outerloop;	
  	  			}	
						
					// check for bifurcation 
					// move to stabillity calculation class 
 	  			if ( b[i].getDeterminant() * b[i-1].getDeterminant() < 0.0 ||
 	  					 b[i].getDeterminant() == 0.0 )
 	  			{
 	  				System.out.println("A Bifurcation may have occured, det(Jacobian) has changed sign"); 
 	  				System.out.println("At " + b[i-1].getConvergedParameter() + " det(J) = " + b[i-1].getDeterminant()); 
 	  				System.out.println("At " + b[i].getConvergedParameter() + " det(J) = " + b[i].getDeterminant());
 	  			}
 	  			
 	  			try{
						long currentTime = System.currentTimeMillis();
  		 			timeWriter.write(i + "\t" + ((double)(currentTime-startTime)/1000.) + "\n" );
  		 			System.out.println("Step " + (i+1-numOfPreviousPoints) + " of " + NumContSteps + ", Total runtime " + 
  		 						((double)(currentTime-startTime)/1000.) + " secs, " + 
  		 						((double)(currentTime-startTime)/60000.) + " mins" );		
						timeWriter.flush();
					} catch (IOException e){
						System.out.println(e);
					} finally {
						try{}
						catch(Exception ex){}
					}
				
			}
			
			long endTime = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println("Runtime = " + totalTime + "\t\tms\n" + 
												((double)totalTime/1000.) + "\t\ts\n" + (
												(double)totalTime/60000.) + "\tmins\n" + 
												((double)totalTime/3600000.) +"\thrs\n" );
												
												
			// Branch switching			
			if (BranchSwitching==true)
			{		
				double alpha = 0.5;
				branchSwitching(b, files, timeWriter, alpha);		
				branchSwitching(b, files, timeWriter, -alpha);		
			}
			
			timeWriter.close();
		} catch (Exception ex) {
			System.out.println(ex);
		}
		
		if (SDWrobustenssCheck == true){
		// for SDW robustness stuff
		
		// check if run was successful or not	
		double[] xx  = new double[NumContSteps];
		double[] yy  = new double[NumContSteps];
		double[] err = new double[NumContSteps];
			
		for (int i=0; i<NumContSteps; i++)
		{
			xx [i] = b[i+numOfPreviousPoints].getConvergedParameter();
			yy [i] = b[i+numOfPreviousPoints].getConvergedState(0);
			err[i] = b[i+numOfPreviousPoints].getStandardDeviation(0);
		}

		Successful s = new Successful(xx, yy, err);
		s.setBounds(3.0);
		s.isSuccessful();			
		
		}	
	}								
									
	// THIS NEEDS ENCAPSULATION!!!!!
	static void branchSwitching(MacroState[] b, String[] files, BufferedWriter timeWriter, double alpha)
	{								
		System.out.println("Checking for bifurcations along branch ...");
		// set up the continuation
		NewtonRaphson newtRaph = new NewtonRaphson();
 	 	FunctionType F  = new FixedPoint();			
		//int n = b.length;
		int NumOfOrders = b[2].getSize(); // index > 2 other wise NumOfOrders is N-1 !!! 
		double[] x0 = new double[NumOfOrders];
		double[] p = param;
		double h = hFD; 
		double tau = timeHorizon;
		long startTime = System.currentTimeMillis();
 	 
  	/* 
 		Create a object to retain any new Branch to reverse continuation along it
  	for generality require an object to hold a Stats object for everypoint
  	along the original curve, i.e. a bifurcation at each point, as the number
  	of bifurcation points is not know apriori so assumed to be the maximum possible
 	 */
		MacroState [][] bSaved = new MacroState [b.length][NumContSteps+2];
		int numOfBifurcations = 0; // monitor the number of bifucations (i.e. bSaved.length)

		// do not include the first two points as they do not have the same Jacobian information
		for (int z=3; z<b.length; z++) 
		{
			// if there is a bifurcation
			if(b[z-1].getDeterminant() * b[z].getDeterminant() < 0 || 
				 b[z].getDeterminant() == 0.0 )	
			{
				// create a new object for the new branch
				MacroState [] bNew = new MacroState [NumContSteps+2];
				for (int k=0; k<NumContSteps+2; k++){
					bNew[k] = new MacroState();
					bNew[k].setSize(NumOfOrders);
				}
			
				// create the saved object
 				for (int k=0; k<NumContSteps+2; k++)
 				{
					bSaved[numOfBifurcations][k] = new MacroState();
					bSaved[numOfBifurcations][k].setSize(NumOfOrders);
				}
			
				// use Eigenvector for Eigenvalue that pass through zero
				bNew[0].setConvergedParameter( b[z-1].getConvergedParameter() );//- continuationStep;
				p[0] = bNew[0].getConvergedParameter();
				int unstableOrder = -1; 
				double BiggestError = 0.0;
				for (int j=0; j<NumOfOrders; j++)
					if (b[z-1].getStandardDeviation(j) > BiggestError)
						BiggestError = b[z-1].getStandardDeviation(j);
											
				for (int j=0; j<NumOfOrders; j++)
				{
					if(b[z-1].getEigenValueMatrix(j,j) * b[z].getEigenValueMatrix(j,j)  < 0 || 
							 b[z].getEigenValueMatrix(j,j) == 0.0 )
						unstableOrder = j; 
						
					System.out.println(b[z].getConvergedParameter() + " " + b[z-1].getConvergedParameter() ) ; 
					for (int jj=0; jj<NumOfOrders; jj++)
							System.out.print(b[z].getEigenVector(j,jj) + " " ) ; 
					System.out.println(" " ) ;
				}
	
						
				// eigenvector 
				for (int j=0; j<NumOfOrders; j++)
				{		
					bNew[0].setConvergedState( b[z].getConvergedState(j), j);
					if (unstableOrder > -1)
						bNew[0].setConvergedState( bNew[0].getConvergedState(j) + alpha * b[z].getEigenVector(j,unstableOrder), j);
							
					System.out.println(" --- " + b[z].getEigenVector(j,unstableOrder) + " " +  bNew[0].getConvergedState(j) );
				}
				x0 = bNew[0].getConvergedState();
				
				// append file to distinguish branches
				appendFile(files[1],"\n\n");
					
    		// try to find fixed point here
				newtRaph.newton1D(F, x0, p, h, maxIter, stepNum, newtonTol, bNew[0], files, tau);
				double[] x1 = bNew[0].getConvergedState();
				double[] v0 = new double[NumOfOrders+1];
    		for (int k=0; k<NumOfOrders; k++)
    			v0[k] = x1[k];
    		v0[NumOfOrders] = p[0];
    		bNew[0].setConvergedState( x1 ); // update with new fixed point
    		bNew[1].setConvergedState( x1 ); // use as guess for next fixed point
    		
    		bNew[1].setConvergedState( bNew[0].getConvergedState(0)-0.1 , 0 );
    		bNew[1].setConvergedState( bNew[1].getConvergedState(1)+0.1 , 1 );
    	
    		x1 = bNew[1].getConvergedState(); // update guess

				// perturb and find new fixed point
    		//p[0] += contStepInitial;
				newtRaph.newton1D(F, x1, p, h, maxIter, stepNum, newtonTol, bNew[1], files, tau);
				double[] x2 = bNew[1].getConvergedState();
				double[] v1 = new double[NumOfOrders+1];
    		for (int k=0; k<NumOfOrders; k++)
    			v1[k] = x2[k];
    		v1[NumOfOrders] = p[0];
				bNew[1].setConvergedState( x2 ); // update with new fixed point
	
			  // follow along the rest of this branch
			  
  		  // poor mans continuation!!!!
  		  for (int i=2; i<NumContSteps; i++)
  		  {
  		  	p[0] += contStepInitial;
					
					newtRaph.newton1D(F, x2, p, h, maxIter, stepNum, newtonTol, bNew[i], files, timeHorizon);
					x2 = bNew[0].getConvergedState();
				
					//bNew[i].setConvergedState( x2 );
					//bNew[i].setConvergedParameter( p[0] );
				}
				
				//followBranch(v0, v1, bNew, continuationStep, p, h, maxIter, secantTol, files, tau, timeWriter, startTime);
				
				// save the output so continaution can be reversed along a branch
				bSaved[numOfBifurcations] = bNew; 
				numOfBifurcations++;
			}
		}					
	
		// Reverse the direction of the continuation along the new branch for each bifurcation point
		System.out.println("reverse direction"); 				
		for (int i=0; i<numOfBifurcations; i++)
		{
			// create a new object for the new branch
			MacroState [] bNew = new MacroState [NumContSteps+2];
			for (int k=0; k<NumContSteps+2; k++)
			{
				bNew[k] = new MacroState();
				bNew[k].setSize(NumOfOrders);
			}
	
			int midPoint = (int) bSaved[i].length / 2; // find mid point of the branch
		
			appendFile(files[1],"\n\n"); // append file to distinguish branches
						
			x0 = bSaved[i][midPoint].getConvergedState();
			p[0] = bSaved[i][midPoint].getConvergedParameter();
			bNew[0].deepCopy( bSaved[i][midPoint] ); // copy entire object
			//bNew[0].parameter = p[0];
	 						
	    // try to find fixed point here
	    // value fro bSaved should already be converged solution 
	    // so dont need to recalculate as can be quite compuationally expensive
			double[] x1 = x0; 
			//double[] x1 = newtRaph.newton1D(F, x0, p, h, maxIter, stepNum, newtonTol, bNew[0], files, 	timeHorizon);
			double[] v0 = new double[NumOfOrders+1];
    	for (int k=0; k<NumOfOrders; k++)
	    	v0[k] = x1[k];
	    v0[NumOfOrders] = p[0];
	   // bNew[0].X = x1; // update with new fixed point
	  //  bNew[1].X = x1; // use as guess for next fixed point
	
	    // Continue along this branch in the opposite direct as previous
			p[0] -= contStepInitial;
			//double[] x2 = newtRaph.newton1D(F, x1, p, h, maxIter, stepNum, newtonTol, bNew[1], files, 	timeHorizon);
			// initialise x2 using the previously obtained values depending on direction 

			if (contStepInitial > 0.0)
				bNew[1].deepCopy(bSaved[i][midPoint-1]); // copy entire object
			else
				bNew[1].deepCopy(bSaved[i][midPoint+1]);
				
			double[] x2 = bNew[1].getConvergedState(); 
			
			double[] v1 = new double[NumOfOrders+1];
	    for (int k=0; k<NumOfOrders; k++)
	    	v1[k] = x2[k];
	    v1[NumOfOrders] = p[0];
			//bNew[1].X = x2; // update with new fixed point
			//bNew[1].parameter = p[0];
				
			// follow along the rest of this branch
			// poor mans
			
	  	for (int k=2; k<NumContSteps; k++)
	  	{
	  		p[0] -= contStepInitial;
				//x2 = newtRaph.newton1D(F, x2, p, h, maxIter, stepNum, newtonTol, bNew[k], files, timeHorizon);
				
				newtRaph.newton1D(F, x2, p, h, maxIter, stepNum, newtonTol, bNew[k], files, timeHorizon);
				x2 = bNew[0].getConvergedState();
				//bNew[k].setConvergedState( x2 );
				//bNew[k].setConvergedParameter( p[0] );
			}
		
		}	

	}

	
	
	// open files
	private static void openFile(String fileName, String input){
		
		Writer file = null;	// Phi(x), distribution of realisations
		
		try{
			file = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName), "utf-8")); 
			
			file.write(input+"\n");	

		} catch (IOException e) {
			System.out.println(e);
		} finally {
			try { 
				file.flush();
				file.close();
			} catch (Exception ex) {}
		}
	}
	
	private static void appendFile(String name, String input){
		// open file
		
		try {
			File file = new File(name);
			FileWriter fileWriter = new FileWriter(file,true);
			BufferedWriter bufferFileWriter  = new BufferedWriter(fileWriter);	
				
			fileWriter.append(input);
			bufferFileWriter.close();
		} catch (IOException e){
			System.out.println(e);
		} finally  {
			try{ } 
			catch (Exception ex) {}
		}
	
	}
	
	
// used in branch switching
	public static double getMean(double[] x)
	{
		double sum = 0.0;
		for (double a : x){
			sum += a;
		}
		return sum/x.length; 
	}

	// used in branch switching
	public static double getVariance(double[] x){
		return Math.pow( getStandardDev(x), 2 );
	}
		
	// used in branch switching
	public static double getStandardDev(double[] x){
		double mean = getMean(x);
		double temp = 0;
		for (double a : x)
			temp += Math.pow((mean-a),2);
		return Math.sqrt( temp/x.length );
	}	
	
	

}  