package docSharing.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Set;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "change_list")
public class ChangeList {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
}
