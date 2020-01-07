package rmi;

import java.lang.reflect.Method;

/**
 * @author Yongbing Hu
 * @version 0.0.0
 * @time 2019-11-11 2:11 p.m.
 * @description
 */

/** this class is Utils for the rmi library
 *
 */
public class Utils {

    /** determine if a class is a remote interface
     *
     * @param c class
     * @return true if it is a remote interface otherwise false
     */
    public static boolean remoteInterface(Class<?> c) {
        if (c == null)
            throw new NullPointerException();
        if (!c.isInterface())
            return false;
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            boolean includeRMIE = false;
            Class<?>[] exceptionTypes = method.getExceptionTypes();
            for (Class<?> et : exceptionTypes) {
                if (et.equals(RMIException.class))
                    includeRMIE = true;
            }
            if (!includeRMIE)
                return false;
        }
        return true;
    }


    public static void remoteInterace(Class<?> c) {
        if (!remoteInterface(c))
            throw new Error(c + " is not a remoteInterface or not a interface");
    }
}
