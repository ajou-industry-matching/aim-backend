package ajou.aim_be.department.service;

import ajou.aim_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final UserRepository userRepository;

    public List<String> getDepartments() {
        return userRepository.findDistinctDepartments()
                .stream()
                .filter(department -> department != null && !department.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .sorted()
                .toList();
    }
}