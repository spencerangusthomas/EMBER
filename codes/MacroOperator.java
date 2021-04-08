import java.util.Random;
import java.util.Arrays;
import java.util.List;
import java.io.*;
import Jama.*;

public class NewtonRaphson extends ContinuationParameters{
	public NewtonRaphson(){
	}
	
	// Newton Method 1D
	//public static double[] newton1D(FunctionType F, double[] u, double[] p, double h, int maxIter, int steps, double tol, MacroState b, String[] files, double timeHorizon){
	public static void newton1D(FunctionType F, double[] u, double[] p, double h, int maxIter, int steps, double tol, MacroState b, String[] files, double timeHorizon){
			
		boolean 		exit = false; 
		int 				numOfOrders = u.length;
		double 			lambda = 1.0;			// Damp factor
		double[] 		funcN = new double[numOfOrders];
		double[] 		du = new double[numOfOrders];
		double[][] 	funcT = new double[numOfOrders][numOfOrders];
		double[][] 	J 	= new double[numOfOrders][numOfOrders];	
		GaussianElimination gaussStep = new GaussianElimination();

		double[] u0 = new double[numOfOrders];
		for (int i=0; i<numOfOrders; i++)
			u0[i] = u[i];

		try {

			// continuation around bifurcation curve
			File cont = new File(files[1]);
			FileWriter contWriter = new FileWriter(cont,true);
			BufferedWriter contBuffer  = new BufferedWriter(contWriter);			
			
			// newton
			File file = new File(files[2]);
			FileWriter fileWriter = new FileWriter(file,true);
			BufferedWriter bufferFileWriter  = new BufferedWriter(fileWriter);	
				
			// Jacobian
			File Jaco = new File(files[4]);
			FileWriter JWriter = new FileWriter(Jaco,true);
			BufferedWriter bufferJWriter  = new BufferedWriter(JWriter);	
						
			fileWriter.append("# initial Newton\n");
					
			double[] func = F.evaluate(u, p, h, steps, timeHorizon, b, files, false);	// evaluate the function

			fileWriter.append("# b.parameter\tb.X[i]\tb.liftAverage\tsolution(f-x)\titerations\n");	
	
			for (int i=0; i<numOfOrders; i++){
				fileWriter.append(b.getConvergedParameter()+"\t"+b.getConvergedState(i)+"\t"+b.getLift(i)
						+"\t"+b.getSolution(i) + "\tinitial\n");					
			}

			int iter = 0;
			int counter = 0;	
				
			do {		
			
			//System.out.print(iter + " ");
			for (int g=0; g<numOfOrders; g++) 
//				System.out.print(func[g] + " " );			
//		System.out.println(" " );
		
					// fT[ F1(u1+h,u2) F2(u1+h,u2) ; F1(u1,u2+h) F2(u1,u2+h) ]
					for (int i=0; i<numOfOrders; i++) {
						double[] uT = new double [numOfOrders];
						for (int j=0; j<numOfOrders; j++) 
							uT[j] = u[j];
	
						uT[i] += h; 			
						funcT[i] = F.evaluate(uT, p, h, steps, timeHorizon, b, files, false);
					}	
					
					for (int i=0; i<numOfOrders; i++)
						funcN[i] = -func[i];
				
					// Jacobian
					for (int i=0; i<numOfOrders; i++){
						for (int k=0; k<numOfOrders; k++){
							J[i][k] = ( funcT[k][i] - func[i] ) / h;	
						}
					}									
					b.setJacobian(J);	
					
					
					iter++;
							
					counter = 0;
					for (int i=0; i<numOfOrders; i++)
						if ( Math.abs(func[i]) < tol ){
							counter++;
						}
				
					if (counter>=numOfOrders){	// if all solutions are below the tolerance exit
						exit=true;
					}		
										
					if (counter < numOfOrders && exit==false){					
						du = gaussStep.lsolve(J, funcN);
						for (int i=0; i<numOfOrders; i++) 
							u[i] += lambda*du[i];
	
								
						func = F.evaluate(u, p, h, steps, timeHorizon, b, files, false);
		
						// Damped Newton
						if (iter>=maxIter && lambda>1e-4){
							lambda /= 10.0; 
							iter=0;
							u = u0;
						}	
					}
			} while( iter < maxIter && exit==false );

			if ( iter >= maxIter ){
				System.out.println("Failed to improve on your initial guess\nIt may not be close enough to the real solution\nPlease try again");
			}

			F.evaluate(u, p, h, steps, timeHorizon, b, files, true);

			// output Jacobian Information
			/*
			int element = b.Jacobian.length;
 	  	double[][] tempArray = new double[element][element];
 	  	for (int k=0; k<element; k++)
 	  		for (int kk=0; kk<element; kk++)
 	  			tempArray[k][kk]= b.Jacobian[k][kk]; 
 	  	*/

 	  	double[][] JacobianMatrix = b.getJacobian();

 	  	// uses the JAMA library
 	  	Jama.Matrix M = new Jama.Matrix(JacobianMatrix);
 	  	Jama.EigenvalueDecomposition E =
  	    new Jama.EigenvalueDecomposition(M);
	  			
			b.setDeterminant ( (Double) M.det() );	// det(J)
	  	Jama.Matrix D = E.getD();							  // EigenValueMatrix
	  	Jama.Matrix V = E.getV();							  // EigenVectors	  	
	  				  	
    	double[][] eigenVector = V.getArray();	
    	b.setEigenValueMatrix ( D.getArray() );	// Convert Jama.Matrix to double[][] 

			int element = b.getSize();
   		// normalised EigenVector
			eigenVector = transpose(eigenVector); // to get in passable form
			for (int z=0; z<element; z++){
				eigenVector[z] = normalisedEigenVector( eigenVector[z] );
			}
			eigenVector = transpose(eigenVector);	// back to original state
			
    	b.setEigenVector ( eigenVector );	
    	
    	// real and imaginary eigenvalues
    	b.setEigenValueReal ( E.getRealEigenvalues() );
    	b.setEigenValueImag ( E.getImagEigenvalues() );
 	     		
   		int nn = JacobianMatrix.length; 

    	// printout Matrix	
    	for (int z=0;z<nn-1;z++)
    	{
	    	for (int y=0;y<nn;y++)
	    		JWriter.append(JacobianMatrix[z][y] + "\t");
	   		JWriter.append("\t|\t");
	   		/*
	   		for (int y=0;y<nn;y++)
	   			JWriter.append(b.EigenValueMatrix[z][y] + "\t");
	   		JWriter.write("\t|\t");
	   		*/
	   		JWriter.append(b.getEigenValueReal(z) + "\t" + b.getEigenValueImag(z) +"\t|\t");
	   		
	   		for (int y=0;y<nn;y++)
	   			JWriter.append(b.getEigenVector(y,z) + "\t");
    					
	    	JWriter.append("\n");
    	}	
    	
    	JWriter.append("#Determinant=\t" + M.det() + "\n");  
    	JWriter.append("#OrderParam=\t"); 		
    	for (int y=0;y<nn-1;y++)
	   		JWriter.write(b.getConvergedState(y) + "\t");
	    JWriter.append("\n#BifParam=\t" + b.getConvergedParameter() + "\n\n\n");


			// output Newton iterations details
			fileWriter.append("# Converged newton Details\n");
			for (int i=0; i<numOfOrders; i++){
				fileWriter.append(b.getConvergedParameter()+"\t"+b.getConvergedState(i)+"\t"+b.getLift(i)+"\t"+b.getSolution(i) + "\t"+iter+"\n");
												
				contWriter.append(b.getConvergedParameter()+"\t"+b.getConvergedState(i)+"\t"+b.getStandardError(i)+"\t"+b.getStandardDeviation(i)+"\t"+b.getVariance(i)+"\t"+
													b.getLift(i)+"\t"+b.getMean(i)+"\t"+b.getMedian(i)+"\t"+b.getSolution(i)+"\t"+b.getEigenValueMatrix(i,i)+"\t"+b.getConvergedParameter()+"\t"+b.getConvergedState(i)+"\t");									
		
			}
		
			fileWriter.append("\n\n");
			contWriter.append(iter+"\t"+ b.getSize()+"\t" + b.getDeterminant() + "\n");
		
			bufferFileWriter.close();	
			contBuffer.close();	
			bufferJWriter.close();
			
		} catch(Exception ex) {
			System.out.println(ex);
		}

		//return u;
	}
	
