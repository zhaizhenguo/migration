package com.oscar.migration.controller;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.oscar.migration.constants.ResponseCode;
import com.oscar.migration.constants.SysConstants;
import com.oscar.migration.entity.SysUser;
import com.oscar.migration.security.JwtAuthenticatioToken;
import com.oscar.migration.service.SysUserService;
import com.oscar.migration.util.*;
import com.oscar.migration.vo.LoginAuthInfo;
import com.oscar.migration.vo.LoginBean;
import com.oscar.migration.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zzg
 * @description: 登录控制器
 * @date 2020/12/23 10:43
 */
@RestController
public class SysLoginController {

    @Autowired
    private Producer producer;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private AuthenticationManager authenticationManager;

    private static String PUBLIC_KEY = null;
    private static String PRIVATE_KEY = null;


    @GetMapping("/captcha.jpg")
    public void captcha(HttpServletResponse response, HttpServletRequest request) throws ServletException, IOException {
        String ip = IPUtils.getIpAddr(request);
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
        /**生成文字验证码*/
        String text = producer.createText();
        /**生成图片验证码*/
        BufferedImage image = producer.createImage(text);
        /**保存到验证码到 session*/
        request.getSession().setAttribute(SysConstants.KAPTCHA_SESSION_KEY, text);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(image, "jpg", out);
        IOUtils.closeQuietly(out);
    }

    /**
     * @description: 获取公钥
     * @author zzg
     * @date 2020/12/28 17:42
     */
    @GetMapping("/getPublicKey")
    public Result getPublicKey() {
        if (StringUtils.isBlank(PUBLIC_KEY)) {
            Map<String, String> keyMap = RSAUtils.genKeyPair();
            PUBLIC_KEY = keyMap.get("publicKey");
            PRIVATE_KEY = keyMap.get("privateKey");
        }
        return Result.ok((Object) PUBLIC_KEY);
    }

    /**
     * @description: 登录接口
     * @author zzg
     * @date: 2020/12/29 11:29
     * @param: [loginBean, request]
     * @return: com.oscar.migration.vo.Result
     */
    @PostMapping(value = "/login")
    public Result login(@RequestBody LoginBean loginBean, HttpServletRequest request) throws IOException {
        String username = loginBean.getUserName();
        String password = loginBean.getPassword();
        String captcha = loginBean.getCaptcha();

        /**从session中获取之前保存的验证码跟前台传来的验证码进行匹配*/
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Result.error(ResponseCode.LOGIN_TIMEOUT_ERROR.code, ResponseCode.LOGIN_TIMEOUT_ERROR.msg);
        }
        String kaptcha = (String) session.getAttribute(Constants.KAPTCHA_SESSION_KEY);
        if (StringUtils.isBlank(kaptcha)) {
            return Result.error("验证码已失效");
        }
        if (!captcha.equalsIgnoreCase(kaptcha)) {
            return Result.error("验证码不正确");
        }
        /**用户信息*/
        SysUser user = sysUserService.findUserByUserName(username);
        if (user == null) {
            return Result.error("账号不存在");
        }
        String decodedPsd = RSAUtils.decrypt(password, PRIVATE_KEY);
        if (!PasswordUtils.matches(user.getSalt(), user.getPassword(), decodedPsd)) {
            return Result.error("密码不正确");
        }
        /**账号锁定*/
//        if (user.getToolUseNum() !=null && user.getToolUseNum() == 0) {
//            return Result.error("账号已被锁定,请联系管理员");
//        }
        /**查询用户权限（仅做展示）*/
        String roles = sysUserService.findUserRoleByUserId(user.getId());
        /**系统登录认证*/
        JwtAuthenticatioToken token = SecurityUtils.login(request, username, decodedPsd, authenticationManager);
        String formatDate = DateFormatUtils.format(user.getCreateTime(), "yyyy-MM-dd");
        LoginAuthInfo authInfo = new LoginAuthInfo();
        authInfo.setUserId(user.getId());
        authInfo.setUserName(user.getName());
        authInfo.setCreateTime(formatDate);
        authInfo.setUserRole(roles);
        authInfo.setToken(token.getToken());
        return Result.ok(authInfo);
    }

    public Result layout() {
        return Result.ok();
    }
}
