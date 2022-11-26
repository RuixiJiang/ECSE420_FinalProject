package tank;

import constant.Constant;
import util.MyThreadFactory;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author
 * @version 1.0
 * tank
 */
public abstract class Tank {
    // coordinates
    private int x;
    private int y;
    // direction
    private int direction;
    // depends on shotIntervalThreadPool
    private boolean allowShot;
    // isAlive
    private boolean live;
    private List<Bullet> bullets;

    private ThreadPoolExecutor bulletThreadPool;

    private ThreadPoolExecutor shotIntervalThreadPool;
    // decide if there is collision
    private List<Tank> allTanks;

    /**
     * init
     */
    private void init() {
        allowShot = true;
        live = true;
        bullets = new CopyOnWriteArrayList<>();

        bulletThreadPool = new ThreadPoolExecutor(
                15,
                40,
                200,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(5),
                new MyThreadFactory("bullet"),
                new ThreadPoolExecutor.DiscardOldestPolicy());

        shotIntervalThreadPool = new ThreadPoolExecutor(
                3,
                5,
                100,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(2),
                new MyThreadFactory("shotInterval"),
                new ThreadPoolExecutor.DiscardOldestPolicy());


        allTanks = new CopyOnWriteArrayList<>();
    }

    /**
     * constructor
     *
     * @param x
     * @param y
     * @param direction
     * (x,y) marks the upper left corner of tank
     */
    public Tank(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        init();
    }

    /**
     * collisions
     *
     * @return
     */
    private boolean isTouch() {
        if (allTanks.size() > 1) {
            int x1, y1;
            Point p1;
            int x2, y2;
            Point p2;

            switch (getDirection()) {
                /*
                 facing up
                 */
                case Constant.UP:
                    // upper left corner
                    x1 = this.getX();
                    y1 = this.getY();
                    p1 = new Point(x1, y1);
                    //upper right corner
                    x2 = x1 + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
                    y2 = y1;
                    p2 = new Point(x2, y2);
                    if (isTouchCurrentTankAndOtherTank(this, p1, p2)) {
                        return true;
                    }

                    break;

                /*
                 facing right
                 */
                case Constant.RIGHT:

                    x1 = this.getX() + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
                    y1 = this.getY();
                    p1 = new Point(x1, y1);
                    x2 = x1;
                    y2 = y1 + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
                    p2 = new Point(x2, y2);
                    if (isTouchCurrentTankAndOtherTank(this, p1, p2)) {
                        return true;
                    }

                    break;

                /*
                 facing down
                 */
                case Constant.DOWN:
                    // lower left corner
                    x1 = this.getX();
                    y1 = this.getY() + Constant.TANK_WHEEL_HEIGHT;
                    p1 = new Point(x1, y1);
                    // lower right corner
                    x2 = x1 + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
                    y2 = y1;
                    p2 = new Point(x2, y2);
                    if (isTouchCurrentTankAndOtherTank(this, p1, p2)) {
                        return true;
                    }

                    break;

                /*
                 facing left
                 */
                default:
                    x1 = this.getX();
                    y1 = this.getY();
                    p1 = new Point(x1, y1);
                    x2 = x1;
                    y2 = y1 + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
                    p2 = new Point(x2, y2);
                    if (isTouchCurrentTankAndOtherTank(this, p1, p2)) {
                        return true;
                    }
            }
        }
        return false;
    }

    /**
     *
     *
     * @param p
     * @param b
     * @return
     */
    private boolean isPointInBounds(Point p, Bounds b) {


        //xp>x1&&yp>y1&&xp<x2&&yp>y1&&xp<x2&&yp<y2

        //xp>x1&&yp>y1
        if (p.x < b.p1.x || p.y < b.p1.y) {
            return false;
        }

        //xp<x2
        if (p.x > b.p2x) {
            return false;
        }

        //yp<y2
        if (p.y > b.p3y) {
            return false;
        }

        return true;
    }
    /**
     *
     *
     * @param currentTank
     * @param p1
     * @param p2
     * @return
     */
    private boolean isTouchCurrentTankAndOtherTank(Tank currentTank, Point p1, Point p2) {
        //enemy tank
        Bounds b;
        for (Tank tank : allTanks) {

            if (!currentTank.equals(tank)) {
                b = getBoundsByTank(tank);
                if (isPointInBounds(p1, b) || isPointInBounds(p2, b)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     *
     *
     * @param tank
     * @return
     */
    private Bounds getBoundsByTank(Tank tank) {
        int op1_x = tank.getX();
        int op1_y = tank.getY();
        Point op1 = new Point(op1_x, op1_y);

        int op2_x;
        int op3_y;

        int d = tank.getDirection();
        if (d == Constant.UP || d == Constant.DOWN) {

            op2_x = op1_x + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
            op3_y = op1_y + Constant.TANK_WHEEL_HEIGHT;
        } else {

            op2_x = op1_x + Constant.TANK_WHEEL_HEIGHT;
            op3_y = op1_y + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
        }
        return new Bounds(op1, op2_x, op3_y);
    }


    class Point {
        int x;
        int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }


    class Bounds {
        // upper left
        Point p1;
        // upper right
        int p2x;
        // lower right
        int p3y;

        public Bounds(Point p1, int p2x, int p3y) {
            this.p1 = p1;
            this.p2x = p2x;
            this.p3y = p3y;
        }
    }


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



    public abstract void shot();



    public void death() {
        if (bulletThreadPool != null) {
            bulletThreadPool.shutdown();
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

