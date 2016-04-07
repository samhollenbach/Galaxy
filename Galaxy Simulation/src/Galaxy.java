import java.util.Random;

/**
 * Created by samhollenbach on 3/11/16.
 */
public class Galaxy {


    double width, height;
    double centerX, centerY, centerZ;
    int numStars;
    SimMain sm;



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
    }


    //TODO: Set star distribution in galaxy based on measured distribution
    public void setStarDistribution(){

        for(int i = 0; i < numStars; i++){
            double distanceFromCenter = getStarDistributionRandomNumber(1,15000);
            double Rsqr = distanceFromCenter*distanceFromCenter;
            double m = Math.random();
            double randSign = Math.random();

            double Y = Math.sqrt(Rsqr/(1+(m*m)));
            double X = Math.sqrt(Rsqr - (Y*Y));

            if(randSign < 0.25){
                Y = -Y;
                X = -X;
            }else if(randSign < 0.5){
                Y = -Y;
            }else if(randSign < 0.75){
                X = -X;
            }

            //Mass in Solar Masses, Positions in pc
            addStar(new Star(sm.starIDCount,1,X,Y,0,this));



//              X^2 + Y^2 = R^2
//             (mY)^2 + Y^2 = R^2
//             (m^2)Y^2 + Y^2 = R^2
//             (1+m^2)Y^2 = R^2
//             Y^2 = R^2/(1+m^2)
//            ** Y = √(R^2/(1+m^2))
//            ** X = √(R^2 - Y^2)

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
            randomMultiplier += (int)starDensityFunction(i);//yourFunction(startIndex) + yourFunction(startIndex + 1) + .. yourFunction(stopIndex -1) + yourFunction(stopIndex)
        }
        Random r = new Random();
        double randomDouble = r.nextDouble() * randomMultiplier;

        //For each possible integer return value, subtract yourFunction value for that possible return value till you get below 0.  Once you get below 0, return the current value.
        int yourFunctionRandomNumber = startIndex;
        randomDouble = randomDouble - starDensityFunction(yourFunctionRandomNumber);
        while (randomDouble >= 0) {
            yourFunctionRandomNumber++;
            randomDouble = randomDouble - starDensityFunction(yourFunctionRandomNumber);
        }

        return yourFunctionRandomNumber;
    }

    /**
     *
     * @param N (random number for distribution)
     * @return R (radius from galactic center)
     */


    public static double starDensityFunction(int N){
        int hR = 3000;
        return -hR*Math.log(N);
    }


    //Set star initial velocities (220km/s outside inner 1kpc)
    //TODO: Find how to make the velocity perpendicular to the center

    public void setStarVelocities(){

    }
}
