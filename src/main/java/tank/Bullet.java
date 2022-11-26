package tank;

import constant.Constant;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author ajun
 * Date 2021/7/11
 * @version 1.0
 * 子弹
 */
public class Bullet{
    // 子弹 X 坐标
    private int x;
    // 子弹 Y 坐标
    private int y;
    // 子弹方向
    private int direction;
    // 子弹存活状态
    private boolean live = true;

    /**
     * 构造器
     *
     * @param x
     * @param y
     * @param direction
     */
    public Bullet(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    /**
     * 射击
     */
    public void shot() {
        // 子弹循环移动
        while (live) {
            // 休眠
            try {
                TimeUnit.MILLISECONDS.sleep(Constant.BULLET_RUN_INTERVAL_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 根据方向移动
            switch (direction) {
                case Constant.UP:
                    y -= Constant.BULLET_SPEED;
                    break;
                case Constant.RIGHT:
                    x += Constant.BULLET_SPEED;
                    break;
                case Constant.DOWN:
                    y += Constant.BULLET_SPEED;
                    break;
                case Constant.LEFT:
                    x -= Constant.BULLET_SPEED;
                    break;
                default:
            }

            // 判断子弹是否到边界
            if (x <= 0 || x >= Constant.WINDOW_WIDTH || y <= Constant.INFO_BAR_HEIGHT || y >= Constant.WINDOW_HEIGHT) {
                live = false;
            }
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bullet bullet = (Bullet) o;
        return x == bullet.x && y == bullet.y && direction == bullet.direction && live == bullet.live;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, direction, live);
    }
}
