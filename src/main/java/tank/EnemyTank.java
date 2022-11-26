package tank;

import constant.Constant;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author ajun
 * Date 2021/7/11
 * @version 1.0
 * 敌方坦克
 */
public class EnemyTank extends Tank {
    // 我方坦克集合：可以让敌方坦克自动追击
    private List<HeroTank> myTanks = null;

    /**
     * 构造器
     *
     * @param x
     * @param y
     */
    public EnemyTank(int x, int y) {
        super(x, y, Constant.DOWN);
    }

    /**
     * 射击
     */
    @Override
    public void shot() {
        // 如果存活就一直发射
        while (isLive()) {
            try {
                TimeUnit.MILLISECONDS.sleep(Constant.ENEMY_TANK_SHOT_MIN_INTERVAL_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 根据方向移动
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
            // 可以发射
            if (isAllowShot()) {
                // 开启线程，发射子弹
                getBulletThreadPool().execute(() -> {
                    // 根据方向创建子弹，并射击
                    int d = getDirection();
                    Bullet bullet = null;
                    switch (d) {
                        case Constant.UP:
                            // 创建子弹
                            bullet = new Bullet(getX() + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2 - Constant.BULLET_RADIUS, getY(), d);
                            break;
                        case Constant.RIGHT:
                            // 创建子弹
                            bullet = new Bullet(getX() + Constant.TANK_WHEEL_HEIGHT, getY() + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2 - Constant.BULLET_RADIUS, d);
                            break;
                        case Constant.DOWN:
                            // 创建子弹
                            bullet = new Bullet(getX() + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2 - Constant.BULLET_RADIUS, getY() + Constant.TANK_WHEEL_HEIGHT, d);
                            break;
                        case Constant.LEFT:
                            // 创建子弹
                            bullet = new Bullet(getX(), getY() + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2 - Constant.BULLET_RADIUS, d);
                            break;
                        default:
                    }
                    if (bullet != null) {
                        // 加入坦克的子弹集合，并射击
                        getBullets().add(bullet);
                        bullet.shot();
                    }
                });

                // 发射后把发射状态设为不可发射
                setAllowShot(false);

                // 开启新线程，设置发射状态。如果在当前线程休眠等待时，会出现阻塞，子弹有停顿现象
                getShotIntervalThreadPool().execute(() -> {
                    try {
                        // 随机休眠
                        ThreadLocalRandom random = ThreadLocalRandom.current();
                        int t = 300 * random.nextInt(10);
                        TimeUnit.MILLISECONDS.sleep(t);
                        // 设为可以发射
                        setAllowShot(true);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                });
                // 改变方向，追击目标坦克
                if (myTanks.size() > 0 && myTanks.get(0) != null && myTanks.get(0).isLive()) {
                    Tank myTank = myTanks.get(0);
                    // 开启新线程，改变方向
                    getShotIntervalThreadPool().execute(() -> {
                        try {
                            // 随机休眠
                            ThreadLocalRandom random = ThreadLocalRandom.current();
                            int t = 1000 + 1000 * random.nextInt(10);
                            TimeUnit.MILLISECONDS.sleep(t);
                            // 判断我方坦克位置，从而改变方向
                            // x 轴距离
                            int xDistance = myTank.getX() - getX() + Constant.TANK_WHEEL_HEIGHT;
                            // y 轴距离
                            int yDistance = myTank.getY() - getY() + Constant.TANK_WHEEL_HEIGHT;
                            // 改变方向
                            if (Math.abs(xDistance) < Math.abs(yDistance)) {
                                // 纵向改变方向
                                if (yDistance >= 0) {
                                    // 向下
                                    setDirection(Constant.DOWN);
                                } else {
                                    // 向上
                                    setDirection(Constant.UP);
                                }
                            } else {
                                // 横向改变方向
                                if (xDistance >= 0) {
                                    // 向右
                                    setDirection(Constant.RIGHT);
                                } else {
                                    // 向左
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
