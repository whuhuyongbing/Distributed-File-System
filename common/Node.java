package common;
import java.io.Serializable;
/**
 * @author Yongbing Hu
 * @version 0.0.0
 * @time 2019-10-31 7:31 p.m.
 * @description
 */

public class Node implements Serializable
{
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Node(String name) {
        this.name = name;
    }
    public Node() {
    }
}