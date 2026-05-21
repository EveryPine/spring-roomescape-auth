package roomescape.domain.store.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.store.dto.request.StoreCreateRequestDto;
import roomescape.domain.store.dto.response.StoreResponseDto;
import roomescape.domain.store.service.StoreService;

@RestController
@RequestMapping("/api/admin/stores")
public class AdminStoreController {

    private final StoreService storeService;

    public AdminStoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
    public ResponseEntity<StoreResponseDto> saveStore(
        @RequestBody @Valid StoreCreateRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(storeService.saveStore(request));
    }
}
