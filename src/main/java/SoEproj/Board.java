package SoEproj;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class Board extends JPanel implements Runnable {

    protected final int TIME_LEVEL = 60;
    protected final int B_WIDTH = 590;
    protected final int B_HEIGHT = 435;
    protected final int DELAY = 15;             // refresh rate in millis
    protected final double BG_SPEED = 0.3;      // background speed

    protected final ImageIcon alienExpl;
    protected final ImageIcon shipExpl;
    protected final File alienExplSound;
    protected final File shipExplSound;
    protected final File powerUpSound;
    protected final File bossHitSound;
    protected File boardSound;

    protected Boolean isPause;

    protected boolean isMusicOn;

    protected ImageIcon bgImgIcon;
    protected Image background;
    protected double bgShiftX;

    protected Thread threadAliensGen;      // alien generator thread
    protected AlienGenerator aliensGen;    // alien generator class
    protected Thread threadPacksGen;
    protected PackGenerator packsGen;
    protected Thread boardAnimator;

    protected GameStateEnum gameState;
    protected JPanel menuPanel;
    protected int keyModality;
    protected int level;
    protected boolean isMultiplayer;

    protected List<SpaceShip> spaceShips; 
    protected List<Alien> aliens;
    protected List<UpgradePack> packs;
    protected MusicManager mumZero;
    
    private int finalScore = 0;
    private int stage;
    private Timer t;
    private TimerTask task;
    private int interstage;
    private boolean interstageEnd;
    private boolean lock;

    protected Board(int shipType, JPanel p, boolean m, int level, int km, boolean mp) {
        this.isMultiplayer = mp;
        this.level = level;
        // Images and soundtracks initialization
        this.isMusicOn = m;         
        this.menuPanel = p;
        this.keyModality = km;       // game commands switcher
        this.isPause = false;

        alienExpl = new ImageIcon("./src/main/java/SoEproj/Resource/ExplosionAliens.png");
        shipExpl = new ImageIcon("./src/main/java/SoEproj/Resource/ExplosionShip.png");
        alienExplSound = new File("./src/main/java/SoEproj/Resource/CollisionSound.wav");
        shipExplSound = new File("./src/main/java/SoEproj/Resource/FinalCollisionSound.wav");
        powerUpSound = new File("./src/main/java/SoEproj/Resource/PowerUp.wav");
        bossHitSound = new File("./src/main/java/SoEproj/Resource/ShoothedBoss.wav");
        boardSound = new File("./src/main/java/SoEproj/Resource/MusicGame.wav");
        
        if(isMusicOn) {
            mumZero = new MusicManager(boardSound);
            mumZero.loopMusic();
        }
        if(level == 1){
            stage = 0;
            interstage = 0;
        }
        if(level == 2){
            stage = 2;
            interstage = 1;
        }
        if(level == 3){
            stage = 4;
            interstage = 2;
        }

        interstageEnd = true;
        t = new Timer();
        lock = false;

        setBackground();
        initGame(shipType);     // shipType may change with level
        gameLaunch();
    }



//---------------------------GAME INITIALIZATION----------------------------------->
    protected void setBackground() {
        if(level == 1)    
            bgImgIcon = new ImageIcon("./src/main/java/SoEproj/Resource/BackGround1.png");
        if(level == 2)
            bgImgIcon = new ImageIcon("./src/main/java/SoEproj/Resource/BackGround2.png");
        if(level == 3)
            bgImgIcon = new ImageIcon("./src/main/java/SoEproj/Resource/BackGround3.png");

        background = bgImgIcon.getImage();
    }

    public void initGame(int shipType) {
        gameState = GameStateEnum.IN_GAME;
        addKeyListener(new TAdapter());
        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));

        spaceShips = new ArrayList<SpaceShip>();
        spaceShips.add( new SpaceShip(0, B_HEIGHT/2, shipType, isMusicOn, keyModality) );
        if(isMultiplayer)
            spaceShips.add( new SpaceShip(0, B_HEIGHT/2 + 60, shipType + 1 % 3 , isMusicOn, keyModality + 1 % 2) ); // +1 % 2 for set a different type

        packs = new LinkedList<UpgradePack>();
        packsGen = new PackGenerator(B_WIDTH, B_HEIGHT, packs);
        packsGen.start();

        aliens = new ArrayList<Alien>();
    }
