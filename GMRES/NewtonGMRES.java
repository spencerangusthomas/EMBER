import java.util.Random;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.io.*;
//import Jama.*;
// Matrix Java Toolbox for GMRES
import no.uib.cipr.matrix.*;
import java.lang.Object.*;
import no.uib.cipr.matrix.sparse.GMRES;


public class NewtonGMRES extends ContinuationParameters {
	public NewtonGMRES(){
	}

	public double[] ngmresLinearSolver(double[] u, FunctionType F, double[] p, MacroState macro, String[] files){

		// standard Newton method root finder with 
		// GMRES iterator to remove the explicit need of Jacobian

		// setup parameters
		int 		 numOfOrders = u.length;
		double 	 lambda = 1.0;					// Damp factor
		double	 h = correctorStep; 		// defined in ContinuationParameters.java
		double	 tau = timeHorizon;			// defined in ContinuationParameters.java
		double[] u0 = u;
		double[] ui = u;
		double[] p0 = p;
		boolean  exit = false; 					// convergence exit flag
		int 		 iter = 0; 							// number of interations (inner) for corrector
		int 		 attempts = 0;					// CCCR attempts (outter interations)
		int 		 counter = 0;						
		
		// files to record the output of the process
		try {

			// continuation around bifurcation curve
			File cont = new File(files[1]);
			FileWriter contWriter = new FileWriter(cont,true);
			BufferedWriter contBuffer  = new BufferedWriter(contWriter);			
			
			// newton
			File newton = new File(files[2]);
			FileWriter newtonWriter = new FileWriter(newton,true);
			BufferedWriter newtonBuffer  = new BufferedWriter(newtonWriter);	
			newtonWriter.append("# initial Newton\n");
				
			// Jacobian
			File Jaco = new File(files[4]);
			FileWriter jacobianWriter = new FileWriter(Jaco,true);
			BufferedWriter jacobainBuffer  = new BufferedWriter(jacobianWriter);	

			newtonWriter.append("# macro.parameter\tmacro.X[i]\tmacro.liftAverage\tsolution(f-x)\titerations\n");	
			for (int i=0; i<numOfOrders; i++)								
				newtonWriter.append(macro.parameter+"\t"+macro.X[i]+"\t"+macro.liftAverage[i]+"\t"+macro.solution[i] + "\tinitial\n");

			do {

				// evaluate the function 
				// Jh = -F -> Aw = b
				// so funciton evlaution is action (Ax)
				double[] Jh = F.evaluate(u0, p, macro, files, false);	
			
	
				no.uib.cipr.matrix.Vector b;

				
				no.uib.cipr.matrix.sparse.GMRES g;// = new no.uib.cipr.matrix.sparse.GMRES(b);
				//g = new no.uib.cipr.matrix.sparse.GMRES(b);
	
	
	/*
	
				double us = 0.0;
				double[] wT = new double[numOfOrders];
				for (int i=0; i<numOfOrders; i++){
					us += ui[i] * (double) w.get(i); 
					wT[i] = (double) w.get(i);
				}
				us /= Norm(wT, 2);
				
				// update the step in the corrector
				if (us != 0)
					h = h * Math.max(Math.abs(us), 1.0) * ( us / Math.abs(us) ) ;
				h /= Norm(wT, 2);
			
				for (int i=0; i<numOfOrders; i++)
					u[i] = ui[i] + h * (double) w.get(i);
					
				double[] fu = F.evaluate(u, p, macro, files, false);

				iter++;
			
				counter = 0;
				for (int i=0; i<numOfOrders; i++)
					if ( Math.abs(fu[i]) < newtonTol )
						counter++;
						
				// if all solutions are below the tolerance exit
				if (counter>=numOfOrders)
					exit=true;
				
				if (counter < numOfOrders && exit==false ){
					// CCCR procedure
					boolean anyNulls = false;
					for (int k=0; k<numOfOrders-1; k++)
						if (macro.solution[k] == null && attempts< NUMOFRESTARTS)
							anyNulls = true;
				
					if (anyNulls==true){
						lambda = 1.0;
						iter = 0;
						for (int k=0; k<u.length; k++)
							u[k] = u0[k];
						for (int k=0; k<p.length; k++)
							p[k] = p0[k];
						attempts++;
						exit = false; 
						for (int k=0; k<numOfOrders-1; k++){
							//func[k] = funcI[k];
							//macro.solution[k] = (Double) funcI[k];
						}
					}
				}
				
				ui = u; 
				
				// Damped Newton
				if (iter>=maxIter && lambda>1e-4){
						lambda /= 10.0; 
						iter=0;
						u = u0;
						ui = u0;
				}	
	*/			
			} while ( iter < maxIter && exit == false );
			
			// output the results to files
			/*
 	  	double[][] tempArray = new double[element][element];
 	  	for (int k=0; k<numOfOrders; k++)
 	  		for (int kk=0; kk<numOfOrders; kk++)
 	  			tempArray[k][kk]= macro.Jacobian[k][kk];
			
			// printout Matrix	
    	for (int z=0;z<numOfOrders;z++){
	    	for (int y=0;y<numOfOrders;y++)
	    		jacobianWriter.append(macro.Jacobian[z][y] + "\t");
	    		
	   		jacobianWriter.append("\t|\t");
	   		jacobianWriter.append(macro.EigenValueReal[z] + "\t" + macro.EigenValueImag[z] +"\t|\t");
	   		
	   		for (int y=0;y<nn;y++)
	   			jacobianWriter.append(macro.EigenVector[y][z] + "\t");
    					
	    	jacobianWriter.append("\n");
    	}	
    	jacobianWriter.append("#Determinant=\t" + M.det() + "\n");  
    	jacobianWriter.append("#OrderParam=\t"); 		
    	for (int y=0;y<numOfOrders;y++)
	   		jacobianWriter.write(macro.X[y] + "\t");
	   		
	    jacobianWriter.append("\n#BifParam=\t" + macro.parameter + "\n\n\n");
			*/
			// output Newton iterations details
			newtonWriter.append("# Converged newton Details\n");
			for (int i=0; i<numOfOrders; i++){
				newtonWriter.append(macro.parameter+"\t"+macro.X[i]+"\t"+macro.liftAverage[i]+"\t"+macro.solution[i] + "\t"+iter+"\n");
												
				contWriter.append(macro.parameter+"\t"+macro.X[i]+"\t"+macro.stdErr[i]+"\t"+macro.stdDev[i]+"\t"+macro.var[i]+"\t"+
													macro.liftAverage[i]+"\t"+macro.mean[i]+"\t"+macro.median[i]+"\t"+macro.solution[i]+"\t"+macro.EigenValueMatrix[i][i]+"\t");									
		
			}
		
			newtonWriter.append("\n\n");
			contWriter.append(iter+"\t"+ macro.size+"\t" + macro.determinantOfJ + "\n");
		
			newtonBuffer.close();	
			contBuffer.close();	
		//	jacobianBuffer.close();

		} catch(Exception ex) {
			System.out.println(ex);
		}


		// call to stabiliity method / class  


		return u;
	}
	
	public static double Norm(double[] x, int normVal){
		double norm = 0.0;
		for (int i=0; i<x.length; i++){
			norm += Math.pow( x[i], normVal );
		}
		norm = Math.pow( norm, 1.0/normVal );
		return norm;
	}
}
