package ajou.aim_be.department.controller;

import ajou.aim_be.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Department", description = "학과 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "전체 학과 조회", description = "사용자 정보에 등록된 학과 목록을 조회합니다.")
    @GetMapping
    public List<String> getDepartments() {
        return departmentService.getDepartments();
    }
}