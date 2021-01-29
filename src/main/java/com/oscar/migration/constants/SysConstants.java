package com.oscar.migration.constants;

import com.oscar.migration.entity.UserResource;

import java.util.Hashtable;
import java.util.Map;

/**
 * @description: 常量接口 不要实现此接口！
 * @author zzg
 * @date 2020/12/21 5:12
 */
public interface SysConstants {

	/**系统管理员用户名*/
	String ADMIN = "admin";

	/** 系统管理员名称*/
	String ADMIN_NAME = "管理员";

	/**验证码session_key*/
	String KAPTCHA_SESSION_KEY = "KAPTCHA_SESSION_KEY";

	/**加密固定公钥*/
	String PUBLICKEY= "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCrkFWszWrMAnHUjOWTCJaQZdoiePC+AQqsC0lo5h5TEER52moyTSs7WnATuGkMA69TK76yV1zC5BtMtjGE6dA+d420ml9/4oUnsNO+12V42Jz1VwqKy0tMIN6h+L+RsFVqLrR22n4JV+5Zc5wBYLtl1IPZOChLLgGgXUoi2h/0VwIDAQAB";

	/**用户资源池*/
	Map<String , UserResource> UserResourceMap = new Hashtable<>(10);
}
