package roomescape.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.domain.managerstore.entity.ManagerStore;
import roomescape.domain.managerstore.repository.FakeManagerStoreRepository;
import roomescape.domain.managerstore.repository.ManagerStoreRepository;
import roomescape.domain.reservation.dto.request.ReservationCreateRequestDto;
import roomescape.domain.reservation.dto.request.ReservationUpdateRequestDto;
import roomescape.domain.reservation.dto.request.StaffReservationCreateRequestDto;
import roomescape.domain.reservation.dto.response.ReservationCreateResponseDto;
import roomescape.domain.reservation.dto.response.ReservationResponseDto;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.repository.FakeReservationRepository;
import roomescape.domain.store.dto.response.StoreResponseDto;
import roomescape.domain.store.entity.Store;
import roomescape.domain.store.repository.FakeStoreRepository;
import roomescape.domain.store.repository.StoreRepository;
import roomescape.domain.theme.dto.response.ThemeResponseDto;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.FakeThemeRepository;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.dto.response.TimeResponseDto;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.repository.FakeTimeRepository;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.global.auth.entity.Member;
import roomescape.global.auth.entity.Role;
import roomescape.global.auth.repository.FakeMemberRepository;
import roomescape.global.auth.repository.MemberRepository;
import roomescape.global.error.ErrorCode;
import roomescape.global.error.ErrorDetail;
import roomescape.global.error.exception.BusinessException;

class ReservationServiceTest {

    private final ReservationService reservationService;
    private final MemberRepository memberRepository;
    private final FakeReservationRepository reservationRepository;
    private final ManagerStoreRepository managerStoreRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final StoreRepository storeRepository;

    private Member manager;

    ReservationServiceTest() {
        this.memberRepository = new FakeMemberRepository();
        this.reservationRepository = new FakeReservationRepository();
        this.managerStoreRepository = new FakeManagerStoreRepository();
        this.timeRepository = new FakeTimeRepository();
        this.themeRepository = new FakeThemeRepository();
        this.storeRepository = new FakeStoreRepository();
        this.reservationService = new ReservationService(reservationRepository,
            managerStoreRepository, timeRepository,
            themeRepository, storeRepository);
    }

    @BeforeEach
    void setUp() {
        this.manager = memberRepository.save(Member.create("매니저", "manager", "1234", Role.MANAGER));
    }

    @Nested
    @DisplayName("getReservation 테스트")
    class GetReservationsTest {

        @Test
        @DisplayName("모든 예약을 조회한다.")
        void 성공() {
            // given
            LocalDate date = LocalDate.of(2026, 4, 30);
            Time time = Time.reconstruct(1L, LocalTime.of(10, 0));
            Theme theme = Theme.reconstruct(1L, "테마 이름", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png");
            Store store = Store.create("지점명").withId(1L);

            reservationRepository.save(
                Reservation.create(1L, date, time, theme,
                    store, LocalDateTime.of(2026, 1, 1, 0, 0)));
            reservationRepository.save(
                Reservation.create(2L, date.plusDays(1),
                    Time.reconstruct(2L, LocalTime.of(11, 0)), theme, store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));
            reservationRepository.save(
                Reservation.create(3L, date.plusDays(2),
                    Time.reconstruct(3L, LocalTime.of(12, 0)), theme, store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));

            // when
            List<ReservationResponseDto> actual = reservationService.getReservations();

            // then
            assertAll(
                () -> assertEquals(3, actual.size()),
                () -> assertEquals(
                    new ReservationResponseDto(1L, 1L, date, TimeResponseDto.from(time),
                        ThemeResponseDto.from(theme), StoreResponseDto.from(store)),
                    actual.get(0)),
                () -> assertEquals(
                    new ReservationResponseDto(2L, 2L, date.plusDays(1),
                        TimeResponseDto.from(Time.reconstruct(2L, LocalTime.of(11, 0))),
                        ThemeResponseDto.from(theme), StoreResponseDto.from(store)),
                    actual.get(1)
                ),
                () -> assertEquals(
                    new ReservationResponseDto(3L, 3L, date.plusDays(2),
                        TimeResponseDto.from(Time.reconstruct(3L, LocalTime.of(12, 0))),
                        ThemeResponseDto.from(theme), StoreResponseDto.from(store)),
                    actual.get(2)
                )
            );
        }
    }

    @Nested
    @DisplayName("getReservationByMemberId 테스트")
    class GetReservationByMemberIdTest {

