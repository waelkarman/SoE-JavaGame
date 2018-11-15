/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SoEproj;

import java.awt.Rectangle;
import java.awt.geom.Area;

public class Missile extends Sprite {

    private final int BOARD_WIDTH = 585;
    private final int type;       // indicates the spaceship type 1 hard, 2 medium, 3 easy

    public Missile(int x, int y, int type) {
        super(x, y);
        initMissile();
        this.type = type;
        SPACE = type * 2;
    }
    
    private void initMissile() {
        loadImage(".\\src\\main\\java\\SoEproj\\Resource\\Laser.png");
        getImageDimensions();        
    }

    // Missiles move in one direction only. They disappear after 
    // they reach the right window border
    public void move() {   
        x += SPACE;

        if (x > BOARD_WIDTH)
            visible = false;
    }

    @Override
    public Area getShape() {
        Rectangle shape = new Rectangle(x, y, width, height);
        
        return new Area(shape);
    }

}
