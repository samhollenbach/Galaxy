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
    static Star[] stars;
    int starIDCount = 0;
    static int iterations = 100;
    static int particleNumber = 100;
    static double timeStep; //(seconds)
    static double timeStepYrs;
    int currentIteration = 0;

    Galaxy andromeda;
    Galaxy milkyWay;
    PrintWriter writer;
    ArrayList<DarkMatterSource> darkMatterSources = new ArrayList<DarkMatterSource>();

    private boolean testing = false;


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


    public void initializeOneGalaxy(){
        milkyWay = new Galaxy(15000,1000,0,0,0,particleNumber-1,this);
        milkyWay.setVel(0,0);
        milkyWay.setColorCode(1);
        milkyWay.setStarDistribution();

    }

    public void initializeTwoGalaxies(){
        milkyWay = new Galaxy(15000,1000,-80000,-60000,0,particleNumber/2-1,this);
        andromeda = new Galaxy(15000,1000,80000,60000,0,particleNumber/2-1,this);
        milkyWay.setColorCode(1);
        andromeda.setColorCode(2);

        milkyWay.setVel(1000,100);
        andromeda.setVel(-1000,-100);
        milkyWay.setStarDistribution();
        andromeda.setStarDistribution();
    }


    //TODO: Add special cases for SMBH's
    public void runSim(int particles, int iterations, double timeStep){

        stars = new Star[particles];


        initializeOneGalaxy();
        timeStep = 1e14;

        /*initializeTwoGalaxies();
        timeStep = 8e14;*/



        timeStepYrs = timeStep /(60*60*24*365.25);


        try {
            writer = new PrintWriter("sim_data.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        simLoop(iterations, timeStep);

        writer.close();

        if(testing){
            SimReader.main(null);
        }

    }

    //Main loop for gravity interactions
    public void simLoop(int iter, double timeStep){

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


        System.out.println("**STARTING SIM LOOP**");
        long simStartTime = System.currentTimeMillis();
        long updateTime = simStartTime;



        //Main sim timestep loop
        for(int loopNumber = 0; loopNumber < iter; loopNumber++){

            milkyWay.updatePosition(timeStep);
            if(andromeda != null){
                andromeda.updatePosition(timeStep);
            }


            milkyWay.dm.updatePos();
            if(andromeda != null){
                andromeda.dm.updatePos();
            }

            //Iterate over each star
            for(Star tempStar : stars){

                Float64Vector netForce = Float64Vector.valueOf(0,0,0);

                //For each star check interaction with all other stars (aside from self)
                for(Star s : stars){
                    if(s.ID == tempStar.ID){
                        continue;
                    }

                    netForce = netForce.plus(getGravityVector(s,tempStar));
                    tempStar.netForce = netForce;
                }

                //Apply dark matter force
                for(DarkMatterSource dm : darkMatterSources){
                    if(tempStar.ID == dm.getTether().ID){
                        continue;
                    }
                    netForce = netForce.plus(getDarkMatterVector(tempStar,dm));
                }

                tempStar.netForce = netForce;

                //System.out.println("NET FORCE " + tempStar.ID + " is " + tempStar.netForce);
            }

            //Do moves
            calculateMovesFromForce(timeStep);

            //Completion updates
            if(System.currentTimeMillis()-updateTime >= 60000){
                System.out.println("Update: " + loopNumber + " iterations complete" );
                updateTime = System.currentTimeMillis();
            }
            if(loopNumber == iter/4){
                System.out.println("Loop 25% Complete: " + loopNumber + "/" + iter + " lines processed in " + (System.currentTimeMillis()-simStartTime)/1000. + "s");
            }else if(loopNumber == iter/2){
                System.out.println("Loop 50% Complete: " + loopNumber + "/" + iter + " lines processed in " + (System.currentTimeMillis()-simStartTime)/1000. + "s");
            }else if(loopNumber == iter*3/4){
                System.out.println("Loop 75% Complete: " + loopNumber + "/" + iter + " lines processed in " + (System.currentTimeMillis()-simStartTime)/1000. + "s");
            }
        }
        System.out.println("**SIM LOOP COMPLETED IN " + (System.currentTimeMillis()-simStartTime)/1000. + "s**");

    }


    //Gravity vector on s1 from the force of gravity from s2
    /// F = G(m1*m2)/r^2
    public static Float64Vector getGravityVector(Star s1, Star s2){

        double p1X = s1.getXInMeters();
        double p2X = s2.getXInMeters();
        double p1Y = s1.getYInMeters();
        double p2Y = s2.getYInMeters();
        double p1Z = s1.getZInMeters();
        double p2Z = s2.getZInMeters();

        double Fg = (G * 1.988e30*s1.mass * 1.988e30*s2.mass) / (((p2X-p1X)*(p2X-p1X))
            + ((p2Y-p1Y)*(p2Y-p1Y)) + ((p2Z-p1Z)*(p2Z-p1Z))); //kg*m/s^2

        Float64Vector vg = Float64Vector.valueOf((p2X-p1X),(p2Y-p1Y),(p2Z-p1Z)); //meters
        double scalar = -Fg/vg.normValue();
        vg = vg.times(scalar); //kg m/s^2

        return vg;
    }

    public Float64Vector getDarkMatterVector(Star s, DarkMatterSource dm){
        Float64Vector f = Float64Vector.valueOf(dm.x-s.posX,dm.y-s.posY,dm.z-s.posZ);
        double scalar = dm.getForceFromDM(s)/(f.normValue());
        f = f.times(scalar);
        return f;
    }



    public void calculateMovesFromForce(double timeStep){
        for(Star s : stars){
            Float64Vector a = s.netForce.times(1/s.getMassInKG()); //m/s^2
            s.velocity = s.velocity.plus(a.times(timeStep)); //m/s

            double metersToPC = 3.2408e-17;
            s.posX += (s.velocity.getValue(0)*timeStep*metersToPC);//m -> pc
            s.posY += (s.velocity.getValue(1)*timeStep*metersToPC);
            s.posZ += (s.velocity.getValue(2)*timeStep*metersToPC);
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

            String w = "iter=" + currentIteration + ",id=" + i + ",X=" + stars[i].posX +
                    ",Y=" + stars[i].posY + ",Z=" + stars[i].posZ + ",c=" + stars[i].colorCode;

            writer.println(w);
            //System.out.println(w);
            //System.out.println("VELX " + i + ": " + stars[i].velocity.get(0));
            /*writer.println(String.format("iteration=%d,id=%d,posX=%d,posY=%d,posZ=%d,color=%d",
                    currentIteration,i,stars[i].posX,stars[i].posY,stars[i].posZ,stars[i].colorCode));*/
        }
        currentIteration++;
    }

    //TODO: Add any other necessary data for the SimReader to begin its iterations
    public void writeFileHeading() throws FileNotFoundException, UnsupportedEncodingException{
        writer.println(String.format("HEAD:particles=%d",particleNumber));
    }



}
