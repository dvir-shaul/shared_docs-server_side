package docSharing.service;

import docSharing.entity.User;
import docSharing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLDataException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    public User findById(Integer id){
        return userRepository.findById(id);
    }
}
