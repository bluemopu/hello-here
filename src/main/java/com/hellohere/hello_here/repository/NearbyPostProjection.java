package com.hellohere.hello_here.repository;

import java.time.LocalDateTime;

public interface NearbyPostProjection {

    Long getId();

    String getContent();

    LocalDateTime getCreatedAt();

    String getDisplayName();

    String getAvatarUrl();

    Double getDistance();

    String getImageUrls();
}
