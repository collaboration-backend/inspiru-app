package com.stc.inspireu.controllers;

import com.stc.inspireu.annotations.Authorize;
import com.stc.inspireu.dtos.GetUserDto;
import com.stc.inspireu.dtos.UserUpdateDto;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.models.Role;
import com.stc.inspireu.services.RoleService;
import com.stc.inspireu.services.UserService;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.RoleName;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.MethodHandles;
import java.util.*;

@RestController
@RequestMapping("/api/${api.version}/management")
@RequiredArgsConstructor
public class UserManagementController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserService userService;

    private final RoleService roleService;

    @GetMapping("users")
    public ResponseEntity<Object> getUsers(@RequestParam(defaultValue = "0") Integer pageNo,
                                           @RequestParam(defaultValue = "50") Integer pageSize,
                                           @RequestParam(defaultValue = "asc") String sortDir,
                                           @RequestParam(defaultValue = "email") String sortBy,
                                           @RequestParam(defaultValue = "email") String filterBy,
                                           @RequestParam(defaultValue = "") String filterKeyword) {

        List<String> fields = new ArrayList<String>(
            Arrays.asList("email", "alias", "invitationStatus", "createdOn", "phoneNumber"));

        Pageable paging = PageRequest.of(pageNo, pageSize);

        if (fields.contains(sortBy)) {
            paging = PageRequest.of(pageNo, pageSize,
                sortDir.equals("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending());
        }

        Page<GetUserDto> ls = userService.searchUsers(fields.contains(filterBy) ? filterBy : "", filterKeyword, paging);

        return ResponseWrapper.response(ls);

    }

    @GetMapping("roles")
    public ResponseEntity<Object> getRoles(@RequestParam(defaultValue = "0") Integer pageNo,
                                           @RequestParam(defaultValue = "50") Integer pageSize,
                                           @RequestParam(defaultValue = "asc") String sortDir,
                                           @RequestParam(defaultValue = "") String name) {

        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by("roleName").ascending());

        if (sortDir.equals("desc")) {
            paging = PageRequest.of(pageNo, pageSize, Sort.by("roleName").descending());
        }

        Page<Role> roles = roleService.findAllRoles(paging);

        return ResponseWrapper.response(roles);
    }

    @GetMapping("users/{userId}")
    public ResponseEntity<Object> getUsers(@PathVariable Long userId) {
        GetUserDto user = userService.getUser(userId);
        return ResponseWrapper.response(user);
    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN})
    @PutMapping("users/{userId}/{isBlock}")
    public ResponseEntity<Object> toggleUserStatus(@PathVariable Long userId, @PathVariable Boolean isBlock) {

        Map<String, Object> result = new HashMap<>();

        if (Boolean.TRUE.equals(isBlock)) {
            result.put("status", Constant.BLOCKED.toString());
        } else {
            result.put("status", Constant.REGISTERED.toString());
        }

        return ResponseWrapper.response(result);

    }

    @Authorize(roles = {RoleName.ROLE_SUPER_ADMIN, RoleName.ROLE_MANAGEMENT_TEAM_ADMIN})
    @PutMapping("users/{userId}")
    public ResponseEntity<Object> userUpdateMangement(@RequestBody UserUpdateDto userUpdateDto,
                                                      BindingResult bindingResult,
                                                      @PathVariable Long userId) {

        Object data = userService.userUpdateMangement(userUpdateDto, userId);

        if (data instanceof String) {
            return ResponseWrapper.response400("invalid " + data, (String) data);
        }

        return ResponseWrapper.response("user updated");

    }

    @GetMapping("roles/{roleId}")
    public ResponseEntity<Object> getRole(@PathVariable Long roleId) {
        Optional<Role> role = roleService.findRoleById(roleId);
        return ResponseWrapper.response(role);
    }

}
