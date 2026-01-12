package vn.web.fashionshop.dto;

import lombok.Data;

@Data
public class GuestRoomRequest {
    private String sessionId;
    private String name;
    private String email;
}
