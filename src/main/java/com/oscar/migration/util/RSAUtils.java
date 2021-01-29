package com.oscar.migration.util;

import com.oscar.migration.constants.SysConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
@Slf4j
public class RSAUtils {

    public static final String RSA_ALGORITHM = "RSA";
    /** RSA密钥长度必须是64的倍数，在512~65536之间。默认是1024
     * 数值越大生成密钥对耗时越长
     */
    public static final int KEYSIZE = 1024;
    public static final Map<String, String> keyPairMap = new HashMap<String, String>(2);

    /**
     * 随机生成密钥对
     *
     * @param
     */
    public static synchronized Map<String, String> genKeyPair() {
        /** KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象*/
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            log.error("RSAUtils NoSuchAlgorithmException",e);
            return keyPairMap;
        }

        assert keyPairGen != null;
        keyPairGen.initialize(KEYSIZE, new SecureRandom());
        /** 生成一个密钥对，保存在keyPair中*/
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()));
        String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())));
        keyPairMap.clear();
        keyPairMap.put("publicKey", publicKeyString);
        keyPairMap.put("privateKey", privateKeyString);
        return keyPairMap;
    }

//    public static void main(String[] args) {
//        Map<String, String> keyMap = RSAUtils.genKeyPair();
//        String publicKey = keyMap.get("publicKey");
//        String privateKey = keyMap.get("privateKey");
//        String encrypt = encrypt(SysConstants.PUBLICKEY, publicKey);
////        String encrypt = encrypt("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC4N6Y+7f3WPpDGM", publicKey);
//        String decrypt = decrypt(encrypt, privateKey);
//        System.out.println(publicKey);
//        System.out.println(privateKey);
//        System.out.println(encrypt);
//        System.out.println(SysConstants.PUBLICKEY);
//        System.out.println(decrypt);
//    }
    /**
     * RSA公钥加密
     *
     * @param data      加密字符串
     * @param publicKey 公钥
     * @return 密文
     */
    public static synchronized String encrypt(String data, String publicKey) {
        if (StringUtils.isBlank(data) || StringUtils.isBlank(publicKey)){
            log.error("RSA公钥加密参数为空");
            return null;
        }
        /**base64编码的公钥*/
        byte[] decoded = Base64.decodeBase64(publicKey);
        RSAPublicKey pubKey = null;
        String outStr = null;
        try {
            pubKey = (RSAPublicKey) KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(new X509EncodedKeySpec(decoded));
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            /**分段加密*/
            outStr = Base64.encodeBase64String(cipherDoFinal(cipher,dataBytes,(KEYSIZE/8-11)));
        } catch (Exception e) {
            log.error("RSAUtils Exception",e);
        }
        return outStr;
    }

    /**
     * 分段大小
     *
     * @param cipher
     * @param srcBytes
     * @param segmentSize
     * @return
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws IOException
     */
    public static byte[] cipherDoFinal(Cipher cipher, byte[] srcBytes, int segmentSize)
            throws IllegalBlockSizeException, BadPaddingException, IOException {
        if (segmentSize <= 0){
            log.error("",new RuntimeException("分段大小必须大于0"));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int inputLen = srcBytes.length;
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > segmentSize) {
                cache = cipher.doFinal(srcBytes, offSet, segmentSize);
            } else {
                cache = cipher.doFinal(srcBytes, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * segmentSize;
        }
        byte[] data = out.toByteArray();
        out.close();
        return data;
    }


    /**
     * RSA私钥解密
     *
     * @param data       加密字符串
     * @param privateKey 私钥
     * @return 铭文
     */
    public static synchronized String decrypt(String data, String privateKey) {
        if (StringUtils.isBlank(data) || StringUtils.isBlank(privateKey)){
            log.error("RSA私钥解密参数为空");
            return null;
        }
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
            /**分段解密*/
            outStr = new String(cipherDoFinal(cipher, inputByte, KEYSIZE/8));
        } catch (Exception e) {
            log.error("RSAUtils Exception",e);
        }
        return outStr;
    }

}