        @Test
        @DisplayName("사용자의 모든 예약을 조회한다.")
        void 성공() {
            LocalDate date = LocalDate.of(2026, 4, 30);
            Time time = Time.reconstruct(1L, LocalTime.of(10, 0));
            Theme theme = Theme.reconstruct(1L, "테마 이름", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png");
            Store store = Store.create("강남점").withId(1L);
            Long memberId = 1L;

            reservationRepository.save(
                Reservation.create(1L, date, time, theme,
                    store, LocalDateTime.of(2026, 1, 1, 0, 0)));
            reservationRepository.save(
                Reservation.create(2L, date.plusDays(1),
                    Time.reconstruct(2L, LocalTime.of(11, 0)), theme,
                    store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));
            reservationRepository.save(
                Reservation.create(3L, date.plusDays(2),
                    Time.reconstruct(3L, LocalTime.of(12, 0)), theme,
                    store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));

            List<ReservationResponseDto> actual = reservationService.getReservationsByMemberId(
                memberId);

            assertAll(
                () -> assertEquals(1, actual.size()),
                () -> assertEquals(
                    new ReservationResponseDto(1L, memberId, date, TimeResponseDto.from(time),
                        ThemeResponseDto.from(theme), new StoreResponseDto(1L, "강남점")),
                    actual.get(0))
            );
        }
    }

    @Nested
    @DisplayName("getReservationsByManagerId 테스트")
    class GetReservationsByManagerIdTest {

        @Test
        @DisplayName("매니저가 담당하는 지점의 예약만 조회한다.")
        void 성공() {
            LocalDate date = LocalDate.of(2026, 4, 30);
            Time time = Time.reconstruct(1L, LocalTime.of(10, 0));
            Theme theme = Theme.reconstruct(1L, "테마 이름", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png");
            Store store1 = Store.create("강남점").withId(1L);
            Store store2 = Store.create("잠실점").withId(2L);
            Long managerId = manager.getId();
            reservationRepository.assignStoreToManager(managerId, store1.getId());
            managerStoreRepository.save(ManagerStore.create(managerId, store1.getId()));

            reservationRepository.save(
                Reservation.create(1L, date, time, theme, store1,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));
            reservationRepository.save(
                Reservation.create(2L, date.plusDays(1),
                    Time.reconstruct(2L, LocalTime.of(11, 0)), theme, store2,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));

            List<ReservationResponseDto> actual = reservationService.getReservationsByManagerId(
                managerId);

            assertAll(
                () -> assertEquals(1, actual.size()),
                () -> assertEquals(
                    new ReservationResponseDto(1L, 1L, date, TimeResponseDto.from(time),
                        ThemeResponseDto.from(theme), StoreResponseDto.from(store1)),
                    actual.get(0))
            );
        }
    }

    @Nested
    @DisplayName("saveReservation 테스트")
    class SaveReservationTest {

        @Test
        @DisplayName("예약을 생성하고, 생성된 예약을 반환한다.")
        void 성공() {
            // given
            Long managerId = manager.getId();
            Long memberId = 1L;
            ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                LocalDate.of(2026, 5, 1),
                1L,
                1L,
                1L
            );

