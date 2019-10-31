package com.hpy.dao;

import com.hpy.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectByUsername(String username);

    int checkByUsername(String username);

    int checkByEmail(String str);

    int checkAnswer(@Param("username") String username,
                    @Param("question") String question,
                    @Param("answer") String answer);

    int updatePasswordByUsername(@Param("password") String password,
                                 @Param("username") String username);

    int checkPasswordByUserId(@Param("password") String password,
                              @Param("userId") Integer userId);

    int updatePasswordByUserId(@Param("password") String password,
                               @Param("userId") Integer userId);

    int checkEmailByUserId(@Param("email") String email,
                           @Param("userId") Integer userId);
}