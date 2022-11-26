package tank;

/**
 * @author ajun
 * Date 2021/7/13
 * @version 1.0
 * 爆炸效果
 */
public class Bomb {
    // 坐标
    private int x,y;
    // 生命值
    private int life;
    // 是否存活
    private boolean live;

    /**
     * 初始化
     */
    private void init(){
        life = 9;
        live = true;
    }

    /**
     * 构造器
     * @param x
     * @param y
     */
    public Bomb(int x, int y) {
        this.x = x;
        this.y = y;
        init();
    }

    /**
     * 生命值减1
     */
    public void lifeDown(){
        if(life > 0){
            life --;
            return;
        }
        live = false;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getLife() {
        return life;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }
}
