package com.oscar.migration.util;

import java.lang.reflect.Field;

/**
 * @author zzg
 * @description:
 * @date 2021/1/15 14:33
 */
public class ObjectUtils {
    /** 将sourceObj的属性拷贝到targetObj
     * @param sourceObj 源对象
     * @param targetObj 目标对象
     * @param clazz 从哪一个类开始(比如sourceObj对象层级为:Object->User->ChineseUser->ChineseMan->ChineseChongQingMan)
     * 如果需要从ChineseUser开始复制，clazz就指定为ChineseUser.class
     */
    public static void cpoyObjAttr(Object sourceObj,Object targetObj, Class<?> clazz)throws Exception{
        if(sourceObj==null || targetObj==null){
            throw new Exception("源对象和目标对象不能为null");
        }
        Field[] fields=clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object sourceValue = field.get(sourceObj);
            field.set(targetObj, sourceValue);
        }
        if(clazz.getSuperclass()==Object.class){
            return;
        }
        cpoyObjAttr(sourceObj,targetObj,clazz.getSuperclass());

    }
}
