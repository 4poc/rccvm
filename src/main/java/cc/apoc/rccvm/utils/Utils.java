package cc.apoc.rccvm.utils;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

public class Utils {
    public static String join(Collection<?> col, String delim) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> iter = col.iterator();
        if (iter.hasNext())
            sb.append(iter.next().toString());
        while (iter.hasNext()) {
            sb.append(delim);
            sb.append(iter.next().toString());
        }
        return sb.toString();
    }

    /**
     * Returns the path relative to the base.
     * 
     * @param patha
     * @param pathb
     * @return
     */
    public static String relativize(String path, String base) {
        return new File(base).toURI().relativize(new File(path).toURI()).getPath();
    }
}
