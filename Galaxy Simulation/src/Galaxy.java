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
        this.width = 150000;
        this.height = 10000;
        this.centerX = 0;
        this.centerY = 0;
        this.centerZ = 0;
    }


    public void setStarDistribution(){

        dm = new DarkMatterSource(centerX,centerY,centerZ);
        sm.darkMatterSources.add(dm);

        for(int i = 0; i < numStars; i++){
            double distanceFromCenter = getStarDistributionRandomNumber(1,(int)width);
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
            Star t = new Star(sm.starIDCount,1,X,Y,0,this);
            setStarVelocity(t);
            t.setColorCode(colorCode);
            addStar(t);
            System.out.println("Star " + sm.starIDCount + ", PosX: " + X + ", PosY: " + Y);


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

        return yourFunctionRandomNumber;
    }



    public static double starDen(int R){
        return Math.exp(-R/3000);
    }

    public static double starDensityFunction(int N){
        int hR = 3000;
        return -hR*Math.log(N);
    }


    //Set star initial velocities (220km/s outside inner 1kpc)
    //TODO: Find how to make the velocity perpendicular to the center

    public void setStarVelocity(Star s){


        double x1 = centerX - s.posX;
        double y1 = centerY - s.posY;

        Float64Vector a = Float64Vector.valueOf(x1,y1,0);
        double r = a.normValue();
        //Float64Vector a = Float64Vector.valueOf(x1,y1,0);


        double velo = 220000;

        if(r < 200){
            velo *= (r+.1)/200;
        }

        //TODO: Check initial velocity direction to assure it is perpendicular
        //Think about using dot product = 0 and solve for arbitrary x,y, then make sure all facing same way using trick below]m

        double theta = Math.atan(y1/x1);
        if(x1 < 0){
            r*=-1;
        }
        Float64Vector v = Float64Vector.valueOf(r*Math.cos(theta+1)-r*Math.cos(theta),r*Math.sin(theta+1)-r*Math.sin(theta),0);
        double speedScale = (velo / v.normValue());
        v = v.times(speedScale);
        //System.out.println("speed : " +  v.normValue());

        s.setVelocity(v);
    }

    public int getColorCode() {
        return colorCode;
    }

    public void setColorCode(int colorCode) {
        this.colorCode = colorCode;
    }
}
