package app.dearobjet.backend.domain.shop.service;

import app.dearobjet.backend.domain.shop.dto.UpdateBusinessHoursRequest;
import app.dearobjet.backend.domain.shop.dto.ShopBusinessHoursResponse;

public interface ShopService {

    ShopBusinessHoursResponse updateBusinessHours(Long userId, UpdateBusinessHoursRequest request);

    ShopBusinessHoursResponse getBusinessHours(Long shopId);
}
