import org.jscience.mathematics.number.Real;
import org.jscience.mathematics.vector.*;
/**
 * Created by samhollenbach on 2/22/16.
 */
public class Star {

    int ID;
    double mass; //In solar masses
    double posX,posY,posZ; //in parsecs, origin placement tbd
    Float64Vector velocity; //In km/s
    Float64Vector netForce;
    Galaxy origin;
    int colorCode;
    boolean smbh = false;

    public Star(int ID, double mass, double posX, double posY, double posZ, Galaxy origin) {
        this.ID = ID;
        this.mass = mass;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.origin = origin;
    }

    public void setSMBH(){
            smbh = true;
    }

    public Float64Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Float64Vector velocity) {
        this.velocity = velocity;
    }

    public Float64Vector getNetForce() {
        return netForce;
    }

    public void setNetForce(Float64Vector netForce) {
        this.netForce = netForce;
    }


    public int getColorCode() {
        return colorCode;
    }

    public void setColorCode(int colorCode) {
        this.colorCode = colorCode;
    }

    public double getMassInKG(){
        return 1.988e30*mass;
    }

    public double getXInMeters(){
        return (3.086e16*posX);
    }

    public double getYInMeters(){
        return (3.086e16*posY);
    }

    public double getZInMeters(){
        return (3.086e16*posZ);
    }


}
