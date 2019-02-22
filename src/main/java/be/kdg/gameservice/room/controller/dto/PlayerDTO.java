package be.kdg.gameservice.room.controller.dto;

import be.kdg.gameservice.round.model.HandType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDTO {
    private int id;
    private String userId;
    private boolean inRoom;
    private int chipCount;
    private HandType handType;
    private int seatNumber;
    private String access_token;
}
