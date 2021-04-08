class PseudoArc extends ContinuationParameters implements FunctionType{
	// Function; returns Phi(u,p)-u
	
	public PseudoArc(){
	}
	public double[] evaluate(double[] v, double[] p, MacroState macro, String[] files, boolean output){
	
		int numOfOrders = v.length-1;
		double[] f = new double[numOfOrders+1];
		double[] u = new double[numOfOrders];
		for (int i=0; i<numOfOrders; i++)
			u[i] = v[i];
		
		// variabels from the macroState object need for PA calculation in Newton method
		double[] v1 = macro.predictedX; 
		double[] alpha = macro.alpha; 
		
		MacroDescription l  = new MacroOperator();
		l.Lift(u, p);

		// evalutate fixed point
		for (int i=0; i<numOfOrders; i++){
				f[i] 									= l.Restrict(i) - u[i];
				macro.solution[i] 		= (Double) f[i];
		}
	
		for (int i=0; i<numOfOrders; i++) {
			macro.X[i]						= u[i];
			macro.mean[i] 				= l.getMean(i);
			macro.stdDev[i] 			= l.getStandardDev(i);
			macro.var[i] 					= l.getVariance(i);
			macro.stdErr[i] 			= l.getStandardErr(i);
			macro.median[i]				= l.getMedian(i);
			macro.liftAverage[i] 	= l.Restrict(i);
		}
		macro.F					= l.getPhi();	
		macro.parameter = p[0];
		macro.size 			= l.getSize();
		
		// is solution far from guess, penalise convergened value
		double range = RANGE;
		double max = 0.0;
		for (int i=0; i<numOfOrders; i++) // take largest of the two 
			if ( macro.stdErr[i] > max )		  	 // needs to be related to ds so range 
				max = macro.stdErr[i]; 					   // is non zero for deterministic systems
		if (continuationStep > max )	
			max = continuationStep;
			
		if ( Math.abs( Math.abs(macro.parameter)-Math.abs(v1[numOfOrders]) ) > range*continuationStep ){
			for (int i=0; i<numOfOrders; i++)
				macro.solution[i] = null;
		}
					
		for (int i=0; i<numOfOrders; i++){ // take largest of the two 
			if ( Math.abs( Math.abs(v1[i])-Math.abs(macro.liftAverage[i]) ) > range*max ){
				macro.solution[i] = null;
			}			
		}
	
		// output realisation distribution for converge solutions only
		if (output==true)
			l.printOut(files[0]);	

		f[numOfOrders] = funcArcLength(alpha, v, v1);
	
		return f;
	}
	
	// computes pseudo arclength
	public double funcArcLength(double[] alpha, double[] v, double[] v0){
	
		int N = v.length;
		double FpsuedoArc = 0.0;
		for (int i=0; i<N; i++){
			FpsuedoArc += alpha[i] * ( v[i] - v0[i] );
		}
		FpsuedoArc -= continuationStep; 
		return FpsuedoArc;	
	}
	
	// computes norm of vector x
	public double Norm(double[] x, int normVal){
		double norm = 0.0;
		for (int i=0; i<x.length; i++){
			norm += Math.pow( x[i], normVal );
		}
		norm = Math.pow( norm, 1.0/normVal );
		return norm;
	}
	
	// computes norm of scalar x
	public double Norm(double x, int normVal){
		double norm = 0.0;
		norm += Math.pow( x, normVal );
		norm = Math.pow( norm, 1.0/normVal );
		return norm;
	}
	
}
