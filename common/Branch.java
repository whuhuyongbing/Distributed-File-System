package common;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yongbing Hu
 * @version 0.0.0
 * @time 2019-11-05 4:53 p.m.
 * @description
 */
public class Branch extends Node {
    public List<Node> list;

    public Branch(String name) {
        super(name);
        this.list = new ArrayList<>();
    }



}
