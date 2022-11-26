package window;

import tank.*;
import constant.Constant;
import util.MyThreadFactory;
import util.MyUitls;
import util.PlayAudio;
import util.Recorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author ajun
 * Date 2021/7/10
 * @version 1.0
 * 坦克活动区域
 */
public class MyPanel extends JPanel implements KeyListener, Runnable {
    // 游戏状态
    private int status;
    // 我方坦克集合
    private volatile List<HeroTank> myTanks;
    // 敌方坦克集合
    private volatile List<EnemyTank> enemyTanks;
    // 所有坦克：用于判断是否碰撞
    private volatile List<Tank> allTanks;
    // 我方坦克数量
    private int myTankNum;
    // 敌方坦克数量
    private int enemyTankNum;
    // 我方坦克生成线程池
    private ThreadPoolExecutor myTankThreadPool;
    // 敌方坦克生成线程池
    private ThreadPoolExecutor enemyTankThreadPool;
    // 爆炸效果图片
    private List<Bomb> bombs;
    private Image image1;
    private Image image2;
    private Image image3;

    /**
     * 初始化
     */
    private void init(int status) {
        this.status = status;
        // 只有运行状态时再初始化其它
        if (this.status == Constant.GAME_RUNNING) {
            // 我方
            myTanks = new CopyOnWriteArrayList<>();
            myTankNum = Constant.MY_TANK_INIT_NUM;
            // 敌方
            enemyTanks = new CopyOnWriteArrayList<>();
            enemyTankNum = Constant.ENEMY_TANK_INIT_NUM;

            // 所有坦克
            allTanks = new CopyOnWriteArrayList<>();

            myTankThreadPool = new ThreadPoolExecutor(
                    1, // 核心线程数
                    1, // 最大线程数
                    200, // 空闲时间
                    TimeUnit.MILLISECONDS, // 时间单位
                    new ArrayBlockingQueue<>(5), // 阻塞队列
                    new MyThreadFactory("我方坦克"), // 线程工厂
                    new ThreadPoolExecutor.DiscardOldestPolicy()); // 拒绝策略

            enemyTankThreadPool = new ThreadPoolExecutor(
                    5, // 核心线程数
                    20, // 最大线程数
                    200, // 空闲时间
                    TimeUnit.MILLISECONDS, // 时间单位
                    new ArrayBlockingQueue<>(30), // 阻塞队列
                    new MyThreadFactory("敌方坦克"), // 线程工厂
                    new ThreadPoolExecutor.DiscardOldestPolicy()); // 拒绝策略

            // 坦克竖向宽度
            int tankWidth = 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH;
            // 竖向一排可以放置的数量
            int num = (int) Math.floor(Constant.WINDOW_WIDTH / tankWidth);

            /*
            初始化我的坦克
            初始位置
            X：横向随机
            y：窗口高度 - 坦克高度 - 偏移量 (偏移量包含标题栏的高度)
             */
            // 生成不重复随机数：用于坦克横向分布
            Object[] xs1 = MyUitls.getRandoms(0, num, myTankNum).toArray();
            int y1 = Constant.WINDOW_HEIGHT - Constant.TANK_WHEEL_HEIGHT - 2 * Constant.WINDOW_TITLE_HEIGHT;
            for (int i = 0; i < myTankNum; i++) {
                final int a = i;
                myTankThreadPool.execute(() -> {
                    HeroTank myTank = new HeroTank(tankWidth * (int) xs1[a], y1);
                    myTanks.add(myTank);

                    // 每二次创建(超过最大核心线程数)
                    if (a >= myTankThreadPool.getCorePoolSize()) {
                        // 给当前所有坦克设置 allTanks 属性
                        setAllTanks();
                    }

                    myTank.waitShot();

                });
            }

            /*
            初始化敌人坦克
            初始位置：顶部随机位置
             */
            // 生成不重复随机数：用于坦克横向分布
            Object[] xs2 = MyUitls.getRandoms(0, num, enemyTankNum).toArray();
            Object[] ys2 = MyUitls.getRandoms(Constant.WINDOW_TITLE_HEIGHT + Constant.INFO_BAR_HEIGHT, Constant.WINDOW_TITLE_HEIGHT + Constant.INFO_BAR_HEIGHT + 100, enemyTankNum).toArray();

            Random random = new Random();
            for (int j = 0; j < enemyTankNum; j++) {
                // 如果生成的随机数小于坦克数量，那么多出的坦克坐标值就在现在的随机数中获取
                int c = j;
                if (j >= num) {
                    c = random.nextInt(num);
                }
                final int b = c;

                enemyTankThreadPool.execute(() -> {
                    EnemyTank enemyTank = new EnemyTank(tankWidth * (int) xs2[b], (int) ys2[b]);
                    enemyTanks.add(enemyTank);
                    enemyTank.setMyTanks(myTanks);
                    enemyTank.shot();
                });
            }

            int myCorePoolSize = myTankThreadPool.getCorePoolSize();
            int enemyCorePoolSize = enemyTankThreadPool.getCorePoolSize();
            // 当所有初始坦克全部创建完后执行
            while (myTanks.size() < myCorePoolSize || enemyTanks.size() < enemyCorePoolSize) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            setAllTanks();

            /*
            爆炸效果
            */
            bombs = new CopyOnWriteArrayList<>();
            image1 = new ImageIcon(getClass().getResource("/res/image/bomb_1.gif")).getImage();
            image2 = new ImageIcon(getClass().getResource("/res/image/bomb_2.gif")).getImage();
            image3 = new ImageIcon(getClass().getResource("/res/image/bomb_3.gif")).getImage();

            /*
             播放音乐
             */
            new Thread(new PlayAudio("bg.wav"),"播放音乐").start();
        }
    }

