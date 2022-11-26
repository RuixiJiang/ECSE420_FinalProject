package util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author ajun
 * Date 2021/7/14
 * @version 1.0
 * 工具类
 */
public class MyUitls {
    /**
     * 生成一组不重复随机数
     *
     * @param start 开始位置：可以为负数
     * @param end   结束位置：end > start
     * @param count 数量 >= 0
     * @return
     */
    public static Set<Integer> getRandoms(int start, int end, int count) {
        // 参数有效性检查
        if (start > end || count < 1) {
            count = 0;
        }
        // 结束值 与 开始值 的差小于 总数量
        if ((end - start) < count) {
            count = (end - start) > 0 ? (end - start) : 0;
        }

        // 定义存放集合
        Set<Integer> set = new HashSet<>(count);
        if (count > 0) {
            Random r = new Random();
            // 一直生成足够数量后再停止
            while (set.size() < count) {
                set.add(start + r.nextInt(end - start));
            }
        }
        return set;
    }
}