//---------------------------END GAME INITIALIZATION----------------------------------->

//-------------------STAGE SHIFTER-------------------------------->
public void setInterStageEnd(boolean finish){
    this.interstageEnd = finish;
}

public void setStage(int stg){
    this.stage = stg;
}

public void setInterStage(int n){
    this.interstage = n;
}
//-------------------END STAGE SHIFTER-------------------------------->

//---------------------GRAPHICS--------------------------------------------------->
    // This method will be executed by the painting subsystem whenever you component needs to be rendered
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(gameState == GameStateEnum.IN_GAME) {        // draw background and game elements
            drawInterface(g);
        }
        else if(gameState == GameStateEnum.GAME_LOST) {   // draw game over background gif after the lose condition
            EndGameFunction(0);     // passing 0 to draw game over background
        }
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawShipAndMissiles(Graphics g) {
        synchronized(spaceShips) {
            for(int i=0; i<spaceShips.size(); i++){
                SpaceShip ship = spaceShips.get(i);
                
                if (ship.isVisible()) {
                    g.drawImage(ship.getImage(), ship.getX(), ship.getY(), this);
                    if (ship.isDying()) 
                        ship.die();
                }
                synchronized(ship){
                    List<Missile> ms = ship.getMissiles();
                    for (Missile missile : ms) {
                        if (missile.isVisible()) {
                            g.drawImage(missile.getImage(), missile.getX(), missile.getY(), this);
                        }
                    }
                }
            }
        }
    }

    private void drawPacks(Graphics g) {
        synchronized(packs) {
            for(UpgradePack pack : packs) {
                if (pack.isVisible())
                    g.drawImage(pack.getImage(), pack.getX(), pack.getY(), this);
                    if (pack.isDying()) 
                        pack.die();
            }
        } 
    }

    private void drawAliensAndMissiles(Graphics g){
        synchronized(aliens) {              // they are drawn only if they have not been previously destroyed.
            for (Alien alien : aliens) {
                if (alien.isVisible()) {
                    g.drawImage(alien.getImage(), alien.getX(), alien.getY(), this);
                    if (alien.isDying()) 
                        alien.die();
                }
                synchronized(alien){
                    List<Missile> as = alien.getMissiles();
                    for (Missile missile : as) {
                        if (missile.isVisible()) 
                            g.drawImage(missile.getImage(), missile.getX(), missile.getY(), this);
                    }
                }
            }
        }        
    }

    private void drawScores(Graphics g){
        g.setColor(Color.WHITE); // In the top-left corner of the window, we draw how many aliens are left.
        int yPos = 15;
        int xPos = 5;
        
        synchronized(spaceShips) {
            for(int i = 0; i < spaceShips.size(); i++) {
                SpaceShip ship = spaceShips.get(i);

                synchronized(ship){
                    g.drawString("Player " + (i+1) + " --- Lives: " + ship.getLife() + "    Score: " + ship.getScore(), xPos, yPos);
                }
                
                yPos += 16;
            }
        }
    }

    private void drawBackground(Graphics g){
        if (bgShiftX > background.getWidth(null)) {
            bgShiftX = 0;
        } else {
            bgShiftX += BG_SPEED;
        }
        g.drawImage(background, (int) -bgShiftX, 0, null);
        g.drawImage(background, background.getWidth(null) - (int) bgShiftX, 0, null);
    }

    protected void drawInterface(Graphics g) {
        drawBackground(g);          //stampa sfondo mobile    
        drawShipAndMissiles(g);     //stampa spaceship e missili per spaceship
        drawAliensAndMissiles(g);   //stampa alieni e missili da essi sparati 
        drawScores(g);              //stampa score 
        drawPacks(g);               //stampa di tutti i pacchetti
        gameOverCondition();
    }

