package com.oscar.migration.security;

import com.oscar.migration.entity.SysUser;
import com.oscar.migration.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


/**
 * 用户登录认证信息查询
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.findUserByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("该用户不存在");
        }
        return new JwtUserDetails(user.getName(), user.getPassword(), user.getSalt());
    }
}