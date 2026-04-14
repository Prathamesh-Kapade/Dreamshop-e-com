package com.demo.dreamshops.request;

import com.demo.dreamshops.model.User;
import com.demo.dreamshops.response.ApiResponse;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Data
public class CreateUserRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String password;


}
