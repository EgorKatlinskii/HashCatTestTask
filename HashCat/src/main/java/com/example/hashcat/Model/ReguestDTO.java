package com.example.hashcat.Model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;


@Data
public class ReguestDTO {

    @NotNull(message = "Email is missing")
    @Email
    private String email;

    @NotNull(message = "Hershey is missing")
    @Size(min = 1,message = "The minimum number of hashes is 1")
    private ArrayList<String> hashList;
}