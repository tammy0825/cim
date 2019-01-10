package com.crossoverjie.cim.route.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.crossoverjie.cim.common.exception.CIMException;
import com.crossoverjie.cim.common.pojo.CIMUserInfo;
import com.crossoverjie.cim.route.pojo.RegisterInfoResDTO;
import com.crossoverjie.cim.route.service.AccountService;
import com.crossoverjie.cim.route.service.UserInfoCacheService;
import com.crossoverjie.cim.route.vo.req.ChatReqVO;
import com.crossoverjie.cim.route.vo.req.LoginReqVO;
import com.crossoverjie.cim.route.vo.res.CIMServerResVO;
import com.crossoverjie.cim.route.vo.res.RegisterInfoResVO;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.crossoverjie.cim.common.constant.CIMUserInfoConstant.USERNAME;
import static com.crossoverjie.cim.common.enums.StatusEnum.*;
import static com.crossoverjie.cim.route.constant.Constant.*;

/**
 * Function:
 *
 * @author crossoverJie
 * Date: 2018/12/23 21:58
 * @since JDK 1.8
 */
@Service
public class AccountServiceRedisImpl implements AccountService {
    private final static Logger LOGGER = LoggerFactory.getLogger(AccountServiceRedisImpl.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserInfoCacheService userInfoCacheService;

    @Autowired
    private OkHttpClient okHttpClient;

    private MediaType mediaType = MediaType.parse("application/json");

    @Override
    public RegisterInfoResVO register(RegisterInfoResDTO info) {
        RegisterInfoResVO infoRes = null;
        String key = ACCOUNT_PREFIX + info.getUserId();

        // 判断userId是否存在
        boolean hasAccount = redisTemplate.hasKey(key);

        if (hasAccount) {
            // 用户id已存在
            throw new CIMException(REGISTER_FAIL);
        } else {
            String password = PASSWORD_PREFIX + info.getUserId();
            // 保存密码
            redisTemplate.opsForValue().set(password, info.getPassword());
            // 保存用户信息
            redisTemplate.opsForHash().put(key, USERNAME, info.getUserName());

            infoRes = new RegisterInfoResVO(info.getUserId(), info.getUserName());
        }


        return infoRes;
    }

    @Override
    public RegisterInfoResVO registerUniqueName(RegisterInfoResDTO info) {
        RegisterInfoResVO infoRes = null;
        String account = ACCOUNT_PREFIX + info.getUserId();

        // 判断用户id是否已存在
        boolean hasAccount = redisTemplate.hasKey(account);
        boolean hasUsername = redisTemplate.opsForHash().hasKey(USERNAME_MAP, info.getUserName());

        if (hasAccount) {
            // 用户id已存在
            throw new CIMException(REGISTER_FAIL);
        } else {
            if (hasUsername) {
                // 用户名已存在
                throw new CIMException(REPEAT_USERNAME);
            } else {
                String password = PASSWORD_PREFIX + info.getUserId();
                // 保存密码
                redisTemplate.opsForValue().set(password, info.getPassword());
                // 保存用户信息
                redisTemplate.opsForHash().put(account, USERNAME, info.getUserName());
                // 保存用户名与用户id对应关系，确保用户名唯一
                redisTemplate.opsForHash().put(USERNAME_MAP, info.getUserName(), info.getUserId().toString());

                infoRes = new RegisterInfoResVO(info.getUserId(), info.getUserName());
            }
        }


        return infoRes;
    }

    @Override
    public boolean login(LoginReqVO loginReqVO) throws Exception {
        //再去Redis里查询
        String key = ACCOUNT_PREFIX + loginReqVO.getUserId();
        String userName = redisTemplate.opsForHash().get(key, USERNAME).toString();

        boolean hasAccount = redisTemplate.hasKey(key);

        if (!hasAccount) {
            throw new CIMException(NOT_REGISTER);
        }

        if (null == userName) {
            return false;
        }

        if (!userName.equals(loginReqVO.getUserName())) {
            return false;
        }

        //登录成功，保存登录状态
        boolean status = userInfoCacheService.saveAndCheckUserLoginStatus(loginReqVO.getUserId());
        if (status == false) {
            //重复登录
            return false;
        }

        return true;
    }

    @Override
    public void saveRouteInfo(LoginReqVO loginReqVO, String msg) throws Exception {
        String key = ROUTE_PREFIX + loginReqVO.getUserId();
        redisTemplate.opsForValue().set(key, msg);
    }

    @Override
    public Map<Long, CIMServerResVO> loadRouteRelated() {

        Map<Long, CIMServerResVO> routes = new HashMap<>(64);

        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        ScanOptions options = ScanOptions.scanOptions()
                .match(ROUTE_PREFIX + "*")
                .build();
        Cursor<byte[]> scan = connection.scan(options);

        while (scan.hasNext()) {
            byte[] next = scan.next();
            String key = new String(next, StandardCharsets.UTF_8);
            LOGGER.info("key={}", key);
            parseServerInfo(routes, key);

        }

        return routes;
    }

    @Override
    public CIMServerResVO loadRouteRelatedByUserId(Long userId) {
        String value = redisTemplate.opsForValue().get(ROUTE_PREFIX + userId);

        if (value == null) {
            throw new CIMException(OFF_LINE);
        }

        String[] server = value.split(":");
        CIMServerResVO cimServerResVO = new CIMServerResVO(server[0], Integer.parseInt(server[1]), Integer.parseInt(server[2]));
        return cimServerResVO;
    }

    private void parseServerInfo(Map<Long, CIMServerResVO> routes, String key) {
        long userId = Long.valueOf(key.split(":")[1]);
        String value = redisTemplate.opsForValue().get(key);
        String[] server = value.split(":");
        CIMServerResVO cimServerResVO = new CIMServerResVO(server[0], Integer.parseInt(server[1]), Integer.parseInt(server[2]));
        routes.put(userId, cimServerResVO);
    }


    @Override
    public void pushMsg(String url, long sendUserId, ChatReqVO groupReqVO) throws Exception {
        CIMUserInfo cimUserInfo = userInfoCacheService.loadUserInfoByUserId(sendUserId);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("msg", cimUserInfo.getUserName() + ":【" + groupReqVO.getMsg() + "】");
        jsonObject.put("userId", groupReqVO.getUserId());
        RequestBody requestBody = RequestBody.create(mediaType, jsonObject.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
    }

    @Override
    public void offLine(Long userId) throws Exception {
        //删除路由
        redisTemplate.delete(ROUTE_PREFIX + userId);

        //删除登录状态
        userInfoCacheService.removeLoginStatus(userId);
    }
}
