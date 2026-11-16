package com.hellohere.hello_here.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class LocationDto {

    @NotNull(message = "緯度を入力してください")
    @DecimalMin(value = "-90.0", message = "緯度は-90以上で入力してください")
    @DecimalMax(value = "90.0", message = "緯度は90以下で入力してください")
    private BigDecimal latitude;

    @NotNull(message = "経度を入力してください")
    @DecimalMin(value = "-180.0", message = "経度は-180以上で入力してください")
    @DecimalMax(value = "180.0", message = "経度は180以下で入力してください")
    private BigDecimal longitude;

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }
}
