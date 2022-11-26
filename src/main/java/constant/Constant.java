package constant;

/**
 * @author ajun
 * Date 2021/7/11
 * @version 1.0
 * 系统常量
 */
public class Constant {
    // 游戏界面宽度
    public static final int WINDOW_WIDTH = 1200;
    // 游戏界面高度
    public static final int WINDOW_HEIGHT = 900;
    // 界面顶部标题栏高度
    public static final int WINDOW_TITLE_HEIGHT = 40;
    // 信息栏高度
    public static  final int INFO_BAR_HEIGHT = 100;

    // 界面重绘时间
    public static final int REPAINT_TIME = 80;

    // 游戏状态
    public static final int GAME_WIN = 1;
    public static final int GAME_RUNNING = 0;
    public static final int GAME_FAIL = -1;
    public static final int GAME_INIT = -2;

    //我方坦克初始数量
    public static final int MY_TANK_INIT_NUM = 3;
    //敌方坦克初始数量
    public static final int ENEMY_TANK_INIT_NUM = 30;

    // 坦克轮子宽度
    public static final int TANK_WHEEL_WIDTH = 10;
    // 坦克轮子高度
    public static final int TANK_WHEEL_HEIGHT = 60;
    // 坦克主体宽度
    public static final int TANK_BODY_WIDTH = 20;
    // 坦克主体高度
    public static final int TANK_BODY_HEIGHT = 40;
    // 坦克速度
    public static final int TANK_SPEED = 3;

    // 子弹半径
    public static final int BULLET_RADIUS = 5;
    // 子弹速度
    public static final int BULLET_SPEED = 10;
    // 子弹每次运行的时间间隔
    public static final int BULLET_RUN_INTERVAL_TIME = 200;
    // 我方坦克发射子弹最少时间间隔(毫秒)，低于这个时间的发射无效
    public static final int MY_TANK_SHOT_MIN_INTERVAL_TIME = 150;
    // 敌方坦克发射子弹最少时间间隔(毫秒)
    public static final int ENEMY_TANK_SHOT_MIN_INTERVAL_TIME = 200;

    // 方向
    public static final int UP = 0;
    public static final int RIGHT = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
}
