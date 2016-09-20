import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by samhollenbach on 2/15/16.
 */
public class SimReader extends JFrame implements KeyListener, MouseWheelListener {

    JFrame frame;
    int windowWidth;
    int windowHeight;
    boolean isRunning = true;
    boolean paused = false;
    final int fps = 60;
    File f;
    int particleNumber;
    BufferedReader br;
    BufferedImage backBuffer;
    boolean loop = true;
    double scale;
    double scalePow = -3;
    double scaleMin = 1e-8;
    double scaleMax = 1e3;
    double readerX = 0;
    double readerY = 0;

    double scaleBar = 0;


    public SimReader(File f){
        this.f = f;
    }


    public static void main(String[] args) {
        SimReader sr = new SimReader(new File("sim_data.txt"));
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

        scale = Math.pow(10,scalePow);

        processHeading();

        startReading();


        setVisible(false);
        System.exit(0);
    }

    void startReading(){
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
    }


    void processHeading(){
        String heading = "";
        try {
            heading = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        heading = heading.substring(15);
        System.out.println(heading);
        particleNumber = Integer.valueOf(heading);
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
        addMouseWheelListener(this);

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


            if(fileLine.startsWith("HEAD")){
                continue;
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
            double yrs1 = numData[0];
            double yrs = (int)((yrs1*SimMain.timeStepYrs)/10000.)/100.;
            //bbg.drawString("("+String.valueOf(yrs)+" Megayrs)",30,65);

            double color1 = numData[5];
            int color = (int)color1;
            Color particleColor;
            //Checks what color the particle should be
            switch (color){
                case 0: particleColor = Color.BLACK;
                    break;
                case 1: particleColor = Color.GREEN;
                    break;
                case 2: particleColor = Color.ORANGE;
                    break;
                default: particleColor = Color.WHITE;
            }

            bbg.setColor(particleColor);



            //TODO: Probably changing to 3D at some point, but for now seen from above on Y axis, viewing X and Z axes
            //Also set at 1 pixel size particle right now, but can adjust for what looks best when closer to finished

            int ovalSize = 4;


            if(color == 0){
                bbg.fillOval(screenLocation[0],screenLocation[1],ovalSize,ovalSize);
                bbg.setColor(Color.WHITE);
                bbg.drawOval(screenLocation[0],screenLocation[1],ovalSize,ovalSize);
            }else{
                bbg.fillOval(screenLocation[0],screenLocation[1],ovalSize,ovalSize);
            }
            //System.out.println("test1");

        }

        bbg.setColor(Color.WHITE);
        bbg.drawLine(200,400,450,400);
        bbg.drawString((int)(scaleBar)/100000.+"kpc",300,420);


        g.drawImage(backBuffer, 0, 0, this);

        bbg.dispose();
        g.dispose();

    }

    //TODO: do this...
    //Takes the 3D coordinates of the particle and translates it to the simulation screen
    public int[] translateCoordinatesToScreen(double x, double y, double z){
        int screenCenterX = windowWidth/2 - (int)readerX;
        int screenCenterY = windowHeight/2 - (int)readerY;

        int screenX = screenCenterX + (int)(scale*x);
        int screenY = screenCenterY + (int)(scale*y);
        int[] coords = {screenX,screenY};
        scaleBar = 25000/scale;

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
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            initializeReader();
        }

        int moveSpeed = 10;
        if(e.getKeyCode() == KeyEvent.VK_LEFT){
            readerX -= moveSpeed;
        }
        if(e.getKeyCode() == KeyEvent.VK_RIGHT){
            readerX += moveSpeed;
        }
        if(e.getKeyCode() == KeyEvent.VK_UP){
            readerY -= moveSpeed;
        }
        if(e.getKeyCode() == KeyEvent.VK_DOWN){
            readerY += moveSpeed;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        scalePow -= (double)e.getUnitsToScroll()/60;
        scale = Math.pow(10,scalePow);
        if(scale > scaleMax){
            scale = scaleMax;
        }
        if(scale < scaleMin){
            scale = scaleMin;
        }





    }
}
