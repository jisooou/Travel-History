package com.project.travel.seeder;

import com.project.travel.place.entity.Place;
import com.project.travel.place.repository.PlaceRepository;
import com.project.travel.record.entity.Record;
import com.project.travel.record.entity.RecordDay;
import com.project.travel.record.entity.TimeSlot;
import com.project.travel.record.entity.TravelType;
import com.project.travel.record.repository.RecordDayRepository;
import com.project.travel.record.repository.RecordRepository;
import com.project.travel.schedule.entity.SchedulePlace;
import com.project.travel.schedule.repository.ScheduleRepository;
import com.project.travel.todo.entity.Todo;
import com.project.travel.todo.repository.TodoRepository;
import com.project.travel.user.entity.User;
import com.project.travel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RecordDetailSeederTest {
    private final UserRepository userRepository;
    private final RecordRepository recordRepository;
    private final RecordDayRepository recordDayRepository;
    private final ScheduleRepository scheduleRepository;
    private final PlaceRepository placeRepository;
    private final TodoRepository todoRepository;

    @Transactional
    public TestData seed(int cnt) {
//        User
        User user = User.builder()
                .email("test" + UUID.randomUUID() + "@gmail.com")
                .userName("test")
                .password("test-password")
                .build();
        userRepository.save(user);

        //        Record
        Record record = Record.builder()
                .owner(user)
                .recordName("test-record ")
                .travelType(TravelType.DOMESTIC)
                .build();
        recordRepository.save(record);

        //        RecordDay
        RecordDay recordDay = RecordDay.builder()
                .record(record)
                .travelDate(LocalDate.of(2026, 9, 1))
                .build();
        recordDayRepository.save(recordDay);

        for (int i = 1; i <= cnt; i++) {
            //        Place
            Place place = Place.builder()
                    .record(record)
                    .placeName("test-place " + i)
                    .placeAddress("test-address " + i)
                    .latitude(new BigDecimal("37.5665"))
                    .longitude(new BigDecimal("126.9780"))
                    .mapSource("test-mapSource")
                    .mapPlaceId("test-placeId " + i)
                    .imageUrl(null)
                    .build();
            placeRepository.save(place);

            //         SchedulePlace
            SchedulePlace schedulePlace = SchedulePlace.builder()
                    .day(recordDay)
                    .place(place)
                    .timeSlot(TimeSlot.MORNING)
                    .sortOrder(i)
                    .build();
            scheduleRepository.save(schedulePlace);
        }

        //        Todo
        Todo todo = Todo.builder()
                .day(recordDay)
                .writer(user)
                .todoContent("test-todo")
                .isCompleted(Todo.CompletedStatus.NOT_DONE)
                .build();
        todoRepository.save(todo);

        return new TestData(
                user.getUserNo(),
                record.getRecordNo()
        );
    }

    public record TestData(
            Integer userNo,
            Integer recordNo
    ) {
    }
}
