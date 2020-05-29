package com.feida.ams.framework.interceptor;

import com.feida.common.annotation.ModuleAuthority;
import com.feida.common.consts.SysConstants;
import com.feida.common.domain.BaseResponse;
import com.feida.common.error.AccountError;
import com.feida.common.util.RedisUtil;
import com.feida.common.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * 功能模块/数据块 权限拦截器
 * @author sjq
 */

public class AuthorityInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisUtil redisUtil;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //当前登录用户如果是超级用户,则不做任何校验
//        Integer isSuper = (Integer) request.getAttribute(SysConstants.REQUEST_IS_SUPER);
//        if (isSuper != null && isSuper.compareTo(1) == 0) {
//            return true;
//        }
//        if(!(handler instanceof HandlerMethod)){
//            return true;
//        }
//        //功能模块权限拦截,验证是否有权限访问相应的功能模块
//        boolean result=true;
//        BaseResponse baseResponse=new BaseResponse();
//
//        HandlerMethod handlerMethod = (HandlerMethod) handler;
//        Method method = handlerMethod.getMethod();
//        ModuleAuthority moduleAuthority = method.getAnnotation(ModuleAuthority.class);
//        if (moduleAuthority != null) {
//            try {
//                String[] mcodes = moduleAuthority.mcode();    //提取当前访问的功能模块编码
//                if (mcodes == null || mcodes.length == 0) {
//                   result=false;
//                }
//                //从session中获取当前登录用户有权限的模块集合
//                Set<String> moduleSet = (Set<String>) request.getAttribute(SysConstants.REQUEST_RIGHT_SET);
//                if (moduleSet != null) {
//                    //如果多个code之间的关系为且,则任何一个code匹配不到功能块就返回false
//                    if (moduleAuthority.mflag() == 0) {
//                        for (String mcode : mcodes) {
//                            if(!moduleSet.contains(mcode)){
//                                result=false;
//                            }
//                        }
//                    } else {
//                        //如果多个code之间的关系为或,则任意一个code匹配到功能模块就返回true
//                        result=false;
//                        for (String mcode : mcodes) {
//                            if (moduleSet.contains(mcode)) {
//                                result=true;
//                            }
//                        }
//                    }
//                } else {
//                    result=false;
//                }
//            } catch (Exception e) {
//                result=false;
//            } finally {
//                if(!result){
//                    baseResponse=new BaseResponse(AccountError.NO_RIGHT.getCode(), AccountError.NO_RIGHT.getMsg());
//                    WebUtils.write(baseResponse.toJson(), response);
//                }
//                return result;
//            }
//        }
        return true;
    }
}