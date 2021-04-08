class FixedPoint extends ContinuationParameters implements FunctionType{
	// Function; returns Phi(u,p)-u
	
	public FixedPoint(){
	}
	
	public double[] evaluate(double[] u, double[] p, double h, int steps, double timeHorizon, MacroState b, String[] files, boolean output){
		int numOfOrders = u.length;
		double[] f = new double[numOfOrders];
		MacroDescription l  = new MacroOperator();
		l.Lift(u, p, steps, timeHorizon);

		b.setF( l.getPhi() );	
		b.setConvergedParameter( p[0] );
		//b.setSize( l.getSize() );
		
		for (int i=0; i<numOfOrders; i++) {
			b.setConvergedState( u[i], i );
			b.setMean( l.getMean(i), i );
			b.setStandardDeviation( l.getStandardDev(i), i );
			b.setVariance( l.getVariance(i), i );
			b.setStandardError( l.getStandardErr(i), i );
			b.setMedian( l.getMedian(i), i );
			b.setLift( l.Restrict(i), i );
			f[i] = b.getLift(i) - b.getConvergedState(i);
			b.setSolution( (Double) f[i], i );
		}
		
		if (output==true)
			l.printOut(files[0]);	// output realisation distribution for converge solutions only
	
		
		return f;
	}
	
	public double[] evaluatePseudoArc(double[] v, double[] p, double[] alpha, double[] v1, double h, int steps, double timeHorizon, MacroState b, String[] files, boolean output, boolean record){
	
		int numOfOrders = v.length-1;
		double[] f = new double[numOfOrders+1];
		double[] u = new double[numOfOrders];
		for (int i=0; i<numOfOrders; i++)
			u[i] = v[i];
		
		MacroDescription l  = new MacroOperator();
		l.Lift(u, p, steps, timeHorizon);

		// evalutate fixed point
		for (int i=0; i<numOfOrders; i++){
				f[i] 							= l.Restrict(i) - u[i];
				b.setSolution( (Double) f[i], i );
		}
		
		//if (record==true){	
			b.setF( l.getPhi() );	
			b.setConvergedParameter( p[0] );
			//b.setSize( l.getSize() );
			for (int i=0; i<numOfOrders; i++) {
				b.setConvergedState( u[i], i );
				b.setMean( l.getMean(i), i );
				b.setStandardDeviation( l.getStandardDev(i), i );
				b.setVariance( l.getVariance(i), i );
				b.setStandardError( l.getStandardErr(i), i );
				b.setMedian( l.getMedian(i), i );
				b.setLift( l.Restrict(i), i );
			}
		
			// is solution far from guess, penalise convergened value
			double range = RANGE;
			double max = 0.0;
			for (int i=0; i<numOfOrders; i++) 						// take largest of the two 
				if ( b.getStandardError(i) > max )		  	  // needs to be related to ds so range 
					max = b.getStandardError(i); 					    // is non zero for deterministic systems
			if (continuationStep > max )	
				max = continuationStep;
			
			if ( Math.abs( 
					Math.abs( b.getConvergedParameter() )-Math.abs(v1[numOfOrders]) ) 
					> range*continuationStep ){
				for (int i=0; i<numOfOrders; i++)
					b.setSolution( null, i );
			}
					
			for (int i=0; i<numOfOrders; i++){ // take largest of the two 
				if ( Math.abs( 
						Math.abs(v1[i])-Math.abs( b.getLift(i) ) ) > range*max ){
					b.setSolution( null, i );
				}
			
			}
			
		// record distribution values (need to re-lift for this so the above values correspond to this distribution
				
			if (output==true)
				l.printOut(files[0]);	// output realisation distribution for converge solutions only
	//	}
	
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