    /**
     * 构造器
     */
    public MyPanel() {
        init(Constant.GAME_INIT);
    }

    /**
     * 绘制
     *
     * @param g 画笔
     */
    @Override
    public void paint(Graphics g) {
        // 清屏
        super.paint(g);
        // 活动区域，填充矩形，默认黑色
        g.fillRect(0, 0, Constant.WINDOW_WIDTH, Constant.WINDOW_HEIGHT);

        // 绘制战绩信息栏
        drawInfo(g);

        switch (status) {
            // 初始化
            case Constant.GAME_INIT:
                g.setColor(Color.green);
                g.setFont(new Font("微软雅黑", Font.BOLD, 40));
                g.drawString("坦克大战", Constant.WINDOW_WIDTH / 2 - 70, Constant.WINDOW_HEIGHT / 2 - 50);
                g.setColor(Color.orange);
                g.setFont(new Font("微软雅黑", Font.PLAIN, 30));
                g.drawString("按回车键Enter 开始游戏", Constant.WINDOW_WIDTH / 2 - 150, Constant.WINDOW_HEIGHT / 2 + 10);
                g.setColor(Color.GRAY);
                g.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                g.drawString("W:向上移动 / A:向左移动 / S:向下移动 / D:向右移动 / ↑:发射", 30, Constant.WINDOW_HEIGHT - 60);
                break;
            // 进行中
            case Constant.GAME_RUNNING:
                // 绘制我方坦克
                Iterator<HeroTank> iterator1 = myTanks.iterator();
                HeroTank myTank;
                List<Bullet> myBullets;
                while (iterator1.hasNext()) {
                    myTank = iterator1.next();
                    if (myTank.isLive()) {
                        drawTank(
                                myTank.getX(),
                                myTank.getY(),
                                g,
                                myTank.getDirection(),
                                0);
                        // 绘制子弹
                        myBullets = myTank.getBullets();
                        for (Bullet bullet1 : myBullets) {
                            // 如果子弹存在，并且是存活的，就绘制
                            if (bullet1 != null && bullet1.isLive()) {
                                g.setColor(Color.MAGENTA);
                                g.fillOval(bullet1.getX(), bullet1.getY(), 2 * Constant.BULLET_RADIUS, 2 * Constant.BULLET_RADIUS);
                            }
                            // 如果子弹存在，并且不再存活，就清除
                            if (bullet1 != null && !bullet1.isLive()) {
                                myBullets.remove(bullet1);
                            }
                        }
                    }
                }

                // 绘制敌人坦克
                Iterator<EnemyTank> iterator2 = enemyTanks.iterator();
                EnemyTank enemyTank;
                List<Bullet> enemyBullets;
                while (iterator2.hasNext()) {
                    enemyTank = iterator2.next();
                    if (enemyTank.isLive()) {
                        drawTank(
                                enemyTank.getX(),
                                enemyTank.getY(),
                                g,
                                enemyTank.getDirection(),
                                1);
                        // 绘制子弹
                        enemyBullets = enemyTank.getBullets();
                        for (Bullet bullet2 : enemyBullets) {
                            // 如果子弹存在，并且是存活的，就绘制
                            if (bullet2 != null && bullet2.isLive()) {
                                g.setColor(Color.PINK);
                                g.fillOval(bullet2.getX(), bullet2.getY(), 2 * Constant.BULLET_RADIUS, 2 * Constant.BULLET_RADIUS);
                            }
                            // 如果子弹存在，并且不再存活，就清除
                            if (bullet2 != null && !bullet2.isLive()) {
                                enemyBullets.remove(bullet2);
                            }
                        }
                    }
                }

                // 绘制爆炸效果
                drawBomb(bombs, g);
                break;
            // 胜利
            case Constant.GAME_WIN:
                // 绘制最后的爆炸效果
                drawBomb(bombs, g);

                g.setColor(Color.green);
                g.setFont(new Font("微软雅黑", Font.BOLD, 40));
                g.drawString("恭喜！胜利了！", Constant.WINDOW_WIDTH / 2 - 120, Constant.WINDOW_HEIGHT / 2 - 50);
                g.setColor(Color.orange);
                g.setFont(new Font("微软雅黑", Font.PLAIN, 30));
                g.drawString("按回车键Enter 重新开始", Constant.WINDOW_WIDTH / 2 - 150, Constant.WINDOW_HEIGHT / 2 + 10);
                g.setColor(Color.GRAY);
                g.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                g.drawString("W:向上移动 / A:向左移动 / S:向下移动 / D:向右移动 / ↑:发射", 30, Constant.WINDOW_HEIGHT - 60);
                break;
            // 失败
            default:
                // 绘制最后的爆炸效果
                drawBomb(bombs, g);

                g.setColor(Color.red);
                g.setFont(new Font("微软雅黑", Font.BOLD, 40));
                g.drawString("游戏结束！", Constant.WINDOW_WIDTH / 2 - 85, Constant.WINDOW_HEIGHT / 2 - 50);
                g.setColor(Color.orange);
                g.setFont(new Font("微软雅黑", Font.PLAIN, 30));
                g.drawString("按回车键Enter 重新开始", Constant.WINDOW_WIDTH / 2 - 150, Constant.WINDOW_HEIGHT / 2 + 10);
                g.setColor(Color.GRAY);
                g.setFont(new Font("微软雅黑", Font.PLAIN, 20));
                g.drawString("W:向上移动 / A:向左移动 / S:向下移动 / D:向右移动 / ↑:发射", 30, Constant.WINDOW_HEIGHT - 60);
        }
    }

