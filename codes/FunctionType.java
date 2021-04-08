// interface used so we can use alternative Operators
interface FunctionType {
	public double[] evaluate				 (double[] u, double[] p, double h, int steps, double timeHorizon, MacroState b, String[] files, boolean output);
	public double[] evaluatePseudoArc(double[] u, double[] p, double[] alpha, double[] v1, double h, int steps, double timeHorizon, MacroState b, String[] files, boolean output, boolean record);
	public double funcArcLength(double[] alpha, double[] v, double[] v0);
	public double Norm(double[] x, int normVal);
	public double Norm(double x, int normVal);
}

