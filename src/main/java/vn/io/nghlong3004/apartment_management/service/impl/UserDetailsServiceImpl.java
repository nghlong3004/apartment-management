package vn.io.nghlong3004.apartment_management.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.io.nghlong3004.apartment_management.exception.ErrorState;
import vn.io.nghlong3004.apartment_management.exception.ResourceException;
import vn.io.nghlong3004.apartment_management.model.User;
import vn.io.nghlong3004.apartment_management.model.UserPrincipal;
import vn.io.nghlong3004.apartment_management.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Long id = Long.valueOf(username);
		log.debug("Loading user by id={}", id);

		User user = userRepository.findById(id).orElseThrow(() -> new ResourceException(ErrorState.NOT_FOUND));

		return UserPrincipal.from(user);
	}

}
