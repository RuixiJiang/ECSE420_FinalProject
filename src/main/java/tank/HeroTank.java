package tank;

import constant.Constant;
import java.util.concurrent.*;

public class HeroTank extends Tank {
    public HeroTank(int x, int y) {
        super(x, y, Constant.UP);
    }

    @Override
    public void shot() {
        if (isAllowShot()) {
            // start a thread and start shooting
            getBulletThreadPool().execute(() -> {
                // create bullets according to the direction
                int d = getDirection();
                Bullet bullet = null;
                switch (d) {
                    case Constant.UP:
                        bullet = new Bullet(getX() + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2 - Constant.BULLET_RADIUS, getY(), d);
                        break;
                    case Constant.RIGHT:
                        bullet = new Bullet(getX() + Constant.TANK_WHEEL_HEIGHT, getY() + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2 - Constant.BULLET_RADIUS, d);
                        break;
                    case Constant.DOWN:
                        bullet = new Bullet(getX() + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2 - Constant.BULLET_RADIUS, getY() + Constant.TANK_WHEEL_HEIGHT, d);
                        break;
                    case Constant.LEFT:
                        bullet = new Bullet(getX(), getY() + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2 - Constant.BULLET_RADIUS, d);
                        break;
                    default:
                }
                // add the bullet to the list of bullets and start shooting
                if (bullet != null) {
                    getBullets().add(bullet);
                    bullet.shot();
                }
            });

            setAllowShot(false);

            // start a new thread and set the shooting state
            getShotIntervalThreadPool().execute(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(Constant.MY_TANK_SHOT_MIN_INTERVAL_TIME);
                    setAllowShot(true);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            });
        }
    }

    public void waitShot() {
        // if the tank is alive, keep waiting for the key pressed for shooting
        while (isLive()) {
            try {
                TimeUnit.MILLISECONDS.sleep(Constant.MY_TANK_SHOT_MIN_INTERVAL_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
