// interface used so we can use alternative Operators
interface MacroDescription {
  public void Lift (double[] x, double[] parameters, int steps, double tau);
  public double Restrict(int i);
  public double getVariance(int i);
  public double getStandardDev(int i);
  public double getStandardErr(int i);
  public int getSize();
  public double getNorm();
  public double getMedian(int i);
  public double getMean(int i);
  public double[][] getPhi();
  public void setPhi(double[][] PhiNew);
  public void printOut(String name);
  public void clone( MacroDescription another );
}


