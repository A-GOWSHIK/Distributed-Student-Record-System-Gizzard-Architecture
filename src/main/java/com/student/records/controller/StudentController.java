package com.student.records.controller;

import com.student.records.domain.Student;
import com.student.records.repository.StudentRepository;
import com.student.records.services.StudentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.student.records.services.SystemLogService;
import com.student.records.services.AttendanceService;
import com.student.records.services.GradesService;
import com.student.records.distributed.ShardRouter;
import com.student.records.distributed.ShardForwardService;
import com.student.records.distributed.ReplicationService;
import com.student.records.distributed.LogicalClock;
import com.student.records.distributed.DistributedLock;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentRepository studentRepository;
    private final StudentServiceImpl studentService;
    private final SystemLogService systemLogService;
    private final AttendanceService attendanceService;
    private final GradesService gradesService;
    private final com.student.records.repository.EnrollmentRepository enrollmentRepository;

    @Autowired
    private ShardRouter shardRouter;

    @Autowired
    private ShardForwardService shardForwardService;

    @Autowired
    private ReplicationService replicationService;

    @Autowired
    private LogicalClock logicalClock;

    @Autowired
    private DistributedLock distributedLock;

    @Value("${server.port}")
    private String serverPort;

    public StudentController(StudentRepository studentRepository,
            StudentServiceImpl studentService,
            SystemLogService systemLogService,
            AttendanceService attendanceService,
            GradesService gradesService,
            com.student.records.repository.EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.studentService = studentService;
        this.systemLogService = systemLogService;
        this.attendanceService = attendanceService;
        this.gradesService = gradesService;
        this.enrollmentRepository = enrollmentRepository;
    }

    private String getRole(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return "";
    }

    // GET /students — Paginated list or all
    @GetMapping
    public ResponseEntity<?> getAllStudents(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String role = getRole(authHeader);
        if (role.equals("STUDENT")) {
            return ResponseEntity.status(403).build();
        }

        if (page != null && size != null) {
            Page<Student> pageResult = studentRepository.findAll(PageRequest.of(page, size));
            return ResponseEntity.ok(pageResult);
        }
        return ResponseEntity.ok(studentRepository.findAll());
    }

    // GET /students/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(studentRepository.findById(id).orElse(null));
    }

    // POST /students
    @PostMapping
    public ResponseEntity<?> addStudent(@RequestBody Student student,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        // Gizzard-style shard routing — ID is required for deterministic sharding
        if (student.getId() == null) {
            return ResponseEntity.badRequest().body("Student ID is required for shard routing.");
        }

        String shard = shardRouter.getShard(student.getId().intValue());

        if (!shard.contains(serverPort)) {
            // Wrong node — forward via RPC to the correct shard node
            System.out.println("[SHARD] Forwarding student id=" + student.getId() + " to " + shard);
            return ResponseEntity.ok(
                    shardForwardService.forward(shard + "/students", student)
            );
        }

        System.out.println("[ROUTER] Student id=" + student.getId() + " handled locally on port " + serverPort);

        // Lamport clock tick before write
        long ts = logicalClock.tick();

        // Acquire distributed lock before saving
        String lockHolder = "node" + serverPort.replace("808", "");
        boolean locked = distributedLock.acquire(lockHolder);
        if (!locked) {
            System.out.println("[Lock] Could not acquire lock — retrying after 100ms");
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            locked = distributedLock.acquire(lockHolder);
        }

        Student saved = studentService.addStudent(student);
        systemLogService.log("Student created: " + saved.getName() + " (id=" + saved.getId() + ") [Lamport=" + ts + "]",
                "Node" + serverPort.replace("808", ""));

        // Release lock after save
        if (locked) distributedLock.release(lockHolder);

        // Replicate to the replica node (ring replication)
        replicationService.replicate(saved);

        return ResponseEntity.ok(saved);
    }

    // PUT /students/{id} — routes to correct shard
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody Student student,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        String shard = shardRouter.getShard(id.intValue());
        if (!shard.contains(serverPort)) {
            System.out.println("[ROUTER] Routing edit student " + id + " → " + shard);
            return ResponseEntity.ok(
                shardForwardService.forwardPut(shard + "/students/" + id, student));
        }

        System.out.println("[ROUTER] Updating student " + id + " locally on port " + serverPort);
        Student existing = studentRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setName(student.getName());
            existing.setEmail(student.getEmail());
            existing.setCourse(student.getCourse());
            return ResponseEntity.ok(studentRepository.save(existing));
        }
        return ResponseEntity.notFound().build();
    }

    // DELETE /students/{id} — routes to correct shard
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (!role.equals("ADMIN")) {
            return ResponseEntity.status(403).build();
        }

        String shard = shardRouter.getShard(id.intValue());
        if (!shard.contains(serverPort)) {
            System.out.println("[ROUTER] Routing delete student " + id + " → " + shard);
            shardForwardService.forwardDelete(shard + "/students/" + id);
            return ResponseEntity.ok().build();
        }

        System.out.println("[ROUTER] Deleting student " + id + " locally on port " + serverPort);
        studentRepository.deleteById(id);
        systemLogService.log("Student deleted: id=" + id, "Node" + serverPort.replace("808", ""));
        return ResponseEntity.ok().build();
    }

    // GET /students/search?name= or ?course=
    @GetMapping("/search")
    public ResponseEntity<?> searchStudents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String course,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String role = getRole(authHeader);
        if (role.equals("STUDENT")) {
            return ResponseEntity.status(403).build();
        }

        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(studentRepository.findByNameContainingIgnoreCase(name));
        }
        if (course != null && !course.isBlank()) {
            return ResponseEntity.ok(studentRepository.findByCourseContainingIgnoreCase(course));
        }
        return ResponseEntity.ok(studentRepository.findAll());
    }

    // GET /students/me
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@RequestHeader(value = "X-User-Email") String email) {
        Optional<Student> studentOpt = studentRepository.findByEmail(email);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Student not found");
        }
        return ResponseEntity.ok(studentOpt.get());
    }

    // GET /students/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<?> getMyDashboard(@RequestHeader(value = "X-User-Email") String email) {
        java.util.Map<String, Object> dashboard = studentService.getStudentDashboard(email);
        if (dashboard == null) {
            return ResponseEntity.status(404).body("Student not found");
        }
        return ResponseEntity.ok(dashboard);
    }

    // GET /students/my-courses
    @GetMapping("/my-courses")
    public ResponseEntity<?> getMyCourses(@RequestHeader(value = "X-User-Email") String email) {
        Optional<Student> studentOpt = studentRepository.findByEmail(email);
        if (studentOpt.isEmpty())
            return ResponseEntity.status(404).body("Student not found");
        List<com.student.records.domain.Enrollment> enrollments = enrollmentRepository
                .findByStudentAndStatus(studentOpt.get(), com.student.records.domain.EnrollmentStatus.APPROVED);
        List<java.util.Map<String, Object>> response = enrollments.stream().map(e -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", e.getCourse().getId());
            map.put("courseName", e.getCourse().getCourseName());
            map.put("instructor", e.getCourse().getInstructor());
            map.put("credits", e.getCourse().getCredits());
            return map;
        }).toList();
        return ResponseEntity.ok(response);
    }

    // GET /students/my-attendance
    @GetMapping("/my-attendance")
    public ResponseEntity<?> getMyAttendanceFormatted(@RequestHeader(value = "X-User-Email") String email) {
        Optional<Student> studentOpt = studentRepository.findByEmail(email);
        if (studentOpt.isEmpty())
            return ResponseEntity.status(404).body("Student not found");
        List<com.student.records.domain.Attendance> attendances = attendanceService
                .getAttendanceByStudent(studentOpt.get().getId());

        java.util.Map<String, java.util.Map<String, Object>> courseMap = new java.util.HashMap<>();
        for (com.student.records.domain.Attendance a : attendances) {
            String cName = a.getCourse().getCourseName();
            courseMap.putIfAbsent(cName,
                    new java.util.HashMap<>(java.util.Map.of("course", cName, "present", 0, "total", 0)));
            java.util.Map<String, Object> stats = courseMap.get(cName);
            stats.put("total", (int) stats.get("total") + 1);
            if (a.getStatus() == com.student.records.domain.AttendanceStatus.PRESENT) {
                stats.put("present", (int) stats.get("present") + 1);
            }
        }

        List<java.util.Map<String, Object>> response = courseMap.values().stream().map(stats -> {
            int present = (int) stats.get("present");
            int total = (int) stats.get("total");
            stats.put("percentage", total == 0 ? 100 : (present * 100 / total));
            return stats;
        }).toList();

        return ResponseEntity.ok(response);
    }

    // GET /students/my-grades
    @GetMapping("/my-grades")
    public ResponseEntity<?> getMyGradesFormatted(@RequestHeader(value = "X-User-Email") String email) {
        Optional<Student> studentOpt = studentRepository.findByEmail(email);
        if (studentOpt.isEmpty())
            return ResponseEntity.status(404).body("Student not found");
        List<com.student.records.domain.Grades> grades = gradesService.getGradesByStudent(studentOpt.get().getId());
        List<java.util.Map<String, Object>> response = grades.stream().map(g -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("course", g.getCourse().getCourseName());
            map.put("marks", g.getMarks());
            map.put("grade", g.getGrade());
            return map;
        }).toList();
        return ResponseEntity.ok(response);
    }

    @Autowired
    private com.student.records.repository.CourseRepository courseRepository;

    @GetMapping("/available-courses")
    public ResponseEntity<?> getAvailableCourses(@RequestHeader(value = "X-User-Email") String email) {
        Optional<Student> studentOpt = studentRepository.findByEmail(email);
        if (studentOpt.isEmpty())
            return ResponseEntity.status(404).body("Student not found");

        List<com.student.records.domain.Course> allCourses = courseRepository.findAll();
        List<com.student.records.domain.Enrollment> myEnrollments = enrollmentRepository
                .findByStudentId(studentOpt.get().getId());

        java.util.Set<Long> excludedCourseIds = myEnrollments.stream()
                .filter(e -> e.getStatus() == com.student.records.domain.EnrollmentStatus.PENDING
                        || e.getStatus() == com.student.records.domain.EnrollmentStatus.APPROVED)
                .map(e -> e.getCourse().getId())
                .collect(java.util.stream.Collectors.toSet());

        List<java.util.Map<String, Object>> response = allCourses.stream()
                .filter(c -> !excludedCourseIds.contains(c.getId()))
                .map(c -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", c.getId());
                    map.put("courseName", c.getCourseName());
                    return map;
                }).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/faculty-students")
    public ResponseEntity<?> getStudentsForFaculty(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String role = getRole(authHeader);
        if (role.equals("STUDENT")) {
            return ResponseEntity.status(403).build();
        }

        List<com.student.records.domain.Student> allStudents = studentRepository.findAll();
        List<java.util.Map<String, Object>> response = allStudents.stream().map(s -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getName());
            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }
}
