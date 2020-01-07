package naming;

import rmi.RMIException;
import rmi.Skeleton;

/**
 * @author Yongbing Hu
 * @version 0.0.0
 * @time 2019-11-01 2:36 a.m.
 * @description
 */
public class ServerEngine<T> implements Runnable {
    private Skeleton<T> skeleton;
    public ServerEngine(Skeleton<T> skeleton) {
        this.skeleton = skeleton;
    }
    @Override
    public void run() {
        try {
            this.skeleton.start();
        } catch (RMIException e) {
            e.printStackTrace();
        }
    }
}
