package com.oscar.migration.service;

import com.oscar.migration.entity.SysUser;
import com.oscar.migration.vo.LoginPassword;
import com.oscar.migration.vo.PageRequest;
import com.oscar.migration.vo.PageResult;
import com.oscar.migration.vo.Result;

public interface SysUserService extends BaseCurdService<SysUser> {

    /**
     * @description: 根据用户名查询用户信息
     * @author zzg
     * @date: 2020/12/23 13:49
     * @param: [userName]
     * @return: com.oscar.migration.entity.SysUser
     */
    SysUser findUserByUserName(String userName);

    /**
     * @description: 根据用户ID查找用户的角色集合
     * @author zzg
     * @date: 2020/12/22 17:32
     * @param: [userId]
     * @return: com.oscar.migration.vo.Result
     */
    Result findRolesByUserId(Long userId);

    /** 
     * @description: 根据用户ID查询其权限
     * @author zzg 
     * @date: 2020/12/24 11:20
     * @param: [userId] 
     * @return: java.lang.String
     */
    String findUserRoleByUserId(Long userId);

    /**
     * @description: 修改密码
     * @author zzg
     * @date: 2020/12/22 17:32
     * @param: [loginPassword]
     * @return: com.oscar.migration.vo.Result
     */
    Result updatePassword(LoginPassword loginPassword);
}
