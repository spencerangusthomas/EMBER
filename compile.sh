#!/bin/bash
clear
echo "Compiling ... "
javac -cp lib/commons-lang3-3.4.jar:lib/mtj-0.9.14.jar:lib/matrix/matrix.jar:lib/NetLogo.jar:lib/scala-library.jar:lib/Jama-1.0.3.jar: codes/Bootstrap.java codes/ContinuationParameters.java codes/Continuation.java codes/FixedPoint.java codes/FunctionType.java  codes/GaussianElimination.java codes/LeastSquaresFit.java codes/MacroDescription.java codes/MacroOperator.java codes/NewtonRaphson.java codes/MacroState.java codes/Predictor.java codes/Successful.java codes/ConfigurationPhase.java

echo "Compilations complete!"
