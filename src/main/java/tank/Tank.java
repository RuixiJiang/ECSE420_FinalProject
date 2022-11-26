package tank;

import constant.Constant;
import util.MyThreadFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author ajun
 * Date 2021/7/10
 * @version 1.0
 * 坦克
 */
public abstract class Tank {
    // 坦克的横坐标
    private int x;
    // 坦克的纵坐标
    private int y;
    // 坦克方向
    private int direction;
    // 是否可以发射；结合发射间隔时间，控制发射频率
    private boolean allowShot;
    // 坦克是否存活
    private boolean live;
    // 子弹集合
    private List<Bullet> bullets;
    // 子弹线程池；当坦克被摧毁时，线程池销毁
    private ThreadPoolExecutor bulletThreadPool;
    // 计算子弹发射间隔时间线程池
    private ThreadPoolExecutor shotIntervalThreadPool;
    // 所有坦克：用于判断是否发生碰撞
    private List<Tank> allTanks;

    /**
     * 初始化
     */
    private void init() {
        // 是否可以发射；结合发射间隔时间，控制发射频率
        allowShot = true;
        // 坦克是否存活
        live = true;
        // 子弹集合
        bullets = new CopyOnWriteArrayList<>();
        // 子弹线程池；当坦克被摧毁时，线程池销毁
        bulletThreadPool = new ThreadPoolExecutor(
                15, // 核心线程数
                40, // 最大线程数
                200, // 空闲时间
                TimeUnit.MILLISECONDS, // 时间单位
                new ArrayBlockingQueue<>(5), // 阻塞队列
                new MyThreadFactory("子弹"), // 线程工厂
                new ThreadPoolExecutor.DiscardOldestPolicy()); // 拒绝策略
        // 计算子弹发射间隔时间线程池
        shotIntervalThreadPool = new ThreadPoolExecutor(
                3, // 核心线程数
                5, // 最大线程数
                100, // 空闲时间
                TimeUnit.MILLISECONDS, // 时间单位
                new ArrayBlockingQueue<>(2), // 阻塞队列
                new MyThreadFactory("子弹发射频率"), // 线程工厂
                new ThreadPoolExecutor.DiscardOldestPolicy()); // 拒绝策略

        // 所有坦克
        allTanks = new CopyOnWriteArrayList<>();
    }

    /**
     * 构造器
     *
     * @param x
     * @param y
     * @param direction
     */
    public Tank(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        init();
    }

    /**
     * 判断 当前坦克this 是否与其它坦克发生碰撞
     *
     * @return
     */
    private boolean isTouch() {
        // 坦克数量大于1时再判断。至少两个坦克才可能发生碰撞
        if (allTanks.size() > 1) {
            // 当前坦克的前方一点
            int x1, y1;
            Point p1;
            // 当前坦克的前方另一点
            int x2, y2;
            Point p2;

            // 根据当前坦克的方向分类判断
            switch (getDirection()) {
                /*
                 向上
                 判断当前坦克的 上面左右任一端 是否进入另一辆坦克的区域
                 */
                case Constant.UP:
                    // 当前坦克上方左侧
                    x1 = this.getX();
                    y1 = this.getY();
                    p1 = new Point(x1, y1);
                    // 当前坦克上方右侧
                    x2 = x1 + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
                    y2 = y1;
                    p2 = new Point(x2, y2);

                    // 判断当前坦克与其它坦克是否相撞
                    if (isTouchCurrentTankAndOtherTank(this, p1, p2)) {
                        return true;
                    }

                    break;

                /*
                 向右
                 判断当前坦克的 右面上下任一端 是否进入另一辆坦克的区域
                 */
                case Constant.RIGHT:
                    // 当前坦克右侧上方
                    x1 = this.getX() + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
                    y1 = this.getY();
                    p1 = new Point(x1, y1);
                    // 当前坦克右侧下方
                    x2 = x1;
                    y2 = y1 + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
                    p2 = new Point(x2, y2);

                    // 判断当前坦克与其它坦克是否相撞
                    if (isTouchCurrentTankAndOtherTank(this, p1, p2)) {
                        return true;
                    }

                    break;

                /*
                 向下
                 判断当前坦克的 下面左右任一端 是否进入另一辆坦克的区域
                 */
                case Constant.DOWN:
                    // 当前坦克下方左侧
                    x1 = this.getX();
                    y1 = this.getY() + Constant.TANK_WHEEL_HEIGHT;
                    p1 = new Point(x1, y1);
                    // 当前坦克下方右侧
                    x2 = x1 + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
                    y2 = y1;
                    p2 = new Point(x2, y2);

                    // 判断当前坦克与其它坦克是否相撞
                    if (isTouchCurrentTankAndOtherTank(this, p1, p2)) {
                        return true;
                    }

                    break;

                /*
                 向左
                 判断当前坦克的 左面上下任一端 是否进入另一辆坦克的区域
                 */
                default:
                    // 当前坦克左侧上方
                    x1 = this.getX();
                    y1 = this.getY();
                    p1 = new Point(x1, y1);
                    // 当前坦克左侧下方
                    x2 = x1;
                    y2 = y1 + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
                    p2 = new Point(x2, y2);

                    // 判断当前坦克与其它坦克是否相撞
                    if (isTouchCurrentTankAndOtherTank(this, p1, p2)) {
                        return true;
                    }
            }
        }
        return false;
    }

