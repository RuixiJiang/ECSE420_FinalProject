package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ajun
 * Date 2021/7/16
 * @version 1.0
 * 记录游戏数据
 */
public class Recorder {
    // 摧毁敌方坦克数量
    public volatile static AtomicInteger destroyEnemyTankNum = new AtomicInteger(0);
    // 我方战毁数量
    public volatile static AtomicInteger destroyMyNum = new AtomicInteger(0);
    // 我方发射子弹数量
    public volatile static AtomicInteger myBulletNum = new AtomicInteger(0);

    // 定义IO对象，存取游戏数据
    private static BufferedWriter bw = null;
    private static BufferedReader br = null;
    private static String recordFile = "gameinfo.txt";

    // 摧毁敌方坦克数量加1
    public static void destroyEnemyTankNumAdd() {
        destroyEnemyTankNum.getAndIncrement();
    }

    // 我方战毁数量
    public static void destroyMyNumAdd() {
        destroyMyNum.getAndIncrement();
    }

    // 我方发射子弹数量加1
    public static void myBulletNumAdd() {
        myBulletNum.getAndIncrement();
    }

    // 保存游戏数据
    public static void saveInfo() {
        try {
            bw = new BufferedWriter(new FileWriter(recordFile));
            StringBuffer sb = new StringBuffer();
            sb.append(destroyEnemyTankNum + "\r\n")
                    .append(destroyMyNum + "\r\n")
                    .append(myBulletNum + "\r\n");
            bw.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 读取游戏数据
    public static void readInfo() {
        try {
            br = new BufferedReader(new FileReader(recordFile));
            String s;
            List<Integer> ls = new ArrayList<>();
            //循环读取
            while ((s = br.readLine()) != null) {
                ls.add(Integer.parseInt(s));
            }
            // 把读取到的数据赋值给记录属性
            if (ls.size() == 3) {
                destroyEnemyTankNum.set(ls.get(0));
                destroyMyNum.set(ls.get(1));
                myBulletNum.set(ls.get(2));
            }
        } catch (IOException e){
            // e.printStackTrace(); 如果读取不到文件，不抛错误信息。写入时会创建
        } finally{
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
