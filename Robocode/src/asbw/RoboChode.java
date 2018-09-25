package asbw;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import robocode.*;
import robocode.util.Utils;

/**
 * SuperSpinBot - a Super Sample Robot by CrazyBassoonist based on the robot RamFire by Mathew Nelson and maintained by Flemming N. Larsen.
 *
 * This robot tries to ram it's opponents.
 *
 */
public class RoboChode extends AdvancedRobot {
    int moveDirection=1;//which way to move
    public double wallStick = 75;
    public int sameDirectionCounter = 0;

    /**
     * run:  Tracker's main run function
     */
    public void run() {
        setAdjustRadarForRobotTurn(true);//keep the radar still while we turn
        setBodyColor(Color.BLACK);
        setGunColor(Color.RED);
        setRadarColor(Color.BLACK);
        setScanColor(Color.red);
        setBulletColor(Color.red);
        setAdjustGunForRobotTurn(true); // Keep the gun still when we turn
        turnRadarRightRadians(Double.POSITIVE_INFINITY);//keep turning radar right
    }

    /**
     * onScannedRobot:  Here's the good stuff
     */
    public void onScannedRobot(ScannedRobotEvent e) {

        double absBearing = e.getBearingRadians() + getHeadingRadians();
        /* Move perpendicular to our enemy, based on our movement direction */
        double goalDirection = absBearing-Math.PI/2.0*moveDirection;

		/* This is too clean for crazy! Add some randomness. */
        goalDirection += (Math.random()-0.5) * (Math.random()*2.0 + 1.0);

		/* Smooth around the walls, if we smooth too much, reverse direction! */
        double x = getX();
        double y = getY();
        double smooth = 0;

		/* Calculate the smoothing we would end up doing if we actually smoothed walls. */
        Rectangle2D fieldRect = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);

        while (!fieldRect.contains(x+Math.sin(goalDirection)*wallStick, y+ Math.cos(goalDirection)*wallStick)) {
			/* turn a little toward enemy and try again */
            goalDirection += moveDirection*0.1;
            smooth += 0.1;
        }

		/* If we would have smoothed to much, then reverse direction. */
		/* Add && sameDirectionCounter != 0 check to make this smarter */
        double latVel=e.getVelocity() * Math.sin(e.getHeadingRadians() -absBearing);//enemies later velocity
        double gunTurnAmt;//amount to turn our gun
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar
        if(Math.random()>.75){
            setMaxVelocity((15*Math.random())+15);//randomly change speed
        }
        if (e.getDistance() > 275) {//if distance is greater than 275
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/26);//amount to turn our gun, lead just a little bit
            setTurnGunRightRadians(gunTurnAmt); //turn our gun
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()+latVel/getVelocity()));//drive towards the enemies predicted future location
            setAhead((e.getDistance() + 150)*moveDirection);//move forward
            setFire(1.92);//fire
        }
        if (e.getDistance() > 150) {//if distance is greater than 150
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/22);//amount to turn our gun, lead just a little bit
            setTurnGunRightRadians(gunTurnAmt); //turn our gun
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()+latVel/getVelocity()));//drive towards the enemies predicted future location
            setAhead((e.getDistance() + 175)*moveDirection);//move forward
            setFire(2.25);//fire
        }
        else{//if we are close enough...
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/15);//amount to turn our gun, lead just a little bit
            setTurnGunRightRadians(gunTurnAmt);//turn our gun
            setTurnLeft(-90-e.getBearing()); //turn perpendicular to the enemy
            setAhead((e.getDistance())/moveDirection);//move forward
            setFire(3);//fire
        }
    }
    public void onHitWall(HitWallEvent e){
        moveDirection=-moveDirection;//reverse direction upon hitting a wall
    }
    /**
     * onWin:  Do a victory dance
     */
    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }
    public void onHitByBullet(HitByBulletEvent e){
        // if hit buy a bullet
        // target the one who hit us!
        moveDirection = -moveDirection;
    }
}