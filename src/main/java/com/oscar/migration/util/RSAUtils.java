package com.oscar.migration.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: RAS加密工具类
 */
public class RSAUtils {

    public static final String RSA_ALGORITHM = "RSA";
    public static final int KEYSIZE = 1024;

    /**
     * 随机生成密钥对
     *
     * @param
     */
    public static Map<String, String> genKeyPair() {
        /** KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象*/
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        /** 初始化密钥对生成器，密钥大小为96-1024位*/
        assert keyPairGen != null;
        keyPairGen.initialize(KEYSIZE, new SecureRandom());
        /** 生成一个密钥对，保存在keyPair中*/
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
        String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())));
        Map<String, String> keyPairMap = new HashMap<String, String>();
        keyPairMap.put("publicKey", publicKeyString);
        keyPairMap.put("privateKey", privateKeyString);
        return keyPairMap;
    }

    /**
     * RSA公钥加密
     *
     * @param data      加密字符串
     * @param publicKey 公钥
     * @return 密文
     */
    public static String encrypt(String data, String publicKey) {
        /**base64编码的公钥*/
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = null;
        String outStr = null;
        try {
            pubKey = (RSAPublicKey) KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(new X509EncodedKeySpec(decoded));
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            outStr = Base64.encodeBase64String(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return outStr;
    }

    /**
     * RSA私钥解密
     *
     * @param data       加密字符串
     * @param privateKey 私钥
     * @return 铭文
     */
    public static String decrypt(String data, String privateKey) {
        /**64位解码加密后的字符串*/
        byte[] inputByte = Base64.decodeBase64(data.getBytes(StandardCharsets.UTF_8));
        /**base64编码的私钥*/
        byte[] decoded = Base64.decodeBase64(privateKey);
        RSAPrivateKey priKey = null;
        Cipher cipher = null;
        String outStr = null;
        try {
            priKey = (RSAPrivateKey) KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(decoded));
            cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            outStr = new String(cipher.doFinal(inputByte));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return outStr;
    }

}
