package de.raidcraft.permissions.tables;

import com.avaje.ebean.validation.NotNull;
import lombok.Data;

import javax.persistence.*;

/**
 * @author Dragonfire
 */
@Data
@Entity
@Table(name = "rc_permission")
public class TPermission {
    @Id
    private int id;
    @NotNull
    @Column(name="group_")
    private String group;
    @NotNull
    private String permission;
    private String world;
    @Lob
    private String comment;
}
