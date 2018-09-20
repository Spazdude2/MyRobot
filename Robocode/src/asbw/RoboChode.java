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
    double previousEnergy = 100;
    int movementDirection = 1;
    int gunDirection = 1;

    //These are constants. One advantage of these is that the logic in them (such as 20-3*BULLET_POWER)
    //does not use codespace, making them cheaper than putting the logic in the actual code.

    final static double firePower= 1.92;//Our bulletpower.
    final static double BULLET_DAMAGE=firePower*4;//Formula for bullet damage.
    final static double bulletSpeed=20*firePower;//Formula for bullet speed.

    //Variables
    static double dir=1.25;
    static double oldEnemyHeading;
    static double enemyEnergy;

    public double wallStick = 120;


    public void run(){

        //Spaz Colors
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);
        setBodyColor(Color.black);
        setGunColor(Color.black);
        setRadarColor(Color.red);
        setScanColor(Color.red);
        setBulletColor(Color.red);
        turnRadarRightRadians(Double.POSITIVE_INFINITY);
    }
    public void onScannedRobot(ScannedRobotEvent e){

        double bulletSpeed = 25 - firePower * 4;
        // distance = rate * time, solved for time
        long time = (long)(e.getDistance() / bulletSpeed);




        setTurnRight(e.getBearing()+75*movementDirection);
        double absBearing=e.getBearingRadians()+getHeadingRadians();
        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();//robot's absolute bearing
        double randomGuessFactor = (Math.random() - .8) * 4;
        double maxEscapeAngle = Math.asin(6.0/(10 - (4 *Math.min(3,getEnergy()/10))));//farthest the enemy can move in the amount of time it would take for a bullet to reach them
        double randomAngle = randomGuessFactor * maxEscapeAngle;//random firing angle
        double firingAngle = Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians()+randomAngle/5);//amount to turn our gun

        absBearing=e.getBearingRadians()+getHeadingRadians();//enemies absolute bearing
        double latVel=e.getVelocity() * Math.sin(e.getHeadingRadians() -absBearing);//enemies later velocity
        double gunTurnAmt;//amount to turn our gun
        setTurnRadarLeftRadians(getRadarTurnRemainingRadians());//lock on the radar
        if(Math.random()>.6){
            setMaxVelocity((12*Math.random())+12);//randomly change speed
        }
        if (e.getDistance() > 250) {
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/15);
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()+latVel/getVelocity()));//Turn perpendicular to them
            setTurnGunRightRadians(gunTurnAmt);//Aim!
            setAhead((e.getDistance() - 140)*movementDirection);
            setFire(firePower/1.5);//Fire, using less energy if we have low energy
            setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));//lock on the radar
            if (Math.random() > .750) {
                dir = -dir;
            }
        }

        else if (e.getDistance() > 150) {//if distance is greater than 150
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/18);//amount to turn our gun, lead just a little bit
            setTurnGunRightRadians(gunTurnAmt); //turn our gun
            setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(absBearing-getHeadingRadians()+latVel/getVelocity()));//drive towards the enemies predicted future location
            setAhead((e.getDistance() - 140)*movementDirection);//move forward
            setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
            setFire(firePower);//fire
            if (Math.random() > .6) {
                dir = -dir;
            }
        }
        else {//if we are close enough...
            gunTurnAmt = robocode.util.Utils.normalRelativeAngle(absBearing- getGunHeadingRadians()+latVel/12);//amount to turn our gun, lead just a little bit
            setTurnGunRightRadians(gunTurnAmt);//turn our gun
            setTurnLeft(-90-e.getBearing()); //turn perpendicular to the enemy
            setAhead((e.getDistance() - 140)*movementDirection);//move forward
            setTurnRadarRightRadians(Utils.normalRelativeAngle(absoluteBearing - getRadarHeadingRadians()));
            setFire(firePower*1.5);//fire
            if (Math.random() > .5) {
                dir = -dir;
            }
        }

        //This makes the amount we want to turn be perpendicular to the enemy.
        double turn=absBearing+Math.PI/2;

        //This formula is used because the 1/e.getDistance() means that as we get closer to the enemy, we will turn to them more sharply.
        //We want to do this because it reduces our chances of being defeated before we reach the enemy robot.
        turn-=Math.max(0.5,(1/e.getDistance())*100)*dir;

        setTurnRightRadians(Utils.normalRelativeAngle(turn-getHeadingRadians()));

        //This block of code detects when an opponents energy drops.
        if(enemyEnergy>(enemyEnergy=e.getEnergy())){

            //We use 300/e.getDistance() to decide if we want to change directions.
            //This means that we will be less likely to reverse right as we are about to ram the enemy robot.
            if(Math.random()>150/e.getDistance()){
                dir=-dir;
            }
        }

        //This line makes us slow down when we need to turn sharply.
        setMaxVelocity(200/getTurnRemaining());

        setAhead(270*dir);

        //Aim and fire.
        setFire(firePower);

        setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing-getRadarHeadingRadians())*2);

        double goalDirection = absBearing-Math.PI/2.0*movementDirection;

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
            goalDirection += movementDirection*0.1;
            smooth += 0.1;
        }
    }
    public void onBulletHit(BulletHitEvent e){
        enemyEnergy-=(BULLET_DAMAGE * 10);
    }
    public void onHitWall(HitWallEvent e){
        dir=-dir;
    }
}