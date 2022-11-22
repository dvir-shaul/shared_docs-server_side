package docSharing.service;

import docSharing.entity.User;
import docSharing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Permission;
import java.sql.SQLDataException;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }

    public void givePermission(Long docId, Long userId, Permission permission){

    }

}
