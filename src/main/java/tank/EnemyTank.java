package tank;

import constant.Constant;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class EnemyTank extends Tank {
    private List<HeroTank> myTanks = null;

    /**
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public EnemyTank(int x, int y) {
        super(x, y, Constant.DOWN);
    }

    @Override
    public void shot() {
        // keep shooting if is alive
        while (isLive()) {
            try {
                TimeUnit.MILLISECONDS.sleep(Constant.ENEMY_TANK_SHOT_MIN_INTERVAL_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // move according to the direction
            switch (getDirection()) {
                case Constant.UP:
                    moveUp();
                    break;
                case Constant.RIGHT:
                    moveRight();
                    break;
                case Constant.DOWN:
                    moveDown();
                    break;
                default:
                    moveLeft();
            }
            // is allowed to shot
            if (isAllowShot()) {
                // start thread and start shooting
                getBulletThreadPool().execute(() -> {
                    // create bullet according to the direction
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
                    if (bullet != null) {
                        // add the bullet to the list of bullets and start shooting
                        getBullets().add(bullet);
                        bullet.shot();
                    }
                });

                // set shooting state to NOT allowed shooing
                setAllowShot(false);

                // start a new thread and set shooting state. If the current thread is sleeping, the bullet will be lagging.
                getShotIntervalThreadPool().execute(() -> {
                    try {
                        // random sleeping
                        ThreadLocalRandom random = ThreadLocalRandom.current();
                        int t = 300 * random.nextInt(10);
                        TimeUnit.MILLISECONDS.sleep(t);
                        setAllowShot(true);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                });
                // change direction and attack hero tank
                if (myTanks.size() > 0 && myTanks.get(0) != null && myTanks.get(0).isLive()) {
                    Tank myTank = myTanks.get(0);
                    // start a thread and change direction
                    getShotIntervalThreadPool().execute(() -> {
                        try {
                            // random sleep
                            ThreadLocalRandom random = ThreadLocalRandom.current();
                            int t = 1000 + 1000 * random.nextInt(10);
                            TimeUnit.MILLISECONDS.sleep(t);
                            // get the direction of hero tank
                            int xDistance = myTank.getX() - getX() + Constant.TANK_WHEEL_HEIGHT;
                            int yDistance = myTank.getY() - getY() + Constant.TANK_WHEEL_HEIGHT;
                            // change direction
                            if (Math.abs(xDistance) < Math.abs(yDistance)) {
                                if (yDistance >= 0) {
                                    setDirection(Constant.DOWN);
                                } else {
                                    setDirection(Constant.UP);
                                }
                            } else {
                                if (xDistance >= 0) {
                                    setDirection(Constant.RIGHT);
                                } else {
                                    setDirection(Constant.LEFT);
                                }
                            }
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    public void setMyTanks(List<HeroTank> myTanks) {
        this.myTanks = myTanks;
    }
}