//-------------------------END GRAPHICS METHODS---------------------------->
    
//--------------------------LEVEL SWITCHER--------------------------------->

private void story(int stage){
    if(stage == 0){
        if(interstage == 0 && !lock){
            lock = true;
            aliensGen = new AlienGenerator(B_WIDTH, B_HEIGHT, aliens, this.level);
            aliensGen.start();
            interStage(TIME_LEVEL,0);
        }else if(interstage == 0 && !interstageEnd){
            aliensGen.Shutdown();
            setStage(1);
            lock = false;
            interstageEnd = true;
        } 
    }

    if(stage == 1){
        if(!lock){
            lock = true;
            aliensGen.generateBoss();
        }else if(aliens.isEmpty()){
            setStage(2);
            setInterStage(1);
            lock = false;
            interstageEnd = true;
        } 
    }

    if(stage == 2){
        if(interstage == 1 && !lock){
            lock = true;
            this.level = 2;
            setBackground();
            aliensGen = new AlienGenerator(B_WIDTH, B_HEIGHT, aliens, this.level);
            aliensGen.start();
            interStage(TIME_LEVEL,1);
            
        }else if(interstage == 1 && !interstageEnd){
            aliensGen.Shutdown();
            setStage(3);
            lock = false;
            interstageEnd = true;
        } 
    }

    if(stage == 3){
        if(!lock){
            lock = true;
            aliensGen.generateBoss();
        }else if(aliens.isEmpty()){
            setStage(4);
            setInterStage(2);
            lock = false;
            interstageEnd = true;
        } 
    }

    if(stage == 4){
        if(interstage == 2 && !lock){
            lock = true;
            this.level = 3;
            setBackground();
            aliensGen = new AlienGenerator(B_WIDTH, B_HEIGHT, aliens, this.level);
            aliensGen.start();
            interStage(TIME_LEVEL,2);
            
        }else if(interstage == 2 && !interstageEnd){
            aliensGen.Shutdown();
            setStage(5);
            lock = false;
            interstageEnd = true;
        } 
    }

    if(stage == 5){
        if(!lock){
            lock = true;
            aliensGen.generateBoss();
        }else if(aliens.isEmpty()){
            setStage(6);
            setInterStage(5);
            lock = false;
            interstageEnd = true;
        } 
    }

    if(stage == 6){
        if(!lock){
            lock = true;
            EndGameFunction(1);
        }
    }

}
//--------------------------LEVEL SWITCHER END------------------------------->


   
//-----------------------UPDATE VARIABLES------------------------------->
    protected void updateShip() {
        synchronized(spaceShips) {
            for(int k=0; k < spaceShips.size(); k++){
                SpaceShip ship = spaceShips.get(k);
                synchronized(ship){
                    List<Missile> ms = ship.getMissiles();
                    for (int i=0; i < ms.size(); i++) {
                        Missile m = ms.get(i);
                        if (m.isVisible())
                            m.move();
                        else
                            ms.remove(m);
                    }
                }
                
                if (ship.isVisible())
                    ship.move();
                else
                    synchronized(ship){
                        if(ship.getMissiles().isEmpty())
                            spaceShips.remove(ship);
                    }
            }
        }
    }

    protected void updatePacks() {
        synchronized(packs){
            for (int i=0; i < packs.size(); i++) {
                UpgradePack pack = packs.get(i);
                synchronized(pack){
                    if (pack.isVisible())
                        pack.move();
                    else
                        packs.remove(i);
                }
            }
        }
    }

    protected void updateAliens() {
        synchronized(aliens){
            for (int i=0; i < aliens.size(); i++) {
                Alien alien = aliens.get(i);

                synchronized(alien){               
                    List<Missile> ms = alien.getMissiles();
                    for (int j=0; j < ms.size(); j++) {
                        Missile m = ms.get(j);
                        if (m.isVisible())
                            m.move();
                        else
                            ms.remove(m);
                    }

                    if (alien.isVisible())
                        alien.move(); 
                    else{
                        synchronized(alien){
                            if(alien.getMissiles().isEmpty())
                                aliens.remove(alien);
                        }
                    }                                
                }
            }
        }
    }

