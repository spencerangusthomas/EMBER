class PoorMans extends ContinuationParameters implements FunctionType{
	// Function; returns Phi(u,p)-u
	
	public PoorMans(){
	}
	
	public double[] evaluate(double[] u, double[] p, MacroState macro, String[] files, boolean output){
		int numOfOrders = u.length;
		double[] f = new double[numOfOrders];
		MacroDescription l  = new MacroOperator();
		l.Lift(u, p);

		for (int i=0; i<numOfOrders; i++) {
			macro.X[i]						= u[i];
			macro.mean[i] 				= l.getMean(i);
			macro.stdDev[i] 			= l.getStandardDev(i);
			macro.var[i] 					= l.getVariance(i);
			macro.stdErr[i] 			= l.getStandardErr(i);
			macro.median[i]				= l.getMedian(i);
			macro.liftAverage[i] 	= l.Restrict(i);
			f[i] 							    = macro.liftAverage[i] - macro.X[i];
			macro.solution[i] 	  = (Double) f[i];
		}
		macro.F					= l.getPhi();	
		macro.parameter = p[0];
		macro.size 			= l.getSize();
	
		if (output==true)
			l.printOut(files[0]);	// output realisation distribution for converge solutions only
			
		return f;
	}
	// NULL methods to use one interface (and therefore one Newton-GMRES code)
	public double funcArcLength(double[] alpha, double[] v, double[] v0){ 
		double temp = -1000000.0;
		return temp; 
	} 
	public double Norm(double[] x, int normVal){ 
		double temp = 0.0;
		return temp;
	}
	public double Norm(double x, int normVal){ 
		double temp = 0.0;
		return temp;
	} 
}
