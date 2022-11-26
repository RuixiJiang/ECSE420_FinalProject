package util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author
 * @version 1.0
 * helper class
 */
public class MyUitls {
    /**
     * generate set of random numbers
     *
     * @param start
     * @param end
     * @param count  >= 0
     * @return
     */
    public static Set<Integer> getRandoms(int start, int end, int count) {

        if (start > end || count < 1) {
            count = 0;
        }

        if ((end - start) < count) {
            count = (end - start) > 0 ? (end - start) : 0;
        }

        // store result
        Set<Integer> set = new HashSet<>(count);
        if (count > 0) {
            Random r = new Random();
            // generate numbers
            while (set.size() < count) {
                set.add(start + r.nextInt(end - start));
            }
        }
        return set;
    }
}