            themeRepository.save(Theme.create("테마명", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png"));
            timeRepository.save(Time.create(LocalTime.of(15, 30)));

            // when
            ReservationCreateResponseDto actual = reservationService.saveReservation(managerId,
                request,
                LocalDateTime.of(2026, 1, 1, 0, 0));

            // then
            assertAll(
                () -> assertEquals(1L, actual.id()),
                () -> assertEquals(memberId, actual.memberId()),
                () -> assertEquals(LocalDate.of(2026, 5, 1), actual.date()),
                () -> assertEquals(1L, actual.timeId()),
                () -> assertEquals(1L, actual.themeId()),
                () -> assertEquals(1L, actual.storeId()),
                () -> assertEquals(1, reservationRepository.findAllReservations().size())
            );
        }

        @Test
        @DisplayName("날짜, 시간과 테마가 모두 같은 예약이 존재하는 경우 예외가 발생한다.")
        void 실패1() {
            Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
            Theme theme = themeRepository.save(Theme.create("테마명", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png"));
            Store store = storeRepository.save(Store.create("지점명"));
            Long memberId = 1L;
            ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                LocalDate.of(2026, 5, 1),
                time.getId(),
                theme.getId(),
                store.getId()
            );
            reservationRepository.save(
                Reservation.create(memberId, request.date(), time, theme, store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));

            assertThatThrownBy(() -> reservationService.saveReservation(memberId, request,
                LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_DUPLICATE);
        }

        @Test
        @DisplayName("요청한 시간 id가 존재하지 않으면 예외가 발생한다.")
        void 실패2() {
            Theme theme = themeRepository.save(Theme.create("테마명", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png"));
            Long wrongTimeId = 99999L;
            ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                LocalDate.of(2026, 5, 1),
                wrongTimeId,
                theme.getId(),
                1L
            );

            assertThatThrownBy(() -> reservationService.saveReservation(1L, request,
                LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMON_INVALID_REQUEST_BODY);
        }

        @Test
        @DisplayName("요청한 테마 id가 존재하지 않으면 예외가 발생한다.")
        void 실패3() {
            Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
            Long wrongThemeId = 99999L;
            ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                LocalDate.of(2026, 5, 1),
                time.getId(),
                wrongThemeId,
                1L
            );

            assertThatThrownBy(() -> reservationService.saveReservation(1L, request,
                LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMON_INVALID_REQUEST_BODY);
        }

        @Test
        @DisplayName("지난 날짜와 시간으로 예약을 생성하려고 하면 예외가 발생한다.")
        void 실패4() {
            Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
            Theme theme = themeRepository.save(Theme.create("테마명", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png"));
            Store store = storeRepository.save(Store.create("지점명"));
            ReservationCreateRequestDto request = new ReservationCreateRequestDto(
                LocalDate.of(2025, 12, 31),
                time.getId(),
                theme.getId(),
                store.getId()
            );

            assertThatThrownBy(() -> reservationService.saveReservation(1L, request,
                LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_ALREADY_PASSED);
        }
    }

    @Nested
    @DisplayName("saveManagerReservation 테스트")
    class SaveManagerReservationTest {

        @Test
        @DisplayName("예약을 생성하고, 생성된 예약을 반환한다.")
        void 성공() {
            Long memberId = 1L;
            Long managerId = manager.getId();
            Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
            Theme theme = themeRepository.save(Theme.create("테마명", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png"));
            Store store = storeRepository.save(Store.create("지점명"));
            StaffReservationCreateRequestDto request = new StaffReservationCreateRequestDto(
                LocalDate.of(2026, 5, 1),
                memberId,
                time.getId(),
                theme.getId(),
                store.getId()
            );
            managerStoreRepository.save(ManagerStore.create(managerId, store.getId()));
            LocalDateTime now = LocalDateTime.of(2026, 1, 1, 0, 0);

            ReservationCreateResponseDto actual = reservationService.saveManagerReservation(
                managerId, request, now);

            assertAll(
                () -> assertEquals(1L, actual.id()),
                () -> assertEquals(memberId, actual.memberId()),
                () -> assertEquals(LocalDate.of(2026, 5, 1), actual.date()),
                () -> assertEquals(time.getId(), actual.timeId()),
                () -> assertEquals(theme.getId(), actual.themeId()),
                () -> assertEquals(store.getId(), actual.storeId()),
                () -> assertEquals(1, reservationRepository.findAllReservations().size())
            );
        }

        @Test
        @DisplayName("요청한 시간 id가 존재하지 않으면 예외가 발생한다.")
        void 실패1() {
            Long managerId = manager.getId();
            Theme theme = themeRepository.save(Theme.create("테마명", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png"));
            Store store = storeRepository.save(Store.create("지점명"));
            StaffReservationCreateRequestDto request = new StaffReservationCreateRequestDto(
                LocalDate.of(2026, 5, 1),
                1L,
                99999L,
                theme.getId(),
                store.getId()
            );

            assertThatThrownBy(() -> reservationService.saveManagerReservation(managerId, request,
                LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMON_INVALID_REQUEST_BODY);
        }

        @Test
        @DisplayName("요청한 테마 id가 존재하지 않으면 예외가 발생한다.")
        void 실패2() {
            Long managerId = manager.getId();
            Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
            Store store = storeRepository.save(Store.create("지점명"));
            StaffReservationCreateRequestDto request = new StaffReservationCreateRequestDto(
                LocalDate.of(2026, 5, 1),
                1L,
                time.getId(),
                99999L,
                store.getId()
            );

            assertThatThrownBy(() -> reservationService.saveManagerReservation(managerId, request,
                LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMON_INVALID_REQUEST_BODY);
        }

        @Test
        @DisplayName("요청한 지점 id가 존재하지 않으면 예외가 발생한다.")
        void 실패3() {
            Long managerId = manager.getId();
            Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
            Theme theme = themeRepository.save(Theme.create("테마명", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png"));
            StaffReservationCreateRequestDto request = new StaffReservationCreateRequestDto(
                LocalDate.of(2026, 5, 1),
                1L,
                time.getId(),
                theme.getId(),
                99999L
            );

            assertThatThrownBy(() -> reservationService.saveManagerReservation(managerId, request,
                LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMMON_INVALID_REQUEST_BODY);
        }

        @Test
        @DisplayName("다른 매장의 예약 생성을 시도하면 예외가 발생한다.")
        void 실패4() {
            Long managerId = manager.getId();
            Long memberId = 1L;
            Member otherManager = memberRepository.save(
                Member.create("매니저2", "manager2", "1234", Role.MANAGER));
            Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
            Theme theme = themeRepository.save(Theme.create("테마명", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png"));
            Store store = storeRepository.save(Store.create("지점명"));
            managerStoreRepository.save(ManagerStore.create(managerId, store.getId()));
            StaffReservationCreateRequestDto request = new StaffReservationCreateRequestDto(
                LocalDate.of(2026, 5, 1),
                memberId,
                time.getId(),
                theme.getId(),
                store.getId()
            );

            assertThatThrownBy(
                () -> reservationService.saveManagerReservation(otherManager.getId(), request,
                    LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_FORBIDDEN);
        }

        @Test
        @DisplayName("같은 지점, 날짜, 시간과 테마가 모두 같은 예약이 존재하는 경우 예외가 발생한다.")
        void 실패5() {
            Long memberId = 1L;
            Long managerId = manager.getId();
            Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
            Theme theme = themeRepository.save(Theme.create("테마명", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png"));
            Store store = storeRepository.save(Store.create("지점명"));
            StaffReservationCreateRequestDto request = new StaffReservationCreateRequestDto(
                LocalDate.of(2026, 5, 1),
                memberId,
                time.getId(),
                theme.getId(),
                store.getId()
            );
            reservationRepository.save(Reservation.create(memberId, request.date(), time, theme,
                store, LocalDateTime.of(2026, 1, 1, 0, 0)));
            managerStoreRepository.save(ManagerStore.create(managerId, store.getId()));

            assertThatThrownBy(() -> reservationService.saveManagerReservation(managerId, request,
                LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_DUPLICATE);
        }

        @Test
        @DisplayName("지난 날짜와 시간으로 예약을 생성하려고 하면 예외가 발생한다.")
        void 실패6() {
            Long managerId = manager.getId();
            Time time = timeRepository.save(Time.create(LocalTime.of(15, 30)));
            Theme theme = themeRepository.save(Theme.create("테마명", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png"));
            Store store = storeRepository.save(Store.create("지점명"));
            StaffReservationCreateRequestDto request = new StaffReservationCreateRequestDto(
                LocalDate.of(2025, 12, 31),
                1L,
                time.getId(),
                theme.getId(),
                store.getId()
            );

            assertThatThrownBy(() -> reservationService.saveManagerReservation(managerId, request,
                LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_ALREADY_PASSED);
        }
    }

    @Nested
    @DisplayName("updateReservation 테스트")
    class UpdateReservationTest {

        @Test
        @DisplayName("주어진 예약의 날짜와 시간을 변경한다.")
        void 성공1() {
            Long memberId = 1L;
            Reservation savedReservation = reservationRepository.save(
                Reservation.create(memberId, LocalDate.of(2026, 5, 3),
                    Time.reconstruct(2L, LocalTime.of(13, 0)),
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    Store.create("강남점").withId(1L),
                    LocalDateTime.of(2026, 1, 1, 0, 0)));
            Long id = savedReservation.getId();
            LocalDate changeDate = LocalDate.of(2026, 5, 2);
            Long changeTimeId = timeRepository.save(Time.create(LocalTime.of(20, 30))).getId();
            ReservationUpdateRequestDto request = new ReservationUpdateRequestDto(
                changeDate, changeTimeId);

            reservationService.updateReservation(memberId, id, request,
                LocalDateTime.of(2026, 1, 1, 0, 0));
            Optional<Reservation> updatedReservation = reservationRepository.findReservationById(
                id);

            assertThat(updatedReservation).isPresent();
            assertAll(
                () -> assertThat(updatedReservation.get().getDate()).isEqualTo(changeDate),
                () -> assertThat(updatedReservation.get().getTime().getId()).isEqualTo(changeTimeId)
            );
        }

        @Test
        @DisplayName("기존 날짜와 시간 그대로 변경 요청하면 자기 자신을 중복 예약으로 보지 않는다.")
        void 성공2() {
            Long memberId = 1L;
            Time time = timeRepository.save(Time.create(LocalTime.of(13, 0)));
            Reservation savedReservation = reservationRepository.save(
                Reservation.create(memberId, LocalDate.of(2026, 5, 3), time,
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    Store.create("강남점").withId(1L),
                    LocalDateTime.of(2026, 1, 1, 0, 0)));
            ReservationUpdateRequestDto request = new ReservationUpdateRequestDto(
                savedReservation.getDate(), time.getId());

            reservationService.updateReservation(memberId, savedReservation.getId(), request,
                LocalDateTime.of(2026, 1, 1, 0, 0));

            Optional<Reservation> actual = reservationRepository.findReservationById(
                savedReservation.getId());

            assertAll(
                () -> assertThat(actual).isPresent(),
                () -> assertThat(actual.get().getDate()).isEqualTo(savedReservation.getDate()),
                () -> assertThat(actual.get().getTime().getId()).isEqualTo(time.getId())
            );
        }

        @Test
        @DisplayName("변경하려는 날짜와 시간이 미래이면 예약을 변경한다.")
        void 성공3() {
            Long memberId = 1L;
            Reservation savedReservation = reservationRepository.save(
                Reservation.create(memberId, LocalDate.of(2026, 1, 1),
                    Time.reconstruct(1L, LocalTime.of(13, 0)),
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    Store.create("강남점").withId(1L),
                    LocalDateTime.of(2025, 12, 31, 0, 0)));
            Long changeTimeId = timeRepository.save(Time.create(LocalTime.of(13, 0))).getId();
            ReservationUpdateRequestDto request = new ReservationUpdateRequestDto(
                LocalDate.of(2026, 1, 1), changeTimeId);

            reservationService.updateReservation(memberId, savedReservation.getId(), request,
                LocalDateTime.of(2026, 1, 1, 10, 0));

            Optional<Reservation> actual = reservationRepository.findReservationById(
                savedReservation.getId());
            assertAll(
                () -> assertThat(actual).isPresent(),
                () -> assertThat(actual.get().getDate()).isEqualTo(request.date()),
                () -> assertThat(actual.get().getTime().getId()).isEqualTo(changeTimeId)
            );
        }

        @Test
        @DisplayName("요청한 시간 id가 존재하지 않으면 예외가 발생한다.")
        void 실패1() {
            Long memberId = 1L;
            Reservation savedReservation = reservationRepository.save(
                Reservation.create(memberId, LocalDate.of(2026, 5, 3),
                    Time.reconstruct(2L, LocalTime.of(13, 0)),
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    Store.create("강남점").withId(1L),
                    LocalDateTime.of(2026, 1, 1, 0, 0)));
            Long id = savedReservation.getId();
            LocalDate changeDate = LocalDate.of(2026, 5, 2);
            Long wrongId = 99999L;
            ReservationUpdateRequestDto request = new ReservationUpdateRequestDto(
                changeDate, wrongId);
            ErrorDetail expectedErrors = new ErrorDetail("timeId", wrongId.toString(),
                "요청한 시간 id가 존재하지 않습니다.");

            assertThatThrownBy(() -> reservationService.updateReservation(memberId, id, request,
                LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOfSatisfying(BusinessException.class, exception -> assertAll(
                    () -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.COMMON_INVALID_REQUEST_BODY),
                    () -> assertThat(exception.getError()).isEqualTo(expectedErrors)
                ));
        }

        @Test
        @DisplayName("요청한 예약에 권한이 없는 경우 예외가 발생한다.")
        void 실패2() {
            Reservation savedReservation = reservationRepository.save(
                Reservation.create(1L, LocalDate.of(2026, 5, 3),
                    Time.reconstruct(2L, LocalTime.of(13, 0)),
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    Store.create("강남점").withId(1L),
                    LocalDateTime.of(2026, 1, 1, 0, 0)));
            Long wrongMemberId = 2L;
            Long id = savedReservation.getId();
            LocalDate changeDate = LocalDate.of(2026, 5, 2);
            Long wrongId = 99999L;
            ReservationUpdateRequestDto request = new ReservationUpdateRequestDto(
                changeDate, wrongId);

            assertThatThrownBy(
                () -> reservationService.updateReservation(wrongMemberId, id, request,
                    LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_FORBIDDEN);
        }

        @Test
        @DisplayName("변경하려는 날짜와 시간에 이미 예약이 있으면 예외가 발생한다.")
        void 실패3() {
            Long memberId = 1L;
            Theme theme = Theme.reconstruct(1L, "테마 이름", "테마 설명",
                "https://roomescape.com/images/themes/ring-banner.png");
            Time originalTime = timeRepository.save(Time.create(LocalTime.of(13, 0)));
            Time duplicatedTime = timeRepository.save(Time.create(LocalTime.of(20, 30)));
            Store store = storeRepository.save(Store.create("지점명"));
            Reservation savedReservation = reservationRepository.save(
                Reservation.create(memberId, LocalDate.of(2026, 5, 3), originalTime, theme,
                    store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));
            reservationRepository.save(
                Reservation.create(2L, LocalDate.of(2026, 5, 4), duplicatedTime, theme,
                    store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));
            Long id = savedReservation.getId();
            ReservationUpdateRequestDto request = new ReservationUpdateRequestDto(
                LocalDate.of(2026, 5, 4), duplicatedTime.getId());

            assertThatThrownBy(() -> reservationService.updateReservation(memberId, id, request,
                LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_DUPLICATE);
        }

        @Test
        @DisplayName("지난 예약을 변경하려고 하면 예외가 발생한다.")
        void 실패4() {
            Long memberId = 1L;
            Store store = storeRepository.save(Store.create("지점명"));
            Reservation savedReservation = reservationRepository.save(
                Reservation.create(memberId, LocalDate.of(2025, 12, 31),
                    Time.reconstruct(2L, LocalTime.of(13, 0)),
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    store,
                    LocalDateTime.MIN).withId(1L));
            Long id = savedReservation.getId();
            LocalDate changeDate = LocalDate.of(2026, 5, 2);
            Long changeTimeId = timeRepository.save(Time.create(LocalTime.of(20, 30))).getId();
            ReservationUpdateRequestDto request = new ReservationUpdateRequestDto(
                changeDate, changeTimeId);

            assertThatThrownBy(() -> reservationService.updateReservation(memberId, id, request,
                LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_ALREADY_PASSED);
        }

        @Test
        @DisplayName("지난 시점으로 변경하려고 하면 예외가 발생한다.")
        void 실패5() {
            Long memberId = 1L;
            Store store = storeRepository.save(Store.create("지점명"));
            Reservation savedReservation = reservationRepository.save(
                Reservation.create(memberId, LocalDate.of(2026, 5, 3),
                    Time.reconstruct(2L, LocalTime.of(13, 0)),
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));
            Long id = savedReservation.getId();
            LocalDate changeDate = LocalDate.of(2025, 12, 31);
            Long changeTimeId = timeRepository.save(Time.create(LocalTime.of(20, 30))).getId();
            ReservationUpdateRequestDto request = new ReservationUpdateRequestDto(
                changeDate, changeTimeId);

            assertThatThrownBy(() -> reservationService.updateReservation(memberId, id, request,
                LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_TIME_ALREADY_PASSED);
        }

        @Test
        @DisplayName("오늘의 지난 시간으로 변경하려고 하면 예외가 발생한다.")
        void 실패6() {
            Long memberId = 1L;
            Store store = storeRepository.save(Store.create("지점명"));
            Reservation savedReservation = reservationRepository.save(
                Reservation.create(memberId, LocalDate.of(2026, 1, 2),
                    Time.reconstruct(2L, LocalTime.of(13, 0)),
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));
            Long pastTimeId = timeRepository.save(Time.create(LocalTime.of(9, 0))).getId();
            ReservationUpdateRequestDto request = new ReservationUpdateRequestDto(
                LocalDate.of(2026, 1, 1), pastTimeId);

            assertThatThrownBy(() -> reservationService.updateReservation(memberId,
                savedReservation.getId(), request, LocalDateTime.of(2026, 1, 1, 10, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_TIME_ALREADY_PASSED);
        }

        @Test
        @DisplayName("존재하지 않는 예약을 변경하려고 하면 예외가 발생한다.")
        void 실패7() {
            Long notFoundId = 99999L;
            Long timeId = timeRepository.save(Time.create(LocalTime.of(20, 30))).getId();
            ReservationUpdateRequestDto request = new ReservationUpdateRequestDto(
                LocalDate.of(2026, 5, 2), timeId);

            assertThatThrownBy(
                () -> reservationService.updateReservation(1L, notFoundId, request,
                    LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);
        }

    }

    @Nested
    @DisplayName("deleteReservationById 테스트")
    class DeleteReservationByIdTest {

        @Test
        @DisplayName("주어진 아이디를 가진 예약을 삭제한다.")
        void 성공() {
            // given
            Store store = storeRepository.save(Store.create("지점명"));
            Reservation savedReservation = reservationRepository.save(
                Reservation.create(1L, LocalDate.of(2026, 5, 2),
                    Time.reconstruct(1L, LocalTime.of(12, 0)),
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));
            reservationRepository.save(
                Reservation.create(2L, LocalDate.of(2026, 5, 3),
                    Time.reconstruct(2L, LocalTime.of(13, 0)),
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));

            // when
            reservationService.deleteReservationById(savedReservation.getId());

            // then
            List<ReservationResponseDto> actual = reservationService.getReservations();
            assertAll(
                () -> assertEquals(1, actual.size()),
                () -> assertEquals(LocalDate.of(2026, 5, 3), actual.getFirst().date())
            );

        }
    }

    @Nested
    @DisplayName("deleteMemberReservationById 테스트")
    class DeleteMemberReservationByIdTest {

        @Test
        @DisplayName("본인의 예약을 삭제한다.")
        void 성공() {
            Long memberId = 1L;
            Store store = storeRepository.save(Store.create("지점명"));
            Reservation savedReservation = reservationRepository.save(
                Reservation.create(memberId, LocalDate.of(2026, 5, 3),
                    Time.reconstruct(1L, LocalTime.of(13, 0)),
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));
            reservationRepository.save(
                Reservation.create(2L, LocalDate.of(2026, 5, 4),
                    Time.reconstruct(2L, LocalTime.of(14, 0)),
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));

            reservationService.deleteMemberReservationById(memberId, savedReservation.getId(),
                LocalDateTime.of(2026, 1, 1, 0, 0));

            List<ReservationResponseDto> actual = reservationService.getReservations();
            assertAll(
                () -> assertEquals(1, actual.size()),
                () -> assertEquals(LocalDate.of(2026, 5, 4), actual.getFirst().date())
            );
        }

        @Test
        @DisplayName("다른 사용자의 예약을 삭제하려고 하면 예외가 발생한다.")
        void 실패1() {
            Store store = storeRepository.save(Store.create("지점명"));
            Reservation savedReservation = reservationRepository.save(
                Reservation.create(1L, LocalDate.of(2026, 5, 3),
                    Time.reconstruct(1L, LocalTime.of(13, 0)),
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    store,
                    LocalDateTime.of(2026, 1, 1, 0, 0)));

            assertThatThrownBy(() -> reservationService.deleteMemberReservationById(2L,
                savedReservation.getId(), LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_FORBIDDEN);
        }

        @Test
        @DisplayName("존재하지 않는 예약을 삭제하려고 하면 예외가 발생한다.")
        void 실패2() {
            Long notFoundId = 99999L;

            assertThatThrownBy(
                () -> reservationService.deleteMemberReservationById(1L, notFoundId,
                    LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_NOT_FOUND);
        }

        @Test
        @DisplayName("지난 예약을 삭제하려고 하면 예외가 발생한다.")
        void 실패3() {
            Long memberId = 1L;
            Store store = storeRepository.save(Store.create("지점명"));
            Reservation savedReservation = reservationRepository.save(
                Reservation.create(memberId, LocalDate.of(2025, 12, 31),
                    Time.reconstruct(1L, LocalTime.of(13, 0)),
                    Theme.reconstruct(1L, "테마 이름", "테마 설명",
                        "https://roomescape.com/images/themes/ring-banner.png"),
                    store,
                    LocalDateTime.MIN).withId(1L));

            assertThatThrownBy(() -> reservationService.deleteMemberReservationById(memberId,
                savedReservation.getId(), LocalDateTime.of(2026, 1, 1, 0, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESERVATION_ALREADY_PASSED);
        }
    }
}