	// Newton Method Pseudo Arclength
	//public static double[] newtonPA(FunctionType F, Predictor predictor, double[] p0, double h, int maxIter, int steps, double tol, MacroState b, String[] files, double timeHorizon, boolean DAMP){
	public static void newtonPA(FunctionType F, Predictor predictor, double[] p0, double h, int maxIter, int steps, double tol, MacroState b, String[] files, double timeHorizon, boolean DAMP){
			
		boolean 		exit = false; 
		int 				attempts = 0;
		int 				numOfOrders = predictor.getSize();

		double 			lambda = 1.0;			// Damp factor if DAMP==true
		double[] 		u  = new double[numOfOrders];	// corrected
		double[] 		up = new double[numOfOrders]; // predicted
		double[] 		u1 = new double[numOfOrders];	// previous
		double[] 		p = new double[p0.length];
		double[] 		funcN = new double[numOfOrders];
		double[] 		du = new double[numOfOrders];
		double[][] 	funcT = new double[numOfOrders][numOfOrders];
		double[][] 	J 	= new double[numOfOrders][numOfOrders];	
		double[]		alpha = predictor.getAlpha();
		GaussianElimination gaussStep = new GaussianElimination();
			
		for (int i=0; i<numOfOrders; i++){
			u [i] = predictor.getPredictor(i);
			up[i] = predictor.getPredictor(i);
			u1[i] = predictor.getPrevious(i);
		}
				
		for (int i=0; i<p0.length; i++)
			p[i] = p0[i];	
		p[0] = u[numOfOrders-1]; // update continuation parameter
				
		// record initial values (predicted solution)
		double[] func = F.evaluatePseudoArc(u, p, alpha, u1, h, steps, timeHorizon, b, files, false, true);	
		double[] funcI = new double[numOfOrders];
		for (int i=0; i<numOfOrders; i++)
			funcI[i] = func[i];


		try {
			// continuation around bifurcation curve
			File cont = new File(files[1]);
			FileWriter contWriter = new FileWriter(cont,true);
			BufferedWriter contBuffer  = new BufferedWriter(contWriter);
			
			// Newton
			File file = new File(files[2]);
			FileWriter fileWriter = new FileWriter(file,true);
			BufferedWriter bufferFileWriter  = new BufferedWriter(fileWriter);		
							
			// Jacobian
			File Jaco = new File(files[4]);
			FileWriter JWriter = new FileWriter(Jaco,true);
			BufferedWriter bufferJWriter  = new BufferedWriter(JWriter);		
				
				
			fileWriter.append("# initial Newton\n");
			fileWriter.append("# b.parameter\tb.X[i]\tb.liftAverage\tsolution(f-x)\titerations\n");	
			for (int i=0; i<numOfOrders-1; i++)										
				fileWriter.append(b.getConvergedParameter()+"\t"+b.getConvergedState(i)+"\t"+b.getLift(i)+"\t"+b.getSolution(i) + "\tinitial\n");					

			int iter = 0;
			int counter = 0;	

			do {
					// fT[ F1(u1+h,u2) F2(u1+h,u2) ; F1(u1,u2+h) F2(u1,u2+h) ]
					//simulating: 
					for (int i=0; i<numOfOrders-1; i++) {
						double[] uT = new double [numOfOrders];
						for (int j=0; j<numOfOrders; j++) 
							uT[j] = u[j];
							
						uT[i] += h; 			
						// evaluate fixed point but dont record the details of the solution as for J calculation only
						funcT[i] = F.evaluatePseudoArc(uT, p, alpha, u1, h, steps, timeHorizon, b, files, false, false);
						//if (b.solution[i]==null)
							//continue simulating;
					}	

					// F(u1,u2,mu+h)
					double[] pT = new double [p.length];
					for (int i=0; i<p.length; i++)
						pT[i] = p[i];
					pT[0] += h;
					double[] uTT = new double [numOfOrders];
					for (int j=0; j<numOfOrders-1; j++) 
							uTT[j] = u[j];
					uTT[numOfOrders-1] = pT[0];
					funcT[numOfOrders-1] = F.evaluatePseudoArc(uTT, pT, alpha, u1, h, steps, timeHorizon, b, files, false, false);
				
					for (int i=0; i<numOfOrders; i++)
						funcN[i] = -func[i];
				
					// Jacobian
					for (int i=0; i<numOfOrders-1; i++)
						for (int k=0; k<numOfOrders; k++)
							J[i][k] = ( funcT[k][i] - func[i] ) / h;	
										
					for (int k=0; k<numOfOrders; k++)
						J[numOfOrders-1][k] = alpha[k];
					b.setJacobian(J);
					
					// Determinant of J
					Jama.Matrix M = new Jama.Matrix(J);
					b.setDeterminant( (Double) M.det() );

					iter++;
							
					counter = 0;
					for (int i=0; i<numOfOrders; i++)
						if ( Math.abs(func[i]) < tol ){
							counter++;
						}
				
					if (counter>=numOfOrders){	// if all solutions are below the tolerance exit
						exit=true;
					}		
							
							
					if (counter < numOfOrders && exit==false ){					
						du = gaussStep.lsolve(J, funcN);
								
						for (int i=0; i<numOfOrders-1; i++) 
							u[i] += lambda*du[i];
						p[0] += lambda*du[numOfOrders-1];
						u[numOfOrders-1] = p[0];
				
						// update fixed point but dont record information
						func = F.evaluatePseudoArc(u, p, alpha, u1, h, steps, timeHorizon, b, files, false, false);
						
						// Damped Newton 
						if (DAMP==true && iter>=maxIter && lambda>1e-5){
							lambda /= 10.0; 
							iter = 0;
							u = up;
							p = p0;
						}	
					//{
					counter = 0;
					for (int i=0; i<numOfOrders; i++)
						if ( Math.abs(func[i]) < tol ){
							counter++;
						}
				
					if (counter>=numOfOrders){	// if all solutions are below the tolerance exit
						exit=true;
					}		
					// restart if convereged too far from initial guess
					boolean anyNulls = false;
					for (int k=0; k<numOfOrders-1; k++){
						if (b.getSolution(k) == null && attempts< NUMOFRESTARTS){
							anyNulls = true;
						}
//			System.out.print(	b.getSolution(k) + " " + attempts + " " + anyNulls + " " + iter);
					}
//					System.out.print("\n");
					if (anyNulls==true){
						lambda = 1.0;
						iter = 0;
						for (int k=0; k<u.length; k++)
							u[k] = up[k];
						for (int k=0; k<p.length; k++)
							p[k] = p0[k];
						attempts++;
						exit = false; 
						for (int k=0; k<numOfOrders-1; k++){
							func[k] = funcI[k];
							b.setSolution( (Double) funcI[k], k);
//						System.out.print(	" --> " + func[k] + " " + b.getSolution(k) + " " + funcI[k] + " " + u[k] + " " + p[k]);	
						}
//					System.out.print("\n");
					}
					
			}		
					//if ( exit == true || iter==maxIter){
					//	System.out.println("\nGET ME OUT OF HERE! " + exit + " " +counter + " " + b.getSolution(0) + " " + iter + " " + anyNulls + " " + attempts  + "\n");
				//	}
				
				
				
				
			} while( iter < maxIter && exit==false );
			
			if ( iter >= maxIter ){
				System.out.println("Failed to improve on your initial guess\nIt may not be close enough to the real solution\nPlease try again");
			}

			// update and record converged information
			func = F.evaluatePseudoArc(u, p, alpha, u1, h, steps, timeHorizon, b, files, true, true);

	
	
			// output Jacobian Information
			/*
			int element = b.getSize();
 	  	double[][] tempArray = new double[element][element];
 	  	for (int k=0; k<element; k++)
 	  		for (int kk=0; kk<element; kk++)
 	  			tempArray[k][kk]= b.Jacobian[k][kk]; 
 	  	*/
 	 		double[][] JacobianMatrix = b.getJacobian();
 	 		
 	  	// uses the JAMA library
 	  	Jama.Matrix M = new Jama.Matrix(JacobianMatrix);
 	  	Jama.EigenvalueDecomposition E =
      	new Jama.EigenvalueDecomposition(M);
	  			
			b.setDeterminant( (Double) M.det() );	// det(J)
	  	Jama.Matrix D = E.getD();							// EigenValueMatrix
	  	Jama.Matrix V = E.getV();							// EigenVectors	  	
	  	
	  	
	  	double[][] eigenVector = V.getArray();	
    	b.setEigenValueMatrix( D.getArray() );	// Convert Jama.Matrix to double[][]
    	
    	int element = b.getSize();	
		
			// normalised EigenVector
			eigenVector = transpose(eigenVector); // to get in passable form
			for (int z=0; z<element; z++){
				eigenVector[z] = normalisedEigenVector( eigenVector[z] );	
			}
			eigenVector = transpose(eigenVector);	// back to original state
				
    	b.setEigenVector( eigenVector );	
 
    	// real and imaginary eigenvalues
    	b.setEigenValueReal( E.getRealEigenvalues() );
    	b.setEigenValueImag( E.getImagEigenvalues() );
    
    	// printout Matrix	
    	for (int z=0;z<element;z++)
    	{
	    	for (int y=0;y<element;y++)
	    		JWriter.append(JacobianMatrix[z][y] + "\t");
	    	
	    	JWriter.append("\t|\t");
	    	/*
	   		for (int y=0;y<nn;y++)
	   			JWriter.append(b.getEigenValueMatrix(z,y) + "\t");
	   		JWriter.write("\t|\t");
	   		*/
	   		JWriter.append(b.getEigenValueReal(z) + "\t" + b.getEigenValueImag(z) +"\t|\t");
	   		
	    	for (int y=0;y<element;y++)
	    		JWriter.append(b.getEigenVector(y,z) + "\t");
	    	
	    	JWriter.append("\n");
    	}	
    	
    	JWriter.append("#Determinant=\t" + M.det() + "\n");  
    	JWriter.append("#OrderParam=\t"); 		
    	for (int y=0;y<element;y++)
	    	JWriter.write(b.getConvergedState(y) + "\t");
	    JWriter.append("\n#BifParam=\t" + b.getConvergedParameter() + "\n\n\n");

    		
			// output Newton iterations details
			fileWriter.append("# Converged newton Details\n");
			for (int i=0; i<numOfOrders-1; i++){
				fileWriter.append(b.getConvergedParameter()+"\t"+b.getConvergedState(i)+"\t"+b.getLift(i)+"\t"+b.getSolution(i) + "\t"+iter+"\n");
												
				contWriter.append(b.getConvergedParameter()+"\t"+b.getConvergedState(i)+"\t"+b.getStandardError(i)+"\t"+b.getStandardDeviation(i)+"\t"+b.getVariance(i)+"\t"+
													b.getLift(i)+"\t"+b.getMean(i)+"\t"+b.getMedian(i)+"\t"+b.getSolution(i)+"\t"+b.getEigenValueMatrix(i,i)+"\t"+b.getPredictedParameter()+"\t"+b.getPredictedState(i)+"\t");										
			}

			fileWriter.append("\n\n");
			contWriter.append(iter+"\t"+ b.getSize()+"\t" + b.getDeterminant() + "\n");
			
			bufferFileWriter.close();	
			contBuffer.close();	
			bufferJWriter.close();
			
		} catch(Exception ex) {
			System.out.println(ex);
		}

		//return u;
	}


	static double[] normalisedEigenVector( double[] eigenVector )
	{
		int size = eigenVector.length;
		double[] normalisedEigenVector = new double[size];
		double temp = 0.0;
		for (int i=0; i<size; i++)
			temp += Math.pow(eigenVector[i], 2.0);
		
		for (int i=0; i<size; i++)
			normalisedEigenVector[i] = eigenVector[i] / Math.sqrt(temp);
			
		return normalisedEigenVector;	
	
	}

	static double[][] transpose(double[][] A)
	{
		int n = A.length;
		int m = A[0].length;
		double[][] Aprime = new double [m][n];
	
		for (int i=0; i<n; i++){
			for (int j=0; j<m; j++){
				Aprime[j][i] = A[i][j]; 
			}
		}		
				
		return Aprime;
	}
	
	
}