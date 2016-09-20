/**
 * Created by Sam Hollenbach on 4/9/2016.
 */
public class DarkMatterSource {

    double x, y, z;
    Star tether;



    public DarkMatterSource(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public DarkMatterSource(Star tether) {
        this.tether = tether;
        this.x = tether.posX;
        this.y = tether.posY;
        this.z = tether.posZ;
    }



    public double getForceFromDM(Star s){
        double r = 3.086e16*distanceFrom(s.posX,s.posY,s.posZ);

        //System.out.println(r);
        /*double intMass = 0;
        for(Star s1 : SimMain.stars){
            if(s1 == s || s1.smbh) {
                continue;
            }
            if(3.086e16*distanceFrom(s1.posX,s1.posY,s1.posZ) < r){
                intMass += s1.mass*1.988e30;
            }

        }*/
        double coef = 1e0;
        //TODO: FIGURE OUT WHY SIM STOPS AFTER LIKE 100
        //System.out.println(Math.abs(intMass*(Math.pow(s.velocity.normValue(),2) - SimMain.G*intMass)/Math.pow(r,2)));

        if(s.smbh){
            return coef*(5e32*Math.pow(220000,2)/r);
        }else if(r < 100*3.086e16){
            return 0;
        }else{
            //TODO: Find projection of net force toward center only
            //System.out.println("test " + s.getMassInKG());
            return coef*(s.getMassInKG()*Math.pow(100000,2)/r);
            //return coef*Math.abs(intMass*(Math.pow(s.velocity.normValue(),2) - SimMain.G*intMass)/Math.pow(r,1));
        }

    }

    public double distanceFrom(double x, double y, double z){
        return Math.sqrt(Math.pow(this.x-x,2)+Math.pow(this.y-y,2)+Math.pow(this.z-z,2));
    }

    public void updatePos(){
        this.x = tether.posX;
        this.y = tether.posY;
        this.z = tether.posZ;
    }

    public Star getTether(){
        return tether;
    }


}
