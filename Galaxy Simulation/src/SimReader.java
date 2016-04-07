import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by samhollenbach on 2/15/16.
 */
public class SimReader extends JFrame implements KeyListener{

    JFrame frame;
    int windowWidth;
    int windowHeight;
    boolean isRunning = true;
    boolean paused = false;
    final int fps = 60;
    File f;
    int particleNumber = 3;
    BufferedReader br;
    BufferedImage backBuffer;
    boolean loop = true;


    public SimReader(File f){
        this.f = f;
    }


    public static void main(String[] args) {
        SimReader sr = new SimReader(new File("sim_data3.txt"));
        sr.run();
    }

    void initializeReader(){
        try {
            br = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Data file not found");
        }
    }

    void run(){
        initialize();

        initializeReader();

        //TODO: Process heading line
        /*String heading;
        try {
            heading = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        while(isRunning)
        {
            long time = System.currentTimeMillis();

            //  delay for each frame  -   time it took for one frame
            time = (1000 / fps) - (System.currentTimeMillis() - time);

            if(!paused){
                readIteration(br);
            }

            //System.out.println(time);
            if (time > 0)
            {
                try{
                    Thread.sleep(time);
                }catch(Exception e){}
            }
        }

        setVisible(false);
        System.exit(0);
    }

    void initialize(){
        frame = this;
        windowWidth = 900;
        windowHeight = 500;
        setTitle("Galaxy Sim");
        setSize(windowWidth, windowHeight);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        addKeyListener(this);

        backBuffer = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
    }

    /*
    * Reads file data for each iteration of the simulation.
    *
    *
    */
    void readIteration(BufferedReader br){
        Graphics g = getGraphics();
        Graphics2D bbg = (Graphics2D)backBuffer.getGraphics();

        //ParticleDrawData[] pdd = new ParticleDrawData[]

        bbg.setColor(Color.BLACK);
        bbg.fillRect(0,0,windowWidth,windowHeight);

        for(int i = 0; i < particleNumber; i++){

            //System.out.println("test2-" + i);





            String fileLine = null;
            try {
                if((fileLine = br.readLine()) == null){
                    if(loop){
                        initializeReader();
                    }
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            String[] data = fileLine.split(",");
            Double[] numData = new Double[data.length];
            for(int j = 0; j < data.length; j++){
                int index = data[j].indexOf("=");
                numData[j] = Double.parseDouble(data[j].substring(index+1,data[j].length()));

            }

            //Gets location to draw
            int[] screenLocation = translateCoordinatesToScreen(numData[2],numData[3],numData[4]);

            bbg.setColor(Color.WHITE);
            bbg.drawString(String.valueOf(numData[0]),30,50);

            //Checks what color the particle should be
            Color particleColor = numData[5] == 1 ? Color.RED : Color.GREEN;
            bbg.setColor(particleColor);



            //TODO: Probably changing to 3D at some point, but for now seen from above on Y axis, viewing X and Z axes
            //Also set at 1 pixel size particle right now, but can adjust for what looks best when closer to finished
            bbg.fillOval(screenLocation[0],screenLocation[1],10,10);
            //System.out.println("test1");

        }


        g.drawImage(backBuffer, 0, 0, this);

        bbg.dispose();
        g.dispose();

    }

    //TODO: do this...
    //Takes the 3D coordinates of the particle and translates it to the simulation screen
    public int[] translateCoordinatesToScreen(double x, double y, double z){
        int screenCenterX = windowWidth/2;
        int screenCenterY = windowHeight/2;
        double scale = 1e-4;
        int screenX = screenCenterX + (int)(scale*x);
        int screenY = screenCenterY + (int)(scale*y);
        int[] coords = {screenX,screenY};
        return coords;

    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SPACE){
            paused = !paused;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