//-----------------------END UPDATE VARIABLES------------------------------->


    public void checkCollisions() {
        Area alienHitbox;
        Area packHitbox;
        Area shipHitbox;
        Area missileHitbox;
        SpaceShip ship;

        synchronized(spaceShips) {
            for(int k=0; k < spaceShips.size(); k++) {
                ship = spaceShips.get(k);
                shipHitbox = ship.getShape();

                synchronized (packs) {
                    for(int i=0; i < packs.size(); i++){        // checking collisions between upbox and spaceship
                        packHitbox = packs.get(i).getShape();
                        packHitbox.intersect(shipHitbox);       // intersection is empty if shapes aren't collided
                        if (!packHitbox.isEmpty() && !packs.get(i).isDying()) {   // isDying() to avoid multiple check
                            synchronized(ship){
                                packs.get(i).updateSpaceShip(ship);
                            }

                            packs.get(i).setDying(true);

                            if(isMusicOn) {
                                MusicManager mumOne = new MusicManager(powerUpSound);
                                mumOne.startMusic();
                            }
                        }
                    }
                }

                synchronized (aliens) {                 // checking collisions between aliens and spaceship
                    for (Alien alien : aliens) {
                        alienHitbox = alien.getShape();
                        alienHitbox.intersect(shipHitbox);
                        if (!alienHitbox.isEmpty() && !alien.isDying() && !ship.isDying()) {   // intersection is empty if shapes aren't collided
                            alien.setImage(alienExpl.getImage());
                            ship.setImage(shipExpl.getImage());
                            alien.setDying(true);
                            ship.setDying(true);
                            finalScore += ship.getScore();
                            
                            if(isMusicOn){
                                
                                    MusicManager mumTwo = new MusicManager(shipExplSound);
                                    mumTwo.startMusic();

                                }
                        }

                        synchronized (alien) {          // checking collisions between alien missiles and spaceship
                            List<Missile> alienMissiles = alien.getMissiles();
                            for (Missile missile : alienMissiles) {
                                missileHitbox = missile.getShape();
                                missileHitbox.intersect(shipHitbox);
                                if (!missileHitbox.isEmpty() && missile.isVisible() && !ship.isDying()) {     // intersection is empty if shapes aren't collided
                                    ship.setupLife(-1);
                                    missile.setVisible(false);
                                    
                                    if(isMusicOn) {
                                        
                                            MusicManager mumThree = new MusicManager(shipExplSound);
                                            mumThree.startMusic();
                                    }
                                    if(ship.getLife() <= 0){
                                            ship.setImage(shipExpl.getImage());
                                            ship.setDying(true);
                                            finalScore += ship.getScore();
                                    }
                                }
                            }
                        }

                        alienHitbox = alien.getShape();
                        synchronized (ship) {           // checking collisions between spaceship missiles and aliens
                            List<Missile> shipMissiles = ship.getMissiles();
                            for (Missile missile : shipMissiles) {
                                missileHitbox = missile.getShape();
                                missileHitbox.intersect(alienHitbox);

                                if (!missileHitbox.isEmpty() && !alien.isDying()) {     // intersection is empty if shapes aren't collided
                                    alien.setupLife(missile.getDamage());
                                    
                                    if(alien.getLife() > 0){
                                        if(isMusicOn){
                                            
                                            MusicManager mumFour = new MusicManager(bossHitSound);
                                            mumFour.startMusic();
                                        
                                        }
                                    }

                                    missile.setVisible(false);

                                    if(alien.getLife() <= 0){
                                        synchronized(alien){
                                            ship.setupScore(alien.getPoints());
                                            alien.setImage(alienExpl.getImage());
                                            alien.setDying(true);
                                        }
                                        if(isMusicOn) {
                                            MusicManager mumFive = new MusicManager(alienExplSound);
                                            mumFive.startMusic();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void gameOverCondition(){   //GAME OVER se tutte le space ship sono morte
        Boolean alive = false;
        synchronized(spaceShips){    
            for(int i = 0; i < spaceShips.size(); i++){
                SpaceShip ship = spaceShips.get(i);   
                if(!ship.isDying())
                    alive = true;
            }
        }
        if(alive == false)
            EndGameFunction(0);
        

    }

    //Outcome is passed to the panel to draw the right image (game won or game lost)
    public void EndGameFunction(int outcome) {
        
        if(mumZero != null)
            mumZero.stopMusic();
        Boolean MusicState = isMusicOn;
        isMusicOn = false;


        JFrame old = (JFrame) SwingUtilities.getWindowAncestor(this);
        old.getContentPane().remove(this);

        /*synchronized(spaceShips){
            for(int k = 0; k < spaceShips.size(); k++) {
                SpaceShip ship = spaceShips.get(k);
                finalScore += ship.getScore();
            }
        }*/

        GameEndPanel gep = new GameEndPanel(outcome, menuPanel, finalScore, MusicState);
        old.add(gep).requestFocusInWindow();
        old.validate();
        old.repaint();
    }


    public void gameLaunch() {
        if(gameState == GameStateEnum.IN_GAME) {
            boardAnimator = new Thread(this);
            boardAnimator.start();
        }
    }


    class TAdapter extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            synchronized(spaceShips) {
                for(int k=0;k<spaceShips.size();k++){
                    SpaceShip Ship = spaceShips.get(k);
                    try {
                        Ship.keyReleased(e);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();                       
            
            if (key == KeyEvent.VK_P){        
                if (isPause == false){
                    isPause = true;
                    synchronized(spaceShips) {
                        for(int k=0; k < spaceShips.size(); k++) {
                            SpaceShip ship = spaceShips.get(k);
                            ship.setFiring(false);
                        }
                    }
                    packsGen.suspend();
                    aliensGen.suspend();
                    pauseGameFunction();
                }else{
                    resumeGame();
                }
            }

            synchronized(spaceShips) {
                for(int k=0; k < spaceShips.size(); k++) {
                    SpaceShip ship = spaceShips.get(k);
                    try {
                        ship.keyPressed(e);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    

    public void resumeGame(){         
        isPause = false;
        packsGen.resume();
        aliensGen.resume();
        boardAnimator.resume();
    }


    public void pauseGame(){           
        boardAnimator.suspend();
    }
  
    public void pauseGameFunction(){  //Alessio funzione per il passaggio a PausePanel
        JFrame old = (JFrame) SwingUtilities.getWindowAncestor(this);
        old.getContentPane().remove(this);
        PausePanel gep = new PausePanel(this,menuPanel,mumZero,isMusicOn);   //Alessio ti passo l'istanza di questa board cosi che puoi lavorarci nel PausePanel
        old.add(gep).requestFocusInWindow();           //TI ho commentato il costruttore e cosa si dovrebbere fare
        old.validate();
        old.repaint();
        pauseGame();  
    }

    @Override
    public void run() {
        long beforeTime, timeDiff, sleep;
        beforeTime = System.currentTimeMillis();
        while (gameState != GameStateEnum.GAME_LOST) {
            if(isPause == true)
                pauseGame();
            else{
                story(stage);
                updateShip();
                updateAliens();
                updatePacks();
                checkCollisions();
                repaint();
            
                timeDiff = System.currentTimeMillis() - beforeTime;
                sleep = DELAY - timeDiff;
                if (sleep < 0)
                    sleep = 2;

                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    System.out.println("Thread Board: " + e.getMessage());
                }
                beforeTime = System.currentTimeMillis();
            }
            repaint();
        }
    }


    private void interStage(int s, int n){ 
        setInterStage(n);
        task = new NextStage(this);
        t.schedule(task, s * 1000);
    }

    class NextStage extends TimerTask  {
        private Board b;

        public NextStage(Board board) {
            this.b = board;
        }
   
        @Override
        public void run() {
            b.setInterStageEnd(false);    
        }
    }

}
