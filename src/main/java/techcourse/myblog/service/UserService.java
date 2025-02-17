package techcourse.myblog.service;

import org.springframework.stereotype.Service;
import techcourse.myblog.domain.exception.UserArgumentException;
import techcourse.myblog.domain.user.User;
import techcourse.myblog.domain.user.UserRepository;
import techcourse.myblog.service.dto.ArticleDto;
import techcourse.myblog.service.dto.UserPublicInfoDto;
import techcourse.myblog.service.dto.UserRequestDto;
import techcourse.myblog.service.dto.UserSessionDto;
import techcourse.myblog.service.exception.NotFoundUserException;
import techcourse.myblog.service.exception.SignUpException;
import techcourse.myblog.service.exception.UserDeleteException;
import techcourse.myblog.service.exception.UserUpdateException;

import javax.transaction.Transactional;
import java.util.List;

import static techcourse.myblog.domain.exception.UserArgumentException.EMAIL_DUPLICATION_MESSAGE;
import static techcourse.myblog.domain.exception.UserArgumentException.PASSWORD_CONFIRM_FAIL_MESSAGE;

@Service
@Transactional
public class UserService {
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(NotFoundUserException::new);
    }

    User findByUserSession(UserSessionDto userSessionDto) {
        return findById(userSessionDto.getId());
    }

    public UserPublicInfoDto findUserPublicInfoById(Long id) {
        User user = findById(id);
        return new UserPublicInfoDto(user.getId(), user.getName(), user.getEmail());
    }

    public UserPublicInfoDto findUserPublicInfoByArticle(ArticleDto article) {
        User user = findById(article.getUserId());
        return new UserPublicInfoDto(user.getId(), user.getName(), user.getEmail());
    }

    public User save(UserRequestDto userRequestDto) {
        try {
            validate(userRequestDto);
            return userRepository.save(userRequestDto.toEntity());
        } catch (UserArgumentException e) {
            throw new SignUpException(e.getMessage());
        }
    }

    private void validate(UserRequestDto userRequestDto) {
        checkDuplicatedEmail(userRequestDto.getEmail());
        checkPasswordConfirm(userRequestDto);
    }

    private void checkDuplicatedEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserArgumentException(EMAIL_DUPLICATION_MESSAGE);
        }
    }

    private void checkPasswordConfirm(UserRequestDto userRequestDto) {
        if (!userRequestDto.confirmPassword()) {
            throw new UserArgumentException(PASSWORD_CONFIRM_FAIL_MESSAGE);
        }
    }

    public UserPublicInfoDto update(Long userId, UserRequestDto userRequestDto) {
        try {
            User user = findById(userId);
            user.updateName(userRequestDto.getName());
            return new UserPublicInfoDto(user.getId(), user.getName(),user.getEmail());
        } catch (NotFoundUserException | UserArgumentException e) {
            throw new UserUpdateException(e.getMessage());
        }
    }

    public UserPublicInfoDto delete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserDeleteException("Not Found User"));
        userRepository.delete(user);
        return new UserPublicInfoDto(user.getId(), user.getName(),user.getEmail());
    }
}
