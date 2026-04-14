package com.demo.dreamshops.service.user;

import com.demo.dreamshops.dto.UserDto;
import com.demo.dreamshops.model.User;
import com.demo.dreamshops.request.CreateUserRequest;
import com.demo.dreamshops.request.UserUpdateRequest;

public interface IUserService {

    User getUserById(Long userId);
    User createUser(CreateUserRequest request);
    User updateUser(UserUpdateRequest request, Long userId);
    void deleteUser(Long userId);

    UserDto convertUserToDto(User user);

    User getAuthenticatedUser();
}
