import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EchoThread extends Thread {
    protected Socket socket;
    final int REQUIRED_ANSWERS=5000;

    public EchoThread(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {
        InputStream inp = null;
        BufferedReader brinp = null;
        DataOutputStream out = null;
        try {
        	socket.setSoTimeout(5000);
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
            out.writeBytes("I'm a bit confused. Can you tell me which base is being used for this math? I know it's between 2 and 16..." + "\n\r");
        } 
        catch (IOException e) {
        	e.printStackTrace();
        }
 
        String line;
        String equation = getMath();
        String answer = equation.substring(equation.lastIndexOf(";") + 1);
        equation = equation.substring(0, equation.lastIndexOf(";"));
        boolean solved=false;
        int count=0;
        try {
			out.writeBytes(equation + "\n\r");
			out.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        while (count < REQUIRED_ANSWERS) {
            try {
                line = brinp.readLine();

                if ((line == null) || !(testMath(equation,Integer.parseInt(line)))) {
                	out.writeBytes("WRONG" + "\n\r");
                    socket.close();
                    return;
                } 
                else if (count < REQUIRED_ANSWERS) {
                	count++;
                	equation = getMath();
                	answer = equation.substring(equation.lastIndexOf(";") + 1);
                	equation = equation.substring(0, equation.lastIndexOf(";"));
                    //out.writeBytes(equation.substring(0, equation.lastIndexOf(";")-1) + "\n\r");
                	out.writeBytes(equation + "\n\r");
                    out.flush();
                }
            } catch (IOException e) {
            	try {
					out.writeBytes("WRONG" + "\n\r");
					out.flush();
					socket.close();
				} 
            	catch (IOException e1) {}
                return;
            }
            catch (NumberFormatException e) {
            	try {
					out.writeBytes("WRONG" + "\n\r");
					out.flush();
					socket.close();
				} 
            	catch (IOException e1) {}
                return;
            }
        }
        try {
			if(count >= REQUIRED_ANSWERS) {
				out.writeBytes("flag{hungry_mathematicians_binomial}" + "\n\r");
				out.flush();
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    // Incredibly inefficient, but who cares
    private boolean testMath(String equation, int base) {
    	String term1="", term2="", soln="";
    	long t1=0,t2=0,s=0;
    	int op = 0;
    	if(equation.contains("+")) {
    		op=1;
    		term1 = equation.substring(0,equation.indexOf("+")-1);
    		term2 = equation.substring(equation.indexOf("+")+2,equation.indexOf("=")-1);
    		soln = equation.substring(equation.indexOf("=")+2);
    	}
    	
    	else if(equation.contains("-")) {
    		op=2;
    		term1 = equation.substring(0,equation.indexOf("-")-1);
    		term2 = equation.substring(equation.indexOf("-")+2,equation.indexOf("=")-1);
    		soln = equation.substring(equation.indexOf("=")+2);
    	}
    		
    	else if(equation.contains("*")) {
    		op=3;
    		term1 = equation.substring(0,equation.indexOf("*")-1);
    		term2 = equation.substring(equation.indexOf("*")+2,equation.indexOf("=")-1);
    		soln = equation.substring(equation.indexOf("=")+2);
    	}
    		
    	else if(equation.contains("/")) {
    		op=4;
    		term1 = equation.substring(0,equation.indexOf("/")-1);
    		term2 = equation.substring(equation.indexOf("/")+2,equation.indexOf("=")-1);
    		soln = equation.substring(equation.indexOf("=")+2);
    	}
    	
    	if(base != 10) {
    		t1 = convertToDecimal(term1, base);
        	t2 = convertToDecimal(term2, base);
        	s = convertToDecimal(soln, base);
    	}
    	else {
    		try {
    			t1 = Integer.parseInt(term1);
        		t2 = Integer.parseInt(term2);
        		s = Integer.parseInt(soln);
    		}
    		catch (NumberFormatException nfe) {
    			System.out.println("NFE IN TEST");
    		}
    	}
    	
    	switch (op) {
	    	case 1: 
	    		return ((t1 + t2) == s);
	    	case 2: 
	    		return ((t1 - t2) == s);
	    	case 3: 
	    		return ((t1 * t2) == s);
	    	case 4: 
	    		return ((t1 / t2) == s);
	    	default:
	    		return false;
		}
    	
    	//return ((decimalToBase(s,base)).equals(soln));
    }
    
    private String getMath() {
    	String math = "";
    	
    	long term1 = randInt(1,1234);
    	long term2 = randInt(1,1234);
    	int base = randInt(2,16);

    	int op = randInt(1,4);
    	
    	//debug
    	/*
    	term1 = 320;
    	term2 = 5;
    	op = 1;
    	base = 16;
		*/
    	
    	switch (op) {
	    	case 1: 
	    		math = decimalToBase(term1,base) + " + " + decimalToBase(term2,base) + " = " + decimalToBase((term1 + term2),base); 
	    		break;
	    	case 2: 
	    		//swap if negative solution
	    		if((term1 - term2) < 0) {
	    			long tmp;
	    			tmp = term1;
	    			term1 = term2;
	    			term2 = tmp;
	    		}
	    		math = decimalToBase(term1,base) + " - " + decimalToBase(term2,base) + " = " + decimalToBase((term1 - term2),base); 
	    		break;
	    	case 3: 
	    		math = decimalToBase(term1,base) + " * " + decimalToBase(term2,base) + " = " + decimalToBase((term1 * term2),base); 
	    		break;
	    	case 4: 
	    		long term3 = term1 * term2;
	    		math = decimalToBase(term3,base) + " / " + decimalToBase(term1,base) + " = " + decimalToBase((term2), base); 
	    		break;
    	}
    	return math + ";" + base;
    }
    
    private int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
    
    static String decimalToBase(long num, int base) {
    	return Long.toString(num, base).toUpperCase();
    }
    
    //http://stackoverflow.com/questions/28000220/converting-a-number-in-any-base-to-decimal-in-java
    public static long convertToDecimal(String str, int base) {
    	return Long.parseLong(Integer.toString(Integer.parseInt(str, base), 10));
    }
        
}