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
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;


public class Board extends JPanel implements Runnable {

    private final int B_WIDTH = 600;
    private final int B_HEIGHT = 450;
    private final int DELAY = 15;
    private final double BG_SPEED = 0.5;    // background speed
    private final ImageIcon alienExpl;
    private final ImageIcon shipExpl;
    private final File alienExplSound;
    private final File shipExplSound;
    private final File powerUpSound;
    private final File bossHitSound;
    private File boardSound;

    private boolean MULTIPLAYER;
    private ArrayList<SpaceShip> spaceShips; 
    private List<Alien> aliens;
    private LinkedList<UpgradePack> packs;
    private int gameState;
    private Thread boardAnimator;
    private int level;

    private Thread threadAliensGen;      // alien generator thread
    private AlienGenerator aliensGen;    // alien generator class
    private Thread threadPacksGen;
    private PackGenerator packsGen;

    private ImageIcon bgImgIcon;
    private Image background;
    private double bgShiftX;

    private JPanel menuPanel;
    private boolean isMusicOn;
    private int keyModality;

    private InputStream in;
    private AudioStream audios;


    public Board(int shipType, JPanel p, boolean m, int level, int km, boolean mp) {
        this.MULTIPLAYER = mp;
        this.level = level;
        // Images and soundtracks initialization
        this.isMusicOn = m;          // TODO music may change in each level
        this.menuPanel = p;
        this.keyModality = km;       // game commands switcher

        if(isMusicOn) {
            boardSound = new File("./src/main/java/SoEproj/Resource/MusicGame.wav");
            try {
                in = new FileInputStream(boardSound);
                audios = new AudioStream(in);
                AudioPlayer.player.start(audios);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        alienExpl = new ImageIcon("./src/main/java/SoEproj/Resource/ExplosionAliens.png");
        shipExpl = new ImageIcon("./src/main/java/SoEproj/Resource/ExplosionShip.png");
        alienExplSound = new File("./src/main/java/SoEproj/Resource/CollisionSound.wav");
        shipExplSound = new File("./src/main/java/SoEproj/Resource/FinalCollisionSound.wav");
        powerUpSound = new File("./src/main/java/SoEproj/Resource/PowerUp.wav");
        bossHitSound = new File("./src/main/java/SoEproj/Resource/ShoothedBoss.wav");

        setBackground();
        initGame(shipType);     // shipType may change with level
        gameLaunch();
    }

    private void setBackground() {
        bgImgIcon = new ImageIcon("./src/main/java/SoEproj/Resource/BackGround1.png");
        if(level == 2)
            bgImgIcon = new ImageIcon("./src/main/java/SoEproj/Resource/BackGround2.png");
        if(level == 3)
            bgImgIcon = new ImageIcon("./src/main/java/SoEproj/Resource/BackGround3.png");

        background = bgImgIcon.getImage();
    }

    public void initGame(int shipType) {
        gameState = 1;
        addKeyListener(new TAdapter());
        setPreferredSize(new Dimension(B_WIDTH, B_HEIGHT));

        spaceShips = new ArrayList<>();
        if(MULTIPLAYER){
            spaceShips.add( new SpaceShip(0, B_HEIGHT/2, shipType, isMusicOn, keyModality) );
            spaceShips.add( new SpaceShip(0, B_HEIGHT/2 + 60, shipType + 1 % 3 , isMusicOn, keyModality + 1 % 2) );// +1 % 2 for set a different type
        }else{
            spaceShips.add( new SpaceShip(0, B_HEIGHT/2, shipType, isMusicOn, keyModality) );
        }

        packs = new LinkedList<>();
        packsGen = new PackGenerator(background.getWidth(null), packs);
        threadPacksGen = new Thread(packsGen);

        aliens = new ArrayList<>();
        aliensGen = new AlienGenerator(background.getWidth(null), aliens, this.level);
        threadAliensGen = new Thread(aliensGen);

        threadAliensGen.start();
        threadPacksGen.start();
    }

    private void drawBackground(Graphics g) {
        if (bgShiftX > background.getWidth(null)) {
            bgShiftX = 0;
        } else {
            bgShiftX += BG_SPEED;
        }
        g.drawImage(background, (int) -bgShiftX, 0, null);
        g.drawImage(background, background.getWidth(null) - (int) bgShiftX, 0, null);
    }

    private void drawGame(Graphics g) {
        for(int i=0;i<spaceShips.size();i++){
            SpaceShip Ship = spaceShips.get(i);
            if (Ship.isVisible()) {
                g.drawImage(Ship.getImage(), Ship.getX(), Ship.getY(), this);

                if (Ship.isDying()) {
                    Ship.die();
                }
            }

            synchronized(Ship){
                List<Missile> ms = Ship.getMissiles();
                for (Missile missile : ms) {
                    if (missile.isVisible()) {
                        g.drawImage(missile.getImage(), missile.getX(), missile.getY(), this);
                    }
                }
            }
        }

        //GAME OVER condition set
        Boolean alive = false;
        for(int i=0;i<spaceShips.size();i++){
            SpaceShip Ship = spaceShips.get(i);
            if(!Ship.isDying())
                alive = true;
        }
        if(alive == false){
            gameState = 2;
        }

        synchronized(packs){
            for(UpgradePack pack : packs){
                if (pack.isVisible()) {
                    g.drawImage(pack.getImage(), pack.getX(), pack.getY(), this);
                }

                if (pack.isDying())
                    pack.die();
            }
        }

        synchronized(aliens) {   // they are drawn only if they have not been previously destroyed.
            for (Alien alien : aliens) {
                if (alien.isVisible()) {
                    g.drawImage(alien.getImage(), alien.getX(), alien.getY(), this);
                }

                synchronized(alien){
                    List<Missile> as = alien.getMissiles();
                    for (Missile missile : as) {
                        if (missile.isVisible()) {
                            g.drawImage(missile.getImage(), missile.getX(), missile.getY(), this);
                        }
                    }
                }
                if (alien.isDying()) {
                    alien.die();
                    if (alien instanceof Boss1Level){ // i'm starting a new level
                        if(this.level < 3){
                            this.level += 1;
                            setBackground();

                            aliensGen = new AlienGenerator(background.getWidth(null), aliens, this.level);
                            threadAliensGen = new Thread(aliensGen);
                            threadAliensGen.start();
                        }
                        else {
                            gameState = 2;
                        }
                    }
                }
            }
        }

        g.setColor(Color.WHITE); // In the top-left corner of the window, we draw how many aliens are left.

        int yPos = 15;
        int xPos = 5;
        int offset = 0;

        for(int i = 0; i < spaceShips.size(); i++) {
            SpaceShip ship = spaceShips.get(i);
            if(i != offset)
                yPos += 16;
            
            synchronized(ship){
                g.drawString("Player " + (i+1) + " --- Lives: " + ship.getLife() + "    Score: " + ship.getScore(), xPos, yPos);
                //g.drawString("Speed: " + ship.getSPACE(), xPos, yPos+20);
            }
            offset = i;
        }
    }

    public void gameLaunch() {
        if(gameState == 1) {
            boardAnimator = new Thread(this);
            boardAnimator.start();
        }

    }

    private void updateShip() {
        for(int k=0;k<spaceShips.size();k++){
            SpaceShip Ship = spaceShips.get(k);
            if (Ship.isVisible()) {
                synchronized(Ship){
                    List<Missile> ms = Ship.getMissiles();
                    for (int i=0; i < ms.size(); i++) {
                        Missile m = ms.get(i);
                        if (m.isVisible()) {
                            m.move();
                        } else {
                            ms.remove(m);
                        }
                    }
                }
            Ship.move();
            }
        }
    }

    private void updatePacks() {
        synchronized(packs){
            for (int i=0; i < packs.size(); i++) {
                UpgradePack pack = packs.get(i);
                synchronized(pack){
                    if (pack.isVisible()) {
                        pack.move();
                    }
                    else {
                        packs.remove(i);
                    }
                }
            }
        }
    }

    private void updateAliens() {
        synchronized(aliens){
            for (int i=0; i < aliens.size(); i++) {
                Alien alien = aliens.get(i);

                synchronized(alien){
                    if (alien.isVisible()) {
                        List<Missile> ms = alien.getMissiles();
                        for (int j=0; j < ms.size(); j++) {
                            Missile m = ms.get(j);
                            if (m.isVisible()) {
                                m.move();
                            } else {
                                ms.remove(m);
                            }
                        }
                        alien.move();
                    }
                    else {
                        aliens.remove(alien);
                    }
                }
            }
        }
    }


    public void checkCollisions() {
        Area alienHitbox;
        Area packsHitbox;
        Area shipHitbox;

        for(int k = 0; k < spaceShips.size(); k++){
            SpaceShip ship = spaceShips.get(k);
            shipHitbox = ship.getShape();
            synchronized (packs) {
                int i=0;
                for(i = 0; i < packs.size(); i++){
                    // checking collisions between upgrade packs and spaceships
                    packsHitbox = packs.get(i).getShape();
                    // intersection is empty if shapes aren't collided
                    packsHitbox.intersect(shipHitbox);
                    if (!packsHitbox.isEmpty()) {
                        synchronized(ship){
                            packs.get(i).updateSpaceShip(ship);
                        }

                        packs.get(i).setDying(true);
                        packs.remove(i);

                        if(isMusicOn) {
                            try {
                                InputStream in = new FileInputStream(powerUpSound);
                                AudioStream audios = new AudioStream(in);
                                AudioPlayer.player.start(audios);
                            } catch (IOException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                }
            }


            synchronized (aliens) {
                // checking collisions between aliens and spaceship
                for (Alien alien : aliens) {
                    alienHitbox = alien.getShape();
                    // intersection is empty if shapes aren't collided
                    alienHitbox.intersect(shipHitbox);
                    if (!alienHitbox.isEmpty()) {
                        alien.setDying(true);
                        alien.setImage(alienExpl.getImage());

                        synchronized(ship) {
                            ship.setupLife(-1);
                        }

                        if(ship.getLife() <= 0) {
                            synchronized(ship){
                                ship.setDying(true);
                                ship.setImage(shipExpl.getImage());
                                ship.getMissiles().clear(); //TODO faccio sparire i missili del P2 perche non avanzano
                            }
                            if(isMusicOn) {
                                try {
                                    InputStream in = new FileInputStream(shipExplSound);
                                    AudioStream audios = new AudioStream(in);
                                    AudioPlayer.player.start(audios);
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }

                    }

                    synchronized (alien) {
                        // checking collisions between alien missiles and spaceship
                        List<Missile> alienMissiles = alien.getMissiles();
                        for (Missile missile : alienMissiles) {
                            Area missileHitbox = missile.getShape();
                            // intersection is empty if shapes aren't collided
                            missileHitbox.intersect(shipHitbox);

                            if (!missileHitbox.isEmpty()) {
                                synchronized(ship) {
                                    ship.setupLife(-1);
                                }

                                missile.setVisible(false); // TODO lasciare i missili degli alieni proseguire nel caso si uccide un alieno

                                if(ship.getLife() <= 0) {
                                    synchronized(ship) {
                                        ship.setDying(true);
                                        ship.setImage(shipExpl.getImage());
                                    }
                                    if(isMusicOn) {
                                        try {
                                            InputStream in = new FileInputStream(shipExplSound);
                                            AudioStream audios = new AudioStream(in);
                                            AudioPlayer.player.start(audios);
                                        } catch (IOException e) {
                                            System.out.println(e.getMessage());
                                        }
                                    }
                                }
                            }

                        }
                    }

                    alienHitbox = alien.getShape();

                    synchronized (ship) {
                        // checking collisions between spaceship missiles and aliens
                        List<Missile> shipMissiles = ship.getMissiles();
                        for (Missile missile : shipMissiles) {
                            Area missileHitbox = missile.getShape();
                            // intersection is empty if shapes aren't collided
                            missileHitbox.intersect(alienHitbox);

                            if (!missileHitbox.isEmpty()) {
                                synchronized(alien){
                                    alien.setupLife(missile.getDamage());
                                }
                                if(alien.getLife() > 0) {
                                    if(isMusicOn) {
                                        try {
                                            InputStream in = new FileInputStream(bossHitSound);
                                            AudioStream audios = new AudioStream(in);
                                            AudioPlayer.player.start(audios);
                                        } catch (IOException e) {
                                            System.out.println(e.getMessage());
                                        }
                                    }
                                }
                                missile.setVisible(false);

                                if(alien.getLife() <= 0){
                                    synchronized(alien){
                                        alien.setDying(true);
                                        // TODO vengono aggiornati qui i punti?
                                        ship.setupScore(alien.getPoints());
                                        alien.setImage(alienExpl.getImage());
                                    }
                                    if(isMusicOn) {
                                        try {
                                            InputStream in = new FileInputStream(alienExplSound);
                                            AudioStream audios = new AudioStream(in);
                                            AudioPlayer.player.start(audios);
                                        } catch (IOException e) {
                                            System.out.println(e.getMessage());
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

    //Outcome is passed to the panel to draw the right image (game won or game lost)
    public void EndGameFunction(int outcome) {
        AudioPlayer.player.stop(audios);
        JFrame old = (JFrame) SwingUtilities.getWindowAncestor(this);
        old.getContentPane().remove(this);

        int finalScore = 0;
        for(int k = 0; k < spaceShips.size(); k++) {
            SpaceShip ship = spaceShips.get(k);
            finalScore += ship.getScore();
        }

        GameEndPanel gep = new GameEndPanel(outcome, menuPanel, finalScore, isMusicOn);
        old.add(gep).requestFocusInWindow();
        old.validate();
        old.repaint();
    }


    private class TAdapter extends KeyAdapter {
        @Override
        public void keyReleased(KeyEvent e) {
            try {
                for(int k = 0; k < spaceShips.size(); k++) {
                    SpaceShip ship = spaceShips.get(k);
                    ship.keyReleased(e);
                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            try{
                for(int k = 0; k < spaceShips.size(); k++) {
                    SpaceShip ship = spaceShips.get(k);
                    ship.keyPressed(e);
                }
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }
    
    // This method will be executed by the painting subsystem whenever you component needs to be rendered
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // TODO Rendere gameState un enum
        if(gameState == 1) {        // draw background and game elements
            drawBackground(g);
            drawGame(g);
        }
        else if(gameState == 2) {   // draw game over background gif after the lose condition
            EndGameFunction(0);     // passing 0 to draw game over background
        }
        // TODO fare la condizione di win
        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void run() {
        long beforeTime, timeDiff, sleep;
        beforeTime = System.currentTimeMillis();
        while (gameState != 2) {
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
}
