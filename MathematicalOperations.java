public class MathematicalOperations {

	/* The division function for numbers with double type */
	public double div(double x, double y) {
		return x / y;
	}

	/* The multiplication function for numbers with double type */
	public double multiply(double x, double y) {
		return x * y;
	}
	
	/* The square function for numbers with double type */
	public double square(double num) {
		return num * num;
	}

	//The remainder function for number withh int type
	public int remainder (int x, int y){
		return x % y;
	}

	/* The add function for numbers with double type */
	public double add(double x, double y){
		return x+y;		
	}
    //The power function for numbers with duoble type
    public double power(double x, double y){
        double result = 1;
        for (int i=0; i<y; i++){
            result = result*x;
        }
        return result;
    }
}