    /**
     * 绘制战绩信息栏
     */
    private void drawInfo(Graphics g) {
        // 摧毁敌方坦克数
        String destroyEnemyTankNum = String.valueOf(Recorder.destroyEnemyTankNum);
        // 我方战损数
        String destroyMyTankNum = String.valueOf(Recorder.destroyMyNum);
        // 我方发射子弹数
        String myBulletNum = String.valueOf(Recorder.myBulletNum);

        // 绘制顶部信息栏
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Constant.WINDOW_WIDTH, Constant.INFO_BAR_HEIGHT);
        // 敌方信息
        g.setColor(Color.GRAY);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 25));
        g.drawString("摧毁", 100, 45);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 25));
        g.drawString("敌方", 100, 75);
        drawTank(160, 20, g, Constant.UP, 1);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 50));
        g.drawString(destroyEnemyTankNum, 220, 70);

        // 我方信息
        g.setColor(Color.GRAY);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 25));
        g.drawString("我方", Constant.WINDOW_WIDTH / 2 - 100, 45);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 25));
        g.drawString("战损", Constant.WINDOW_WIDTH / 2 - 100, 75);
        drawTank(Constant.WINDOW_WIDTH / 2 - 40, 20, g, Constant.UP, 0);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 50));
        g.drawString(destroyMyTankNum, Constant.WINDOW_WIDTH / 2 + 20, 70);

        g.setColor(Color.GRAY);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 25));
        g.drawString("发射子弹：", Constant.WINDOW_WIDTH / 2 + 230, 60);
        g.setColor(Color.WHITE);
        g.setFont(new Font("微软雅黑", Font.PLAIN, 30));
        g.drawString(myBulletNum, Constant.WINDOW_WIDTH / 2 + 350, 62);
    }

    /**
     * 绘制坦克
     *
     * @param x         坦克左上角的 x 坐标
     * @param y         坦克左上角的 y 坐标
     * @param g         画笔
     * @param direction 方向：up：向上，down：向下，left：向左，right：向右
     * @param type      类型：敌、我...
     */
    private void drawTank(int x, int y, Graphics g, int direction, int type) {
        // 根据类型设置坦克颜色
        switch (type) {
            // 我的坦克
            case 0:
                g.setColor(Color.ORANGE);
                break;
            // 敌人坦克
            case 1:
                g.setColor(Color.CYAN);
                break;
            default:
        }

        // 根据方向绘制坦克
        switch (direction) {
            case Constant.UP:
                // 左边轮子
                g.fill3DRect(
                        x,
                        y,
                        Constant.TANK_WHEEL_WIDTH,
                        Constant.TANK_WHEEL_HEIGHT,
                        false);
                // 右边轮子
                g.fill3DRect(
                        x + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH,
                        y,
                        Constant.TANK_WHEEL_WIDTH,
                        Constant.TANK_WHEEL_HEIGHT,
                        false);
                // 坦克体
                g.fill3DRect(
                        x + Constant.TANK_WHEEL_WIDTH,
                        y + (Constant.TANK_WHEEL_HEIGHT - Constant.TANK_BODY_HEIGHT) / 2,
                        Constant.TANK_BODY_WIDTH,
                        Constant.TANK_BODY_HEIGHT,
                        false);
                // 坦克转盘
                g.fillOval(
                        x + Constant.TANK_WHEEL_WIDTH,
                        y + (Constant.TANK_WHEEL_HEIGHT - Constant.TANK_BODY_WIDTH) / 2,
                        Constant.TANK_BODY_WIDTH,
                        Constant.TANK_BODY_WIDTH);
                // 炮塔
                g.drawLine(
                        x + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2,
                        y + Constant.TANK_WHEEL_HEIGHT / 2,
                        x + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2,
                        y);
                break;
            case Constant.DOWN:
                // 左边轮子
                g.fill3DRect(
                        x,
                        y,
                        Constant.TANK_WHEEL_WIDTH,
                        Constant.TANK_WHEEL_HEIGHT,
                        false);
                // 右边轮子
                g.fill3DRect(
                        x + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH,
                        y,
                        Constant.TANK_WHEEL_WIDTH,
                        Constant.TANK_WHEEL_HEIGHT,
                        false);
                // 坦克体
                g.fill3DRect(
                        x + Constant.TANK_WHEEL_WIDTH,
                        y + (Constant.TANK_WHEEL_HEIGHT - Constant.TANK_BODY_HEIGHT) / 2,
                        Constant.TANK_BODY_WIDTH,
                        Constant.TANK_BODY_HEIGHT,
                        false);
                // 坦克转盘
                g.fillOval(
                        x + Constant.TANK_WHEEL_WIDTH,
                        y + (Constant.TANK_WHEEL_HEIGHT - Constant.TANK_BODY_WIDTH) / 2,
                        Constant.TANK_BODY_WIDTH,
                        Constant.TANK_BODY_WIDTH);
                // 炮塔
                g.drawLine(
                        x + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2,
                        y + Constant.TANK_WHEEL_HEIGHT / 2,
                        x + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2,
                        y + Constant.TANK_WHEEL_HEIGHT);
                break;
            case Constant.LEFT:
                // 上边轮子
                g.fill3DRect(
                        x,
                        y,
                        Constant.TANK_WHEEL_HEIGHT,
                        Constant.TANK_WHEEL_WIDTH,
                        false);
                // 下边轮子
                g.fill3DRect(
                        x,
                        y + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH,
                        Constant.TANK_WHEEL_HEIGHT,
                        Constant.TANK_WHEEL_WIDTH,
                        false);
                // 坦克体
                g.fill3DRect(
                        x + (Constant.TANK_WHEEL_HEIGHT - Constant.TANK_BODY_HEIGHT) / 2,
                        y + Constant.TANK_WHEEL_WIDTH,
                        Constant.TANK_BODY_HEIGHT,
                        Constant.TANK_BODY_WIDTH,
                        false);
                // 坦克转盘
                g.fillOval(
                        x + (Constant.TANK_WHEEL_HEIGHT - Constant.TANK_BODY_WIDTH) / 2,
                        y + Constant.TANK_WHEEL_WIDTH,
                        Constant.TANK_BODY_WIDTH,
                        Constant.TANK_BODY_WIDTH);
                // 炮塔
                g.drawLine(
                        x + Constant.TANK_WHEEL_HEIGHT / 2,
                        y + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2,
                        x,
                        y + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2);
                break;
            case Constant.RIGHT:
                // 上边轮子
                g.fill3DRect(
                        x,
                        y,
                        Constant.TANK_WHEEL_HEIGHT,
                        Constant.TANK_WHEEL_WIDTH,
                        false);
                // 下边轮子
                g.fill3DRect(
                        x,
                        y + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH,
                        Constant.TANK_WHEEL_HEIGHT,
                        Constant.TANK_WHEEL_WIDTH,
                        false);
                // 坦克体
                g.fill3DRect(
                        x + (Constant.TANK_WHEEL_HEIGHT - Constant.TANK_BODY_HEIGHT) / 2,
                        y + Constant.TANK_WHEEL_WIDTH,
                        Constant.TANK_BODY_HEIGHT,
                        Constant.TANK_BODY_WIDTH,
                        false);
                // 坦克转盘
                g.fillOval(
                        x + (Constant.TANK_WHEEL_HEIGHT - Constant.TANK_BODY_WIDTH) / 2,
                        y + Constant.TANK_WHEEL_WIDTH,
                        Constant.TANK_BODY_WIDTH,
                        Constant.TANK_BODY_WIDTH);
                // 炮塔
                g.drawLine(
                        x + Constant.TANK_WHEEL_HEIGHT / 2,
                        y + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2,
                        x + Constant.TANK_WHEEL_HEIGHT,
                        y + Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH / 2);
                break;
            default:
        }
    }

    /**
     * 绘制爆炸效果
     *
     * @param bombs
     * @param g
     */
    private void drawBomb(List<Bomb> bombs, Graphics g) {
        // 绘制爆炸效果
        for (Bomb bomb : bombs) {
            if (bomb.getLife() > 6) {
                g.drawImage(image1, bomb.getX(), bomb.getY(), Constant.TANK_WHEEL_HEIGHT, Constant.TANK_WHEEL_HEIGHT, this);
            } else if (bomb.getLife() > 3) {
                g.drawImage(image2, bomb.getX(), bomb.getY(), Constant.TANK_WHEEL_HEIGHT, Constant.TANK_WHEEL_HEIGHT, this);
            } else {
                g.drawImage(image3, bomb.getX(), bomb.getY(), Constant.TANK_WHEEL_HEIGHT, Constant.TANK_WHEEL_HEIGHT, this);
            }
            bomb.lifeDown();
            if (bomb.getLife() == 0) {
                bombs.remove(bomb);
            }
        }
    }

    /**
     * 给当前所有坦克设置 allTanks 属性
     * 用于防止碰撞
     *
     * @return
     */
    private void setAllTanks() {
        allTanks.clear();

        allTanks.addAll(myTanks);
        allTanks.addAll(enemyTanks);

        // 遍历所有坦克设置 allTanks 属性
        for (Tank tank : allTanks) {
            tank.setAllTanks(allTanks);
        }
    }

    /**
     * 键盘输出
     *
     * @param e
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * 键盘按下
     *
     * @param e
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // 判断按键
        switch (keyCode) {
            // w：向上
            case KeyEvent.VK_W:
                if (myTanks != null && myTanks.size() > 0) {
                    myTanks.get(0).setDirection(Constant.UP);
                    myTanks.get(0).moveUp();
                }
                break;
            // s：向下
            case KeyEvent.VK_S:
                if (myTanks != null && myTanks.size() > 0) {
                    myTanks.get(0).setDirection(Constant.DOWN);
                    myTanks.get(0).moveDown();
                }
                break;
            // a：向左
            case KeyEvent.VK_A:
                if (myTanks != null && myTanks.size() > 0) {
                    myTanks.get(0).setDirection(Constant.LEFT);
                    myTanks.get(0).moveLeft();
                }
                break;
            // d：向右
            case KeyEvent.VK_D:
                if (myTanks != null && myTanks.size() > 0) {
                    myTanks.get(0).setDirection(Constant.RIGHT);
                    myTanks.get(0).moveRight();
                }
                break;
            // 向上箭头：发射子弹
            case KeyEvent.VK_UP:
                // 发射
                if (myTanks != null && myTanks.size() > 0) {
                    myTanks.get(0).shot();
                    Recorder.myBulletNumAdd();
                }
                break;
            // 回车键：重新开始
            case KeyEvent.VK_ENTER:
                // 重新开始
                init(Constant.GAME_RUNNING);
                break;
            default:
        }

        // 重绘
        repaint();
    }

    /**
     * 键盘释放
     *
     * @param e
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }

    /**
     * 循环
     * 判断我方子弹是否击中敌方坦克
     * 让敌方坦克循环发射
     * 判断游戏状态
     */
    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(Constant.REPAINT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 运行状态时再做判断
            if (status == Constant.GAME_RUNNING) {
                // 判断我方子弹是否击中敌方坦克
                List<Bullet> myBs;
                for (HeroTank myTank : myTanks) {
                    myBs = myTank.getBullets();
                    for (Bullet mb : myBs) {
                        if (mb.isLive()) {
                            for (EnemyTank enemyTank : enemyTanks) {
                                hitTank(mb, enemyTank);
                            }
                        }
                    }
                }
                // 判断敌人子弹是否击中我方坦克
                List<Bullet> enemyBs;
                for (EnemyTank enemyTank : enemyTanks) {
                    enemyBs = enemyTank.getBullets();
                    for (Bullet eb : enemyBs) {
                        if (eb.isLive()) {
                            for (HeroTank myTank : myTanks) {
                                hitTank(eb, myTank);
                            }
                        }
                    }
                }
            }
            // 判断游戏状态
            status = gameStatus();
            // 重绘
            repaint();
        }
    }

    /**
     * 击中坦克
     *
     * @param bullet
     * @param tank
     */
    private void hitTank(Bullet bullet, Tank tank) {
        // 根据坦克方向判断子弹是否进入坦克区域
        boolean xArea;
        boolean yArea;
        switch (tank.getDirection()) {
            case Constant.UP:
            case Constant.DOWN:
                // x 区域：
                xArea = bullet.getX() > tank.getX() && bullet.getX() < (tank.getX() + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH);
                // y 区域：
                yArea = bullet.getY() > tank.getY() && bullet.getY() < (tank.getY() + Constant.TANK_WHEEL_HEIGHT);
                if (xArea && yArea) {
                    // 定义爆炸效果
                    Bomb bomb = new Bomb(tank.getX(), tank.getY());
                    // 加入爆炸集合
                    bombs.add(bomb);
                    // 爆炸后把子弹live设为 false
                    bullet.setLive(false);
                    // 爆炸后把坦克live设为 false
                    tank.setLive(false);
                    // 是否为敌人坦克
                    if (tank instanceof EnemyTank) {
                        // 爆炸后把坦克从敌方坦克集合中清除
                        enemyTanks.remove(tank);
                        Recorder.destroyEnemyTankNumAdd();
                    } else {
                        // 爆炸后把我方坦克从我方坦克集合中清除
                        myTanks.remove(tank);
                        Recorder.destroyMyNumAdd();
                    }
                    // 调用坦克的死亡方法：停止发射子弹线程池、子弹发射间隔时间线程池
                    tank.death();

                    // 给当前所有坦克设置 allTanks 属性
                    setAllTanks();
                }
                break;
            case Constant.LEFT:
            case Constant.RIGHT:
                // x 区域：
                xArea = bullet.getX() > tank.getX() && bullet.getX() < (tank.getX() + Constant.TANK_WHEEL_HEIGHT);
                // y 区域：
                yArea = bullet.getY() > tank.getY() && bullet.getY() < (tank.getY() + 2 * Constant.TANK_WHEEL_WIDTH + Constant.TANK_BODY_WIDTH);
                if (xArea && yArea) {
                    Bomb bomb = new Bomb(tank.getX(), tank.getY());
                    bombs.add(bomb);
                    bullet.setLive(false);
                    tank.setLive(false);
                    // 是否为敌人坦克
                    if (tank instanceof EnemyTank) {
                        // 爆炸后把坦克从敌方坦克集合中清除
                        enemyTanks.remove(tank);
                        Recorder.destroyEnemyTankNumAdd();
                    } else {
                        // 爆炸后把我方坦克从我方坦克集合中清除
                        myTanks.remove(tank);
                        Recorder.destroyMyNumAdd();
                    }
                    tank.death();

                    // 给当前所有坦克设置 allTanks 属性
                    setAllTanks();
                }
                break;
            default:
        }
    }

    /**
     * 游戏状态
     *
     * @return
     */
    private int gameStatus() {
        // 运行状态时再做判断
        if (status == Constant.GAME_RUNNING) {
            // 我方坦克全部催毁（数量为 0）
            if (myTanks.size() < 1 && myTankThreadPool.getCompletedTaskCount() == myTankNum) {
                // 把敌方坦克全部清除，结束线程
                for (EnemyTank enemyTank : enemyTanks) {
                    enemyTank.setLive(false);
                    enemyTank.death();
                }
                // 结束线程池
                if (myTankThreadPool != null) {
                    myTankThreadPool.shutdown();
                }
                if (enemyTankThreadPool != null) {
                    enemyTankThreadPool.shutdown();
                }
                //保存游戏数据
                Recorder.saveInfo();
                return Constant.GAME_FAIL;
            }
            // 敌方坦克全部催毁（数量为 0）
            if (enemyTanks.size() < 1 && enemyTankThreadPool.getCompletedTaskCount() == enemyTankNum) {
                // 把我方坦克清除，结束线程
                for (HeroTank myTank : myTanks) {
                    myTank.setLive(false);
                    myTank.death();
                }
                // 结束线程池
                if (myTankThreadPool != null) {
                    myTankThreadPool.shutdown();
                }
                if (enemyTankThreadPool != null) {
                    enemyTankThreadPool.shutdown();
                }
                //保存游戏数据
                Recorder.saveInfo();
                return Constant.GAME_WIN;
            }
        } else {
            // 非运行状态时保持状态不变
            if (myTanks != null) {
                for (Tank tank : myTanks) {
                    tank.setLive(false);
                    tank.death();
                }
            }
            if (enemyTanks != null) {
                for (Tank tank : enemyTanks) {
                    tank.setLive(false);
                    tank.death();
                }
            }
        }
        return status;
    }
}
