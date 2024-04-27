package com.cranker.cranker.user;

import com.cranker.cranker.exception.ResourceNotFoundException;
import com.cranker.cranker.friendship.*;
import com.cranker.cranker.profile_pic.model.ProfilePicture;
import com.cranker.cranker.profile_pic.payload.PictureDTO;
import com.cranker.cranker.profile_pic.payload.PictureMapper;
import com.cranker.cranker.profile_pic.repository.ProfilePictureRepository;
import com.cranker.cranker.user.payload.UserDTO;
import com.cranker.cranker.user.payload.UserRequestDTO;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final ProfilePictureRepository pictureRepository;
    private final Logger logger = LogManager.getLogger(this);
    private final FriendshipRepository friendshipRepository;

    public UserDTO retrieveUserInfo(String email) {
        User user = repository.findUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        logger.info("Retrieving user info for User: {}", user.getEmail());
        return UserResponseMapper.INSTANCE.entityToDTO(user);
    }

    public UserRequestDTO changeUserPersonalInfo(String email, UserRequestDTO requestDTO) {
        User user = repository.findUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        UserResponseMapper.INSTANCE.updateUserFromDto(requestDTO, user);
        logger.info("Updating User's personal info for User: {}", user.getEmail());
        return UserResponseMapper.INSTANCE.entityToRequestDTO(repository.save(user));
    }

    public List<PictureDTO> retrieveUserProfilePictures(String email) {
        User user = repository.findUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        logger.info("Retrieving user profile pictures for: {}", email);
        return PictureMapper.INSTANCE.entityToDTO(user.getProfilePictures());
    }

    public UserDTO changeUserProfilePicture(String email, Long pictureId) {
        User user = repository.findUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        ProfilePicture picture = pictureRepository.findById(pictureId)
                .orElseThrow(() -> new ResourceNotFoundException("Picture", "Id", pictureId));

        user.setSelectedPic(picture);
        logger.info("Set  profile picture: {} for user: {}", picture.getName(),  email);
        return UserResponseMapper.INSTANCE.entityToDTO(repository.save(user));
    }

    public FriendshipResponse retrieveUserFriends(String email, int pageNo, int pageSize, String sortBy, String sortDir) {
        User user = repository.findUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Friendship> friendshipPage = friendshipRepository.findAllFriendsByUserId(user.getId(), pageable);

        List<FriendshipDTO> friendships = new ArrayList<>();

        for (Friendship friendship : friendshipPage) {
            if (friendship.getUser().getId().equals(user.getId())) {
                friendships.add(FriendshipMapper.INSTANCE.friendshipToFriendshipDTOUserVersion(friendship));
            }  else {
                friendships.add(FriendshipMapper.INSTANCE.friendshipToFriendshipDTOFriendVersion(friendship));
            }
        }

        logger.info("Retrieving friends for: {}", email);
        return FriendshipMapper.INSTANCE.pageToFriendshipResponse(friendshipPage, friendships);
    }
}
