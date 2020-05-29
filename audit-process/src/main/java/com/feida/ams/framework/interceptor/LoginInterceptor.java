package com.feida.ams.framework.interceptor;

import com.alibaba.fastjson.JSON;
import com.feida.ams.api.pc.system.account.service.AccountService;
import com.feida.common.consts.SysConstants;
import com.feida.common.domain.BaseDto;
import com.feida.common.domain.BaseResponse;
import com.feida.common.domain.Dto;
import com.feida.common.error.AccountError;
import com.feida.common.error.MyError;
import com.feida.common.error.SystemError;
import com.feida.common.util.JwtToken;
import com.feida.common.util.RedisUtil;
import com.feida.common.util.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginInterceptor implements HandlerInterceptor {
    public static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @Autowired
    private AccountService accountService;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        String url = request.getRequestURI();
        if (url.equals("/error")) {
            logger.error(SysConstants.NO_URL);
            errResponse(response, SystemError.NO_URL);
            return false;
        }
        // 获取参数 检查token
        Dto params = WebUtils.getParamsAsDto();
        String token = params.getString(SysConstants.REQUEST_TOKEN_NAME);
        if (null == token || "".equals(token) || "undefined".equals(token)) {
            logger.error(AccountError.TOKEN_NONE.getMsg());
            errResponse(response, AccountError.NO_TOKEN);
            return false;
        }

        Integer appUID = null;
        try {
            appUID = JwtToken.getAppUID(token);
        } catch (Exception e) {
            logger.error(AccountError.WRONG_TOKEN.getMsg());
            errResponse(response, AccountError.WRONG_TOKEN);
            return false;
        }
        if (appUID == null || appUID.compareTo(1) < 0) {
            logger.error(AccountError.WRONG_TOKEN.getMsg());
            errResponse(response, AccountError.WRONG_TOKEN);
            return false;
        }

        Dto account = null;
        String tokenInCache = (String) redisUtil.get(SysConstants.REDIS_TOKEN_KEY_PREFIX + appUID);
        if (StringUtils.isBlank(tokenInCache)) {
            if ("1".equals(params.getString("reqFromType"))) { // 如果是浏览器
                errResponse(response, AccountError.LOGIN_TIME_OUT);
                return false;
            }
            account = accountService.getDtoByToken(new BaseDto("token", token));
            if (null == account) {
                logger.error(AccountError.TOKEN_NONE.getMsg());
                errResponse(response, AccountError.TOKEN_NONE);
                return false;
            }
            try {
                accountService.getRightsToRedis(account);
            } catch (Exception e) {
                logger.error(AccountError.PRIVILEGES_ERROR.getMsg());
                errResponse(response, AccountError.PRIVILEGES_ERROR);
                return false;
            }
        } else if (!token.equals(tokenInCache)) {
            logger.error(AccountError.TOKEN_NONE.getMsg());
            errResponse(response, AccountError.TOKEN_NONE);
            return false;
        } else {
            account = (Dto) redisUtil.get(SysConstants.REDIS_ACCOUNT_KEY_PREFIX + appUID);
            redisUtil.expire(SysConstants.REDIS_ACCOUNT_KEY_PREFIX + appUID, 9000);
            redisUtil.expire(SysConstants.REDIS_TOKEN_KEY_PREFIX + appUID, 9000);
        }
        if (null == account) {
            errResponse(response, AccountError.LOGIN_TIME_OUT);
            return false;
        }
//        System.out.println(token+ " token key expire:"+redisUtil.getExpire(SysConstants.REDIS_TOKEN_KEY_PREFIX + appUID)+ "秒");

        params.put("loggedAccountId", account.get("id"));

        request.setAttribute(SysConstants.SESSION_LOGGED_USER, account);
        request.setAttribute(SysConstants.REQUEST_PARAM_NAME, params);
        request.setAttribute(SysConstants.REQUEST_RIGHT_SET, account.get(SysConstants.REQUEST_RIGHT_SET));
        request.setAttribute(SysConstants.REQUEST_IS_SUPER, account.get("isSuperuser"));
        return true;
    }

    public void errResponse(HttpServletResponse response, MyError error) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(JSON.toJSONString(new BaseResponse(error.getCode(), error.getMsg())));
    }

}
