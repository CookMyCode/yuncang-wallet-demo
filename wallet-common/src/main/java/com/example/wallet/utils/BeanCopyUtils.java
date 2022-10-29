package com.example.wallet.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class BeanCopyUtils {

    public static <S, T> T copyProperties(S source, Class<T> type) {
        T target = BeanUtils.instantiateClass(type);
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <S, T> List<T> copyProperties(List<S> sources, Class<T> type) {
        List<T> targetList = new ArrayList<>();
        if (sources != null) {
            for (S source : sources) {
                T target = BeanUtils.instantiateClass(type);
                BeanUtils.copyProperties(source, target);
                targetList.add(target);
            }
        }
        return targetList;
    }

}
