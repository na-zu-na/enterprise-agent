package org.cc.enterpriseagent.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserContext {
    public static Long getUserId(){
        ServletRequestAttributes attributes= (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes==null){
            return null;
        }

        HttpServletRequest request=attributes.getRequest();
        Object currentUserId = request.getAttribute("CURRENT_USER_ID");
        return currentUserId!=null? Long.valueOf(currentUserId.toString()):null;
    }

    public static String getToken(){
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        Object userIdObj = request.getAttribute("TOKEN");
        return userIdObj != null ? (String) userIdObj : null;
    }
}
