package common;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Yongbing Hu
 * @version 0.0.0
 * @time 2019-10-31 7:31 p.m.
 * @description
 */
public class Info implements Serializable {
    private String methodName;
    private Object[] args;
    private Class<?>[] argsTypes;



    public Info(String methodName, Object[] args, Class<?>[] argsTypes) {
        this.methodName = methodName;
        this.args = args;
        this.argsTypes = argsTypes;
    }

    @Override
    public String toString() {
        return "Info{" +
                "methodName='" + methodName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", argsTypes=" + Arrays.toString(argsTypes) +
                '}';
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Class<?>[] getArgsTypes() {
        return argsTypes;
    }

    public void setArgsTypes(Class<?>[] argsTypes) {
        this.argsTypes = argsTypes;
    }
}

