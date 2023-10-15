package com.stc.inspireu.services.impl;

import com.stc.inspireu.authorization.Roles;
import com.stc.inspireu.beans.CurrentUserObject;
import com.stc.inspireu.beans.MailMetadata;
import com.stc.inspireu.dtos.*;
import com.stc.inspireu.enums.Constant;
import com.stc.inspireu.enums.EmailKey;
import com.stc.inspireu.exceptions.CustomRunTimeException;
import com.stc.inspireu.exceptions.ItemNotFoundException;
import com.stc.inspireu.mappers.UserMapper;
import com.stc.inspireu.models.Role;
import com.stc.inspireu.models.User;
import com.stc.inspireu.repositories.RoleRepository;
import com.stc.inspireu.repositories.UserRepository;
import com.stc.inspireu.services.NotificationService;
import com.stc.inspireu.services.UserService;
import com.stc.inspireu.utils.FileAdapter;
import com.stc.inspireu.utils.PasswordUtil;
import com.stc.inspireu.utils.ResponseWrapper;
import com.stc.inspireu.utils.RoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordUtil passwordUtil;
    private final UserRepository userRepository;
    private final FileAdapter fileAdapter;
    private final RoleRepository roleRepository;
    private final NotificationService notificationService;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public Object changePassword(CurrentUserObject currentUserObject, ChangePasswordDto changePasswordDto) {
        User user = userRepository.findByEmailAndInvitationStatus(currentUserObject.getEmail(),
            Constant.REGISTERED.toString());
        if (passwordUtil.verifyPassword(changePasswordDto.getOldPassword(), user.getPassword())) {
            String hashed1 = passwordUtil.getHashedPassword(changePasswordDto.getConfirmPassword());
            user.setPassword(hashed1);
            userRepository.save(user);
            return user;
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public Object updateNotification(CurrentUserObject currentUserObject,
                                     @Valid UpdateNotificationDto updateNotificationDto) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent()) {
            user.get().setEnableEmail(updateNotificationDto.getEnableEmail());
            user.get().setEnableWeb(updateNotificationDto.getEnableWeb());
            userRepository.save(user.get());
        }
        return user;
    }

    @Transactional
    @Override
    public Object getNotification(CurrentUserObject currentUserObject) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        Map<String, Boolean> data = null;
        if (user.isPresent()) {
            data = new HashMap<>();
            data.put("enableEmail", user.get().getEnableEmail());
            data.put("enableWeb", user.get().getEnableWeb());
        }
        return data;
    }

    @Transactional
    @Override
    public GetManagementProfileDto getManagementMyProfile(CurrentUserObject currentUserObject) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        if (user.isPresent()) {
            GetManagementProfileDto getManagementProfileDto = new GetManagementProfileDto();
            getManagementProfileDto.setId(user.get().getId());
            getManagementProfileDto.setMemberName(user.get().getAlias());
            getManagementProfileDto.setRegistratedEmailAddress(user.get().getEmail());
            getManagementProfileDto.setPhoneNumber(user.get().getPhoneNumber());
            getManagementProfileDto.setPhoneCountryCodeIso2(user.get().getPhoneCountryCodeIso2());
            getManagementProfileDto.setPhoneDialCode(user.get().getPhoneDialCode());
            getManagementProfileDto.setMemberName(user.get().getAlias());
            getManagementProfileDto.setProfilePic(user.get().getProfilePic());
            getManagementProfileDto.setSignaturePic(user.get().getSignaturePic());
            return getManagementProfileDto;
        } else {
            return null;
        }
    }

    @Transactional
    @Override
    public User updateManagementMyProfile(CurrentUserObject currentUserObject,
                                          ManagementProfileDto managementProfileDto) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        String profilePicPath = null;
        String signaturePath = null;
        if (!user.getEmail().equals(managementProfileDto.getRegistratedEmailAddress())) {
            User u = userRepository.findByEmail(managementProfileDto.getRegistratedEmailAddress());
            if (u != null) {
                return null;
            }
        }
        if (managementProfileDto.getProfilePic() != null) {
            profilePicPath = fileAdapter.saveProfilePic(currentUserObject.getUserId(),
                managementProfileDto.getProfilePic());
        }
        if (managementProfileDto.getSignature() != null) {
            signaturePath = fileAdapter.saveSignaturePic(currentUserObject.getUserId(),
                managementProfileDto.getSignature());
        }
        user.setEmail(managementProfileDto.getRegistratedEmailAddress() != null
            ? managementProfileDto.getRegistratedEmailAddress()
            : currentUserObject.getEmail());
        user.setAlias(managementProfileDto.getMemberName());
        user.setPhoneNumber(managementProfileDto.getPhoneNumber());
        user.setPhoneCountryCodeIso2(managementProfileDto.getPhoneCountryCodeIso2());
        user.setPhoneDialCode(managementProfileDto.getPhoneDialCode());
        user.setAlias(managementProfileDto.getMemberName());
        if (Objects.nonNull(profilePicPath)) {
            user.setProfilePic(profilePicPath);
        }
        if (Objects.nonNull(signaturePath)) {
            user.setSignaturePic(signaturePath);
        }
        userRepository.save(user);
        notificationService.updateProfileCardNotification(user);
        return user;
    }

    @Transactional
    @Override
    public ResponseEntity<Object> updateSignature(CurrentUserObject currentUserObject, MultipartFile signaturePic) {
        User user = userRepository.findById(currentUserObject.getUserId()).orElseThrow(() -> ItemNotFoundException.builder("User").build());
        String signaturePath = null;
        if (signaturePic != null) {
            signaturePath = fileAdapter.saveSignaturePic(currentUserObject.getUserId(), signaturePic);
        }
        if (signaturePath != null) {
            user.setSignaturePic(signaturePath);
        }
        User result = userRepository.save(user);
        return (Objects.nonNull(signaturePath) && signaturePath.equalsIgnoreCase(result.getSignaturePic()))
            ? ResponseWrapper.response("signature updated", HttpStatus.OK)
            : ResponseWrapper.response("signature updating failed ", HttpStatus.EXPECTATION_FAILED);
    }

    @Transactional
    @Override
    public Page<GetUserDto> getUsers(Pageable paging) {
        return userRepository.getAllUsers(paging);
    }

    @Override
    @Transactional
    public User getUserRoles(long l) {
        return userRepository.getUserRole(l);
    }

    @Override
    @Transactional
    public Object userUpdateMangement(UserUpdateDto userUpdateDto, Long userId) {
        return userRepository.findById(userId)
            .map(user -> {
                if (!userUpdateDto.getAlias().isEmpty()) {
                    user.setAlias(userUpdateDto.getAlias());
                }
                if (!userUpdateDto.getPhoneDialCode().isEmpty() && !userUpdateDto.getPhoneNumber().isEmpty()) {
                    user.setPhoneDialCode(userUpdateDto.getPhoneDialCode());
                    user.setPhoneCountryCodeIso2(userUpdateDto.getPhoneCountryCodeIso2());
                    user.setPhoneNumber(userUpdateDto.getPhoneNumber());
                }
                Optional<Role> role = roleRepository.findById(userUpdateDto.getRoleId());
                if (role.isPresent()) {
                    if (user.getRole().getRoleName().equals(Roles.ROLE_SUPER_ADMIN.name())
                        && !role.get().getRoleName().equals(Roles.ROLE_SUPER_ADMIN.name()))
                        throw new CustomRunTimeException("You can't change role of a super admin");
                    List<String> excludedRoles = new ArrayList<>();
                    excludedRoles.add(Roles.ROLE_SUPER_ADMIN.name());
                    excludedRoles.add(Roles.ROLE_STARTUPS_MEMBER.name());
                    excludedRoles.add(Roles.ROLE_STARTUPS_ADMIN.name());
                    if (excludedRoles.contains(role.get().getRoleName()) &&
                        !excludedRoles.contains(user.getRole().getRoleName()))
                        throw new CustomRunTimeException("You can't update a user as " + role.get().getRoleName());
                    user.setRole(role.get());
                }
                userRepository.save(user);

                return (Map<String, Object>) new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;

                    {
                        put("userId", user.getId());
                        put("registratedEmailAddress", user.getEmail());
                    }
                };
            }).orElse(null);
    }

    @Transactional
    @Override
    public Page<GetUserDto> searchUsers(String filterBy, String filterKeyword, Pageable paging) {
        Page<User> ls = userRepository.searchUsers(filterBy, filterKeyword, paging);
        return ls.map(userMapper::toGetUserDto);
    }

    @Transactional
    @Override
    public GetUserDto getUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        return user.map(userMapper::toGetUserDto).orElse(null);
    }

    @Transactional
    @Override
    public User toggleUserStatus(Long userId, String sts) {
        return userRepository.findById(userId)
            .map(user -> {
                user.setInvitationStatus(sts);
                userRepository.save(user);
                MailMetadata mailMetadata = new MailMetadata();
                Map<String, Object> props = new HashMap<>();
                props.put("toMail", user.getEmail());
                mailMetadata.setFrom("");
                mailMetadata.setTo(user.getEmail());
                mailMetadata.setProps(props);
                mailMetadata.setSubject("user unblocked");
                mailMetadata.setTemplateFile(EmailKey.user_unblocked.toString());
                notificationService.sendMailForUnblockedUsersByAdmin(mailMetadata, sts.equals(Constant.BLOCKED.toString()));
                return user;
            }).orElse(null);
    }

    @Transactional
    @Override
    public User findByEmailAndInvitationStatus(String registratedEmailAddress, String sts) {
        return userRepository.findByEmailAndInvitationStatus(registratedEmailAddress, sts);
    }

    @Transactional
    @Override
    public List<Object> dropdownTrainers(String string) {
        List<Object> list1 = new ArrayList<>();
        List<User> list = userRepository
            .findByRole_RoleNameAndStartupIdIsNull(RoleName.Value.ROLE_COACHES_AND_TRAINERS.toString());
        list.forEach(t -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", t.getId());
            map.put("name", t.getAlias());
            list1.add(map);
        });
        return list1;
    }

    @Transactional
    @Override
    public Object userContext(CurrentUserObject currentUserObject) {
        Optional<User> user = userRepository.findById(currentUserObject.getUserId());
        Map<String, Object> data = null;
        if (user.isPresent()) {
            data = new HashMap<>();
            data.put("userId", user.get().getId());
            data.put("userName", user.get().getAlias());
            data.put("email", user.get().getEmail());
            data.put("startupId", null);
            data.put("startupName", null);
            if (user.get().getStartup() != null) {
                data.put("startupId", user.get().getStartup().getId());
                data.put("startupName", user.get().getStartup().getStartupName());
            }
        }
        return data;
    }

    @Override
    public User findUserByEmailId(String email) {
        return userRepository.findByEmail(email);
    }

}