    /**
     * 判断点 p 是否在面 b 内部
     *
     * @param p
     * @param b
     * @return
     */
    private boolean isPointInBounds(Point p, Bounds b) {
        /*
        原理：
        用点 p ,分别与面 b 的四个值比较
        同时满足以下三个条件即表示点在面内，返回 true
        1） p 在面左上角的右下方
        2） p 在面右上角的左下方
        3） p 在面右下角的左上方
        */

        /*
         p 与 b 的左上角比较
         如果不返回 false,说明 p 在 矩形左上角 的右下方
         ●--------------
         |      |
         |      |
         |      |
         |      |
         --------
         |
         |

         */
        if (p.x < b.p1.x || p.y < b.p1.y) {
            return false;
        }

        /*
         p 与 b 的右上角 x 比较
         如果不返回 false ,说明 p 不在矩形的右方
         -------●
         |      |
         |      |
         |      |
         |      |
         --------
         |      |
         |      |

         */
        if (p.x > b.p2x) {
            return false;
        }

        /*
         p 与 b 的右下角 y 比较
         如果不返回 false ,说明 p 不在矩形的下方,可以确定 p 在 b 中（包括边界）
         --------
         |      |
         |      |
         |      |
         |      |
         -------●
         */
        if (p.y > b.p3y) {
            return false;
        }

        return true;
    }
    /**
     * 判断当前坦克与其它坦克是否相撞
     *
     * @param currentTank
     * @param p1          当前坦克前方一点
     * @param p2          当前坦克前方另一点
     * @return
     */
    private boolean isTouchCurrentTankAndOtherTank(Tank currentTank, Point p1, Point p2) {
        // 目标坦克区域面
        Bounds b;
        // 遍历坦克集合。比较与其它坦克是否发生碰撞
        for (Tank tank : allTanks) {
            // 避免与自已比较
            if (!currentTank.equals(tank)) {
                // 得到 tank 的区域面
                b = getBoundsByTank(tank);
                // 判断当前坦克的 上左点p1 或 上右点p2 是否与 目标坦克区域面b 重叠
                if (isPointInBounds(p1, b) || isPointInBounds(p2, b)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 获取坦克的区域面
     *
     * @param tank
     * @return
     */
    private Bounds getBoundsByTank(Tank tank) {
        // 目标坦克的上左
        int op1_x = tank.getX();
        int op1_y = tank.getY();
        Point op1 = new Point(op1_x, op1_y);

        // 目标坦克的上右x
        int op2_x;
        // 目标坦克的下右y
        int op3_y;

        // 目标坦克方向
        int d = tank.getDirection();
        if (d == Constant.UP || d == Constant.DOWN) { // 上下移动：计算方法一致
            // 目标坦克的上右x
            op2_x = op1_x + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
            // 目标坦克的下右y
            op3_y = op1_y + Constant.TANK_WHEEL_HEIGHT;
        } else { // 左右移动：计算方法一致
            // 目标坦克的上右x
            op2_x = op1_x + Constant.TANK_WHEEL_HEIGHT;
            // 目标坦克的下右y
            op3_y = op1_y + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
        }
        // 目标坦克的区域面
        return new Bounds(op1, op2_x, op3_y);
    }
    /**
     * 内部类：点
     */
    class Point {
        int x;
        int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    /**
     * 内部类：面
     * 由于面是一个规则的矩形，由 左上(xy)、右上(x)、右下(y) 三个点即可确定位置
     * 右上点的 y 和 左上点的 y 相等
     * 右下点的 x 和 右上点的 x 相等
     * 左下点的 x 和 左上点的 x 相等
     * 左下点的 y 和 右下点的 y 相等
     */
    class Bounds {
        // 左上
        Point p1;
        // 右上x
        int p2x;
        // 右下y
        int p3y;

        public Bounds(Point p1, int p2x, int p3y) {
            this.p1 = p1;
            this.p2x = p2x;
            this.p3y = p3y;
        }
    }
    /**
     * 上移
     */
    public void moveUp() {
        if (isTouch()) {
            direction = y % 2 == 0 ? Constant.DOWN : Constant.RIGHT;
        } else {
            y -= Constant.TANK_SPEED;
            if (y < Constant.INFO_BAR_HEIGHT) {
                y = Constant.INFO_BAR_HEIGHT;
                direction = Constant.RIGHT;
            }
        }
    }

    /**
     * 下移
     */
    public void moveDown() {
        if (isTouch()) {
            direction = y % 2 == 0 ? Constant.UP : Constant.LEFT;
        } else {
            y += Constant.TANK_SPEED;
            if (y > Constant.WINDOW_HEIGHT - Constant.TANK_WHEEL_HEIGHT - Constant.WINDOW_TITLE_HEIGHT) {
                y = Constant.WINDOW_HEIGHT - Constant.TANK_WHEEL_HEIGHT - Constant.WINDOW_TITLE_HEIGHT;
                direction = Constant.LEFT;
            }
        }
    }

    /**
     * 左移
     */
    public void moveLeft() {
        if (isTouch()) {
            direction = y % 2 == 0 ? Constant.RIGHT : Constant.DOWN;
        } else {
            x -= Constant.TANK_SPEED;
            if (x < 1) {
                x = 0;
                direction = Constant.UP;
            }
        }
    }

    /**
     * 右移
     */
    public void moveRight() {
        if (isTouch()) {
            direction = y % 2 == 0 ? Constant.LEFT : Constant.UP;
        } else {
            x += Constant.TANK_SPEED;
            if (x > Constant.WINDOW_WIDTH - Constant.TANK_WHEEL_HEIGHT) {
                x = Constant.WINDOW_WIDTH - Constant.TANK_WHEEL_HEIGHT;
                direction = Constant.DOWN;
            }
        }
    }

    /**
     * 射击
     * 抽象方法：由子类实现
     */
    public abstract void shot();

    /**
     * 死亡
     */
    public void death() {
        if (bulletThreadPool != null) {
            bulletThreadPool.shutdown();
            // 把该坦克的子弹存活状态设为 false
            for (Bullet bullet : bullets) {
                bullet.setLive(false);
            }
        }
        if (shotIntervalThreadPool != null) {
            shotIntervalThreadPool.shutdown();
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public boolean isAllowShot() {
        return allowShot;
    }

    public void setAllowShot(boolean allowShot) {
        this.allowShot = allowShot;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public ThreadPoolExecutor getBulletThreadPool() {
        return bulletThreadPool;
    }

    public ThreadPoolExecutor getShotIntervalThreadPool() {
        return shotIntervalThreadPool;
    }

    public List<Tank> getAllTanks() {
        return allTanks;
    }

    public void setAllTanks(List<Tank> allTanks) {
        this.allTanks = allTanks;
    }
}

