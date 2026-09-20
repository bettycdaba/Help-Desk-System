package com.helpdesk.helpdesk_backend.config;

import com.helpdesk.helpdesk_backend.entity.Department;
import com.helpdesk.helpdesk_backend.entity.Role;
import com.helpdesk.helpdesk_backend.repository.DepartmentRepository;
import com.helpdesk.helpdesk_backend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedDepartments();
    }

    private void seedRoles() {
        List<String> defaultRoles = List.of("EMPLOYEE", "SUPPORT_OFFICER", "SUPERVISOR", "ADMIN");
        for (String roleName : defaultRoles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(roleName + " role")
                        .active(true)
                        .build();
                roleRepository.save(role);
                log.info("Seeded default role: {}", roleName);
            }
        }
    }

    private void seedDepartments() {
        if (departmentRepository.count() == 0) {
            List<String> defaultDepartments = List.of(
                    "IT Support",
                    "Human Resources",
                    "Finance & Accounting",
                    "Customer Support",
                    "Operations",
                    "Core Banking"
            );

            for (String deptName : defaultDepartments) {
                Department dept = Department.builder()
                        .name(deptName)
                        .description(deptName + " Department")
                        .active(true)
                        .build();
                departmentRepository.save(dept);
                log.info("Seeded default department: {}", deptName);
            }
        }
    }
}
