package de.raidcraft.permissions.tables;


import com.avaje.ebean.validation.NotNull;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * @author Dragonfire
 */
@Data
@Entity
@Table(name = "rc_permission_group_member")
public class TPermissionGroupMember {
    @Id
    private int id;
    @NotNull
    private UUID player;
    // This is dirty, better is to have a TGroup,
    // but I want to keep it simple and readable [DR]
    @NotNull
    @Column(name="group_")
    private String group;
    private String world;
}
