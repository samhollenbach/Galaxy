import org.jscience.mathematics.vector.*;

/**
 * Created by samhollenbach on 3/11/16.
 */
public class VectorTest {

    public static void main(String[] args) {

        Float64Vector v = Float64Vector.valueOf(5,6,7);
        System.out.println(v.getValue(0));

        String fileLine = "iteration=5,id=12,posX=3.41,posY=341.5,posZ=3123.3,color=1";

        String[] data = fileLine.split(",");
        for(int j = 0; j < data.length; j++){
            int index = data[j].indexOf("=");
            data[j] = data[j].substring(index+1,data[j].length());

            //System.out.println(data[j]);
        }

    }

    public static double getVecMagnitude(Float64Vector v){
        return Math.sqrt(Math.pow(v.getValue(0),2) + Math.pow(v.getValue(1),2) + Math.pow(v.getValue(2),2));
    }

}
