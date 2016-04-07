import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.jscience.mathematics.structure.Field;
import org.jscience.mathematics.vector.*;

/**
 * Created by samhollenbach on 2/15/16.
 */
public class SimMain {



    static final double G = 6.67e-11;
    Star[] stars;
    int starIDCount = 0;
    static int iterations = 1000;
    static int particleNumber = 2;
    static int timeStep = 10000000; //(seconds)

    int currentIteration = 0;

    Galaxy andromeda;
    Galaxy milkyWay;
    PrintWriter writer;


    public static void main(String[] args) {


        SimMain sm = new SimMain();
        sm.runSim(particleNumber,iterations,timeStep);
    }


    /**
     *
     * Make simulation calculate every particle without moving them, and make a file
     * showing the different moves for every particle, and once all calculations for
     * that loop are finished, apply all moves and reset loop with new positions
     *
     *
     *
     *
     *
     */


    //TODO: Add special cases for SMBH's
    public void runSim(int particles, int iterations, int timeStep){

        stars = new Star[particles];

        milkyWay = new Galaxy(particleNumber/2,this);
        andromeda = new Galaxy(particleNumber/2,this);
        /*milkyWay.setStarDistribution();
        andromeda.setStarDistribution();*/

        stars[0] = new Star(0,1,
                1e6,1e6,
                0,milkyWay);
        stars[1] = new Star(1,1,
                -1e6,-1e6,
                0,andromeda);
        /*stars[2] = new Star(2,1,
                1e6,-1e6,
                0,andromeda);*/

        Float64Vector tempV1 = Float64Vector.valueOf(-2e13,2e13,0);
        Float64Vector tempV2 = Float64Vector.valueOf(2e13,-2e13,0);
        //Float64Vector tempV3 = Float64Vector.valueOf(-4e13,3e12,0);
        stars[0].setVelocity(tempV1);
        stars[1].setVelocity(tempV2);
        //stars[2].setVelocity(tempV3);

        try {
            writer = new PrintWriter("sim_data.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        simLoop(iterations, timeStep);

        writer.close();

    }

    //Main loop for gravity interactions
    public void simLoop(int iter, int timeStep){

        //write data about num-particles and center position of galaxies
        try {
            writeFileHeading();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            writeParticleMoves();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //Main sim timestep loop
        for(int loopNumber = 0; loopNumber < iter; loopNumber++){

            //Iterate over each star
            for(Star tempStar : stars){

                Float64Vector netForce = Float64Vector.valueOf(0,0,0);

                //For each star check interaction with all other stars (aside from self)
                for(Star s : stars){
                    if(s.ID == tempStar.ID){
                        continue;
                    }

                    netForce = netForce.plus(getGravityVector(s,tempStar));
                    //System.out.println("***** " + s.ID + " -- " + getGravityVector(s,tempStar));
                }
                tempStar.netForce = netForce;
                //System.out.println("NET FORCE " + s.ID + " is " + s.netForce);
            }
            calculateMovesFromForce(timeStep);
        }
    }


    //Gravity vector on s1 from the force of gravity from s2
    /// F = G(m1*m2)/r^2
    public Float64Vector getGravityVector(Star s1, Star s2){

        //TODO: Check this if stuff seems weird, lots of shady math goin on
        double p1X = s1.getXInMeters();
        double p2X = s2.getXInMeters();
        double p1Y = s1.getYInMeters();
        double p2Y = s2.getYInMeters();
        double p1Z = s1.getZInMeters();
        double p2Z = s2.getZInMeters();

        //System.out.println("P1X - P2X: " + p1X + " - " + p2X);


        double Fg = (G * 1.988e30*s1.mass * 1.988e30*s2.mass) / (((p2X-p1X)*(p2X-p1X))
            + ((p2Y-p1Y)*(p2Y-p1Y)) + ((p2Z-p1Z)*(p2Z-p1Z)));


        Float64Vector vg = Float64Vector.valueOf((p2X-p1X),(p2Y-p1Y),(p2Z-p1Z));
        double scalar = -Fg/vg.normValue();
        //System.out.println("vg i : " + vg);
        vg = vg.times(scalar);
        //System.out.println("vg : " + vg);




        return vg;
    }



    //TODO: Check if this is actually the right calculation for movement from the force

    //Also consider changing the way its set up to force changing the velocity, and then velocity changing position
    public void calculateMovesFromForce(int timeStep){
        for(Star s : stars){

            Float64Vector a = s.netForce.times(1/s.mass);

            //v = d/t
            //F = m*a
            //d = 1/2*a*t^2
            s.velocity = s.velocity.plus(a.times(timeStep));
            double metersToPC = 3.2408e-17;

            s.posX += (s.velocity.getValue(0)*timeStep*metersToPC);
            s.posY += (s.velocity.getValue(1)*timeStep*metersToPC);
            s.posZ += (s.velocity.getValue(2)*timeStep*metersToPC);

            /*s.posX = 0.5 * a.getValue(0) * timeStep * timeStep;
            s.posY = 0.5 * a.getValue(1) * timeStep * timeStep;
            s.posZ = 0.5 * a.getValue(2) * timeStep * timeStep;*/

        }



        try {
            writeParticleMoves();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    /*
     *
     * Write ID, Position, and color data for each iteration of the simulations
     *
     * Color code corresponds to which galaxy the star originated from
     * 0 for Milky Way, 1 for Andromeda
     *
     *
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    //TODO:
    public void writeParticleMoves() throws FileNotFoundException, UnsupportedEncodingException {

        for(int i = 0; i < stars.length; i++){

            String w = "iteration=" + currentIteration + ",id=" + i + ",posX=" + stars[i].posX +
                    ",posY=" + stars[i].posY + ",posZ=" + stars[i].colorCode + ",color=" + stars[i].posZ;

            writer.println(w);
            System.out.println(w);
            //System.out.println("VELX " + i + ": " + stars[i].velocity.get(0));
            /*writer.println(String.format("iteration=%d,id=%d,posX=%d,posY=%d,posZ=%d,color=%d",
                    currentIteration,i,stars[i].posX,stars[i].posY,stars[i].posZ,stars[i].colorCode));*/
        }
        currentIteration++;
    }

    //TODO: Add any other necessary data for the SimReader to begin its iterations
    public void writeFileHeading() throws FileNotFoundException, UnsupportedEncodingException{
        PrintWriter writer = new PrintWriter("sim_data.txt", "UTF-8");
        writer.println(String.format("particles=%d",particleNumber));
    }



}
