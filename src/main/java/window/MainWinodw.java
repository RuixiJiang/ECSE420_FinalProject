package window;

import constant.Constant;
import util.MyThreadFactory;
import util.Recorder;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.*;

/**
 * @author ajun
 * Date 2021/7/10
 * @version 1.0
 */
public class MainWinodw extends JFrame {
    // 活动面板
    private MyPanel myPanel;
    // 线程池
    private ThreadPoolExecutor threadPool;

    /**
     * 初始化
     */
    private void init(){
        // 线程池
        threadPool = new ThreadPoolExecutor(
                1, // 核心线程数
                1, // 最大线程数
                0L, // 空闲时间
                TimeUnit.MILLISECONDS, // 时间单位
                new LinkedBlockingQueue<Runnable>(),// 阻塞队列
                new MyThreadFactory("游戏界面"), // 线程工厂
                new ThreadPoolExecutor.DiscardOldestPolicy()); // 拒绝策略

        myPanel = new MyPanel();
        try{
            threadPool.execute(myPanel);
        }finally {
            threadPool.shutdown();
        }

        //new Thread(myPanel,"游戏界面").start();

        // 添加面板
        this.add(myPanel);
        // 窗口位置及大小
        this.setBounds(50,50, Constant.WINDOW_WIDTH,Constant.WINDOW_HEIGHT);
        // 关闭
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 保存游戏数据
                Recorder.saveInfo();
                System.exit(0);
            }
        });

        // 添加键盘监听
        this.addKeyListener(myPanel);
        // 不可改变窗口大小
        this.setResizable(false);
        // 设置窗口可见
        this.setVisible(true);
    }

    /**
     * 构造器
     */
    public MainWinodw(){
        // 游戏名称
        super("坦克大战");
        // 读取游戏数据
        Recorder.readInfo();
        // 初始化
        init();
    }
}
