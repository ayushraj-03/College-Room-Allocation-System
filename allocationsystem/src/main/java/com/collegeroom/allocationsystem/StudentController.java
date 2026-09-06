package com.collegeroom.allocationsystem;

import com.collegeroom.allocationsystem.model.Booking;
import com.collegeroom.allocationsystem.model.BookingStatus;
import com.collegeroom.allocationsystem.model.Room;
import com.collegeroom.allocationsystem.model.User;
import com.collegeroom.allocationsystem.repository.BookingRepository;
import com.collegeroom.allocationsystem.repository.RoomRepository;
import com.collegeroom.allocationsystem.repository.UserRepository;
import com.collegeroom.allocationsystem.service.BookingConflictService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
public class StudentController {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BookingConflictService bookingConflictService;

    public StudentController(
            RoomRepository roomRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository,
            BookingConflictService bookingConflictService) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.bookingConflictService = bookingConflictService;
    }

    @GetMapping("/student/dashboard")
    public String studentDashboard(
            Model model,
            Authentication authentication) {

        List<Room> rooms = roomRepository.findAll();
        model.addAttribute("rooms", rooms);

        User student = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<Booking> myBookings =
                bookingRepository.findByRequestedById(student.getId());

        long approvedCount = myBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .count();
        long pendingCount = myBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING || b.getStatus() == BookingStatus.HOD_APPROVED)
                .count();
        long rejectedCount = myBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                .count();

        model.addAttribute("myBookings", myBookings);
        model.addAttribute("student", student);
        model.addAttribute("totalCount", myBookings.size());
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("rejectedCount", rejectedCount);

        return "student-dashboard";
    }

    @PostMapping("/student/bookings")
    public String createBooking(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam String purpose,
            Authentication authentication) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        User student = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            return "redirect:/student/dashboard?error=invalid_time";
        }

        if (date.isBefore(LocalDate.now())) {
            return "redirect:/student/dashboard?error=past_date";
        }

        boolean conflict = bookingConflictService.hasConflict(
                roomId,
                date,
                startTime,
                endTime
        );

        if (conflict) {
            return "redirect:/student/dashboard?error=conflict";
        }

        Booking booking = new Booking();

        booking.setRoom(room);
        booking.setRequestedBy(student);
        booking.setDate(date);
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setPurpose(purpose);

        bookingRepository.save(booking);

        return "redirect:/student/dashboard?success=created";
    }
}