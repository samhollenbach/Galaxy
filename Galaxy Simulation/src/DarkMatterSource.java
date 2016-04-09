/**
 * Created by Sam Hollenbach on 4/9/2016.
 */
public class DarkMatterSource {

    double x, y, z;


    //TODO: MAKE SOURCE FOLLOW GALACTIC CENTERS
    public DarkMatterSource(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public double getForceFromDM(double x, double y, double z){
        double r = distanceFrom(x,y,z);
        double coef = 6e20;

        //System.out.println(r);

        if(r > 3000){
            //return Math.abs(coef*Math.exp(Math.pow((r/3000-3000),2)));
            return coef*3000/r;
        }else if(r < 0.1){
            return 1;
        }else{
            return Math.abs(coef*Math.pow(r+10,1/6));
        }
    }

    public double distanceFrom(double x, double y, double z){
        return Math.sqrt(Math.pow(this.x-x,2)+Math.pow(this.y-y,2)+Math.pow(this.z-z,2));
    }



}
