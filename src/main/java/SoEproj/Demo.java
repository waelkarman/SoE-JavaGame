package SoEproj;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Demo extends Board implements Runnable {

    private Boolean posizionato;
    private int stage;
    private Timer t;
    private TimerTask task;
    private int interstage;
    private boolean interstageEnd;
    private boolean lock;
    private static Demo istance = null; //Vittorio

    private Demo(int shipType, JPanel p, boolean m, int level, int km, boolean mp){
        super(shipType, p, m, level, km, mp);
        addKeyListener(new TAdapter());
        posizionato = false;
        stage = 0;
        interstage = -1;
        interstageEnd = true;
        t = new Timer();
        lock = false;
    }


    public static Demo setDemo(int shipType, JPanel p, boolean m, int level, int km, boolean mp){

        if(istance == null){
            istance = new Demo(shipType,p, m, level,km, mp);
        }
        return istance;

    }

    public static void resetDemo(){ //Vittorio ho cambiato da privato a pubblico
        istance = null;
    }



    @Override
    public void initGame(int shipType) {
        gameState = GameStateEnum.IN_GAME;
        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));

        spaceShips = new ArrayList<SpaceShip>();
        spaceShips.add( new SpaceShip(630, B_HEIGHT/2, shipType, isMusicOn, keyModality) );

        packs = new LinkedList<UpgradePack>();
        aliens = new ArrayList<Alien>();
    }

    public void setInterStageEnd(boolean finish){
        this.interstageEnd = finish;
    }

    public void setStage(int stg){
        this.stage = stg;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(gameState == GameStateEnum.IN_GAME) {        // draw background and game elements
            drawInterface(g);
            InterStage(g);
        }
        else if(gameState == GameStateEnum.GAME_LOST) {   // draw game over background gif after the lose condition
            EndGameFunction(0);     // passing 0 to draw game over background
        }
        Toolkit.getDefaultToolkit().sync();
    }

    private void InterStage(Graphics g) {
        for(int i=0; i<spaceShips.size(); i++){
            SpaceShip ship = spaceShips.get(i);
            if(interstage == 0){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/1.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()+ship.width/2, ship.getY()-100, this);
            }
            if(interstage == 1){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/2.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()+ship.width/2, ship.getY()-100, this);
            }
            if(interstage == 2){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/3.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()-100, ship.getY()+ship.height, this);    
            }
            if(interstage == 3){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/4.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()+ship.width/2, ship.getY()-100, this);
            }
            if(interstage == 4){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/5.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()-100, ship.getY()+ship.height, this);
            }
            if(interstage == 5){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/6.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()+ship.width/2, ship.getY()-100, this);
            }
            if(interstage == 6){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/7.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()-100, ship.getY()+ship.height, this);
            }
            if(interstage == 7){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/8.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()+ship.width/2, ship.getY()-100, this);
            }
            if(interstage == 8){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/9.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()-100, ship.getY()+ship.height, this);
            }
            if(interstage == 9){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/10.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()+ship.width/2, ship.getY()-100, this);
            }
            if(interstage == 10){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/11.png");
                Image image = ii.getImage();
                ImageIcon ii1 = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/11b.png");
                Image image1 = ii1.getImage();
                g.drawImage(image, ship.getX()+ship.width/2, ship.getY()-100, this);
                g.drawImage(image1, ship.getX()-100, ship.getY()+ship.height, this);
            }
            if(interstage == 11){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/12.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()-100, ship.getY()+ship.height, this);
            }
            if(interstage == 12){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/13.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()+ship.width/2, ship.getY()-100, this);
            }
            if(interstage == 13){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/14.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()+ship.width/2, ship.getY()-100, this);
            }
            if(interstage == 14){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/15.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()-100, ship.getY()+ship.height, this);
            }
            if(interstage == 15){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/16.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()+ship.width/2, ship.getY()-100, this);
            }
            if(interstage == 16){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/17.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()+ship.width/2, ship.getY()-100, this);
            }
            if(interstage == 17){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/18.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()-100, ship.getY()+ship.height, this);
            }
            if(interstage == 18){
                ImageIcon ii = new ImageIcon("./src/main/java/SoEproj/Resource/dialogo1/19.png");
                Image image = ii.getImage();
                g.drawImage(image, ship.getX()+ship.width/2, ship.getY()-100, this);
            }

        }
    }

    private void DemoGuide(int stage){
        for(int k=0; k < spaceShips.size(); k++){
            SpaceShip ship = spaceShips.get(k);
            
            if(stage == 0){
                    posizionato = ship.ForceMove(150, B_HEIGHT/2);
                    if(posizionato){
                        setInterStage(0);
                        setStage(1);
                    }
                }

            if(stage == 1){
                if(interstage == 0 && !lock){
                    lock = true;
                    interStage(4,0);
                }else if(interstage == 0 && !interstageEnd){
                    setInterStage(1);
                    lock = false;
                    interstageEnd = true;
                } 

                if(interstage == 1 && !lock){
                    lock = true;
                    interStage(4,1);
                }else if(interstage == 1 && !interstageEnd){
                    setInterStage(2);
                    lock = false;
                    interstageEnd = true;
                } 

                if(interstage == 2 && !lock){
                    lock = true;
                    interStage(4,2);
                }else if(interstage == 2 && !interstageEnd){
                    setInterStage(3);
                    lock = false;
                    interstageEnd = true;
                } 

                if(interstage == 3 && !lock){
                    lock = true;
                    interStage(4,3);
                }else if(interstage == 3 && !interstageEnd){
                    setInterStage(4);
                    lock = false;
                    interstageEnd = true;
                } 
             
                if(interstage == 4 && !lock){
                    lock = true;
                    interStage(4,4);
                }else if(interstage == 4 && !interstageEnd){
                    setInterStage(5);
                    lock = false;
                    interstageEnd = true;
                } 

                if(interstage == 5 && !lock){ 
                    lock = true;
                    interStage(4,5);
                }else if(interstage == 5 && !interstageEnd){
                    setInterStage(-1);
                    setStage(2);
                    lock = false;
                    interstageEnd = true;
                } 

            }
            
            if(stage == 2){
                posizionato = ship.ForceMove(200, B_HEIGHT/2);
                if(posizionato){
                    setStage(3);
                }
            }

            if(stage == 3){
                posizionato = ship.ForceMove(100, B_HEIGHT/2);
                if(posizionato){
                    setStage(4);
                }
            }

            if(stage == 4){
                posizionato = ship.ForceMove(150, B_HEIGHT/2);
                if(posizionato){
                    setStage(5);
                }
            }

            if(stage == 5){
                posizionato = ship.ForceMove(150, B_HEIGHT/2+50);
                if(posizionato){
                    setStage(6);
                }
            }

            if(stage == 6){
                posizionato = ship.ForceMove(150, B_HEIGHT/2-50);
                if(posizionato){
                    setStage(7);
                }
            }

            if(stage == 7){
                posizionato = ship.ForceMove(150, B_HEIGHT/2);
                if(posizionato){
                    setStage(8);
                    setInterStage(6);
                }
            }
            
            if(stage == 8){
                
                if(interstage == 6 && !lock){
                    lock = true;
                    interStage(4,6);
                }else if(interstage == 6 && !interstageEnd){
                    setInterStage(7);
                    lock = false;
                    interstageEnd = true;
                } 

                if(interstage == 7 && !lock){
                    lock = true;
                    interStage(4,7);
                }else if(interstage == 7 && !interstageEnd){
                    setInterStage(-1);
                    setStage(9);
                    lock = false;
                    interstageEnd = true;
                }

            }
            
            if(stage == 9){
                if(!lock){
                    lock = true;
                    ship.missiles.add(new Missile(ship.getX() + ship.width, ship.getY() + ship.height / 2, "Laser", "leftToRight" ));
                }else if(ship.missiles.isEmpty()){
                    setInterStage(8);
                    setStage(10);
                    lock = false;
                }
            }

            if(stage == 10){

                if(interstage == 8 && !lock){
                    lock = true;
                    interStage(4,8);
                }else if(interstage == 8 && !interstageEnd){
                    setInterStage(9);
                    lock = false;
                    interstageEnd = true;
                }

                if(interstage == 9 && !lock){
                    lock = true;
                    interStage(4,9);
                }else if(interstage == 9 && !interstageEnd){
                    setInterStage(10);
                    lock = false;
                    interstageEnd = true;
                }

                if(interstage == 10 && !lock){
                    lock = true;
                    interStage(4,10);
                }else if(interstage == 10 && !interstageEnd){
                    setInterStage(-1);
                    setStage(11);
                    lock = false;
                    interstageEnd = true;
                }
                
            }

            if(stage == 11){
                if(!lock){
                    lock = true;
                    synchronized(aliens){
                        AlienGenerator a = new AlienGenerator(background.getWidth(null),background.getHeight(null), aliens, 1);
                        a.generateAliens(B_HEIGHT/2+20);
                    }
                }else if(!aliens.isEmpty()){
                    setInterStage(11);
                    setStage(12);
                    lock = false;
                }
            }

            if(stage == 12){
                if(interstage == 11 && !lock){
                    lock = true;
                    interStage(3,11);
                }else if(interstage == 11 && !interstageEnd){
                    setInterStage(12);
                    lock = false;
                    interstageEnd = true;
                }
            
                if(interstage == 12 && !lock){
                    lock = true;
                    interStage(2,12);
                }else if(interstage == 12 && !interstageEnd){
                    setInterStage(-1);
                    setStage(13);
                    lock = false;
                    interstageEnd = true;
                }

            }


            if(stage == 13){
                if(!lock){
                    lock = true;
                    ship.missiles.add(new Missile(ship.getX() + ship.width, ship.getY() + ship.height / 2, "Laser", "leftToRight" ));  
                }else if(ship.missiles.isEmpty()){
                    setInterStage(13);
                    setStage(14);
                    lock = false;
                }
            }

            if(stage == 14){
                if(interstage == 13 && !lock){
                    lock = true;
                    interStage(4,13);
                }else if(interstage == 13 && !interstageEnd){
                    setInterStage(14);
                    lock = false;
                    interstageEnd = true;
                }

                if(interstage == 14 && !lock){
                    lock = true;
                    interStage(5,14);
                }else if(interstage == 14 && !interstageEnd){
                    setInterStage(-1);
                    setStage(15);
                    lock = false;
                    interstageEnd = true;
                }
            }


            if(stage == 15){
                if(!lock){
                    lock = true;
                    synchronized(packs){
                        packs.add(new UpgradePack(600 , 170, 5));
                    }
                }else if(!packs.isEmpty()){
                    setInterStage(15);
                    setStage(16);
                    lock = false;
                }
            }

            if(stage == 16){
                if(interstage == 15 && !lock){
                    lock = true;
                    interStage(4,15);
                }else if(interstage == 15 && !interstageEnd){
                    setInterStage(-1);
                    setStage(17);
                    lock = false;
                    interstageEnd = true;
                }
            }

            if(stage == 17){
                
                posizionato = ship.ForceMove(270, 150); 
                if(posizionato){
                    setStage(18);
                    setInterStage(16);
                }
            
            }


            if(stage == 18){
                if(interstage == 16 && !lock){
                    lock = true;
                    interStage(4,16);
                }else if(interstage == 16 && !interstageEnd){
                    setInterStage(-1);
                    setStage(19);
                    lock = false;
                    interstageEnd = true;
                }
            }

            if(stage == 19){
                if(!lock){
                    lock = true;
                    ship.missiles.add(new Missile(ship.getX() + ship.width, ship.getY() + ship.height / 2, "blueFireball", "leftToRight" ));  
                }else if(ship.missiles.isEmpty()){
                    setInterStage(17);
                    setStage(20);
                    lock = false;
                }
            }

            if(stage == 20){
                if(interstage == 17 && !lock){
                    lock = true;
                    interStage(4,17);
                }else if(interstage == 17 && !interstageEnd){
                    setInterStage(18);
                    lock = false;
                    interstageEnd = true;
                }
                
                if(interstage == 18 && !lock){
                    lock = true;
                    interStage(4,18);
                }else if(interstage == 18 && !interstageEnd){
                    setInterStage(19);
                    lock = false;
                    interstageEnd = true;
                }

                if(interstage == 19 && !lock){
                    lock = true;
                    interStage(4,19);
                }else if(interstage == 19 && !interstageEnd){
                    lock = false;
                    interstageEnd = true;
                    gameState = GameStateEnum.GAME_LOST;
                    EndGameFunction();
                }
            }

        }

    }



    public void EndGameFunction() {
        if(mumZero != null)
            mumZero.stopMusic();      
        GameMainMenu old = (GameMainMenu) SwingUtilities.getWindowAncestor(this);        
        old.getContentPane().remove(this);
        old.add(menuPanel);
        if(isMusicOn)
            old.startMusic();
        Demo.resetDemo();
        old.validate();
        old.repaint();
    }


    public void setInterStage(int n){
        this.interstage = n;
    }
    public int getText(){
        return this.interstage;
    }

    @Override
    public void run() {
        long beforeTime, timeDiff, sleep;
        beforeTime = System.currentTimeMillis();
        while (gameState != GameStateEnum.GAME_LOST) {
            
            DemoGuide(stage);
            updateShip();
            updateAliens();
            updatePacks();
            checkCollisions();
            repaint();
              

            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = DELAY - timeDiff;
            if (sleep < 0) {
                sleep = 2;
            }

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                System.out.println("Thread Board: " + e.getMessage());
            }
            beforeTime = System.currentTimeMillis();
        }
        repaint();
    }


    class TAdapter extends KeyAdapter {
        //@Override
        //public void keyReleased(KeyEvent e) {
            //try {
            //    for(int k=0;k<spaceShips.size();k++){
            //        SpaceShip Ship = spaceShips.get(k);
            //        Ship.keyReleased(e);
            //    }
            //} catch (InterruptedException e1) {
            //    e1.printStackTrace();
            //}
        //}

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();                       
            
            if (key == KeyEvent.VK_P){        
                if (isPause == false){
                    System.out.println("METTI LA PAUSA");
                    isPause = true;
                    pauseGameFunction();
                }
                
            }

            //try{
            //    for(int k=0; k < spaceShips.size(); k++) {
            //        SpaceShip ship = spaceShips.get(k);
            //        ship.keyPressed(e);
            //    }
            //} catch (InterruptedException e1) {
            //    e1.printStackTrace();
            //}
        }
    }

    @Override
    public void resumeGame(){         
        isPause = false;
        boardAnimator.resume();
    }


    
    private void interStage(int s, int n){ 
        setInterStage(n);
        task = new NextStage(this);
        t.schedule(task, s * 1000);
    }

    class NextStage extends TimerTask  {
        Demo d;

        public NextStage(Demo demo) {
            this.d = demo;
        }
   
        @Override
        public void run() {
            d.setInterStageEnd(false);
            
        }
    }


}


