package org.cc.enterpriseagent.common.utils;

import java.util.List;
import java.util.stream.Collectors;

public class VectorUtils {
    public static String toVectorString(List<Float> vector){
        return vector.stream().map(String::valueOf).collect(Collectors.joining(",","[","]"));
    }
}
