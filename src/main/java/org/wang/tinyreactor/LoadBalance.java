package org.wang.tinyreactor;

import java.util.List;

public interface LoadBalance {


    /**
     * 负载均衡
     *
     * @param reactorList
     * @return
     */
    static SubReactor getSubReactor(List<SubReactor> reactorList) {
        return reactorList.get(0);
    }
}
