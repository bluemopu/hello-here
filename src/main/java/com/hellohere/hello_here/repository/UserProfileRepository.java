package com.hellohere.hello_here.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hellohere.hello_here.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

}
