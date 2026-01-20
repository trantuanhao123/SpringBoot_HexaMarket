package com.hexamarket.code.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.hexamarket.code.dto.request.UserCreationRequest;
import com.hexamarket.code.dto.request.UserUpdateRequest;
import com.hexamarket.code.dto.response.UserResponse;
import com.hexamarket.code.entity.Role;
import com.hexamarket.code.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
	// Roles sẽ xử lý logic riêng nên sẽ không map vào
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "isActive", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "roles", ignore = true)
	User toUser(UserCreationRequest userRequest);

	@Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoles")
	@Mapping(target = "isActive", ignore = true)
	UserResponse toUserResponse(User user);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "username", ignore = true)
	@Mapping(target = "password", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "roles", ignore = true)
	void updateUser(@MappingTarget User user, UserUpdateRequest userRequest);

	@Named("mapRoles")
	default Set<String> mapRoles(Set<Role> roles) {
		if (roles == null) {
			return Set.of();
		}
		return roles.stream().map(Role::getName).collect(Collectors.toSet());
	}
}
