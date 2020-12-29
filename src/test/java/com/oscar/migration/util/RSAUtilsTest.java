package com.oscar.migration.util;

import org.junit.jupiter.api.Test;

import java.util.Map;


/**
 * @author zzg
 * @date 2020/12/29 11:22
 */
class RSAUtilsTest {

    @Test
    void test() {
        //生成公钥和私钥
        Map<String, String> map = RSAUtils.genKeyPair();
        //加密字符串
        String message = "df723820";
        System.out.println("随机生成的公钥为:" + map.get("publicKey"));
        System.out.println("随机生成的私钥为:" + map.get("privateKey"));
        String messageEn = RSAUtils.encrypt(message, map.get("publicKey"));
        System.out.println("加密后的字符串为:" + messageEn);
        String messageDe = RSAUtils.decrypt(messageEn, map.get("privateKey"));
        System.out.println("还原后的字符串为:" + messageDe);
    }
}