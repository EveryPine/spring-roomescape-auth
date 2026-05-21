package roomescape.domain.store.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.domain.store.dto.response.StoreResponseDto;
import roomescape.domain.store.service.StoreService;

@Repository
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    public ResponseEntity<List<StoreResponseDto>> getAllStores() {
        return ResponseEntity.status(HttpStatus.OK)
            .body(storeService.getStores());
    }
}
