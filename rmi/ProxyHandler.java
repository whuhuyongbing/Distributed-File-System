package rmi;
import common.Info;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import static java.lang.reflect.Proxy.isProxyClass;

/**
 * @author Yongbing Hu
 * @version 0.0.0
 * @time 2019-11-08 7:22 p.m.
 * @description
 */
public class ProxyHandler<T> implements InvocationHandler, Serializable {
    private Class<T> ci;
    private InetSocketAddress sockAddr;

    public ProxyHandler(Class<T> c, InetSocketAddress sockAddr) {
        if (c == null || sockAddr == null)
            throw new NullPointerException();

        this.ci = c;
        this.sockAddr = sockAddr;
    }

    public Class<T> getClassInterface() {
        return ci;
    }

    public InetSocketAddress getSockAddr() {
        return sockAddr;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.equals(Object.class.getMethod("equals", Object.class))) {
            if (args[0] == null)
                return false;

            if (!isProxyClass(args[0].getClass()))
                return false;

            ProxyHandler ph = (ProxyHandler) Proxy.getInvocationHandler(args[0]);

            return ci.equals(ph.getClassInterface()) && sockAddr.equals(ph.getSockAddr());
        }

        if (method.equals(Object.class.getMethod("hashCode"))) {
            return (sockAddr.toString()  + ci.toString()).hashCode();
        }

        if (method.equals(Object.class.getMethod("toString"))) {
            return "Class: " + ci + ", Address: " + sockAddr;
        } else
            return run(method, args);
    }

    public Object run(Method method, Object[] args) throws Throwable {
        Object result;
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;
        Socket socket = null;
        try {

            socket = new Socket();
            socket.connect(this.sockAddr);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(socket.getInputStream());
            Info info = new Info(method.getName(), args, method.getParameterTypes());
            outputStream.writeObject(info);
            Object resultObj = inputStream.readObject();
            if (resultObj instanceof InvocationTargetException
                    || resultObj instanceof ClassNotFoundException || resultObj instanceof IllegalAccessException
                    || resultObj instanceof IllegalArgumentException || resultObj instanceof SecurityException)
                throw ((Exception) resultObj).getCause();
            if (resultObj instanceof NoSuchMethodException)
                throw new RMIException("No such method in interface");
            result = resultObj;
        } catch (Exception e) {
            if (Arrays.asList(method.getExceptionTypes()).contains(e.getClass()))
                throw e;
            if (e instanceof IOException)
                throw new RMIException(e.getMessage());
            else
                throw e;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
                if (socket != null) socket.close();
            } catch (IOException ioe) {
                 ioe.printStackTrace();
            }
        }
        return result;
    }
}

