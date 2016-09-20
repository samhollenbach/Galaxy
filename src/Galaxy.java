import org.jscience.mathematics.vector.Float64Vector;

import java.util.Random;

/**
 * Created by samhollenbach on 3/11/16.
 */
public class Galaxy {


    double width, height;
    double centerX, centerY, centerZ;
    int numStars;
    SimMain sm;
    int colorCode;
    DarkMatterSource dm;

    Float64Vector vel;



    public Galaxy(double width, double height, double centerX, double centerY, double centerZ, int numStars, SimMain sm) {
        this.width = width;
        this.height = height;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.numStars = numStars;
        this.sm = sm;
    }

    public Galaxy(int numStars, SimMain sm) {
        this.numStars = numStars;
        this.sm = sm;
        this.width = 15000;
        this.height = 1000;
        this.centerX = 0;
        this.centerY = 0;
        this.centerZ = 0;

    }

    public void setVel(int x, int y){
        vel = Float64Vector.valueOf(x,y,0);
    }


    public void setStarDistribution(){

        Star smbh = new Star(sm.starIDCount,1e8,
                centerX,centerY,
                centerZ,this);
        smbh.setColorCode(0);
        smbh.setSMBH();
        addStar(smbh);
        smbh.setVelocity(vel);

        dm = new DarkMatterSource(smbh);
        sm.darkMatterSources.add(dm);

        System.out.println("Setting Galaxy Star Distribution for Galaxy " + getColorCode());


        for(int i = 0; i < numStars; i++){
            double distanceFromCenter = getStarDistributionRandomNumber(1,(int)width) + 150;
            System.out.println(distanceFromCenter);
            double Rsqr = distanceFromCenter*distanceFromCenter;
            double m = Math.random()*2+1;
            double randSign = Math.random();

            double Y = Math.sqrt(Rsqr/(m*m));
            double X = Math.sqrt(Rsqr - (Y*Y));
            X *= 5;
            Y *= 5;

            if(randSign < 0.25){
                Y = -Y;
                X = -X;
            }else if(randSign < 0.5){
                Y = -Y;
            }else if(randSign < 0.75){
                X = -X;
            }

            //Mass in Solar Masses, Positions in pc
            double starMass = 100 * (0.8+Math.random()*10);

            Star t = new Star(sm.starIDCount,starMass,centerX+X,centerY+Y,0,this);
            setStarVelocity(t);
            t.setColorCode(colorCode);
            addStar(t);
            //System.out.println("Star " + sm.starIDCount + ", PosX: " + X + ", PosY: " + Y);


        }
    }

    public void addStar(Star s){
        sm.stars[sm.starIDCount] = s;
        sm.starIDCount++;
    }


    /**
     *
     * @param startIndex = 0
     * @param stopIndex = number of stars
     * @return
     */
    public static int getStarDistributionRandomNumber(int startIndex, int stopIndex) {
        //Generate a random number whose value ranges from 0.0 to the sum of the values of yourFunction for all the possible integer return values from startIndex to stopIndex.
        double randomMultiplier = 0;
        for (int i = startIndex; i <= stopIndex; i++) {
            randomMultiplier += (int)starDen(i);//yourFunction(startIndex) + yourFunction(startIndex + 1) + .. yourFunction(stopIndex -1) + yourFunction(stopIndex)
        }
        Random r = new Random();
        double randomDouble = r.nextDouble() * randomMultiplier;

        //For each possible integer return value, subtract yourFunction value for that possible return value till you get below 0.  Once you get below 0, return the current value.
        int yourFunctionRandomNumber = startIndex;
        randomDouble = randomDouble - starDen(yourFunctionRandomNumber);

        while (randomDouble >= 0) {
            yourFunctionRandomNumber++;
            randomDouble = randomDouble - starDen(yourFunctionRandomNumber);
        }
        //System.out.println(randomDouble);

        return yourFunctionRandomNumber;
    }



    public static double starDen(int R){
        return 5*Math.exp(-R/3000);
    }

    public static double starDensityFunction(int N){
        int hR = 3000;
        return -hR*Math.log(N);
    }


    //Set star initial velocities (220km/s outside inner 1kpc)

    public void setStarVelocity(Star s){


        double x1 = centerX - s.posX;
        double y1 = centerY - s.posY;

        Float64Vector a = Float64Vector.valueOf(x1,y1,0);
        double r = a.normValue();
        //Float64Vector a = Float64Vector.valueOf(x1,y1,0);


        //TODO: FIGURE SHIT OUT

        double velo = 100000;
        int r1 = 2000;

        if(r < r1){
            velo *= (0.5+(0.5*r)/r1);
        }

        //TODO: Check initial velocity direction to assure it is perpendicular
        //Think about using dot product = 0 and solve for arbitrary x,y, then make sure all facing same way using trick below]m

        double theta = Math.atan(y1/x1);
        if(x1 < 0){
            r*=-1;
        }

       //double yv = -(r*Math.cos(theta))*(r*Math.cos(theta+1))/y1;
        //yv = y1 < 0 ? yv : -yv;
        double tempX = -(r*Math.cos(theta-0.00001)-r*Math.cos(theta));
        double tempY = -tempX*x1/y1;


        //Float64Vector v = Float64Vector.valueOf(r*Math.cos(theta-0.00001)-r*Math.cos(theta),r*Math.sin(theta-.00001)-r*Math.sin(theta),0);
        Float64Vector v = Float64Vector.valueOf(tempX,tempY,0);

        double speedScale = (velo / v.normValue());
        v = v.times(speedScale);
        //System.out.println("speed : " +  v.normValue());
        v = v.plus(vel);
        s.setVelocity(v);
    }

    public int getColorCode() {
        return colorCode;
    }

    public void setColorCode(int colorCode) {
        this.colorCode = colorCode;
    }

    public void updatePosition(double timeStep){
        double metersToPC = 3.2408e-17;
        centerX += vel.getValue(0)*timeStep*metersToPC;
        centerY += vel.getValue(1)*timeStep*metersToPC;
        centerZ += vel.getValue(2)*timeStep*metersToPC;
    }
}
