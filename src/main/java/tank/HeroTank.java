package tank;

import constant.Constant;

import java.util.concurrent.*;

/**
 * @author ajun
 * Date 2021/7/10
 * @version 1.0
 * 我的坦克
 */
public class HeroTank extends Tank {
    /**
     * 构造器
     *
     * @param x
     * @param y
     */
    public HeroTank(int x, int y) {
        super(x, y, Constant.UP);
    }

    /**
     * 射击
     */
    @Override
    public void shot() {
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
                // 加入坦克的子弹集合，并射击
                if (bullet != null) {
                    getBullets().add(bullet);
                    bullet.shot();
                }
            });

            // 发射后把发射状态设为不可发射
            setAllowShot(false);

            // 开启新线程，设置发射状态。如果在当前线程休眠等待时，会出现阻塞，子弹有停顿现象
            getShotIntervalThreadPool().execute(() -> {
                try {
                    // 休眠最小间隔时间
                    TimeUnit.MILLISECONDS.sleep(Constant.MY_TANK_SHOT_MIN_INTERVAL_TIME);
                    // 设为可以发射
                    setAllowShot(true);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            });
        }
    }

    /**
     * 等待射击
     */
    public void waitShot() {
        // 如果坦克是存活状态，就一直循环，等待点击发射键
        while (isLive()) {
            try {
                TimeUnit.MILLISECONDS.sleep(Constant.MY_TANK_SHOT_MIN_INTERVAL_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
